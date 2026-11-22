package com.example.ui.components

import android.graphics.Matrix
import android.net.Uri
import android.view.TextureView
import android.view.ViewGroup
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import com.example.R
import com.example.model.AppSymbolPreset
import com.example.model.CustomAppOverride
import com.example.model.IconColorTheme
import com.example.model.IconShapeStyle
import com.example.model.InstalledApp
import com.example.model.WallpaperChoice
import com.example.ui.strings.AppStrings
import com.example.ui.theme.SpaceGroteskFontFamily
import java.io.File
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

fun iconShapeFor(style: IconShapeStyle): Shape = when (style) {
    IconShapeStyle.CIRCLE -> CircleShape
    IconShapeStyle.SQUIRCLE -> RoundedCornerShape(28)
    IconShapeStyle.ROUNDED_SQUARE -> RoundedCornerShape(16)
}

fun symbolVectorFor(preset: AppSymbolPreset): ImageVector? = when (preset) {
    AppSymbolPreset.NONE -> null
    AppSymbolPreset.PHONE -> Icons.Rounded.Call
    AppSymbolPreset.CHAT -> Icons.Rounded.ChatBubble
    AppSymbolPreset.CAMERA -> Icons.Rounded.PhotoCamera
    AppSymbolPreset.MUSIC -> Icons.Rounded.MusicNote
    AppSymbolPreset.VIDEO -> Icons.Rounded.Videocam
    AppSymbolPreset.BROWSER -> Icons.Rounded.Public
    AppSymbolPreset.SETTINGS -> Icons.Rounded.Settings
    AppSymbolPreset.BOLT -> Icons.Rounded.Bolt
    AppSymbolPreset.STAR -> Icons.Rounded.Star
    AppSymbolPreset.HEART -> Icons.Rounded.Favorite
    AppSymbolPreset.SHIELD -> Icons.Rounded.Shield
    AppSymbolPreset.TERMINAL -> Icons.Rounded.Terminal
    AppSymbolPreset.GAME -> Icons.Rounded.SportsEsports
    AppSymbolPreset.COMPASS -> Icons.Rounded.Explore
    AppSymbolPreset.MAIL -> Icons.Rounded.Mail
}

fun resolvedIconColors(
    theme: IconColorTheme,
    customBgHex: Long,
    customFgHex: Long
): Pair<Color, Color> = when (theme) {
    IconColorTheme.ORIGINAL -> Color.Transparent to Color.Unspecified
    IconColorTheme.CUSTOM_DUOTONE -> Color(customBgHex) to Color(customFgHex)
    else -> Color(theme.bgHex) to Color(theme.fgHex)
}

fun duotoneColorFilter(bgColor: Color, fgColor: Color): ColorFilter {
    val dr = fgColor.red - bgColor.red
    val dg = fgColor.green - bgColor.green
    val db = fgColor.blue - bgColor.blue
    val matrix = ColorMatrix(
        floatArrayOf(
            0.299f * dr, 0.587f * dr, 0.114f * dr, 0f, bgColor.red * 255f,
            0.299f * dg, 0.587f * dg, 0.114f * dg, 0f, bgColor.green * 255f,
            0.299f * db, 0.587f * db, 0.114f * db, 0f, bgColor.blue * 255f,
            0f, 0f, 0f, 1f, 0f
        )
    )
    return ColorFilter.colorMatrix(matrix)
}

@Composable
fun liquidGlassFillBrush(isDark: Boolean): Brush {
    return if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.16f),
                Color(0xFF141923).copy(alpha = 0.62f),
                Color(0xFF0E121B).copy(alpha = 0.72f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.84f),
                Color.White.copy(alpha = 0.62f),
                Color(0xFFE9EEF7).copy(alpha = 0.68f)
            )
        )
    }
}

@Composable
fun liquidGlassBorderBrush(isDark: Boolean): Brush {
    return if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.42f),
                Color.White.copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.20f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f),
                Color.Black.copy(alpha = 0.08f),
                Color.White.copy(alpha = 0.65f)
            )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiquidGlassSurface(
    shape: Shape = MaterialTheme.shapes.large,
    isLiquidGlass: Boolean = true,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val glassFill = liquidGlassFillBrush(isDark)
    val glassBorder = liquidGlassBorderBrush(isDark)

    val solidContainer = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.90f else 0.96f)
    val solidBorder = if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.08f)

    val decoratedMod = modifier
        .shadow(
            elevation = if (isLiquidGlass) 12.dp else 4.dp,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = if (isDark) 0.45f else 0.14f),
            spotColor = Color.Black.copy(alpha = if (isDark) 0.45f else 0.16f)
        )
        .clip(shape)
        .let { m ->
            if (isLiquidGlass) {
                m.background(glassFill).border(1.1.dp, glassBorder, shape)
            } else {
                m.background(solidContainer).border(1.dp, solidBorder, shape)
            }
        }
        .let { m ->
            if (onClick != null || onLongClick != null) {
                m.combinedClickable(
                    onClick = { onClick?.invoke() },
                    onLongClick = onLongClick
                )
            } else m
        }

    Box(modifier = decoratedMod) {
        content()
    }
}

@Composable
fun LauncherWallpaperBackground(
    wallpaper: WallpaperChoice,
    customWallpaperPath: String?,
    customVideoWallpaperPath: String? = null,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (wallpaper) {
            WallpaperChoice.PURE_MINIMAL -> {
                val bg = if (darkTheme) Color(0xFF080A0E) else Color(0xFFF6F4EE)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bg)
                )
            }
            WallpaperChoice.OBSIDIAN_WAVES -> {
                Image(
                    painter = painterResource(id = R.drawable.img_wallpaper_obsidian),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = if (darkTheme) 0.35f else 0.18f))
                )
            }
            WallpaperChoice.SANDSTONE_ARCH -> {
                Image(
                    painter = painterResource(id = R.drawable.img_wallpaper_sandstone),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (darkTheme) Color.Black.copy(alpha = 0.45f)
                            else Color.White.copy(alpha = 0.22f)
                        )
                )
            }
            WallpaperChoice.LIVE_AURORA -> {
                LiveAuroraWallpaper(
                    darkTheme = darkTheme,
                    modifier = Modifier.testTag("live_wallpaper_aurora")
                )
            }
            WallpaperChoice.LIVE_CYBER_GRID -> {
                LiveCyberGridWallpaper(
                    darkTheme = darkTheme,
                    modifier = Modifier.testTag("live_wallpaper_cyber_grid")
                )
            }
            WallpaperChoice.LIVE_LIQUID_FLOW -> {
                LiveLiquidFlowWallpaper(
                    darkTheme = darkTheme,
                    modifier = Modifier.testTag("live_wallpaper_liquid_flow")
                )
            }
            WallpaperChoice.CUSTOM_PHOTO -> {
                val file = customWallpaperPath?.let { File(it) }?.takeIf { it.exists() }
                if (file != null) {
                    AsyncImage(
                        model = file,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.34f))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (darkTheme) Color(0xFF080A0E) else Color(0xFFF6F4EE))
                    )
                }
            }
            WallpaperChoice.CUSTOM_VIDEO -> {
                val videoFile = customVideoWallpaperPath?.let { File(it) }?.takeIf { it.exists() }
                if (videoFile != null) {
                    VideoWallpaperPlayer(
                        videoPath = videoFile.absolutePath,
                        darkTheme = darkTheme,
                        modifier = Modifier.testTag("live_wallpaper_video")
                    )
                } else {
                    LiveAuroraWallpaper(
                        darkTheme = darkTheme,
                        modifier = Modifier.testTag("live_wallpaper_video_fallback")
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveAuroraWallpaper(
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "live_aurora")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aurora_phase"
    )
    val baseColor = if (darkTheme) Color(0xFF060913) else Color(0xFFEAF0FA)
    val c1 = if (darkTheme) Color(0xFF2563EB) else Color(0xFF60A5FA)
    val c2 = if (darkTheme) Color(0xFF9333EA) else Color(0xFFC084FC)
    val c3 = if (darkTheme) Color(0xFF10B981) else Color(0xFF34D399)
    val c4 = if (darkTheme) Color(0xFFFF4D2D) else Color(0xFFFB7185)

    Box(modifier = modifier.fillMaxSize().background(baseColor)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val p1 = Offset(
                x = w * (0.28f + 0.22f * cos(phase)),
                y = h * (0.26f + 0.16f * sin(phase))
            )
            val p2 = Offset(
                x = w * (0.72f + 0.20f * sin(phase * 0.8f)),
                y = h * (0.48f + 0.18f * cos(phase * 1.1f))
            )
            val p3 = Offset(
                x = w * (0.45f + 0.26f * cos(phase * 1.3f + 1.2f)),
                y = h * (0.76f + 0.14f * sin(phase * 0.9f))
            )
            val p4 = Offset(
                x = w * (0.18f + 0.18f * sin(phase + 2.1f)),
                y = h * (0.62f + 0.15f * cos(phase + 0.7f))
            )
            val r = max(w, h) * 0.65f
            val alphaTop = if (darkTheme) 0.42f else 0.32f
            drawCircle(
                brush = Brush.radialGradient(listOf(c1.copy(alpha = alphaTop), Color.Transparent), center = p1, radius = r),
                radius = r,
                center = p1
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(c2.copy(alpha = alphaTop * 0.9f), Color.Transparent), center = p2, radius = r),
                radius = r,
                center = p2
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(c3.copy(alpha = alphaTop * 0.8f), Color.Transparent), center = p3, radius = r * 0.9f),
                radius = r * 0.9f,
                center = p3
            )
            drawCircle(
                brush = Brush.radialGradient(listOf(c4.copy(alpha = alphaTop * 0.6f), Color.Transparent), center = p4, radius = r * 0.75f),
                radius = r * 0.75f,
                center = p4
            )
        }
    }
}

@Composable
private fun LiveCyberGridWallpaper(
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "live_cyber")
    val flow by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cyber_flow"
    )
    val bgTop = if (darkTheme) Color(0xFF05070D) else Color(0xFFEFF4FF)
    val bgBottom = if (darkTheme) Color(0xFF100B1C) else Color(0xFFFDEEF5)
    val lineA = if (darkTheme) Color(0xFF38BDF8) else Color(0xFF0284C7)
    val lineB = if (darkTheme) Color(0xFFFF2D55) else Color(0xFFE11D48)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgTop, bgBottom)))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val waveCount = 7
            for (i in 0 until waveCount) {
                val baseY = h * (0.16f + i * 0.11f)
                val amp = 32f + i * 6f
                val path = Path().apply {
                    moveTo(0f, baseY)
                    var x = 0f
                    while (x <= w + 24f) {
                        val norm = x / max(w, 1f)
                        val y = baseY + sin((norm * 4.5f + flow + i * 0.6f).toDouble()).toFloat() * amp
                        lineTo(x, y)
                        x += 24f
                    }
                }
                val waveColor = if (i % 2 == 0) lineA else lineB
                drawPath(
                    path = path,
                    color = waveColor.copy(alpha = if (darkTheme) 0.28f else 0.18f),
                    style = Stroke(width = 3.5f)
                )
                drawPath(
                    path = path,
                    color = waveColor.copy(alpha = if (darkTheme) 0.08f else 0.06f),
                    style = Stroke(width = 16f)
                )
            }
            val glowCenter = Offset(w * (0.5f + 0.25f * cos(flow)), h * 0.38f)
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(lineA.copy(alpha = if (darkTheme) 0.22f else 0.14f), Color.Transparent),
                    center = glowCenter,
                    radius = w * 0.6f
                ),
                radius = w * 0.6f,
                center = glowCenter
            )
        }
    }
}

@Composable
private fun LiveLiquidFlowWallpaper(
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "live_liquid")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "liquid_drift"
    )
    val bgBrush = if (darkTheme) {
        Brush.verticalGradient(listOf(Color(0xFF0A0E17), Color(0xFF141B2D)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF3F6FC), Color(0xFFE5ECF8)))
    }

    Box(modifier = modifier.fillMaxSize().background(bgBrush)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val orbs = listOf(
                Triple(0.24f to 0.25f, 0.28f, Color(0xFF38BDF8)),
                Triple(0.76f to 0.34f, 0.34f, Color(0xFFA855F7)),
                Triple(0.32f to 0.72f, 0.31f, Color(0xFF1FC77E)),
                Triple(0.78f to 0.78f, 0.25f, Color(0xFFFF6B4A))
            )
            orbs.forEachIndexed { idx, (pos, radiusFrac, color) ->
                val phase = drift + idx * 1.4f
                val center = Offset(
                    x = w * (pos.first + 0.08f * cos(phase)),
                    y = h * (pos.second + 0.07f * sin(phase * 0.9f))
                )
                val rad = minOf(w, h) * radiusFrac
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (darkTheme) 0.28f else 0.65f),
                            color.copy(alpha = if (darkTheme) 0.34f else 0.36f),
                            color.copy(alpha = 0.04f)
                        ),
                        center = center - Offset(rad * 0.25f, rad * 0.25f),
                        radius = rad * 1.25f
                    ),
                    radius = rad,
                    center = center
                )
                drawCircle(
                    color = Color.White.copy(alpha = if (darkTheme) 0.24f else 0.55f),
                    radius = rad,
                    center = center,
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}

@Composable
private fun VideoWallpaperPlayer(
    videoPath: String,
    darkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var videoWidth by remember { mutableIntStateOf(0) }
    var videoHeight by remember { mutableIntStateOf(0) }

    val exoPlayer = remember(videoPath) {
        runCatching {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(Uri.fromFile(File(videoPath))))
                repeatMode = Player.REPEAT_MODE_ALL
                volume = 0f
                playWhenReady = true
                prepare()
            }
        }.getOrNull()
    }

    DisposableEffect(exoPlayer, lifecycleOwner) {
        if (exoPlayer == null) {
            onDispose { }
        } else {
            val playerListener = object : Player.Listener {
                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    if (videoSize.width > 0 && videoSize.height > 0) {
                        videoWidth = videoSize.width
                        videoHeight = videoSize.height
                    }
                }
            }
            exoPlayer.addListener(playerListener)
            val lifecycleObserver = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> exoPlayer.play()
                    Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                    else -> Unit
                }
            }
            lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
                exoPlayer.removeListener(playerListener)
                exoPlayer.release()
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (exoPlayer != null) {
            AndroidView(
                factory = { ctx ->
                    TextureView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        exoPlayer.setVideoTextureView(this)
                    }
                },
                update = { textureView ->
                    exoPlayer.setVideoTextureView(textureView)
                    if (videoWidth > 0 && videoHeight > 0 && textureView.width > 0 && textureView.height > 0) {
                        val viewW = textureView.width.toFloat()
                        val viewH = textureView.height.toFloat()
                        val vidRatio = videoWidth.toFloat() / videoHeight.toFloat()
                        val viewRatio = viewW / viewH
                        val scaleX: Float
                        val scaleY: Float
                        if (vidRatio > viewRatio) {
                            scaleX = vidRatio / viewRatio
                            scaleY = 1f
                        } else {
                            scaleX = 1f
                            scaleY = viewRatio / vidRatio
                        }
                        val matrix = Matrix()
                        matrix.setScale(scaleX, scaleY, viewW / 2f, viewH / 2f)
                        textureView.setTransform(matrix)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = if (darkTheme) 0.28f else 0.14f))
            )
        } else {
            LiveAuroraWallpaper(darkTheme = darkTheme)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppIconTile(
    app: InstalledApp,
    iconShape: IconShapeStyle,
    iconColorTheme: IconColorTheme,
    customBgHex: Long,
    customFgHex: Long,
    override: CustomAppOverride? = null,
    isLiquidGlass: Boolean = true,
    iconSize: Dp = 56.dp,
    showLabel: Boolean = true,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val shape = remember(iconShape) { iconShapeFor(iconShape) }
    val cornerDp = when (iconShape) {
        IconShapeStyle.CIRCLE -> 32f
        IconShapeStyle.SQUIRCLE -> 18f
        IconShapeStyle.ROUNDED_SQUARE -> 13f
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "app_tile_scale"
    )

    val isThemed = iconColorTheme != IconColorTheme.ORIGINAL
    val (themeBg, themeFg) = remember(iconColorTheme, customBgHex, customFgHex) {
        resolvedIconColors(iconColorTheme, customBgHex, customFgHex)
    }
    val duotoneFilter = remember(isThemed, themeBg, themeFg) {
        if (isThemed) duotoneColorFilter(themeBg, themeFg) else null
    }

    val styledBitmap = rememberStyledAppBitmap(
        packageName = app.packageName,
        fullIcon = app.icon,
        foregroundIcon = app.foregroundIcon,
        theme = iconColorTheme,
        customBgHex = customBgHex,
        customFgHex = customFgHex
    )

    val tileBackgroundColor = when {
        iconColorTheme == IconColorTheme.PRISM_GLASS -> Color(0xFF0A101E).copy(alpha = 0.86f)
        isThemed -> themeBg
        isLiquidGlass -> if (isDark) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.65f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
    }
    val tileBorderColor = when {
        iconColorTheme == IconColorTheme.PRISM_GLASS -> Color.Transparent
        iconColorTheme == IconColorTheme.CRIMSON_NEON -> Color(0xFFFF1F34).copy(alpha = 0.65f)
        iconColorTheme == IconColorTheme.EMERALD_NEON -> Color(0xFF42FF2A).copy(alpha = 0.65f)
        iconColorTheme == IconColorTheme.SPIDER_CRIMSON -> Color(0xFFFF1726).copy(alpha = 0.55f)
        iconColorTheme == IconColorTheme.ACID_SLIME -> Color(0xFF88EC18).copy(alpha = 0.58f)
        iconColorTheme == IconColorTheme.LIQUID_CHROME -> Color(0xFFE2E8F0).copy(alpha = 0.55f)
        isThemed -> themeFg.copy(alpha = 0.50f)
        isLiquidGlass -> Color.White.copy(alpha = if (isDark) 0.34f else 0.90f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.38f)
    }

    val displayLabel = override?.customLabel?.takeIf { it.isNotBlank() } ?: app.label
    val customImageFile = override?.customIconPath?.let { File(it) }?.takeIf { it.exists() }
    val customSymbol = override?.symbolPreset?.let(::symbolVectorFor)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .testTag("app_tile_${app.packageName}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(iconSize)
                .shadow(
                    elevation = if (isLiquidGlass || isThemed) 8.dp else 4.dp,
                    shape = shape,
                    ambientColor = if (isThemed) themeFg.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.28f),
                    spotColor = if (isThemed) themeFg.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.28f)
                )
                .clip(shape)
                .background(tileBackgroundColor)
                .border(
                    width = if (isThemed || isLiquidGlass) 1.2.dp else 0.75.dp,
                    color = tileBorderColor,
                    shape = shape
                )
        ) {
            when {
                customSymbol != null -> {
                    val symbolTint = if (isThemed) themeFg else MaterialTheme.colorScheme.primary
                    Icon(
                        imageVector = customSymbol,
                        contentDescription = displayLabel,
                        tint = symbolTint,
                        modifier = Modifier.size(iconSize * 0.54f)
                    )
                }
                customImageFile != null -> {
                    AsyncImage(
                        model = customImageFile,
                        contentDescription = displayLabel,
                        contentScale = ContentScale.Crop,
                        colorFilter = duotoneFilter,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                styledBitmap != null -> {
                    val pad = if (isThemed && app.foregroundIcon != null) 7.dp else 0.dp
                    Image(
                        bitmap = styledBitmap,
                        contentDescription = displayLabel,
                        contentScale = if (isThemed && app.foregroundIcon != null) ContentScale.Fit else ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(pad)
                    )
                }
                else -> {
                    Text(
                        text = displayLabel.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isThemed) themeFg else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Overlay design rim/effects (Spider webs, Prism glass glare, Neon bloom, Acid slime drips, Chrome sweep)
            StylizedIconFrameOverlay(
                theme = iconColorTheme,
                cornerRadiusDp = cornerDp
            )
        }

        if (showLabel) {
            Spacer(modifier = Modifier.height(6.dp))
            if (isLiquidGlass) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isDark) Color.White.copy(alpha = 0.12f)
                            else Color.White.copy(alpha = 0.68f)
                        )
                        .border(
                            width = 0.7.dp,
                            color = if (isDark) Color.White.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.92f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                        .testTag("glass_label_${app.packageName}")
                ) {
                    Text(
                        text = displayLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = displayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun FloatingBottomDock(
    emblemChar: String,
    isLiquidGlass: Boolean = true,
    strings: AppStrings,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val dockFillBrush = if (isLiquidGlass) {
        liquidGlassFillBrush(isDark)
    } else {
        val solid = if (isDark) Color(0xFF141720).copy(alpha = 0.94f) else Color.White.copy(alpha = 0.96f)
        Brush.verticalGradient(listOf(solid, solid))
    }
    val dockBorderBrush = if (isLiquidGlass) {
        liquidGlassBorderBrush(isDark)
    } else {
        val b = if (isDark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.10f)
        Brush.verticalGradient(listOf(b, b))
    }

    val nInteraction = remember { MutableInteractionSource() }
    val nPressed by nInteraction.collectIsPressedAsState()
    val nScale by animateFloatAsState(
        targetValue = if (nPressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "n_button_scale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Circular floating N button matching Search pill surface + modern sculpted N design
        Surface(
            onClick = onOpenDrawer,
            interactionSource = nInteraction,
            shape = CircleShape,
            color = Color.Transparent,
            shadowElevation = if (isLiquidGlass) 14.dp else 10.dp,
            modifier = Modifier
                .size(62.dp)
                .scale(nScale)
                .testTag("n_drawer_button")
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = dockFillBrush, shape = CircleShape)
                    .border(width = 1.2.dp, brush = dockBorderBrush, shape = CircleShape)
            ) {
                val cleanChar = emblemChar.trim()
                if (cleanChar.isEmpty() || cleanChar.equals("N", ignoreCase = true)) {
                    ModernGeometricNMonogram(
                        accentColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(34.dp)
                    )
                } else {
                    Text(
                        text = cleanChar,
                        fontFamily = SpaceGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Floating Search Pill matching the N circle button's surface & border
        Surface(
            onClick = onOpenSearch,
            shape = RoundedCornerShape(31.dp),
            color = Color.Transparent,
            shadowElevation = if (isLiquidGlass) 14.dp else 10.dp,
            modifier = Modifier
                .weight(1f)
                .height(62.dp)
                .clip(RoundedCornerShape(31.dp))
                .background(brush = dockFillBrush, shape = RoundedCornerShape(31.dp))
                .border(1.2.dp, dockBorderBrush, RoundedCornerShape(31.dp))
                .testTag("floating_search_bar")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = strings.searchPlaceholder,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = strings.searchPlaceholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun DefaultLauncherGateScreen(
    strings: AppStrings,
    onSetDefaultClick: () -> Unit,
    onContinuePreviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF080A0E))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .testTag("default_launcher_gate")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.widthIn(max = 460.dp)
        ) {
            // Iconic modern N emblem preview
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(94.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.16f), Color(0xFF141821))
                        )
                    )
                    .border(
                        width = 1.6.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.45f), MaterialTheme.colorScheme.primary)
                        ),
                        shape = CircleShape
                    )
            ) {
                ModernGeometricNMonogram(
                    accentColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
            ) {
                Text(
                    text = strings.gateBadge,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = strings.gateTitle,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = strings.gateSubtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF9AA3B5),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                GateFeaturePill(icon = Icons.Rounded.Home, text = strings.gateStep1)
                GateFeaturePill(icon = Icons.Rounded.Search, text = strings.gateStep2)
                GateFeaturePill(icon = Icons.Rounded.Palette, text = strings.gateStep3)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onSetDefaultClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("set_default_launcher_button")
            ) {
                Text(
                    text = strings.gateCtaPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onContinuePreviewClick,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("skip_default_launcher_button")
            ) {
                Text(
                    text = strings.gateCtaSecondary,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF9AA3B5)
                )
            }
        }
    }
}

@Composable
private fun GateFeaturePill(icon: ImageVector, text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.12f), Color(0xFF13161F).copy(alpha = 0.85f))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.06f))
                ),
                shape = RoundedCornerShape(18.dp)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
        }
    }
}

@Composable
fun GlassWidgetCard(
    modifier: Modifier = Modifier,
    isLiquidGlass: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    LiquidGlassSurface(
        shape = MaterialTheme.shapes.large,
        isLiquidGlass = isLiquidGlass,
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        content = content
    )
}

@Composable
fun EmptyAppsHint(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Apps,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
