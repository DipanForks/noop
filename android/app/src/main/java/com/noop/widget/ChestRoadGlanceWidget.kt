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
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
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
 * Home-screen widget: the Chest Road — how many days to the next chest, with the leg's progress
 * as a bar. Renders purely from [WidgetCompanionStore] — one snapshot, three readers, so this
 * panel cannot disagree with the mood or the fire — and tapping opens the app.
 */
class ChestRoadGlanceWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val companion = runCatching { WidgetCompanionStore.load(context) }.getOrNull()
        val dark = WidgetTheme.isDark(context)
        provideContent { ChestRoadWidgetContent(companion, dark) }
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
private fun chestSurface(dark: Boolean) = ColorProvider(if (dark) Color(0xFF0A1322) else Color(0xFFF4F1EA))
private fun chestTextPrimary(dark: Boolean) = ColorProvider(if (dark) Color(0xFFF4F6F8) else Color(0xFF1A2230))
private fun chestTextSecondary(dark: Boolean) = ColorProvider(if (dark) Color(0xFF8A94A4) else Color(0xFF7C8696))

/** The warm tint the chest shares with its siblings: one colour across the trio. */
private fun chestWarm() = ColorProvider(Color(0xFFF4B35E))

/** The bar's fill width, clamped so a tiny share still shows a stub. Pure, so it is unit-testable. */
internal fun chestFillWidthDp(trackWidthDp: Int, share: Double): Int =
    maxOf(12, (trackWidthDp * share).toInt())

/** Card padding, both sides: the track must subtract the same figure or the bar drifts. */
private const val CHEST_CARD_PADDING_TOTAL_DP = 28

/** The chest picture's edge, and the gaps and figure width around the numbers on either side. */
private const val CHEST_IMAGE_DP = 44
private const val CHEST_ROW_GAP_DP = 8
private const val CHEST_START_NUMBER_DP = 10

@Composable
private fun ChestRoadWidgetContent(companion: WidgetCompanion?, dark: Boolean) {
    val widthDp = LocalSize.current.width.value.toInt()
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(chestSurface(dark))
            .cornerRadius(16.dp)
            .padding(14.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        if (companion == null || !companion.ownsRoad ||
            companion.legStart == null || companion.legTarget == null
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                Text(
                    text = uiString(R.string.widget_locked_glyph),
                    style = TextStyle(color = chestTextSecondary(dark), fontSize = 18.sp),
                )
                Text(
                    text = uiString(R.string.widget_chest_name),
                    style = TextStyle(color = chestTextPrimary(dark), fontSize = 13.sp),
                )
                Text(
                    text = uiString(R.string.widget_locked_line),
                    style = TextStyle(color = chestTextSecondary(dark), fontSize = 11.sp),
                )
            }
        } else {
            val legStart = companion.legStart ?: 0
            val legTarget = companion.legTarget ?: 0
            val start = legStart
            Text(
                    text = uiString(R.string.widget_chest_days_left, maxOf(0, legTarget - companion.streakDays)),
                style = TextStyle(color = chestTextPrimary(dark), fontSize = 15.sp),
            )
            Spacer(modifier = GlanceModifier.height(10.dp))
            val numbersWidth = CHEST_IMAGE_DP + (if (start > 0) CHEST_ROW_GAP_DP * 2 + CHEST_START_NUMBER_DP else CHEST_ROW_GAP_DP)
            val trackWidthDp = widthDp - CHEST_CARD_PADDING_TOTAL_DP - numbersWidth
            val share = companion.roadShare()
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
            ) {
                if (start > 0) {
                    Text(
                        text = start.toString(),
                        style = TextStyle(color = chestWarm(), fontSize = 12.sp),
                    )
                    Spacer(modifier = GlanceModifier.width(CHEST_ROW_GAP_DP.dp))
                }
                Row(
                    modifier = GlanceModifier
                        .defaultWeight()
                        .height(12.dp)
                        .background(ColorProvider(Color(0x1FFFFFF)))
                        .cornerRadius(6.dp),
                ) {
                    if (share > 0.0) {
                        Row(
                            modifier = GlanceModifier
                                .width(chestFillWidthDp(trackWidthDp, share).dp)
                                .height(12.dp)
                                .background(chestWarm())
                                .cornerRadius(6.dp),
                        ) { }
                    }
                }
                Spacer(modifier = GlanceModifier.width(CHEST_ROW_GAP_DP.dp))
                Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
                    val chest = picture(LocalContext.current, "chest")
                    if (chest != null) {
                        Image(
                            provider = ImageProvider(chest),
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = GlanceModifier.width(CHEST_IMAGE_DP.dp).height(CHEST_IMAGE_DP.dp),
                        )
                    }
                    Text(
                        text = legTarget.toString(),
                        style = TextStyle(color = chestTextSecondary(dark), fontSize = 12.sp),
                    )
                }
            }
        }
    }
}
