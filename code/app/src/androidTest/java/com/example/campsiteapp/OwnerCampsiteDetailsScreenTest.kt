package com.example.campsiteapp

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
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
                onBack = {}
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
    }
}