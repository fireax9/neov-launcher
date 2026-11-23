package com.example.ui.components

import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.BatteryStd
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Brightness6
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PcFileEntry
import com.example.data.PcFileSystemManager
import com.example.model.PcExeShortcut
import com.example.model.ThemeMode
import com.example.ui.components.pc.CalculatorExeBody
import com.example.ui.components.pc.ChromePcWindowBody
import com.example.ui.components.pc.CmdExeBody
import com.example.ui.components.pc.ExeRunnerBody
import com.example.ui.components.pc.NotepadExeBody
import com.example.ui.components.pc.PcDownloadItem
import com.example.ui.components.pc.PcFloatingWindowState
import com.example.ui.components.pc.PhotoViewerExeBody
import com.example.ui.components.pc.TaskManagerExeBody
import com.example.ui.components.pc.ThisPcExplorerBody
import com.example.ui.components.pc.Win11SettingsBody
import com.example.ui.components.pc.Win11WindowKind
import com.example.ui.strings.AppStrings
import com.example.ui.theme.SpaceGroteskFontFamily
import com.example.viewmodel.LauncherUiState
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

private val Win11Blue = Color(0xFF0078D4)
private val Win11Sky = Color(0xFF60CDFF)

private data class PcMenuProgram(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val tint: Color,
    val windowKind: Win11WindowKind? = null,
    val isThemes: Boolean = false
)

@Composable
fun PcConversionConfirmDialog(
    strings: AppStrings,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Win11Blue.copy(alpha = 0.16f))
            ) {
                Win11LogoGlyph(modifier = Modifier.size(28.dp))
            }
        },
        title = {
            Text(
                text = strings.pcConfirmTitle,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = strings.pcConfirmMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Win11Blue, contentColor = Color.White),
                modifier = Modifier.testTag("confirm_pc_mode_button")
            ) {
                Text(strings.pcConfirmYes)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_pc_mode_button")
            ) {
                Text(strings.pcConfirmNo)
            }
        },
        modifier = Modifier.testTag("pc_mode_confirm_dialog")
    )
}

@Composable
fun Windows11DesktopRoot(
    state: LauncherUiState,
    strings: AppStrings,
    effectiveDarkTheme: Boolean,
    onExitPcMode: () -> Unit,
    onOpenThemesHub: () -> Unit,
    onPickExeFile: () -> Unit,
    onImportFileToPcFolder: (String) -> Unit,
    onRunExeShortcut: (PcExeShortcut) -> Unit,
    onRemoveExeShortcut: (String) -> Unit,
    onToggleThemeMode: (ThemeMode) -> Unit,
    onOpenSystemAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fileManager = remember(context) { PcFileSystemManager(context) }
    LaunchedEffect(fileManager) {
        fileManager.ensureInitialized()
    }

    val config = state.config
    var startMenuOpen by remember { mutableStateOf(false) }
    var startShowAllApps by remember { mutableStateOf(false) }
    var startSearchQuery by remember { mutableStateOf("") }
    var quickSettingsOpen by remember { mutableStateOf(false) }
    val openWindows = remember { mutableStateListOf<PcFloatingWindowState>() }
    val chromeDownloads = remember { mutableStateListOf<PcDownloadItem>() }

    val acrylicSurface = if (effectiveDarkTheme) Color(0xFF1A1F29).copy(alpha = 0.90f) else Color(0xFFF3F6FC).copy(alpha = 0.92f)
    val acrylicBorder = if (effectiveDarkTheme) Color.White.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.11f)
    val desktopTextColor = Color.White

    fun titleForKind(kind: Win11WindowKind, customTitle: String? = null): String {
        if (!customTitle.isNullOrBlank()) return customTitle
        return when (kind) {
            Win11WindowKind.CHROME_PC -> strings.winChromeApp
            Win11WindowKind.THIS_PC -> strings.winThisPc
            Win11WindowKind.EXE_RUNNER -> strings.winExeRunner
            Win11WindowKind.WIN_SETTINGS -> strings.winSettings
            Win11WindowKind.NOTEPAD_EXE -> strings.winNotepadApp
            Win11WindowKind.CALCULATOR_EXE -> strings.winCalcApp
            Win11WindowKind.CMD_EXE -> strings.winCmdApp
            Win11WindowKind.TASK_MANAGER_EXE -> strings.winTaskMgrApp
            Win11WindowKind.PHOTO_VIEWER_EXE -> strings.winPhotoViewer
        }
    }

    fun openOrFocusWindow(
        kind: Win11WindowKind,
        fileToOpenPath: String? = null,
        initialExplorerPath: String = "",
        customTitle: String? = null
    ) {
        startMenuOpen = false
        quickSettingsOpen = false
        val existingIdx = openWindows.indexOfFirst { it.kind == kind }
        if (existingIdx >= 0) {
            val existing = openWindows.removeAt(existingIdx)
            val updated = existing.copy(
                isMinimized = false,
                title = customTitle ?: existing.title,
                fileToOpenPath = fileToOpenPath ?: existing.fileToOpenPath,
                initialExplorerPath = initialExplorerPath.ifBlank { existing.initialExplorerPath }
            )
            openWindows.add(updated)
            return
        }
        val cascade = openWindows.size % 5
        val defaultW = when (kind) {
            Win11WindowKind.CALCULATOR_EXE -> 290f
            Win11WindowKind.CHROME_PC, Win11WindowKind.THIS_PC -> 520f
            else -> 460f
        }
        val defaultH = when (kind) {
            Win11WindowKind.CALCULATOR_EXE -> 320f
            Win11WindowKind.CHROME_PC, Win11WindowKind.THIS_PC -> 325f
            else -> 295f
        }
        openWindows.add(
            PcFloatingWindowState(
                instanceId = "win_${UUID.randomUUID().toString().take(8)}",
                kind = kind,
                title = titleForKind(kind, customTitle),
                fileToOpenPath = fileToOpenPath,
                initialExplorerPath = initialExplorerPath,
                offsetXDp = 96f + cascade * 24f,
                offsetYDp = 16f + cascade * 20f,
                widthDp = defaultW,
                heightDp = defaultH
            )
        )
    }

    fun launchShortcutOrBuiltIn(shortcut: PcExeShortcut) {
        val n = shortcut.name.lowercase()
        when {
            n.contains("calc") -> openOrFocusWindow(Win11WindowKind.CALCULATOR_EXE)
            n.contains("notepad") -> openOrFocusWindow(Win11WindowKind.NOTEPAD_EXE)
            n.contains("cmd") -> openOrFocusWindow(Win11WindowKind.CMD_EXE)
            n.contains("taskmgr") -> openOrFocusWindow(Win11WindowKind.TASK_MANAGER_EXE)
            n.contains("chrome") -> openOrFocusWindow(Win11WindowKind.CHROME_PC)
            else -> {
                onRunExeShortcut(shortcut)
                openOrFocusWindow(Win11WindowKind.EXE_RUNNER)
            }
        }
    }

    fun openPcFileEntry(entry: PcFileEntry) {
        if (entry.isDirectory) {
            openOrFocusWindow(Win11WindowKind.THIS_PC, initialExplorerPath = entry.relativePath)
            return
        }
        val lowerName = entry.name.lowercase()
        when {
            entry.isVirtualBuiltinExe || (entry.extension == "exe" && lowerName in setOf(
                "chrome.exe", "explorer.exe", "notepad.exe", "calc.exe", "cmd.exe", "taskmgr.exe"
            )) -> {
                when (lowerName) {
                    "chrome.exe" -> openOrFocusWindow(Win11WindowKind.CHROME_PC)
                    "explorer.exe" -> openOrFocusWindow(Win11WindowKind.THIS_PC)
                    "notepad.exe" -> openOrFocusWindow(Win11WindowKind.NOTEPAD_EXE)
                    "calc.exe" -> openOrFocusWindow(Win11WindowKind.CALCULATOR_EXE)
                    "cmd.exe" -> openOrFocusWindow(Win11WindowKind.CMD_EXE)
                    "taskmgr.exe" -> openOrFocusWindow(Win11WindowKind.TASK_MANAGER_EXE)
                }
            }
            entry.extension in setOf("exe", "msi", "bat") -> {
                if (lowerName.contains("calc")) {
                    openOrFocusWindow(Win11WindowKind.CALCULATOR_EXE)
                } else {
                    val uri = Uri.fromFile(File(entry.absolutePath)).toString()
                    val shortcut = PcExeShortcut(
                        id = "fs_${entry.relativePath.hashCode()}",
                        name = entry.name,
                        uriString = uri,
                        sizeBytes = entry.sizeBytes
                    )
                    onRunExeShortcut(shortcut)
                    openOrFocusWindow(Win11WindowKind.EXE_RUNNER)
                }
            }
            entry.extension in setOf("txt", "log", "md", "json", "xml", "cfg", "ini", "csv", "html") -> {
                openOrFocusWindow(
                    kind = Win11WindowKind.NOTEPAD_EXE,
                    fileToOpenPath = entry.absolutePath,
                    customTitle = "${entry.name} — Notepad"
                )
            }
            entry.extension in setOf("png", "jpg", "jpeg", "webp", "bmp", "gif") -> {
                openOrFocusWindow(
                    kind = Win11WindowKind.PHOTO_VIEWER_EXE,
                    fileToOpenPath = entry.absolutePath,
                    customTitle = entry.name
                )
            }
            else -> {
                val uri = Uri.fromFile(File(entry.absolutePath)).toString()
                val shortcut = PcExeShortcut(
                    id = "fs_${entry.relativePath.hashCode()}",
                    name = entry.name,
                    uriString = uri,
                    sizeBytes = entry.sizeBytes
                )
                onRunExeShortcut(shortcut)
                openOrFocusWindow(Win11WindowKind.EXE_RUNNER)
            }
        }
    }

    fun focusWindow(instanceId: String) {
        val idx = openWindows.indexOfFirst { it.instanceId == instanceId }
        if (idx >= 0) {
            val win = openWindows.removeAt(idx).copy(isMinimized = false)
            openWindows.add(win)
        }
    }

    fun toggleTaskbarWindow(instanceId: String) {
        val idx = openWindows.indexOfFirst { it.instanceId == instanceId }
        if (idx < 0) return
        val win = openWindows[idx]
        val isTopVisible = !win.isMinimized && idx == openWindows.lastIndex
        if (isTopVisible) {
            openWindows[idx] = win.copy(isMinimized = true)
        } else {
            focusWindow(instanceId)
        }
    }

    fun toggleTaskbarPinnedKind(kind: Win11WindowKind) {
        val existing = openWindows.lastOrNull { it.kind == kind }
        if (existing != null) toggleTaskbarWindow(existing.instanceId)
        else openOrFocusWindow(kind)
    }

    fun closeWindow(instanceId: String) {
        openWindows.removeAll { it.instanceId == instanceId }
    }

    fun updateWindow(instanceId: String, transform: (PcFloatingWindowState) -> PcFloatingWindowState) {
        val idx = openWindows.indexOfFirst { it.instanceId == instanceId }
        if (idx >= 0) {
            openWindows[idx] = transform(openWindows[idx])
        }
    }

    val pcPrograms = remember(strings) {
        listOf(
            PcMenuProgram("chrome", strings.winChromeApp, Icons.Rounded.Language, Color(0xFF4285F4), Win11WindowKind.CHROME_PC),
            PcMenuProgram("this_pc", strings.winThisPc, Icons.Rounded.Computer, Win11Sky, Win11WindowKind.THIS_PC),
            PcMenuProgram("exe_runner", strings.winExeRunner, Icons.Rounded.Terminal, Color(0xFFFBBF24), Win11WindowKind.EXE_RUNNER),
            PcMenuProgram("notepad", strings.winNotepadApp, Icons.Rounded.Description, Win11Sky, Win11WindowKind.NOTEPAD_EXE),
            PcMenuProgram("calc", strings.winCalcApp, Icons.Rounded.Calculate, Color(0xFF34D399), Win11WindowKind.CALCULATOR_EXE),
            PcMenuProgram("cmd", strings.winCmdApp, Icons.Rounded.Terminal, Color(0xFFE2E8F0), Win11WindowKind.CMD_EXE),
            PcMenuProgram("taskmgr", strings.winTaskMgrApp, Icons.Rounded.MonitorHeart, Color(0xFFF472B6), Win11WindowKind.TASK_MANAGER_EXE),
            PcMenuProgram("settings", strings.winSettings, Icons.Rounded.Settings, Win11Blue, Win11WindowKind.WIN_SETTINGS),
            PcMenuProgram("themes", strings.winThemes, Icons.Rounded.Palette, Color(0xFFA78BFA), isThemes = true)
        )
    }

    val anyVisibleWindow = openWindows.any { !it.isMinimized }
    BackHandler(enabled = startMenuOpen || quickSettingsOpen || anyVisibleWindow) {
        when {
            startMenuOpen -> startMenuOpen = false
            quickSettingsOpen -> quickSettingsOpen = false
            else -> {
                val topIdx = openWindows.indexOfLast { !it.isMinimized }
                if (topIdx >= 0) openWindows.removeAt(topIdx)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("windows11_desktop_root")
    ) {
        // Preserved User Wallpaper (Photo / Live / Video / Minimal)
        LauncherWallpaperBackground(
            wallpaper = config.wallpaper,
            customWallpaperPath = config.customWallpaperPath,
            customVideoWallpaperPath = config.customVideoWallpaperPath,
            darkTheme = effectiveDarkTheme
        )

        // Subtle Windows 11 Mica tint overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.14f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.36f)
                        )
                    )
                )
        )

        // Desktop Workspace Area (True Full-Screen Windows 11 PC multi-window arena above Taskbar)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 54.dp)
                .pointerInput(Unit) {
                    detectTapGestures {
                        startMenuOpen = false
                        quickSettingsOpen = false
                    }
                }
        ) {
            val workspaceWidth = maxWidth
            val workspaceHeight = maxHeight
            val isWideLandscape = workspaceWidth >= 500.dp

            // Desktop Shortcut Columns — Pure PC programs + .EXE shortcuts + Standalone "Launcher" return tile
            DesktopPcColumnsGrid(
                exeShortcuts = config.pcExeShortcuts,
                strings = strings,
                emblemChar = config.emblemCharacter,
                desktopTextColor = desktopTextColor,
                workspaceHeight = workspaceHeight,
                onOpenChrome = { openOrFocusWindow(Win11WindowKind.CHROME_PC) },
                onOpenThisPc = { openOrFocusWindow(Win11WindowKind.THIS_PC) },
                onOpenExeRunner = { openOrFocusWindow(Win11WindowKind.EXE_RUNNER) },
                onOpenNotepad = { openOrFocusWindow(Win11WindowKind.NOTEPAD_EXE) },
                onOpenCalc = { openOrFocusWindow(Win11WindowKind.CALCULATOR_EXE) },
                onOpenCmd = { openOrFocusWindow(Win11WindowKind.CMD_EXE) },
                onOpenTaskMgr = { openOrFocusWindow(Win11WindowKind.TASK_MANAGER_EXE) },
                onOpenWinSettings = { openOrFocusWindow(Win11WindowKind.WIN_SETTINGS) },
                onOpenThemes = onOpenThemesHub,
                onReturnToPhoneLauncher = onExitPcMode,
                onRunExe = { shortcut -> launchShortcutOrBuiltIn(shortcut) },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )

            // Right-Side Desktop Status & Quick PC Actions HUD on Landscape Monitors
            if (isWideLandscape) {
                DesktopWinLauncherSideHud(
                    state = state,
                    strings = strings,
                    acrylicSurface = acrylicSurface,
                    acrylicBorder = acrylicBorder,
                    onOpenChrome = { openOrFocusWindow(Win11WindowKind.CHROME_PC) },
                    onOpenExeRunner = { openOrFocusWindow(Win11WindowKind.EXE_RUNNER) },
                    onOpenCmd = { openOrFocusWindow(Win11WindowKind.CMD_EXE) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                )
            }

            // Multiple Simultaneous Draggable, Customizable-Size Floating PC Windows (Z-Ordered)
            openWindows.filterNot { it.isMinimized }.forEach { win ->
                val density = LocalDensity.current
                val maxW = (workspaceWidth - 12.dp).coerceAtLeast(240.dp)
                val maxH = (workspaceHeight - 12.dp).coerceAtLeast(210.dp)
                val winW = if (win.isMaximized) workspaceWidth else win.widthDp.dp.coerceIn(240.dp, maxW)
                val winH = if (win.isMaximized) workspaceHeight else win.heightDp.dp.coerceIn(210.dp, maxH)
                val maxOffsetX = (workspaceWidth - winW).coerceAtLeast(0.dp)
                val maxOffsetY = (workspaceHeight - winH).coerceAtLeast(0.dp)
                val clampedX = if (win.isMaximized) 0.dp else win.offsetXDp.dp.coerceIn(0.dp, maxOffsetX)
                val clampedY = if (win.isMaximized) 0.dp else win.offsetYDp.dp.coerceIn(0.dp, maxOffsetY)

                Win11MultiFloatingWindow(
                    window = win,
                    state = state,
                    fileManager = fileManager,
                    chromeDownloads = chromeDownloads,
                    openWindows = openWindows,
                    workspaceWidthDp = maxW.value,
                    workspaceHeightDp = maxH.value,
                    strings = strings,
                    effectiveDarkTheme = effectiveDarkTheme,
                    acrylicSurface = acrylicSurface,
                    acrylicBorder = acrylicBorder,
                    onBringToFront = { focusWindow(win.instanceId) },
                    onMinimize = { updateWindow(win.instanceId) { it.copy(isMinimized = true) } },
                    onToggleMaximize = { updateWindow(win.instanceId) { it.copy(isMaximized = !it.isMaximized) } },
                    onClose = { closeWindow(win.instanceId) },
                    onUpdateBounds = { newX, newY, newW, newH, maximize ->
                        updateWindow(win.instanceId) { current ->
                            current.copy(
                                offsetXDp = newX.coerceIn(0f, (workspaceWidth.value - 240f).coerceAtLeast(0f)),
                                offsetYDp = newY.coerceIn(0f, (workspaceHeight.value - 210f).coerceAtLeast(0f)),
                                widthDp = newW.coerceIn(240f, maxW.value),
                                heightDp = newH.coerceIn(210f, maxH.value),
                                isMaximized = maximize
                            )
                        }
                    },
                    onDragDelta = { dxPx, dyPx ->
                        val dxDp = with(density) { dxPx.toDp().value }
                        val dyDp = with(density) { dyPx.toDp().value }
                        updateWindow(win.instanceId) { current ->
                            if (current.isMaximized) current
                            else current.copy(
                                offsetXDp = (current.offsetXDp + dxDp).coerceIn(0f, maxOffsetX.value),
                                offsetYDp = (current.offsetYDp + dyDp).coerceIn(0f, maxOffsetY.value)
                            )
                        }
                    },
                    onResizeDelta = { dxPx, dyPx ->
                        val dxDp = with(density) { dxPx.toDp().value }
                        val dyDp = with(density) { dyPx.toDp().value }
                        updateWindow(win.instanceId) { current ->
                            if (current.isMaximized) current
                            else current.copy(
                                widthDp = (current.widthDp + dxDp).coerceIn(260f, maxW.value),
                                heightDp = (current.heightDp + dyDp).coerceIn(210f, maxH.value)
                            )
                        }
                    },
                    onOpenWindowKind = { openOrFocusWindow(it) },
                    onOpenExplorerFolder = { folder ->
                        openOrFocusWindow(Win11WindowKind.THIS_PC, initialExplorerPath = folder)
                    },
                    onOpenFileEntry = { entry -> openPcFileEntry(entry) },
                    onImportFileToFolder = onImportFileToPcFolder,
                    onPickExeFile = onPickExeFile,
                    onRunExeShortcut = onRunExeShortcut,
                    onRemoveExeShortcut = onRemoveExeShortcut,
                    onFocusWindow = { focusWindow(it) },
                    onCloseWindowById = { closeWindow(it) },
                    onOpenThemesHub = onOpenThemesHub,
                    onToggleThemeMode = onToggleThemeMode,
                    onExitPcMode = onExitPcMode,
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                with(density) { clampedX.roundToPx() },
                                with(density) { clampedY.roundToPx() }
                            )
                        }
                        .width(winW)
                        .height(winH)
                )
            }

            // Centered Windows 11 Start Menu + PC App Manager (with standalone "Launcher" return tile)
            AnimatedVisibility(
                visible = startMenuOpen,
                enter = fadeIn() + slideInVertically { it / 4 },
                exit = fadeOut() + slideOutVertically { it / 4 },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Win11StartMenu(
                    state = state,
                    pcPrograms = pcPrograms,
                    exeShortcuts = config.pcExeShortcuts,
                    showAllApps = startShowAllApps,
                    searchQuery = startSearchQuery,
                    strings = strings,
                    acrylicSurface = acrylicSurface,
                    acrylicBorder = acrylicBorder,
                    onToggleAllApps = { startShowAllApps = !startShowAllApps },
                    onSearchQueryChange = { startSearchQuery = it },
                    onOpenProgram = { prog ->
                        if (prog.isThemes) {
                            startMenuOpen = false
                            onOpenThemesHub()
                        } else if (prog.windowKind != null) {
                            openOrFocusWindow(prog.windowKind)
                        }
                    },
                    onRunExe = { shortcut ->
                        startMenuOpen = false
                        launchShortcutOrBuiltIn(shortcut)
                    },
                    onReturnToPhoneLauncher = onExitPcMode
                )
            }

            // Quick Settings Flyout
            AnimatedVisibility(
                visible = quickSettingsOpen,
                enter = fadeIn() + slideInVertically { it / 5 },
                exit = fadeOut() + slideOutVertically { it / 5 },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Win11QuickSettingsFlyout(
                    strings = strings,
                    effectiveDarkTheme = effectiveDarkTheme,
                    acrylicSurface = acrylicSurface,
                    acrylicBorder = acrylicBorder,
                    onToggleTheme = {
                        onToggleThemeMode(if (effectiveDarkTheme) ThemeMode.LIGHT else ThemeMode.DARK)
                    },
                    onOpenSystemAction = {
                        quickSettingsOpen = false
                        onOpenSystemAction(it)
                    },
                    onOpenThemes = {
                        quickSettingsOpen = false
                        onOpenThemesHub()
                    },
                    onReturnToPhoneLauncher = onExitPcMode
                )
            }
        }

        // Centered Bottom Windows 11 Acrylic Taskbar (NO Launcher emblem next to Wi-Fi)
        Win11Taskbar(
            now = state.now,
            batteryLevel = state.battery.level,
            isCharging = state.battery.isCharging,
            openWindows = openWindows,
            startMenuOpen = startMenuOpen,
            strings = strings,
            acrylicSurface = acrylicSurface,
            acrylicBorder = acrylicBorder,
            onToggleStart = {
                quickSettingsOpen = false
                startMenuOpen = !startMenuOpen
            },
            onOpenSearch = {
                quickSettingsOpen = false
                startShowAllApps = true
                startMenuOpen = true
            },
            onClickPinnedKind = { toggleTaskbarPinnedKind(it) },
            onClickWindowTab = { toggleTaskbarWindow(it) },
            onMinimizeAll = {
                val anyOpen = openWindows.any { !it.isMinimized }
                for (i in openWindows.indices) {
                    openWindows[i] = openWindows[i].copy(isMinimized = anyOpen)
                }
            },
            onToggleQuickSettings = {
                startMenuOpen = false
                quickSettingsOpen = !quickSettingsOpen
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun DesktopPcColumnsGrid(
    exeShortcuts: List<PcExeShortcut>,
    strings: AppStrings,
    emblemChar: String,
    desktopTextColor: Color,
    workspaceHeight: androidx.compose.ui.unit.Dp,
    onOpenChrome: () -> Unit,
    onOpenThisPc: () -> Unit,
    onOpenExeRunner: () -> Unit,
    onOpenNotepad: () -> Unit,
    onOpenCalc: () -> Unit,
    onOpenCmd: () -> Unit,
    onOpenTaskMgr: () -> Unit,
    onOpenWinSettings: () -> Unit,
    onOpenThemes: () -> Unit,
    onReturnToPhoneLauncher: () -> Unit,
    onRunExe: (PcExeShortcut) -> Unit,
    modifier: Modifier = Modifier
) {
    val entries = buildList<@Composable () -> Unit> {
        // Standalone "Launcher" return tile outside Windows 11 styling
        add {
            DesktopNativeLauncherReturnIcon(
                label = strings.returnLauncherLabel,
                emblemChar = emblemChar,
                textColor = desktopTextColor,
                onClick = onReturnToPhoneLauncher,
                modifier = Modifier.testTag("win11_desktop_return_launcher")
            )
        }
        add {
            DesktopVectorIcon(
                label = strings.winThisPc,
                icon = Icons.Rounded.Computer,
                tint = Win11Sky,
                textColor = desktopTextColor,
                onClick = onOpenThisPc,
                modifier = Modifier.testTag("win11_desktop_this_pc")
            )
        }
        add {
            DesktopVectorIcon(
                label = strings.winChromeApp,
                icon = Icons.Rounded.Language,
                tint = Color(0xFF4285F4),
                textColor = desktopTextColor,
                onClick = onOpenChrome,
                modifier = Modifier.testTag("win11_desktop_chrome")
            )
        }
        add {
            DesktopVectorIcon(
                label = strings.winExeRunner,
                icon = Icons.Rounded.Terminal,
                tint = Color(0xFFFBBF24),
                textColor = desktopTextColor,
                onClick = onOpenExeRunner,
                modifier = Modifier.testTag("win11_desktop_exe_runner")
            )
        }
        add {
            DesktopVectorIcon(
                label = strings.winNotepadApp,
                icon = Icons.Rounded.Description,
                tint = Win11Sky,
                textColor = desktopTextColor,
                onClick = onOpenNotepad,
                modifier = Modifier.testTag("win11_desktop_notepad")
            )
        }
        add {
            DesktopVectorIcon(
                label = strings.winCalcApp,
                icon = Icons.Rounded.Calculate,
                tint = Color(0xFF34D399),
                textColor = desktopTextColor,
                onClick = onOpenCalc,
                modifier = Modifier.testTag("win11_desktop_calc")
            )
        }
        add {
            DesktopVectorIcon(
                label = strings.winCmdApp,
                icon = Icons.Rounded.Terminal,
                tint = Color(0xFFE2E8F0),
                textColor = desktopTextColor,
                onClick = onOpenCmd,
                modifier = Modifier.testTag("win11_desktop_cmd")
            )
        }
        add {
            DesktopVectorIcon(
                label = strings.winTaskMgrApp,
                icon = Icons.Rounded.MonitorHeart,
                tint = Color(0xFFF472B6),
                textColor = desktopTextColor,
                onClick = onOpenTaskMgr,
                modifier = Modifier.testTag("win11_desktop_taskmgr")
            )
        }
        add {
            DesktopVectorIcon(
                label = strings.winThemes,
                icon = Icons.Rounded.Palette,
                tint = Color(0xFFA78BFA),
                textColor = desktopTextColor,
                onClick = onOpenThemes,
                modifier = Modifier.testTag("win11_desktop_themes")
            )
        }
        add {
            DesktopVectorIcon(
                label = strings.winSettings,
                icon = Icons.Rounded.Settings,
                tint = Win11Blue,
                textColor = desktopTextColor,
                onClick = onOpenWinSettings,
                modifier = Modifier.testTag("win11_desktop_settings")
            )
        }
        exeShortcuts.take(8).forEach { exe ->
            add {
                DesktopVectorIcon(
                    label = exe.name,
                    icon = Icons.Rounded.PlayArrow,
                    tint = Color(0xFF38BDF8),
                    textColor = desktopTextColor,
                    onClick = { onRunExe(exe) },
                    modifier = Modifier.testTag("win11_desktop_exe_${exe.id}")
                )
            }
        }
    }

    val rowsPerColumn = ((workspaceHeight.value - 20f) / 84f).toInt().coerceIn(2, 6)
    val columns = entries.chunked(rowsPerColumn)

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.horizontalScroll(rememberScrollState())
    ) {
        columns.forEach { columnItems ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                columnItems.forEach { renderItem ->
                    Box(modifier = Modifier.width(78.dp)) {
                        renderItem()
                    }
                }
            }
        }
    }
}

@Composable
private fun DesktopWinLauncherSideHud(
    state: LauncherUiState,
    strings: AppStrings,
    acrylicSurface: Color,
    acrylicBorder: Color,
    onOpenChrome: () -> Unit,
    onOpenExeRunner: () -> Unit,
    onOpenCmd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeStr = state.now.format(DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH))
    val dateStr = state.now.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH))
    val ramProgress = if (state.device.totalRamMb > 0) {
        (state.device.usedRamMb.toFloat() / state.device.totalRamMb.toFloat()).coerceIn(0f, 1f)
    } else 0.42f

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = acrylicSurface.copy(alpha = 0.78f),
        modifier = modifier
            .width(210.dp)
            .border(1.dp, acrylicBorder, RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = Win11Sky
                )
            }
            Text(
                text = strings.winDesktopPreservedNote(state.config.pcExeShortcuts.size),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            LinearProgressIndicator(
                progress = { ramProgress },
                color = Win11Sky,
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                Surface(
                    onClick = onOpenChrome,
                    shape = RoundedCornerShape(10.dp),
                    color = Win11Blue.copy(alpha = 0.22f),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Chrome",
                        style = MaterialTheme.typography.labelSmall,
                        color = Win11Sky,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
                Surface(
                    onClick = onOpenExeRunner,
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFBBF24).copy(alpha = 0.2f),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = ".EXE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFBBF24),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
                Surface(
                    onClick = onOpenCmd,
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "CMD",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Win11MultiFloatingWindow(
    window: PcFloatingWindowState,
    state: LauncherUiState,
    fileManager: PcFileSystemManager,
    chromeDownloads: MutableList<PcDownloadItem>,
    openWindows: List<PcFloatingWindowState>,
    workspaceWidthDp: Float,
    workspaceHeightDp: Float,
    strings: AppStrings,
    effectiveDarkTheme: Boolean,
    acrylicSurface: Color,
    acrylicBorder: Color,
    onBringToFront: () -> Unit,
    onMinimize: () -> Unit,
    onToggleMaximize: () -> Unit,
    onClose: () -> Unit,
    onUpdateBounds: (newX: Float, newY: Float, newW: Float, newH: Float, maximize: Boolean) -> Unit,
    onDragDelta: (Float, Float) -> Unit,
    onResizeDelta: (Float, Float) -> Unit,
    onOpenWindowKind: (Win11WindowKind) -> Unit,
    onOpenExplorerFolder: (String) -> Unit,
    onOpenFileEntry: (PcFileEntry) -> Unit,
    onImportFileToFolder: (String) -> Unit,
    onPickExeFile: () -> Unit,
    onRunExeShortcut: (PcExeShortcut) -> Unit,
    onRemoveExeShortcut: (String) -> Unit,
    onFocusWindow: (String) -> Unit,
    onCloseWindowById: (String) -> Unit,
    onOpenThemesHub: () -> Unit,
    onToggleThemeMode: (ThemeMode) -> Unit,
    onExitPcMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSizePanel by remember(window.instanceId) { mutableStateOf(false) }
    val corner = if (window.isMaximized) 0.dp else 14.dp

    Surface(
        shape = RoundedCornerShape(corner),
        color = acrylicSurface,
        shadowElevation = 20.dp,
        modifier = modifier
            .border(1.dp, acrylicBorder, RoundedCornerShape(corner))
            .pointerInput(window.instanceId) {
                detectTapGestures(onPress = {
                    onBringToFront()
                    tryAwaitRelease()
                })
            }
            .testTag("win11_window_${window.kind.name}")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Draggable Windows 11 Titlebar with Window Size Customizer, (—), (□), and (X)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (effectiveDarkTheme) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f))
                        .pointerInput(window.instanceId, window.isMaximized) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                onBringToFront()
                                onDragDelta(dragAmount.x, dragAmount.y)
                            }
                        }
                        .padding(start = 12.dp, end = 6.dp, top = 4.dp, bottom = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = iconForWindowKind(window.kind),
                            contentDescription = null,
                            tint = tintForWindowKind(window.kind),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = window.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Customize Window Size & Snap
                        IconButton(
                            onClick = {
                                onBringToFront()
                                showSizePanel = !showSizePanel
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("win11_window_size_${window.kind.name}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AspectRatio,
                                contentDescription = strings.winWindowSize,
                                tint = if (showSizePanel) Win11Sky else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        // — Minimize
                        TextButton(
                            onClick = onMinimize,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("win11_window_minimize_${window.kind.name}")
                        ) {
                            Text("—", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        // □ Maximize / ❐ Restore
                        TextButton(
                            onClick = onToggleMaximize,
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("win11_window_maximize_${window.kind.name}")
                        ) {
                            Text(if (window.isMaximized) "❐" else "□", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        // X Close
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFC42B1C).copy(alpha = 0.16f))
                                .testTag("win11_window_close_${window.kind.name}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = strings.winClose,
                                tint = Color(0xFFFF5F56),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Window Size & Snap Customizer Panel
                AnimatedVisibility(visible = showSizePanel) {
                    Surface(
                        color = if (effectiveDarkTheme) Color(0xFF151923) else Color(0xFFEAF0FA),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, acrylicBorder)
                            .testTag("win11_window_size_panel")
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${strings.winWindowSize}: ${window.widthDp.toInt()} × ${window.heightDp.toInt()} dp",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Win11Sky
                                )
                                IconButton(onClick = { showSizePanel = false }, modifier = Modifier.size(22.dp)) {
                                    Icon(Icons.Rounded.Close, contentDescription = strings.winClose, modifier = Modifier.size(14.dp))
                                }
                            }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(
                                    strings.winSizeSmall to {
                                        onUpdateBounds(window.offsetXDp, window.offsetYDp, 340f, 240f, false)
                                    },
                                    strings.winSizeMedium to {
                                        onUpdateBounds(window.offsetXDp, window.offsetYDp, 480f, 310f, false)
                                    },
                                    strings.winSizeLarge to {
                                        onUpdateBounds(window.offsetXDp, window.offsetYDp, workspaceWidthDp * 0.86f, workspaceHeightDp * 0.88f, false)
                                    },
                                    strings.winSnapLeft to {
                                        onUpdateBounds(0f, 0f, workspaceWidthDp / 2f, workspaceHeightDp, false)
                                    },
                                    strings.winSnapRight to {
                                        onUpdateBounds(workspaceWidthDp / 2f, 0f, workspaceWidthDp / 2f, workspaceHeightDp, false)
                                    },
                                    strings.winMaximize to {
                                        onUpdateBounds(0f, 0f, workspaceWidthDp, workspaceHeightDp, true)
                                    }
                                ).forEach { (presetLabel, apply) ->
                                    Surface(
                                        onClick = apply,
                                        shape = RoundedCornerShape(8.dp),
                                        color = Win11Blue.copy(alpha = 0.2f),
                                        modifier = Modifier.testTag("win11_size_preset_$presetLabel")
                                    ) {
                                        Text(
                                            text = presetLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Win11Sky,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                            val maxSliderW = workspaceWidthDp.coerceAtLeast(320f)
                            val maxSliderH = workspaceHeightDp.coerceAtLeast(260f)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(strings.winWidth, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(56.dp))
                                Slider(
                                    value = window.widthDp.coerceIn(260f, maxSliderW),
                                    onValueChange = { newW ->
                                        onUpdateBounds(window.offsetXDp, window.offsetYDp, newW, window.heightDp, false)
                                    },
                                    valueRange = 260f..maxSliderW,
                                    modifier = Modifier.weight(1f).height(26.dp)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(strings.winHeight, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(56.dp))
                                Slider(
                                    value = window.heightDp.coerceIn(210f, maxSliderH),
                                    onValueChange = { newH ->
                                        onUpdateBounds(window.offsetXDp, window.offsetYDp, window.widthDp, newH, false)
                                    },
                                    valueRange = 210f..maxSliderH,
                                    modifier = Modifier.weight(1f).height(26.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = acrylicBorder)

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    when (window.kind) {
                        Win11WindowKind.CHROME_PC -> ChromePcWindowBody(
                            initialUrl = window.initialUrl,
                            strings = strings,
                            effectiveDarkTheme = effectiveDarkTheme,
                            fileManager = fileManager,
                            downloads = chromeDownloads,
                            onOpenDownloadsFolder = { onOpenExplorerFolder("Downloads") },
                            onOpenDownloadedFile = onOpenFileEntry
                        )
                        Win11WindowKind.THIS_PC -> ThisPcExplorerBody(
                            state = state,
                            initialRelativePath = window.initialExplorerPath,
                            fileManager = fileManager,
                            strings = strings,
                            onOpenFileEntry = onOpenFileEntry,
                            onImportFileToFolder = onImportFileToFolder
                        )
                        Win11WindowKind.EXE_RUNNER -> ExeRunnerBody(
                            exeShortcuts = state.config.pcExeShortcuts,
                            fileManager = fileManager,
                            wineBridges = state.wineBridgePackages,
                            logs = state.pcExecutionLogs,
                            strings = strings,
                            onPickExeFile = onPickExeFile,
                            onRunExeShortcut = onRunExeShortcut,
                            onRemoveExeShortcut = onRemoveExeShortcut,
                            onOpenDownloadedExe = onOpenFileEntry,
                            onOpenBuiltInExe = onOpenWindowKind
                        )
                        Win11WindowKind.WIN_SETTINGS -> Win11SettingsBody(
                            state = state,
                            strings = strings,
                            effectiveDarkTheme = effectiveDarkTheme,
                            onToggleTheme = {
                                onToggleThemeMode(if (effectiveDarkTheme) ThemeMode.LIGHT else ThemeMode.DARK)
                            },
                            onOpenThemes = onOpenThemesHub,
                            onReturnToPhoneLauncher = onExitPcMode
                        )
                        Win11WindowKind.NOTEPAD_EXE -> NotepadExeBody(
                            strings = strings,
                            fileToOpenPath = window.fileToOpenPath,
                            fileManager = fileManager
                        )
                        Win11WindowKind.CALCULATOR_EXE -> CalculatorExeBody()
                        Win11WindowKind.CMD_EXE -> CmdExeBody(
                            state = state,
                            fileManager = fileManager,
                            onOpenWindowKind = onOpenWindowKind
                        )
                        Win11WindowKind.TASK_MANAGER_EXE -> TaskManagerExeBody(
                            openWindows = openWindows,
                            state = state,
                            strings = strings,
                            onFocusWindow = onFocusWindow,
                            onEndTaskWindow = onCloseWindowById
                        )
                        Win11WindowKind.PHOTO_VIEWER_EXE -> PhotoViewerExeBody(
                            filePath = window.fileToOpenPath,
                            strings = strings
                        )
                    }
                }
            }

            // Bottom-right Resize Grip when floating
            if (!window.isMaximized) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(20.dp)
                        .pointerInput(window.instanceId) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                onBringToFront()
                                onResizeDelta(dragAmount.x, dragAmount.y)
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.45f),
                            start = Offset(size.width * 0.2f, size.height),
                            end = Offset(size.width, size.height * 0.2f),
                            strokeWidth = 2f
                        )
                    }
                }
            }
        }
    }
}

private fun iconForWindowKind(kind: Win11WindowKind): ImageVector = when (kind) {
    Win11WindowKind.CHROME_PC -> Icons.Rounded.Language
    Win11WindowKind.THIS_PC -> Icons.Rounded.Computer
    Win11WindowKind.EXE_RUNNER -> Icons.Rounded.Terminal
    Win11WindowKind.WIN_SETTINGS -> Icons.Rounded.Settings
    Win11WindowKind.NOTEPAD_EXE -> Icons.Rounded.Description
    Win11WindowKind.CALCULATOR_EXE -> Icons.Rounded.Calculate
    Win11WindowKind.CMD_EXE -> Icons.Rounded.Terminal
    Win11WindowKind.TASK_MANAGER_EXE -> Icons.Rounded.MonitorHeart
    Win11WindowKind.PHOTO_VIEWER_EXE -> Icons.Rounded.Image
}

private fun tintForWindowKind(kind: Win11WindowKind): Color = when (kind) {
    Win11WindowKind.CHROME_PC -> Color(0xFF4285F4)
    Win11WindowKind.THIS_PC -> Win11Sky
    Win11WindowKind.EXE_RUNNER -> Color(0xFFFBBF24)
    Win11WindowKind.WIN_SETTINGS -> Win11Blue
    Win11WindowKind.NOTEPAD_EXE -> Win11Sky
    Win11WindowKind.CALCULATOR_EXE -> Color(0xFF34D399)
    Win11WindowKind.CMD_EXE -> Color(0xFFE2E8F0)
    Win11WindowKind.TASK_MANAGER_EXE -> Color(0xFFF472B6)
    Win11WindowKind.PHOTO_VIEWER_EXE -> Color(0xFFA78BFA)
}

@Composable
private fun DesktopNativeLauncherReturnIcon(
    label: String,
    emblemChar: String,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 5.dp, horizontal = 3.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(Color(0xFF141824), MaterialTheme.colorScheme.primary))
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.72f), CircleShape)
        ) {
            val ch = emblemChar.trim()
            if (ch.isEmpty() || ch.equals("N", ignoreCase = true)) {
                ModernGeometricNMonogram(
                    accentColor = Color.White,
                    contentColor = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = ch,
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = desktopLabelStyle(textColor),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DesktopVectorIcon(
    label: String,
    icon: ImageVector,
    tint: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 5.dp, horizontal = 3.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Color(0xFF101828).copy(alpha = 0.68f))
                .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(11.dp))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(25.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = desktopLabelStyle(textColor),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

private fun desktopLabelStyle(textColor: Color) = TextStyle(
    fontSize = 10.5.sp,
    fontWeight = FontWeight.Medium,
    color = textColor,
    shadow = Shadow(
        color = Color.Black.copy(alpha = 0.92f),
        offset = Offset(0f, 1.5f),
        blurRadius = 4f
    )
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Win11StartMenu(
    state: LauncherUiState,
    pcPrograms: List<PcMenuProgram>,
    exeShortcuts: List<PcExeShortcut>,
    showAllApps: Boolean,
    searchQuery: String,
    strings: AppStrings,
    acrylicSurface: Color,
    acrylicBorder: Color,
    onToggleAllApps: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onOpenProgram: (PcMenuProgram) -> Unit,
    onRunExe: (PcExeShortcut) -> Unit,
    onReturnToPhoneLauncher: () -> Unit
) {
    val q = searchQuery.trim().lowercase()
    val filteredPrograms = remember(pcPrograms, q) {
        if (q.isEmpty()) pcPrograms else pcPrograms.filter { it.title.lowercase().contains(q) }
    }
    val filteredExes = remember(exeShortcuts, q) {
        if (q.isEmpty()) exeShortcuts else exeShortcuts.filter { it.name.lowercase().contains(q) }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = acrylicSurface,
        shadowElevation = 22.dp,
        modifier = Modifier
            .widthIn(max = 520.dp)
            .fillMaxWidth()
            .heightIn(max = 390.dp)
            .border(1.dp, acrylicBorder, RoundedCornerShape(20.dp))
            .testTag("win11_start_menu")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Win11Sky) },
                placeholder = { Text(strings.winSearchHint, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                singleLine = true,
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (showAllApps || q.isNotEmpty()) strings.winAllApps else strings.winPinned,
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(
                    onClick = onToggleAllApps,
                    modifier = Modifier.testTag("win11_all_apps_toggle")
                ) {
                    Text(if (showAllApps) strings.winPinned else "${strings.winAllApps} ›")
                }
            }

            // Prominent standalone "Launcher" return tile in PC App Manager OUTSIDE Windows 11 styling
            Surface(
                onClick = onReturnToPhoneLauncher,
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.88f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                    .testTag("win11_start_return_launcher")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        ModernGeometricNMonogram(
                            accentColor = Color.White,
                            contentColor = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.returnLauncherLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = strings.winSwitchToMobile,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Rounded.PhoneAndroid, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (showAllApps || q.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f, fill = false).heightIn(max = 180.dp)
                ) {
                    items(filteredPrograms, key = { it.id }) { prog ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onOpenProgram(prog) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(prog.icon, contentDescription = prog.title, tint = prog.tint, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(prog.title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        }
                    }
                    items(filteredExes, key = { it.id }) { exe ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onRunExe(exe) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = exe.name, tint = Win11Sky, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(exe.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    pcPrograms.forEach { prog ->
                        val tag = when {
                            prog.windowKind != null -> "win11_start_tile_${prog.windowKind.name}"
                            prog.isThemes -> "win11_start_tile_THEMES"
                            else -> "win11_start_tile_${prog.id}"
                        }
                        StartMiniTile(prog.title, prog.icon, prog.tint, Modifier.testTag(tag)) {
                            onOpenProgram(prog)
                        }
                    }
                    exeShortcuts.take(6).forEach { exe ->
                        StartMiniTile(exe.name, Icons.Rounded.PlayArrow, Win11Sky) {
                            onRunExe(exe)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = acrylicBorder)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(24.dp).clip(CircleShape).background(Win11Blue)
                    ) {
                        Text(
                            text = state.config.emblemCharacter.take(1).ifBlank { "N" },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Windows 11 PC", style = MaterialTheme.typography.labelMedium)
                }
                TextButton(
                    onClick = onReturnToPhoneLauncher,
                    modifier = Modifier.testTag("win11_switch_to_mobile_button")
                ) {
                    Icon(Icons.Rounded.Home, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.returnLauncherLabel)
                }
            }
        }
    }
}

@Composable
private fun StartMiniTile(
    label: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(64.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(tint.copy(alpha = 0.16f))
        ) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(19.dp))
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun Win11QuickSettingsFlyout(
    strings: AppStrings,
    effectiveDarkTheme: Boolean,
    acrylicSurface: Color,
    acrylicBorder: Color,
    onToggleTheme: () -> Unit,
    onOpenSystemAction: (String) -> Unit,
    onOpenThemes: () -> Unit,
    onReturnToPhoneLauncher: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = acrylicSurface,
        shadowElevation = 20.dp,
        modifier = Modifier
            .widthIn(max = 310.dp)
            .border(1.dp, acrylicBorder, RoundedCornerShape(18.dp))
            .testTag("win11_quick_settings")
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                WinQuickTile(Icons.Rounded.Wifi, strings.wifiShortcut, Modifier.weight(1f)) {
                    onOpenSystemAction(Settings.ACTION_WIFI_SETTINGS)
                }
                WinQuickTile(Icons.Rounded.Bluetooth, strings.bluetoothShortcut, Modifier.weight(1f)) {
                    onOpenSystemAction(Settings.ACTION_BLUETOOTH_SETTINGS)
                }
                WinQuickTile(Icons.Rounded.Brightness6, strings.displayShortcut, Modifier.weight(1f)) {
                    onOpenSystemAction(Settings.ACTION_DISPLAY_SETTINGS)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                WinQuickTile(
                    if (effectiveDarkTheme) Icons.Rounded.DarkMode else Icons.Rounded.LightMode,
                    strings.themeModeName(if (effectiveDarkTheme) ThemeMode.DARK else ThemeMode.LIGHT),
                    Modifier.weight(1f),
                    onClick = onToggleTheme
                )
                WinQuickTile(Icons.Rounded.Palette, strings.winThemes, Modifier.weight(1f), onClick = onOpenThemes)
            }
            Button(
                onClick = onReturnToPhoneLauncher,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.PhoneAndroid, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("${strings.returnLauncherLabel} (${strings.winSwitchToMobile})")
            }
        }
    }
}

@Composable
private fun WinQuickTile(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
        ) {
            Icon(icon, contentDescription = label, tint = Win11Sky, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(3.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun Win11Taskbar(
    now: java.time.LocalDateTime,
    batteryLevel: Int,
    isCharging: Boolean,
    openWindows: List<PcFloatingWindowState>,
    startMenuOpen: Boolean,
    strings: AppStrings,
    acrylicSurface: Color,
    acrylicBorder: Color,
    onToggleStart: () -> Unit,
    onOpenSearch: () -> Unit,
    onClickPinnedKind: (Win11WindowKind) -> Unit,
    onClickWindowTab: (String) -> Unit,
    onMinimizeAll: () -> Unit,
    onToggleQuickSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeStr = now.format(DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH))
    val dateStr = now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH))
    val topVisibleId = openWindows.lastOrNull { !it.isMinimized }?.instanceId

    val pinnedKinds = listOf(
        Win11WindowKind.THIS_PC,
        Win11WindowKind.CHROME_PC,
        Win11WindowKind.EXE_RUNNER
    )
    val dynamicExtraWindows = openWindows.filter { it.kind !in pinnedKinds }

    Surface(
        color = acrylicSurface,
        shadowElevation = 18.dp,
        modifier = modifier
            .border(width = 1.dp, color = acrylicBorder)
            .testTag("win11_taskbar")
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 8.dp)
        ) {
            val compact = maxWidth < 460.dp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {
                if (!compact) {
                    Spacer(modifier = Modifier.width(64.dp))
                }

                // Centered Windows 11 Dock (Start + Search + Pinned PC Apps + Running Windows)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        TaskbarIconBox(
                            active = startMenuOpen,
                            hasIndicator = startMenuOpen,
                            onClick = onToggleStart,
                            modifier = Modifier.testTag("win11_start_button")
                        ) {
                            Win11LogoGlyph(modifier = Modifier.size(20.dp))
                        }

                        if (compact) {
                            TaskbarIconBox(
                                active = false,
                                hasIndicator = false,
                                onClick = onOpenSearch,
                                modifier = Modifier.testTag("win11_search_button")
                            ) {
                                Icon(Icons.Rounded.Search, contentDescription = strings.winSearchHint, tint = Win11Sky, modifier = Modifier.size(18.dp))
                            }
                        } else {
                            Surface(
                                onClick = onOpenSearch,
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                modifier = Modifier
                                    .height(34.dp)
                                    .widthIn(min = 100.dp, max = 148.dp)
                                    .testTag("win11_search_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                ) {
                                    Icon(Icons.Rounded.Search, contentDescription = null, tint = Win11Sky, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Search", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        pinnedKinds.forEach { kind ->
                            val matching = openWindows.lastOrNull { it.kind == kind }
                            val tag = when (kind) {
                                Win11WindowKind.THIS_PC -> "win11_taskbar_explorer"
                                Win11WindowKind.CHROME_PC -> "win11_taskbar_chrome"
                                Win11WindowKind.EXE_RUNNER -> "win11_taskbar_exe"
                                else -> "win11_taskbar_${kind.name}"
                            }
                            TaskbarIconBox(
                                active = matching != null && matching.instanceId == topVisibleId,
                                hasIndicator = matching != null,
                                onClick = { onClickPinnedKind(kind) },
                                modifier = Modifier.testTag(tag)
                            ) {
                                Icon(
                                    imageVector = if (kind == Win11WindowKind.THIS_PC) Icons.Rounded.Folder else iconForWindowKind(kind),
                                    contentDescription = kind.name,
                                    tint = if (kind == Win11WindowKind.THIS_PC) Color(0xFFFACC15) else tintForWindowKind(kind),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        dynamicExtraWindows.forEach { win ->
                            TaskbarIconBox(
                                active = win.instanceId == topVisibleId,
                                hasIndicator = true,
                                onClick = { onClickWindowTab(win.instanceId) },
                                modifier = Modifier.testTag("win11_taskbar_dyn_${win.kind.name}")
                            ) {
                                Icon(
                                    imageVector = iconForWindowKind(win.kind),
                                    contentDescription = win.title,
                                    tint = tintForWindowKind(win.kind),
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }
                }

                // Right System Tray (Wi-Fi, Volume, Battery, Clock) + Show Desktop sliver
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onToggleQuickSettings)
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("win11_system_tray")
                    ) {
                        if (!compact) {
                            Icon(Icons.Rounded.Wifi, contentDescription = strings.wifiShortcut, modifier = Modifier.size(15.dp))
                            Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = strings.mediaVolume, modifier = Modifier.size(15.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCharging) Icons.Rounded.BatteryChargingFull else Icons.Rounded.BatteryStd,
                                contentDescription = "$batteryLevel%",
                                tint = if (isCharging) Color(0xFF4ADE80) else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(text = "$batteryLevel%", style = MaterialTheme.typography.labelSmall)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = timeStr, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Text(text = dateStr, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(34.dp)
                            .padding(start = 2.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.14f))
                            .clickable(onClick = onMinimizeAll)
                            .testTag("win11_show_desktop_sliver")
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskbarIconBox(
    active: Boolean,
    hasIndicator: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .size(38.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (active) Color.White.copy(alpha = 0.16f) else Color.Transparent)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
            content()
        }
        if (hasIndicator) {
            Box(
                modifier = Modifier
                    .width(if (active) 16.dp else 7.dp)
                    .height(2.5.dp)
                    .clip(CircleShape)
                    .background(if (active) Win11Sky else Color.White.copy(alpha = 0.55f))
            )
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

@Composable
fun Win11LogoGlyph(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val gap = size.minDimension * 0.10f
        val pane = (size.minDimension - gap) / 2f
        val r = CornerRadius(pane * 0.14f, pane * 0.14f)
        drawRoundRect(color = Color(0xFF7DE0FF), topLeft = Offset.Zero, size = Size(pane, pane), cornerRadius = r)
        drawRoundRect(color = Color(0xFF50C3FF), topLeft = Offset(pane + gap, 0f), size = Size(pane, pane), cornerRadius = r)
        drawRoundRect(color = Color(0xFF3AA8FA), topLeft = Offset(0f, pane + gap), size = Size(pane, pane), cornerRadius = r)
        drawRoundRect(color = Color(0xFF0078D4), topLeft = Offset(pane + gap, pane + gap), size = Size(pane, pane), cornerRadius = r)
    }
}
