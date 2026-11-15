package com.noop.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import com.noop.analytics.BiscuitMood
import com.noop.analytics.BiscuitOutfit
import com.noop.analytics.FlameSkin
import com.noop.analytics.GoalRewards
import com.noop.analytics.StreakChallenge
import com.noop.analytics.WidgetCompanion
import com.noop.ui.BiscuitVectorArt
import java.io.File
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/**
 * Draws the three companion widget pictures once per (face, flame, tier) signature.
 * Mirrors the reference NoopCompanionPublish.drawPictures; the widgets only ever read
 * these PNGs, so all three panels agree by construction.
 */
internal object CompanionPictures {
    const val DIR = "noop_widget_companion"
    private const val SIGNATURE_KEY = "noop.widget.companion.pictures"
    private const val PREFS = "noop_widget"

    fun drawPictures(context: Context, leg: StreakChallenge.Challenge?): Boolean {
        val face = com.noop.ui.GoalsStore.equippedFace(context)
        val flame = com.noop.ui.GoalsStore.equippedFlame(context)
        val tier = leg?.let { GoalRewards.tierForLeg(it.legStart, it.target) } ?: GoalRewards.Tier.RARE
        val signature = "$face|$flame|${tier.ordinal}"
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getString(SIGNATURE_KEY, null) == signature) return false
        val outfits = outfitForFace(face)
        val dir = File(context.filesDir, DIR); dir.mkdirs()
        val art = runCatching { BiscuitVectorArt.load(context) }.getOrNull()
        if (art != null) {
            for (mood in WidgetCompanion.moods) {
                val bmp = Bitmap.createBitmap(320, 320, Bitmap.Config.ARGB_8888)
                art.drawObjects(
                    Canvas(bmp), 320f, 320f,
                    art.scene.still(BiscuitMood.valueOf(mood.uppercase()), outfits),
                )
                save(bmp, File(dir, "companion-$mood.png"))
            }
        }
        val flameBmp = Bitmap.createBitmap(360, 360, Bitmap.Config.ARGB_8888)
        Canvas(flameBmp).apply { drawFlame(this, 260f, 50f, 50f, FlameSkin.named(flame)) }
        save(flameBmp, File(dir, "companion-flame.png"))
        val chestBmp = Bitmap.createBitmap(240, 240, Bitmap.Config.ARGB_8888)
        drawChest(Canvas(chestBmp), 240f, 0f, 0f, tier)
        save(chestBmp, File(dir, "companion-chest.png"))
        prefs.edit().putString(SIGNATURE_KEY, signature).apply()
        return true
    }

    /** Pure, so it is unit-testable. */
    fun outfitForFace(face: String): Set<BiscuitOutfit> = when (face) {
        "face-coach" -> setOf(BiscuitOutfit.COACH)
        "face-glasses" -> setOf(BiscuitOutfit.GLASSES)
        "face-nightcap" -> setOf(BiscuitOutfit.NIGHTCAP)
        "face-crown" -> setOf(BiscuitOutfit.CROWN)
        else -> emptySet()
    }

    /** Deterministic static stand-in for the animated flame shader; fixed time keeps PNGs stable. */
    fun drawFlame(canvas: Canvas, size: Float, x: Float, y: Float, skin: FlameSkin) {
        val frac = { v: Double -> v - floor(v) }
        val tier = skin.rarity
        val epic = tier.ordinal >= GoalRewards.Tier.EPIC.ordinal
        val w = size * if (epic) 0.86f else 0.78f
        val h = size * if (epic) 1.08f else 1.0f
        val cx = x + size / 2f
        val baseY = y + size
        val tipY = y - (h - size) / 2f
        val flameCenterY = (tipY + baseY) / 2f
        val t = 1234.0
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Rays (LEGENDARY), behind the body: 12 wedges of 24° with 6° gaps.
        if (tier == GoalRewards.Tier.LEGENDARY) {
            val radius = size * 0.85f
            val rect = RectF(cx - radius, flameCenterY - radius, cx + radius, flameCenterY + radius)
            paint.style = Paint.Style.FILL
            paint.shader = null
            paint.color = withAlpha(skin.glow, 0.12f)
            for (i in 0 until 12) {
                val path = Path()
                path.moveTo(cx, flameCenterY)
                path.arcTo(rect, i * 30f, 24f)
                path.close()
                canvas.drawPath(path, paint)
            }
        }

        // Orbit dots (LEGENDARY): back half now, front half after the body.
        val orbitCenterY = y + size / 2f + size * 0.05f
        fun orbitDots(front: Boolean) {
            for (i in 0 until 3) {
                val angle = t * 1.4 + i * 2.0 * Math.PI / 3.0
                val sy = sin(angle)
                if (front == (sy > 0.0)) continue
                val dotX = cx + (size * 0.55f) * cos(angle).toFloat()
                val dotY = orbitCenterY + (size * 0.16f) * sy.toFloat()
                paint.color = if (front) skin.core[0] else withAlpha(skin.core[0], 0.45f)
                val r = if (front) size * 0.04f else size * 0.025f
                canvas.drawCircle(dotX, dotY, r, paint)
            }
        }
        orbitDots(front = false)

        // Halo (tier >= EPIC).
        if (epic) {
            paint.style = Paint.Style.STROKE
            paint.shader = null
            paint.strokeWidth = size * 0.035f
            paint.color = withAlpha(skin.glow, 0.6f)
            canvas.drawCircle(cx, y + size / 2f, size * 1.18f / 2f, paint)
        }

        paint.style = Paint.Style.FILL
        // Body silhouette: cubic edges bowing in at the waist, tip overshooting the top.
        val body = Path()
        body.moveTo(cx - w / 2f, baseY)
        body.cubicTo(cx - 0.30f * w, baseY - 0.10f * h, cx - 0.50f * w, baseY - 0.55f * h, cx, tipY)
        body.cubicTo(cx + 0.50f * w, baseY - 0.55f * h, cx + 0.30f * w, baseY - 0.10f * h, cx + w / 2f, baseY)
        body.close()
        paint.shader = LinearGradient(cx, baseY, cx, tipY, intArrayOf(skin.outer[2], skin.outer[1], skin.outer[0]), null, Shader.TileMode.CLAMP)
        canvas.drawPath(body, paint)

        // Core: same silhouette at 0.55×, base lifted 30% of the body height.
        val coreW = 0.55f * w
        val coreH = 0.55f * h
        val coreBaseY = baseY - 0.30f * h
        val coreTipY = coreBaseY - 0.55f * (baseY - tipY)
        val core = Path()
        core.moveTo(cx - coreW / 2f, coreBaseY)
        core.cubicTo(cx - 0.30f * coreW, coreBaseY - 0.10f * coreH, cx - 0.50f * coreW, coreBaseY - 0.55f * coreH, cx, coreTipY)
        core.cubicTo(cx + 0.50f * coreW, coreBaseY - 0.55f * coreH, cx + 0.30f * coreW, coreBaseY - 0.10f * coreH, cx + coreW / 2f, coreBaseY)
        core.close()
        paint.shader = LinearGradient(cx, coreBaseY, cx, coreTipY, intArrayOf(skin.core[1], skin.core[0]), null, Shader.TileMode.CLAMP)
        canvas.drawPath(core, paint)

        // Embers (tier >= RARE): hashed positions evaluated once at the fixed time.
        if (tier.ordinal >= GoalRewards.Tier.RARE.ordinal) {
            val count = when (tier) {
                GoalRewards.Tier.RARE -> 7
                GoalRewards.Tier.EPIC -> 10
                else -> 14
            }
            val box = size.toDouble() * 1.4
            val boxLeft = cx.toDouble() - box / 2
            val boxTop = flameCenterY.toDouble() - box / 2
            for (i in 0 until count) {
                val seed = i * 12.9898
                val period = 1.6 + frac(sin(seed) * 43758.5) * 1.4
                val p = frac(t / period + frac(sin(seed * 3.1) * 9183.1))
                val ex = boxLeft + box * (0.5 + frac(sin(seed * 7.7) * 1234.5) * 0.5 + sin(t * 2 + seed) * 0.04 * p)
                val ey = boxTop + box * (0.45 - p * 0.45)
                val r = (size.toDouble() * (0.03 + 0.022 * frac(sin(seed * 5.3) * 777.7)) * (1 - p * 0.55)).toFloat()
                val life = (1 - p) * minOf(1.0, p * 4)
                paint.style = Paint.Style.FILL
                paint.shader = RadialGradient(
                    ex.toFloat(), ey.toFloat(), r * 2.6f,
                    intArrayOf(withAlpha(skin.glow, life.toFloat() * 0.35f), Color.TRANSPARENT),
                    null, Shader.TileMode.CLAMP,
                )
                canvas.drawCircle(ex.toFloat(), ey.toFloat(), r * 2.6f, paint)
                val dotColor = when (i % 3) {
                    0 -> skin.core[0]
                    else -> if (i % 2 == 0) skin.outer[0] else skin.outer[1]
                }
                paint.shader = null
                paint.color = withAlpha(dotColor, life.toFloat())
                canvas.drawCircle(ex.toFloat(), ey.toFloat(), r, paint)
            }
        }
        paint.shader = null
        orbitDots(front = true)
    }

    /** Static port of the reference NoopChestArt; `size` doubles as the artboard width. */
    fun drawChest(canvas: Canvas, size: Float, x: Float, y: Float, tier: GoalRewards.Tier) {
        val (lid, body, metal, gem) = chestMaterials(tier)
        val w = size
        val lidH = size * 0.42f
        val bodyH = size * 0.48f
        val bodyTop = y + lidH
        val baseY = bodyTop + bodyH
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        fun color(value: Int, alpha: Float): Int = withAlpha(value, alpha)

        // 1. Ground shadow.
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = color(0xFF000000.toInt(), 0.45f)
        paint.setShadowLayer(size * 0.04f, 0f, 0f, color(0xFF000000.toInt(), 0.45f))
        canvas.drawOval(
            RectF(x + w / 2f - w * 0.46f, baseY - size * 0.045f, x + w / 2f + w * 0.46f, baseY + size * 0.045f),
            paint,
        )
        paint.setShadowLayer(0f, 0f, 0f, 0)

        // 2. Body: rounded rect, wood gradient, planks, bottom darkening, metal base strip.
        val bodyRect = RectF(x, bodyTop, x + w, baseY)
        val bodyPath = Path().apply { addRoundRect(bodyRect, size * 0.05f, size * 0.05f, Path.Direction.CW) }
        paint.shader = LinearGradient(x, bodyTop, x, baseY, intArrayOf(body[0], body[1]), null, Shader.TileMode.CLAMP)
        canvas.drawPath(bodyPath, paint)
        canvas.save()
        canvas.clipPath(bodyPath)
        paint.shader = null
        val lineW = maxOf(1f, size * 0.008f)
        paint.color = color(0xFF000000.toInt(), 0.22f)
        val usable = bodyH - size * 0.08f
        for (third in 1..2) {
            val lineY = bodyTop + size * 0.04f + usable * third / 3f
            canvas.drawRect(x, lineY - lineW / 2f, x + w, lineY + lineW / 2f, paint)
        }
        paint.color = 0xFF000000.toInt()
        paint.shader = LinearGradient(x, baseY - bodyH / 3f, x, baseY, intArrayOf(0x00000000, 0x4D000000), null, Shader.TileMode.CLAMP)
        canvas.drawRect(x, baseY - bodyH / 3f, x + w, baseY, paint)
        paint.shader = LinearGradient(x, baseY - size * 0.05f, x, baseY, intArrayOf(metal[0], metal[1], metal[2]), null, Shader.TileMode.CLAMP)
        canvas.drawRect(x, baseY - size * 0.05f, x + w, baseY, paint)
        canvas.restore()

        // 3. Straps with rivets; the lid portion is clipped to the lid dome.
        val strapW = size * 0.09f
        val strapTop = y
        val strapBottom = baseY
        fun drawStrap(fraction: Float) {
            val left = x + fraction * w - strapW / 2f
            canvas.save()
            canvas.clipRect(left, strapTop, left + strapW, bodyTop)
            paint.style = Paint.Style.FILL
            paint.shader = LinearGradient(left, 0f, left + strapW, 0f, intArrayOf(metal[2], metal[0], metal[1], metal[2]), null, Shader.TileMode.CLAMP)
            canvas.drawRect(left, strapTop, left + strapW, bodyTop, paint)
            canvas.restore()
            paint.shader = LinearGradient(left, 0f, left + strapW, 0f, intArrayOf(metal[2], metal[0], metal[1], metal[2]), null, Shader.TileMode.CLAMP)
            canvas.drawRect(left, bodyTop, left + strapW, strapBottom, paint)
            drawRivet(canvas, paint, left + strapW / 2f, y + lidH * 0.45f, size, metal[2])
            drawRivet(canvas, paint, left + strapW / 2f, bodyTop + bodyH * 0.18f, size, metal[2])
            drawRivet(canvas, paint, left + strapW / 2f, bodyTop + bodyH * 0.86f, size, metal[2])
        }
        drawStrap(0.22f)
        drawStrap(0.78f)

        // 4. Lid: flat bottom, domed quadratic top.
        val shoulderY = y + lidH * 0.42f
        val lidPath = Path()
        lidPath.moveTo(x, bodyTop)
        lidPath.lineTo(x, shoulderY)
        lidPath.quadTo(x + w / 2f, y - lidH * 0.42f, x + w, shoulderY)
        lidPath.lineTo(x + w, bodyTop)
        lidPath.close()
        paint.shader = LinearGradient(x, y, x, bodyTop, intArrayOf(lid[0], lid[1]), null, Shader.TileMode.CLAMP)
        canvas.drawPath(lidPath, paint)
        val highlight = Path()
        highlight.moveTo(x + size * 0.03f, shoulderY + size * 0.03f)
        highlight.quadTo(x + w / 2f, y - lidH * 0.42f + size * 0.03f, x + w - size * 0.03f, shoulderY + size * 0.03f)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = maxOf(1.5f, size * 0.018f)
        paint.shader = LinearGradient(x, 0f, x + w, 0f, intArrayOf(color(0xFFFFFFFF.toInt(), 0.55f), color(0xFFFFFFFF.toInt(), 0.05f)), null, Shader.TileMode.CLAMP)
        canvas.drawPath(highlight, paint)
        paint.shader = null
        paint.strokeWidth = maxOf(1f, size * 0.008f)
        paint.color = color(0xFF000000.toInt(), 0.35f)
        canvas.drawPath(lidPath, paint)
        paint.style = Paint.Style.FILL

        // 5. Base bar under the lock line.
        val barRect = RectF(x - size * 0.015f, y + lidH - size * 0.03f, x - size * 0.015f + w * 1.03f, y + lidH - size * 0.03f + size * 0.065f)
        paint.shader = LinearGradient(0f, barRect.top, 0f, barRect.bottom, intArrayOf(metal[0], metal[1], metal[2]), null, Shader.TileMode.CLAMP)
        canvas.drawRoundRect(barRect, size * 0.015f, size * 0.015f, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = maxOf(1f, size * 0.008f)
        paint.color = color(0xFFFFFFFF.toInt(), 0.5f)
        canvas.drawLine(barRect.left + size * 0.015f, barRect.top, barRect.right - size * 0.015f, barRect.top, paint)
        paint.style = Paint.Style.FILL

        // 6. Lock with gem and keyhole.
        val lockW = size * 0.2f
        val lockH = size * 0.24f
        val lockLeft = x + w / 2f - lockW / 2f
        val lockTop = y + lidH - size * 0.07f
        val lockRect = RectF(lockLeft, lockTop, lockLeft + lockW, lockTop + lockH)
        val lockRadii = floatArrayOf(
            size * 0.03f, size * 0.03f, size * 0.03f, size * 0.03f,
            size * 0.09f, size * 0.09f, size * 0.09f, size * 0.09f,
        )
        val lockPath = Path().apply { addRoundRect(lockRect, lockRadii, Path.Direction.CW) }
        paint.shader = LinearGradient(0f, lockRect.top, 0f, lockRect.bottom, intArrayOf(metal[0], metal[1], metal[2]), null, Shader.TileMode.CLAMP)
        canvas.drawPath(lockPath, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = maxOf(1f, size * 0.006f)
        paint.color = color(0xFF000000.toInt(), 0.3f)
        canvas.drawPath(lockPath, paint)
        paint.style = Paint.Style.FILL
        val lockCenterX = x + w / 2f
        val lockCenterY = lockTop + lockH / 2f
        if (gem != null) {
            val gemR = size * 0.1f / 2f
            val gemShadow = Paint(paint)
            gemShadow.color = color(gem[1], 0.4f)
            canvas.drawOval(RectF(lockCenterX - gemR, lockCenterY - gemR + size * 0.035f, lockCenterX + gemR, lockCenterY + gemR + size * 0.035f), gemShadow)
            paint.shader = RadialGradient(
                lockCenterX - size * 0.1f * 0.38f, lockCenterY - size * 0.1f * 0.32f, gemR * 1.4f,
                intArrayOf(gem[0], gem[1], gem[2]), null, Shader.TileMode.CLAMP,
            )
            canvas.drawCircle(lockCenterX, lockCenterY, gemR, paint)
            paint.shader = null
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = maxOf(1f, size * 0.006f)
            paint.color = color(0xFF000000.toInt(), 0.35f)
            canvas.drawCircle(lockCenterX, lockCenterY, gemR, paint)
            paint.style = Paint.Style.FILL
            paint.color = color(0xFFFFFFFF.toInt(), 0.9f)
            canvas.drawCircle(lockCenterX - size * 0.015f, lockCenterY - size * 0.015f, size * 0.025f, paint)
        }
        paint.color = color(0xFF000000.toInt(), 0.7f)
        val keyholeY = if (gem != null) lockTop + size * 0.155f else lockTop + size * 0.09f
        val keyhole = RectF(lockCenterX - size * 0.012f, keyholeY - size * 0.055f / 2f, lockCenterX + size * 0.012f, keyholeY + size * 0.055f / 2f)
        canvas.drawRoundRect(keyhole, size * 0.012f, size * 0.012f, paint)
        paint.shader = null
    }

    private fun chestMaterials(tier: GoalRewards.Tier): ChestMaterials = when (tier) {
        GoalRewards.Tier.COMMON -> ChestMaterials(
            intArrayOf(0xFFA86E3E.toInt(), 0xFF74472A.toInt()),
            intArrayOf(0xFF8E5A33.toInt(), 0xFF553420.toInt()),
            intArrayOf(0xFFE4E4EA.toInt(), 0xFFA2A2AE.toInt(), 0xFF5E5E6A.toInt()), null,
        )
        GoalRewards.Tier.RARE -> ChestMaterials(
            intArrayOf(0xFF3D7AC0.toInt(), 0xFF1E4677.toInt()),
            intArrayOf(0xFF2F639E.toInt(), 0xFF152F52.toInt()),
            intArrayOf(0xFFF7FBFF.toInt(), 0xFFBACBDC.toInt(), 0xFF6E8196.toInt()),
            intArrayOf(0xFFBDE7FF.toInt(), 0xFF3B8FE8.toInt(), 0xFF163E8A.toInt()),
        )
        GoalRewards.Tier.EPIC -> ChestMaterials(
            intArrayOf(0xFFB0304A.toInt(), 0xFF6A1626.toInt()),
            intArrayOf(0xFF952640.toInt(), 0xFF4A0F1C.toInt()),
            intArrayOf(0xFFFFF2BC.toInt(), 0xFFE5B651.toInt(), 0xFF95631A.toInt()),
            intArrayOf(0xFFFFC2CC.toInt(), 0xFFE8304E.toInt(), 0xFF7A0F22.toInt()),
        )
        GoalRewards.Tier.LEGENDARY -> ChestMaterials(
            intArrayOf(0xFF34375A.toInt(), 0xFF14152A.toInt()),
            intArrayOf(0xFF262844.toInt(), 0xFF0A0B15.toInt()),
            intArrayOf(0xFFC9FFF3.toInt(), 0xFF7CF5D8.toInt(), 0xFF9A63E8.toInt()),
            intArrayOf(0xFFFFFFFF.toInt(), 0xFF7CF5D8.toInt(), 0xFF8A4FE0.toInt()),
        )
    }

    private fun drawRivet(canvas: Canvas, paint: Paint, centerX: Float, centerY: Float, size: Float, base: Int) {
        val d = size * 0.034f
        val r = d / 2f
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(centerX - d * 0.15f, centerY - d * 0.15f, r * 2f, intArrayOf(
            withAlpha(0xFFFFFFFF.toInt(), 0.95f), base,
        ), null, Shader.TileMode.CLAMP)
        canvas.drawCircle(centerX, centerY, r, paint)
        paint.shader = null
    }

    private fun withAlpha(value: Int, opacity: Float): Int {
        val alpha = (Color.alpha(value) * opacity).toInt().coerceIn(0, 255)
        return Color.argb(alpha, Color.red(value), Color.green(value), Color.blue(value))
    }

    private fun save(bmp: Bitmap, file: File) {
        file.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}

/** Lid/body/metal/gem colors for one tier; gem is null for COMMON. */
private data class ChestMaterials(
    val lid: IntArray,
    val body: IntArray,
    val metal: IntArray,
    val gem: IntArray?,
)
