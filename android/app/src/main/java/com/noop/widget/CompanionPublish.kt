package com.noop.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.runBlocking
import com.noop.analytics.ActivityStreak
import com.noop.analytics.StreakChallenge
import com.noop.analytics.WidgetCompanion
import com.noop.ui.GoalsStore
import java.time.Instant

object CompanionPublish {
    fun publish(context: Context, reading: ActivityStreak.Reading?, trainedToday: Boolean, leg: StreakChallenge.Challenge?) {
        val store = GoalsStore.load(context)
        val previous = WidgetCompanionStore.load(context)
        // Mirror the Swift publish: when streaks are off, neither the challenge leg nor the
        // reading may reach the companion (leg nil → rare tier, no stamps, zero day count).
        val shown = if (store.streaksEnabled) store.streakChallenge else null
        val streak = if (store.streaksEnabled) reading else null
        val trained = trainedToday || streak?.todayCover == ActivityStreak.Cover.TRAINED
        val now = Instant.now()
        val companion = WidgetCompanion(
            streaksOn = store.streaksEnabled,
            streakDays = streak?.current ?: 0,
            legStart = shown?.legStart,
            legTarget = shown?.target,
            trainedOn = if (trained) now else previous?.trainedOn,
            restOn = if (streak?.todayCover == ActivityStreak.Cover.EARNED_REST) now else previous?.restOn,
            milestoneOn = if (trained && StreakChallenge.ladder.contains(streak?.current ?: 0)) now else previous?.milestoneOn,
            ownsFire = "widget-fire" in store.ownedRewards,
            ownsRoad = "widget-road" in store.ownedRewards,
            biscuitOn = context.getSharedPreferences("noop_prefs", Context.MODE_PRIVATE)
                .getBoolean("noop.ai.biscuit", false),
        )
        val wrote = WidgetCompanionStore.saveIfChanged(context, companion)
        val drew = CompanionPictures.drawPictures(context, leg = shown)
        if (wrote || drew) refreshWidgets(context)
    }

    private fun refreshWidgets(context: Context) {
        runBlocking {
            val mood = runCatching { GlanceAppWidgetManager(context).getGlanceIds(MoodGlanceWidget::class.java) }.getOrDefault(emptyList())
            val fire = runCatching { GlanceAppWidgetManager(context).getGlanceIds(KeepersFireGlanceWidget::class.java) }.getOrDefault(emptyList())
            val road = runCatching { GlanceAppWidgetManager(context).getGlanceIds(ChestRoadGlanceWidget::class.java) }.getOrDefault(emptyList())
            if (mood.isNotEmpty()) runCatching { MoodGlanceWidget().updateAll(context) }
            if (fire.isNotEmpty()) runCatching { KeepersFireGlanceWidget().updateAll(context) }
            if (road.isNotEmpty()) runCatching { ChestRoadGlanceWidget().updateAll(context) }
        }
    }
}
