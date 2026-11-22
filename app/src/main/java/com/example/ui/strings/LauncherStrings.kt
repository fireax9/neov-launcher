package com.example.ui.strings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.example.R
import com.example.model.AppCategory
import com.example.model.ChargingSource
import com.example.model.ClockStyle
import com.example.model.EmblemStyle
import com.example.model.HomeWidgetId
import com.example.model.IconColorTheme
import com.example.model.IconShapeStyle
import com.example.model.LauncherAccent
import com.example.model.PhotoFrameStyle
import com.example.model.ThemeMode
import com.example.model.WallpaperChoice

class AppStrings(private val context: Context) {
    val defaultTagline: String get() = context.getString(R.string.default_tagline)
    val defaultPhotoCaption: String get() = context.getString(R.string.default_photo_caption)
    val searchPlaceholder: String get() = context.getString(R.string.search_placeholder)
    val allAppsTitle: String get() = context.getString(R.string.all_apps_title)
    fun appsCountLabel(count: Int): String = context.getString(R.string.apps_count_label, count)
    val noAppsFound: String get() = context.getString(R.string.no_apps_found)
    val clearSearch: String get() = context.getString(R.string.clear_search)
    val openDrawerCd: String get() = context.getString(R.string.open_drawer_cd)
    val closeCd: String get() = context.getString(R.string.close_cd)

    val gateBadge: String get() = context.getString(R.string.gate_badge)
    val gateTitle: String get() = context.getString(R.string.gate_title)
    val gateSubtitle: String get() = context.getString(R.string.gate_subtitle)
    val gateStep1: String get() = context.getString(R.string.gate_step_1)
    val gateStep2: String get() = context.getString(R.string.gate_step_2)
    val gateStep3: String get() = context.getString(R.string.gate_step_3)
    val gateCtaPrimary: String get() = context.getString(R.string.gate_cta_primary)
    val gateCtaSecondary: String get() = context.getString(R.string.gate_cta_secondary)

    val longPressHint: String get() = context.getString(R.string.long_press_hint)
    val emptyPageTitle: String get() = context.getString(R.string.empty_page_title)
    val emptyPageSubtitle: String get() = context.getString(R.string.empty_page_subtitle)
    val addToThisPage: String get() = context.getString(R.string.add_to_this_page)
    fun pageLabel(index: Int): String = if (index == 0) {
        context.getString(R.string.page_main_label)
    } else {
        context.getString(R.string.page_numbered_label, index + 1)
    }

    val hubTitle: String get() = context.getString(R.string.hub_title)
    val hubSubtitle: String get() = context.getString(R.string.hub_subtitle)
    val tabAddApps: String get() = context.getString(R.string.tab_add_apps)
    val tabAddWidgets: String get() = context.getString(R.string.tab_add_widgets)
    val tabIconColors: String get() = context.getString(R.string.tab_icon_colors)
    val tabPagesLook: String get() = context.getString(R.string.tab_pages_look)

    val addFolderCard: String get() = context.getString(R.string.add_folder_card)
    val addFolderDesc: String get() = context.getString(R.string.add_folder_desc)
    val pinAppsTitle: String get() = context.getString(R.string.pin_apps_title)
    val widgetsPickerTitle: String get() = context.getString(R.string.widgets_picker_title)
    val addedBadge: String get() = context.getString(R.string.added_badge)
    val addBadge: String get() = context.getString(R.string.add_badge)
    val removeBadge: String get() = context.getString(R.string.remove_badge)

    val iconPackTitle: String get() = context.getString(R.string.icon_pack_title)
    val iconPackDesc: String get() = context.getString(R.string.icon_pack_desc)
    val customBgColor: String get() = context.getString(R.string.custom_bg_color)
    val customFgColor: String get() = context.getString(R.string.custom_fg_color)
    val iconShapeTitle: String get() = context.getString(R.string.icon_shape_title)

    val appCustomizeTitle: String get() = context.getString(R.string.app_customize_title)
    val changeAppLogo: String get() = context.getString(R.string.change_app_logo)
    val pickLogoImage: String get() = context.getString(R.string.pick_logo_image)
    val resetDefaultLogo: String get() = context.getString(R.string.reset_default_logo)
    val symbolLibrary: String get() = context.getString(R.string.symbol_library)
    val renameAppLabel: String get() = context.getString(R.string.rename_app_label)
    val pinOnPage: String get() = context.getString(R.string.pin_on_page)
    val removeFromPage: String get() = context.getString(R.string.remove_from_page)
    val addToFolder: String get() = context.getString(R.string.add_to_folder)
    val openApp: String get() = context.getString(R.string.open_app)
    val appInfo: String get() = context.getString(R.string.app_info)

    val newFolder: String get() = context.getString(R.string.new_folder)
    val folderEmptyHint: String get() = context.getString(R.string.folder_empty_hint)
    val manageApps: String get() = context.getString(R.string.manage_apps)
    val folderNameLabel: String get() = context.getString(R.string.folder_name_label)
    val deleteFolder: String get() = context.getString(R.string.delete_folder)
    val save: String get() = context.getString(R.string.save)
    val cancel: String get() = context.getString(R.string.cancel)

    val chargingTitle: String get() = context.getString(R.string.charging_title)
    val chargingActive: String get() = context.getString(R.string.charging_active)
    val chargingIdle: String get() = context.getString(R.string.charging_idle)
    val tempLabel: String get() = context.getString(R.string.temp_label)
    val voltageLabel: String get() = context.getString(R.string.voltage_label)
    val photoWidgetTitle: String get() = context.getString(R.string.photo_widget_title)
    val pickPhoto: String get() = context.getString(R.string.pick_photo)
    val frameStyleLabel: String get() = context.getString(R.string.frame_style_label)
    val photoCaptionLabel: String get() = context.getString(R.string.photo_caption_label)
    val mediaWidgetTitle: String get() = context.getString(R.string.media_widget_title)
    val mediaSubtitle: String get() = context.getString(R.string.media_subtitle)
    val mediaVolume: String get() = context.getString(R.string.media_volume)
    val openMusicApp: String get() = context.getString(R.string.open_music_app)
    val systemPulseTitle: String get() = context.getString(R.string.system_pulse_title)
    val ramLabel: String get() = context.getString(R.string.ram_label)
    val storageLabel: String get() = context.getString(R.string.storage_label)
    val wifiShortcut: String get() = context.getString(R.string.wifi_shortcut)
    val bluetoothShortcut: String get() = context.getString(R.string.bluetooth_shortcut)
    val displayShortcut: String get() = context.getString(R.string.display_shortcut)
    val setHomeShortcut: String get() = context.getString(R.string.set_home_shortcut)

    val pagesManagerTitle: String get() = context.getString(R.string.pages_manager_title)
    val addNewPage: String get() = context.getString(R.string.add_new_page)
    val deletePage: String get() = context.getString(R.string.delete_page)
    val wallpaperTitle: String get() = context.getString(R.string.wallpaper_title)
    val themeModeTitle: String get() = context.getString(R.string.theme_mode_title)
    val liquidGlassTitle: String get() = context.getString(R.string.liquid_glass_title)
    val liquidGlassDesc: String get() = context.getString(R.string.liquid_glass_desc)
    val liquidGlassOn: String get() = context.getString(R.string.liquid_glass_on)
    val liquidGlassOff: String get() = context.getString(R.string.liquid_glass_off)
    val iconDesignsSectionTitle: String get() = context.getString(R.string.icon_designs_section_title)
    val iconColorsSectionTitle: String get() = context.getString(R.string.icon_colors_section_title)
    val emblemSettingsTitle: String get() = context.getString(R.string.emblem_settings_title)
    val emblemCharLabel: String get() = context.getString(R.string.emblem_char_label)
    val emblemTaglineLabel: String get() = context.getString(R.string.emblem_tagline_label)

    fun themeModeName(mode: ThemeMode): String = when (mode) {
        ThemeMode.SYSTEM -> context.getString(R.string.theme_mode_system)
        ThemeMode.LIGHT -> context.getString(R.string.theme_mode_light)
        ThemeMode.DARK -> context.getString(R.string.theme_mode_dark)
    }

    fun categoryName(category: AppCategory): String = when (category) {
        AppCategory.ALL -> context.getString(R.string.cat_all)
        AppCategory.COMMUNICATION -> context.getString(R.string.cat_communication)
        AppCategory.MEDIA -> context.getString(R.string.cat_media)
        AppCategory.UTILITIES -> context.getString(R.string.cat_utilities)
        AppCategory.PRODUCTIVITY -> context.getString(R.string.cat_productivity)
        AppCategory.OTHER -> context.getString(R.string.cat_other)
    }

    fun widgetName(id: HomeWidgetId): String = when (id) {
        HomeWidgetId.EMBLEM_LOGO -> context.getString(R.string.widget_emblem_name)
        HomeWidgetId.CHARGING_POD -> context.getString(R.string.widget_charging_name)
        HomeWidgetId.PHOTO_FRAME -> context.getString(R.string.widget_photo_name)
        HomeWidgetId.MEDIA_CONTROL -> context.getString(R.string.widget_media_name)
        HomeWidgetId.SYSTEM_PULSE -> context.getString(R.string.widget_system_name)
    }

    fun widgetDescription(id: HomeWidgetId): String = when (id) {
        HomeWidgetId.EMBLEM_LOGO -> context.getString(R.string.widget_emblem_desc)
        HomeWidgetId.CHARGING_POD -> context.getString(R.string.widget_charging_desc)
        HomeWidgetId.PHOTO_FRAME -> context.getString(R.string.widget_photo_desc)
        HomeWidgetId.MEDIA_CONTROL -> context.getString(R.string.widget_media_desc)
        HomeWidgetId.SYSTEM_PULSE -> context.getString(R.string.widget_system_desc)
    }

    fun iconThemeName(theme: IconColorTheme): String = when (theme) {
        IconColorTheme.ORIGINAL -> context.getString(R.string.icon_theme_original)
        IconColorTheme.SPIDER_CRIMSON -> context.getString(R.string.icon_theme_spider_crimson)
        IconColorTheme.PRISM_GLASS -> context.getString(R.string.icon_theme_prism_glass)
        IconColorTheme.CRIMSON_NEON -> context.getString(R.string.icon_theme_crimson_neon)
        IconColorTheme.ACID_SLIME -> context.getString(R.string.icon_theme_acid_slime)
        IconColorTheme.LIQUID_CHROME -> context.getString(R.string.icon_theme_liquid_chrome)
        IconColorTheme.EMERALD_NEON -> context.getString(R.string.icon_theme_emerald_neon)
        IconColorTheme.BLACK_RED -> context.getString(R.string.icon_theme_black_red)
        IconColorTheme.BLACK_WHITE -> context.getString(R.string.icon_theme_black_white)
        IconColorTheme.BLACK_GOLD -> context.getString(R.string.icon_theme_black_gold)
        IconColorTheme.CYBER_BLUE -> context.getString(R.string.icon_theme_cyber_blue)
        IconColorTheme.CUSTOM_DUOTONE -> context.getString(R.string.icon_theme_custom)
    }

    fun iconShapeName(shape: IconShapeStyle): String = when (shape) {
        IconShapeStyle.CIRCLE -> context.getString(R.string.shape_circle)
        IconShapeStyle.SQUIRCLE -> context.getString(R.string.shape_squircle)
        IconShapeStyle.ROUNDED_SQUARE -> context.getString(R.string.shape_rounded)
    }

    fun wallpaperName(choice: WallpaperChoice): String = when (choice) {
        WallpaperChoice.PURE_MINIMAL -> context.getString(R.string.wp_pure_minimal)
        WallpaperChoice.OBSIDIAN_WAVES -> context.getString(R.string.wp_obsidian)
        WallpaperChoice.SANDSTONE_ARCH -> context.getString(R.string.wp_sandstone)
        WallpaperChoice.LIVE_AURORA -> context.getString(R.string.wp_live_aurora)
        WallpaperChoice.LIVE_CYBER_GRID -> context.getString(R.string.wp_live_cyber_grid)
        WallpaperChoice.LIVE_LIQUID_FLOW -> context.getString(R.string.wp_live_liquid_flow)
        WallpaperChoice.CUSTOM_PHOTO -> context.getString(R.string.wp_custom)
        WallpaperChoice.CUSTOM_VIDEO -> context.getString(R.string.wp_custom_video)
    }

    fun emblemStyleName(style: EmblemStyle): String = when (style) {
        EmblemStyle.RIBBON_DARK -> context.getString(R.string.emblem_ribbon)
        EmblemStyle.NEON_RING -> context.getString(R.string.emblem_neon)
        EmblemStyle.MINIMAL_PILL -> context.getString(R.string.emblem_pill)
    }

    fun photoStyleName(style: PhotoFrameStyle): String = when (style) {
        PhotoFrameStyle.BENTO_ROUNDED -> context.getString(R.string.photo_bento)
        PhotoFrameStyle.ARCH_WINDOW -> context.getString(R.string.photo_arch)
        PhotoFrameStyle.POLAROID_CARD -> context.getString(R.string.photo_polaroid)
    }

    fun accentName(accent: LauncherAccent): String = when (accent) {
        LauncherAccent.CORAL -> context.getString(R.string.accent_coral)
        LauncherAccent.COBALT -> context.getString(R.string.accent_cobalt)
        LauncherAccent.MINT -> context.getString(R.string.accent_mint)
        LauncherAccent.AMBER -> context.getString(R.string.accent_amber)
    }

    fun chargingSourceName(source: ChargingSource): String = when (source) {
        ChargingSource.AC -> context.getString(R.string.source_ac)
        ChargingSource.USB -> context.getString(R.string.source_usb)
        ChargingSource.WIRELESS -> context.getString(R.string.source_wireless)
        ChargingSource.BATTERY -> context.getString(R.string.source_battery)
    }

    val clockStyleTitle: String get() = context.getString(R.string.clock_style_title)
    fun clockStyleName(style: ClockStyle): String = when (style) {
        ClockStyle.MINIMAL_CLEAN -> context.getString(R.string.clock_style_minimal)
        ClockStyle.TALL_CONDENSED_GLASS -> context.getString(R.string.clock_style_tall_glass)
        ClockStyle.BUBBLY_OVERLAP_GLASS -> context.getString(R.string.clock_style_bubbly_glass)
    }

    val editLayoutDone: String get() = context.getString(R.string.edit_layout_done)
    val editLayoutHint: String get() = context.getString(R.string.edit_layout_hint)
    val movePrev: String get() = context.getString(R.string.move_prev)
    val moveNext: String get() = context.getString(R.string.move_next)
    val movePage: String get() = context.getString(R.string.move_page)
    val uninstallApp: String get() = context.getString(R.string.uninstall_app)
    val customizeAppShort: String get() = context.getString(R.string.customize_app_short)
    val removeFromHome: String get() = context.getString(R.string.remove_from_home)
    val editHomeAppsAction: String get() = context.getString(R.string.edit_home_apps_action)

    val wallpaperAnimatedBadge: String get() = context.getString(R.string.wallpaper_animated_badge)
    val pickVideoWallpaper: String get() = context.getString(R.string.pick_video_wallpaper)
    fun maxPagesReached(max: Int): String = context.getString(R.string.max_pages_reached, max)
    fun pagesLimitHint(max: Int): String = context.getString(R.string.pages_limit_hint, max)
    val iconSizeTitle: String get() = context.getString(R.string.icon_size_title)
    val iconSizeSmall: String get() = context.getString(R.string.icon_size_small)
    val iconSizeMedium: String get() = context.getString(R.string.icon_size_medium)
    val iconSizeLarge: String get() = context.getString(R.string.icon_size_large)
    val iconSizeXl: String get() = context.getString(R.string.icon_size_xl)
    val themesAppLabel: String get() = context.getString(R.string.themes_app_label)
    val editAddQuick: String get() = context.getString(R.string.edit_add_quick)
    val editThemesQuick: String get() = context.getString(R.string.edit_themes_quick)
}

@Composable
fun rememberAppStrings(): AppStrings {
    LocalConfiguration.current
    val context = LocalContext.current
    return AppStrings(context)
}
