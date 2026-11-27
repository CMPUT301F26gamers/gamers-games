package com.example.campsiteapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.compose.ui.test.performScrollTo


class OwnerCampsiteDetailsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        val campsiteRepo = CampsiteRepositoryStud()
        composeTestRule.setContent {
            OwnerCampsiteDetailsScreen(
                campsite = Campsite(
                    "1",
                    "Balls"
                ),
                profile = Profile(
                    uid = "1"
                ),
                onBack = {},
                onUpdateDetails = { _, _ ->}
            )
        }
    }

    @Test
    fun test_owner_details_buttons() {
        composeTestRule
            .onNodeWithTag("editButton")
            .performScrollTo()
            .assertExists()

        composeTestRule
            .onNodeWithTag("editButton")
            .performScrollTo()
            .performClick()

        composeTestRule
            .onNodeWithTag("newName")
            .performScrollTo()
            .assertExists()

        composeTestRule
            .onNodeWithTag("newName")
            .performTextInput("a")

        composeTestRule
            .onNodeWithTag("newLocation")
            .performScrollTo()
            .assertExists()

        composeTestRule
            .onNodeWithTag("newLocation")
            .performTextInput("b")

        composeTestRule
            .onNodeWithTag("newOccupancy")
            .performScrollTo()
            .assertExists()

        composeTestRule
            .onNodeWithTag("newOccupancy")
            .performTextInput("5")

        composeTestRule
            .onNodeWithTag("newDescription")
            .performScrollTo()
            .assertExists()

        composeTestRule
            .onNodeWithTag("newDescription")
            .performTextInput("Yapping yapping...")

        composeTestRule
            .onNodeWithTag("confirmButton")
            .performScrollTo()
            .assertExists()

        composeTestRule
            .onNodeWithTag("confirmButton")
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag("confirmButton")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithTag("campsiteName")
            .performScrollTo()
            .assertExists()

        composeTestRule
            .onNodeWithTag("campsiteName")
            .assertTextEquals("Name: a")

        composeTestRule
            .onNodeWithTag("campsiteLocation")
            .performScrollTo()
            .assertExists()

        composeTestRule
            .onNodeWithTag("campsiteLocation")
            .assertTextEquals("Location: b")

        composeTestRule
            .onNodeWithTag("campsiteOccupancy")
            .performScrollTo()
            .assertExists()

        composeTestRule
            .onNodeWithTag("campsiteOccupancy")
            .assertTextEquals("Maximum Occupancy: 5")

        composeTestRule
            .onNodeWithTag("campsiteDescription")
            .performScrollTo()
            .assertExists()

        composeTestRule
            .onNodeWithTag("campsiteDescription")
            .assertTextEquals("Yapping yapping...")
    }

    @Test
    fun qrCode_isDisplayedForCampsite() {
        composeTestRule
            .onNodeWithText("Campsite QR Code")
            .assertExists()

        composeTestRule
            .onNodeWithTag("campsiteQrCodeImage")
            .assertExists()
            .assertIsDisplayed()
    }
}
