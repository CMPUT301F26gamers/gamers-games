package com.example.campsiteapp

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests US 02.03.01: View a list of my campsites.
 */
@RunWith(AndroidJUnit4::class)
class OwnerCampsiteListTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Variables to track callback events
    private var backClicked = false
    private var clickedCampsite = Campsite(name = "NONE")

    // Dummy data to test the UI
    private val dummyCampsites = listOf(
        Campsite(name = "Banff Site"),
        Campsite(name = "Jasper Site")
    )

    @Before
    fun setup() {
        // Reset tracking variables before each test
        backClicked = false
        clickedCampsite = Campsite(name = "NONE")

        // Render the screen ONCE
        composeTestRule.setContent {
            OwnerCampsitesScreen(
                campsites = dummyCampsites,
                onBack = { backClicked = true },
                onCampsiteClick = { clickedCampsite = it }
            )
        }
    }

    @Test
    fun test_screen_displays_campsites_and_title() {
        // Verify the title exists
        composeTestRule.onNodeWithTag("screenTitle").assertExists()

        // Verify the dummy campsites are displayed in the list
        composeTestRule.onNodeWithText("Banff Site").assertExists()
        composeTestRule.onNodeWithText("Jasper Site").assertExists()
    }

    @Test
    fun test_back_button_triggers_callback() {
        // Click the back button
        composeTestRule.onNodeWithTag("backButton").performClick()

        // Verify the callback was triggered
        assertTrue(backClicked)
    }

    @Test
    fun test_clicking_campsite_triggers_navigation() {
        // Click the first campsite by its text
        composeTestRule.onNodeWithText("Banff Site").performClick()

        // Verify the campsite was clicked
        assertEquals("Banff Site", clickedCampsite.name)
    }
}