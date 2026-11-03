package com.example.campsiteapp

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var saveCalled = false
    private var savedName = ""
    private var savedEmail = ""
    private var savedPhone = ""

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ProfileScreen(
                name = "Alex P.",
                email = "alex@example.com",
                phone = "780-555-0123",
                onSave = { name, email, phone ->
                    saveCalled = true
                    savedName = name
                    savedEmail = email
                    savedPhone = phone
                },
                onBack = {}
            )
        }
    }

    @Test
    fun profile_shows_current_information() {
        composeTestRule
            .onNodeWithText("Alex P.")
            .assertExists()

        composeTestRule
            .onNodeWithText("alex@example.com")
            .assertExists()

        composeTestRule
            .onNodeWithText("780-555-0123")
            .assertExists()
    }

    @Test
    fun update_name_and_email_saves_and_shows_confirmation() {
        composeTestRule
            .onNodeWithTag("nameInput")
            .performTextClearance()

        composeTestRule
            .onNodeWithTag("nameInput")
            .performTextInput("Jordan M.")

        composeTestRule
            .onNodeWithTag("emailInput")
            .performTextClearance()

        composeTestRule
            .onNodeWithTag("emailInput")
            .performTextInput("jordan@example.com")

        composeTestRule
            .onNodeWithTag("saveButton")
            .performClick()

        assertTrue(saveCalled)
        assertEquals("Jordan M.", savedName)
        assertEquals("jordan@example.com", savedEmail)
        assertEquals("780-555-0123", savedPhone)

        composeTestRule
            .onNodeWithText("Profile saved")
            .assertExists()
    }

    @Test
    fun invalid_email_shows_error_and_does_not_save() {
        composeTestRule
            .onNodeWithTag("emailInput")
            .performTextClearance()

        composeTestRule
            .onNodeWithTag("emailInput")
            .performTextInput("not-an-email")

        composeTestRule
            .onNodeWithTag("saveButton")
            .performClick()

        assertFalse(saveCalled)

        composeTestRule
            .onNodeWithText("Enter a valid email")
            .assertExists()

        composeTestRule
            .onNodeWithText("Profile saved")
            .assertDoesNotExist()
    }

    @Test
    fun invalid_phone_shows_error_and_does_not_save() {
        composeTestRule
            .onNodeWithTag("phoneInput")
            .performTextClearance()

        composeTestRule
            .onNodeWithTag("phoneInput")
            .performTextInput("12")

        composeTestRule
            .onNodeWithTag("saveButton")
            .performClick()

        assertFalse(saveCalled)

        composeTestRule
            .onNodeWithText("Enter a valid phone number")
            .assertExists()
    }

    @Test
    fun empty_name_shows_error_and_does_not_save() {
        composeTestRule
            .onNodeWithTag("nameInput")
            .performTextClearance()

        composeTestRule
            .onNodeWithTag("saveButton")
            .performClick()

        assertFalse(saveCalled)

        composeTestRule
            .onNodeWithText("Name can't be empty")
            .assertExists()
    }
}
