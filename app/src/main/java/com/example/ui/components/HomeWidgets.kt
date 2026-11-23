package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.R
import com.example.model.AppFolder
import com.example.model.BatteryTelemetry
import com.example.model.ClockStyle
import com.example.model.CustomAppOverride
import com.example.model.DeviceTelemetry
import com.example.model.EmblemStyle
import com.example.model.HomeWidgetId
import com.example.model.IconColorTheme
import com.example.model.IconShapeStyle
import com.example.model.InstalledApp
import com.example.model.LauncherConfig
import com.example.model.PhotoFrameStyle
import com.example.ui.strings.AppStrings
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun MinimalClockHeader(
    now: LocalDateTime,
    config: LauncherConfig,
    onCycleClockStyle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModernClockHeader(
        now = now,
        config = config,
        onCycleClockStyle = onCycleClockStyle,
        modifier = modifier
    )
}

@Composable
fun HomeEditModeBar(
    selectedApp: InstalledApp?,
    canMovePrev: Boolean,
    canMoveNext: Boolean,
    canMovePage: Boolean,
    isLiquidGlass: Boolean,
    strings: AppStrings,
    onMovePrev: () -> Unit,
    onMoveNext: () -> Unit,
    onMovePage: () -> Unit,
    onCustomizeApp: () -> Unit,
    onRemoveApp: () -> Unit,
    onUninstallApp: () -> Unit,
    onOpenAddHub: () -> Unit = {},
    onOpenThemesHub: () -> Unit = {},
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    LiquidGlassSurface(
        shape = RoundedCornerShape(24.dp),
        isLiquidGlass = isLiquidGlass,
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_edit_mode_bar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = strings.editLayoutHint,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    onClick = onDone,
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("done_edit_mode_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = strings.editLayoutDone,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strings.editLayoutDone,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                EditActionChip(
                    icon = Icons.Rounded.Add,
                    label = strings.editAddQuick,
                    enabled = true,
                    onClick = onOpenAddHub,
                    modifier = Modifier.testTag("edit_bar_add_button")
                )
                EditActionChip(
                    icon = Icons.Rounded.Palette,
                    label = strings.editThemesQuick,
                    enabled = true,
                    onClick = onOpenThemesHub,
                    modifier = Modifier.testTag("edit_bar_themes_button")
                )
                if (selectedApp != null) {
                    EditActionChip(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        label = strings.movePrev,
                        enabled = canMovePrev,
                        onClick = onMovePrev,
                        modifier = Modifier.testTag("move_app_prev_button")
                    )
                    EditActionChip(
                        icon = Icons.AutoMirrored.Rounded.ArrowForward,
                        label = strings.moveNext,
                        enabled = canMoveNext,
                        onClick = onMoveNext,
                        modifier = Modifier.testTag("move_app_next_button")
                    )
                    if (canMovePage) {
                        EditActionChip(
                            icon = Icons.Rounded.SwapHoriz,
                            label = strings.movePage,
                            enabled = true,
                            onClick = onMovePage,
                            modifier = Modifier.testTag("move_app_page_button")
                        )
                    }
                    EditActionChip(
                        icon = Icons.Rounded.Edit,
                        label = strings.customizeAppShort,
                        enabled = true,
                        onClick = onCustomizeApp,
                        modifier = Modifier.testTag("edit_app_customize_button")
                    )
                    EditActionChip(
                        icon = Icons.Rounded.Close,
                        label = strings.removeFromHome,
                        enabled = true,
                        tint = Color(0xFFFF6B4A),
                        onClick = onRemoveApp,
                        modifier = Modifier.testTag("edit_app_remove_button")
                    )
                    if (!selectedApp.isInternalApp) {
                        EditActionChip(
                            icon = Icons.Rounded.DeleteForever,
                            label = strings.uninstallApp,
                            enabled = true,
                            tint = Color(0xFFFF2D46),
                            onClick = onUninstallApp,
                            modifier = Modifier.testTag("edit_app_uninstall_button")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EditActionChip(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    tint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        color = if (enabled) tint.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (enabled) tint else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun ScatteredAppsGrid(
    apps: List<InstalledApp>,
    iconShape: IconShapeStyle,
    iconColorTheme: IconColorTheme,
    customBgHex: Long,
    customFgHex: Long,
    customBgEndHex: Long = 0xFF2B0F1CL,
    isIconGradientEnabled: Boolean = true,
    overrides: Map<String, CustomAppOverride>,
    iconSizeDp: Int = 58,
    isLiquidGlass: Boolean = true,
    isEditMode: Boolean = false,
    selectedEditPackage: String? = null,
    onLaunchApp: (InstalledApp) -> Unit,
    onLongPressApp: (InstalledApp) -> Unit = {},
    onSelectEditApp: (InstalledApp) -> Unit = {},
    onMiniRemoveApp: (InstalledApp) -> Unit = {},
    onMoveApp: (draggedPackage: String, targetPackage: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    if (apps.isEmpty()) return

    val resolvedIconSize = iconSizeDp.coerceIn(44, 78).dp
    var gridCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val itemRects = remember { mutableStateMapOf<String, Rect>() }
    var draggedPackage by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val currentOnMoveApp by rememberUpdatedState(onMoveApp)
    val currentOnLongPressApp by rememberUpdatedState(onLongPressApp)

    val jiggleTransition = rememberInfiniteTransition(label = "edit_jiggle")
    val jiggleAngle by jiggleTransition.animateFloat(
        initialValue = -2.2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 150),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jiggle_angle"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { gridCoords = it }
            .testTag("scattered_apps_grid")
    ) {
        val columns = when {
            maxWidth >= 520.dp && iconSizeDp <= 48 -> 6
            maxWidth >= 520.dp && iconSizeDp >= 70 -> 4
            maxWidth >= 520.dp -> 5
            iconSizeDp <= 48 -> 5
            iconSizeDp >= 70 -> 3
            else -> 4
        }
        val rows = apps.chunked(columns)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            rows.forEachIndexed { rowIdx, rowApps ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowApps.forEachIndexed { colIdx, app ->
                        val isDragging = draggedPackage == app.packageName
                        val isSelected = isEditMode && selectedEditPackage == app.packageName
                        val dragScale by animateFloatAsState(
                            targetValue = if (isDragging) 1.12f else 1f,
                            animationSpec = spring(),
                            label = "drag_scale"
                        )
                        val tilt = when {
                            isDragging || !isEditMode -> 0f
                            (rowIdx + colIdx) % 2 == 0 -> jiggleAngle
                            else -> -jiggleAngle
                        }

                        val dragModifier = if (!isEditMode) {
                            Modifier.pointerInput(app.packageName) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        draggedPackage = app.packageName
                                        dragOffset = Offset.Zero
                                        currentOnLongPressApp(app)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount
                                        val activeRect = itemRects[app.packageName]
                                        if (activeRect != null) {
                                            val draggedCenter = activeRect.center + dragOffset
                                            val hitThreshold = min(activeRect.width, activeRect.height) * 0.52f
                                            val targetEntry = itemRects.entries.firstOrNull { (pkg, rect) ->
                                                pkg != app.packageName &&
                                                    hypot(
                                                        (draggedCenter.x - rect.center.x).toDouble(),
                                                        (draggedCenter.y - rect.center.y).toDouble()
                                                    ) <= hitThreshold
                                            }
                                            if (targetEntry != null) {
                                                val deltaShift = targetEntry.value.center - activeRect.center
                                                currentOnMoveApp(app.packageName, targetEntry.key)
                                                dragOffset -= deltaShift
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        draggedPackage = null
                                        dragOffset = Offset.Zero
                                    },
                                    onDragCancel = {
                                        draggedPackage = null
                                        dragOffset = Offset.Zero
                                    }
                                )
                            }
                        } else {
                            Modifier.pointerInput(app.packageName, isEditMode) {
                                detectDragGestures(
                                    onDragStart = {
                                        draggedPackage = app.packageName
                                        dragOffset = Offset.Zero
                                        currentOnLongPressApp(app)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragOffset += dragAmount
                                        val activeRect = itemRects[app.packageName]
                                        if (activeRect != null) {
                                            val draggedCenter = activeRect.center + dragOffset
                                            val hitThreshold = min(activeRect.width, activeRect.height) * 0.52f
                                            val targetEntry = itemRects.entries.firstOrNull { (pkg, rect) ->
                                                pkg != app.packageName &&
                                                    hypot(
                                                        (draggedCenter.x - rect.center.x).toDouble(),
                                                        (draggedCenter.y - rect.center.y).toDouble()
                                                    ) <= hitThreshold
                                            }
                                            if (targetEntry != null) {
                                                val deltaShift = targetEntry.value.center - activeRect.center
                                                currentOnMoveApp(app.packageName, targetEntry.key)
                                                dragOffset -= deltaShift
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        draggedPackage = null
                                        dragOffset = Offset.Zero
                                    },
                                    onDragCancel = {
                                        draggedPackage = null
                                        dragOffset = Offset.Zero
                                    }
                                )
                            }
                        }

                        Box(
                            contentAlignment = Alignment.TopCenter,
                            modifier = Modifier
                                .weight(1f)
                                .zIndex(if (isDragging) 20f else 0f)
                                .onGloballyPositioned { coords ->
                                    gridCoords?.let { root ->
                                        if (root.isAttached && coords.isAttached) {
                                            itemRects[app.packageName] = root.localBoundingBoxOf(coords, clipBounds = false)
                                        }
                                    }
                                }
                                .offset {
                                    if (isDragging) {
                                        IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt())
                                    } else {
                                        IntOffset.Zero
                                    }
                                }
                                .scale(dragScale)
                                .rotate(tilt)
                                .then(
                                    if (isSelected) {
                                        Modifier
                                            .clip(RoundedCornerShape(18.dp))
                                            .border(
                                                width = 1.6.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(18.dp)
                                            )
                                    } else Modifier
                                )
                                .then(dragModifier)
                        ) {
                            AppIconTile(
                                app = app,
                                iconShape = iconShape,
                                iconColorTheme = iconColorTheme,
                                customBgHex = customBgHex,
                                customFgHex = customFgHex,
                                customBgEndHex = customBgEndHex,
                                isIconGradientEnabled = isIconGradientEnabled,
                                override = overrides[app.packageName],
                                isLiquidGlass = isLiquidGlass,
                                iconSize = resolvedIconSize,
                                showLabel = true,
                                onClick = {
                                    if (isEditMode) onSelectEditApp(app)
                                    else onLaunchApp(app)
                                },
                                onLongClick = { onLongPressApp(app) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (isEditMode && !isDragging) {
                                Surface(
                                    onClick = { onMiniRemoveApp(app) },
                                    shape = CircleShape,
                                    color = Color(0xFFFF2D46),
                                    shadowElevation = 4.dp,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 1.dp, end = 4.dp)
                                        .size(22.dp)
                                        .border(1.2.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                                        .testTag("mini_remove_badge_${app.packageName}")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    repeat(columns - rowApps.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun EmblemLogoWidget(
    emblemChar: String,
    tagline: String,
    emblemStyle: EmblemStyle,
    isLiquidGlass: Boolean = true,
    strings: AppStrings,
    onCustomizeEmblem: () -> Unit,
    onRemoveWidget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val surfaceHigh = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    val displayTagline = tagline.ifBlank { strings.defaultTagline }

    GlassWidgetCard(
        isLiquidGlass = isLiquidGlass,
        onClick = onCustomizeEmblem,
        modifier = modifier.testTag("widget_emblem_logo")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            val shape = when (emblemStyle) {
                EmblemStyle.RIBBON_DARK -> RoundedCornerShape(20.dp)
                EmblemStyle.NEON_RING -> CircleShape
                EmblemStyle.MINIMAL_PILL -> RoundedCornerShape(16.dp)
            }
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .clip(shape)
                    .background(
                        when (emblemStyle) {
                            EmblemStyle.RIBBON_DARK -> Brush.linearGradient(listOf(Color(0xFF1A1E28), Color(0xFF0C0E13)))
                            EmblemStyle.NEON_RING -> Brush.radialGradient(listOf(primary.copy(alpha = 0.25f), surfaceHigh))
                            EmblemStyle.MINIMAL_PILL -> Brush.linearGradient(listOf(primary, primary.copy(alpha = 0.8f)))
                        }
                    )
                    .border(
                        width = if (emblemStyle == EmblemStyle.NEON_RING) 2.5.dp else 1.5.dp,
                        color = if (emblemStyle == EmblemStyle.MINIMAL_PILL) Color.White.copy(alpha = 0.25f) else primary,
                        shape = shape
                    )
            ) {
                Text(
                    text = emblemChar.ifBlank { "N" },
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = if (emblemStyle == EmblemStyle.NEON_RING) onSurface else Color.White
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.widgetName(HomeWidgetId.EMBLEM_LOGO),
                    style = MaterialTheme.typography.titleMedium,
                    color = onSurface
                )
                Text(
                    text = displayTagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onRemoveWidget) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = strings.removeBadge,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PageFoldersGrid(
    folders: List<AppFolder>,
    appsByPackage: Map<String, InstalledApp>,
    iconShape: IconShapeStyle,
    iconColorTheme: IconColorTheme,
    customBgHex: Long,
    customFgHex: Long,
    customBgEndHex: Long = 0xFF2B0F1CL,
    isIconGradientEnabled: Boolean = true,
    overrides: Map<String, CustomAppOverride>,
    iconSizeDp: Int = 58,
    isLiquidGlass: Boolean = true,
    strings: AppStrings,
    onLaunchApp: (InstalledApp) -> Unit,
    onCustomizeApp: (InstalledApp) -> Unit,
    onOpenFolder: (AppFolder) -> Unit,
    modifier: Modifier = Modifier
) {
    if (folders.isEmpty()) return
    BoxWithConstraints(modifier = modifier.fillMaxWidth().testTag("widget_smart_folders")) {
        val columns = if (maxWidth >= 560.dp) 3 else 2
        val rows = folders.chunked(columns)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            rows.forEach { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowItems.forEach { folder ->
                        FolderPodCard(
                            folder = folder,
                            resolvedApps = folder.packageNames.mapNotNull { appsByPackage[it] },
                            iconShape = iconShape,
                            iconColorTheme = iconColorTheme,
                            customBgHex = customBgHex,
                            customFgHex = customFgHex,
                            customBgEndHex = customBgEndHex,
                            isIconGradientEnabled = isIconGradientEnabled,
                            overrides = overrides,
                            iconSizeDp = iconSizeDp,
                            isLiquidGlass = isLiquidGlass,
                            strings = strings,
                            onLaunchApp = onLaunchApp,
                            onCustomizeApp = onCustomizeApp,
                            onOpenFolder = { onOpenFolder(folder) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    repeat(columns - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderPodCard(
    folder: AppFolder,
    resolvedApps: List<InstalledApp>,
    iconShape: IconShapeStyle,
    iconColorTheme: IconColorTheme,
    customBgHex: Long,
    customFgHex: Long,
    customBgEndHex: Long = 0xFF2B0F1CL,
    isIconGradientEnabled: Boolean = true,
    overrides: Map<String, CustomAppOverride>,
    iconSizeDp: Int = 58,
    isLiquidGlass: Boolean,
    strings: AppStrings,
    onLaunchApp: (InstalledApp) -> Unit,
    onCustomizeApp: (InstalledApp) -> Unit,
    onOpenFolder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val folderAccent = Color(folder.accentHex)
    GlassWidgetCard(
        isLiquidGlass = isLiquidGlass,
        onClick = onOpenFolder,
        modifier = modifier.testTag("folder_card_${folder.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(folderAccent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = folder.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = folderAccent.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = "${resolvedApps.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = folderAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (resolvedApps.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                ) {
                    Text(
                        text = strings.folderEmptyHint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            } else {
                val previewApps = resolvedApps.take(4)
                val gridRows = previewApps.chunked(2)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    gridRows.forEach { pair ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            pair.forEach { app ->
                                AppIconTile(
                                    app = app,
                                    iconShape = iconShape,
                                    iconColorTheme = iconColorTheme,
                                    customBgHex = customBgHex,
                                    customFgHex = customFgHex,
                                    customBgEndHex = customBgEndHex,
                                    isIconGradientEnabled = isIconGradientEnabled,
                                    override = overrides[app.packageName],
                                    isLiquidGlass = isLiquidGlass,
                                    iconSize = (iconSizeDp * 0.76f).roundToInt().coerceIn(34, 54).dp,
                                    showLabel = true,
                                    onClick = { onLaunchApp(app) },
                                    onLongClick = { onCustomizeApp(app) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChargingPodWidget(
    battery: BatteryTelemetry,
    isLiquidGlass: Boolean = true,
    strings: AppStrings,
    onOpenBatterySettings: () -> Unit,
    onRemoveWidget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "charging_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val chargeColor = if (battery.isCharging) {
        Color(0xFF1FC77E)
    } else if (battery.level <= 20) {
        Color(0xFFFF4D2D)
    } else {
        MaterialTheme.colorScheme.primary
    }

    GlassWidgetCard(
        isLiquidGlass = isLiquidGlass,
        onClick = onOpenBatterySettings,
        modifier = modifier.testTag("widget_charging_pod")
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(80.dp)
                ) {
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = 9.dp.toPx()
                        val diameter = size.minDimension - stroke
                        val topLeft = Offset(stroke / 2f, stroke / 2f)
                        drawArc(
                            color = trackColor,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = chargeColor.copy(alpha = if (battery.isCharging) pulseAlpha else 1f),
                            startAngle = -90f,
                            sweepAngle = (battery.level.coerceIn(0, 100) / 100f) * 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = Size(diameter, diameter),
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (battery.isCharging) Icons.Rounded.Bolt else Icons.Rounded.BatteryStd,
                            contentDescription = strings.chargingTitle,
                            tint = chargeColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "${battery.level}%",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.BatteryChargingFull,
                            contentDescription = null,
                            tint = chargeColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.chargingTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (battery.isCharging) {
                            "${strings.chargingActive} • ${strings.chargingSourceName(battery.source)}"
                        } else {
                            "${strings.chargingIdle} • ${strings.chargingSourceName(battery.source)}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (battery.isCharging) chargeColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TelemetryChip(
                            label = strings.tempLabel,
                            value = "${String.format(Locale.ENGLISH, "%.1f", battery.temperatureCelsius)}°C"
                        )
                        TelemetryChip(
                            label = strings.voltageLabel,
                            value = "${String.format(Locale.ENGLISH, "%.2f", battery.voltageVolts)}V"
                        )
                    }
                }
            }

            IconButton(
                onClick = onRemoveWidget,
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = strings.removeBadge,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun TelemetryChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun PhotoFrameWidget(
    photoPath: String?,
    caption: String,
    frameStyle: PhotoFrameStyle,
    isLiquidGlass: Boolean = true,
    strings: AppStrings,
    onPickPhoto: () -> Unit,
    onCustomizeFrame: () -> Unit,
    onRemoveWidget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val frameShape = when (frameStyle) {
        PhotoFrameStyle.BENTO_ROUNDED -> RoundedCornerShape(26.dp)
        PhotoFrameStyle.ARCH_WINDOW -> RoundedCornerShape(topStart = 96.dp, topEnd = 96.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
        PhotoFrameStyle.POLAROID_CARD -> RoundedCornerShape(16.dp)
    }
    val resolvedCaption = caption.ifBlank { strings.defaultPhotoCaption }
    val customFile = photoPath?.let { File(it) }?.takeIf { it.exists() }

    GlassWidgetCard(
        isLiquidGlass = isLiquidGlass,
        onClick = onCustomizeFrame,
        modifier = modifier.testTag("widget_photo_frame")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (frameStyle == PhotoFrameStyle.POLAROID_CARD) 14.dp else 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(185.dp)
                    .clip(frameShape)
            ) {
                if (customFile != null) {
                    AsyncImage(
                        model = customFile,
                        contentDescription = resolvedCaption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.img_photo_widget_default),
                        contentDescription = resolvedCaption,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                IconButton(
                    onClick = onRemoveWidget,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = strings.removeBadge,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.68f))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.photoWidgetTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                            Text(
                                text = resolvedCaption,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        FilledTonalIconButton(
                            onClick = onPickPhoto,
                            modifier = Modifier.testTag("photo_widget_pick_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PhotoLibrary,
                                contentDescription = strings.pickPhoto
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MediaControlWidget(
    volumePercent: Int,
    isPlayingVisual: Boolean,
    isLiquidGlass: Boolean = true,
    strings: AppStrings,
    onPrev: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    onOpenMusicApp: () -> Unit,
    onRemoveWidget: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassWidgetCard(isLiquidGlass = isLiquidGlass, modifier = modifier.testTag("widget_media_control")) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = if (isPlayingVisual) Icons.Rounded.GraphicEq else Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = strings.mediaWidgetTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.mediaSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row {
                    IconButton(onClick = onOpenMusicApp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = strings.openMusicApp,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onRemoveWidget) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = strings.removeBadge,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledTonalIconButton(onClick = onPrev, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous")
                }
                Surface(
                    onClick = onPlayPause,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isPlayingVisual) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                FilledTonalIconButton(onClick = onNext, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.VolumeUp,
                    contentDescription = strings.mediaVolume,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = volumePercent.toFloat(),
                    onValueChange = { onVolumeChange(it.roundToInt()) },
                    valueRange = 0f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$volumePercent%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SystemPulseWidget(
    device: DeviceTelemetry,
    isLiquidGlass: Boolean = true,
    strings: AppStrings,
    onWifiClick: () -> Unit,
    onBluetoothClick: () -> Unit,
    onDisplayClick: () -> Unit,
    onSetDefaultHomeClick: () -> Unit,
    onRemoveWidget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ramProgress = if (device.totalRamMb > 0) {
        (device.usedRamMb.toFloat() / device.totalRamMb.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val usedStorageGb = (device.totalStorageGb - device.freeStorageGb).coerceAtLeast(0f)
    val storageProgress = if (device.totalStorageGb > 0f) {
        (usedStorageGb / device.totalStorageGb).coerceIn(0f, 1f)
    } else 0f

    GlassWidgetCard(isLiquidGlass = isLiquidGlass, modifier = modifier.testTag("widget_system_pulse")) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = strings.systemPulseTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onRemoveWidget) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = strings.removeBadge,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                PulseStatBlock(
                    icon = Icons.Rounded.Memory,
                    label = strings.ramLabel,
                    detail = "${String.format(Locale.ENGLISH, "%.1f", device.usedRamMb / 1024f)} / ${String.format(Locale.ENGLISH, "%.1f", device.totalRamMb / 1024f)} GB",
                    progress = ramProgress,
                    modifier = Modifier.weight(1f)
                )
                PulseStatBlock(
                    icon = Icons.Rounded.Storage,
                    label = strings.storageLabel,
                    detail = "${String.format(Locale.ENGLISH, "%.0f", device.freeStorageGb)} GB",
                    progress = storageProgress,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickTogglePill(Icons.Rounded.Wifi, strings.wifiShortcut, onWifiClick, Modifier.weight(1f))
                QuickTogglePill(Icons.Rounded.Bluetooth, strings.bluetoothShortcut, onBluetoothClick, Modifier.weight(1f))
                QuickTogglePill(Icons.Rounded.Brightness6, strings.displayShortcut, onDisplayClick, Modifier.weight(1f))
                QuickTogglePill(Icons.Rounded.Home, strings.setHomeShortcut, onSetDefaultHomeClick, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PulseStatBlock(
    icon: ImageVector,
    label: String,
    detail: String,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(detail, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50))
            )
        }
    }
}

@Composable
private fun QuickTogglePill(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        modifier = modifier.height(64.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
