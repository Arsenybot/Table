package com.touchpanel.android.domain.widgets

import kotlin.math.abs

/**
 * Mobile Grid Collision Resolver.
 * Manages spatial allocation of widgets on an 8-column x 4-row landscape mobile layout
 * across multiple pages. Ensures no accidental overlapping, handles collision relocation,
 * bounds checking, and graceful push/swap.
 */
object GridCollisionResolver {

    const val DEFAULT_COLUMNS = 8
    const val DEFAULT_ROWS = 4
    const val MAX_PAGES = 4

    /**
     * Checks if a widget of given size can fit at (targetCol, targetRow) on pageIndex
     * without intersecting layout boundaries or other widgets.
     */
    fun canPlace(
        colSpan: Int,
        rowSpan: Int,
        targetCol: Int,
        targetRow: Int,
        pageIndex: Int,
        excludeWidgetId: String?,
        layout: DashboardLayout
    ): Boolean {
        val maxCols = layout.columns
        val maxRows = layout.rows

        // Boundary check
        if (targetCol < 0 || targetRow < 0) return false
        if (targetCol + colSpan > maxCols) return false
        if (targetRow + rowSpan > maxRows) return false

        // Intersection check with other widgets on the same page
        for (other in layout.widgets) {
            if (other.id == excludeWidgetId) continue
            if (other.pageIndex != pageIndex) continue

            if (isIntersecting(
                    targetCol, targetRow, colSpan, rowSpan,
                    other.gridCol, other.gridRow, other.colSpan, other.rowSpan
                )
            ) {
                return false
            }
        }
        return true
    }

    /**
     * Checks rectangle intersection
     */
    fun isIntersecting(
        x1: Int, y1: Int, w1: Int, h1: Int,
        x2: Int, y2: Int, w2: Int, h2: Int
    ): Boolean {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2
    }

    /**
     * Finds the nearest unoccupied slot for a widget on a page
     */
    fun findNearestFreeSlot(
        colSpan: Int,
        rowSpan: Int,
        preferredCol: Int,
        preferredRow: Int,
        pageIndex: Int,
        excludeWidgetId: String?,
        layout: DashboardLayout
    ): Pair<Int, Int>? {
        var bestSlot: Pair<Int, Int>? = null
        var minDistance = Int.MAX_VALUE

        val maxCols = layout.columns
        val maxRows = layout.rows

        for (r in 0..maxRows - rowSpan) {
            for (c in 0..maxCols - colSpan) {
                if (canPlace(colSpan, rowSpan, c, r, pageIndex, excludeWidgetId, layout)) {
                    val distance = abs(c - preferredCol) + abs(r - preferredRow)
                    if (distance < minDistance) {
                        minDistance = distance
                        bestSlot = Pair(c, r)
                    }
                }
            }
        }
        return bestSlot
    }

    /**
     * Moves a widget to a new position. If target slot has colliding widgets,
     * attempts to relocate colliding widgets to nearest free slots.
     */
    fun moveWidget(
        widgetId: String,
        newCol: Int,
        newRow: Int,
        newPage: Int,
        layout: DashboardLayout
    ): DashboardLayout {
        val targetWidget = layout.widgets.find { it.id == widgetId } ?: return layout
        val clampedCol = newCol.coerceIn(0, layout.columns - targetWidget.colSpan)
        val clampedRow = newRow.coerceIn(0, layout.rows - targetWidget.rowSpan)
        val clampedPage = newPage.coerceIn(0, layout.pageCount - 1)

        val updatedWidget = targetWidget.copy(
            gridCol = clampedCol,
            gridRow = clampedRow,
            pageIndex = clampedPage
        )

        // Find widgets that collide with the new position
        val remainingWidgets = layout.widgets.filter { it.id != widgetId }.toMutableList()
        val colliding = remainingWidgets.filter { other ->
            other.pageIndex == clampedPage && isIntersecting(
                clampedCol, clampedRow, targetWidget.colSpan, targetWidget.rowSpan,
                other.gridCol, other.gridRow, other.colSpan, other.rowSpan
            )
        }

        if (colliding.isEmpty()) {
            return layout.copy(widgets = remainingWidgets + updatedWidget)
        }

        // Relocate colliding widgets
        val tempLayout = layout.copy(widgets = listOf(updatedWidget))
        val relocatedList = mutableListOf(updatedWidget)

        for (other in remainingWidgets) {
            if (colliding.contains(other)) {
                // Find nearest slot
                val freeSlot = findNearestFreeSlot(
                    other.colSpan, other.rowSpan,
                    other.gridCol, other.gridRow,
                    clampedPage,
                    null,
                    layout.copy(widgets = relocatedList)
                ) ?: findNearestFreeSlot(
                    other.colSpan, other.rowSpan,
                    0, 0,
                    (clampedPage + 1) % layout.pageCount,
                    null,
                    layout.copy(widgets = relocatedList)
                )

                if (freeSlot != null) {
                    relocatedList.add(other.copy(gridCol = freeSlot.first, gridRow = freeSlot.second))
                } else {
                    // Cannot place: keep original
                    relocatedList.add(other)
                }
            } else {
                relocatedList.add(other)
            }
        }

        return layout.copy(widgets = relocatedList)
    }

    /**
     * Resizes a widget. Checks boundary and collision.
     * If valid, updates spans and returns new layout; otherwise returns null.
     */
    fun resizeWidget(
        widgetId: String,
        newColSpan: Int,
        newRowSpan: Int,
        layout: DashboardLayout
    ): DashboardLayout? {
        val target = layout.widgets.find { it.id == widgetId } ?: return null
        val desc = WidgetCatalog.getDescriptor(target.type)

        val clampedColSpan = newColSpan.coerceIn(desc.minColSpan, desc.maxColSpan.coerceAtMost(layout.columns))
        val clampedRowSpan = newRowSpan.coerceIn(desc.minRowSpan, desc.maxRowSpan.coerceAtMost(layout.rows))

        // Check if fits inside grid
        if (target.gridCol + clampedColSpan > layout.columns) return null
        if (target.gridRow + clampedRowSpan > layout.rows) return null

        // Check collisions with other widgets on the same page
        if (!canPlace(clampedColSpan, clampedRowSpan, target.gridCol, target.gridRow, target.pageIndex, widgetId, layout)) {
            return null
        }

        val updated = layout.widgets.map {
            if (it.id == widgetId) it.copy(colSpan = clampedColSpan, rowSpan = clampedRowSpan) else it
        }
        return layout.copy(widgets = updated)
    }
}
