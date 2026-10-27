package com.touchpanel.android

import com.touchpanel.android.data.layout.DefaultLayout
import com.touchpanel.android.domain.widgets.DashboardLayout
import com.touchpanel.android.domain.widgets.GridCollisionResolver
import com.touchpanel.android.domain.widgets.WidgetModel
import com.touchpanel.android.domain.widgets.WidgetType
import org.junit.Assert.*
import org.junit.Test

class GridCollisionResolverTest {

    @Test
    fun testIsIntersecting() {
        // (0,0, 2,2) and (1,1, 2,2) intersect
        assertTrue(GridCollisionResolver.isIntersecting(0, 0, 2, 2, 1, 1, 2, 2))

        // (0,0, 2,2) and (2,0, 2,2) do not intersect (adjacent)
        assertFalse(GridCollisionResolver.isIntersecting(0, 0, 2, 2, 2, 0, 2, 2))

        // (0,0, 2,2) and (0,2, 2,2) do not intersect (adjacent vertical)
        assertFalse(GridCollisionResolver.isIntersecting(0, 0, 2, 2, 0, 2, 2, 2))

        // Separated
        assertFalse(GridCollisionResolver.isIntersecting(0, 0, 2, 2, 4, 3, 2, 1))
    }

    @Test
    fun testCanPlaceWithinBoundaries() {
        val layout = DashboardLayout(
            columns = 8,
            rows = 4,
            pageCount = 2,
            widgets = emptyList()
        )

        // Valid slot
        assertTrue(GridCollisionResolver.canPlace(2, 2, 0, 0, 0, null, layout))
        assertTrue(GridCollisionResolver.canPlace(2, 2, 6, 2, 0, null, layout))

        // Out of right boundary: col 7 + span 2 = 9 > 8
        assertFalse(GridCollisionResolver.canPlace(2, 2, 7, 0, 0, null, layout))

        // Out of bottom boundary: row 3 + span 2 = 5 > 4
        assertFalse(GridCollisionResolver.canPlace(2, 2, 0, 3, 0, null, layout))

        // Negative coordinates
        assertFalse(GridCollisionResolver.canPlace(2, 2, -1, 0, 0, null, layout))
        assertFalse(GridCollisionResolver.canPlace(2, 2, 0, -1, 0, null, layout))
    }

    @Test
    fun testCanPlaceCollisionDetection() {
        val widget1 = WidgetModel(
            id = "w1",
            type = WidgetType.CLOCK,
            title = "Clock",
            gridCol = 0,
            gridRow = 0,
            colSpan = 2,
            rowSpan = 2,
            pageIndex = 0
        )
        val layout = DashboardLayout(columns = 8, rows = 4, pageCount = 2, widgets = listOf(widget1))

        // Overlapping slot on same page
        assertFalse(GridCollisionResolver.canPlace(2, 2, 1, 1, 0, null, layout))

        // Overlapping but excluded widget itself (moving/resizing itself)
        assertTrue(GridCollisionResolver.canPlace(2, 2, 0, 0, 0, "w1", layout))

        // Different page (page 1): should be free!
        assertTrue(GridCollisionResolver.canPlace(2, 2, 0, 0, 1, null, layout))

        // Adjacent slot on same page: free
        assertTrue(GridCollisionResolver.canPlace(2, 2, 2, 0, 0, null, layout))
    }

    @Test
    fun testFindNearestFreeSlot() {
        val w1 = WidgetModel(
            id = "w1",
            type = WidgetType.CLOCK,
            title = "Clock",
            gridCol = 0,
            gridRow = 0,
            colSpan = 2,
            rowSpan = 2,
            pageIndex = 0
        )
        val layout = DashboardLayout(columns = 8, rows = 4, pageCount = 2, widgets = listOf(w1))

        val freeSlot = GridCollisionResolver.findNearestFreeSlot(
            colSpan = 2,
            rowSpan = 2,
            preferredCol = 0,
            preferredRow = 0,
            pageIndex = 0,
            excludeWidgetId = null,
            layout = layout
        )

        assertNotNull(freeSlot)
        // Should find adjacent slot (either (2,0) or (0,2))
        val isAdjacent = (freeSlot!!.first == 2 && freeSlot.second == 0) || (freeSlot.first == 0 && freeSlot.second == 2)
        assertTrue(isAdjacent)
    }

    @Test
    fun testMoveWidgetWithoutCollision() {
        val defaultLayout = DefaultLayout.create()
        val movedLayout = GridCollisionResolver.moveWidget(
            widgetId = "w_clock",
            newCol = 0,
            newRow = 0,
            newPage = 0,
            layout = defaultLayout
        )

        val clock = movedLayout.widgets.find { it.id == "w_clock" }
        assertNotNull(clock)
        assertEquals(0, clock!!.gridCol)
        assertEquals(0, clock.gridRow)
    }

    @Test
    fun testResizeWidgetWithinLimits() {
        val defaultLayout = DefaultLayout.create()
        // Clock starts at col 0, row 0, span 2x2. Next to it at col 2 is Mango Talker.
        // Expanding clock to 3 columns without moving Mango should fail due to collision
        val failResize = GridCollisionResolver.resizeWidget(
            widgetId = "w_clock",
            newColSpan = 3,
            newRowSpan = 2,
            layout = defaultLayout
        )
        assertNull("Should fail when expanding into another widget without relocation", failResize)

        // Resizing down to 2x1 should succeed if descriptor allows
        val validResize = GridCollisionResolver.resizeWidget(
            widgetId = "w_clock",
            newColSpan = 2,
            newRowSpan = 1,
            layout = defaultLayout
        )
        assertNotNull("Should succeed shrinking height", validResize)
        val updatedClock = validResize!!.widgets.find { it.id == "w_clock" }
        assertEquals(1, updatedClock!!.rowSpan)
    }
}
