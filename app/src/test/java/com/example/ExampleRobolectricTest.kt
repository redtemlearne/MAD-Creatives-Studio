package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.fakes.FakeMediaRepository
import com.example.fakes.FakeProjectRepository
import com.example.fakes.TestAppContainer
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `verify app name string resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MAD Creatives Studio", appName)
    }

    @Test
    fun `verify home screen empty state and new project bottom sheet`() {
        val container = TestAppContainer()

        composeTestRule.setContent {
            MADCreativesStudioApp(appContainer = container)
        }

        composeTestRule.waitForIdle()

        // Verify branding and empty state on initial launch
        composeTestRule.onNodeWithText("MAD Creatives Studio").assertIsDisplayed()
        composeTestRule.onNodeWithText("No projects yet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("empty_state_new_project_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_settings_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_rotate_button").assertDoesNotExist()

        // Tap New Project button
        composeTestRule.onNodeWithTag("empty_state_new_project_button").performClick()
        composeTestRule.waitForIdle()

        // Bottom sheet should be displayed with project name input
        composeTestRule.onNodeWithTag("project_name_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("create_project_confirm_button").assertIsDisplayed()

        // Click create project
        composeTestRule.onNodeWithTag("create_project_confirm_button").performClick()
        composeTestRule.waitForIdle()

        // Should now be inside the Editor shell
        composeTestRule.onNodeWithTag("editor_back_button").assertExists()
        composeTestRule.onNodeWithTag("editor_rotate_button").assertDoesNotExist()
        composeTestRule.onNodeWithTag("video_preview_area").assertExists()
        composeTestRule.onNodeWithTag("playback_bar").assertExists()
        composeTestRule.onNodeWithTag("playback_timecode").assertExists()
        composeTestRule.onNodeWithTag("play_pause_button").assertExists()
        composeTestRule.onNodeWithTag("timeline_container").assertExists()
        composeTestRule.onNodeWithText("Your timeline is empty").assertExists()

        // Go back to Home
        composeTestRule.onNodeWithTag("editor_back_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("My Projects").assertExists()

        // When project list is non-empty, only FAB is shown (no duplicate header button)
        composeTestRule.onNodeWithTag("new_project_fab").assertExists()
        composeTestRule.onNodeWithTag("header_new_project_button").assertDoesNotExist()
    }

    @Test
    fun `verify editor more menu bottom sheet has only rename action`() {
        val container = TestAppContainer()

        composeTestRule.setContent {
            MADCreativesStudioApp(appContainer = container)
        }

        composeTestRule.waitForIdle()

        // Create project
        composeTestRule.onNodeWithTag("empty_state_new_project_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("create_project_confirm_button").performClick()
        composeTestRule.waitForIdle()

        // Open More options bottom sheet
        composeTestRule.onNodeWithTag("editor_more_button").performClick()
        composeTestRule.waitForIdle()

        // Only Rename should exist; rotation items should not exist
        composeTestRule.onNodeWithTag("rename_menu_item").assertExists()
        composeTestRule.onNodeWithTag("toggle_orientation_menu_item").assertDoesNotExist()
        composeTestRule.onNodeWithTag("sensor_orientation_menu_item").assertDoesNotExist()

        // Clicking Rename opens rename bottom sheet
        composeTestRule.onNodeWithTag("rename_menu_item").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("rename_input").assertExists()
        composeTestRule.onNodeWithTag("rename_confirm_button").assertExists()
        composeTestRule.onNodeWithTag("cancel_rename_button").performClick()
    }
}
