package com.example.campsiteapp

import com.google.firebase.firestore.CollectionReference
import io.mockk.mockk
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
