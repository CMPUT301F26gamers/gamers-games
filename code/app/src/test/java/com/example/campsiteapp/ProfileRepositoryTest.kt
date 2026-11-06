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
import io.mockk.verifyOrder
import org.junit.After
import org.junit.Assert.*
import org.junit.Test

class ProfileRepositoryTest {

    private val ref = mockk<CollectionReference>(relaxed = true)
    private val repo = ProfileRepository(ref)
    private val campsiteRepo = mockk<CampsiteRepo>(relaxUnitFun = true)
    private val lotteryRepo = mockk<LotteryRepository>(relaxUnitFun = true)

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

    // These tests were written by codex (2026-11-05): "Write test for the cascade deletion of the profiles
    @Test
    fun unlinksOwnedCampsitesFromLotteriesBeforeDeletingTheProfile() {
        val profile = Profile(uid = "owner")
        val lake = Campsite(id = "lake", ownerId = profile.uid)
        val forest = Campsite(id = "forest", ownerId = profile.uid)
        val lottery = Lottery(id = "lottery")
        every { campsiteRepo.getCampsitesByOwner(profile.uid) } returns listOf(lake, forest)
        every { lotteryRepo.getLotteries(lake.id) } returns listOf(lottery)
        every { lotteryRepo.getLotteries(forest.id) } returns emptyList()

        repo.cascadeDeleteProfile(profile, campsiteRepo, lotteryRepo)

        verifyOrder {
            campsiteRepo.getCampsitesByOwner(profile.uid)
            lotteryRepo.getLotteries(lake.id)
            lotteryRepo.removeCampsite(lottery.id, lake.id, any())
            campsiteRepo.removeCampsite(lake)
            lotteryRepo.getLotteries(forest.id)
            campsiteRepo.removeCampsite(forest)
            ref.document(profile.uid).delete()
        }
    }

    @Test
    fun removesProfileEvenWhenItOwnsNoCampsites() {
        val profile = Profile(uid = "entrant")
        every { campsiteRepo.getCampsitesByOwner(profile.uid) } returns emptyList()

        repo.cascadeDeleteProfile(profile, campsiteRepo, lotteryRepo)

        verifyOrder {
            campsiteRepo.getCampsitesByOwner(profile.uid)
            ref.document(profile.uid).delete()
        }
    }
}