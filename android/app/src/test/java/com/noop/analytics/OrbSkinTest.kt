package com.noop.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class OrbSkinTest {

    @Test fun theCatalogHasExactlySixEntriesInAuraOrder() {
        val ids = OrbSkin.catalog.map { it.id }
        assertEquals(
            listOf("orb-dawn", "orb-lagoon", "orb-orchid", "orb-cinder", "orb-gold", "orb-void"),
            ids,
        )
    }

    @Test fun namedFallsBackToNone() {
        assertNull(OrbSkin.named(null))
        assertNull(OrbSkin.named(""))
        assertNull(OrbSkin.named("bogus"))
    }

    @Test fun catalogHasNoDefaultEntry() {
        val ids = OrbSkin.catalog.map { it.id }.toSet()
        assertFalse(ids.contains("none"))
    }

    @Test fun namedReturnsCatalogEntries() {
        OrbSkin.catalog.forEach { skin ->
            assertSame(skin, OrbSkin.named(skin.id))
        }
    }

    @Test fun colorsAreOpaque0xFFAlpha() {
        OrbSkin.catalog.forEach { skin ->
            assertEquals(0xFF, skin.top.ushr(24) and 0xFF)
            assertEquals(0xFF, skin.bottom.ushr(24) and 0xFF)
        }
    }
}
