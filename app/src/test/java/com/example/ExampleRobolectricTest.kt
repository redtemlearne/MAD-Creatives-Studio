package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.state.ProjectSessionManager
import com.example.ui.theme.MADCreativesTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
    fun `verify project creation and session rename`() {
        val sessionManager = ProjectSessionManager()
        val project = sessionManager.createProject("Test Vlog")

        assertEquals("Test Vlog", project.name)
        assertEquals(1, sessionManager.projects.value.size)
        assertEquals(project.id, sessionManager.activeProject.value?.id)

        sessionManager.renameProject(project.id, "Renamed Vlog")
        assertEquals("Renamed Vlog", sessionManager.activeProject.value?.name)
        assertEquals("Renamed Vlog", sessionManager.getProject(project.id)?.name)
    }

    @Test
    fun `verify home screen empty state and new project dialog`() {
        composeTestRule.setContent {
            MADCreativesStudioApp()
        }

        // Verify branding and empty state on initial launch
        composeTestRule.onNodeWithText("MAD Creatives Studio").assertIsDisplayed()
        composeTestRule.onNodeWithText("No projects yet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("empty_state_new_project_button").assertIsDisplayed()

        // Tap New Project button
        composeTestRule.onNodeWithTag("empty_state_new_project_button").performClick()

        // Dialog should be displayed with project name input
        composeTestRule.onNodeWithTag("project_name_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("create_project_confirm_button").assertIsDisplayed()

        // Click create project
        composeTestRule.onNodeWithTag("create_project_confirm_button").performClick()

        // Should now be inside the Editor shell
        composeTestRule.onNodeWithTag("editor_back_button").assertExists()
        composeTestRule.onNodeWithTag("video_preview_area").assertExists()
        composeTestRule.onNodeWithTag("playback_bar").assertExists()
        composeTestRule.onNodeWithTag("timeline_container").assertExists()
        composeTestRule.onNodeWithText("Your timeline is empty").assertExists()

        // Tapping Add Media should show Phase 1 Notice
        composeTestRule.onNodeWithTag("timeline_add_media_button").performClick()
        composeTestRule.onNodeWithText("Media import is coming in Phase 1.").assertExists()
        composeTestRule.onNodeWithTag("close_phase1_dialog_button").performClick()

        // Go back to Home
        composeTestRule.onNodeWithTag("editor_back_button").performClick()
        composeTestRule.onNodeWithText("My Projects").assertExists()
    }

    @Test
    fun `verify screen rotation controls on home and editor screens`() {
        composeTestRule.setContent {
            MADCreativesStudioApp()
        }

        // Verify rotation button is accessible on Home top bar
        composeTestRule.onNodeWithTag("home_rotate_button").assertExists().assertIsDisplayed()

        // Create a project to enter Editor screen
        composeTestRule.onNodeWithTag("empty_state_new_project_button").performClick()
        composeTestRule.onNodeWithTag("create_project_confirm_button").performClick()

        // Verify rotation button is accessible on Editor top bar
        composeTestRule.onNodeWithTag("editor_rotate_button").assertExists().assertIsDisplayed()

        // Verify more menu has rotation options
        composeTestRule.onNodeWithTag("editor_more_button").performClick()
        composeTestRule.onNodeWithTag("toggle_orientation_menu_item").assertExists()
        composeTestRule.onNodeWithTag("sensor_orientation_menu_item").assertExists()
    }
}
