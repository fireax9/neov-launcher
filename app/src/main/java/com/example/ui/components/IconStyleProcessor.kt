package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.util.LruCache
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.model.IconColorTheme
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

private object StyledIconCache {
    private val cache = object : LruCache<String, ImageBitmap>(180) {}

    fun getOrCreate(
        key: String,
        builder: () -> ImageBitmap?
    ): ImageBitmap? {
        cache.get(key)?.let { return it }
        val built = builder() ?: return null
        cache.put(key, built)
        return built
    }
}

@Composable
fun rememberStyledAppBitmap(
    packageName: String,
    fullIcon: ImageBitmap?,
    foregroundIcon: ImageBitmap?,
    theme: IconColorTheme,
    customBgHex: Long,
    customFgHex: Long
): ImageBitmap? {
    val source = foregroundIcon ?: fullIcon ?: return null
    if (theme == IconColorTheme.ORIGINAL) return fullIcon ?: source

    val cacheKey = remember(packageName, theme, customBgHex, customFgHex, source.width, source.height) {
        "${packageName}_${theme.name}_${customBgHex}_${customFgHex}_${source.width}x${source.height}"
    }

    return remember(cacheKey, source) {
        StyledIconCache.getOrCreate(cacheKey) {
            runCatching {
                stylizeBitmap(
                    source = source,
                    theme = theme,
                    customBgHex = customBgHex,
                    customFgHex = customFgHex
                )
            }.getOrNull()
        }
    }
}

private fun stylizeBitmap(
    source: ImageBitmap,
    theme: IconColorTheme,
    customBgHex: Long,
    customFgHex: Long
): ImageBitmap {
    val raw = source.asAndroidBitmap()
    val targetSize = 96
    val scaled = if (raw.width != targetSize || raw.height != targetSize) {
        Bitmap.createScaledBitmap(raw, targetSize, targetSize, true)
    } else {
        raw
    }

    val w = scaled.width
    val h = scaled.height
    val pixels = IntArray(w * h)
    scaled.getPixels(pixels, 0, w, 0, 0, w, h)

    val out = IntArray(w * h)
    val lum = FloatArray(w * h)
    val alphaArr = FloatArray(w * h)

    for (i in pixels.indices) {
        val p = pixels[i]
        val a = AndroidColor.alpha(p) / 255f
        val r = AndroidColor.red(p) / 255f
        val g = AndroidColor.green(p) / 255f
        val b = AndroidColor.blue(p) / 255f
        alphaArr[i] = a
        lum[i] = (0.299f * r + 0.587f * g + 0.114f * b)
    }

    when (theme) {
        IconColorTheme.ORIGINAL -> {
            System.arraycopy(pixels, 0, out, 0, pixels.size)
        }
        IconColorTheme.SPIDER_CRIMSON -> {
            val cx = w / 2f
            val cy = h / 2f
            for (y in 0 until h) {
                for (x in 0 until w) {
                    val idx = y * w + x
                    val a = alphaArr[idx]
                    if (a < 0.06f) continue
                    val l = lum[idx]
                    val dx = (x - cx) / cx
                    val dy = (y - cy) / cy
                    val dist = hypot(dx, dy)
                    val angle = atan2(dy, dx)
                    val radialWeb = abs(sin(angle * 6f)) < 0.12f
                    val ringWeb = abs(sin(dist * 15f + 0.4f * cos(angle * 6f))) < 0.16f
                    val isWebLine = (radialWeb || ringWeb) && dist > 0.14f
                    val shade = if (isWebLine) 0.16f else (0.58f + 0.42f * l)
                    val r = (255f * shade).toInt().coerceIn(0, 255)
                    val g = (20f * shade).toInt().coerceIn(0, 255)
                    val b = (32f * shade).toInt().coerceIn(0, 255)
                    out[idx] = AndroidColor.argb((a * 255f).toInt(), r, g, b)
                }
            }
        }
        IconColorTheme.PRISM_GLASS -> {
            for (y in 0 until h) {
                val ny = y / h.toFloat()
                for (x in 0 until w) {
                    val idx = y * w + x
                    val a = alphaArr[idx]
                    if (a < 0.06f) continue
                    val nx = x / w.toFloat()
                    val t = (nx * 0.72f + ny * 0.28f).coerceIn(0f, 1f)
                    val baseRgb = samplePrismGradient(t)
                    val l = lum[idx]
                    val boost = 0.72f + 0.38f * l
                    val edgeBoost = edgeStrength(alphaArr, lum, x, y, w, h) * 0.55f
                    val r = min(255, ((AndroidColor.red(baseRgb) * boost) + edgeBoost * 255f).toInt())
                    val g = min(255, ((AndroidColor.green(baseRgb) * boost) + edgeBoost * 255f).toInt())
                    val b = min(255, ((AndroidColor.blue(baseRgb) * boost) + edgeBoost * 255f).toInt())
                    out[idx] = AndroidColor.argb((a * 255f).toInt(), r, g, b)
                }
            }
        }
        IconColorTheme.CRIMSON_NEON -> {
            val edgeMap = computeEdgeGlowMap(alphaArr, lum, w, h)
            for (i in pixels.indices) {
                val glow = edgeMap[i]
                val body = alphaArr[i] * (0.14f + 0.16f * lum[i])
                val intensity = max(glow, body).coerceIn(0f, 1f)
                if (intensity < 0.04f) continue
                val core = ((glow - 0.58f) / 0.42f).coerceIn(0f, 1f)
                val r = (255f * min(1f, intensity * 1.25f)).toInt().coerceIn(0, 255)
                val g = (30f * intensity + 140f * core).toInt().coerceIn(0, 255)
                val b = (48f * intensity + 150f * core).toInt().coerceIn(0, 255)
                out[i] = AndroidColor.argb((intensity * 255f).toInt().coerceIn(0, 255), r, g, b)
            }
        }
        IconColorTheme.EMERALD_NEON -> {
            val edgeMap = computeEdgeGlowMap(alphaArr, lum, w, h)
            for (i in pixels.indices) {
                val glow = edgeMap[i]
                val body = alphaArr[i] * (0.12f + 0.14f * lum[i])
                val intensity = max(glow, body).coerceIn(0f, 1f)
                if (intensity < 0.04f) continue
                val core = ((glow - 0.56f) / 0.44f).coerceIn(0f, 1f)
                val r = (54f * intensity + 195f * core).toInt().coerceIn(0, 255)
                val g = (255f * min(1f, intensity * 1.25f)).toInt().coerceIn(0, 255)
                val b = (36f * intensity + 185f * core).toInt().coerceIn(0, 255)
                out[i] = AndroidColor.argb((intensity * 255f).toInt().coerceIn(0, 255), r, g, b)
            }
        }
        IconColorTheme.ACID_SLIME -> {
            for (y in 0 until h) {
                for (x in 0 until w) {
                    val idx = y * w + x
                    val a = alphaArr[idx]
                    if (a < 0.06f) continue
                    val l = lum[idx]
                    val dx = sampleHeight(alphaArr, lum, x - 1, y, w, h) - sampleHeight(alphaArr, lum, x + 1, y, w, h)
                    val dy = sampleHeight(alphaArr, lum, x, y - 1, w, h) - sampleHeight(alphaArr, lum, x, y + 1, w, h)
                    val spec = ((dx * 0.6f + dy * 0.8f) * 1.3f).coerceIn(0f, 1f)
                    val shade = (0.58f + 0.42f * l).coerceIn(0.42f, 1f)
                    val r = (132f * shade + 123f * spec).toInt().coerceIn(0, 255)
                    val g = (238f * shade + 17f * spec).toInt().coerceIn(0, 255)
                    val b = (22f * shade + 175f * spec).toInt().coerceIn(0, 255)
                    out[idx] = AndroidColor.argb((a * 255f).toInt(), r, g, b)
                }
            }
        }
        IconColorTheme.LIQUID_CHROME -> {
            for (y in 0 until h) {
                for (x in 0 until w) {
                    val idx = y * w + x
                    val a = alphaArr[idx]
                    if (a < 0.06f) continue
                    val dx = sampleHeight(alphaArr, lum, x - 1, y, w, h) - sampleHeight(alphaArr, lum, x + 1, y, w, h)
                    val dy = sampleHeight(alphaArr, lum, x, y - 1, w, h) - sampleHeight(alphaArr, lum, x, y + 1, w, h)
                    val bezel = (dx * 0.7f + dy * 0.7f) * 1.6f
                    val sweep = cos(((x + y) / (w + h).toFloat()) * 6.28f) * 0.14f
                    val chrome = (0.48f + 0.36f * lum[idx] + bezel + sweep).coerceIn(0.08f, 1f)
                    val edgeBoost = edgeStrength(alphaArr, lum, x, y, w, h) * 0.4f
                    val valByte = ((chrome + edgeBoost).coerceIn(0f, 1f) * 255f).toInt()
                    val bByte = min(255, (valByte * 1.04f).toInt())
                    out[idx] = AndroidColor.argb((a * 255f).toInt(), valByte, valByte, bByte)
                }
            }
        }
        IconColorTheme.BLACK_RED,
        IconColorTheme.BLACK_WHITE,
        IconColorTheme.BLACK_GOLD,
        IconColorTheme.CYBER_BLUE,
        IconColorTheme.CUSTOM_DUOTONE -> {
            val fgColor = if (theme == IconColorTheme.CUSTOM_DUOTONE) {
                Color(customFgHex).toArgb()
            } else {
                Color(theme.fgHex).toArgb()
            }
            val fgR = AndroidColor.red(fgColor)
            val fgG = AndroidColor.green(fgColor)
            val fgB = AndroidColor.blue(fgColor)
            for (i in pixels.indices) {
                val a = alphaArr[i]
                if (a < 0.06f) continue
                val shade = (0.62f + 0.38f * lum[i]).coerceIn(0.5f, 1f)
                out[i] = AndroidColor.argb(
                    (a * 255f).toInt(),
                    (fgR * shade).toInt().coerceIn(0, 255),
                    (fgG * shade).toInt().coerceIn(0, 255),
                    (fgB * shade).toInt().coerceIn(0, 255)
                )
            }
        }
    }

    val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    result.setPixels(out, 0, w, 0, 0, w, h)
    return result.asImageBitmap()
}

private fun sampleHeight(alphaArr: FloatArray, lum: FloatArray, x: Int, y: Int, w: Int, h: Int): Float {
    val cx = x.coerceIn(0, w - 1)
    val cy = y.coerceIn(0, h - 1)
    val idx = cy * w + cx
    return alphaArr[idx] * (0.45f + 0.55f * lum[idx])
}

private fun edgeStrength(alphaArr: FloatArray, lum: FloatArray, x: Int, y: Int, w: Int, h: Int): Float {
    val gx = sampleHeight(alphaArr, lum, x + 1, y, w, h) - sampleHeight(alphaArr, lum, x - 1, y, w, h)
    val gy = sampleHeight(alphaArr, lum, x, y + 1, w, h) - sampleHeight(alphaArr, lum, x, y - 1, w, h)
    return sqrt(gx * gx + gy * gy).coerceIn(0f, 1f)
}

private fun computeEdgeGlowMap(alphaArr: FloatArray, lum: FloatArray, w: Int, h: Int): FloatArray {
    val rawEdge = FloatArray(w * h)
    for (y in 0 until h) {
        for (x in 0 until w) {
            val idx = y * w + x
            val edge = edgeStrength(alphaArr, lum, x, y, w, h)
            rawEdge[idx] = (edge * 1.65f).coerceIn(0f, 1f)
        }
    }
    val glow = FloatArray(w * h)
    for (y in 0 until h) {
        for (x in 0 until w) {
            val idx = y * w + x
            var maxGlow = rawEdge[idx]
            for (dy in -2..2) {
                for (dx in -2..2) {
                    val nx = (x + dx).coerceIn(0, w - 1)
                    val ny = (y + dy).coerceIn(0, h - 1)
                    val d = abs(dx) + abs(dy)
                    val decay = when (d) {
                        0 -> 1f
                        1 -> 0.72f
                        2 -> 0.45f
                        else -> 0.24f
                    }
                    maxGlow = max(maxGlow, rawEdge[ny * w + nx] * decay)
                }
            }
            glow[idx] = maxGlow.coerceIn(0f, 1f)
        }
    }
    return glow
}

private fun samplePrismGradient(t: Float): Int {
    return when {
        t < 0.35f -> lerpArgb(0xFF1CE2FF.toInt(), 0xFF4F7BFF.toInt(), t / 0.35f)
        t < 0.68f -> lerpArgb(0xFF4F7BFF.toInt(), 0xFFC246FF.toInt(), (t - 0.35f) / 0.33f)
        else -> lerpArgb(0xFFC246FF.toInt(), 0xFFFF7A18.toInt(), (t - 0.68f) / 0.32f)
    }
}

private fun lerpArgb(a: Int, b: Int, t: Float): Int {
    val clamped = t.coerceIn(0f, 1f)
    val r = (AndroidColor.red(a) + (AndroidColor.red(b) - AndroidColor.red(a)) * clamped).toInt()
    val g = (AndroidColor.green(a) + (AndroidColor.green(b) - AndroidColor.green(a)) * clamped).toInt()
    val bl = (AndroidColor.blue(a) + (AndroidColor.blue(b) - AndroidColor.blue(a)) * clamped).toInt()
    return AndroidColor.argb(255, r, g, bl)
}

@Composable
fun StylizedIconFrameOverlay(
    theme: IconColorTheme,
    cornerRadiusDp: Float = 16f,
    modifier: Modifier = Modifier
) {
    if (theme == IconColorTheme.ORIGINAL) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cr = cornerRadiusDp.dp.toPx().coerceAtMost(w * 0.48f)

        when (theme) {
            IconColorTheme.SPIDER_CRIMSON -> {
                val webColor = Color(0xFFFF1726).copy(alpha = 0.26f)
                drawLine(webColor, Offset(0f, 0f), Offset(w * 0.36f, h * 0.36f), strokeWidth = 1.dp.toPx())
                drawLine(webColor, Offset(w, 0f), Offset(w * 0.64f, h * 0.36f), strokeWidth = 1.dp.toPx())
                drawArc(
                    color = webColor,
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(-w * 0.24f, -h * 0.24f),
                    size = Size(w * 0.56f, h * 0.56f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            IconColorTheme.PRISM_GLASS -> {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF23D2FF),
                            Color(0xFF8B5CF6),
                            Color(0xFFFF6B2C),
                            Color(0xFFFFB800)
                        ),
                        start = Offset.Zero,
                        end = Offset(w, h)
                    ),
                    cornerRadius = CornerRadius(cr, cr),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.White.copy(alpha = 0.32f), Color.Transparent),
                        start = Offset(0f, 0f),
                        end = Offset(w * 0.65f, h * 0.65f)
                    ),
                    cornerRadius = CornerRadius(cr, cr)
                )
            }
            IconColorTheme.CRIMSON_NEON -> {
                drawRoundRect(
                    color = Color(0xFFFF1F34).copy(alpha = 0.32f),
                    cornerRadius = CornerRadius(cr, cr),
                    style = Stroke(width = 3.dp.toPx())
                )
                drawRoundRect(
                    color = Color(0xFFFF5E6E).copy(alpha = 0.85f),
                    cornerRadius = CornerRadius(cr, cr),
                    style = Stroke(width = 1.1.dp.toPx())
                )
            }
            IconColorTheme.ACID_SLIME -> {
                val slimeColor = Color(0xFF88EC18)
                val dripPath = Path().apply {
                    moveTo(w * 0.16f, 0f)
                    lineTo(w * 0.84f, 0f)
                    lineTo(w * 0.84f, h * 0.08f)
                    cubicTo(w * 0.78f, h * 0.20f, w * 0.71f, h * 0.20f, w * 0.66f, h * 0.08f)
                    cubicTo(w * 0.56f, h * 0.03f, w * 0.44f, h * 0.24f, w * 0.36f, h * 0.09f)
                    cubicTo(w * 0.27f, h * 0.03f, w * 0.22f, h * 0.16f, w * 0.16f, h * 0.06f)
                    close()
                }
                drawPath(dripPath, color = slimeColor.copy(alpha = 0.72f))
                drawCircle(
                    color = slimeColor.copy(alpha = 0.85f),
                    radius = w * 0.045f,
                    center = Offset(w * 0.24f, h * 0.88f)
                )
                drawCircle(
                    color = slimeColor.copy(alpha = 0.85f),
                    radius = w * 0.032f,
                    center = Offset(w * 0.73f, h * 0.85f)
                )
            }
            IconColorTheme.LIQUID_CHROME -> {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.24f),
                            Color.Transparent,
                            Color(0xFFB4BECB).copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(w, h)
                    ),
                    cornerRadius = CornerRadius(cr, cr)
                )
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFF8FAFC).copy(alpha = 0.75f), Color(0xFF475569).copy(alpha = 0.5f)),
                        start = Offset.Zero,
                        end = Offset(w, h)
                    ),
                    cornerRadius = CornerRadius(cr, cr),
                    style = Stroke(width = 1.4.dp.toPx())
                )
            }
            IconColorTheme.EMERALD_NEON -> {
                drawRoundRect(
                    color = Color(0xFF42FF2A).copy(alpha = 0.30f),
                    cornerRadius = CornerRadius(cr, cr),
                    style = Stroke(width = 3.dp.toPx())
                )
                drawRoundRect(
                    color = Color(0xFFB8FFAE).copy(alpha = 0.85f),
                    cornerRadius = CornerRadius(cr, cr),
                    style = Stroke(width = 1.1.dp.toPx())
                )
            }
            else -> Unit
        }
    }
}

@Composable
fun ModernGeometricNMonogram(
    accentColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.15f
        val leftX = w * 0.24f
        val rightX = w * 0.76f
        val topY = h * 0.22f
        val botY = h * 0.78f

        // Left vertical pillar
        drawLine(
            color = contentColor,
            start = Offset(leftX, botY),
            end = Offset(leftX, topY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Right vertical pillar
        drawLine(
            color = contentColor,
            start = Offset(rightX, botY),
            end = Offset(rightX, topY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Sculpted modern diagonal ribbon with accent gradient
        drawLine(
            brush = Brush.linearGradient(
                colors = listOf(accentColor, contentColor),
                start = Offset(leftX, topY),
                end = Offset(rightX, botY)
            ),
            start = Offset(leftX, topY),
            end = Offset(rightX, botY),
            strokeWidth = stroke * 1.06f,
            cap = StrokeCap.Round
        )
        // Specular jewel dot on top-right crown
        drawCircle(
            color = accentColor,
            radius = stroke * 0.36f,
            center = Offset(rightX, topY)
        )
    }
}
