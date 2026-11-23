package com.example.ui.components.pc

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.PcFileEntry
import com.example.data.PcFileSystemManager
import com.example.ui.strings.AppStrings
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.viewmodel.LauncherUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.UUID

private val ChromeBlue = Color(0xFF1A73E8)
private val Win11Blue = Color(0xFF0078D4)
private val Win11Sky = Color(0xFF60CDFF)

private const val DESKTOP_CHROME_UA =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChromePcWindowBody(
    initialUrl: String,
    strings: AppStrings,
    effectiveDarkTheme: Boolean,
    fileManager: PcFileSystemManager,
    downloads: MutableList<PcDownloadItem>,
    onOpenDownloadsFolder: () -> Unit,
    onOpenDownloadedFile: (PcFileEntry) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tabs = remember {
        mutableStateListOf(
            ChromeTabState(
                id = "tab_1",
                title = strings.winChromeNewTab,
                url = initialUrl.ifBlank { "chrome://newtab" }
            )
        )
    }
    var activeTabIndex by remember { mutableIntStateOf(0) }
    val activeTab = tabs.getOrElse(activeTabIndex) { tabs.first() }
    var urlInput by remember(activeTab.id, activeTab.url) {
        mutableStateOf(if (activeTab.url == "chrome://newtab") "" else activeTab.url)
    }
    var pageProgress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(false) }
    var showDownloadsPanel by remember { mutableStateOf(false) }
    var directDownloadUrl by remember { mutableStateOf("") }

    fun startPcDownload(
        targetUrl: String,
        userAgent: String = DESKTOP_CHROME_UA,
        contentDisposition: String? = null,
        mimeType: String? = null
    ) {
        val cleanUrl = targetUrl.trim()
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) return
        val downloadId = "dl_${UUID.randomUUID().toString().take(8)}"
        val guessedName = runCatching {
            URLUtil.guessFileName(cleanUrl, contentDisposition, mimeType)
        }.getOrDefault("download_${System.currentTimeMillis()}.bin")

        downloads.add(
            0,
            PcDownloadItem(
                id = downloadId,
                fileName = guessedName,
                url = cleanUrl,
                progressPercent = 2,
                status = PcDownloadStatus.DOWNLOADING
            )
        )
        showDownloadsPanel = true

        scope.launch {
            val result = fileManager.downloadHttpFile(
                urlString = cleanUrl,
                userAgent = userAgent,
                contentDisposition = contentDisposition,
                mimeType = mimeType,
                onProgress = { pct, downloadedBytes, totalBytes ->
                    scope.launch(Dispatchers.Main) {
                        val idx = downloads.indexOfFirst { it.id == downloadId }
                        if (idx >= 0) {
                            downloads[idx] = downloads[idx].copy(
                                progressPercent = pct,
                                downloadedBytes = downloadedBytes,
                                totalBytes = totalBytes
                            )
                        }
                    }
                }
            )
            withContext(Dispatchers.Main) {
                val idx = downloads.indexOfFirst { it.id == downloadId }
                if (idx >= 0) {
                    result.onSuccess { entry ->
                        downloads[idx] = downloads[idx].copy(
                            fileName = entry.name,
                            savedRelativePath = entry.relativePath,
                            savedAbsolutePath = entry.absolutePath,
                            progressPercent = 100,
                            downloadedBytes = entry.sizeBytes,
                            totalBytes = entry.sizeBytes,
                            status = PcDownloadStatus.COMPLETED
                        )
                    }.onFailure { err ->
                        downloads[idx] = downloads[idx].copy(
                            status = PcDownloadStatus.FAILED,
                            errorMessage = err.message ?: "Download error"
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    val webView = remember {
        runCatching {
            WebView(context).apply {
                CookieManager.getInstance().setAcceptCookie(true)
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    userAgentString = DESKTOP_CHROME_UA
                    mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                }
                setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
                    startPcDownload(
                        targetUrl = url,
                        userAgent = userAgent.ifBlank { DESKTOP_CHROME_UA },
                        contentDisposition = contentDisposition,
                        mimeType = mimeType
                    )
                }
                setOnLongClickListener { view ->
                    val hit = (view as? WebView)?.hitTestResult
                    val extra = hit?.extra
                    if (!extra.isNullOrBlank() && (extra.startsWith("http://") || extra.startsWith("https://"))) {
                        if (hit.type == WebView.HitTestResult.IMAGE_TYPE ||
                            hit.type == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE ||
                            hit.type == WebView.HitTestResult.SRC_ANCHOR_TYPE
                        ) {
                            startPcDownload(extra)
                            return@setOnLongClickListener true
                        }
                    }
                    false
                }
            }
        }.getOrNull()
    }

    DisposableEffect(webView) {
        onDispose {
            runCatching { webView?.destroy() }
        }
    }

    fun navigateActiveTab(rawInput: String) {
        val trimmed = rawInput.trim()
        val resolved = when {
            trimmed.isEmpty() || trimmed == "chrome://newtab" -> "chrome://newtab"
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.contains(".") && !trimmed.contains(" ") -> "https://$trimmed"
            else -> "https://www.google.com/search?q=${java.net.URLEncoder.encode(trimmed, "UTF-8")}"
        }
        val idx = activeTabIndex.coerceIn(0, tabs.lastIndex)
        tabs[idx] = tabs[idx].copy(
            url = resolved,
            title = if (resolved == "chrome://newtab") strings.winChromeNewTab else resolved.removePrefix("https://").removePrefix("http://").substringBefore("/")
        )
        urlInput = if (resolved == "chrome://newtab") "" else resolved
        if (resolved != "chrome://newtab") {
            runCatching { webView?.loadUrl(resolved) }
        }
    }

    val tabBarBg = if (effectiveDarkTheme) Color(0xFF1F2229) else Color(0xFFDEE3EC)
    val toolbarBg = if (effectiveDarkTheme) Color(0xFF2B2F3A) else Color(0xFFF6F8FC)
    val omniboxBg = if (effectiveDarkTheme) Color(0xFF1B1E25) else Color.White
    val activeDownloadCount = downloads.count { it.status == PcDownloadStatus.DOWNLOADING }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("win11_chrome_pc_body")
    ) {
        // Desktop Chrome Tab Strip
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(tabBarBg)
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            tabs.forEachIndexed { idx, tab ->
                val selected = idx == activeTabIndex
                Surface(
                    onClick = {
                        activeTabIndex = idx
                        if (tab.url != "chrome://newtab") {
                            runCatching {
                                if (webView?.url != tab.url) webView?.loadUrl(tab.url)
                            }
                        }
                    },
                    shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 6.dp, bottomEnd = 6.dp),
                    color = if (selected) toolbarBg else Color.Transparent,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Public,
                            contentDescription = null,
                            tint = if (selected) ChromeBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(86.dp)
                        )
                        if (tabs.size > 1) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = strings.winClose,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        tabs.removeAt(idx)
                                        if (activeTabIndex >= tabs.size) activeTabIndex = tabs.lastIndex
                                    }
                                    .padding(1.dp)
                            )
                        }
                    }
                }
            }
            IconButton(
                onClick = {
                    val newTab = ChromeTabState(
                        id = "tab_${UUID.randomUUID().toString().take(6)}",
                        title = strings.winChromeNewTab,
                        url = "chrome://newtab"
                    )
                    tabs.add(newTab)
                    activeTabIndex = tabs.lastIndex
                },
                modifier = Modifier
                    .size(28.dp)
                    .testTag("chrome_pc_add_tab")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = strings.winChromeNewTab, modifier = Modifier.size(16.dp))
            }
        }

        // Desktop Chrome Address & Navigation Toolbar + Downloads Toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(toolbarBg)
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            IconButton(
                onClick = {
                    if (webView?.canGoBack() == true) webView.goBack()
                    else navigateActiveTab("chrome://newtab")
                },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            IconButton(
                onClick = { if (webView?.canGoForward() == true) webView.goForward() },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            IconButton(
                onClick = {
                    if (activeTab.url == "chrome://newtab") navigateActiveTab("chrome://newtab")
                    else runCatching { webView?.reload() }
                },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            IconButton(
                onClick = { navigateActiveTab("chrome://newtab") },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(Icons.Rounded.Home, contentDescription = null, modifier = Modifier.size(16.dp))
            }

            // Omnibox
            Surface(
                shape = RoundedCornerShape(50),
                color = omniboxBg,
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(50))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp)
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = ChromeBlue, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (urlInput.isEmpty()) {
                            Text(
                                text = strings.winChromeUrlHint,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp
                            ),
                            cursorBrush = SolidColor(ChromeBlue),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = { navigateActiveTab(urlInput) }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("chrome_pc_omnibox")
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = ChromeBlue.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = "PC",
                            color = ChromeBlue,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Chrome PC Downloads Manager Button
            Surface(
                onClick = { showDownloadsPanel = !showDownloadsPanel },
                shape = RoundedCornerShape(10.dp),
                color = if (showDownloadsPanel || activeDownloadCount > 0) ChromeBlue.copy(alpha = 0.18f) else Color.Transparent,
                modifier = Modifier
                    .height(30.dp)
                    .testTag("chrome_pc_downloads_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = strings.winDownloads,
                        tint = if (activeDownloadCount > 0 || showDownloadsPanel) ChromeBlue else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(17.dp)
                    )
                    if (downloads.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (activeDownloadCount > 0) "$activeDownloadCount" else "${downloads.size}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChromeBlue
                        )
                    }
                }
            }
        }

        if (isLoading && pageProgress > 0f && pageProgress < 1f) {
            LinearProgressIndicator(
                progress = { pageProgress },
                color = ChromeBlue,
                modifier = Modifier.fillMaxWidth().height(2.dp)
            )
        }

        // Expandable Chrome PC Downloads Drawer
        AnimatedVisibility(visible = showDownloadsPanel) {
            Surface(
                color = if (effectiveDarkTheme) Color(0xFF1D222D) else Color(0xFFF0F4FA),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 185.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    .testTag("chrome_pc_downloads_panel")
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${strings.winDownloads} (C:\\Users\\PC\\Downloads)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = ChromeBlue
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = onOpenDownloadsFolder) {
                                Icon(Icons.Rounded.FolderOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(strings.winOpenFolder, fontSize = 11.sp)
                            }
                            IconButton(onClick = { showDownloadsPanel = false }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Rounded.Close, contentDescription = strings.winClose, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    // Direct URL download bar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = directDownloadUrl,
                            onValueChange = { directDownloadUrl = it },
                            placeholder = { Text("https://... (.exe, .zip, .pdf, .txt)", fontSize = 11.sp, maxLines = 1) },
                            singleLine = true,
                            textStyle = TextStyle(fontSize = 11.sp),
                            modifier = Modifier.weight(1f).height(44.dp)
                        )
                        Button(
                            onClick = {
                                val u = directDownloadUrl.trim().ifBlank {
                                    activeTab.url.takeIf { it.startsWith("http") } ?: ""
                                }
                                if (u.isNotBlank()) {
                                    startPcDownload(u)
                                    directDownloadUrl = ""
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ChromeBlue, contentColor = Color.White),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }

                    if (downloads.isEmpty()) {
                        Text(
                            text = strings.winNoDownloads,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(max = 90.dp)
                        ) {
                            items(downloads, key = { it.id }) { item ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (item.status) {
                                                PcDownloadStatus.DOWNLOADING -> Icons.Rounded.Download
                                                PcDownloadStatus.COMPLETED -> Icons.Rounded.CheckCircle
                                                PcDownloadStatus.FAILED -> Icons.Rounded.ErrorOutline
                                            },
                                            contentDescription = null,
                                            tint = when (item.status) {
                                                PcDownloadStatus.DOWNLOADING -> ChromeBlue
                                                PcDownloadStatus.COMPLETED -> Color(0xFF4ADE80)
                                                PcDownloadStatus.FAILED -> Color(0xFFFF6B6B)
                                            },
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(item.fileName, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            when (item.status) {
                                                PcDownloadStatus.DOWNLOADING -> {
                                                    LinearProgressIndicator(
                                                        progress = { item.progressPercent / 100f },
                                                        color = ChromeBlue,
                                                        modifier = Modifier.fillMaxWidth().height(3.dp)
                                                    )
                                                }
                                                PcDownloadStatus.COMPLETED -> {
                                                    Text(
                                                        text = "${strings.winDownloadComplete} • ${formatFileSize(item.downloadedBytes)}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF4ADE80)
                                                    )
                                                }
                                                PcDownloadStatus.FAILED -> {
                                                    Text(
                                                        text = "${strings.winDownloadFailed}: ${item.errorMessage.orEmpty()}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontSize = 10.sp,
                                                        color = Color(0xFFFF6B6B),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                        if (item.status == PcDownloadStatus.COMPLETED && item.savedAbsolutePath.isNotBlank()) {
                                            TextButton(
                                                onClick = {
                                                    val f = File(item.savedAbsolutePath)
                                                    if (f.exists()) {
                                                        onOpenDownloadedFile(
                                                            PcFileEntry(
                                                                name = f.name,
                                                                relativePath = item.savedRelativePath,
                                                                absolutePath = f.absolutePath,
                                                                isDirectory = false,
                                                                sizeBytes = f.length(),
                                                                lastModifiedMillis = f.lastModified(),
                                                                extension = f.extension.lowercase()
                                                            )
                                                        )
                                                    } else {
                                                        onOpenDownloadsFolder()
                                                    }
                                                },
                                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                            ) {
                                                Text(strings.winOpenSelected, fontSize = 10.sp, color = ChromeBlue)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Content Area: Chrome PC Speed Dial / New Tab OR Live Desktop WebView
        if (activeTab.url == "chrome://newtab" || webView == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (effectiveDarkTheme) Color(0xFF171920) else Color(0xFFFDFEFF))
                    .padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(ChromeBlue.copy(alpha = 0.14f))
                        ) {
                            Icon(Icons.Rounded.Language, contentDescription = null, tint = ChromeBlue, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Google Chrome • PC Edition",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = strings.winChromeDesktopUa,
                                style = MaterialTheme.typography.labelSmall,
                                color = ChromeBlue
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "Google" to "https://www.google.com",
                            "YouTube" to "https://www.youtube.com",
                            "GitHub" to "https://github.com",
                            "Wikipedia" to "https://www.wikipedia.org"
                        ).forEach { (name, targetUrl) ->
                            Surface(
                                onClick = { navigateActiveTab(targetUrl) },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Rounded.Public, contentDescription = null, tint = ChromeBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(name, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            AndroidView(
                factory = {
                    webView.apply {
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                                url?.let { loaded ->
                                    urlInput = loaded
                                    val idx = activeTabIndex.coerceIn(0, tabs.lastIndex)
                                    tabs[idx] = tabs[idx].copy(url = loaded)
                                }
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                val idx = activeTabIndex.coerceIn(0, tabs.lastIndex)
                                val pageTitle = view?.title?.takeIf { it.isNotBlank() }
                                    ?: url?.removePrefix("https://")?.substringBefore("/")
                                    ?: tabs[idx].title
                                tabs[idx] = tabs[idx].copy(title = pageTitle)
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                pageProgress = newProgress / 100f
                            }
                        }
                        if (url != activeTab.url) {
                            loadUrl(activeTab.url)
                        }
                    }
                },
                update = { view ->
                    if (activeTab.url != "chrome://newtab" && view.url != activeTab.url && !isLoading) {
                        view.loadUrl(activeTab.url)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun NotepadExeBody(
    strings: AppStrings,
    fileToOpenPath: String? = null,
    fileManager: PcFileSystemManager? = null
) {
    var content by remember(fileToOpenPath) {
        mutableStateOf(
            "@echo off\r\nREM Windows 11 N Launcher Shell Script\r\necho Welcome to PC Mode Desktop!\r\n"
        )
    }
    var savedBanner by remember { mutableStateOf(false) }

    LaunchedEffect(fileToOpenPath) {
        if (!fileToOpenPath.isNullOrBlank() && fileManager != null) {
            content = withContext(Dispatchers.IO) { fileManager.readTextFile(fileToOpenPath) }
        }
    }

    val linesCount = content.lines().size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("win11_notepad_body")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { content = "" }) {
                    Text("New", fontSize = 12.sp)
                }
                TextButton(onClick = {
                    content = "winver\nsysteminfo\nstart chrome.exe\n"
                }) {
                    Text("Sample .bat", fontSize = 12.sp)
                }
                if (!fileToOpenPath.isNullOrBlank() && fileManager != null) {
                    TextButton(onClick = {
                        savedBanner = fileManager.writeTextFile(fileToOpenPath, content)
                    }) {
                        Icon(Icons.Rounded.Save, contentDescription = strings.save, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (savedBanner) "Saved" else strings.save, fontSize = 12.sp)
                    }
                }
            }
            Text(
                text = "Ln $linesCount • ${content.length} chars",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextField(
            value = content,
            onValueChange = {
                content = it
                savedBanner = false
            },
            placeholder = { Text(strings.winNotepadHint) },
            textStyle = TextStyle(
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun PhotoViewerExeBody(
    filePath: String?,
    strings: AppStrings
) {
    val bitmap = remember(filePath) {
        if (filePath.isNullOrBlank()) null
        else runCatching { BitmapFactory.decodeFile(filePath)?.asImageBitmap() }.getOrNull()
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D111A))
            .padding(12.dp)
            .testTag("win11_photo_viewer_body")
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = File(filePath.orEmpty()).name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = File(filePath.orEmpty()).name,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        } else {
            Icon(Icons.Rounded.Image, contentDescription = null, tint = Win11Sky, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(strings.winPhotoViewer, color = Color.White)
        }
    }
}

@Composable
fun CalculatorExeBody() {
    var display by remember { mutableStateOf("0") }
    var previousVal by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var resetOnNextDigit by remember { mutableStateOf(false) }

    fun formatResult(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        return if (value % 1.0 == 0.0 && kotlin.math.abs(value) < 1e12) {
            value.toLong().toString()
        } else {
            String.format(Locale.ENGLISH, "%.6f", value).trimEnd('0').trimEnd('.')
        }
    }

    fun evaluate(a: Double, b: Double, op: String): Double = when (op) {
        "+" -> a + b
        "−" -> a - b
        "×" -> a * b
        "÷" -> if (b != 0.0) a / b else Double.NaN
        else -> b
    }

    fun onKey(key: String) {
        when (key) {
            "C" -> {
                display = "0"
                previousVal = null
                pendingOp = null
                resetOnNextDigit = false
            }
            "⌫" -> {
                display = if (display == "Error" || display.length <= 1) "0" else display.dropLast(1)
            }
            "+", "−", "×", "÷" -> {
                val cur = display.toDoubleOrNull() ?: 0.0
                if (previousVal != null && pendingOp != null && !resetOnNextDigit) {
                    val res = evaluate(previousVal!!, cur, pendingOp!!)
                    if (res.isNaN() || res.isInfinite()) {
                        display = "Error"
                        previousVal = null
                        pendingOp = null
                        resetOnNextDigit = true
                        return
                    }
                    previousVal = res
                    display = formatResult(res)
                } else {
                    previousVal = cur
                }
                pendingOp = key
                resetOnNextDigit = true
            }
            "=" -> {
                val cur = display.toDoubleOrNull() ?: 0.0
                if (previousVal != null && pendingOp != null) {
                    val res = evaluate(previousVal!!, cur, pendingOp!!)
                    display = formatResult(res)
                    previousVal = null
                    pendingOp = null
                    resetOnNextDigit = true
                }
            }
            "%" -> {
                val cur = display.toDoubleOrNull() ?: 0.0
                display = formatResult(cur / 100.0)
                resetOnNextDigit = true
            }
            "." -> {
                if (resetOnNextDigit || display == "Error") {
                    display = "0."
                    resetOnNextDigit = false
                } else if (!display.contains(".")) {
                    display += "."
                }
            }
            else -> {
                display = if (resetOnNextDigit || display == "0" || display == "Error") key else display + key
                resetOnNextDigit = false
            }
        }
    }

    val rows = listOf(
        listOf("C", "⌫", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "−"),
        listOf("1", "2", "3", "+"),
        listOf("0", ".", "=")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .testTag("win11_calc_body")
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (previousVal != null && pendingOp != null) "${formatResult(previousVal!!)} $pendingOp" else "Standard",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = display,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 22.sp,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("calc_display_text")
                )
            }
        }

        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalMinimumInteractiveComponentSize provides androidx.compose.ui.unit.Dp.Unspecified
        ) {
            rows.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) {
                    row.forEach { label ->
                        val isAccent = label == "="
                        val isOp = label in listOf("÷", "×", "−", "+", "C", "⌫", "%")
                        Surface(
                            onClick = { onKey(label) },
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                isAccent -> Win11Blue
                                isOp -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            },
                            modifier = Modifier
                                .weight(if (label == "0") 2f else 1f)
                                .fillMaxSize()
                                .testTag("calc_key_$label")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = label,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    color = if (isAccent) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CmdExeBody(
    state: LauncherUiState,
    fileManager: PcFileSystemManager,
    onOpenWindowKind: (Win11WindowKind) -> Unit
) {
    var input by remember { mutableStateOf("") }
    val history = remember {
        mutableStateListOf(
            "Microsoft Windows 11 [Version 10.0.26100.2033]",
            "(c) N Launcher PC Environment. Type 'help' for commands.",
            ""
        )
    }

    fun executeCommand(raw: String) {
        val cmd = raw.trim()
        if (cmd.isEmpty()) return
        history.add("C:\\Users\\PC> $cmd")
        when (cmd.lowercase()) {
            "help" -> {
                history.add("Commands: help, ver, dir, downloads, systeminfo, cls, start chrome.exe, start explorer.exe, start notepad.exe, start calc.exe, start taskmgr.exe")
            }
            "ver" -> {
                history.add("Windows 11 Pro 24H2 (N Launcher PC Kernel)")
            }
            "systeminfo" -> {
                history.add("Host: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                history.add("Memory: ${state.device.usedRamMb} / ${state.device.totalRamMb} MB")
                history.add("Disk C: ${String.format(Locale.ENGLISH, "%.1f / %.1f GB free", state.device.freeStorageGb, state.device.totalStorageGb)}")
                history.add("Battery: ${state.battery.level}%")
            }
            "dir" -> {
                history.add(" Directory of C:\\Users\\PC")
                fileManager.listEntries("").forEach { entry ->
                    val prefix = if (entry.isDirectory) "<DIR>" else "${entry.sizeBytes / 1024} KB"
                    history.add("  ${prefix.padEnd(9)} ${entry.name}")
                }
            }
            "downloads" -> {
                val dlFiles = fileManager.listDownloadFiles()
                if (dlFiles.isEmpty()) {
                    history.add("  C:\\Users\\PC\\Downloads is empty.")
                } else {
                    dlFiles.forEach { f -> history.add("  ${f.name} (${formatFileSize(f.sizeBytes)})") }
                }
            }
            "cls" -> history.clear()
            "start chrome.exe", "chrome.exe", "chrome" -> {
                onOpenWindowKind(Win11WindowKind.CHROME_PC)
                history.add("Started Google Chrome (PC Desktop Mode).")
            }
            "start explorer.exe", "explorer.exe", "explorer" -> {
                onOpenWindowKind(Win11WindowKind.THIS_PC)
                history.add("Started Windows 11 File Explorer.")
            }
            "start notepad.exe", "notepad.exe", "notepad" -> {
                onOpenWindowKind(Win11WindowKind.NOTEPAD_EXE)
                history.add("Started Notepad.exe.")
            }
            "start calc.exe", "calc.exe", "calc" -> {
                onOpenWindowKind(Win11WindowKind.CALCULATOR_EXE)
                history.add("Started Calc.exe.")
            }
            "start taskmgr.exe", "taskmgr.exe", "taskmgr" -> {
                onOpenWindowKind(Win11WindowKind.TASK_MANAGER_EXE)
                history.add("Started TaskMgr.exe.")
            }
            else -> history.add("'$cmd' executed or queued in shell. Type 'help' to see built-in commands.")
        }
        input = ""
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0F16))
            .padding(10.dp)
            .testTag("win11_cmd_body")
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            items(history) { line ->
                Text(
                    text = line,
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.5.sp,
                    color = Color(0xFFD6E4FF)
                )
            }
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.14f), modifier = Modifier.padding(vertical = 6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "C:\\Users\\PC> ",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 12.sp,
                color = Color(0xFF4ADE80)
            )
            BasicTextField(
                value = input,
                onValueChange = { input = it },
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 12.sp,
                    color = Color.White
                ),
                cursorBrush = SolidColor(Color(0xFF4ADE80)),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { executeCommand(input) }),
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { executeCommand(input) }) {
                Text("Run", color = Win11Sky, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun TaskManagerExeBody(
    openWindows: List<PcFloatingWindowState>,
    state: LauncherUiState,
    strings: AppStrings,
    onFocusWindow: (String) -> Unit,
    onEndTaskWindow: (String) -> Unit
) {
    val dev = state.device
    val ramRatio = if (dev.totalRamMb > 0) (dev.usedRamMb.toFloat() / dev.totalRamMb.toFloat()).coerceIn(0f, 1f) else 0.4f

    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize().testTag("win11_taskmgr_body")
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "RAM: ${dev.usedRamMb} / ${dev.totalRamMb} MB • Battery: ${state.battery.level}%",
                        style = MaterialTheme.typography.labelLarge
                    )
                    LinearProgressIndicator(
                        progress = { ramRatio },
                        color = Win11Sky,
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)
                    )
                }
            }
        }

        items(openWindows, key = { it.instanceId }) { win ->
            Surface(
                onClick = { onFocusWindow(win.instanceId) },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Rounded.Computer, contentDescription = null, tint = Win11Sky, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(win.title, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            text = if (win.isMinimized) strings.winMinimize else "Running (${win.kind.name})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { onEndTaskWindow(win.instanceId) }) {
                        Text(strings.winEndTask, color = Color(0xFFFF6B6B), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

internal fun formatFileSize(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val kb = bytes / 1024.0
    if (kb < 1024.0) return String.format(Locale.ENGLISH, "%.0f KB", kb.coerceAtLeast(1.0))
    val mb = kb / 1024.0
    return String.format(Locale.ENGLISH, "%.1f MB", mb)
}
