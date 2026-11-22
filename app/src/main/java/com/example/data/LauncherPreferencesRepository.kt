package com.example.data

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppFolder
import com.example.model.AppSymbolPreset
import com.example.model.ClockStyle
import com.example.model.CustomAppOverride
import com.example.model.DEFAULT_ICON_SIZE_DP
import com.example.model.EmblemStyle
import com.example.model.HomePageData
import com.example.model.HomeWidgetId
import com.example.model.IconColorTheme
import com.example.model.IconShapeStyle
import com.example.model.LauncherAccent
import com.example.model.LauncherConfig
import com.example.model.MAX_HOME_PAGES
import com.example.model.MAX_ICON_SIZE_DP
import com.example.model.MIN_ICON_SIZE_DP
import com.example.model.PhotoFrameStyle
import com.example.model.ThemeMode
import com.example.model.WallpaperChoice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

private val Context.launcherDataStore: DataStore<Preferences> by preferencesDataStore(name = "n_launcher_prefs_v2")

class LauncherPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_LIQUID_GLASS_ENABLED = booleanPreferencesKey("is_liquid_glass_enabled")
        val CLOCK_STYLE = stringPreferencesKey("clock_style")
        val ACCENT = stringPreferencesKey("accent")
        val WALLPAPER = stringPreferencesKey("wallpaper")
        val CUSTOM_WALLPAPER_PATH = stringPreferencesKey("custom_wallpaper_path")
        val CUSTOM_VIDEO_WALLPAPER_PATH = stringPreferencesKey("custom_video_wallpaper_path")
        val ICON_SHAPE = stringPreferencesKey("icon_shape")
        val HOME_ICON_SIZE_DP = intPreferencesKey("home_icon_size_dp")
        val ICON_COLOR_THEME = stringPreferencesKey("icon_color_theme")
        val CUSTOM_ICON_BG = longPreferencesKey("custom_icon_bg")
        val CUSTOM_ICON_FG = longPreferencesKey("custom_icon_fg")
        val APP_OVERRIDES_JSON = stringPreferencesKey("app_overrides_json")
        val EMBLEM_CHAR = stringPreferencesKey("emblem_char")
        val EMBLEM_TAGLINE = stringPreferencesKey("emblem_tagline")
        val EMBLEM_STYLE = stringPreferencesKey("emblem_style")
        val PHOTO_FRAME_PATH = stringPreferencesKey("photo_frame_path")
        val PHOTO_FRAME_CAPTION = stringPreferencesKey("photo_frame_caption")
        val PHOTO_FRAME_STYLE = stringPreferencesKey("photo_frame_style")
        val PAGES_JSON = stringPreferencesKey("pages_json")
        val INITIALIZED_DEFAULTS = booleanPreferencesKey("initialized_defaults")
        val HAS_PLACED_THEMES_APP = booleanPreferencesKey("has_placed_themes_app")
    }

    val configFlow: Flow<LauncherConfig> = context.launcherDataStore.data.map { prefs ->
        val themeMode = enumValueOrDefault(prefs[Keys.THEME_MODE], ThemeMode.SYSTEM)
        val clockStyle = enumValueOrDefault(prefs[Keys.CLOCK_STYLE], ClockStyle.MINIMAL_CLEAN)
        val accent = enumValueOrDefault(prefs[Keys.ACCENT], LauncherAccent.CORAL)
        val wallpaper = enumValueOrDefault(prefs[Keys.WALLPAPER], WallpaperChoice.PURE_MINIMAL)
        val iconShape = enumValueOrDefault(prefs[Keys.ICON_SHAPE], IconShapeStyle.SQUIRCLE)
        val iconColorTheme = enumValueOrDefault(prefs[Keys.ICON_COLOR_THEME], IconColorTheme.ORIGINAL)
        val emblemStyle = enumValueOrDefault(prefs[Keys.EMBLEM_STYLE], EmblemStyle.RIBBON_DARK)
        val photoStyle = enumValueOrDefault(prefs[Keys.PHOTO_FRAME_STYLE], PhotoFrameStyle.BENTO_ROUNDED)

        val defaultPages = listOf(
            HomePageData(id = "page_main"),
            HomePageData(id = "page_right")
        )
        val parsedPages = parsePagesJson(prefs[Keys.PAGES_JSON]).ifEmpty { defaultPages }.take(MAX_HOME_PAGES)
        val iconSizeDp = (prefs[Keys.HOME_ICON_SIZE_DP] ?: DEFAULT_ICON_SIZE_DP).coerceIn(MIN_ICON_SIZE_DP, MAX_ICON_SIZE_DP)

        LauncherConfig(
            themeMode = themeMode,
            isLiquidGlassEnabled = prefs[Keys.IS_LIQUID_GLASS_ENABLED] ?: true,
            clockStyle = clockStyle,
            accent = accent,
            wallpaper = wallpaper,
            customWallpaperPath = prefs[Keys.CUSTOM_WALLPAPER_PATH],
            customVideoWallpaperPath = prefs[Keys.CUSTOM_VIDEO_WALLPAPER_PATH],
            iconShape = iconShape,
            homeIconSizeDp = iconSizeDp,
            iconColorTheme = iconColorTheme,
            customIconBgHex = prefs[Keys.CUSTOM_ICON_BG] ?: 0xFF0A0A0DL,
            customIconFgHex = prefs[Keys.CUSTOM_ICON_FG] ?: 0xFFFF1E38L,
            customAppOverrides = parseAppOverridesJson(prefs[Keys.APP_OVERRIDES_JSON]),
            emblemCharacter = prefs[Keys.EMBLEM_CHAR]?.take(2)?.ifBlank { "N" } ?: "N",
            emblemTagline = prefs[Keys.EMBLEM_TAGLINE] ?: "",
            emblemStyle = emblemStyle,
            photoFramePath = prefs[Keys.PHOTO_FRAME_PATH],
            photoFrameCaption = prefs[Keys.PHOTO_FRAME_CAPTION] ?: "",
            photoFrameStyle = photoStyle,
            pages = parsedPages,
            hasInitializedDefaults = prefs[Keys.INITIALIZED_DEFAULTS] ?: false,
            hasPlacedThemesApp = prefs[Keys.HAS_PLACED_THEMES_APP] ?: false
        )
    }

    suspend fun updateThemeMode(mode: ThemeMode) {
        context.launcherDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun updateLiquidGlassEnabled(enabled: Boolean) {
        context.launcherDataStore.edit { it[Keys.IS_LIQUID_GLASS_ENABLED] = enabled }
    }

    suspend fun updateClockStyle(style: ClockStyle) {
        context.launcherDataStore.edit { it[Keys.CLOCK_STYLE] = style.name }
    }

    suspend fun updateAccent(accent: LauncherAccent) {
        context.launcherDataStore.edit { it[Keys.ACCENT] = accent.name }
    }

    suspend fun updateWallpaper(choice: WallpaperChoice) {
        context.launcherDataStore.edit { it[Keys.WALLPAPER] = choice.name }
    }

    suspend fun updateIconShape(shape: IconShapeStyle) {
        context.launcherDataStore.edit { it[Keys.ICON_SHAPE] = shape.name }
    }

    suspend fun updateIconSizeDp(sizeDp: Int) {
        context.launcherDataStore.edit {
            it[Keys.HOME_ICON_SIZE_DP] = sizeDp.coerceIn(MIN_ICON_SIZE_DP, MAX_ICON_SIZE_DP)
        }
    }

    suspend fun updateIconColorTheme(theme: IconColorTheme, customBgHex: Long? = null, customFgHex: Long? = null) {
        context.launcherDataStore.edit {
            it[Keys.ICON_COLOR_THEME] = theme.name
            if (customBgHex != null) it[Keys.CUSTOM_ICON_BG] = customBgHex
            if (customFgHex != null) it[Keys.CUSTOM_ICON_FG] = customFgHex
        }
    }

    suspend fun saveAppOverrides(overrides: Map<String, CustomAppOverride>) {
        context.launcherDataStore.edit {
            it[Keys.APP_OVERRIDES_JSON] = encodeAppOverridesJson(overrides)
        }
    }

    suspend fun importCustomAppIcon(packageName: String, uri: Uri): String? = withContext(Dispatchers.IO) {
        val safePkg = packageName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val file = copyUriToInternalFile(uri, "app_icon_${safePkg}_${System.currentTimeMillis()}.jpg")
        if (file != null) {
            context.filesDir.listFiles()?.forEach { old ->
                if (old.name.startsWith("app_icon_${safePkg}_") && old.absolutePath != file.absolutePath) {
                    old.delete()
                }
            }
            file.absolutePath
        } else null
    }

    suspend fun updateEmblem(character: String, tagline: String, style: EmblemStyle) {
        context.launcherDataStore.edit {
            it[Keys.EMBLEM_CHAR] = character.trim().take(2).ifEmpty { "N" }
            it[Keys.EMBLEM_TAGLINE] = tagline
            it[Keys.EMBLEM_STYLE] = style.name
        }
    }

    suspend fun updatePhotoFrameStyle(style: PhotoFrameStyle, caption: String) {
        context.launcherDataStore.edit {
            it[Keys.PHOTO_FRAME_STYLE] = style.name
            it[Keys.PHOTO_FRAME_CAPTION] = caption
        }
    }

    suspend fun savePages(
        pages: List<HomePageData>,
        markInitialized: Boolean = true,
        markThemesPlaced: Boolean = false
    ) {
        context.launcherDataStore.edit {
            it[Keys.PAGES_JSON] = encodePagesJson(pages.take(MAX_HOME_PAGES))
            if (markInitialized) {
                it[Keys.INITIALIZED_DEFAULTS] = true
            }
            if (markThemesPlaced) {
                it[Keys.HAS_PLACED_THEMES_APP] = true
            }
        }
    }

    suspend fun markThemesAppPlaced() {
        context.launcherDataStore.edit {
            it[Keys.HAS_PLACED_THEMES_APP] = true
        }
    }

    suspend fun importPhotoForWidget(uri: Uri): String? = withContext(Dispatchers.IO) {
        val file = copyUriToInternalFile(uri, "photo_frame_${System.currentTimeMillis()}.jpg")
        if (file != null) {
            context.filesDir.listFiles()?.forEach { old ->
                if (old.name.startsWith("photo_frame_") && old.absolutePath != file.absolutePath) {
                    old.delete()
                }
            }
            context.launcherDataStore.edit {
                it[Keys.PHOTO_FRAME_PATH] = file.absolutePath
            }
            file.absolutePath
        } else null
    }

    suspend fun importCustomWallpaper(uri: Uri): String? = withContext(Dispatchers.IO) {
        val file = copyUriToInternalFile(uri, "custom_wallpaper_${System.currentTimeMillis()}.jpg")
        if (file != null) {
            context.filesDir.listFiles()?.forEach { old ->
                if (old.name.startsWith("custom_wallpaper_") && old.absolutePath != file.absolutePath) {
                    old.delete()
                }
            }
            context.launcherDataStore.edit {
                it[Keys.CUSTOM_WALLPAPER_PATH] = file.absolutePath
                it[Keys.WALLPAPER] = WallpaperChoice.CUSTOM_PHOTO.name
            }
            file.absolutePath
        } else null
    }

    suspend fun importCustomVideoWallpaper(uri: Uri): String? = withContext(Dispatchers.IO) {
        val file = copyUriToInternalFile(uri, "custom_video_wallpaper_${System.currentTimeMillis()}.mp4")
        if (file != null) {
            context.filesDir.listFiles()?.forEach { old ->
                if (old.name.startsWith("custom_video_wallpaper_") && old.absolutePath != file.absolutePath) {
                    old.delete()
                }
            }
            context.launcherDataStore.edit {
                it[Keys.CUSTOM_VIDEO_WALLPAPER_PATH] = file.absolutePath
                it[Keys.WALLPAPER] = WallpaperChoice.CUSTOM_VIDEO.name
            }
            file.absolutePath
        } else null
    }

    private fun copyUriToInternalFile(uri: Uri, targetName: String): File? {
        return runCatching {
            val outFile = File(context.filesDir, targetName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            outFile
        }.getOrNull()
    }

    private fun parsePagesJson(raw: String?): List<HomePageData> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val pinnedArr = obj.optJSONArray("pinnedPackages") ?: JSONArray()
                    val pinned = buildList {
                        for (p in 0 until pinnedArr.length()) {
                            pinnedArr.optString(p).takeIf { it.isNotBlank() }?.let(::add)
                        }
                    }

                    val foldersArr = obj.optJSONArray("folders") ?: JSONArray()
                    val folders = buildList {
                        for (f in 0 until foldersArr.length()) {
                            val fObj = foldersArr.getJSONObject(f)
                            val fPkgsArr = fObj.optJSONArray("packageNames") ?: JSONArray()
                            val fPkgs = buildList {
                                for (k in 0 until fPkgsArr.length()) {
                                    fPkgsArr.optString(k).takeIf { it.isNotBlank() }?.let(::add)
                                }
                            }
                            add(
                                AppFolder(
                                    id = fObj.optString("id", "pod_${i}_$f"),
                                    title = fObj.optString("title", "Pod"),
                                    accentHex = fObj.optLong("accentHex", 0xFFFF4D2DL),
                                    packageNames = fPkgs
                                )
                            )
                        }
                    }

                    val widgetsArr = obj.optJSONArray("widgets") ?: JSONArray()
                    val widgets = buildList {
                        for (w in 0 until widgetsArr.length()) {
                            runCatching { HomeWidgetId.valueOf(widgetsArr.optString(w)) }
                                .getOrNull()
                                ?.let(::add)
                        }
                    }

                    add(
                        HomePageData(
                            id = obj.optString("id", "page_$i"),
                            pinnedPackages = pinned,
                            folders = folders,
                            widgets = widgets
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun encodePagesJson(pages: List<HomePageData>): String {
        val array = JSONArray()
        pages.forEach { page ->
            val foldersArr = JSONArray()
            page.folders.forEach { folder ->
                foldersArr.put(
                    JSONObject().apply {
                        put("id", folder.id)
                        put("title", folder.title)
                        put("accentHex", folder.accentHex)
                        put("packageNames", JSONArray(folder.packageNames))
                    }
                )
            }
            val obj = JSONObject().apply {
                put("id", page.id)
                put("pinnedPackages", JSONArray(page.pinnedPackages))
                put("folders", foldersArr)
                put("widgets", JSONArray(page.widgets.map { it.name }))
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseAppOverridesJson(raw: String?): Map<String, CustomAppOverride> {
        if (raw.isNullOrBlank()) return emptyMap()
        return runCatching {
            val array = JSONArray(raw)
            buildMap {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val pkg = obj.optString("packageName")
                    if (pkg.isNotBlank()) {
                        put(
                            pkg,
                            CustomAppOverride(
                                packageName = pkg,
                                customLabel = obj.optString("customLabel").takeIf { it.isNotBlank() },
                                customIconPath = obj.optString("customIconPath").takeIf { it.isNotBlank() },
                                symbolPreset = enumValueOrDefault(
                                    obj.optString("symbolPreset"),
                                    AppSymbolPreset.NONE
                                )
                            )
                        )
                    }
                }
            }
        }.getOrDefault(emptyMap())
    }

    private fun encodeAppOverridesJson(overrides: Map<String, CustomAppOverride>): String {
        val array = JSONArray()
        overrides.values.forEach { ov ->
            array.put(
                JSONObject().apply {
                    put("packageName", ov.packageName)
                    put("customLabel", ov.customLabel ?: "")
                    put("customIconPath", ov.customIconPath ?: "")
                    put("symbolPreset", ov.symbolPreset.name)
                }
            )
        }
        return array.toString()
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(raw: String?, fallback: T): T {
        if (raw.isNullOrBlank()) return fallback
        return runCatching { enumValueOf<T>(raw) }.getOrDefault(fallback)
    }
}
