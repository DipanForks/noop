package com.noop.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class FlameSkinTest {

    @Test fun theCatalogHasExactlyNineEntriesInAuraOrder() {
        val ids = FlameSkin.catalog.map { it.id }
        assertEquals(
            listOf("sunset", "citrus", "ocean", "jade", "rose", "ice", "gold", "aurora", "void"),
            ids,
        )
    }

    @Test fun namedFallsBackToEmber() {
        assertSame(FlameSkin.ember, FlameSkin.named(null))
        assertSame(FlameSkin.ember, FlameSkin.named("ember"))
        assertSame(FlameSkin.ember, FlameSkin.named("bogus"))
    }

    @Test fun emberIsCommonAndNotInCatalog() {
        assertEquals(GoalRewards.Tier.COMMON, FlameSkin.ember.rarity)
        assertEquals("ember", FlameSkin.ember.id)
        assertEquals("Ember", FlameSkin.ember.name)
        val ids = FlameSkin.catalog.map { it.id }.toSet()
        org.junit.Assert.assertFalse(ids.contains("ember"))
    }

    @Test fun catalogTiersMatchAura() {
        assertEquals(GoalRewards.Tier.COMMON, FlameSkin.catalog[0].rarity) // sunset
        assertEquals(GoalRewards.Tier.COMMON, FlameSkin.catalog[1].rarity) // citrus
        assertEquals(GoalRewards.Tier.RARE, FlameSkin.catalog[2].rarity)   // ocean
        assertEquals(GoalRewards.Tier.RARE, FlameSkin.catalog[3].rarity)   // jade
        assertEquals(GoalRewards.Tier.RARE, FlameSkin.catalog[4].rarity)   // rose
        assertEquals(GoalRewards.Tier.EPIC, FlameSkin.catalog[5].rarity)   // ice
        assertEquals(GoalRewards.Tier.EPIC, FlameSkin.catalog[6].rarity)   // gold
        assertEquals(GoalRewards.Tier.LEGENDARY, FlameSkin.catalog[7].rarity) // aurora
        assertEquals(GoalRewards.Tier.LEGENDARY, FlameSkin.catalog[8].rarity) // void
    }

    @Test fun namedReturnsCatalogEntries() {
        FlameSkin.catalog.forEach { skin ->
            assertSame(skin, FlameSkin.named(skin.id))
        }
    }

    @Test fun colorArraysAreTripletAndPair() {
        FlameSkin.catalog.forEach { skin ->
            assertEquals(3, skin.outer.size)
            assertEquals(2, skin.core.size)
        }
        assertEquals(3, FlameSkin.ember.outer.size)
        assertEquals(2, FlameSkin.ember.core.size)
    }
}
