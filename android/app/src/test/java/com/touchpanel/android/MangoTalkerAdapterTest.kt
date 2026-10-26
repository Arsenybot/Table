package com.touchpanel.android

import com.touchpanel.android.integration.mango.MangoTalkerAdapter
import org.junit.Assert.*
import org.junit.Test

class MangoTalkerAdapterTest {

    @Test
    fun testPackageConstants() {
        assertEquals("com.mangotele.mtalker", MangoTalkerAdapter.MANGO_PACKAGE)
        assertTrue(MangoTalkerAdapter.PLAY_STORE_URI.contains("com.mangotele.mtalker"))
        assertTrue(MangoTalkerAdapter.WEB_STORE_URI.startsWith("https://play.google.com/store/apps/details?id="))
    }

    @Test
    fun testCleanPhoneNumberSanitization() {
        fun sanitize(phone: String): String = phone.replace(Regex("[^0-9+]"), "")

        assertEquals("+74952129298", sanitize("+7 (495) 212-92-98"))
        assertEquals("+79161234567", sanitize("+7-916-123-45-67"))
        assertEquals("88005553535", sanitize("8 (800) 555-35-35"))
    }
}
