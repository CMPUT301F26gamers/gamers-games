package com.example.campsiteapp

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*


@RunWith(AndroidJUnit4::class)
class BrowseProfilesScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val profiles = listOf(
        Profile(uid = "1", name = "Alex P.", email = "AlexEmail"),
        Profile(uid = "2", name = "Jane D.", email = "JaneEmail"),
        Profile(uid = "3", name = "Sam K.", email = "SamEmail"),
        Profile(uid = "4", name = "Mark T.", email = "MarkEmail")
    )

    @Test
    fun testProfilesDisplayed() {
        composeTestRule.setContent {
            BrowseProfilesScreen(profiles, {}, {})
        }

        composeTestRule.onNodeWithText("Alex").assertExists()
        composeTestRule.onNodeWithText("Jane").assertExists()
        composeTestRule.onNodeWithText("Sam").assertExists()
    }

    @Test
    fun testSearch() {
        composeTestRule.setContent {
            BrowseProfilesScreen(profiles, {}, {})
        }

        composeTestRule.onNodeWithTag("profileSearch")
            .performTextInput("Alex")

        composeTestRule.onNodeWithText("Alex").assertExists()
        composeTestRule.onNodeWithText("Jane").assertDoesNotExist()
        composeTestRule.onNodeWithText("Sam").assertDoesNotExist()
    }

    @Test
    fun testViewProfile() {
        var viewedProfile: Profile? = null

        composeTestRule.setContent {
            BrowseProfilesScreen(
                profiles,
                onDeleteProfile = {},
                onViewProfile = { viewedProfile = it }
            )
        }

        composeTestRule.onNodeWithText("Alex").performClick()

        assertEquals("AlexEmail", viewedProfile?.email)
    }

    @Test
    fun testRemoveProfile() {
        var deletedProfile: Profile? = null

        composeTestRule.setContent {
            BrowseProfilesScreen(
                profiles,
                onDeleteProfile = { deletedProfile = it },
                onViewProfile = {}
            )
        }

        composeTestRule.onNodeWithTag("removeButton_JaneEmail")
            .performClick()

        composeTestRule.onNodeWithTag("confirmRemove")
            .performClick()

        assertEquals("JaneEmail", deletedProfile?.email)
    }

    @Test
    fun testCancelRemove() {
        var deletedProfile: Profile? = null

        composeTestRule.setContent {
            BrowseProfilesScreen(
                profiles,
                onDeleteProfile = { deletedProfile = it },
                onViewProfile = {}
            )
        }

        composeTestRule.onNodeWithTag("removeButton_1")
            .performClick()

        composeTestRule.onNodeWithTag("cancelRemove")
            .performClick()

        assertNull(deletedProfile)
    }
}