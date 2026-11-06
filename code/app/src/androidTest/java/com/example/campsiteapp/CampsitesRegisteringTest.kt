package com.example.campsiteapp

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterCampsiteTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun test_register_campsite() {
        var savedName = ""
        var savedLocation = ""
        var backClicked = false

        composeTestRule.setContent {
            RegisterCampsiteScreen(
                onRegister = { name, location ->
                    savedName = name
                    savedLocation = location
                },
                onBack = { backClicked = true }
            )
        }

        // Fill out the fields
        composeTestRule.onNodeWithTag("nameInput").performTextInput("My Camp")
        composeTestRule.onNodeWithTag("locationInput").performTextInput("Banff")

        // Click the Register button
        composeTestRule.onNodeWithTag("registerButton").performClick()

        // Verify the fields were saved correctly
        assertEquals("My Camp", savedName)
        assertEquals("Banff", savedLocation)
    }
}