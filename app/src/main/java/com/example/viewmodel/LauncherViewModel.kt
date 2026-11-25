package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.KeyEvent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.BatteryMonitorState
import com.example.data.InstalledAppsRepository
import com.example.data.LauncherPreferencesRepository
import com.example.data.SystemTelemetryRepository
import com.example.model.ActiveOverlay
import com.example.model.AppCategory
import com.example.ui.components.pc.WebAppLaunchReport
import com.example.model.AppFolder
import com.example.model.AppSymbolPreset
import com.example.model.BatteryTelemetry
import com.example.model.ClockStyle
import com.example.model.CustomAppOverride
import com.example.model.DeviceTelemetry
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
import com.example.model.PC_APP_PACKAGE
import com.example.model.PcExeShortcut
import com.example.model.PhotoFrameStyle
import com.example.model.RETURN_LAUNCHER_PACKAGE
import com.example.model.THEMES_APP_PACKAGE
import com.example.model.ThemeMode
import com.example.model.WallpaperChoice
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDateTime
import java.util.UUID

data class LauncherUiState(
    val config: LauncherConfig = LauncherConfig(),
    val installedApps: List<InstalledApp> = emptyList(),
    val isDefaultLauncher: Boolean = false,
    val gateCompleted: Boolean = false,
    val battery: BatteryTelemetry = BatteryTelemetry(),
    val batteryMonitor: BatteryMonitorState = BatteryMonitorState(),
    val device: DeviceTelemetry = DeviceTelemetry(),
    val now: LocalDateTime = LocalDateTime.now(),
    val activeOverlay: ActiveOverlay = ActiveOverlay.NONE,
    val currentPageIndex: Int = 0,
    val searchQuery: String = "",
    val selectedDrawerCategory: AppCategory = AppCategory.ALL,
    val activeFolderRef: Pair<Int, String>? = null,
    val customizingApp: InstalledApp? = null,
    val isMediaPlayingVisual: Boolean = false,
    val isHomeEditMode: Boolean = false,
    val selectedEditAppPackage: String? = null,
    val initialHubTab: Int = 0,
    val hubSessionId: Int = 0,
    val showPcConfirmDialog: Boolean = false,
    val showBuiltInCalculatorDialog: Boolean = false,
    val wineBridgePackages: List<String> = emptyList(),
    val pcExecutionLogs: List<String> = emptyList()
) {
    val showDefaultLauncherGate: Boolean
        get() = !isDefaultLauncher && !gateCompleted

    val appsByPackage: Map<String, InstalledApp>
        get() = installedApps.associateBy { it.packageName }

    fun displayLabelFor(app: InstalledApp): String {
        return config.customAppOverrides[app.packageName]?.customLabel?.takeIf { it.isNotBlank() }
            ?: app.label
    }

    val filteredSearchApps: List<InstalledApp>
        get() {
            val q = searchQuery.trim().lowercase()
            if (q.isEmpty()) return installedApps
            return installedApps.filter {
                displayLabelFor(it).lowercase().contains(q) ||
                    it.label.lowercase().contains(q) ||
                    it.packageName.lowercase().contains(q)
            }
        }

    val drawerApps: List<InstalledApp>
        get() {
            val base = if (selectedDrawerCategory == AppCategory.ALL) {
                installedApps
            } else {
                installedApps.filter { it.category == selectedDrawerCategory }
            }
            val q = searchQuery.trim().lowercase()
            return if (q.isEmpty()) base else base.filter {
                displayLabelFor(it).lowercase().contains(q) ||
                    it.label.lowercase().contains(q) ||
                    it.packageName.lowercase().contains(q)
            }
        }
}

class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val appsRepo = InstalledAppsRepository(application)
    private val telemetryRepo = SystemTelemetryRepository(application)
    private val prefsRepo = LauncherPreferencesRepository(application)

    private val interactionState = MutableStateFlow(
        InteractionState(isDefaultLauncher = appsRepo.isDefaultLauncher())
    )
    private val pagesMutex = Mutex()
    private val prefsMutex = Mutex()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val events = _events.asSharedFlow()

    private val _webAppLaunchEvents = MutableSharedFlow<WebAppLaunchReport>(extraBufferCapacity = 4)
    val webAppLaunchEvents = _webAppLaunchEvents.asSharedFlow()

    val batteryMonitorState: StateFlow<BatteryMonitorState> = telemetryRepo.observeBatteryMonitorState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = BatteryMonitorState()
        )

    val uiState: StateFlow<LauncherUiState> = combine(
        prefsRepo.configFlow,
        appsRepo.observeInstalledApps(),
        batteryMonitorState,
        telemetryRepo.observeDeviceTelemetry(),
        telemetryRepo.observeClock(),
        interactionState
    ) { values ->
        val rawConfig = values[0] as LauncherConfig
        @Suppress("UNCHECKED_CAST")
        val apps = values[1] as List<InstalledApp>
        val monitorState = values[2] as BatteryMonitorState
        val battery = monitorState.toBatteryTelemetry()
        val device = values[3] as DeviceTelemetry
        val now = values[4] as LocalDateTime
        val local = values[5] as InteractionState

        val config = rawConfig.copy(
            pages = local.pendingPages ?: rawConfig.pages,
            homeIconSizeDp = local.pendingIconSizeDp ?: rawConfig.homeIconSizeDp,
            wallpaper = local.pendingWallpaper ?: rawConfig.wallpaper,
            themeMode = local.pendingThemeMode ?: rawConfig.themeMode,
            isLiquidGlassEnabled = local.pendingLiquidGlassEnabled ?: rawConfig.isLiquidGlassEnabled,
            clockStyle = local.pendingClockStyle ?: rawConfig.clockStyle,
            accent = local.pendingAccent ?: rawConfig.accent,
            useCustomAccent = local.pendingUseCustomAccent ?: rawConfig.useCustomAccent,
            customAccentHex = local.pendingCustomAccentStart ?: rawConfig.customAccentHex,
            customAccentEndHex = local.pendingCustomAccentEnd ?: rawConfig.customAccentEndHex,
            isAccentGradientEnabled = local.pendingIsAccentGradient ?: rawConfig.isAccentGradientEnabled,
            iconShape = local.pendingIconShape ?: rawConfig.iconShape,
            iconColorTheme = local.pendingIconColorTheme?.first ?: rawConfig.iconColorTheme,
            customIconBgHex = local.pendingIconColorTheme?.second ?: rawConfig.customIconBgHex,
            customIconFgHex = local.pendingIconColorTheme?.third ?: rawConfig.customIconFgHex,
            customIconBgEndHex = local.pendingCustomIconBgEnd ?: rawConfig.customIconBgEndHex,
            isIconGradientEnabled = local.pendingIsIconGradient ?: rawConfig.isIconGradientEnabled,
            isPcMode = local.pendingIsPcMode ?: rawConfig.isPcMode
        )

        if (!config.hasInitializedDefaults && apps.isNotEmpty() && local.pendingPages == null) {
            initializeCleanDefaultPages(apps)
        } else if (config.hasInitializedDefaults && (!config.hasPlacedThemesApp || !config.hasPlacedPcApp) && apps.isNotEmpty() && local.pendingPages == null) {
            ensureBuiltInAppsPlaced(config.pages, needThemes = !config.hasPlacedThemesApp, needPc = !config.hasPlacedPcApp)
        }

        val safePageIndex = local.currentPageIndex.coerceIn(0, (config.pages.size - 1).coerceAtLeast(0))

        LauncherUiState(
            config = config,
            installedApps = apps,
            isDefaultLauncher = local.isDefaultLauncher,
            gateCompleted = local.gateCompleted,
            battery = battery,
            batteryMonitor = monitorState,
            device = device.copy(
                mediaVolumePercent = local.overrideVolumePercent ?: device.mediaVolumePercent
            ),
            now = now,
            activeOverlay = local.activeOverlay,
            currentPageIndex = safePageIndex,
            searchQuery = local.searchQuery,
            selectedDrawerCategory = local.selectedDrawerCategory,
            activeFolderRef = local.activeFolderRef,
            customizingApp = local.customizingApp,
            isMediaPlayingVisual = local.isMediaPlayingVisual,
            isHomeEditMode = local.isHomeEditMode,
            selectedEditAppPackage = local.selectedEditAppPackage,
            initialHubTab = local.initialHubTab,
            hubSessionId = local.hubSessionId,
            showPcConfirmDialog = local.showPcConfirmDialog,
            showBuiltInCalculatorDialog = local.showBuiltInCalculatorDialog,
            wineBridgePackages = local.wineBridgePackages,
            pcExecutionLogs = local.pcExecutionLogs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = LauncherUiState(isDefaultLauncher = appsRepo.isDefaultLauncher())
    )

    private var defaultsTriggered = false
    private var builtInsPlacementTriggered = false

    init {
        telemetryRepo.startBatteryMonitorService()
        viewModelScope.launch {
            val bridges = appsRepo.findInstalledWineBridgeApps()
            interactionState.update { it.copy(wineBridgePackages = bridges) }
        }
    }

    private fun initializeCleanDefaultPages(apps: List<InstalledApp>) {
        if (defaultsTriggered) return
        defaultsTriggered = true
        builtInsPlacementTriggered = true
        viewModelScope.launch {
            // Scattered individual main apps ONLY on Page 0 with Themes & PC apps centered among them; Page 1 starts clean
            val regularApps = apps
                .filterNot { it.packageName == THEMES_APP_PACKAGE || it.packageName == PC_APP_PACKAGE }
                .take(7)
                .map { it.packageName }
                .toMutableList()
            val midIndex = (regularApps.size / 2).coerceIn(0, regularApps.size)
            regularApps.add(midIndex, THEMES_APP_PACKAGE)
            val pcIndex = (midIndex + 1).coerceIn(0, regularApps.size)
            regularApps.add(pcIndex, PC_APP_PACKAGE)
            val initialPages = listOf(
                HomePageData(
                    id = "page_main",
                    pinnedPackages = regularApps,
                    folders = emptyList(),
                    widgets = emptyList()
                ),
                HomePageData(
                    id = "page_right",
                    pinnedPackages = emptyList(),
                    folders = emptyList(),
                    widgets = emptyList()
                )
            )
            prefsRepo.savePages(initialPages, markInitialized = true, markThemesPlaced = true, markPcPlaced = true)
        }
    }

    private fun ensureBuiltInAppsPlaced(
        currentPages: List<HomePageData>,
        needThemes: Boolean,
        needPc: Boolean
    ) {
        if (builtInsPlacementTriggered) return
        builtInsPlacementTriggered = true
        viewModelScope.launch {
            pagesMutex.withLock {
                val mutable = currentPages.toMutableList()
                val page0 = mutable.firstOrNull() ?: HomePageData("page_main")
                val pinned = page0.pinnedPackages.toMutableList()
                var changed = false

                if (needThemes && currentPages.none { THEMES_APP_PACKAGE in it.pinnedPackages }) {
                    val midIndex = (pinned.size / 2).coerceIn(0, pinned.size)
                    pinned.add(midIndex, THEMES_APP_PACKAGE)
                    changed = true
                }
                if (needPc && currentPages.none { PC_APP_PACKAGE in it.pinnedPackages }) {
                    val themesPos = pinned.indexOf(THEMES_APP_PACKAGE)
                    val pcPos = if (themesPos >= 0) (themesPos + 1).coerceIn(0, pinned.size) else pinned.size
                    pinned.add(pcPos, PC_APP_PACKAGE)
                    changed = true
                }

                if (changed) {
                    if (mutable.isEmpty()) {
                        mutable.add(page0.copy(pinnedPackages = pinned))
                    } else {
                        mutable[0] = page0.copy(pinnedPackages = pinned)
                    }
                    prefsRepo.savePages(mutable, markInitialized = true, markThemesPlaced = true, markPcPlaced = true)
                } else {
                    if (needThemes) prefsRepo.markThemesAppPlaced()
                    if (needPc) prefsRepo.markPcAppPlaced()
                }
            }
        }
    }

    fun refreshDefaultLauncherStatus() {
        val isDefault = appsRepo.isDefaultLauncher()
        interactionState.update {
            it.copy(
                isDefaultLauncher = isDefault,
                gateCompleted = it.gateCompleted || isDefault
            )
        }
    }

    fun createSetDefaultLauncherIntent(): Intent = appsRepo.createSetDefaultLauncherIntent()

    fun dismissDefaultLauncherGate() {
        interactionState.update { it.copy(gateCompleted = true) }
    }

    fun setCurrentPage(index: Int) {
        interactionState.update { it.copy(currentPageIndex = index.coerceAtLeast(0)) }
    }

    fun openAppDrawer() {
        interactionState.update {
            it.copy(
                activeOverlay = ActiveOverlay.APP_DRAWER,
                searchQuery = "",
                selectedDrawerCategory = AppCategory.ALL,
                activeFolderRef = null
            )
        }
    }

    fun openSearch() {
        interactionState.update {
            it.copy(
                activeOverlay = ActiveOverlay.SEARCH,
                searchQuery = "",
                activeFolderRef = null
            )
        }
    }

    fun openCustomizeHub(initialTab: Int = 0) {
        interactionState.update {
            it.copy(
                activeOverlay = ActiveOverlay.CUSTOMIZE_HUB,
                activeFolderRef = null,
                initialHubTab = initialTab.coerceIn(0, 3),
                hubSessionId = it.hubSessionId + 1
            )
        }
    }

    fun closeOverlay() {
        interactionState.update {
            it.copy(
                activeOverlay = ActiveOverlay.NONE,
                searchQuery = "",
                activeFolderRef = null,
                customizingApp = null
            )
        }
    }

    fun updateSearchQuery(query: String) {
        interactionState.update { it.copy(searchQuery = query) }
    }

    fun selectDrawerCategory(category: AppCategory) {
        interactionState.update { it.copy(selectedDrawerCategory = category) }
    }

    fun openFolder(pageIndex: Int, folderId: String) {
        interactionState.update { it.copy(activeFolderRef = pageIndex to folderId) }
    }

    fun closeFolder() {
        interactionState.update { it.copy(activeFolderRef = null) }
    }

    fun openAppCustomizer(app: InstalledApp) {
        interactionState.update { it.copy(customizingApp = app) }
    }

    fun dismissAppCustomizer() {
        interactionState.update { it.copy(customizingApp = null) }
    }

    fun enterHomeEditMode(selectedPackage: String? = null) {
        interactionState.update {
            it.copy(
                isHomeEditMode = true,
                selectedEditAppPackage = selectedPackage ?: it.selectedEditAppPackage,
                activeOverlay = ActiveOverlay.NONE
            )
        }
    }

    fun selectEditApp(packageName: String?) {
        interactionState.update { it.copy(selectedEditAppPackage = packageName) }
    }

    fun exitHomeEditMode() {
        interactionState.update {
            it.copy(isHomeEditMode = false, selectedEditAppPackage = null)
        }
    }

    fun launchApp(app: InstalledApp, hostContext: Context? = null) {
        when (app.packageName) {
            THEMES_APP_PACKAGE -> {
                openCustomizeHub(initialTab = 2)
                return
            }
            PC_APP_PACKAGE -> {
                requestPcModeConfirmation()
                return
            }
            RETURN_LAUNCHER_PACKAGE -> {
                setPcMode(false)
                return
            }
        }
        val launched = appsRepo.launchApp(app.packageName, app.activityName, hostContext)
        if (launched) {
            closeOverlay()
        } else if (appsRepo.isCalculatorApp(app.packageName, app.label)) {
            openBuiltInCalculator()
        } else {
            _events.tryEmit(getApplication<Application>().getString(R.string.launch_error))
        }
    }

    fun openBuiltInCalculator() {
        interactionState.update {
            it.copy(
                showBuiltInCalculatorDialog = true,
                activeOverlay = ActiveOverlay.NONE,
                isHomeEditMode = false
            )
        }
    }

    fun dismissBuiltInCalculator() {
        interactionState.update { it.copy(showBuiltInCalculatorDialog = false) }
    }

    fun requestPcModeConfirmation() {
        interactionState.update {
            it.copy(
                showPcConfirmDialog = true,
                activeOverlay = ActiveOverlay.NONE,
                isHomeEditMode = false
            )
        }
    }

    fun dismissPcModeConfirmation() {
        interactionState.update { it.copy(showPcConfirmDialog = false) }
    }

    fun setPcMode(enabled: Boolean) {
        interactionState.update {
            it.copy(
                pendingIsPcMode = enabled,
                showPcConfirmDialog = false,
                activeOverlay = ActiveOverlay.NONE,
                isHomeEditMode = false,
                wineBridgePackages = appsRepo.findInstalledWineBridgeApps()
            )
        }
        viewModelScope.launch {
            prefsMutex.withLock {
                val target = interactionState.value.pendingIsPcMode ?: enabled
                prefsRepo.updatePcMode(target)
                interactionState.update { if (it.pendingIsPcMode == target) it.copy(pendingIsPcMode = null) else it }
            }
        }
    }

    fun isGameApp(app: InstalledApp): Boolean = appsRepo.isGameApp(app.packageName, app.label)

    fun isBrowserApp(app: InstalledApp): Boolean {
        val key = "${app.packageName.lowercase()} ${app.label.lowercase()}"
        return listOf(
            "chrome", "browser", "firefox", "mozilla", "edge", "emmx",
            "brave", "opera", "vivaldi", "duckduckgo", "kiwi", "sbrowser"
        ).any { key.contains(it) }
    }

    fun launchAppInPcWindowMode(app: InstalledApp) {
        when (app.packageName) {
            THEMES_APP_PACKAGE -> {
                openCustomizeHub(initialTab = 2)
                return
            }
            PC_APP_PACKAGE -> return
            RETURN_LAUNCHER_PACKAGE -> {
                setPcMode(false)
                return
            }
        }
        val launched = appsRepo.launchAppInPcWindowMode(app.packageName, app.activityName)
        if (!launched) {
            _events.tryEmit(getApplication<Application>().getString(R.string.launch_error))
        }
    }

    fun addExeShortcutFromUri(uri: Uri) {
        viewModelScope.launch {
            val shortcut = prefsRepo.importExeEntry(uri) ?: return@launch
            val current = uiState.value.config.pcExeShortcuts
            val next = (listOf(shortcut) + current.filterNot { it.uriString == shortcut.uriString }).take(30)
            prefsRepo.savePcExeShortcuts(next)
            val headerNote = prefsRepo.inspectExeHeader(shortcut.uriString)
            interactionState.update {
                it.copy(pcExecutionLogs = (listOf("[WIN11] Added ${shortcut.name} • $headerNote") + it.pcExecutionLogs).take(20))
            }
        }
    }

    fun removeExeShortcut(id: String) {
        viewModelScope.launch {
            val next = uiState.value.config.pcExeShortcuts.filterNot { it.id == id }
            prefsRepo.savePcExeShortcuts(next)
        }
    }

    fun runExeShortcut(shortcut: PcExeShortcut) {
        viewModelScope.launch {
            val headerStatus = prefsRepo.inspectExeHeader(shortcut.uriString)
            val dispatch = appsRepo.dispatchDownloadedPackageExternal(shortcut.uriString, shortcut.name)
            val bridges = appsRepo.findInstalledWineBridgeApps()
            val ext = shortcut.name.substringAfterLast('.', "exe").lowercase()
            val appCtx = getApplication<Application>()

            val statusHeadline = when {
                dispatch.launched -> appCtx.getString(R.string.win_webapp_dispatched, dispatch.targetRuntimeName ?: "External Runtime")
                else -> appCtx.getString(R.string.win_webapp_sandbox_opened)
            }
            val explanation = when {
                dispatch.launched -> statusHeadline
                headerStatus.contains("HTML", ignoreCase = true) -> appCtx.getString(R.string.win_webapp_html_warning)
                ext == "apk" -> appCtx.getString(R.string.win_webapp_apk_guide)
                ext == "jar" -> appCtx.getString(R.string.win_webapp_need_pojav)
                else -> appCtx.getString(R.string.win_webapp_need_winlator, shortcut.name)
            }

            val trace = buildList {
                add("[INIT] Package: ${shortcut.name} (${formatBytes(shortcut.sizeBytes)})")
                add("[HEADER] $headerStatus")
                add("[BRIDGES] ${if (bridges.isEmpty()) "None detected (No Winlator / Wine / PojavLauncher installed)" else bridges.joinToString()}")
                if (dispatch.launched) {
                    add("[EXEC] Handed off via FileProvider -> ${dispatch.targetRuntimeName}")
                } else {
                    add("[STATUS] ${dispatch.reason}")
                }
            }

            val report = WebAppLaunchReport(
                appName = shortcut.name,
                uriString = shortcut.uriString,
                filePath = Uri.parse(shortcut.uriString).takeIf { it.scheme == "file" }?.path,
                sizeBytes = shortcut.sizeBytes,
                extension = ext,
                headerInspection = headerStatus,
                dispatchedExternally = dispatch.launched,
                externalRuntimeName = dispatch.targetRuntimeName,
                installedBridges = bridges,
                statusHeadline = statusHeadline,
                explanation = explanation,
                traceLines = trace
            )

            val entry = if (dispatch.launched) {
                "[EXEC] Dispatched ${shortcut.name} via ${dispatch.targetRuntimeName} ($headerStatus)"
            } else {
                "[WIN11 RUNTIME] Opened ${shortcut.name} (${formatBytes(shortcut.sizeBytes)}) • $headerStatus"
            }

            interactionState.update {
                it.copy(
                    wineBridgePackages = bridges,
                    pcExecutionLogs = (listOf(entry) + it.pcExecutionLogs).take(20)
                )
            }
            _webAppLaunchEvents.tryEmit(report)
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) return "URI"
        val kb = bytes / 1024.0
        return if (kb >= 1024.0) String.format(java.util.Locale.ENGLISH, "%.1f MB", kb / 1024.0)
        else String.format(java.util.Locale.ENGLISH, "%.0f KB", kb)
    }

    fun openAppInfo(app: InstalledApp) {
        dismissAppCustomizer()
        appsRepo.openAppInfo(app.packageName)
    }

    fun uninstallApp(app: InstalledApp) {
        dismissAppCustomizer()
        appsRepo.uninstallApp(app.packageName)
    }

    fun openSystemAction(action: String) {
        appsRepo.openSystemAction(action)
    }

    fun openDefaultMusicApp() {
        val mediaApp = uiState.value.installedApps.firstOrNull {
            it.category == AppCategory.MEDIA &&
                (it.packageName.contains("music") || it.packageName.contains("spotify") || it.packageName.contains("youtube"))
        } ?: uiState.value.installedApps.firstOrNull { it.category == AppCategory.MEDIA }

        if (mediaApp != null) launchApp(mediaApp)
        else openSystemAction(Settings.ACTION_SOUND_SETTINGS)
    }

    fun setMediaVolume(percent: Int) {
        val actual = telemetryRepo.setMediaVolumePercent(percent)
        interactionState.update { it.copy(overrideVolumePercent = actual) }
    }

    fun toggleMediaPlayPause() {
        telemetryRepo.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
        interactionState.update { it.copy(isMediaPlayingVisual = !it.isMediaPlayingVisual) }
    }

    fun mediaNext() {
        telemetryRepo.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
    }

    fun mediaPrevious() {
        telemetryRepo.dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
    }

    private fun currentPages(): List<HomePageData> {
        return interactionState.value.pendingPages ?: uiState.value.config.pages
    }

    private fun commitPages(
        next: List<HomePageData>,
        extraState: (InteractionState) -> InteractionState = { it }
    ) {
        val capped = next.take(MAX_HOME_PAGES)
        interactionState.update { state ->
            extraState(state).copy(pendingPages = capped)
        }
        viewModelScope.launch {
            pagesMutex.withLock {
                val snapshot = interactionState.value.pendingPages ?: return@withLock
                prefsRepo.savePages(snapshot)
                interactionState.update { state ->
                    if (state.pendingPages == snapshot) state.copy(pendingPages = null) else state
                }
            }
        }
    }

    // Page Content Management
    fun toggleAppPinnedOnPage(pageIndex: Int, packageName: String) {
        val pages = currentPages().toMutableList()
        val target = pages.getOrNull(pageIndex) ?: return
        val nextPinned = if (packageName in target.pinnedPackages) {
            target.pinnedPackages - packageName
        } else {
            target.pinnedPackages + packageName
        }
        pages[pageIndex] = target.copy(pinnedPackages = nextPinned)
        commitPages(pages)
    }

    fun removeAppFromPage(pageIndex: Int, packageName: String) {
        val pages = currentPages().toMutableList()
        val target = pages.getOrNull(pageIndex) ?: return
        if (packageName !in target.pinnedPackages) return
        pages[pageIndex] = target.copy(pinnedPackages = target.pinnedPackages - packageName)
        commitPages(pages) { state ->
            if (state.selectedEditAppPackage == packageName) state.copy(selectedEditAppPackage = null) else state
        }
    }

    fun moveAppOnPage(pageIndex: Int, draggedPackage: String, targetPackage: String) {
        if (draggedPackage == targetPackage) return
        val pages = currentPages().toMutableList()
        val target = pages.getOrNull(pageIndex) ?: return
        val list = target.pinnedPackages.toMutableList()
        val fromIdx = list.indexOf(draggedPackage)
        val toIdx = list.indexOf(targetPackage)
        if (fromIdx == -1 || toIdx == -1) return
        val item = list.removeAt(fromIdx)
        list.add(toIdx, item)
        pages[pageIndex] = target.copy(pinnedPackages = list)
        commitPages(pages)
    }

    fun nudgeAppOnPage(pageIndex: Int, packageName: String, delta: Int) {
        val pages = currentPages().toMutableList()
        val target = pages.getOrNull(pageIndex) ?: return
        val list = target.pinnedPackages.toMutableList()
        val fromIdx = list.indexOf(packageName)
        if (fromIdx == -1) return
        val toIdx = (fromIdx + delta).coerceIn(0, list.lastIndex)
        if (toIdx == fromIdx) return
        val item = list.removeAt(fromIdx)
        list.add(toIdx, item)
        pages[pageIndex] = target.copy(pinnedPackages = list)
        commitPages(pages)
    }

    fun moveAppToPage(fromPageIndex: Int, packageName: String, toPageIndex: Int) {
        if (fromPageIndex == toPageIndex) return
        val pages = currentPages().toMutableList()
        val fromPage = pages.getOrNull(fromPageIndex) ?: return
        val toPage = pages.getOrNull(toPageIndex) ?: return
        pages[fromPageIndex] = fromPage.copy(pinnedPackages = fromPage.pinnedPackages - packageName)
        val nextDestPinned = if (packageName in toPage.pinnedPackages) toPage.pinnedPackages else toPage.pinnedPackages + packageName
        pages[toPageIndex] = toPage.copy(pinnedPackages = nextDestPinned)
        commitPages(pages) { state ->
            state.copy(currentPageIndex = toPageIndex, selectedEditAppPackage = packageName)
        }
    }

    fun toggleWidgetOnPage(pageIndex: Int, widgetId: HomeWidgetId) {
        val pages = currentPages().toMutableList()
        val target = pages.getOrNull(pageIndex) ?: return
        val nextWidgets = if (widgetId in target.widgets) {
            target.widgets - widgetId
        } else {
            target.widgets + widgetId
        }
        pages[pageIndex] = target.copy(widgets = nextWidgets)
        commitPages(pages)
    }

    fun createFolderOnPage(pageIndex: Int, title: String, accentHex: Long, packageNames: List<String>) {
        val pages = currentPages().toMutableList()
        val target = pages.getOrNull(pageIndex) ?: return
        val defaultName = getApplication<Application>().getString(R.string.default_folder_name)
        val created = AppFolder(
            id = "pod_${UUID.randomUUID().toString().take(8)}",
            title = title.trim().ifEmpty { defaultName },
            accentHex = accentHex,
            packageNames = packageNames.distinct()
        )
        pages[pageIndex] = target.copy(folders = target.folders + created)
        commitPages(pages)
    }

    fun updateFolderOnPage(pageIndex: Int, folderId: String, newTitle: String, accentHex: Long, packageNames: List<String>) {
        val pages = currentPages().toMutableList()
        val target = pages.getOrNull(pageIndex) ?: return
        val nextFolders = target.folders.map { f ->
            if (f.id == folderId) {
                f.copy(
                    title = newTitle.trim().ifEmpty { f.title },
                    accentHex = accentHex,
                    packageNames = packageNames.distinct()
                )
            } else f
        }
        pages[pageIndex] = target.copy(folders = nextFolders)
        commitPages(pages)
    }

    fun addAppToFolder(pageIndex: Int, folderId: String, packageName: String) {
        val pages = currentPages().toMutableList()
        val target = pages.getOrNull(pageIndex) ?: return
        val nextFolders = target.folders.map { f ->
            if (f.id == folderId && packageName !in f.packageNames) {
                f.copy(packageNames = f.packageNames + packageName)
            } else f
        }
        pages[pageIndex] = target.copy(folders = nextFolders)
        commitPages(pages)
    }

    fun deleteFolderOnPage(pageIndex: Int, folderId: String) {
        val pages = currentPages().toMutableList()
        val target = pages.getOrNull(pageIndex) ?: return
        pages[pageIndex] = target.copy(folders = target.folders.filterNot { it.id == folderId })
        commitPages(pages) { state ->
            if (state.activeFolderRef == (pageIndex to folderId)) state.copy(activeFolderRef = null) else state
        }
    }

    fun addNewPage(): Int {
        val current = currentPages()
        if (current.size >= MAX_HOME_PAGES) {
            _events.tryEmit(getApplication<Application>().getString(R.string.max_pages_reached, MAX_HOME_PAGES))
            return current.lastIndex
        }
        val newPage = HomePageData(id = "page_${UUID.randomUUID().toString().take(6)}")
        val next = current + newPage
        commitPages(next)
        return next.lastIndex
    }

    fun deletePage(pageIndex: Int) {
        val current = currentPages()
        if (current.size <= 1 || pageIndex <= 0 || pageIndex !in current.indices) return
        val next = current.toMutableList().apply { removeAt(pageIndex) }
        commitPages(next) { state ->
            state.copy(currentPageIndex = state.currentPageIndex.coerceAtMost(next.lastIndex))
        }
    }

    // Global Icon Colors & Per-App Logo Customizer
    fun setIconColorTheme(theme: IconColorTheme, customBgHex: Long? = null, customFgHex: Long? = null) {
        val pending = Triple(theme, customBgHex, customFgHex)
        interactionState.update { it.copy(pendingIconColorTheme = pending) }
        viewModelScope.launch {
            prefsMutex.withLock {
                prefsRepo.updateIconColorTheme(theme, customBgHex, customFgHex)
                interactionState.update { if (it.pendingIconColorTheme == pending) it.copy(pendingIconColorTheme = null) else it }
            }
        }
    }

    fun setCustomIconColorsAndGradient(
        bgStartHex: Long,
        bgEndHex: Long,
        fgHex: Long,
        gradientEnabled: Boolean
    ) {
        val pending = Triple(IconColorTheme.CUSTOM_DUOTONE, bgStartHex as Long?, fgHex as Long?)
        interactionState.update {
            it.copy(
                pendingIconColorTheme = pending,
                pendingCustomIconBgEnd = bgEndHex,
                pendingIsIconGradient = gradientEnabled
            )
        }
        viewModelScope.launch {
            prefsMutex.withLock {
                prefsRepo.updateIconColorTheme(
                    theme = IconColorTheme.CUSTOM_DUOTONE,
                    customBgHex = bgStartHex,
                    customFgHex = fgHex,
                    customBgEndHex = bgEndHex,
                    isGradientEnabled = gradientEnabled
                )
                interactionState.update {
                    it.copy(
                        pendingIconColorTheme = if (it.pendingIconColorTheme == pending) null else it.pendingIconColorTheme,
                        pendingCustomIconBgEnd = if (it.pendingCustomIconBgEnd == bgEndHex) null else it.pendingCustomIconBgEnd,
                        pendingIsIconGradient = if (it.pendingIsIconGradient == gradientEnabled) null else it.pendingIsIconGradient
                    )
                }
            }
        }
    }

    fun setIconShape(shape: IconShapeStyle) {
        interactionState.update { it.copy(pendingIconShape = shape) }
        viewModelScope.launch {
            prefsMutex.withLock {
                prefsRepo.updateIconShape(shape)
                interactionState.update { if (it.pendingIconShape == shape) it.copy(pendingIconShape = null) else it }
            }
        }
    }

    fun setIconSizeDp(sizeDp: Int) {
        val clamped = sizeDp.coerceIn(MIN_ICON_SIZE_DP, MAX_ICON_SIZE_DP)
        interactionState.update { it.copy(pendingIconSizeDp = clamped) }
        viewModelScope.launch {
            prefsMutex.withLock {
                val target = interactionState.value.pendingIconSizeDp ?: clamped
                prefsRepo.updateIconSizeDp(target)
                interactionState.update { if (it.pendingIconSizeDp == target) it.copy(pendingIconSizeDp = null) else it }
            }
        }
    }

    fun saveAppCustomization(
        packageName: String,
        customLabel: String?,
        symbolPreset: AppSymbolPreset
    ) {
        viewModelScope.launch {
            val current = uiState.value.config.customAppOverrides.toMutableMap()
            val existing = current[packageName]
            val cleanLabel = customLabel?.trim()?.takeIf { it.isNotEmpty() }
            val nextIconPath = if (symbolPreset != AppSymbolPreset.NONE) null else existing?.customIconPath
            if (cleanLabel == null && nextIconPath == null && symbolPreset == AppSymbolPreset.NONE) {
                current.remove(packageName)
            } else {
                current[packageName] = CustomAppOverride(
                    packageName = packageName,
                    customLabel = cleanLabel,
                    customIconPath = nextIconPath,
                    symbolPreset = symbolPreset
                )
            }
            prefsRepo.saveAppOverrides(current)
        }
    }

    fun importCustomAppIcon(packageName: String, uri: Uri) {
        viewModelScope.launch {
            val savedPath = prefsRepo.importCustomAppIcon(packageName, uri) ?: return@launch
            val current = uiState.value.config.customAppOverrides.toMutableMap()
            val existing = current[packageName]
            current[packageName] = CustomAppOverride(
                packageName = packageName,
                customLabel = existing?.customLabel,
                customIconPath = savedPath,
                symbolPreset = AppSymbolPreset.NONE
            )
            prefsRepo.saveAppOverrides(current)
        }
    }

    fun resetCustomAppOverride(packageName: String) {
        viewModelScope.launch {
            val current = uiState.value.config.customAppOverrides.toMutableMap()
            current.remove(packageName)
            prefsRepo.saveAppOverrides(current)
        }
    }

    // Theme & Wallpaper & Widgets
    fun setThemeMode(mode: ThemeMode) {
        interactionState.update { it.copy(pendingThemeMode = mode) }
        viewModelScope.launch {
            prefsMutex.withLock {
                val target = interactionState.value.pendingThemeMode ?: mode
                prefsRepo.updateThemeMode(target)
                interactionState.update { if (it.pendingThemeMode == target) it.copy(pendingThemeMode = null) else it }
            }
        }
    }

    fun setLiquidGlassEnabled(enabled: Boolean) {
        interactionState.update { it.copy(pendingLiquidGlassEnabled = enabled) }
        viewModelScope.launch {
            prefsMutex.withLock {
                val target = interactionState.value.pendingLiquidGlassEnabled ?: enabled
                prefsRepo.updateLiquidGlassEnabled(target)
                interactionState.update { if (it.pendingLiquidGlassEnabled == target) it.copy(pendingLiquidGlassEnabled = null) else it }
            }
        }
    }

    fun setClockStyle(style: ClockStyle) {
        interactionState.update { it.copy(pendingClockStyle = style) }
        viewModelScope.launch {
            prefsMutex.withLock {
                val target = interactionState.value.pendingClockStyle ?: style
                prefsRepo.updateClockStyle(target)
                interactionState.update { if (it.pendingClockStyle == target) it.copy(pendingClockStyle = null) else it }
            }
        }
    }

    fun cycleClockStyle() {
        val entries = ClockStyle.entries
        val current = uiState.value.config.clockStyle
        val next = entries[(entries.indexOf(current) + 1) % entries.size]
        setClockStyle(next)
    }

    fun setAccent(accent: LauncherAccent) {
        interactionState.update {
            it.copy(
                pendingAccent = accent,
                pendingUseCustomAccent = false
            )
        }
        viewModelScope.launch {
            prefsMutex.withLock {
                val target = interactionState.value.pendingAccent ?: accent
                prefsRepo.updateAccent(target)
                interactionState.update {
                    it.copy(
                        pendingAccent = if (it.pendingAccent == target) null else it.pendingAccent,
                        pendingUseCustomAccent = if (it.pendingUseCustomAccent == false) null else it.pendingUseCustomAccent
                    )
                }
            }
        }
    }

    fun setCustomAccent(startHex: Long, endHex: Long, gradientEnabled: Boolean) {
        interactionState.update {
            it.copy(
                pendingUseCustomAccent = true,
                pendingCustomAccentStart = startHex,
                pendingCustomAccentEnd = endHex,
                pendingIsAccentGradient = gradientEnabled
            )
        }
        viewModelScope.launch {
            prefsMutex.withLock {
                prefsRepo.updateCustomAccent(
                    useCustom = true,
                    startHex = startHex,
                    endHex = endHex,
                    gradientEnabled = gradientEnabled
                )
                interactionState.update {
                    it.copy(
                        pendingUseCustomAccent = if (it.pendingUseCustomAccent == true) null else it.pendingUseCustomAccent,
                        pendingCustomAccentStart = if (it.pendingCustomAccentStart == startHex) null else it.pendingCustomAccentStart,
                        pendingCustomAccentEnd = if (it.pendingCustomAccentEnd == endHex) null else it.pendingCustomAccentEnd,
                        pendingIsAccentGradient = if (it.pendingIsAccentGradient == gradientEnabled) null else it.pendingIsAccentGradient
                    )
                }
            }
        }
    }

    fun setWallpaper(choice: WallpaperChoice) {
        interactionState.update { it.copy(pendingWallpaper = choice) }
        viewModelScope.launch {
            prefsMutex.withLock {
                val target = interactionState.value.pendingWallpaper ?: choice
                prefsRepo.updateWallpaper(target)
                interactionState.update { if (it.pendingWallpaper == target) it.copy(pendingWallpaper = null) else it }
            }
        }
    }

    fun updateEmblem(character: String, tagline: String, style: EmblemStyle) {
        viewModelScope.launch { prefsRepo.updateEmblem(character, tagline, style) }
    }

    fun updatePhotoFrameStyle(style: PhotoFrameStyle, caption: String) {
        viewModelScope.launch { prefsRepo.updatePhotoFrameStyle(style, caption) }
    }

    fun importPhotoForWidget(uri: Uri) {
        viewModelScope.launch { prefsRepo.importPhotoForWidget(uri) }
    }

    fun importCustomWallpaper(uri: Uri) {
        viewModelScope.launch { prefsRepo.importCustomWallpaper(uri) }
    }

    fun importCustomVideoWallpaper(uri: Uri) {
        viewModelScope.launch { prefsRepo.importCustomVideoWallpaper(uri) }
    }

    private data class InteractionState(
        val isDefaultLauncher: Boolean = false,
        val gateCompleted: Boolean = false,
        val activeOverlay: ActiveOverlay = ActiveOverlay.NONE,
        val currentPageIndex: Int = 0,
        val searchQuery: String = "",
        val selectedDrawerCategory: AppCategory = AppCategory.ALL,
        val activeFolderRef: Pair<Int, String>? = null,
        val customizingApp: InstalledApp? = null,
        val overrideVolumePercent: Int? = null,
        val isMediaPlayingVisual: Boolean = false,
        val isHomeEditMode: Boolean = false,
        val selectedEditAppPackage: String? = null,
        val pendingPages: List<HomePageData>? = null,
        val pendingIconSizeDp: Int? = null,
        val pendingWallpaper: WallpaperChoice? = null,
        val pendingThemeMode: ThemeMode? = null,
        val pendingLiquidGlassEnabled: Boolean? = null,
        val pendingClockStyle: ClockStyle? = null,
        val pendingAccent: LauncherAccent? = null,
        val pendingUseCustomAccent: Boolean? = null,
        val pendingCustomAccentStart: Long? = null,
        val pendingCustomAccentEnd: Long? = null,
        val pendingIsAccentGradient: Boolean? = null,
        val pendingIconShape: IconShapeStyle? = null,
        val pendingIconColorTheme: Triple<IconColorTheme, Long?, Long?>? = null,
        val pendingCustomIconBgEnd: Long? = null,
        val pendingIsIconGradient: Boolean? = null,
        val pendingIsPcMode: Boolean? = null,
        val showPcConfirmDialog: Boolean = false,
        val showBuiltInCalculatorDialog: Boolean = false,
        val wineBridgePackages: List<String> = emptyList(),
        val pcExecutionLogs: List<String> = emptyList(),
        val initialHubTab: Int = 0,
        val hubSessionId: Int = 0
    )
}
