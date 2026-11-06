package com.example.campsiteapp

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Before
import org.junit.Rule
import org.junit.Test


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
            .assertExists()

        composeTestRule
            .onNodeWithTag("editButton")
            .performClick()

        composeTestRule
            .onNodeWithTag("newName")
            .assertExists()

        composeTestRule
            .onNodeWithTag("newName")
            .performTextInput("a")

        composeTestRule
            .onNodeWithTag("newLocation")
            .assertExists()

        composeTestRule
            .onNodeWithTag("newLocation")
            .performTextInput("b")

        composeTestRule
            .onNodeWithTag("newOccupancy")
            .assertExists()

        composeTestRule
            .onNodeWithTag("newOccupancy")
            .performTextInput("5")

        composeTestRule
            .onNodeWithTag("newDescription")
            .assertExists()

        composeTestRule
            .onNodeWithTag("newDescription")
            .performTextInput("Yapping yapping...")

        composeTestRule
            .onNodeWithTag("confirmButton")
            .assertExists()

        composeTestRule
            .onNodeWithTag("confirmButton")
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag("confirmButton")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithTag("campsiteName")
            .assertExists()

        composeTestRule
            .onNodeWithTag("campsiteName")
            .assertTextEquals("Name: a")

        composeTestRule
            .onNodeWithTag("campsiteLocation")
            .assertExists()

        composeTestRule
            .onNodeWithTag("campsiteLocation")
            .assertTextEquals("Location: b")

        composeTestRule
            .onNodeWithTag("campsiteOccupancy")
            .assertExists()

        composeTestRule
            .onNodeWithTag("campsiteOccupancy")
            .assertTextEquals("Maximum Occupancy: 5")

        composeTestRule
            .onNodeWithTag("campsiteDescription")
            .assertExists()

        composeTestRule
            .onNodeWithTag("campsiteDescription")
            .assertTextEquals("Yapping yapping...")
    }
}