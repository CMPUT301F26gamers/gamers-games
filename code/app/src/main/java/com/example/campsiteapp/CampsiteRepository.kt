package com.example.campsiteapp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class CampsiteRepository : CampsiteRepo {
    private val db = Firebase.firestore
    private val campsitesRef = db.collection("campsites")
    private val _campsites = mutableStateListOf<Campsite>()

    val NO_DATE_SELECTED = Date(0L)
    override var searchQuery: String = ""
    override var minGuests: Int = 0
    override var selectedLocation: String = ""
    override var selectedDate: Date by mutableStateOf(NO_DATE_SELECTED)

    init {
        campsitesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _campsites.clear()

            snapshot?.documents?.forEach { document ->
                val campsite = document.toObject(Campsite::class.java)
                if (campsite != null) {
                    _campsites.add(campsite)
                }
            }
        }
    }

    override val campsites: List<Campsite>
        get() = _campsites.filter { campsite ->
            // Match Search Query (if provided)
            val matchesQuery = searchQuery.isBlank() ||
                    campsite.name.contains(searchQuery, ignoreCase = true)

            // Match Min Guests (if provided / > 0)
            val matchesGuests = minGuests <= 0 ||
                    campsite.numGuests >= minGuests

            // Match Location (if provided)
            val matchesLocation = selectedLocation.isBlank() ||
                    campsite.location.equals(selectedLocation, ignoreCase = true)

            val matchesDate = selectedDate == NO_DATE_SELECTED || isSameDay(campsite.date, selectedDate)

            matchesQuery && matchesGuests && matchesLocation && matchesDate
        }

    /**
     * Helper to compare two Date objects by year, month, and day of month.
     */
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    override fun addCampsite(campsite: Campsite) {
        campsitesRef.document(campsite.id).set(campsite)
    }

    override fun removeCampsite(campsite: Campsite) {
        campsitesRef.document(campsite.id).delete()
            .addOnSuccessListener { println("Successfully deleted document: ${campsite.id}") }
            .addOnFailureListener { e -> println("Failed to delete document: ${e.message}") }
    }

    override fun getCampsitesByOwner(ownerId: String): List<Campsite> {
        return _campsites.filter { it.ownerId == ownerId }
    }

    fun updateCampsite(oldCampsite: Campsite, newCampsite: Campsite) {
        campsitesRef.document(oldCampsite.id).set(newCampsite)
            .addOnSuccessListener { println("Successfully updated document: ${oldCampsite.id}") }
            .addOnFailureListener { e -> println("Failed to update document: ${e.message}") }
    }
}