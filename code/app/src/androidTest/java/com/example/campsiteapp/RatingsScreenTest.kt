package com.example.campsiteapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.campsiteapp.ui.theme.CampsiteAppTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RatingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var currentProfile by mutableStateOf(Profile(uid = "1"))

    @Before
    fun setUp() {
        val campsite: Campsite = Campsite("1", "Camp Melody")
        campsite.addRating("1", 1)
        campsite.addRating("2", 2, "This Campsite is ass")
        campsite.addRating("3", 5, "The GOAT")
        composeTestRule.setContent {
            RatingsScreen(
                campsite = campsite,
                profile = currentProfile,
                onBack = { }
            )
        }
    }

    @Test
    fun test_entrant() {
        composeTestRule
            .onNodeWithText("Rating: 1")
            .assertExists()

        composeTestRule
            .onNodeWithText("Rating: 2")
            .assertExists()

        composeTestRule
            .onNodeWithText("Rating: 3")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText("Rating: 5")
            .assertExists()

        composeTestRule
            .onNodeWithText("Camp Melody Ratings")
            .assertExists()

        composeTestRule
            .onNodeWithText("This Campsite is ass")
            .assertExists()

        composeTestRule
            .onNodeWithText("The GOAT")
            .assertExists()

        composeTestRule
            .onNodeWithTag("remove_rating_1")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithTag("remove_rating_2")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithTag("remove_rating_3")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithTag("remove_review_1")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithTag("remove_review_2")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithTag("remove_review_3")
            .assertDoesNotExist()
    }

    @Test
    fun test_admin() {
        currentProfile = Profile(uid = "1").apply {
            addRole(Roles.ADMIN)
        }

        composeTestRule
            .onNodeWithTag("remove_rating_1")
            .assertExists()

        composeTestRule
            .onNodeWithTag("remove_rating_2")
            .assertExists()

        composeTestRule
            .onNodeWithTag("remove_rating_3")
            .assertExists()

        composeTestRule
            .onNodeWithTag("remove_review_1")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithTag("remove_review_2")
            .assertExists()

        composeTestRule
            .onNodeWithTag("remove_review_3")
            .assertExists()

        composeTestRule
            .onNodeWithTag("remove_rating_1")
            .performClick()

        composeTestRule
            .onNodeWithText("Rating: 1")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithTag("remove_review_2")
            .performClick()

        composeTestRule
            .onNodeWithText("Rating: 2")
            .assertExists()

        composeTestRule
            .onNodeWithText("This Campsite is ass")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithTag("remove_rating_3")
            .performClick()

        composeTestRule
            .onNodeWithText("Rating: 5")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText("The GOAT")
            .assertDoesNotExist()
    }
}