package com.noop.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/** Manifest entry point for the Chest Road widget — all rendering lives in [ChestRoadGlanceWidget]. */
class ChestRoadWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ChestRoadGlanceWidget()
}
