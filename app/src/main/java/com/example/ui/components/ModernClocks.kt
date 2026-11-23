package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClockStyle
import com.example.model.LauncherConfig
import com.example.ui.theme.DotGothic16FontFamily
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OswaldFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.SixCapsFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.ui.theme.SyneFontFamily
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun accentGradientBrush(config: LauncherConfig, fallbackPrimary: Color): Brush {
    return if (config.useCustomAccent) {
        val start = Color(config.customAccentHex)
        val end = if (config.isAccentGradientEnabled) Color(config.customAccentEndHex) else start
        Brush.linearGradient(listOf(start, end))
    } else {
        Brush.linearGradient(listOf(fallbackPrimary, fallbackPrimary.copy(alpha = 0.72f)))
    }
}

@Composable
fun ModernClockHeader(
    now: LocalDateTime,
    config: LauncherConfig,
    onCycleClockStyle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val locale = Locale.getDefault()
    val hourText = now.format(DateTimeFormatter.ofPattern("HH", Locale.ENGLISH))
    val minuteText = now.format(DateTimeFormatter.ofPattern("mm", Locale.ENGLISH))
    val secondText = now.format(DateTimeFormatter.ofPattern("ss", Locale.ENGLISH))
    val timeText = "$hourText:$minuteText"
    val dateText = now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale))
    val shortDateText = now.format(DateTimeFormatter.ofPattern("EEE // dd MMM", Locale.ENGLISH)).uppercase()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val primaryColor = MaterialTheme.colorScheme.primary
    val accentEnd = if (config.useCustomAccent && config.isAccentGradientEnabled) {
        Color(config.customAccentEndHex)
    } else {
        primaryColor.copy(alpha = 0.7f)
    }

    val frostedBrush = remember(isDark, config.useCustomAccent, config.customAccentHex, config.customAccentEndHex, config.isAccentGradientEnabled) {
        if (config.useCustomAccent) {
            val s = Color(config.customAccentHex)
            val e = if (config.isAccentGradientEnabled) Color(config.customAccentEndHex) else s
            Brush.verticalGradient(listOf(s, e))
        } else if (isDark) {
            Brush.verticalGradient(
                listOf(
                    Color.White.copy(alpha = 0.94f),
                    Color.White.copy(alpha = 0.64f),
                    Color.White.copy(alpha = 0.34f)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    Color(0xFF161922).copy(alpha = 0.92f),
                    Color(0xFF161922).copy(alpha = 0.66f),
                    Color(0xFF161922).copy(alpha = 0.38f)
                )
            )
        }
    }

    val containerModifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(24.dp))
        .clickable { onCycleClockStyle() }
        .testTag("home_clock_header")

    when (config.clockStyle) {
        ClockStyle.MINIMAL_CLEAN -> {
            Column(modifier = containerModifier.padding(horizontal = 8.dp, vertical = 12.dp)) {
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 56.sp,
                        lineHeight = 60.sp,
                        brush = if (config.useCustomAccent) frostedBrush else null
                    ),
                    color = if (config.useCustomAccent) Color.Unspecified else MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        ClockStyle.TALL_CONDENSED_GLASS -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = containerModifier.padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = timeText,
                    style = TextStyle(
                        fontFamily = SixCapsFontFamily,
                        fontSize = 114.sp,
                        lineHeight = 112.sp,
                        letterSpacing = 2.sp,
                        brush = frostedBrush
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer(scaleY = 1.12f)
                )
            }
        }

        ClockStyle.BUBBLY_OVERLAP_GLASS -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = containerModifier.padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timeText,
                    style = TextStyle(
                        fontFamily = FredokaFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 76.sp,
                        lineHeight = 80.sp,
                        letterSpacing = (-7).sp,
                        brush = frostedBrush
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        // 1. iOS Depth-Effect Serif
        ClockStyle.IOS_DEPTH_SERIF -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = containerModifier.padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = primaryColor.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.labelLarge,
                        color = primaryColor,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = timeText,
                        style = TextStyle(
                            fontFamily = PlayfairDisplayFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 80.sp,
                            lineHeight = 84.sp,
                            color = primaryColor.copy(alpha = 0.16f)
                        ),
                        modifier = Modifier.offset(y = 5.dp)
                    )
                    Text(
                        text = timeText,
                        style = TextStyle(
                            fontFamily = PlayfairDisplayFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 80.sp,
                            lineHeight = 84.sp,
                            brush = frostedBrush
                        )
                    )
                }
            }
        }

        // 2. Nothing OS Dot-Matrix Clock
        ClockStyle.NOTHING_DOT_MATRIX -> {
            LiquidGlassSurface(
                shape = RoundedCornerShape(24.dp),
                isLiquidGlass = config.isLiquidGlassEnabled,
                modifier = containerModifier
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF2A3C))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = shortDateText,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = timeText,
                            style = TextStyle(
                                fontFamily = DotGothic16FontFamily,
                                fontSize = 54.sp,
                                lineHeight = 58.sp,
                                letterSpacing = 3.sp,
                                brush = frostedBrush
                            )
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = primaryColor.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = secondText,
                            style = TextStyle(
                                fontFamily = DotGothic16FontFamily,
                                fontSize = 22.sp,
                                color = primaryColor
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // 3. Android Material You Fluid Expressive Pill Stack
        ClockStyle.ANDROID_MATERIAL_PILL -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = containerModifier.padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            Brush.linearGradient(listOf(primaryColor.copy(alpha = 0.88f), accentEnd.copy(alpha = 0.78f)))
                        )
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = hourText,
                        style = TextStyle(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 52.sp,
                            color = Color.White
                        )
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(30.dp))
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f)
                        )
                        .border(1.dp, primaryColor.copy(alpha = 0.45f), RoundedCornerShape(30.dp))
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = minuteText,
                        style = TextStyle(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 52.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = now.format(DateTimeFormatter.ofPattern("EEE", locale)).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = primaryColor
                    )
                    Text(
                        text = now.format(DateTimeFormatter.ofPattern("dd/MM", Locale.ENGLISH)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 4. Samsung One UI Kinetic Staggered Stack
        ClockStyle.ONE_UI_KINETIC -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = containerModifier.padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .height(82.dp)
                        .clip(CircleShape)
                        .background(Brush.verticalGradient(listOf(primaryColor, accentEnd)))
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = hourText,
                            style = TextStyle(
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 58.sp,
                                lineHeight = 56.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = minuteText,
                            style = TextStyle(
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 58.sp,
                                lineHeight = 56.sp,
                                brush = Brush.linearGradient(listOf(primaryColor, accentEnd))
                            ),
                            modifier = Modifier.offset(y = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$dateText • :$secondText",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 5. Retro Split-Flap Mechanical Clock
        ClockStyle.RETRO_SPLIT_FLAP -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = containerModifier.padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SplitFlapTile(value = hourText, accentColor = primaryColor, isDark = isDark)
                    Text(
                        text = ":",
                        style = TextStyle(
                            fontFamily = OswaldFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 42.sp,
                            color = primaryColor
                        )
                    )
                    SplitFlapTile(value = minuteText, accentColor = accentEnd, isDark = isDark)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$shortDateText • $secondText",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 6. Cyber Neon Wireframe Glow
        ClockStyle.NEON_CYBER_GLOW -> {
            Box(
                modifier = containerModifier
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF070B14).copy(alpha = 0.85f),
                                primaryColor.copy(alpha = 0.16f)
                            )
                        )
                    )
                    .border(
                        width = 1.4.dp,
                        brush = Brush.linearGradient(listOf(primaryColor, accentEnd)),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = timeText,
                        style = TextStyle(
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 50.sp,
                            color = Color.White,
                            shadow = Shadow(
                                color = primaryColor.copy(alpha = 0.92f),
                                offset = Offset.Zero,
                                blurRadius = 24f
                            )
                        )
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = ":$secondText",
                            style = TextStyle(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = primaryColor,
                                shadow = Shadow(color = primaryColor, blurRadius = 14f)
                            )
                        )
                        Text(
                            text = now.format(DateTimeFormatter.ofPattern("EEE dd", Locale.ENGLISH)).uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.78f)
                        )
                    }
                }
            }
        }

        // 7. Bold Swiss Monospace Grid
        ClockStyle.SWISS_MONO_GRID -> {
            Column(
                modifier = containerModifier
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(14.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = shortDateText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "S.$secondText",
                        style = TextStyle(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = primaryColor
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.14f))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = hourText,
                        style = TextStyle(
                            fontFamily = OswaldFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 62.sp,
                            lineHeight = 60.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = ":$minuteText",
                        style = TextStyle(
                            fontFamily = OswaldFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 62.sp,
                            lineHeight = 60.sp,
                            brush = Brush.linearGradient(listOf(primaryColor, accentEnd))
                        )
                    )
                }
            }
        }

        // 8. Editorial Split Bento Card
        ClockStyle.EDITORIAL_SPLIT_CARD -> {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = containerModifier.padding(vertical = 4.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1.1f)
                        .height(102.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF0E1118))
                        .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(22.dp))
                ) {
                    Text(
                        text = timeText,
                        style = TextStyle(
                            fontFamily = SyneFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 42.sp,
                            brush = Brush.linearGradient(listOf(Color.White, primaryColor))
                        )
                    )
                }
                LiquidGlassSurface(
                    shape = RoundedCornerShape(22.dp),
                    isLiquidGlass = config.isLiquidGlassEnabled,
                    modifier = Modifier
                        .weight(0.9f)
                        .height(102.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = now.format(DateTimeFormatter.ofPattern("EEEE", locale)),
                            style = MaterialTheme.typography.titleMedium,
                            color = primaryColor,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = now.format(DateTimeFormatter.ofPattern("d MMMM", locale)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${now.year} • :$secondText",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 9. Life / Day Progress HUD Clock
        ClockStyle.LIFE_PROGRESS_HUD -> {
            val minutesOfDay = now.hour * 60 + now.minute
            val dayProgress = (minutesOfDay / 1440f).coerceIn(0f, 1f)
            val pct = (dayProgress * 100).toInt()
            LiquidGlassSurface(
                shape = RoundedCornerShape(24.dp),
                isLiquidGlass = config.isLiquidGlassEnabled,
                modifier = containerModifier
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = timeText,
                            style = if (config.useCustomAccent) {
                                TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 48.sp,
                                    brush = frostedBrush
                                )
                            } else {
                                TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 48.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = primaryColor.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = "$pct%",
                                style = MaterialTheme.typography.labelLarge,
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { dayProgress },
                        color = primaryColor,
                        trackColor = primaryColor.copy(alpha = 0.18f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 10. Ambient Line-Art Analog + Digital Hybrid
        ClockStyle.AMBIENT_ANALOG_HYBRID -> {
            LiquidGlassSurface(
                shape = RoundedCornerShape(24.dp),
                isLiquidGlass = config.isLiquidGlassEnabled,
                modifier = containerModifier
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    val handColor = MaterialTheme.colorScheme.onSurface
                    Canvas(modifier = Modifier.size(72.dp)) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val r = size.minDimension / 2f - 4f
                        drawCircle(
                            color = handColor.copy(alpha = 0.22f),
                            radius = r,
                            center = center,
                            style = Stroke(width = 2.dp.toPx())
                        )
                        for (i in 0 until 12) {
                            val angle = (i * 30.0 - 90.0) * PI / 180.0
                            val start = Offset(
                                center.x + ((r - 6f) * cos(angle)).toFloat(),
                                center.y + ((r - 6f) * sin(angle)).toFloat()
                            )
                            val end = Offset(
                                center.x + (r * cos(angle)).toFloat(),
                                center.y + (r * sin(angle)).toFloat()
                            )
                            drawLine(handColor.copy(alpha = 0.45f), start, end, strokeWidth = 1.5.dp.toPx())
                        }
                        val hourAngle = (((now.hour % 12) + now.minute / 60f) * 30.0 - 90.0) * PI / 180.0
                        val minAngle = ((now.minute + now.second / 60f) * 6.0 - 90.0) * PI / 180.0
                        val secAngle = (now.second * 6.0 - 90.0) * PI / 180.0
                        drawLine(
                            color = handColor,
                            start = center,
                            end = Offset(center.x + (r * 0.52f * cos(hourAngle)).toFloat(), center.y + (r * 0.52f * sin(hourAngle)).toFloat()),
                            strokeWidth = 3.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = handColor,
                            start = center,
                            end = Offset(center.x + (r * 0.76f * cos(minAngle)).toFloat(), center.y + (r * 0.76f * sin(minAngle)).toFloat()),
                            strokeWidth = 2.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = primaryColor,
                            start = center,
                            end = Offset(center.x + (r * 0.84f * cos(secAngle)).toFloat(), center.y + (r * 0.84f * sin(secAngle)).toFloat()),
                            strokeWidth = 1.4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        drawCircle(color = primaryColor, radius = 3.5.dp.toPx(), center = center)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = timeText,
                            style = if (config.useCustomAccent) {
                                TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 36.sp,
                                    brush = frostedBrush
                                )
                            } else {
                                TextStyle(
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 36.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        )
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SplitFlapTile(
    value: String,
    accentColor: Color,
    isDark: Boolean
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(width = 78.dp, height = 84.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (isDark) Color(0xFF141822) else Color(0xFF1F2430))
            .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
    ) {
        Text(
            text = value,
            style = TextStyle(
                fontFamily = OswaldFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 50.sp,
                color = Color.White
            )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(Color.Black.copy(alpha = 0.55f))
        )
    }
}

@Composable
fun ClockStylePreviewSample(style: ClockStyle, isDark: Boolean) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val previewBrush = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.94f),
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
        )
    )
    val previewTextStyle = when (style) {
        ClockStyle.MINIMAL_CLEAN -> TextStyle(
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        ClockStyle.TALL_CONDENSED_GLASS -> TextStyle(
            fontFamily = SixCapsFontFamily,
            fontSize = 34.sp,
            lineHeight = 34.sp,
            letterSpacing = 1.sp,
            brush = previewBrush
        )
        ClockStyle.BUBBLY_OVERLAP_GLASS -> TextStyle(
            fontFamily = FredokaFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            letterSpacing = (-3).sp,
            brush = previewBrush
        )
        ClockStyle.IOS_DEPTH_SERIF -> TextStyle(
            fontFamily = PlayfairDisplayFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            brush = previewBrush
        )
        ClockStyle.NOTHING_DOT_MATRIX -> TextStyle(
            fontFamily = DotGothic16FontFamily,
            fontSize = 21.sp,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        ClockStyle.ANDROID_MATERIAL_PILL -> TextStyle(
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = primaryColor
        )
        ClockStyle.ONE_UI_KINETIC -> TextStyle(
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        ClockStyle.RETRO_SPLIT_FLAP -> TextStyle(
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color.White
        )
        ClockStyle.NEON_CYBER_GLOW -> TextStyle(
            fontFamily = SyneFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = primaryColor,
            shadow = Shadow(color = primaryColor, blurRadius = 12f)
        )
        ClockStyle.SWISS_MONO_GRID -> TextStyle(
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 23.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        ClockStyle.EDITORIAL_SPLIT_CARD -> TextStyle(
            fontFamily = SyneFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        ClockStyle.LIFE_PROGRESS_HUD -> TextStyle(
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = primaryColor
        )
        ClockStyle.AMBIENT_ANALOG_HYBRID -> TextStyle(
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(38.dp)
            .let { mod ->
                if (style == ClockStyle.RETRO_SPLIT_FLAP) {
                    mod
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color(0xFF141822) else Color(0xFF1F2430))
                        .padding(horizontal = 8.dp)
                } else mod
            }
    ) {
        Text(
            text = "12:00",
            style = previewTextStyle,
            maxLines = 1
        )
    }
}
