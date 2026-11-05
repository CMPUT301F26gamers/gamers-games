package com.example.campsiteapp

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithText
import org.junit.Before
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class PolicyAndGuidelinesScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    // 1. Declare the variable here so both @Before and @Test can see it
    private var backClicked = false

    @Before
    fun setup() {
        backClicked = false

        // Render the screen ONCE, and capture the click
        composeTestRule.setContent {
            Policy_GuidelinesScreen(
                onBack = { backClicked = true }
            )
        }
    }

    @Test
    fun test_content_is_displayed() {
        // Verify the title exists
        composeTestRule.onNodeWithTag("screenTitle").assertExists()

        // Verify the text exists
        composeTestRule.onNodeWithText("RANDOM TEXT GO", substring = true).assertExists()
    }

    @Test
    fun test_back_button() {
        // Click the back button
        composeTestRule.onNodeWithTag("backButton").performClick()

        // Verify the callback was called
        assertTrue(backClicked)
    }
}