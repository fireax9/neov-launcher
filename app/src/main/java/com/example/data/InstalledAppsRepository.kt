package com.example.data

import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.R
import com.example.model.AppCategory
import com.example.model.InstalledApp
import com.example.model.THEMES_APP_PACKAGE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class InstalledAppsRepository(private val context: Context) {

    fun observeInstalledApps(): Flow<List<InstalledApp>> = callbackFlow {
        val emitApps = {
            trySend(loadInstalledAppsSync())
        }

        emitApps()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                emitApps()
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(receiver, filter)
        }

        awaitClose {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun refreshInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        loadInstalledAppsSync()
    }

    private fun loadInstalledAppsSync(): List<InstalledApp> {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(mainIntent, 0)
        }

        val systemApps = resolveInfos
            .asSequence()
            .filter { it.activityInfo?.packageName != null && it.activityInfo.packageName != context.packageName }
            .distinctBy { it.activityInfo.packageName }
            .map { info ->
                val pkg = info.activityInfo.packageName
                val act = info.activityInfo.name
                val label = info.loadLabel(pm)?.toString()?.trim().orEmpty().ifEmpty { pkg }
                val appInfo = info.activityInfo.applicationInfo
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val category = classifyApp(pkg, label, appInfo)
                val rawDrawable = runCatching { info.loadIcon(pm) }.getOrNull()
                val iconBitmap = rawDrawable?.let {
                    runCatching { drawableToImageBitmap(it, ICON_RENDER_PX) }.getOrNull()
                }
                val foregroundBitmap = rawDrawable?.let {
                    runCatching { extractForegroundImageBitmap(it, ICON_RENDER_PX) }.getOrNull()
                } ?: iconBitmap

                InstalledApp(
                    packageName = pkg,
                    activityName = act,
                    label = label,
                    category = category,
                    isSystemApp = isSystem,
                    icon = iconBitmap,
                    foregroundIcon = foregroundBitmap
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()

        return listOf(createBuiltInThemesApp()) + systemApps
    }

    private fun createBuiltInThemesApp(): InstalledApp {
        val fullIcon = renderThemesAppBitmap(includeBackground = true, sizePx = ICON_RENDER_PX)
        val fgIcon = renderThemesAppBitmap(includeBackground = false, sizePx = ICON_RENDER_PX)
        return InstalledApp(
            packageName = THEMES_APP_PACKAGE,
            activityName = "",
            label = context.getString(R.string.themes_app_label),
            category = AppCategory.UTILITIES,
            isSystemApp = true,
            icon = fullIcon,
            foregroundIcon = fgIcon,
            isInternalApp = true
        )
    }

    private fun renderThemesAppBitmap(includeBackground: Boolean, sizePx: Int): ImageBitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val s = sizePx.toFloat()

        if (includeBackground) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, s, s,
                    intArrayOf(
                        Color.rgb(22, 24, 42),
                        Color.rgb(59, 44, 133),
                        Color.rgb(255, 77, 45)
                    ),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, s, s, bgPaint)
        }

        // Sleek 4-tile theme studio grid + central sparkle glyph
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.WHITE
        }
        val tileGap = s * 0.08f
        val tileSize = s * 0.24f
        val left = (s - (tileSize * 2 + tileGap)) / 2f
        val top = (s - (tileSize * 2 + tileGap)) / 2f
        val r = s * 0.075f

        val colors = if (includeBackground) {
            intArrayOf(
                Color.argb(245, 255, 255, 255),
                Color.argb(235, 56, 189, 248),
                Color.argb(235, 255, 184, 0),
                Color.argb(245, 255, 92, 124)
            )
        } else {
            intArrayOf(
                Color.argb(255, 255, 255, 255),
                Color.argb(215, 255, 255, 255),
                Color.argb(195, 255, 255, 255),
                Color.argb(245, 255, 255, 255)
            )
        }

        // Top-left rounded square
        strokePaint.color = colors[0]
        canvas.drawRoundRect(RectF(left, top, left + tileSize, top + tileSize), r, r, strokePaint)
        // Top-right circle
        strokePaint.color = colors[1]
        canvas.drawCircle(left + tileSize + tileGap + tileSize / 2f, top + tileSize / 2f, tileSize / 2f, strokePaint)
        // Bottom-left pill
        strokePaint.color = colors[2]
        canvas.drawRoundRect(
            RectF(left, top + tileSize + tileGap, left + tileSize, top + tileSize * 2 + tileGap),
            tileSize / 2f,
            tileSize / 2f,
            strokePaint
        )
        // Bottom-right sparkle star
        strokePaint.color = colors[3]
        val cx = left + tileSize + tileGap + tileSize / 2f
        val cy = top + tileSize + tileGap + tileSize / 2f
        val sr = tileSize * 0.56f
        val inner = sr * 0.28f
        val starPath = Path().apply {
            moveTo(cx, cy - sr)
            lineTo(cx + inner, cy - inner)
            lineTo(cx + sr, cy)
            lineTo(cx + inner, cy + inner)
            lineTo(cx, cy + sr)
            lineTo(cx - inner, cy + inner)
            lineTo(cx - sr, cy)
            lineTo(cx - inner, cy - inner)
            close()
        }
        canvas.drawPath(starPath, strokePaint)

        return bitmap.asImageBitmap()
    }

    private fun classifyApp(pkg: String, label: String, appInfo: ApplicationInfo): AppCategory {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (appInfo.category) {
                ApplicationInfo.CATEGORY_SOCIAL -> return AppCategory.COMMUNICATION
                ApplicationInfo.CATEGORY_AUDIO,
                ApplicationInfo.CATEGORY_VIDEO,
                ApplicationInfo.CATEGORY_IMAGE,
                ApplicationInfo.CATEGORY_GAME -> return AppCategory.MEDIA
                ApplicationInfo.CATEGORY_PRODUCTIVITY -> return AppCategory.PRODUCTIVITY
                ApplicationInfo.CATEGORY_MAPS,
                ApplicationInfo.CATEGORY_NEWS -> return AppCategory.UTILITIES
            }
        }
        val key = "$pkg ${label.lowercase()}"
        return when {
            key.contains("dialer") || key.contains("phone") || key.contains("messaging") ||
                key.contains("sms") || key.contains("whatsapp") || key.contains("telegram") ||
                key.contains("mail") || key.contains("gmail") || key.contains("chat") ||
                key.contains("contacts") -> AppCategory.COMMUNICATION
            key.contains("music") || key.contains("youtube") || key.contains("spotify") ||
                key.contains("gallery") || key.contains("photos") || key.contains("camera") ||
                key.contains("video") || key.contains("media") || key.contains("radio") -> AppCategory.MEDIA
            key.contains("calc") || key.contains("clock") || key.contains("settings") ||
                key.contains("files") || key.contains("chrome") || key.contains("browser") ||
                key.contains("maps") || key.contains("weather") -> AppCategory.UTILITIES
            key.contains("calendar") || key.contains("notes") || key.contains("docs") ||
                key.contains("drive") || key.contains("keep") || key.contains("tasks") -> AppCategory.PRODUCTIVITY
            else -> AppCategory.OTHER
        }
    }

    private fun drawableToImageBitmap(drawable: Drawable, sizePx: Int): ImageBitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            val bmp = drawable.bitmap
            if (bmp.width <= sizePx * 2 && bmp.height <= sizePx * 2) {
                return bmp.asImageBitmap()
            }
        }
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap.asImageBitmap()
    }

    private fun extractForegroundImageBitmap(drawable: Drawable, sizePx: Int): ImageBitmap? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable is AdaptiveIconDrawable) {
            val fg = drawable.foreground
            if (fg != null) {
                val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                val pad = (sizePx * 0.16f).toInt()
                fg.setBounds(-pad, -pad, sizePx + pad, sizePx + pad)
                fg.draw(canvas)
                return bitmap.asImageBitmap()
            }
        }

        val src = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(src)
        drawable.setBounds(0, 0, sizePx, sizePx)
        drawable.draw(canvas)

        val pixels = IntArray(sizePx * sizePx)
        src.getPixels(pixels, 0, sizePx, 0, 0, sizePx, sizePx)
        val corners = intArrayOf(
            pixels[2 * sizePx + 2],
            pixels[2 * sizePx + (sizePx - 3)],
            pixels[(sizePx - 3) * sizePx + 2],
            pixels[(sizePx - 3) * sizePx + (sizePx - 3)]
        )
        val opaqueCorners = corners.count { Color.alpha(it) > 220 }
        if (opaqueCorners >= 3) {
            val ref = corners[0]
            val refR = Color.red(ref)
            val refG = Color.green(ref)
            val refB = Color.blue(ref)
            var matched = 0
            for (c in corners) {
                val dist = kotlin.math.abs(Color.red(c) - refR) +
                    kotlin.math.abs(Color.green(c) - refG) +
                    kotlin.math.abs(Color.blue(c) - refB)
                if (dist < 42) matched++
            }
            if (matched >= 3) {
                for (i in pixels.indices) {
                    val p = pixels[i]
                    val a = Color.alpha(p)
                    if (a < 20) continue
                    val dist = kotlin.math.abs(Color.red(p) - refR) +
                        kotlin.math.abs(Color.green(p) - refG) +
                        kotlin.math.abs(Color.blue(p) - refB)
                    if (dist < 38) {
                        pixels[i] = Color.TRANSPARENT
                    } else if (dist < 70) {
                        val factor = ((dist - 38) / 32f).coerceIn(0f, 1f)
                        pixels[i] = Color.argb(
                            (a * factor).toInt(),
                            Color.red(p),
                            Color.green(p),
                            Color.blue(p)
                        )
                    }
                }
                val out = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
                out.setPixels(pixels, 0, sizePx, 0, 0, sizePx, sizePx)
                return out.asImageBitmap()
            }
        }
        return src.asImageBitmap()
    }

    fun launchApp(packageName: String, activityName: String? = null): Boolean {
        return runCatching {
            val intent = if (!activityName.isNullOrBlank()) {
                Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    component = ComponentName(packageName, activityName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                }
            } else {
                context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } ?: return false
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun openAppInfo(packageName: String): Boolean {
        return runCatching {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun uninstallApp(packageName: String): Boolean {
        return runCatching {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        }.getOrElse {
            openAppInfo(packageName)
        }
    }

    fun openSystemAction(action: String): Boolean {
        return runCatching {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        }.getOrElse {
            runCatching {
                val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
                true
            }.getOrDefault(false)
        }
    }

    fun isDefaultLauncher(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                return roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            }
        }
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.resolveActivity(
                homeIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        }
        return resolveInfo?.activityInfo?.packageName == context.packageName
    }

    fun createSetDefaultLauncherIntent(): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager != null &&
                roleManager.isRoleAvailable(RoleManager.ROLE_HOME) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            ) {
                return roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
            }
        }
        return Intent(Settings.ACTION_HOME_SETTINGS)
    }

    companion object {
        private const val ICON_RENDER_PX = 128
    }
}
