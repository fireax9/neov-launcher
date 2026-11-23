package com.example.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.LauncherConfig
import com.example.ui.strings.AppStrings
import java.util.Locale

private enum class ColorStudioTarget {
    ICON_BG,
    ICON_FG,
    THEME_ACCENT
}

@Composable
fun CustomColorGradientStudio(
    config: LauncherConfig,
    strings: AppStrings,
    onIconCustomColorsChange: (bgStartHex: Long, bgEndHex: Long, fgHex: Long, gradientEnabled: Boolean) -> Unit,
    onThemeAccentCustomChange: (startHex: Long, endHex: Long, gradientEnabled: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var target by remember { mutableIntStateOf(0) } // 0 = Icon Bg, 1 = Icon Fg, 2 = Theme Accent
    var editingEndColor by remember { mutableIntStateOf(0) } // 0 = start color, 1 = end/gradient color

    val activeTarget = ColorStudioTarget.entries[target.coerceIn(0, 2)]
    val isGradientSupported = activeTarget != ColorStudioTarget.ICON_FG

    val currentStartHex = when (activeTarget) {
        ColorStudioTarget.ICON_BG -> config.customIconBgHex
        ColorStudioTarget.ICON_FG -> config.customIconFgHex
        ColorStudioTarget.THEME_ACCENT -> config.customAccentHex
    }
    val currentEndHex = when (activeTarget) {
        ColorStudioTarget.ICON_BG -> config.customIconBgEndHex
        ColorStudioTarget.ICON_FG -> config.customIconFgHex
        ColorStudioTarget.THEME_ACCENT -> config.customAccentEndHex
    }
    val gradientEnabled = when (activeTarget) {
        ColorStudioTarget.ICON_BG -> config.isIconGradientEnabled
        ColorStudioTarget.ICON_FG -> false
        ColorStudioTarget.THEME_ACCENT -> config.isAccentGradientEnabled
    }

    val activeChannelHex = if (isGradientSupported && editingEndColor == 1) currentEndHex else currentStartHex
    val initialHsv = remember(activeChannelHex) { colorLongToHsv(activeChannelHex) }
    var hue by remember(activeTarget, editingEndColor, activeChannelHex) { mutableFloatStateOf(initialHsv[0]) }
    var sat by remember(activeTarget, editingEndColor, activeChannelHex) { mutableFloatStateOf(initialHsv[1]) }
    var value by remember(activeTarget, editingEndColor, activeChannelHex) { mutableFloatStateOf(initialHsv[2]) }

    fun applyHsv(newH: Float, newS: Float, newV: Float) {
        hue = newH.coerceIn(0f, 360f)
        sat = newS.coerceIn(0f, 1f)
        value = newV.coerceIn(0f, 1f)
        val pickedHex = hsvToColorLong(hue, sat, value)
        when (activeTarget) {
            ColorStudioTarget.ICON_BG -> {
                val start = if (editingEndColor == 0) pickedHex else config.customIconBgHex
                val end = if (editingEndColor == 1) pickedHex else config.customIconBgEndHex
                onIconCustomColorsChange(start, end, config.customIconFgHex, config.isIconGradientEnabled)
            }
            ColorStudioTarget.ICON_FG -> {
                onIconCustomColorsChange(
                    config.customIconBgHex,
                    config.customIconBgEndHex,
                    pickedHex,
                    config.isIconGradientEnabled
                )
            }
            ColorStudioTarget.THEME_ACCENT -> {
                val start = if (editingEndColor == 0) pickedHex else config.customAccentHex
                val end = if (editingEndColor == 1) pickedHex else config.customAccentEndHex
                onThemeAccentCustomChange(start, end, config.isAccentGradientEnabled)
            }
        }
    }

    fun applyAutoShade() {
        val baseHsv = colorLongToHsv(currentStartHex)
        val shiftedHue = (baseHsv[0] + 28f) % 360f
        val shiftedSat = (baseHsv[1] * 0.92f + 0.08f).coerceIn(0f, 1f)
        val shiftedVal = if (baseHsv[2] > 0.45f) (baseHsv[2] * 0.48f).coerceIn(0.14f, 0.95f)
        else (baseHsv[2] + 0.34f).coerceIn(0.25f, 0.98f)
        val autoEnd = hsvToColorLong(shiftedHue, shiftedSat, shiftedVal)
        when (activeTarget) {
            ColorStudioTarget.ICON_BG -> onIconCustomColorsChange(
                config.customIconBgHex,
                autoEnd,
                config.customIconFgHex,
                true
            )
            ColorStudioTarget.THEME_ACCENT -> onThemeAccentCustomChange(
                config.customAccentHex,
                autoEnd,
                true
            )
            ColorStudioTarget.ICON_FG -> Unit
        }
    }

    LiquidGlassSurface(
        shape = RoundedCornerShape(22.dp),
        isLiquidGlass = config.isLiquidGlassEnabled,
        modifier = modifier
            .fillMaxWidth()
            .testTag("custom_color_gradient_studio")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.ColorLens,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(strings.customColorStudioTitle, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = strings.customColorStudioDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Target selector: Icon Background / Icon Symbol / Launcher Theme & Clock
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                FilterChip(
                    selected = activeTarget == ColorStudioTarget.ICON_BG,
                    onClick = { target = 0 },
                    label = { Text(strings.colorTargetIconBg, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.testTag("color_target_ICON_BG")
                )
                FilterChip(
                    selected = activeTarget == ColorStudioTarget.ICON_FG,
                    onClick = {
                        target = 1
                        editingEndColor = 0
                    },
                    label = { Text(strings.colorTargetIconFg, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.testTag("color_target_ICON_FG")
                )
                FilterChip(
                    selected = activeTarget == ColorStudioTarget.THEME_ACCENT,
                    onClick = { target = 2 },
                    label = { Text(strings.colorTargetThemeAccent, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.testTag("color_target_THEME_ACCENT")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live Gradient / Color Preview Banner
            val previewBrush = if (isGradientSupported && gradientEnabled) {
                Brush.linearGradient(listOf(Color(currentStartHex), Color(currentEndHex)))
            } else {
                Brush.linearGradient(listOf(Color(currentStartHex), Color(currentStartHex)))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(previewBrush)
                    .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                val hexText = if (isGradientSupported && gradientEnabled) {
                    "${formatHexRgb(currentStartHex)} → ${formatHexRgb(currentEndHex)}"
                } else {
                    formatHexRgb(currentStartHex)
                }
                val textColor = if (Color(currentStartHex).red * 0.3f + Color(currentStartHex).green * 0.59f + Color(currentStartHex).blue * 0.11f > 0.65f) {
                    Color.Black
                } else {
                    Color.White
                }
                Text(
                    text = hexText,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = textColor
                )
            }

            if (isGradientSupported) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(strings.enableGradientLabel, style = MaterialTheme.typography.labelLarge)
                    Switch(
                        checked = gradientEnabled,
                        onCheckedChange = { enabled ->
                            if (activeTarget == ColorStudioTarget.ICON_BG) {
                                onIconCustomColorsChange(
                                    config.customIconBgHex,
                                    config.customIconBgEndHex,
                                    config.customIconFgHex,
                                    enabled
                                )
                            } else {
                                onThemeAccentCustomChange(
                                    config.customAccentHex,
                                    config.customAccentEndHex,
                                    enabled
                                )
                            }
                            if (!enabled) editingEndColor = 0
                        },
                        modifier = Modifier.testTag("custom_gradient_switch")
                    )
                }

                if (gradientEnabled) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = editingEndColor == 0,
                            onClick = { editingEndColor = 0 },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color(currentStartHex))
                                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                )
                            },
                            label = { Text(strings.gradientStartColor, maxLines = 1) },
                            modifier = Modifier.weight(1f).testTag("gradient_start_tab")
                        )
                        FilterChip(
                            selected = editingEndColor == 1,
                            onClick = { editingEndColor = 1 },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(Color(currentEndHex))
                                        .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                )
                            },
                            label = { Text(strings.gradientEndColor, maxLines = 1) },
                            modifier = Modifier.weight(1f).testTag("gradient_end_tab")
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { applyAutoShade() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auto_shade_gradient_button")
                    ) {
                        Icon(Icons.Rounded.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.autoShadeButton)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(strings.saturationShadeLabel, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(6.dp))

            // 2D Saturation × Brightness/Shade Canvas
            SaturationValueBox(
                hue = hue,
                saturation = sat,
                value = value,
                onChange = { s, v -> applyHsv(hue, s, v) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp)
                    .testTag("color_sv_canvas")
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(strings.hueLabel, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(6.dp))

            // Full 360° Hue Rainbow Slider
            HueSpectrumBar(
                hue = hue,
                onHueChange = { newH -> applyHsv(newH, sat, value) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .testTag("color_hue_bar")
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(strings.brightnessLabel, style = MaterialTheme.typography.labelMedium)
                Text(
                    text = "${(value * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = value,
                onValueChange = { applyHsv(hue, sat, it) },
                valueRange = 0.05f..1f,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("color_brightness_slider")
            )

            // Instant Shades strip of current Hue
            Text(strings.shadesStripLabel, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                val shadePairs = listOf(
                    0.25f to 1.0f,
                    0.45f to 0.98f,
                    0.75f to 0.95f,
                    1.0f to 0.92f,
                    0.95f to 0.68f,
                    0.90f to 0.42f,
                    0.85f to 0.18f
                )
                shadePairs.forEachIndexed { idx, (sVal, vVal) ->
                    val shadeHex = hsvToColorLong(hue, sVal, vVal)
                    val selected = kotlin.math.abs(sat - sVal) < 0.08f && kotlin.math.abs(value - vVal) < 0.08f
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(shadeHex))
                            .border(
                                width = if (selected) 2.5.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.35f),
                                shape = CircleShape
                            )
                            .clickable { applyHsv(hue, sVal, vVal) }
                            .testTag("color_shade_chip_$idx")
                    ) {
                        if (selected) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                tint = if (vVal > 0.65f && sVal < 0.45f) Color.Black else Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SaturationValueBox(
    hue: Float,
    saturation: Float,
    value: Float,
    onChange: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val pureHueColor = Color(hsvToColorLong(hue, 1f, 1f))
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .pointerInput(hue) {
                detectTapGestures { pos ->
                    val s = (pos.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                    val v = (1f - pos.y / size.height.coerceAtLeast(1)).coerceIn(0.03f, 1f)
                    onChange(s, v)
                }
            }
            .pointerInput(hue) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val s = (change.position.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                    val v = (1f - change.position.y / size.height.coerceAtLeast(1)).coerceIn(0.03f, 1f)
                    onChange(s, v)
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(brush = Brush.horizontalGradient(listOf(Color.White, pureHueColor)))
            drawRect(brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
            val cx = (saturation * size.width).coerceIn(8f, size.width - 8f)
            val cy = ((1f - value) * size.height).coerceIn(8f, size.height - 8f)
            drawCircle(
                color = Color.White,
                radius = 9.dp.toPx(),
                center = Offset(cx, cy),
                style = Stroke(width = 2.5.dp.toPx())
            )
            drawCircle(
                color = Color.Black.copy(alpha = 0.45f),
                radius = 10.5.dp.toPx(),
                center = Offset(cx, cy),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

@Composable
private fun HueSpectrumBar(
    hue: Float,
    onHueChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val rainbow = remember {
        listOf(
            Color(0xFFFF0000),
            Color(0xFFFFFF00),
            Color(0xFF00FF00),
            Color(0xFF00FFFF),
            Color(0xFF0000FF),
            Color(0xFFFF00FF),
            Color(0xFFFF0000)
        )
    }
    Box(
        modifier = modifier
            .clip(CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
            .pointerInput(Unit) {
                detectTapGestures { pos ->
                    val h = ((pos.x / size.width.coerceAtLeast(1)) * 360f).coerceIn(0f, 360f)
                    onHueChange(h)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val h = ((change.position.x / size.width.coerceAtLeast(1)) * 360f).coerceIn(0f, 360f)
                    onHueChange(h)
                }
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(brush = Brush.horizontalGradient(rainbow))
            val tx = ((hue / 360f) * size.width).coerceIn(10f, size.width - 10f)
            drawCircle(
                color = Color.White,
                radius = size.height * 0.42f,
                center = Offset(tx, size.height / 2f),
                style = Stroke(width = 2.5.dp.toPx())
            )
        }
    }
}

fun colorLongToHsv(hex: Long): FloatArray {
    val hsv = FloatArray(3)
    AndroidColor.colorToHSV(Color(hex).toArgb(), hsv)
    return hsv
}

fun hsvToColorLong(h: Float, s: Float, v: Float): Long {
    val argb = AndroidColor.HSVToColor(255, floatArrayOf(h.coerceIn(0f, 360f), s.coerceIn(0f, 1f), v.coerceIn(0f, 1f)))
    return argb.toLong() and 0xFFFFFFFFL
}

fun formatHexRgb(hex: Long): String {
    val rgb = (hex and 0xFFFFFFL).toInt()
    return String.format(Locale.ENGLISH, "#%06X", rgb)
}
