package com.example.campsiteapp

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterCampsiteTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var registerCalled = false
    private var savedName = ""
    private var savedLocation = ""
    private var savedMaxGuests = 0
    private var savedDescription = ""
    private var savedTags = listOf<String>()

    private fun showScreen() {
        composeTestRule.setContent {
            RegisterCampsiteScreen(
                onRegister = { name, location, maxGuests, description, tags ->
                    registerCalled = true
                    savedName = name
                    savedLocation = location
                    savedMaxGuests = maxGuests
                    savedDescription = description
                    savedTags = tags
                },
                onBack = {}
            )
        }
    }

    private fun pressRegister() {
        composeTestRule.onNodeWithTag("registerButton").performScrollTo().performClick()
    }

    @Test
    fun test_register_campsite() {
        var savedName = ""
        var savedLocation = ""
        var backClicked = false

        composeTestRule.setContent {
            RegisterCampsiteScreen(
                onRegister = { name, location, _, _, _ ->
                    savedName = name
                    savedLocation = location
                },
                onBack = { backClicked = true }
            )
        }

        // Fill out the fields
        composeTestRule.onNodeWithTag("nameInput").performTextInput("My Camp")
        composeTestRule.onNodeWithTag("locationInput").performTextInput("Banff")
        composeTestRule.onNodeWithTag("maxGuestsInput").performTextInput("4")

        // Click the Register button
        composeTestRule.onNodeWithTag("registerButton").performClick()

        // Verify the fields were saved correctly
        assertEquals("My Camp", savedName)
        assertEquals("Banff", savedLocation)
    }

    @Test
    fun all_campsite_detail_fields_are_shown() {
        showScreen()

        composeTestRule.onNodeWithTag("nameInput").assertExists()
        composeTestRule.onNodeWithTag("locationInput").assertExists()
        composeTestRule.onNodeWithTag("maxGuestsInput").assertExists()
        composeTestRule.onNodeWithTag("descriptionInput").assertExists()
        composeTestRule.onNodeWithTag("tagsInput").assertExists()
    }

    @Test
    fun register_sends_all_details() {
        showScreen()

        composeTestRule.onNodeWithTag("nameInput").performTextInput("Pine Ridge")
        composeTestRule.onNodeWithTag("locationInput").performTextInput("Jasper, AB")
        composeTestRule.onNodeWithTag("maxGuestsInput").performTextInput("6")
        composeTestRule.onNodeWithTag("descriptionInput").performTextInput("Fire pit, picnic table, washrooms")
        composeTestRule.onNodeWithTag("tagsInput").performTextInput("lakeside, pet friendly")
        pressRegister()

        assertTrue(registerCalled)
        assertEquals("Pine Ridge", savedName)
        assertEquals("Jasper, AB", savedLocation)
        assertEquals(6, savedMaxGuests)
        assertEquals("Fire pit, picnic table, washrooms", savedDescription)
        assertEquals(listOf("lakeside", "pet friendly"), savedTags)
    }

    @Test
    fun description_and_tags_are_optional() {
        showScreen()

        composeTestRule.onNodeWithTag("nameInput").performTextInput("Pine Ridge")
        composeTestRule.onNodeWithTag("locationInput").performTextInput("Jasper, AB")
        composeTestRule.onNodeWithTag("maxGuestsInput").performTextInput("2")
        pressRegister()

        assertTrue(registerCalled)
        assertEquals("", savedDescription)
        assertTrue(savedTags.isEmpty())
    }

    @Test
    fun empty_form_shows_errors_and_does_not_register() {
        showScreen()

        pressRegister()

        composeTestRule.onNodeWithText("Name can't be empty").assertExists()
        composeTestRule.onNodeWithText("Location can't be empty").assertExists()
        composeTestRule.onNodeWithText("Max guests must be a whole number of 1 or more").assertExists()
        assertFalse(registerCalled)
    }

    @Test
    fun zero_max_guests_shows_error() {
        showScreen()

        composeTestRule.onNodeWithTag("nameInput").performTextInput("Pine Ridge")
        composeTestRule.onNodeWithTag("locationInput").performTextInput("Jasper, AB")
        composeTestRule.onNodeWithTag("maxGuestsInput").performTextInput("0")
        pressRegister()

        composeTestRule.onNodeWithText("Max guests must be a whole number of 1 or more").assertExists()
        assertFalse(registerCalled)
    }
}
