package com.example.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.BatteryMonitorState
import com.example.data.PcFileSystemManager
import com.example.data.rememberBatteryMonitorState
import com.example.model.ActiveOverlay
import com.example.model.HomePageData
import com.example.model.HomeWidgetId
import com.example.model.ThemeMode
import com.example.ui.components.AppCustomizerDialog
import com.example.ui.components.AppDrawerOverlay
import com.example.ui.components.ChargingPodWidget
import com.example.ui.components.DefaultLauncherGateScreen
import com.example.ui.components.EmblemLogoWidget
import com.example.ui.components.FloatingBottomDock
import com.example.ui.components.FolderEditorDialog
import com.example.ui.components.GlassWidgetCard
import com.example.ui.components.HomeEditModeBar
import com.example.ui.components.LauncherWallpaperBackground
import com.example.ui.components.LiquidGlassSurface
import com.example.ui.components.MediaControlWidget
import com.example.ui.components.MinimalClockHeader
import com.example.ui.components.PageFoldersGrid
import com.example.ui.components.PcConversionConfirmDialog
import com.example.ui.components.PhotoFrameEditorDialog
import com.example.ui.components.PhotoFrameWidget
import com.example.ui.components.ScatteredAppsGrid
import com.example.ui.components.SearchOverlay
import com.example.ui.components.SystemPulseWidget
import com.example.ui.components.VisualCustomizeHubSheet
import com.example.ui.components.Windows11DesktopRoot
import com.example.ui.components.pc.CalculatorExeBody
import com.example.ui.strings.AppStrings
import com.example.ui.strings.rememberAppStrings
import com.example.ui.theme.NLauncherTheme
import com.example.viewmodel.LauncherViewModel
import kotlinx.coroutines.launch

@Composable
fun LauncherRoot(viewModel: LauncherViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val batteryMonitorComposeState = rememberBatteryMonitorState(viewModel.batteryMonitorState)
    val config = state.config
    val strings = rememberAppStrings()
    val systemDark = isSystemInDarkTheme()
    val effectiveDarkTheme = when (config.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val systemDirection = LocalLayoutDirection.current
    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val context = LocalContext.current
    val hostActivity = remember(context) { context.findHostActivity() }
    DisposableEffect(config.isPcMode, hostActivity) {
        hostActivity?.requestedOrientation = if (config.isPcMode) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        applyPcFullScreen(hostActivity, config.isPcMode)
        onDispose {
            if (config.isPcMode) {
                applyPcFullScreen(hostActivity, false)
            }
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, config.isPcMode, hostActivity) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshDefaultLauncherStatus()
                applyPcFullScreen(hostActivity, config.isPcMode)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val defaultHomeRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshDefaultLauncherStatus()
    }

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showPhotoEditorDialog by remember { mutableStateOf(false) }

    val photoWidgetPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.importPhotoForWidget(it) }
    }

    val customWallpaperPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.importCustomWallpaper(it) }
    }

    val customVideoWallpaperPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.importCustomVideoWallpaper(it) }
    }

    val customAppLogoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val target = state.customizingApp
        if (uri != null && target != null) {
            viewModel.importCustomAppIcon(target.packageName, uri)
        }
    }

    val exeFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.addExeShortcutFromUri(it) }
    }

    val pcFileManager = remember(context) { PcFileSystemManager(context) }
    var pcImportTargetFolder by remember { mutableStateOf("Downloads") }
    val pcFileImportPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val imported = pcFileManager.importDocumentUri(uri, pcImportTargetFolder)
                if (imported != null) {
                    snackbarHostState.showSnackbar("${imported.name} → C:\\$pcImportTargetFolder")
                }
            }
        }
    }

    NLauncherTheme(
        darkTheme = effectiveDarkTheme,
        accent = config.accent,
        useCustomAccent = config.useCustomAccent,
        customAccentHex = config.customAccentHex
    ) {
        if (state.showDefaultLauncherGate) {
            DefaultLauncherGateScreen(
                strings = strings,
                onSetDefaultClick = {
                    runCatching {
                        defaultHomeRoleLauncher.launch(viewModel.createSetDefaultLauncherIntent())
                    }.onFailure {
                        viewModel.openSystemAction(Settings.ACTION_HOME_SETTINGS)
                    }
                },
                onContinuePreviewClick = { viewModel.dismissDefaultLauncherGate() }
            )
        } else {
            BackHandler(enabled = state.isHomeEditMode) {
                viewModel.exitHomeEditMode()
            }

            val pagerState = rememberPagerState(
                initialPage = state.currentPageIndex.coerceIn(0, (config.pages.size - 1).coerceAtLeast(0)),
                pageCount = { config.pages.size.coerceAtLeast(1) }
            )

            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.currentPage }.collect { page ->
                    viewModel.setCurrentPage(page)
                }
            }

            LaunchedEffect(state.currentPageIndex, config.pages.size) {
                val clamped = state.currentPageIndex.coerceIn(0, (config.pages.size - 1).coerceAtLeast(0))
                if (pagerState.currentPage != clamped) {
                    pagerState.animateScrollToPage(clamped)
                }
            }

            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = if (config.isPcMode) WindowInsets(0, 0, 0, 0) else ScaffoldDefaults.contentWindowInsets,
                snackbarHost = {
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier.padding(bottom = if (config.isPcMode) 60.dp else 0.dp)
                    )
                }
            ) { _ ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .let { mod ->
                            if (!config.isPcMode) {
                                mod.pointerInput(Unit) {
                                    detectTapGestures(onLongPress = { viewModel.enterHomeEditMode() })
                                }
                            } else mod
                        }
                ) {
                    if (config.isPcMode) {
                        Windows11DesktopRoot(
                            state = state,
                            strings = strings,
                            effectiveDarkTheme = effectiveDarkTheme,
                            onExitPcMode = { viewModel.setPcMode(false) },
                            onOpenThemesHub = { viewModel.openCustomizeHub(2) },
                            onPickExeFile = { exeFilePicker.launch(arrayOf("*/*")) },
                            onImportFileToPcFolder = { folder ->
                                pcImportTargetFolder = folder.ifBlank { "Downloads" }
                                pcFileImportPicker.launch(arrayOf("*/*"))
                            },
                            onRunExeShortcut = { viewModel.runExeShortcut(it) },
                            onRemoveExeShortcut = { viewModel.removeExeShortcut(it) },
                            onToggleThemeMode = { viewModel.setThemeMode(it) },
                            onOpenSystemAction = { viewModel.openSystemAction(it) },
                            webAppLaunchEvents = viewModel.webAppLaunchEvents
                        )
                    } else {
                        LauncherWallpaperBackground(
                            wallpaper = config.wallpaper,
                            customWallpaperPath = config.customWallpaperPath,
                            customVideoWallpaperPath = config.customVideoWallpaperPath,
                            darkTheme = effectiveDarkTheme
                        )

                    // Multi-page Home Screen: Page 0 (Main) & Page 1 (to the right) + user-added pages
                    Box(
                        contentAlignment = Alignment.TopCenter,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier
                                    .widthIn(max = 640.dp)
                                    .fillMaxSize()
                                    .statusBarsPadding()
                                    .testTag("home_pager")
                            ) { pageIndex ->
                                val pageData = config.pages.getOrElse(pageIndex) { HomePageData("page_$pageIndex") }
                                CompositionLocalProvider(LocalLayoutDirection provides systemDirection) {
                                    HomePageContent(
                                        pageIndex = pageIndex,
                                        pageData = pageData,
                                        state = state,
                                        batteryMonitorState = batteryMonitorComposeState,
                                        strings = strings,
                                        onLaunchApp = { viewModel.launchApp(it, hostActivity) },
                                        onCustomizeApp = { viewModel.openAppCustomizer(it) },
                                        onOpenFolder = { folder -> viewModel.openFolder(pageIndex, folder.id) },
                                        onRemoveWidget = { widgetId -> viewModel.toggleWidgetOnPage(pageIndex, widgetId) },
                                        onOpenCustomizeHub = { tab -> viewModel.openCustomizeHub(tab) },
                                        onEnterHomeEditMode = { viewModel.enterHomeEditMode() },
                                        onCycleClockStyle = { viewModel.cycleClockStyle() },
                                        onLongPressApp = { app -> viewModel.enterHomeEditMode(app.packageName) },
                                        onSelectEditApp = { app -> viewModel.selectEditApp(app.packageName) },
                                        onMiniRemoveApp = { app -> viewModel.removeAppFromPage(pageIndex, app.packageName) },
                                        onMoveApp = { draggedPkg, targetPkg ->
                                            viewModel.moveAppOnPage(pageIndex, draggedPkg, targetPkg)
                                        },
                                        onNudgeApp = { pkg, delta ->
                                            viewModel.nudgeAppOnPage(pageIndex, pkg, delta)
                                        },
                                        onMoveAppToNextPage = { pkg ->
                                            val next = (pageIndex + 1) % config.pages.size.coerceAtLeast(1)
                                            viewModel.moveAppToPage(pageIndex, pkg, next)
                                        },
                                        onUninstallApp = { app -> viewModel.uninstallApp(app) },
                                        onExitEditMode = { viewModel.exitHomeEditMode() },
                                        onPickPhoto = {
                                            photoWidgetPicker.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        onOpenPhotoEditor = { showPhotoEditorDialog = true },
                                        onOpenBatterySettings = { viewModel.openSystemAction(Settings.ACTION_BATTERY_SAVER_SETTINGS) },
                                        onPrevMedia = { viewModel.mediaPrevious() },
                                        onPlayPauseMedia = { viewModel.toggleMediaPlayPause() },
                                        onNextMedia = { viewModel.mediaNext() },
                                        onVolumeChange = { viewModel.setMediaVolume(it) },
                                        onOpenMusicApp = { viewModel.openDefaultMusicApp() },
                                        onWifiClick = { viewModel.openSystemAction(Settings.ACTION_WIFI_SETTINGS) },
                                        onBluetoothClick = { viewModel.openSystemAction(Settings.ACTION_BLUETOOTH_SETTINGS) },
                                        onDisplayClick = { viewModel.openSystemAction(Settings.ACTION_DISPLAY_SETTINGS) },
                                        onSetDefaultHomeClick = { viewModel.openSystemAction(Settings.ACTION_HOME_SETTINGS) }
                                    )
                                }
                            }
                        }
                    }

                    // Page Dots + Floating Bottom Dock (Circular N Button + Search Pill)
                    AnimatedVisibility(
                        visible = state.activeOverlay == ActiveOverlay.NONE,
                        enter = fadeIn() + slideInVertically { it / 2 },
                        exit = fadeOut() + slideOutVertically { it / 2 },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .widthIn(max = 640.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .horizontalScroll(rememberScrollState())
                                        .padding(horizontal = 24.dp, vertical = 2.dp)
                                        .testTag("page_indicator_dots")
                                ) {
                                    config.pages.forEachIndexed { idx, _ ->
                                        val active = idx == pagerState.currentPage
                                        Box(
                                            modifier = Modifier
                                                .height(6.dp)
                                                .width(if (active) 18.dp else 6.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (active) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                                )
                                                .clickable {
                                                    coroutineScope.launch { pagerState.animateScrollToPage(idx) }
                                                }
                                                .testTag("page_dot_$idx")
                                        )
                                    }
                                }
                            }

                            FloatingBottomDock(
                                emblemChar = config.emblemCharacter,
                                isLiquidGlass = config.isLiquidGlassEnabled,
                                strings = strings,
                                onOpenDrawer = { viewModel.openAppDrawer() },
                                onOpenSearch = { viewModel.openSearch() }
                            )
                        }
                    }

                    // App Drawer Overlay (Opened via the circular N button)
                    AnimatedVisibility(
                        visible = !config.isPcMode && state.activeOverlay == ActiveOverlay.APP_DRAWER,
                        enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
                            scaleIn(initialScale = 0.94f, animationSpec = spring(stiffness = Spring.StiffnessMedium)),
                        exit = fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
                            scaleOut(targetScale = 0.94f, animationSpec = spring(stiffness = Spring.StiffnessMedium))
                    ) {
                        AppDrawerOverlay(
                            emblemChar = config.emblemCharacter,
                            apps = state.drawerApps,
                            totalAppCount = state.installedApps.size,
                            selectedCategory = state.selectedDrawerCategory,
                            searchQuery = state.searchQuery,
                            iconShape = config.iconShape,
                            iconColorTheme = config.iconColorTheme,
                            customBgHex = config.customIconBgHex,
                            customFgHex = config.customIconFgHex,
                            customBgEndHex = config.customIconBgEndHex,
                            isIconGradientEnabled = config.isIconGradientEnabled,
                            overrides = config.customAppOverrides,
                            iconSizeDp = config.homeIconSizeDp,
                            isLiquidGlass = config.isLiquidGlassEnabled,
                            strings = strings,
                            onSelectCategory = { viewModel.selectDrawerCategory(it) },
                            onQueryChange = { viewModel.updateSearchQuery(it) },
                            onLaunchApp = { viewModel.launchApp(it, hostActivity) },
                            onCustomizeApp = { viewModel.openAppCustomizer(it) },
                            onClose = { viewModel.closeOverlay() }
                        )
                    }

                    // Search Overlay
                    AnimatedVisibility(
                        visible = !config.isPcMode && state.activeOverlay == ActiveOverlay.SEARCH,
                        enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)),
                        exit = fadeOut(spring(stiffness = Spring.StiffnessMedium))
                    ) {
                        SearchOverlay(
                            query = state.searchQuery,
                            matchingApps = state.filteredSearchApps,
                            iconShape = config.iconShape,
                            iconColorTheme = config.iconColorTheme,
                            customBgHex = config.customIconBgHex,
                            customFgHex = config.customIconFgHex,
                            customBgEndHex = config.customIconBgEndHex,
                            isIconGradientEnabled = config.isIconGradientEnabled,
                            overrides = config.customAppOverrides,
                            isLiquidGlass = config.isLiquidGlassEnabled,
                            strings = strings,
                            onQueryChange = { viewModel.updateSearchQuery(it) },
                            onLaunchApp = { viewModel.launchApp(it, hostActivity) },
                            onCustomizeApp = { viewModel.openAppCustomizer(it) },
                            onClose = { viewModel.closeOverlay() }
                        )
                    }
                    } // close Mobile Launcher branch (`if (config.isPcMode) ... else ...`)

                    // Long-Press / Themes App Visual Customization Hub
                    if (state.activeOverlay == ActiveOverlay.CUSTOMIZE_HUB) {
                        VisualCustomizeHubSheet(
                            config = config,
                            currentPageIndex = pagerState.currentPage,
                            allApps = state.installedApps,
                            strings = strings,
                            initialTab = state.initialHubTab,
                            hubSessionId = state.hubSessionId,
                            onToggleAppOnPage = { pkg ->
                                viewModel.toggleAppPinnedOnPage(pagerState.currentPage, pkg)
                            },
                            onOpenCreateFolder = { showCreateFolderDialog = true },
                            onToggleWidgetOnPage = { widgetId ->
                                viewModel.toggleWidgetOnPage(pagerState.currentPage, widgetId)
                            },
                            onIconColorThemeChange = { theme, bg, fg ->
                                viewModel.setIconColorTheme(theme, bg, fg)
                            },
                            onIconCustomColorsAndGradient = { bgStart, bgEnd, fg, gradEnabled ->
                                viewModel.setCustomIconColorsAndGradient(bgStart, bgEnd, fg, gradEnabled)
                            },
                            onIconShapeChange = { viewModel.setIconShape(it) },
                            onIconSizeChange = { viewModel.setIconSizeDp(it) },
                            onSelectPage = { idx ->
                                coroutineScope.launch { pagerState.animateScrollToPage(idx) }
                            },
                            onAddNewPage = {
                                val newIndex = viewModel.addNewPage()
                                coroutineScope.launch { pagerState.animateScrollToPage(newIndex) }
                            },
                            onDeletePage = { idx -> viewModel.deletePage(idx) },
                            onWallpaperChange = { viewModel.setWallpaper(it) },
                            onPickCustomWallpaper = {
                                customWallpaperPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onPickCustomVideoWallpaper = {
                                customVideoWallpaperPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            onThemeModeChange = { viewModel.setThemeMode(it) },
                            onLiquidGlassChange = { viewModel.setLiquidGlassEnabled(it) },
                            onClockStyleChange = { viewModel.setClockStyle(it) },
                            onEnterHomeEditMode = { viewModel.enterHomeEditMode() },
                            onAccentChange = { viewModel.setAccent(it) },
                            onCustomAccentChange = { startHex, endHex, gradEnabled ->
                                viewModel.setCustomAccent(startHex, endHex, gradEnabled)
                            },
                            onUpdateEmblem = { ch, tag, style ->
                                viewModel.updateEmblem(ch, tag, style)
                            },
                            onCustomizeAppClick = { app -> viewModel.openAppCustomizer(app) },
                            onDismiss = { viewModel.closeOverlay() }
                        )
                    }

                    // PC Mode Conversion Confirmation Dialog ("Switch smartphone to PC Mode?")
                    if (state.showPcConfirmDialog) {
                        PcConversionConfirmDialog(
                            strings = strings,
                            onConfirm = { viewModel.setPcMode(true) },
                            onDismiss = { viewModel.dismissPcModeConfirmation() }
                        )
                    }

                    // Folder Editor / Viewer Dialog
                    val folderRef = state.activeFolderRef
                    val openFolder = folderRef?.let { (pIdx, fId) ->
                        config.pages.getOrNull(pIdx)?.folders?.firstOrNull { it.id == fId }
                    }
                    if (openFolder != null || showCreateFolderDialog) {
                        val targetPage = folderRef?.first ?: pagerState.currentPage
                        FolderEditorDialog(
                            folder = openFolder,
                            allApps = state.installedApps,
                            iconShape = config.iconShape,
                            iconColorTheme = config.iconColorTheme,
                            customBgHex = config.customIconBgHex,
                            customFgHex = config.customIconFgHex,
                            customBgEndHex = config.customIconBgEndHex,
                            isIconGradientEnabled = config.isIconGradientEnabled,
                            overrides = config.customAppOverrides,
                            isLiquidGlass = config.isLiquidGlassEnabled,
                            strings = strings,
                            onLaunchApp = { viewModel.launchApp(it, hostActivity) },
                            onCustomizeApp = { viewModel.openAppCustomizer(it) },
                            onSaveFolder = { title, accentHex, pkgs ->
                                if (openFolder == null) {
                                    viewModel.createFolderOnPage(targetPage, title, accentHex, pkgs)
                                } else {
                                    viewModel.updateFolderOnPage(targetPage, openFolder.id, title, accentHex, pkgs)
                                }
                            },
                            onDeleteFolder = if (openFolder != null) {
                                { viewModel.deleteFolderOnPage(targetPage, openFolder.id) }
                            } else null,
                            onDismiss = {
                                showCreateFolderDialog = false
                                viewModel.closeFolder()
                            }
                        )
                    }

                    // Photo Frame Editor Dialog
                    if (showPhotoEditorDialog) {
                        PhotoFrameEditorDialog(
                            currentStyle = config.photoFrameStyle,
                            currentCaption = config.photoFrameCaption,
                            isLiquidGlass = config.isLiquidGlassEnabled,
                            strings = strings,
                            onPickPhoto = {
                                photoWidgetPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onSave = { style, caption ->
                                viewModel.updatePhotoFrameStyle(style, caption)
                            },
                            onDismiss = { showPhotoEditorDialog = false }
                        )
                    }

                    // Per-App Logo & Label Customizer Dialog
                    state.customizingApp?.let { targetApp ->
                        val currentPage = config.pages.getOrElse(pagerState.currentPage) { HomePageData("page_main") }
                        AppCustomizerDialog(
                            app = targetApp,
                            currentOverride = config.customAppOverrides[targetApp.packageName],
                            isPinnedOnCurrentPage = targetApp.packageName in currentPage.pinnedPackages,
                            currentPageFolders = currentPage.folders,
                            isLiquidGlass = config.isLiquidGlassEnabled,
                            strings = strings,
                            onLaunch = {
                                viewModel.dismissAppCustomizer()
                                viewModel.launchApp(targetApp, hostActivity)
                            },
                            onTogglePinOnPage = {
                                viewModel.toggleAppPinnedOnPage(pagerState.currentPage, targetApp.packageName)
                            },
                            onAddToFolder = { folderId ->
                                viewModel.addAppToFolder(pagerState.currentPage, folderId, targetApp.packageName)
                            },
                            onPickCustomLogoImage = {
                                customAppLogoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onSaveCustomization = { customLabel, symbolPreset ->
                                viewModel.saveAppCustomization(targetApp.packageName, customLabel, symbolPreset)
                            },
                            onResetDefault = {
                                viewModel.resetCustomAppOverride(targetApp.packageName)
                            },
                            onOpenInfo = { viewModel.openAppInfo(targetApp) },
                            onUninstall = if (targetApp.isInternalApp) null else {
                                { viewModel.uninstallApp(targetApp) }
                            },
                            onDismiss = { viewModel.dismissAppCustomizer() }
                        )
                    }

                    // Built-in Calculator Fallback Dialog (Mobile Mode when external Calculator fails/stalls)
                    if (state.showBuiltInCalculatorDialog) {
                        AlertDialog(
                            onDismissRequest = { viewModel.dismissBuiltInCalculator() },
                            title = { Text(strings.winCalcApp.removeSuffix(".exe")) },
                            text = {
                                Box(modifier = Modifier.fillMaxWidth().height(340.dp)) {
                                    CalculatorExeBody()
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { viewModel.dismissBuiltInCalculator() }) {
                                    Text(strings.winClose)
                                }
                            },
                            modifier = Modifier.testTag("mobile_calculator_dialog")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomePageContent(
    pageIndex: Int,
    pageData: HomePageData,
    state: com.example.viewmodel.LauncherUiState,
    batteryMonitorState: State<BatteryMonitorState>,
    strings: AppStrings,
    onLaunchApp: (com.example.model.InstalledApp) -> Unit,
    onCustomizeApp: (com.example.model.InstalledApp) -> Unit,
    onOpenFolder: (com.example.model.AppFolder) -> Unit,
    onRemoveWidget: (HomeWidgetId) -> Unit,
    onOpenCustomizeHub: (Int) -> Unit,
    onEnterHomeEditMode: () -> Unit,
    onCycleClockStyle: () -> Unit,
    onLongPressApp: (com.example.model.InstalledApp) -> Unit,
    onSelectEditApp: (com.example.model.InstalledApp) -> Unit,
    onMiniRemoveApp: (com.example.model.InstalledApp) -> Unit,
    onMoveApp: (String, String) -> Unit,
    onNudgeApp: (String, Int) -> Unit,
    onMoveAppToNextPage: (String) -> Unit,
    onUninstallApp: (com.example.model.InstalledApp) -> Unit,
    onExitEditMode: () -> Unit,
    onPickPhoto: () -> Unit,
    onOpenPhotoEditor: () -> Unit,
    onOpenBatterySettings: () -> Unit,
    onPrevMedia: () -> Unit,
    onPlayPauseMedia: () -> Unit,
    onNextMedia: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    onOpenMusicApp: () -> Unit,
    onWifiClick: () -> Unit,
    onBluetoothClick: () -> Unit,
    onDisplayClick: () -> Unit,
    onSetDefaultHomeClick: () -> Unit
) {
    val config = state.config
    val appsByPkg = state.appsByPackage
    val pinnedApps = pageData.pinnedPackages.mapNotNull { appsByPkg[it] }
    val activeEditPkg = state.selectedEditAppPackage?.takeIf { it in pageData.pinnedPackages }
        ?: pageData.pinnedPackages.firstOrNull()
    val isCompletelyEmptySecondary = pageIndex > 0 &&
        pinnedApps.isEmpty() &&
        pageData.folders.isEmpty() &&
        pageData.widgets.isEmpty()

    LazyColumn(
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 14.dp,
            bottom = 136.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(state.isHomeEditMode) {
                detectTapGestures(
                    onTap = { if (state.isHomeEditMode) onExitEditMode() },
                    onLongPress = { onEnterHomeEditMode() }
                )
            }
            .testTag("home_page_$pageIndex")
    ) {
        if (pageIndex == 0) {
            item(key = "clock_header") {
                MinimalClockHeader(
                    now = state.now,
                    config = config,
                    onCycleClockStyle = onCycleClockStyle
                )
            }
        }

        if (state.isHomeEditMode && pageIndex == state.currentPageIndex) {
            item(key = "${pageData.id}_edit_bar") {
                val selectedIdx = activeEditPkg?.let { pageData.pinnedPackages.indexOf(it) } ?: -1
                val selectedApp = activeEditPkg?.let { appsByPkg[it] }
                HomeEditModeBar(
                    selectedApp = selectedApp,
                    canMovePrev = selectedIdx > 0,
                    canMoveNext = selectedIdx in 0 until pageData.pinnedPackages.lastIndex,
                    canMovePage = config.pages.size > 1 && selectedApp != null,
                    isLiquidGlass = config.isLiquidGlassEnabled,
                    strings = strings,
                    onMovePrev = { activeEditPkg?.let { onNudgeApp(it, -1) } },
                    onMoveNext = { activeEditPkg?.let { onNudgeApp(it, +1) } },
                    onMovePage = { activeEditPkg?.let { onMoveAppToNextPage(it) } },
                    onCustomizeApp = { selectedApp?.let { onCustomizeApp(it) } },
                    onRemoveApp = { selectedApp?.let { onMiniRemoveApp(it) } },
                    onUninstallApp = { selectedApp?.let { onUninstallApp(it) } },
                    onOpenAddHub = { onOpenCustomizeHub(0) },
                    onOpenThemesHub = { onOpenCustomizeHub(2) },
                    onDone = onExitEditMode
                )
            }
        }

        if (isCompletelyEmptySecondary) {
            item(key = "empty_secondary_page") {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillParentMaxHeight(0.75f)
                        .fillMaxWidth()
                        .testTag("empty_page_placeholder")
                ) {
                    LiquidGlassSurface(
                        shape = RoundedCornerShape(28.dp),
                        isLiquidGlass = config.isLiquidGlassEnabled,
                        onClick = { onOpenCustomizeHub(0) },
                        onLongClick = onEnterHomeEditMode,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(28.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = strings.addToThisPage,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = strings.emptyPageTitle,
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = strings.emptyPageSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        items(pageData.widgets, key = { "${pageData.id}_${it.name}" }) { widgetId ->
            when (widgetId) {
                HomeWidgetId.EMBLEM_LOGO -> {
                    EmblemLogoWidget(
                        emblemChar = config.emblemCharacter,
                        tagline = config.emblemTagline,
                        emblemStyle = config.emblemStyle,
                        isLiquidGlass = config.isLiquidGlassEnabled,
                        strings = strings,
                        onCustomizeEmblem = { onOpenCustomizeHub(3) },
                        onRemoveWidget = { onRemoveWidget(widgetId) }
                    )
                }
                HomeWidgetId.CHARGING_POD -> {
                    ChargingPodWidget(
                        battery = batteryMonitorState.value.toBatteryTelemetry(),
                        isLiquidGlass = config.isLiquidGlassEnabled,
                        strings = strings,
                        onOpenBatterySettings = onOpenBatterySettings,
                        onRemoveWidget = { onRemoveWidget(widgetId) }
                    )
                }
                HomeWidgetId.PHOTO_FRAME -> {
                    PhotoFrameWidget(
                        photoPath = config.photoFramePath,
                        caption = config.photoFrameCaption,
                        frameStyle = config.photoFrameStyle,
                        isLiquidGlass = config.isLiquidGlassEnabled,
                        strings = strings,
                        onPickPhoto = onPickPhoto,
                        onCustomizeFrame = onOpenPhotoEditor,
                        onRemoveWidget = { onRemoveWidget(widgetId) }
                    )
                }
                HomeWidgetId.MEDIA_CONTROL -> {
                    MediaControlWidget(
                        volumePercent = state.device.mediaVolumePercent,
                        isPlayingVisual = state.isMediaPlayingVisual,
                        isLiquidGlass = config.isLiquidGlassEnabled,
                        strings = strings,
                        onPrev = onPrevMedia,
                        onPlayPause = onPlayPauseMedia,
                        onNext = onNextMedia,
                        onVolumeChange = onVolumeChange,
                        onOpenMusicApp = onOpenMusicApp,
                        onRemoveWidget = { onRemoveWidget(widgetId) }
                    )
                }
                HomeWidgetId.SYSTEM_PULSE -> {
                    SystemPulseWidget(
                        device = state.device,
                        isLiquidGlass = config.isLiquidGlassEnabled,
                        strings = strings,
                        onWifiClick = onWifiClick,
                        onBluetoothClick = onBluetoothClick,
                        onDisplayClick = onDisplayClick,
                        onSetDefaultHomeClick = onSetDefaultHomeClick,
                        onRemoveWidget = { onRemoveWidget(widgetId) }
                    )
                }
            }
        }

        if (pageData.folders.isNotEmpty()) {
            item(key = "${pageData.id}_folders") {
                PageFoldersGrid(
                    folders = pageData.folders,
                    appsByPackage = appsByPkg,
                    iconShape = config.iconShape,
                    iconColorTheme = config.iconColorTheme,
                    customBgHex = config.customIconBgHex,
                    customFgHex = config.customIconFgHex,
                    customBgEndHex = config.customIconBgEndHex,
                    isIconGradientEnabled = config.isIconGradientEnabled,
                    overrides = config.customAppOverrides,
                    iconSizeDp = config.homeIconSizeDp,
                    isLiquidGlass = config.isLiquidGlassEnabled,
                    strings = strings,
                    onLaunchApp = onLaunchApp,
                    onCustomizeApp = onCustomizeApp,
                    onOpenFolder = onOpenFolder
                )
            }
        }

        if (pinnedApps.isNotEmpty()) {
            item(key = "${pageData.id}_apps") {
                ScatteredAppsGrid(
                    apps = pinnedApps,
                    iconShape = config.iconShape,
                    iconColorTheme = config.iconColorTheme,
                    customBgHex = config.customIconBgHex,
                    customFgHex = config.customIconFgHex,
                    customBgEndHex = config.customIconBgEndHex,
                    isIconGradientEnabled = config.isIconGradientEnabled,
                    overrides = config.customAppOverrides,
                    iconSizeDp = config.homeIconSizeDp,
                    isLiquidGlass = config.isLiquidGlassEnabled,
                    isEditMode = state.isHomeEditMode,
                    selectedEditPackage = activeEditPkg,
                    onLaunchApp = onLaunchApp,
                    onLongPressApp = onLongPressApp,
                    onSelectEditApp = onSelectEditApp,
                    onMiniRemoveApp = onMiniRemoveApp,
                    onMoveApp = onMoveApp
                )
            }
        }

        if (pageIndex == 0) {
            item(key = "long_press_hint") {
                Text(
                    text = strings.longPressHint,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .clickable { onOpenCustomizeHub(2) }
                )
            }
        }
    }
}

private fun applyPcFullScreen(activity: Activity?, enabled: Boolean) {
    val window = activity?.window ?: return
    WindowCompat.setDecorFitsSystemWindows(window, false)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val lp = window.attributes
        lp.layoutInDisplayCutoutMode = if (enabled) {
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        } else {
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
        }
        window.attributes = lp
    }
    val controller = WindowCompat.getInsetsController(window, window.decorView)
    if (enabled) {
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    } else {
        controller.show(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
    }
}

private tailrec fun Context.findHostActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findHostActivity()
    else -> null
}
