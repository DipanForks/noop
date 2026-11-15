package com.noop.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.noop.R
import com.noop.analytics.WidgetCompanion
import com.noop.ui.MainActivity
import com.noop.ui.uiString

/**
 * Home-screen widget: the Keeper's Fire — the flame picture with the streak day number.
 * Renders purely from [WidgetCompanionStore] — one snapshot, three readers, so this panel
 * cannot disagree with the mood or the chest — and tapping opens the app.
 */
class KeepersFireGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val companion = runCatching { WidgetCompanionStore.load(context) }.getOrNull()
        val dark = WidgetTheme.isDark(context)
        provideContent { KeepersFireWidgetContent(companion, dark) }
    }

    /** Same defence as [HrGlanceWidget.onCompositionError]: swap Glance's built-in error layout for
     *  ours. The widget heals on the next successful push. */
    override fun onCompositionError(
        context: Context,
        glanceId: GlanceId,
        appWidgetId: Int,
        throwable: Throwable,
    ) {
        runCatching {
            val rv = android.widget.RemoteViews(context.packageName, R.layout.noop_widget_error)
            android.appwidget.AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, rv)
        }
    }
}

// Local widget colours, mirroring the siblings rather than reading Palette: Glance composes outside the
// app theme, so every widget in this package carries its own copy on purpose.
private fun fireSurface(dark: Boolean) = ColorProvider(if (dark) Color(0xFF0A1322) else Color(0xFFF4F1EA))
private fun fireTextPrimary(dark: Boolean) = ColorProvider(if (dark) Color(0xFFF4F6F8) else Color(0xFF1A2230))
private fun fireTextSecondary(dark: Boolean) = ColorProvider(if (dark) Color(0xFF8A94A4) else Color(0xFF7C8696))

/** The warm tint the flame, the streak label and the chest share: one colour across the trio. */
private fun fireWarm() = ColorProvider(Color(0xFFF4B35E))

/** The streak number's size: the widget may resize wide, so a wide cell earns the bigger figure. */
internal fun fireNumberFontSp(widthDp: Int): Int = if (widthDp >= 150) 56 else 38

@Composable
private fun KeepersFireWidgetContent(companion: WidgetCompanion?, dark: Boolean) {
    val widthDp = LocalSize.current.width.value.toInt()
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(fireSurface(dark))
            .cornerRadius(16.dp)
            .padding(14.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        if (companion == null || !companion.ownsFire) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                Text(
                    text = uiString(R.string.widget_locked_glyph),
                    style = TextStyle(color = fireTextSecondary(dark), fontSize = 18.sp),
                )
                Text(
                    text = uiString(R.string.widget_fire_name),
                    style = TextStyle(color = fireTextPrimary(dark), fontSize = 13.sp),
                )
                Text(
                    text = uiString(R.string.widget_locked_line),
                    style = TextStyle(color = fireTextSecondary(dark), fontSize = 11.sp),
                )
            }
        } else {
            val flame = picture(LocalContext.current, "flame")
            Row(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                if (flame != null) {
                    Image(
                        provider = ImageProvider(flame),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds,
                        modifier = GlanceModifier.defaultWeight(),
                    )
                    Spacer(modifier = GlanceModifier.width(12.dp))
                }
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    horizontalAlignment = if (widthDp >= 150) Alignment.Horizontal.Start
                        else Alignment.Horizontal.CenterHorizontally,
                    verticalAlignment = Alignment.Vertical.CenterVertically,
                ) {
                    Text(
                        text = companion.streakDays.toString(),
                        style = TextStyle(color = fireTextPrimary(dark), fontSize = fireNumberFontSp(widthDp).sp),
                    )
                    Text(
                        text = uiString(R.string.widget_fire_day_streak),
                        style = TextStyle(color = fireWarm(), fontSize = 12.sp),
                    )
                }
            }
        }
    }
}
