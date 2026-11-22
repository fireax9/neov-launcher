package com.example

import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appNameAndSystemLocaleResourcesWork() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("N Launcher", context.getString(R.string.app_name))

        val arConfig = Configuration(context.resources.configuration).apply {
            setLocale(Locale.forLanguageTag("ar"))
        }
        val arContext = context.createConfigurationContext(arConfig)
        assertEquals("تلقائي (الهاتف)", arContext.getString(R.string.theme_mode_system))
    }

    @Test
    fun twoPagesCleanStartLongPressHubThemeModeAndFloatingDock() {
        composeTestRule.waitForIdle()

        if (composeTestRule.onAllNodesWithTag("default_launcher_gate").fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onNodeWithTag("skip_default_launcher_button").performClick()
            composeTestRule.waitForIdle()
        }

        // 1. Main Home Page (Page 0) has Clock, built-in Themes app, and NO pre-added charging/photo widgets
        composeTestRule.onNodeWithTag("home_clock_header").assertIsDisplayed()
        composeTestRule.onNodeWithTag("app_tile_com.nlauncher.internal.themes").assertIsDisplayed()
        assertTrue(composeTestRule.onAllNodesWithTag("widget_charging_pod").fetchSemanticsNodes().isEmpty())
        assertTrue(composeTestRule.onAllNodesWithTag("widget_photo_frame").fetchSemanticsNodes().isEmpty())

        // Tapping built-in Themes app opens Visual Customize Hub directly
        composeTestRule.onNodeWithTag("app_tile_com.nlauncher.internal.themes").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("visual_customize_hub").assertIsDisplayed()
        composeTestRule.onNodeWithTag("close_customize_hub_button").performClick()
        composeTestRule.waitForIdle()

        // 2. Page indicators show 2 pages; tapping dot 1 navigates to empty right page
        composeTestRule.onNodeWithTag("page_dot_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("page_dot_1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("empty_page_placeholder").assertIsDisplayed()

        // 3. Long-pressing empty space enters phone-like jiggle Home Edit Mode (does not hijack with Hub modal)
        composeTestRule.onNodeWithTag("empty_page_placeholder").performTouchInput { longClick() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_edit_mode_bar").assertIsDisplayed()
        assertTrue(composeTestRule.onAllNodesWithTag("visual_customize_hub").fetchSemanticsNodes().isEmpty())

        // From Edit Mode bar, user can tap Themes or Add to open Visual Customize Hub
        composeTestRule.onNodeWithTag("edit_bar_themes_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("visual_customize_hub").assertIsDisplayed()

        composeTestRule.onNodeWithTag("hub_tab_widgets").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("widget_picker_CHARGING_POD").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("hub_tab_colors").performClick()
        composeTestRule.waitForIdle()
        listOf(
            "icon_theme_SPIDER_CRIMSON",
            "icon_theme_PRISM_GLASS",
            "icon_theme_CRIMSON_NEON",
            "icon_theme_ACID_SLIME",
            "icon_theme_LIQUID_CHROME",
            "icon_theme_EMERALD_NEON",
            "icon_theme_BLACK_RED"
        ).forEach { tag ->
            composeTestRule.onNodeWithTag(tag).performScrollTo().performClick()
            composeTestRule.waitForIdle()
        }

        composeTestRule.onNodeWithTag("icon_size_preset_68").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("icon_size_value_label").assertTextContains("68 dp")

        composeTestRule.onNodeWithTag("hub_tab_pages").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("liquid_glass_toggle").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("liquid_glass_toggle").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("liquid_glass_toggle").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("clock_style_TALL_CONDENSED_GLASS").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("clock_style_BUBBLY_OVERLAP_GLASS").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Test Animated Live Wallpapers & Video Picker visibility
        composeTestRule.onNodeWithTag("pick_video_wallpaper_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("wallpaper_chip_LIVE_AURORA").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("wallpaper_chip_LIVE_LIQUID_FLOW").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Test adding pages up to max 15
        composeTestRule.onNodeWithTag("pages_count_badge").performScrollTo().assertTextContains("2 / 15")
        repeat(13) {
            composeTestRule.onNodeWithTag("add_new_page_button").performScrollTo().performClick()
            composeTestRule.waitForIdle()
        }
        composeTestRule.onNodeWithTag("pages_count_badge").performScrollTo().assertTextContains("15 / 15")
        composeTestRule.onNodeWithTag("add_new_page_button").performScrollTo().assertIsNotEnabled()

        composeTestRule.onNodeWithTag("theme_mode_SYSTEM").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_mode_LIGHT").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("theme_mode_SYSTEM").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Enter Home Edit Mode from Hub Apps tab, verify edit bar, and exit via Done button
        composeTestRule.onNodeWithTag("hub_tab_apps").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("hub_enter_edit_mode_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_edit_mode_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("done_edit_mode_button").performClick()
        composeTestRule.waitForIdle()
        assertTrue(composeTestRule.onAllNodesWithTag("home_edit_mode_bar").fetchSemanticsNodes().isEmpty())

        // Verify live animated wallpaper and switch back to Page 0 clock header
        composeTestRule.onNodeWithTag("live_wallpaper_liquid_flow").assertIsDisplayed()
        composeTestRule.onNodeWithTag("page_dot_0").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_clock_header").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()

        // 4. Floating N button opens App Drawer; Floating Search Bar opens Search overlay
        composeTestRule.onNodeWithTag("n_drawer_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("app_drawer_overlay").assertIsDisplayed()

        composeTestRule.onNodeWithTag("close_drawer_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("floating_search_bar").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("search_overlay").assertIsDisplayed()
    }
}
