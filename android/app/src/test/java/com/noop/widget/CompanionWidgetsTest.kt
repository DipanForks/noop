package com.noop.widget

import org.junit.Assert.assertEquals
import org.junit.Test

class CompanionWidgetsTest {
    @Test
    fun fireNumberFontSpSwitchesToLargeSizeOnlyAtFifteenDp() {
        assertEquals(38, fireNumberFontSp(149))
        assertEquals(56, fireNumberFontSp(150))
        assertEquals(56, fireNumberFontSp(300))
    }

    @Test
    fun chestFillWidthDpClampsToMinimumAndFollowsShare() {
        assertEquals(12, chestFillWidthDp(100, 0.0))
        assertEquals(12, chestFillWidthDp(20, 0.5))
        assertEquals(150, chestFillWidthDp(300, 0.5))
        assertEquals(300, chestFillWidthDp(300, 1.0))
    }
}
