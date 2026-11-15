package com.noop.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoopAISwitchesTest {

    @Test fun biscuitOnNeedsBothSwitches() {
        assertTrue(NoopAISwitches.biscuitOn(true, true))
        assertFalse(NoopAISwitches.biscuitOn(true, false))
        assertFalse(NoopAISwitches.biscuitOn(false, true))
        assertFalse(NoopAISwitches.biscuitOn(false, false))
    }

    @Test fun keysLiveInTheSharedNoopPrefsFile() {
        assertEquals("noop.ai.companion", NoopAISwitches.COMPANION_KEY)
        assertEquals("noop.ai.biscuit", NoopAISwitches.BISCUIT_KEY)
    }
}
