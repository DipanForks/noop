package com.noop.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/** Manifest entry point for the Keeper's Fire widget — all rendering lives in [KeepersFireGlanceWidget]. */
class KeepersFireWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = KeepersFireGlanceWidget()
}
