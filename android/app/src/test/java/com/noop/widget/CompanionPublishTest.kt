package com.noop.widget

import android.content.Context
import com.noop.analytics.ActivityStreak
import com.noop.analytics.StreakChallenge
import com.noop.ui.GoalsSnapshot
import com.noop.ui.GoalsStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CompanionPublishTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()

    private fun saveGoals(streaksEnabled: Boolean, challenge: StreakChallenge.Challenge?, owned: List<String> = emptyList()) {
        GoalsStore.save(context, GoalsSnapshot(streaksEnabled = streaksEnabled, streakChallenge = challenge, ownedRewards = owned))
    }

    private fun reading(current: Int, cover: ActivityStreak.Cover) = ActivityStreak.Reading(
        days = emptyList(), current = current, longest = current, atRisk = false, todayCover = cover,
        restLeftThisWeek = 2, freezesLeftThisMonth = 1, freezesBanked = 0, since = null,
    )

    @Test
    fun publishStoresLegAndMilestones() {
        val challenge = StreakChallenge.Challenge(7, 0L, null, 0, 6)
        saveGoals(streaksEnabled = true, challenge = challenge, owned = listOf("widget-fire"))
        CompanionPublish.publish(context, reading(7, ActivityStreak.Cover.TRAINED), trainedToday = false, leg = challenge)
        val stored = WidgetCompanionStore.load(context)!!
        assertEquals(6, stored.legStart)
        assertEquals(7, stored.legTarget)
        assertEquals(7, stored.streakDays)
        assertEquals(true, stored.ownsFire)
        assertNotNull(stored.trainedOn)
        assertNotNull(stored.milestoneOn)
    }

    @Test
    fun publishKeepsPreviousStampsWhenNotTrained() {
        val challenge = StreakChallenge.Challenge(7, 0L, null, 0, 6)
        saveGoals(streaksEnabled = true, challenge = challenge)
        CompanionPublish.publish(context, reading(7, ActivityStreak.Cover.TRAINED), trainedToday = false, leg = challenge)
        val first = WidgetCompanionStore.load(context)!!
        CompanionPublish.publish(context, reading(8, ActivityStreak.Cover.NONE), trainedToday = false, leg = challenge)
        val second = WidgetCompanionStore.load(context)!!
        assertEquals(first.trainedOn, second.trainedOn)
        assertEquals(first.milestoneOn, second.milestoneOn)
    }

    @Test
    fun publishStoresZeroStreakWhenStreaksDisabled() {
        saveGoals(streaksEnabled = false, challenge = null)
        CompanionPublish.publish(context, reading(5, ActivityStreak.Cover.TRAINED), trainedToday = false, leg = null)
        val stored = WidgetCompanionStore.load(context)!!
        assertEquals(0, stored.streakDays)
        assertEquals(false, stored.streaksOn)
    }

    @Test
    fun publishIgnoresChallengeAndReadingWhenStreaksDisabled() {
        val challenge = StreakChallenge.Challenge(7, 0L, null, 0, 6)
        saveGoals(streaksEnabled = false, challenge = challenge)
        CompanionPublish.publish(context, reading(7, ActivityStreak.Cover.TRAINED), trainedToday = false, leg = challenge)
        val stored = WidgetCompanionStore.load(context)!!
        assertNull(stored.legStart)
        assertNull(stored.legTarget)
        assertNull(stored.trainedOn)
        assertNull(stored.milestoneOn)
        assertEquals(0, stored.streakDays)
    }
}
