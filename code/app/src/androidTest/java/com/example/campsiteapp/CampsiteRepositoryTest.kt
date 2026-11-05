package com.example.campsiteapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import junit.framework.TestCase
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class CampsiteRepositoryTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var repository: CampsiteRepository

    companion object {
        private var isEmulatorConfigured = false
    }

    @Before
    fun setUp() {
        firestore = FirebaseFirestore.getInstance()

        if (!isEmulatorConfigured) {
            firestore.useEmulator("10.0.2.2", 8080)

            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                .build()
            firestore.firestoreSettings = settings

            isEmulatorConfigured = true
        }

        repository = CampsiteRepository()
    }

    @After
    fun tearDown() {
        val latch = CountDownLatch(1)
        firestore.collection("campsites").get().addOnSuccessListener { snapshot ->
            val batch = firestore.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().addOnCompleteListener { latch.countDown() }
        }
        latch.await(5, TimeUnit.SECONDS)
    }

    @Test
    fun addCampsite_updatesCampsitesList() {
        val testCampsite = Campsite(id = "site_1", name = "Pine Ridge", location = "Sector 4", date = Date(), numGuests = 2)
        val latch = CountDownLatch(1)

        repository.addCampsite(testCampsite)

        firestore.collection("campsites").document("site_1")
            .addSnapshotListener { _, _ ->
                if (repository.campsites.any { it.id == "site_1" }) {
                    latch.countDown()
                }
            }

        val reachedZero = latch.await(3, TimeUnit.SECONDS)

        TestCase.assertTrue("SnapshotListener did not update the list in time", reachedZero)
        TestCase.assertEquals(1, repository.campsites.size)
        TestCase.assertEquals("Pine Ridge", repository.campsites.first().name)
    }

    @Test
    fun removeCampsite_clearsFromCampsitesList() = runTest {
        val testCampsite = Campsite(id = "site_2", name = "Lakeview", location = "Sector 2")
        repository.addCampsite(testCampsite)

        repository.removeCampsite(testCampsite)

        assertTrue("Repository campsites list did not empty out", repository.campsites.isEmpty())
    }

}