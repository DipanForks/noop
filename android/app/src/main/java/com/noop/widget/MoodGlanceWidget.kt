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
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.noop.R
import com.noop.analytics.WidgetCompanion
import com.noop.ui.MainActivity
import com.noop.ui.uiString
import java.time.ZonedDateTime

/**
 * Home-screen widget: Biscuit's face in the mood the shared companion snapshot says it is right
 * now. Renders purely from [WidgetCompanionStore] — one snapshot, three readers, so this panel
 * cannot disagree with the fire or the chest — and tapping opens the app.
 */
class MoodGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val companion = runCatching { WidgetCompanionStore.load(context) }.getOrNull()
        val dark = WidgetTheme.isDark(context)
        provideContent { MoodWidgetContent(companion, dark) }
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
private fun moodSurface(dark: Boolean) = ColorProvider(if (dark) Color(0xFF0A1322) else Color(0xFFF4F1EA))
private fun moodTextPrimary(dark: Boolean) = ColorProvider(if (dark) Color(0xFFF4F6F8) else Color(0xFF1A2230))
private fun moodTextSecondary(dark: Boolean) = ColorProvider(if (dark) Color(0xFF8A94A4) else Color(0xFF7C8696))

/** The mood's line, resolved from resources: copy assigned inline is invisible to the i18n audit. */
private fun moodLine(mood: String): String = when (mood) {
    "asleep" -> uiString(R.string.widget_mood_asleep)
    "radiant" -> uiString(R.string.widget_mood_radiant)
    "proud" -> uiString(R.string.widget_mood_proud)
    "calm" -> uiString(R.string.widget_mood_calm)
    "worried" -> uiString(R.string.widget_mood_worried)
    else -> uiString(R.string.widget_mood_happy)
}

@Composable
private fun MoodWidgetContent(companion: WidgetCompanion?, dark: Boolean) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(moodSurface(dark))
            .cornerRadius(16.dp)
            .padding(14.dp)
            .clickable(actionStartActivity<MainActivity>()),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        if (companion != null && companion.biscuitOn != true) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                Text(
                    text = uiString(R.string.widget_biscuit_off),
                    style = TextStyle(color = moodTextSecondary(dark), fontSize = 12.sp),
                )
            }
        } else {
            val mood = companion?.mood(ZonedDateTime.now()) ?: "happy"
            val bmp = picture(LocalContext.current, mood)
            if (bmp != null) {
                Image(
                    provider = ImageProvider(bmp),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = GlanceModifier.fillMaxSize().defaultWeight(),
                )
            }
            Text(
                text = moodLine(mood),
                style = TextStyle(color = moodTextSecondary(dark), fontSize = 13.sp),
            )
        }
    }
}
