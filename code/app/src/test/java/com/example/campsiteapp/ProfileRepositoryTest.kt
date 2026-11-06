package com.example.campsiteapp

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.CollectionReference
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class ProfileRepositoryTest {

    private val ref = mockk<CollectionReference>(relaxed = true)
    private val repo = ProfileRepository(ref)

    @After
    fun tearDown() {
        AppUser.profile = null
    }

    // This photo implementation was provided via Codex (2026-11-05):
    // "Provide implementation for profile viewing and saving a profile picture using BitMap, LocalContext, and the Gallery"
    @Test
    fun saveProfilePictureUpdatesTheProfileAfterFirestoreConfirms() {
        val profile = Profile("u1")
        val picture = mockk<Blob>()
        val write = mockk<Task<Void>>(relaxed = true)
        val success = slot<OnSuccessListener<Void>>()
        every { ref.document("u1").update("profilePicture", picture) } returns write
        every { write.addOnSuccessListener(capture(success)) } returns write
        var saved: Boolean? = null

        repo.saveProfilePicture(profile, picture) { saved = it }

        assertNull(profile.profilePicture)
        assertNull(saved)
        success.captured.onSuccess(null)

        assertSame(picture, profile.profilePicture)
        assertEquals(true, saved)
    }

    // This photo implementation was provided via Codex (2026-11-05):
    // "Provide implementation for profile viewing and saving a profile picture using BitMap, LocalContext, and the Gallery"
    @Test
    fun failedPhotoSaveKeepsThePreviousPicture() {
        val previous = mockk<Blob>()
        val profile = Profile("u1", profilePicture = previous)
        val replacement = mockk<Blob>()
        val write = mockk<Task<Void>>(relaxed = true)
        val failure = slot<OnFailureListener>()
        every { ref.document("u1").update("profilePicture", replacement) } returns write
        every { write.addOnSuccessListener(any()) } returns write
        every { write.addOnFailureListener(capture(failure)) } returns write
        var saved: Boolean? = null

        repo.saveProfilePicture(profile, replacement) { saved = it }
        failure.captured.onFailure(IllegalStateException("Write failed"))

        assertSame(previous, profile.profilePicture)
        assertEquals(false, saved)
    }

    @Test
    fun deleteProfileRemovesItFromFirestore() {
        repo.deleteProfile(Profile("u1"))
        verify { ref.document("u1").delete() }
    }

    @Test
    fun deleteProfileSignsOutCurrentUser() {
        val profile = Profile("u1", "Alex P.", "alex@example.com")
        AppUser.profile = profile
        repo.deleteProfile(profile)
        assertNull(AppUser.profile)
    }

    @Test
    fun deleteOtherProfileKeepsCurrentUserSignedIn() {
        val current = Profile("u2", "Jane D.", "jane@example.com")
        AppUser.profile = current
        repo.deleteProfile(Profile("u1"))
        assertEquals(current, AppUser.profile)
    }

    @Test
    fun deleteProfileRemovesItFromProfilesList() {
        repo.deleteProfile(Profile("u1"))
        assertTrue(repo.profiles.none { it.uid == "u1" })
    }

    @Test
    fun deleteProfileOnlyDeletesThatProfile() {
        repo.deleteProfile(Profile("u1"))
        verify(exactly = 0) { ref.document("u2") }
    }
}
