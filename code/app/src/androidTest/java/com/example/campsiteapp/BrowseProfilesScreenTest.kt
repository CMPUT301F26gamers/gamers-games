package com.example.campsiteapp

import androidx.compose.runtime.mutableStateListOf
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BrowseProfilesScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testingProfiles = mutableStateListOf(
        Profile(uid = "1", name = "Alex P.", email = "AlexEmail"),
        Profile(uid = "2", name = "Jane D.", email = "JaneEmail"),
        Profile(uid = "3", name = "Sam K.", email = "SamEmail"),
        Profile(uid = "4", name = "Mark T.", email = "MarkEmail")
    )
    private var deletedProfile: Profile? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            BrowseProfilesScreen(
                navController = rememberNavController(),
                profiles = testingProfiles,
                onDeleteProfile = { profile ->
                    deletedProfile = profile
                    testingProfiles.remove(profile)
                }
            )
        }
    }

    @Test
    fun testProfilesDisplayed() {
        composeTestRule.onNodeWithText("Alex P.").assertExists()
        composeTestRule.onNodeWithText("Jane D.").assertExists()
        composeTestRule.onNodeWithText("Sam K.").assertExists()
        composeTestRule.onNodeWithText("Mark T.").assertExists()
    }

    @Test
    fun testSearch() {
        composeTestRule.onNodeWithTag("searchField").performTextInput("aLeX")
        composeTestRule.onNodeWithText("Alex P.").assertExists()
        composeTestRule.onNodeWithText("Jane D.").assertDoesNotExist()
        composeTestRule.onNodeWithText("Sam K.").assertDoesNotExist()
        composeTestRule.onNodeWithText("Mark T.").assertDoesNotExist()
    }

    @Test
    fun testRemoveProfile() {
        composeTestRule.onNodeWithTag("removeProfileButton_2").performClick()
        composeTestRule.onNodeWithText("Are you sure you want to remove Jane D.?").assertExists()
        composeTestRule.runOnIdle { assertNull(deletedProfile) }
        composeTestRule.onNodeWithTag("confirmRemove").performClick()
        composeTestRule.runOnIdle {
            assertEquals("2", deletedProfile?.uid)
        }
        composeTestRule.onNodeWithText("Jane D.").assertDoesNotExist()
        composeTestRule.onNodeWithText("Alex P.").assertExists()
        composeTestRule.onNodeWithTag("confirmRemove").assertDoesNotExist()
    }

    @Test
    fun testCancelRemove() {
        composeTestRule.onNodeWithTag("removeProfileButton_1").performClick()
        composeTestRule.onNodeWithTag("cancelRemove").performClick()
        composeTestRule.runOnIdle { assertNull(deletedProfile) }
        composeTestRule.onNodeWithText("Alex P.").assertExists()
        composeTestRule.onNodeWithTag("confirmRemove").assertDoesNotExist()
    }
}