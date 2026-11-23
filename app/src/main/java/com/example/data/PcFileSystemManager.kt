package com.example.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder

data class PcFileEntry(
    val name: String,
    val relativePath: String,
    val absolutePath: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModifiedMillis: Long,
    val extension: String,
    val isVirtualBuiltinExe: Boolean = false
)

class PcFileSystemManager(private val context: Context) {

    private val rootDir: File by lazy {
        File(context.filesDir, "WindowsPC").also { dir ->
            if (!dir.exists()) dir.mkdirs()
        }
    }

    fun ensureInitialized() {
        listOf("Desktop", "Downloads", "Documents", "Pictures", "Program Files").forEach { sub ->
            val dir = File(rootDir, sub)
            if (!dir.exists()) dir.mkdirs()
        }
        val readme = File(rootDir, "Documents/Welcome_Windows11.txt")
        if (!readme.exists()) {
            runCatching {
                readme.writeText(
                    "Windows 11 PC Workspace (N Launcher)\n" +
                        "====================================\n\n" +
                        "• Downloads saved from Chrome PC appear in C:\\Users\\PC\\Downloads.\n" +
                        "• Double-tap any folder or file (.exe, .txt, images) to open it.\n" +
                        "• Built-in executables are located in C:\\Program Files.\n"
                )
            }
        }
        val progDir = File(rootDir, "Program Files")
        listOf(
            "chrome.exe" to "Built-in Google Chrome PC Browser Executable",
            "explorer.exe" to "Built-in Windows 11 File Explorer",
            "notepad.exe" to "Built-in Notepad Text & Script Editor",
            "calc.exe" to "Built-in Windows Calculator",
            "cmd.exe" to "Built-in Windows Command Prompt",
            "taskmgr.exe" to "Built-in Windows Task Manager"
        ).forEach { (exeName, desc) ->
            val f = File(progDir, exeName)
            if (!f.exists()) {
                runCatching { f.writeText("MZ\u0000NLAUNCHER_BUILTIN_EXE:$desc") }
            }
        }
    }

    fun listEntries(relativePath: String): List<PcFileEntry> {
        ensureInitialized()
        val cleanRel = relativePath.trim().trim('/')
        val target = if (cleanRel.isEmpty()) rootDir else File(rootDir, cleanRel)
        if (!target.exists() || !target.isDirectory) return emptyList()
        // Prevent path traversal outside WindowsPC root
        if (!target.canonicalPath.startsWith(rootDir.canonicalPath)) return emptyList()

        val children = target.listFiles()?.toList().orEmpty()
        return children
            .sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name.lowercase() })
            .map { file ->
                val rel = file.canonicalPath
                    .removePrefix(rootDir.canonicalPath)
                    .trimStart(File.separatorChar)
                    .replace(File.separatorChar, '/')
                val ext = if (file.isDirectory) "" else file.extension.lowercase()
                val isBuiltin = cleanRel == "Program Files" && ext == "exe" &&
                    file.name.lowercase() in BUILTIN_EXE_NAMES
                PcFileEntry(
                    name = file.name,
                    relativePath = rel,
                    absolutePath = file.absolutePath,
                    isDirectory = file.isDirectory,
                    sizeBytes = if (file.isDirectory) 0L else file.length(),
                    lastModifiedMillis = file.lastModified(),
                    extension = ext,
                    isVirtualBuiltinExe = isBuiltin
                )
            }
    }

    fun listDownloadFiles(): List<PcFileEntry> = listEntries("Downloads")

    fun createFolder(parentRelativePath: String, folderName: String): Boolean {
        ensureInitialized()
        val safeName = folderName.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_")
        if (safeName.isEmpty()) return false
        val parent = if (parentRelativePath.isBlank()) rootDir else File(rootDir, parentRelativePath.trim('/'))
        if (!parent.canonicalPath.startsWith(rootDir.canonicalPath)) return false
        val newDir = File(parent, safeName)
        return newDir.mkdirs() || newDir.exists()
    }

    fun deleteEntry(relativePath: String): Boolean {
        val clean = relativePath.trim().trim('/')
        if (clean.isEmpty() || clean in PROTECTED_FOLDERS) return false
        val target = File(rootDir, clean)
        if (!target.canonicalPath.startsWith(rootDir.canonicalPath)) return false
        return if (target.isDirectory) target.deleteRecursively() else target.delete()
    }

    fun readTextFile(absolutePath: String): String {
        val f = File(absolutePath)
        if (!f.exists() || !f.canonicalPath.startsWith(rootDir.canonicalPath)) return ""
        return runCatching { f.readText().take(65536) }.getOrDefault("")
    }

    fun writeTextFile(absolutePath: String, content: String): Boolean {
        val f = File(absolutePath)
        if (!f.canonicalPath.startsWith(rootDir.canonicalPath)) return false
        return runCatching {
            f.writeText(content)
            true
        }.getOrDefault(false)
    }

    suspend fun importDocumentUri(uri: Uri, targetFolder: String = "Downloads"): PcFileEntry? =
        withContext(Dispatchers.IO) {
            ensureInitialized()
            val dir = File(rootDir, targetFolder.trim('/')).also { if (!it.exists()) it.mkdirs() }
            val rawName = queryDisplayName(uri) ?: "imported_file_${System.currentTimeMillis()}"
            val dest = uniqueFile(dir, sanitizeFileName(rawName))
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(dest).use { out ->
                        input.copyTo(out)
                    }
                } ?: return@withContext null
                val rel = dest.canonicalPath
                    .removePrefix(rootDir.canonicalPath)
                    .trimStart(File.separatorChar)
                    .replace(File.separatorChar, '/')
                PcFileEntry(
                    name = dest.name,
                    relativePath = rel,
                    absolutePath = dest.absolutePath,
                    isDirectory = false,
                    sizeBytes = dest.length(),
                    lastModifiedMillis = dest.lastModified(),
                    extension = dest.extension.lowercase()
                )
            }.getOrNull()
        }

    suspend fun downloadHttpFile(
        urlString: String,
        userAgent: String,
        contentDisposition: String?,
        mimeType: String?,
        onProgress: (percent: Int, downloadedBytes: Long, totalBytes: Long) -> Unit
    ): Result<PcFileEntry> = withContext(Dispatchers.IO) {
        ensureInitialized()
        runCatching {
            var currentUrl = urlString
            var connection: HttpURLConnection? = null
            var redirects = 0
            while (redirects < 6) {
                val url = URL(currentUrl)
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = false
                    connectTimeout = 15000
                    readTimeout = 30000
                    setRequestProperty("User-Agent", userAgent)
                }
                val code = conn.responseCode
                if (code in 300..399) {
                    val location = conn.getHeaderField("Location")
                    conn.disconnect()
                    if (location.isNullOrBlank()) break
                    currentUrl = URL(url, location).toString()
                    redirects++
                } else {
                    connection = conn
                    break
                }
            }

            val conn = connection ?: throw IllegalStateException("Too many redirects")
            try {
                val respCode = conn.responseCode
                if (respCode >= 400) {
                    throw IllegalStateException("HTTP $respCode")
                }
                val totalBytes = conn.contentLengthLong.coerceAtLeast(-1L)
                val headerDisposition = contentDisposition ?: conn.getHeaderField("Content-Disposition")
                val fileName = resolveDownloadFileName(currentUrl, headerDisposition, mimeType ?: conn.contentType)
                val downloadsDir = File(rootDir, "Downloads").also { if (!it.exists()) it.mkdirs() }
                val destFile = uniqueFile(downloadsDir, sanitizeFileName(fileName))

                var downloaded = 0L
                val buffer = ByteArray(16 * 1024)
                conn.inputStream.use { input ->
                    FileOutputStream(destFile).use { out ->
                        var read: Int
                        var lastReported = -1
                        while (input.read(buffer).also { read = it } != -1) {
                            out.write(buffer, 0, read)
                            downloaded += read
                            val pct = if (totalBytes > 0L) {
                                ((downloaded * 100L) / totalBytes).toInt().coerceIn(0, 100)
                            } else {
                                50
                            }
                            if (pct != lastReported) {
                                lastReported = pct
                                onProgress(pct, downloaded, totalBytes)
                            }
                        }
                    }
                }
                onProgress(100, downloaded, if (totalBytes > 0) totalBytes else downloaded)

                val rel = destFile.canonicalPath
                    .removePrefix(rootDir.canonicalPath)
                    .trimStart(File.separatorChar)
                    .replace(File.separatorChar, '/')
                PcFileEntry(
                    name = destFile.name,
                    relativePath = rel,
                    absolutePath = destFile.absolutePath,
                    isDirectory = false,
                    sizeBytes = destFile.length(),
                    lastModifiedMillis = destFile.lastModified(),
                    extension = destFile.extension.lowercase()
                )
            } finally {
                conn.disconnect()
            }
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        }.getOrNull() ?: uri.lastPathSegment?.substringAfterLast('/')
    }

    private fun resolveDownloadFileName(
        url: String,
        contentDisposition: String?,
        mimeType: String?
    ): String {
        if (!contentDisposition.isNullOrBlank()) {
            val utfMatch = Regex("filename\\*=UTF-8''([^;\\r\\n]+)", RegexOption.IGNORE_CASE).find(contentDisposition)
            if (utfMatch != null) {
                return runCatching { URLDecoder.decode(utfMatch.groupValues[1], "UTF-8") }.getOrDefault(utfMatch.groupValues[1])
            }
            val match = Regex("filename=\"?([^\";\\r\\n]+)\"?", RegexOption.IGNORE_CASE).find(contentDisposition)
            if (match != null && match.groupValues[1].isNotBlank()) {
                return match.groupValues[1].trim()
            }
        }
        val pathPart = runCatching { URL(url).path }.getOrNull().orEmpty()
        val lastSeg = pathPart.substringAfterLast('/').substringBefore('?').trim()
        if (lastSeg.isNotBlank() && lastSeg.contains('.')) {
            return runCatching { URLDecoder.decode(lastSeg, "UTF-8") }.getOrDefault(lastSeg)
        }
        val ext = when {
            mimeType?.contains("pdf") == true -> "pdf"
            mimeType?.contains("zip") == true -> "zip"
            mimeType?.contains("x-msdownload") == true || mimeType?.contains("exe") == true -> "exe"
            mimeType?.contains("png") == true -> "png"
            mimeType?.contains("jpeg") == true || mimeType?.contains("jpg") == true -> "jpg"
            mimeType?.contains("text/plain") == true -> "txt"
            mimeType?.contains("html") == true -> "html"
            else -> "bin"
        }
        val base = lastSeg.ifBlank { "download_${System.currentTimeMillis()}" }
        return if (base.contains('.')) base else "$base.$ext"
    }

    private fun sanitizeFileName(raw: String): String {
        return raw.replace(Regex("[\\\\/:*?\"<>|]"), "_").ifBlank { "file_${System.currentTimeMillis()}" }
    }

    private fun uniqueFile(dir: File, name: String): File {
        var candidate = File(dir, name)
        if (!candidate.exists()) return candidate
        val base = name.substringBeforeLast('.', name)
        val ext = name.substringAfterLast('.', "")
        var idx = 1
        while (candidate.exists()) {
            val nextName = if (ext.isNotEmpty() && base != name) "$base ($idx).$ext" else "$name ($idx)"
            candidate = File(dir, nextName)
            idx++
        }
        return candidate
    }

    companion object {
        private val PROTECTED_FOLDERS = setOf("Desktop", "Downloads", "Documents", "Pictures", "Program Files")
        private val BUILTIN_EXE_NAMES = setOf(
            "chrome.exe", "explorer.exe", "notepad.exe", "calc.exe", "cmd.exe", "taskmgr.exe"
        )
    }
}
