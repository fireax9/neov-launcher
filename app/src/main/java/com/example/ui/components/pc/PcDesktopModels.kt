package com.example.ui.components.pc

enum class Win11WindowKind {
    CHROME_PC,
    THIS_PC,
    EXE_RUNNER,
    WEB_APP_RUNNER,
    WIN_SETTINGS,
    NOTEPAD_EXE,
    CALCULATOR_EXE,
    CMD_EXE,
    TASK_MANAGER_EXE,
    PHOTO_VIEWER_EXE
}

data class WebAppLaunchReport(
    val appName: String,
    val uriString: String,
    val filePath: String? = null,
    val sizeBytes: Long = 0L,
    val extension: String = "exe",
    val headerInspection: String = "",
    val dispatchedExternally: Boolean = false,
    val externalRuntimeName: String? = null,
    val installedBridges: List<String> = emptyList(),
    val statusHeadline: String = "",
    val explanation: String = "",
    val traceLines: List<String> = emptyList()
)

data class PcFloatingWindowState(
    val instanceId: String,
    val kind: Win11WindowKind,
    val title: String,
    val initialUrl: String = "chrome://newtab",
    val fileToOpenPath: String? = null,
    val initialExplorerPath: String = "",
    val launchReport: WebAppLaunchReport? = null,
    val offsetXDp: Float = 44f,
    val offsetYDp: Float = 24f,
    val widthDp: Float = 470f,
    val heightDp: Float = 310f,
    val isMinimized: Boolean = false,
    val isMaximized: Boolean = false
)

data class ChromeTabState(
    val id: String,
    val title: String,
    val url: String
)

enum class PcDownloadStatus {
    DOWNLOADING,
    COMPLETED,
    FAILED
}

data class PcDownloadItem(
    val id: String,
    val fileName: String,
    val url: String,
    val savedRelativePath: String = "",
    val savedAbsolutePath: String = "",
    val progressPercent: Int = 0,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = -1L,
    val status: PcDownloadStatus = PcDownloadStatus.DOWNLOADING,
    val errorMessage: String? = null
)
