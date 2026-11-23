package com.example.model

import androidx.compose.ui.graphics.ImageBitmap

enum class AppCategory {
    ALL,
    COMMUNICATION,
    MEDIA,
    UTILITIES,
    PRODUCTIVITY,
    OTHER
}

data class InstalledApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val category: AppCategory,
    val isSystemApp: Boolean,
    val icon: ImageBitmap?,
    val foregroundIcon: ImageBitmap? = null,
    val isInternalApp: Boolean = false
)

data class AppFolder(
    val id: String,
    val title: String,
    val accentHex: Long,
    val packageNames: List<String>
)

enum class HomeWidgetId {
    EMBLEM_LOGO,
    CHARGING_POD,
    PHOTO_FRAME,
    MEDIA_CONTROL,
    SYSTEM_PULSE
}

data class HomePageData(
    val id: String,
    val pinnedPackages: List<String> = emptyList(),
    val folders: List<AppFolder> = emptyList(),
    val widgets: List<HomeWidgetId> = emptyList()
)

const val MAX_HOME_PAGES = 15
const val MIN_ICON_SIZE_DP = 44
const val DEFAULT_ICON_SIZE_DP = 58
const val MAX_ICON_SIZE_DP = 78
const val THEMES_APP_PACKAGE = "com.nlauncher.internal.themes"
const val PC_APP_PACKAGE = "com.nlauncher.internal.pc"
const val RETURN_LAUNCHER_PACKAGE = "com.nlauncher.internal.return_launcher"

data class PcExeShortcut(
    val id: String,
    val name: String,
    val uriString: String,
    val sizeBytes: Long = 0L,
    val addedAtMillis: Long = 0L
)

enum class WallpaperChoice {
    PURE_MINIMAL,
    OBSIDIAN_WAVES,
    SANDSTONE_ARCH,
    LIVE_AURORA,
    LIVE_CYBER_GRID,
    LIVE_LIQUID_FLOW,
    CUSTOM_PHOTO,
    CUSTOM_VIDEO
}

enum class IconShapeStyle {
    CIRCLE,
    SQUIRCLE,
    ROUNDED_SQUARE
}

enum class IconColorTheme(
    val bgHex: Long,
    val fgHex: Long
) {
    ORIGINAL(0xFF1A1D24L, 0xFFFFFFFFL),
    SPIDER_CRIMSON(0xFF040406L, 0xFFFF1726L),
    PRISM_GLASS(0xFF0E1524L, 0xFF38BDF8L),
    CRIMSON_NEON(0xFF030305L, 0xFFFF1F34L),
    ACID_SLIME(0xFF040604L, 0xFF88EC18L),
    LIQUID_CHROME(0xFF050608L, 0xFFE6ECF5L),
    EMERALD_NEON(0xFF030603L, 0xFF42FF2AL),
    BLACK_RED(0xFF0A0A0DL, 0xFFFF1E38L),
    BLACK_WHITE(0xFF0E1015L, 0xFFF5F7FAL),
    BLACK_GOLD(0xFF0C0B08L, 0xFFFFB800L),
    CYBER_BLUE(0xFF080D16L, 0xFF38BDF8L),
    CUSTOM_DUOTONE(0xFF0B0B0EL, 0xFFFF2438L)
}

enum class AppSymbolPreset {
    NONE,
    PHONE,
    CHAT,
    CAMERA,
    MUSIC,
    VIDEO,
    BROWSER,
    SETTINGS,
    BOLT,
    STAR,
    HEART,
    SHIELD,
    TERMINAL,
    GAME,
    COMPASS,
    MAIL
}

data class CustomAppOverride(
    val packageName: String,
    val customLabel: String? = null,
    val customIconPath: String? = null,
    val symbolPreset: AppSymbolPreset = AppSymbolPreset.NONE
)

enum class EmblemStyle {
    RIBBON_DARK,
    NEON_RING,
    MINIMAL_PILL
}

enum class PhotoFrameStyle {
    BENTO_ROUNDED,
    ARCH_WINDOW,
    POLAROID_CARD
}

enum class LauncherAccent {
    CORAL,
    COBALT,
    MINT,
    AMBER
}

enum class ChargingSource {
    AC,
    USB,
    WIRELESS,
    BATTERY
}

data class BatteryTelemetry(
    val level: Int = 82,
    val isCharging: Boolean = false,
    val source: ChargingSource = ChargingSource.BATTERY,
    val temperatureCelsius: Float = 31.2f,
    val voltageVolts: Float = 4.12f
)

data class DeviceTelemetry(
    val usedRamMb: Long = 0L,
    val totalRamMb: Long = 0L,
    val freeStorageGb: Float = 0f,
    val totalStorageGb: Float = 0f,
    val mediaVolumePercent: Int = 50
)

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class ClockStyle {
    MINIMAL_CLEAN,
    TALL_CONDENSED_GLASS,
    BUBBLY_OVERLAP_GLASS,
    IOS_DEPTH_SERIF,
    NOTHING_DOT_MATRIX,
    ANDROID_MATERIAL_PILL,
    ONE_UI_KINETIC,
    RETRO_SPLIT_FLAP,
    NEON_CYBER_GLOW,
    SWISS_MONO_GRID,
    EDITORIAL_SPLIT_CARD,
    LIFE_PROGRESS_HUD,
    AMBIENT_ANALOG_HYBRID
}

data class LauncherConfig(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isLiquidGlassEnabled: Boolean = true,
    val clockStyle: ClockStyle = ClockStyle.MINIMAL_CLEAN,
    val accent: LauncherAccent = LauncherAccent.CORAL,
    val useCustomAccent: Boolean = false,
    val customAccentHex: Long = 0xFFFF4D2DL,
    val customAccentEndHex: Long = 0xFFFF9A44L,
    val isAccentGradientEnabled: Boolean = true,
    val wallpaper: WallpaperChoice = WallpaperChoice.PURE_MINIMAL,
    val customWallpaperPath: String? = null,
    val customVideoWallpaperPath: String? = null,
    val iconShape: IconShapeStyle = IconShapeStyle.SQUIRCLE,
    val homeIconSizeDp: Int = DEFAULT_ICON_SIZE_DP,
    val iconColorTheme: IconColorTheme = IconColorTheme.ORIGINAL,
    val customIconBgHex: Long = 0xFF0A0A0DL,
    val customIconBgEndHex: Long = 0xFF2B0F1CL,
    val isIconGradientEnabled: Boolean = true,
    val customIconFgHex: Long = 0xFFFF1E38L,
    val customAppOverrides: Map<String, CustomAppOverride> = emptyMap(),
    val emblemCharacter: String = "N",
    val emblemTagline: String = "",
    val emblemStyle: EmblemStyle = EmblemStyle.RIBBON_DARK,
    val photoFramePath: String? = null,
    val photoFrameCaption: String = "",
    val photoFrameStyle: PhotoFrameStyle = PhotoFrameStyle.BENTO_ROUNDED,
    val pages: List<HomePageData> = listOf(
        HomePageData(id = "page_main"),
        HomePageData(id = "page_right")
    ),
    val hasInitializedDefaults: Boolean = false,
    val hasPlacedThemesApp: Boolean = false,
    val hasPlacedPcApp: Boolean = false,
    val isPcMode: Boolean = false,
    val pcExeShortcuts: List<PcExeShortcut> = emptyList()
)

enum class ActiveOverlay {
    NONE,
    APP_DRAWER,
    SEARCH,
    CUSTOMIZE_HUB
}
