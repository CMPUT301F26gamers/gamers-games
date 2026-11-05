package com.example.campsiteapp

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CampsiteDetailsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        val campsiteRepo = CampsiteRepositoryStud()
        composeTestRule.setContent {
            CampsiteDetailsScreen(
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
    fun test_user_details_buttons() {
        composeTestRule
            .onNodeWithTag("lotteryButton")
            .assertExists()

        composeTestRule
            .onNodeWithTag("lotteryButton")
            .performClick()

        composeTestRule
            .onNodeWithTag("rateButton")
            .assertExists()

        composeTestRule
            .onNodeWithTag("rateButton")
            .performClick()
    }
}