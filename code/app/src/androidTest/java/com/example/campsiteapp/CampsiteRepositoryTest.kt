package com.example.campsiteapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class CampsiteRepositoryTest {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var repository: CampsiteRepository

    companion object {
        private var isEmulatorConfigured = false
    }

    @Before
    fun setUp() = runBlocking {
        firestore = FirebaseFirestore.getInstance()

        try {
            firestore.useEmulator("10.0.2.2", 8080)
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                .build()
            firestore.firestoreSettings = settings
        } catch (e: IllegalStateException) {
        }

        val snapshot = firestore.collection("campsites").get().await()
        for (doc in snapshot.documents) {
            doc.reference.delete().await()
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
    fun removeCampsite_clearsFromCampsitesList() = runBlocking {
        val testCampsite = Campsite(id = "site_2", name = "Lakeview", location = "Sector 2")

        repository.addCampsite(testCampsite)

        val addLatch = CountDownLatch(1)
        val addRegistration = firestore.collection("campsites")
            .addSnapshotListener { _, _ ->
                if (repository.campsites.any { it.id == "site_2" }) {
                    addLatch.countDown()
                }
            }
        addLatch.await(3, TimeUnit.SECONDS)
        addRegistration.remove()

        val removeLatch = CountDownLatch(1)
        val removeRegistration = firestore.collection("campsites")
            .addSnapshotListener { _, _ ->
                if (repository.campsites.none { it.id == "site_2" }) {
                    removeLatch.countDown()
                }
            }

        repository.removeCampsite(testCampsite)

        val reachedZero = removeLatch.await(3, TimeUnit.SECONDS)
        removeRegistration.remove()

        assertTrue("SnapshotListener did not update on remove in time", reachedZero)
        assertTrue("Site 2 was not removed from campsites list", repository.campsites.none { it.id == "site_2" })
    }

    @Test
    fun campsitesGetter_filtersByNameSearchQuery() = runBlocking {
        val site1 = Campsite(id = "site_1", name = "Pine Ridge", location = "Sector 4")
        val site2 = Campsite(id = "site_2", name = "Pines Lake", location = "Sector 1")
        val site3 = Campsite(id = "site_3", name = "Lakeview", location = "Sector 2")

        val latch = CountDownLatch(1)
        val registration = firestore.collection("campsites")
            .addSnapshotListener { _, _ ->
                if (repository.campsites.size == 3) {
                    latch.countDown()
                }
            }

        repository.addCampsite(site1)
        repository.addCampsite(site2)
        repository.addCampsite(site3)

        val reachedThree = latch.await(3, TimeUnit.SECONDS)
        registration.remove()

        assertTrue("SnapshotListener did not sync campsites in time", reachedThree)

        repository.searchQuery = ""
        assertEquals(3, repository.campsites.size)

        repository.searchQuery = "Pin"
        assertEquals(2, repository.campsites.size)
        assertTrue(repository.campsites.any { it.name == "Pine Ridge" })
        assertTrue(repository.campsites.any { it.name == "Pines Lake" })

        repository.searchQuery = "lake"
        assertEquals(2, repository.campsites.size)
        assertTrue(repository.campsites.any { it.name == "Lakeview" })

        repository.searchQuery = "Mountain"
        assertTrue(repository.campsites.isEmpty())
    }

    @Test
    fun campsitesGetter_filtersByDateLocationAndGuests() {
        val cal = Calendar.getInstance()

        // Setup target dates
        cal.set(2026, Calendar.JULY, 15, 10, 0)
        val july15 = cal.time

        cal.set(2026, Calendar.AUGUST, 20, 14, 30)
        val aug20 = cal.time

        // Sample data setup
        val site1 = Campsite(id = "site_1", name = "Pine Ridge", location = "Sector 4", numGuests = 2, date = july15)
        val site2 = Campsite(id = "site_2", name = "Pines Lake", location = "Sector 1", numGuests = 6, date = july15)
        val site3 = Campsite(id = "site_3", name = "Lakeview",   location = "Sector 4", numGuests = 4, date = aug20)

        val latch = CountDownLatch(1)
        val registration = firestore.collection("campsites")
            .addSnapshotListener { _, _ ->
                if (repository.campsites.size == 3) latch.countDown()
            }

        repository.addCampsite(site1)
        repository.addCampsite(site2)
        repository.addCampsite(site3)

        latch.await(3, TimeUnit.SECONDS)
        registration.remove()

        // -------------------------------------------------------------
        // 1. Test Date Filter
        // -------------------------------------------------------------
        repository.selectedDate = july15
        var results = repository.campsites

        assertEquals(2, results.size)
        assertTrue(results.any { it.name == "Pine Ridge" })
        assertTrue(results.any { it.name == "Pines Lake" })
        assertFalse(results.any { it.name == "Lakeview" })

        // Reset date filter
        repository.selectedDate = repository.NO_DATE_SELECTED

        // -------------------------------------------------------------
        // 2. Test Location Filter (case-insensitive)
        // -------------------------------------------------------------
        repository.selectedLocation = "sector 4"
        results = repository.campsites

        assertEquals(2, results.size)
        assertTrue(results.any { it.name == "Pine Ridge" })
        assertTrue(results.any { it.name == "Lakeview" })
        assertFalse(results.any { it.name == "Pines Lake" })

        // Reset location filter
        repository.selectedLocation = ""

        // -------------------------------------------------------------
        // 3. Test Min Guests Filter
        // -------------------------------------------------------------
        repository.minGuests = 4
        results = repository.campsites

        assertEquals(2, results.size)
        assertTrue(results.any { it.name == "Pines Lake" }) // 6 guests >= 4
        assertTrue(results.any { it.name == "Lakeview" })   // 4 guests >= 4
        assertFalse(results.any { it.name == "Pine Ridge" }) // 2 guests < 4

        // Reset guests filter
        repository.minGuests = 0

        // -------------------------------------------------------------
        // 4. Test Combining All Filters Together
        // (Search: "Pin", Location: "Sector 1", Min Guests: 5, Date: July 15)
        // -------------------------------------------------------------
        repository.searchQuery = "Pin"
        repository.selectedLocation = "Sector 1"
        repository.minGuests = 5
        repository.selectedDate = july15

        results = repository.campsites

        assertEquals(1, results.size)
        assertEquals("Pines Lake", results.first().name)
    }

    @Test
    fun updateCampsite_updatesCampsite() {
        val testCampsite = Campsite(id = "site_10", name = "Cedar Woods", location = "Sector 10", date = Date(), numGuests = 2)
        val updateCampsite = Campsite(id = "site_10", name = "Crimson Grove", location = "Sector 7", date = testCampsite.date, numGuests = 2)
        val latch = CountDownLatch(1)

        repository.addCampsite(testCampsite)
        repository.updateCampsite(testCampsite, updateCampsite)

        val registration = firestore.collection("campsites")
            .addSnapshotListener { _, _ ->
                val updatedSite = repository.campsites.find { it.id == "site_10" }
                if (updatedSite != null && updatedSite.name == "Crimson Grove" && updatedSite.location == "Sector 7") {
                    latch.countDown()
                }
            }
        repository.updateCampsite(testCampsite, updateCampsite)

        val reachedZero = latch.await(3, TimeUnit.SECONDS)

        registration.remove()

        assertTrue(
            "SnapshotListener did not update campsite in time",
            reachedZero
        )

        val result = repository.campsites.first { it.id == "site_10" }

        assertEquals("Crimson Grove", result.name)
        assertEquals("Sector 7", result.location)
    }

}