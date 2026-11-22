package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DashboardCustomize
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AppFolder
import com.example.model.AppSymbolPreset
import com.example.model.ClockStyle
import com.example.model.CustomAppOverride
import com.example.model.EmblemStyle
import com.example.model.HomePageData
import com.example.model.HomeWidgetId
import com.example.model.IconColorTheme
import com.example.model.IconShapeStyle
import com.example.model.InstalledApp
import com.example.model.LauncherAccent
import com.example.model.LauncherConfig
import com.example.model.MAX_HOME_PAGES
import com.example.model.MAX_ICON_SIZE_DP
import com.example.model.MIN_ICON_SIZE_DP
import com.example.model.PhotoFrameStyle
import com.example.model.ThemeMode
import com.example.model.WallpaperChoice
import com.example.ui.strings.AppStrings
import com.example.ui.theme.FredokaFontFamily
import com.example.ui.theme.SixCapsFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import kotlin.math.roundToInt

private val FolderAccentChoices = listOf(
    0xFFFF4D2DL,
    0xFF3D7EFFL,
    0xFF1FC77EL,
    0xFFF59E0BL,
    0xFFAA66FFL
)

private val IconColorSwatches = listOf(
    0xFF0A0A0DL,
    0xFF181C24L,
    0xFFFFFFFFL,
    0xFFFF1E38L,
    0xFF3D7EFFL,
    0xFF1FC77EL,
    0xFFFFB800L,
    0xFFA855F7L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisualCustomizeHubSheet(
    config: LauncherConfig,
    currentPageIndex: Int,
    allApps: List<InstalledApp>,
    strings: AppStrings,
    initialTab: Int = 0,
    hubSessionId: Int = 0,
    onToggleAppOnPage: (String) -> Unit,
    onOpenCreateFolder: () -> Unit,
    onToggleWidgetOnPage: (HomeWidgetId) -> Unit,
    onIconColorThemeChange: (IconColorTheme, Long?, Long?) -> Unit,
    onIconShapeChange: (IconShapeStyle) -> Unit,
    onIconSizeChange: (Int) -> Unit,
    onSelectPage: (Int) -> Unit,
    onAddNewPage: () -> Unit,
    onDeletePage: (Int) -> Unit,
    onWallpaperChange: (WallpaperChoice) -> Unit,
    onPickCustomWallpaper: () -> Unit,
    onPickCustomVideoWallpaper: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLiquidGlassChange: (Boolean) -> Unit,
    onClockStyleChange: (ClockStyle) -> Unit,
    onEnterHomeEditMode: () -> Unit,
    onAccentChange: (LauncherAccent) -> Unit,
    onUpdateEmblem: (String, String, EmblemStyle) -> Unit,
    onCustomizeAppClick: (InstalledApp) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember(hubSessionId, initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 3)) }
    val currentPage = config.pages.getOrElse(currentPageIndex) { HomePageData("page_main") }
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val sheetBg = if (config.isLiquidGlassEnabled) {
        if (isDark) Color(0xFF101520).copy(alpha = 0.88f) else Color(0xFFF5F7FC).copy(alpha = 0.90f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 6.dp)
                .testTag("visual_customize_hub")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "${strings.hubTitle} • ${strings.pageLabel(currentPageIndex)}",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = strings.hubSubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_customize_hub_button")
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = strings.closeCd)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                HubVisualTab(
                    icon = Icons.Rounded.Apps,
                    label = strings.tabAddApps,
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f).testTag("hub_tab_apps")
                )
                HubVisualTab(
                    icon = Icons.Rounded.DashboardCustomize,
                    label = strings.tabAddWidgets,
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f).testTag("hub_tab_widgets")
                )
                HubVisualTab(
                    icon = Icons.Rounded.Palette,
                    label = strings.tabIconColors,
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.weight(1f).testTag("hub_tab_colors")
                )
                HubVisualTab(
                    icon = Icons.Rounded.ViewCarousel,
                    label = strings.tabPagesLook,
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    modifier = Modifier.weight(1f).testTag("hub_tab_pages")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp, max = 460.dp)
            ) {
                when (selectedTab) {
                    0 -> HubAppsAndFoldersTab(
                        currentPage = currentPage,
                        allApps = allApps,
                        config = config,
                        strings = strings,
                        onToggleAppOnPage = onToggleAppOnPage,
                        onOpenCreateFolder = onOpenCreateFolder,
                        onEnterHomeEditMode = onEnterHomeEditMode,
                        onCustomizeAppClick = onCustomizeAppClick
                    )
                    1 -> HubWidgetsPickerTab(
                        currentPage = currentPage,
                        strings = strings,
                        onToggleWidget = onToggleWidgetOnPage
                    )
                    2 -> HubIconColorsTab(
                        config = config,
                        strings = strings,
                        onIconColorThemeChange = onIconColorThemeChange,
                        onIconShapeChange = onIconShapeChange,
                        onIconSizeChange = onIconSizeChange
                    )
                    else -> HubPagesAndLookTab(
                        config = config,
                        currentPageIndex = currentPageIndex,
                        strings = strings,
                        onSelectPage = onSelectPage,
                        onAddNewPage = onAddNewPage,
                        onDeletePage = onDeletePage,
                        onWallpaperChange = onWallpaperChange,
                        onPickCustomWallpaper = onPickCustomWallpaper,
                        onPickCustomVideoWallpaper = onPickCustomVideoWallpaper,
                        onThemeModeChange = onThemeModeChange,
                        onLiquidGlassChange = onLiquidGlassChange,
                        onClockStyleChange = onClockStyleChange,
                        onAccentChange = onAccentChange,
                        onUpdateEmblem = onUpdateEmblem
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun HubVisualTab(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.height(64.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HubAppsAndFoldersTab(
    currentPage: HomePageData,
    allApps: List<InstalledApp>,
    config: LauncherConfig,
    strings: AppStrings,
    onToggleAppOnPage: (String) -> Unit,
    onOpenCreateFolder: () -> Unit,
    onEnterHomeEditMode: () -> Unit,
    onCustomizeAppClick: (InstalledApp) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(
            onClick = onOpenCreateFolder,
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hub_create_folder_card")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(14.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Rounded.Folder, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(strings.addFolderCard, style = MaterialTheme.typography.titleMedium)
                    Text(
                        strings.addFolderDesc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(Icons.Rounded.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LiquidGlassSurface(
            shape = RoundedCornerShape(18.dp),
            isLiquidGlass = config.isLiquidGlassEnabled,
            onClick = onEnterHomeEditMode,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hub_enter_edit_mode_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.OpenWith,
                        contentDescription = strings.editHomeAppsAction,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = strings.editHomeAppsAction,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Text(strings.pinAppsTitle, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(allApps, key = { it.packageName }) { app ->
                val pinned = app.packageName in currentPage.pinnedPackages
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (pinned) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                        .border(
                            width = if (pinned) 1.5.dp else 0.dp,
                            color = if (pinned) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(18.dp)
                        )
                ) {
                    AppIconTile(
                        app = app,
                        iconShape = config.iconShape,
                        iconColorTheme = config.iconColorTheme,
                        customBgHex = config.customIconBgHex,
                        customFgHex = config.customIconFgHex,
                        override = config.customAppOverrides[app.packageName],
                        isLiquidGlass = config.isLiquidGlassEnabled,
                        iconSize = 46.dp,
                        showLabel = true,
                        onClick = { onToggleAppOnPage(app.packageName) },
                        onLongClick = { onCustomizeAppClick(app) }
                    )
                    if (pinned) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = strings.addedBadge,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HubWidgetsPickerTab(
    currentPage: HomePageData,
    strings: AppStrings,
    onToggleWidget: (HomeWidgetId) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(strings.widgetsPickerTitle, style = MaterialTheme.typography.labelLarge)
        HomeWidgetId.entries.forEach { widgetId ->
            val added = widgetId in currentPage.widgets
            val (icon, tint) = when (widgetId) {
                HomeWidgetId.EMBLEM_LOGO -> Icons.Rounded.AutoAwesome to Color(0xFFFF4D2D)
                HomeWidgetId.CHARGING_POD -> Icons.Rounded.BatteryChargingFull to Color(0xFF1FC77E)
                HomeWidgetId.PHOTO_FRAME -> Icons.Rounded.PhotoLibrary to Color(0xFF3D7EFF)
                HomeWidgetId.MEDIA_CONTROL -> Icons.Rounded.MusicNote to Color(0xFFA855F7)
                HomeWidgetId.SYSTEM_PULSE -> Icons.Rounded.Speed to Color(0xFFF59E0B)
            }
            Surface(
                onClick = { onToggleWidget(widgetId) },
                shape = RoundedCornerShape(20.dp),
                color = if (added) tint.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (added) 1.5.dp else 1.dp,
                        color = if (added) tint else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .testTag("widget_picker_${widgetId.name}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(tint.copy(alpha = 0.2f))
                    ) {
                        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(strings.widgetName(widgetId), style = MaterialTheme.typography.titleMedium)
                        Text(
                            strings.widgetDescription(widgetId),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (added) tint else MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = if (added) strings.addedBadge else strings.addBadge,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (added) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HubIconColorsTab(
    config: LauncherConfig,
    strings: AppStrings,
    onIconColorThemeChange: (IconColorTheme, Long?, Long?) -> Unit,
    onIconShapeChange: (IconShapeStyle) -> Unit,
    onIconSizeChange: (Int) -> Unit
) {
    val featuredDesigns = listOf(
        IconColorTheme.SPIDER_CRIMSON,
        IconColorTheme.PRISM_GLASS,
        IconColorTheme.CRIMSON_NEON,
        IconColorTheme.ACID_SLIME,
        IconColorTheme.LIQUID_CHROME,
        IconColorTheme.EMERALD_NEON
    )
    val standardPacks = listOf(
        IconColorTheme.ORIGINAL,
        IconColorTheme.BLACK_RED,
        IconColorTheme.BLACK_WHITE,
        IconColorTheme.BLACK_GOLD,
        IconColorTheme.CYBER_BLUE,
        IconColorTheme.CUSTOM_DUOTONE
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(strings.iconPackTitle, style = MaterialTheme.typography.titleMedium)
        Text(
            strings.iconPackDesc,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = strings.iconDesignsSectionTitle,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))

        featuredDesigns.chunked(2).forEach { pair ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
            ) {
                pair.forEach { theme ->
                    IconThemeOptionCard(
                        theme = theme,
                        selected = config.iconColorTheme == theme,
                        customBgHex = config.customIconBgHex,
                        customFgHex = config.customIconFgHex,
                        isLiquidGlass = config.isLiquidGlassEnabled,
                        label = strings.iconThemeName(theme),
                        onClick = { onIconColorThemeChange(theme, null, null) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = strings.iconColorsSectionTitle,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.height(8.dp))

        standardPacks.chunked(2).forEach { pair ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
            ) {
                pair.forEach { theme ->
                    IconThemeOptionCard(
                        theme = theme,
                        selected = config.iconColorTheme == theme,
                        customBgHex = config.customIconBgHex,
                        customFgHex = config.customIconFgHex,
                        isLiquidGlass = config.isLiquidGlassEnabled,
                        label = strings.iconThemeName(theme),
                        onClick = { onIconColorThemeChange(theme, null, null) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (config.iconColorTheme == IconColorTheme.CUSTOM_DUOTONE) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(strings.customBgColor, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                IconColorSwatches.forEach { hex ->
                    ColorSwatchCircle(
                        color = Color(hex),
                        selected = config.customIconBgHex == hex,
                        onClick = { onIconColorThemeChange(IconColorTheme.CUSTOM_DUOTONE, hex, config.customIconFgHex) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(strings.customFgColor, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                IconColorSwatches.forEach { hex ->
                    ColorSwatchCircle(
                        color = Color(hex),
                        selected = config.customIconFgHex == hex,
                        onClick = { onIconColorThemeChange(IconColorTheme.CUSTOM_DUOTONE, config.customIconBgHex, hex) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(strings.iconShapeTitle, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            IconShapeStyle.entries.forEach { shapeStyle ->
                val selected = config.iconShape == shapeStyle
                val shape = iconShapeFor(shapeStyle)
                Surface(
                    onClick = { onIconShapeChange(shapeStyle) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(shape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(strings.iconShapeName(shapeStyle), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(strings.iconSizeTitle, style = MaterialTheme.typography.labelLarge)
            Text(
                text = "${config.homeIconSizeDp} dp",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("icon_size_value_label")
            )
        }
        Slider(
            value = config.homeIconSizeDp.toFloat(),
            onValueChange = { onIconSizeChange(it.roundToInt()) },
            valueRange = MIN_ICON_SIZE_DP.toFloat()..MAX_ICON_SIZE_DP.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("icon_size_slider")
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(
                46 to strings.iconSizeSmall,
                58 to strings.iconSizeMedium,
                68 to strings.iconSizeLarge,
                76 to strings.iconSizeXl
            ).forEach { (presetDp, label) ->
                val selected = kotlin.math.abs(config.homeIconSizeDp - presetDp) <= 2
                FilterChip(
                    selected = selected,
                    onClick = { onIconSizeChange(presetDp) },
                    label = {
                        Text(
                            text = label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("icon_size_preset_$presetDp")
                )
            }
        }
    }
}

@Composable
private fun IconThemeOptionCard(
    theme: IconColorTheme,
    selected: Boolean,
    customBgHex: Long,
    customFgHex: Long,
    isLiquidGlass: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgPreview = if (theme == IconColorTheme.CUSTOM_DUOTONE) Color(customBgHex) else Color(theme.bgHex)
    val fgPreview = if (theme == IconColorTheme.CUSTOM_DUOTONE) Color(customFgHex) else Color(theme.fgHex)
    val previewGlyph = when (theme) {
        IconColorTheme.SPIDER_CRIMSON -> Icons.Rounded.AutoAwesome
        IconColorTheme.PRISM_GLASS -> Icons.Rounded.Palette
        IconColorTheme.CRIMSON_NEON -> Icons.Rounded.MusicNote
        IconColorTheme.ACID_SLIME -> Icons.Rounded.DashboardCustomize
        IconColorTheme.LIQUID_CHROME -> Icons.Rounded.Speed
        IconColorTheme.EMERALD_NEON -> Icons.Rounded.Apps
        else -> Icons.Rounded.Apps
    }

    LiquidGlassSurface(
        shape = RoundedCornerShape(18.dp),
        isLiquidGlass = isLiquidGlass,
        onClick = onClick,
        modifier = modifier
            .then(
                if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(18.dp))
                else Modifier
            )
            .testTag("icon_theme_${theme.name}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (theme) {
                            IconColorTheme.ORIGINAL -> Color(0xFF283044)
                            IconColorTheme.PRISM_GLASS -> Color(0xFF0A101E)
                            else -> bgPreview
                        }
                    )
                    .border(
                        width = if (theme == IconColorTheme.PRISM_GLASS) 0.dp else 1.4.dp,
                        color = if (theme == IconColorTheme.ORIGINAL) Color.White.copy(0.2f) else fgPreview,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                if (theme == IconColorTheme.PRISM_GLASS) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF23D2FF), Color(0xFFA855F7), Color(0xFFFF7A18))
                                )
                            )
                    ) {
                        Icon(
                            imageVector = previewGlyph,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = previewGlyph,
                        contentDescription = null,
                        tint = if (theme == IconColorTheme.ORIGINAL) Color(0xFF38BDF8) else fgPreview,
                        modifier = Modifier.size(20.dp)
                    )
                }
                StylizedIconFrameOverlay(theme = theme, cornerRadiusDp = 12f)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HubPagesAndLookTab(
    config: LauncherConfig,
    currentPageIndex: Int,
    strings: AppStrings,
    onSelectPage: (Int) -> Unit,
    onAddNewPage: () -> Unit,
    onDeletePage: (Int) -> Unit,
    onWallpaperChange: (WallpaperChoice) -> Unit,
    onPickCustomWallpaper: () -> Unit,
    onPickCustomVideoWallpaper: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLiquidGlassChange: (Boolean) -> Unit,
    onClockStyleChange: (ClockStyle) -> Unit,
    onAccentChange: (LauncherAccent) -> Unit,
    onUpdateEmblem: (String, String, EmblemStyle) -> Unit
) {
    var emblemChar by remember(config.emblemCharacter) { mutableStateOf(config.emblemCharacter) }
    var emblemTagline by remember(config.emblemTagline) { mutableStateOf(config.emblemTagline) }
    val canAddMorePages = config.pages.size < MAX_HOME_PAGES

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(strings.pagesManagerTitle, style = MaterialTheme.typography.labelLarge)
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
            ) {
                Text(
                    text = "${config.pages.size} / $MAX_HOME_PAGES",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("pages_count_badge")
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            config.pages.forEachIndexed { idx, page ->
                val selected = idx == currentPageIndex
                Surface(
                    onClick = { onSelectPage(idx) },
                    shape = RoundedCornerShape(18.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .width(104.dp)
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(18.dp)
                        )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Text(strings.pageLabel(idx), style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = "${page.pinnedPackages.size} • ${page.widgets.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (idx > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = strings.deletePage,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFF4D2D),
                                modifier = Modifier.clickable { onDeletePage(idx) }
                            )
                        }
                    }
                }
            }

            Surface(
                onClick = onAddNewPage,
                enabled = canAddMorePages,
                shape = RoundedCornerShape(18.dp),
                color = if (canAddMorePages) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                },
                modifier = Modifier
                    .width(124.dp)
                    .border(
                        width = 1.dp,
                        color = if (canAddMorePages) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(18.dp)
                    )
                    .testTag("add_new_page_button")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = strings.addNewPage,
                        tint = if (canAddMorePages) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (canAddMorePages) strings.addNewPage else strings.maxPagesReached(MAX_HOME_PAGES),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (canAddMorePages) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(14.dp))

        // iOS Liquid Glass Toggle Card
        LiquidGlassSurface(
            shape = RoundedCornerShape(20.dp),
            isLiquidGlass = config.isLiquidGlassEnabled,
            onClick = { onLiquidGlassChange(!config.isLiquidGlassEnabled) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("liquid_glass_toggle")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF38BDF8).copy(alpha = 0.35f), Color(0xFFA855F7).copy(alpha = 0.30f))
                            )
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(strings.liquidGlassTitle, style = MaterialTheme.typography.titleMedium)
                    Text(
                        strings.liquidGlassDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = config.isLiquidGlassEnabled,
                    onCheckedChange = onLiquidGlassChange
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Clock Style Picker (Minimal / Tall Condensed Glass / Bubbly Overlap Glass)
        Text(strings.clockStyleTitle, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ClockStyle.entries.forEach { style ->
                val selected = config.clockStyle == style
                val previewBrush = Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.94f),
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                )
                LiquidGlassSurface(
                    shape = RoundedCornerShape(18.dp),
                    isLiquidGlass = config.isLiquidGlassEnabled,
                    onClick = { onClockStyleChange(style) },
                    modifier = Modifier
                        .weight(1f)
                        .then(
                            if (selected) Modifier.border(
                                2.dp,
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(18.dp)
                            ) else Modifier
                        )
                        .testTag("clock_style_${style.name}")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 10.dp)
                    ) {
                        val previewTextStyle = when (style) {
                            ClockStyle.MINIMAL_CLEAN -> TextStyle(
                                fontFamily = SpaceGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            ClockStyle.TALL_CONDENSED_GLASS -> TextStyle(
                                fontFamily = SixCapsFontFamily,
                                fontSize = 36.sp,
                                lineHeight = 36.sp,
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
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.height(38.dp)
                        ) {
                            Text(
                                text = "12:00",
                                style = previewTextStyle,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = strings.clockStyleName(style),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Appearance Mode: Auto (Follow System) / Light / Dark
        Text(strings.themeModeTitle, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(
                Triple(ThemeMode.SYSTEM, Icons.Rounded.BrightnessAuto, strings.themeModeName(ThemeMode.SYSTEM)),
                Triple(ThemeMode.LIGHT, Icons.Rounded.LightMode, strings.themeModeName(ThemeMode.LIGHT)),
                Triple(ThemeMode.DARK, Icons.Rounded.DarkMode, strings.themeModeName(ThemeMode.DARK))
            ).forEach { (mode, icon, label) ->
                val selected = config.themeMode == mode
                Surface(
                    onClick = { onThemeModeChange(mode) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("theme_mode_${mode.name}")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Wallpaper Picker (Static, Animated Live, Custom Photo, Custom Video)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Wallpaper, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.wallpaperTitle, style = MaterialTheme.typography.labelLarge)
            }
            Text(
                text = strings.wallpaperAnimatedBadge,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            WallpaperChoice.entries.forEach { wp ->
                val isAnimated = wp == WallpaperChoice.LIVE_AURORA ||
                    wp == WallpaperChoice.LIVE_CYBER_GRID ||
                    wp == WallpaperChoice.LIVE_LIQUID_FLOW ||
                    wp == WallpaperChoice.CUSTOM_VIDEO
                FilterChip(
                    selected = config.wallpaper == wp,
                    onClick = {
                        when {
                            wp == WallpaperChoice.CUSTOM_PHOTO && config.customWallpaperPath == null -> onPickCustomWallpaper()
                            wp == WallpaperChoice.CUSTOM_VIDEO && config.customVideoWallpaperPath == null -> onPickCustomVideoWallpaper()
                            else -> onWallpaperChange(wp)
                        }
                    },
                    leadingIcon = if (isAnimated) {
                        {
                            Icon(
                                imageVector = if (wp == WallpaperChoice.CUSTOM_VIDEO) Icons.Rounded.VideoLibrary else Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    } else null,
                    label = { Text(strings.wallpaperName(wp)) },
                    modifier = Modifier.testTag("wallpaper_chip_${wp.name}")
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onPickCustomWallpaper,
                modifier = Modifier
                    .weight(1f)
                    .testTag("pick_photo_wallpaper_button")
            ) {
                Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.pickPhoto, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            OutlinedButton(
                onClick = onPickCustomVideoWallpaper,
                modifier = Modifier
                    .weight(1f)
                    .testTag("pick_video_wallpaper_button")
            ) {
                Icon(Icons.Rounded.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.pickVideoWallpaper, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            LauncherAccent.entries.forEach { acc ->
                FilterChip(
                    selected = config.accent == acc,
                    onClick = { onAccentChange(acc) },
                    label = { Text(strings.accentName(acc)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(14.dp))

        Text(strings.emblemSettingsTitle, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EmblemStyle.entries.forEach { style ->
                FilterChip(
                    selected = config.emblemStyle == style,
                    onClick = { onUpdateEmblem(emblemChar, emblemTagline, style) },
                    label = { Text(strings.emblemStyleName(style)) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = emblemChar,
                onValueChange = {
                    emblemChar = it.take(2)
                    onUpdateEmblem(emblemChar, emblemTagline, config.emblemStyle)
                },
                label = { Text(strings.emblemCharLabel) },
                singleLine = true,
                modifier = Modifier.width(96.dp)
            )
            OutlinedTextField(
                value = emblemTagline,
                onValueChange = {
                    emblemTagline = it
                    onUpdateEmblem(emblemChar, emblemTagline, config.emblemStyle)
                },
                label = { Text(strings.emblemTaglineLabel) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ColorSwatchCircle(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 2.5.dp else 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.25f),
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    ) {
        if (selected) {
            Icon(
                Icons.Rounded.Check,
                contentDescription = null,
                tint = if (color == Color.White) Color.Black else Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun AppCustomizerDialog(
    app: InstalledApp,
    currentOverride: CustomAppOverride?,
    isPinnedOnCurrentPage: Boolean,
    currentPageFolders: List<AppFolder>,
    isLiquidGlass: Boolean = true,
    strings: AppStrings,
    onLaunch: () -> Unit,
    onTogglePinOnPage: () -> Unit,
    onAddToFolder: (String) -> Unit,
    onPickCustomLogoImage: () -> Unit,
    onSaveCustomization: (customLabel: String?, symbolPreset: AppSymbolPreset) -> Unit,
    onResetDefault: () -> Unit,
    onOpenInfo: () -> Unit,
    onUninstall: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var labelText by remember(app.packageName) { mutableStateOf(currentOverride?.customLabel ?: "") }
    var selectedSymbol by remember(app.packageName) { mutableStateOf(currentOverride?.symbolPreset ?: AppSymbolPreset.NONE) }

    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassSurface(
            shape = RoundedCornerShape(28.dp),
            isLiquidGlass = isLiquidGlass,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .testTag("app_customizer_dialog")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(strings.appCustomizeTitle, style = MaterialTheme.typography.titleLarge)
                        Text(
                            app.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = strings.closeCd)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onLaunch, modifier = Modifier.weight(1f)) {
                        Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.openApp, maxLines = 1)
                    }
                    if (!app.isInternalApp) {
                        OutlinedButton(onClick = onOpenInfo, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Rounded.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.appInfo, maxLines = 1)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = {
                            onTogglePinOnPage()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isPinnedOnCurrentPage) strings.removeFromPage else strings.pinOnPage,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (onUninstall != null) {
                        OutlinedButton(
                            onClick = {
                                onUninstall()
                                onDismiss()
                            },
                            modifier = Modifier.testTag("app_customizer_uninstall_button")
                        ) {
                            Icon(
                                Icons.Rounded.DeleteForever,
                                contentDescription = strings.uninstallApp,
                                tint = Color(0xFFFF2D46),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.uninstallApp, color = Color(0xFFFF2D46), maxLines = 1)
                        }
                    }
                }

                if (currentPageFolders.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        currentPageFolders.forEach { folder ->
                            FilterChip(
                                selected = app.packageName in folder.packageNames,
                                onClick = {
                                    onAddToFolder(folder.id)
                                    onDismiss()
                                },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Folder, contentDescription = null, tint = Color(folder.accentHex), modifier = Modifier.size(16.dp))
                                },
                                label = { Text("${strings.addToFolder}: ${folder.title}") }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                Text(strings.changeAppLogo, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onPickCustomLogoImage, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.pickLogoImage, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    IconButton(onClick = {
                        onResetDefault()
                        onDismiss()
                    }) {
                        Icon(Icons.Rounded.Refresh, contentDescription = strings.resetDefaultLogo)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(strings.symbolLibrary, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))

                val presets = AppSymbolPreset.entries.filter { it != AppSymbolPreset.NONE }
                presets.chunked(5).forEach { rowPresets ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        rowPresets.forEach { preset ->
                            val selected = selectedSymbol == preset
                            val vec = symbolVectorFor(preset)
                            Surface(
                                onClick = {
                                    selectedSymbol = if (selected) AppSymbolPreset.NONE else preset
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.weight(1f).height(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (vec != null) {
                                        Icon(
                                            imageVector = vec,
                                            contentDescription = preset.name,
                                            tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
                        repeat(5 - rowPresets.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = labelText,
                    onValueChange = { labelText = it },
                    label = { Text(strings.renameAppLabel) },
                    placeholder = { Text(app.label) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text(strings.cancel) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onSaveCustomization(labelText.ifBlank { null }, selectedSymbol)
                        onDismiss()
                    }) {
                        Text(strings.save)
                    }
                }
            }
        }
    }
}

@Composable
fun FolderEditorDialog(
    folder: AppFolder?,
    allApps: List<InstalledApp>,
    iconShape: IconShapeStyle,
    iconColorTheme: IconColorTheme,
    customBgHex: Long,
    customFgHex: Long,
    overrides: Map<String, CustomAppOverride>,
    isLiquidGlass: Boolean = true,
    strings: AppStrings,
    onLaunchApp: (InstalledApp) -> Unit,
    onCustomizeApp: (InstalledApp) -> Unit,
    onSaveFolder: (title: String, accentHex: Long, packages: List<String>) -> Unit,
    onDeleteFolder: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    var editingApps by remember(folder?.id) { mutableStateOf(folder == null) }
    var title by remember(folder?.id) { mutableStateOf(folder?.title ?: "") }
    var accentHex by remember(folder?.id) { mutableLongStateOf(folder?.accentHex ?: FolderAccentChoices.first()) }
    val selectedPackages = remember(folder?.id) {
        mutableStateListOf<String>().apply {
            addAll(folder?.packageNames.orEmpty())
        }
    }

    val appsByPkg = remember(allApps) { allApps.associateBy { it.packageName } }
    val resolvedApps = selectedPackages.mapNotNull { appsByPkg[it] }

    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassSurface(
            shape = RoundedCornerShape(28.dp),
            isLiquidGlass = isLiquidGlass,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (folder == null) strings.newFolder else folder.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (folder != null) {
                        IconButton(onClick = { editingApps = !editingApps }) {
                            Icon(Icons.Rounded.Edit, contentDescription = strings.manageApps, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = strings.closeCd)
                    }
                }

                if (!editingApps) {
                    Spacer(modifier = Modifier.height(10.dp))
                    if (resolvedApps.isEmpty()) {
                        EmptyAppsHint(message = strings.folderEmptyHint)
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
                        ) {
                            items(resolvedApps, key = { it.packageName }) { app ->
                                AppIconTile(
                                    app = app,
                                    iconShape = iconShape,
                                    iconColorTheme = iconColorTheme,
                                    customBgHex = customBgHex,
                                    customFgHex = customFgHex,
                                    override = overrides[app.packageName],
                                    isLiquidGlass = isLiquidGlass,
                                    iconSize = 54.dp,
                                    onClick = {
                                        onDismiss()
                                        onLaunchApp(app)
                                    },
                                    onLongClick = {
                                        onDismiss()
                                        onCustomizeApp(app)
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { editingApps = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(strings.manageApps)
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(strings.folderNameLabel) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FolderAccentChoices.forEach { hex ->
                            val isSelected = accentHex == hex
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(hex))
                                    .border(
                                        width = if (isSelected) 2.5.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { accentHex = hex }
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(max = 260.dp)
                    ) {
                        items(allApps, key = { it.packageName }) { app ->
                            val checked = app.packageName in selectedPackages
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (checked) selectedPackages.remove(app.packageName)
                                        else selectedPackages.add(app.packageName)
                                    }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { isChecked ->
                                        if (isChecked) {
                                            if (!checked) selectedPackages.add(app.packageName)
                                        } else {
                                            selectedPackages.remove(app.packageName)
                                        }
                                    }
                                )
                                AppIconTile(
                                    app = app,
                                    iconShape = iconShape,
                                    iconColorTheme = iconColorTheme,
                                    customBgHex = customBgHex,
                                    customFgHex = customFgHex,
                                    override = overrides[app.packageName],
                                    isLiquidGlass = isLiquidGlass,
                                    iconSize = 34.dp,
                                    showLabel = false,
                                    onClick = {}
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = overrides[app.packageName]?.customLabel ?: app.label,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (onDeleteFolder != null) {
                            IconButton(onClick = onDeleteFolder) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.deleteFolder, tint = Color(0xFFFF4D2D))
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = onDismiss) { Text(strings.cancel) }
                        Button(
                            onClick = {
                                onSaveFolder(title, accentHex, selectedPackages.toList())
                                onDismiss()
                            }
                        ) {
                            Text(strings.save)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoFrameEditorDialog(
    currentStyle: PhotoFrameStyle,
    currentCaption: String,
    isLiquidGlass: Boolean = true,
    strings: AppStrings,
    onPickPhoto: () -> Unit,
    onSave: (PhotoFrameStyle, String) -> Unit,
    onDismiss: () -> Unit
) {
    var style by remember { mutableStateOf(currentStyle) }
    var caption by remember { mutableStateOf(currentCaption) }

    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassSurface(
            shape = RoundedCornerShape(28.dp),
            isLiquidGlass = isLiquidGlass,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(strings.photoWidgetTitle, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(onClick = onPickPhoto, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.pickPhoto)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(strings.frameStyleLabel, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PhotoFrameStyle.entries.forEach { entry ->
                        FilterChip(
                            selected = style == entry,
                            onClick = { style = entry },
                            label = { Text(strings.photoStyleName(entry)) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it },
                    label = { Text(strings.photoCaptionLabel) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text(strings.cancel) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onSave(style, caption)
                        onDismiss()
                    }) {
                        Text(strings.save)
                    }
                }
            }
        }
    }
}
