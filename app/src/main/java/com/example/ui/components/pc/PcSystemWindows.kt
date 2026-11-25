package com.example.ui.components.pc

import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PcFileEntry
import com.example.data.PcFileSystemManager
import com.example.model.PcExeShortcut
import com.example.model.ThemeMode
import com.example.ui.strings.AppStrings
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.viewmodel.LauncherUiState
import java.util.Locale

private val Win11Blue = Color(0xFF0078D4)
private val Win11Sky = Color(0xFF60CDFF)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThisPcExplorerBody(
    state: LauncherUiState,
    initialRelativePath: String,
    fileManager: PcFileSystemManager,
    strings: AppStrings,
    onOpenFileEntry: (PcFileEntry) -> Unit,
    onImportFileToFolder: (String) -> Unit
) {
    val dev = state.device
    val storageUsed = (dev.totalStorageGb - dev.freeStorageGb).coerceAtLeast(0f)
    val storageProgress = if (dev.totalStorageGb > 0f) (storageUsed / dev.totalStorageGb).coerceIn(0f, 1f) else 0.35f

    var currentPath by remember(initialRelativePath) { mutableStateOf(initialRelativePath) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedEntryPath by remember { mutableStateOf<String?>(null) }
    var refreshTick by remember { mutableIntStateOf(0) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    LaunchedEffect(initialRelativePath) {
        if (initialRelativePath.isNotBlank()) {
            currentPath = initialRelativePath
        }
    }

    val entries = remember(currentPath, refreshTick, state.config.pcExeShortcuts) {
        fileManager.listEntries(currentPath)
    }
    val filteredEntries = remember(entries, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) entries else entries.filter { it.name.lowercase().contains(q) }
    }
    val selectedEntry = filteredEntries.firstOrNull { it.relativePath == selectedEntryPath }

    fun openEntry(entry: PcFileEntry) {
        if (entry.isDirectory) {
            currentPath = entry.relativePath
            selectedEntryPath = null
        } else {
            onOpenFileEntry(entry)
        }
    }

    val protectedRoots = setOf("Desktop", "Downloads", "Documents", "Pictures", "Program Files")
    val quickPlaces = listOf(
        "" to strings.winThisPc,
        "Desktop" to "Desktop",
        "Downloads" to strings.winDownloads,
        "Documents" to "Documents",
        "Pictures" to "Pictures",
        "Program Files" to "Program Files"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("win11_this_pc_explorer")
    ) {
        // Windows 11 Explorer Command Ribbon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            IconButton(
                onClick = {
                    if (currentPath.isNotEmpty()) {
                        currentPath = if (currentPath.contains('/')) currentPath.substringBeforeLast('/') else ""
                        selectedEntryPath = null
                    }
                },
                enabled = currentPath.isNotEmpty(),
                modifier = Modifier.size(30.dp).testTag("explorer_up_button")
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Up", modifier = Modifier.size(16.dp))
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                modifier = Modifier.weight(1f).height(30.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp)) {
                    Icon(Icons.Rounded.Folder, contentDescription = null, tint = Color(0xFFFACC15), modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    val displayPath = if (currentPath.isEmpty()) "This PC (C:)" else "C:\\${currentPath.replace('/', '\\')}"
                    Text(
                        text = displayPath,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("explorer_path_text")
                    )
                }
            }

            IconButton(
                onClick = { showNewFolderDialog = true },
                modifier = Modifier.size(30.dp).testTag("explorer_new_folder_button")
            ) {
                Icon(Icons.Rounded.CreateNewFolder, contentDescription = strings.winNewFolder, tint = Win11Sky, modifier = Modifier.size(17.dp))
            }
            IconButton(
                onClick = { onImportFileToFolder(currentPath.ifBlank { "Downloads" }) },
                modifier = Modifier.size(30.dp).testTag("explorer_import_file_button")
            ) {
                Icon(Icons.Rounded.UploadFile, contentDescription = strings.winImportFile, tint = Win11Sky, modifier = Modifier.size(17.dp))
            }
            IconButton(
                onClick = { refreshTick++ },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Refresh", modifier = Modifier.size(16.dp))
            }
        }

        // Adaptive Quick Access + Files Area
        androidx.compose.foundation.layout.BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val useSidebar = maxWidth >= 400.dp

            @Composable
            fun QuickPlacesStrip(vertical: Boolean) {
                quickPlaces.forEach { (rel, label) ->
                    val active = currentPath == rel
                    Surface(
                        onClick = {
                            currentPath = rel
                            selectedEntryPath = null
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (active) Win11Blue.copy(alpha = 0.2f) else Color.Transparent,
                        modifier = (if (vertical) Modifier.fillMaxWidth() else Modifier)
                            .testTag("explorer_nav_${rel.ifBlank { "root" }}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = when (rel) {
                                    "" -> Icons.Rounded.Computer
                                    "Downloads" -> Icons.Rounded.Download
                                    "Pictures" -> Icons.Rounded.Image
                                    else -> Icons.Rounded.Folder
                                },
                                contentDescription = label,
                                tint = if (active) Win11Sky else Color(0xFFFACC15),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            @Composable
            fun FilesContentPane(modifier: Modifier = Modifier) {
                Column(modifier = modifier.fillMaxSize()) {
                    // Search + Open selected bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(15.dp)) },
                            placeholder = { Text(strings.winFilesSearch, fontSize = 11.sp, maxLines = 1) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).height(42.dp)
                        )
                        if (selectedEntry != null) {
                            Button(
                                onClick = { openEntry(selectedEntry) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Win11Blue, contentColor = Color.White),
                                modifier = Modifier.height(34.dp).testTag("explorer_open_selected_button")
                            ) {
                                Text(strings.winOpenSelected, fontSize = 11.sp)
                            }
                        }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag("explorer_files_list")
                    ) {
                        if (currentPath.isEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                    ) {
                                        Icon(Icons.Rounded.Storage, contentDescription = null, tint = Win11Sky, modifier = Modifier.size(16.dp))
                                        Text("Local Disk (C:)", style = MaterialTheme.typography.labelMedium)
                                        LinearProgressIndicator(
                                            progress = { storageProgress },
                                            color = Win11Blue,
                                            modifier = Modifier.weight(1f).height(5.dp).clip(CircleShape)
                                        )
                                        Text(
                                            text = String.format(Locale.ENGLISH, "%.0f/%.0f GB", dev.freeStorageGb, dev.totalStorageGb),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                    if (filteredEntries.isEmpty()) {
                        item {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth().padding(24.dp)
                            ) {
                                Text(
                                    text = strings.winEmptyFolder,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(filteredEntries, key = { it.relativePath }) { entry ->
                            val isSelected = selectedEntryPath == entry.relativePath
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Win11Blue.copy(alpha = 0.22f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (isSelected) 1.dp else 0.dp,
                                        color = if (isSelected) Win11Sky else Color.Transparent,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .combinedClickable(
                                        onClick = { selectedEntryPath = entry.relativePath },
                                        onDoubleClick = { openEntry(entry) }
                                    )
                                    .testTag("explorer_entry_${entry.name}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = iconForFileEntry(entry),
                                        contentDescription = entry.name,
                                        tint = tintForFileEntry(entry),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = entry.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (entry.isDirectory) "File Folder" else "${entry.extension.uppercase().ifBlank { "FILE" }} • ${formatFileSize(entry.sizeBytes)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (!entry.isDirectory || entry.relativePath !in protectedRoots) {
                                        IconButton(
                                            onClick = {
                                                if (fileManager.deleteEntry(entry.relativePath)) {
                                                    if (selectedEntryPath == entry.relativePath) selectedEntryPath = null
                                                    refreshTick++
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.deletePage, tint = Color(0xFFFF6B6B), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Status Bar
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                Text(
                    text = "${filteredEntries.size} items • ${strings.winDoubleTapHint}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            }

            if (useSidebar) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier
                            .width(108.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                            .verticalScroll(rememberScrollState())
                            .padding(6.dp)
                    ) {
                        QuickPlacesStrip(vertical = true)
                    }
                    FilesContentPane(modifier = Modifier.weight(1f))
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        QuickPlacesStrip(vertical = false)
                    }
                    FilesContentPane(modifier = Modifier.weight(1f))
                }
            }
        }
    }

    if (showNewFolderDialog) {
        AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text(strings.winNewFolder) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    singleLine = true,
                    placeholder = { Text("New Folder") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fileManager.createFolder(currentPath, newFolderName.ifBlank { "New Folder" })) {
                            refreshTick++
                        }
                        newFolderName = ""
                        showNewFolderDialog = false
                    }
                ) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewFolderDialog = false }) { Text(strings.cancel) }
            }
        )
    }
}

private fun iconForFileEntry(entry: PcFileEntry): ImageVector = when {
    entry.isDirectory -> Icons.Rounded.Folder
    entry.extension in setOf("exe", "msi", "bat", "jar", "apk") -> Icons.Rounded.Terminal
    entry.extension in setOf("png", "jpg", "jpeg", "webp", "bmp") -> Icons.Rounded.Image
    entry.extension in setOf("txt", "log", "md", "json", "xml", "cfg") -> Icons.Rounded.Description
    else -> Icons.Rounded.InsertDriveFile
}

private fun tintForFileEntry(entry: PcFileEntry): Color = when {
    entry.isDirectory -> Color(0xFFFACC15)
    entry.extension in setOf("exe", "msi", "bat") -> Color(0xFF38BDF8)
    entry.extension in setOf("jar", "apk") -> Color(0xFF4ADE80)
    entry.extension in setOf("png", "jpg", "jpeg", "webp", "bmp") -> Color(0xFFA78BFA)
    entry.extension in setOf("txt", "log", "md", "json", "xml", "cfg") -> Win11Sky
    else -> Color(0xFF94A3B8)
}

@Composable
fun ExeRunnerBody(
    exeShortcuts: List<PcExeShortcut>,
    fileManager: PcFileSystemManager,
    wineBridges: List<String>,
    logs: List<String>,
    strings: AppStrings,
    onPickExeFile: () -> Unit,
    onRunExeShortcut: (PcExeShortcut) -> Unit,
    onRemoveExeShortcut: (String) -> Unit,
    onOpenDownloadedExe: (PcFileEntry) -> Unit,
    onOpenBuiltInExe: (Win11WindowKind) -> Unit
) {
    val downloadedExes = remember(exeShortcuts, logs) {
        fileManager.listDownloadFiles().filter { !it.isDirectory && it.extension in setOf("exe", "msi", "bat", "jar", "apk") }
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize().testTag("win11_exe_runner_body")
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Win11Blue.copy(alpha = 0.14f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(strings.winWineBridgeNote, style = MaterialTheme.typography.bodySmall)
                    if (wineBridges.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Wine/Bridge: ${wineBridges.joinToString()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4ADE80)
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = onPickExeFile,
                colors = ButtonDefaults.buttonColors(containerColor = Win11Blue, contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("win11_pick_exe_button")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.winPickExe)
            }
        }

        // Built-in ready-to-launch Windows .EXE programs
        item {
            Text(strings.winExeSuiteTitle, style = MaterialTheme.typography.labelLarge, color = Win11Sky)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { onOpenBuiltInExe(Win11WindowKind.CHROME_PC) }, modifier = Modifier.weight(1f)) {
                    Text("Chrome.exe", maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 10.5.sp)
                }
                OutlinedButton(onClick = { onOpenBuiltInExe(Win11WindowKind.NOTEPAD_EXE) }, modifier = Modifier.weight(1f)) {
                    Text("Notepad.exe", maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 10.5.sp)
                }
                OutlinedButton(onClick = { onOpenBuiltInExe(Win11WindowKind.CALCULATOR_EXE) }, modifier = Modifier.weight(1f)) {
                    Text("Calc.exe", maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 10.5.sp)
                }
                OutlinedButton(onClick = { onOpenBuiltInExe(Win11WindowKind.CMD_EXE) }, modifier = Modifier.weight(1f)) {
                    Text("CMD.exe", maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 10.5.sp)
                }
            }
        }

        if (downloadedExes.isNotEmpty()) {
            item {
                Text("${strings.winDownloads} (.EXE)", style = MaterialTheme.typography.labelLarge, color = Color(0xFF4ADE80))
            }
            items(downloadedExes, key = { it.relativePath }) { dlExe ->
                Surface(
                    onClick = { onOpenDownloadedExe(dlExe) },
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = Color(0xFF4ADE80))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(dlExe.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(formatFileSize(dlExe.sizeBytes), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(
                            onClick = { onOpenDownloadedExe(dlExe) },
                            modifier = Modifier.testTag("run_dl_exe_${dlExe.name}")
                        ) {
                            Text(strings.winRunExe, color = Win11Sky)
                        }
                    }
                }
            }
        }

        if (exeShortcuts.isEmpty() && downloadedExes.isEmpty()) {
            item {
                Text(
                    text = strings.winNoExeHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(exeShortcuts, key = { it.id }) { exe ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Rounded.Terminal, contentDescription = null, tint = Color(0xFFFBBF24))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(exe.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                text = if (exe.sizeBytes > 0L) "${exe.sizeBytes / 1024} KB" else "Windows Package",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(
                            onClick = { onRunExeShortcut(exe) },
                            modifier = Modifier.testTag("run_shortcut_exe_${exe.name}")
                        ) {
                            Text(strings.winRunExe, color = Win11Sky)
                        }
                        IconButton(onClick = { onRemoveExeShortcut(exe.id) }) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = strings.deletePage, tint = Color(0xFFFF6B6B))
                        }
                    }
                }
            }
        }

        if (logs.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(strings.winRuntimeConsole, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0A0D14),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        logs.take(8).forEach { line ->
                            Text(
                                text = line,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                color = Color(0xFF7DD3FC)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Win11SettingsBody(
    state: LauncherUiState,
    strings: AppStrings,
    effectiveDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onOpenThemes: () -> Unit,
    onReturnToPhoneLauncher: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(strings.winSystemSpecs, style = MaterialTheme.typography.titleMedium)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Windows 11 PC Edition (N Launcher Shell)", fontWeight = FontWeight.Bold)
                Text("Device: ${Build.MANUFACTURER} ${Build.MODEL}", style = MaterialTheme.typography.bodySmall)
                Text("Android Core: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})", style = MaterialTheme.typography.bodySmall)
                Text("RAM: ${state.device.totalRamMb} MB • Battery: ${state.battery.level}%", style = MaterialTheme.typography.bodySmall)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onToggleTheme, modifier = Modifier.weight(1f)) {
                Icon(if (effectiveDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.themeModeName(if (effectiveDarkTheme) ThemeMode.LIGHT else ThemeMode.DARK))
            }
            OutlinedButton(onClick = onOpenThemes, modifier = Modifier.weight(1f)) {
                Icon(Icons.Rounded.Palette, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.winThemes)
            }
        }
        Button(
            onClick = onReturnToPhoneLauncher,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.PhoneAndroid, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("${strings.returnLauncherLabel} — ${strings.winSwitchToMobile}")
        }
    }
}
