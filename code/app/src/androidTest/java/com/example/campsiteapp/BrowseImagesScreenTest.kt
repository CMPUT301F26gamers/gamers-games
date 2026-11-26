package com.example.campsiteapp

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.firestore.Blob
import java.io.ByteArrayOutputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BrowseImagesScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val picture = createPicture()

    private val testingProfiles = mutableStateListOf(
        Profile(uid = "1", name = "Alex P.", profilePicture = picture),
        Profile(uid = "2", name = "Jane D.", profilePicture = picture),
        Profile(uid = "3", name = "Sam K.")
    )
    private var deletedProfile: Profile? = null
    private var sentMessage: String? = null

    private fun createPicture(): Blob {
        val bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bytes)
        bitmap.recycle()
        return Blob.fromBytes(bytes.toByteArray())
    }

    @Before
    fun setUp() {
        composeTestRule.setContent {
            BrowseImagesScreen(
                navController = rememberNavController(),
                profiles = testingProfiles,
                onDeleteImage = { profile, message ->
                    deletedProfile = profile
                    sentMessage = message
                    val index = testingProfiles.indexOf(profile)
                    testingProfiles[index] = Profile(uid = profile.uid, name = profile.name)
                }
            )
        }
    }

    @Test
    fun testOnlyProfilesWithImagesDisplayed() {
        composeTestRule.onNodeWithText("Alex P.").assertExists()
        composeTestRule.onNodeWithText("Jane D.").assertExists()
        composeTestRule.onNodeWithText("Sam K.").assertDoesNotExist()
    }

    @Test
    fun testDeleteButtonDisabledUntilImageSelected() {
        composeTestRule.onNodeWithTag("deleteImageButton").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("imageRow_1").performClick()
        composeTestRule.onNodeWithTag("deleteImageButton").assertIsEnabled()
        composeTestRule.onNodeWithTag("imageRow_1").performClick()
        composeTestRule.onNodeWithTag("deleteImageButton").assertIsNotEnabled()
    }

    @Test
    fun testRemoveImage() {
        composeTestRule.onNodeWithTag("imageRow_2").performClick()
        composeTestRule.onNodeWithTag("deleteImageButton").performClick()
        composeTestRule.onNodeWithText("Are you sure you want to remove Jane D.'s image?").assertExists()
        composeTestRule.runOnIdle { assertNull(deletedProfile) }
        composeTestRule.onNodeWithTag("confirmDeleteImage").performClick()
        composeTestRule.runOnIdle {
            assertEquals("2", deletedProfile?.uid)
            assertEquals("", sentMessage)
        }
        composeTestRule.onNodeWithText("Jane D.").assertDoesNotExist()
        composeTestRule.onNodeWithText("Alex P.").assertExists()
        composeTestRule.onNodeWithTag("deleteImageButton").assertIsNotEnabled()
    }

    @Test
    fun testRemoveImageWithMessage() {
        composeTestRule.onNodeWithTag("imageRow_1").performClick()
        composeTestRule.onNodeWithTag("deleteImageButton").performClick()
        composeTestRule.onNodeWithTag("deleteMessageField").performTextInput("Inappropriate image")
        composeTestRule.onNodeWithTag("confirmDeleteImage").performClick()
        composeTestRule.runOnIdle {
            assertEquals("1", deletedProfile?.uid)
            assertEquals("Inappropriate image", sentMessage)
        }
    }

    @Test
    fun testCancelRemoveImage() {
        composeTestRule.onNodeWithTag("imageRow_1").performClick()
        composeTestRule.onNodeWithTag("deleteImageButton").performClick()
        composeTestRule.onNodeWithTag("cancelDeleteImage").performClick()
        composeTestRule.runOnIdle { assertNull(deletedProfile) }
        composeTestRule.onNodeWithText("Alex P.").assertExists()
        composeTestRule.onNodeWithTag("confirmDeleteImage").assertDoesNotExist()
    }
}
