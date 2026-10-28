package com.example.campsiteapp
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class CampsiteRepository {
    private val db = Firebase.firestore
    private val campsitesRef = db.collection("campsites")
    private val _campsites = mutableStateListOf<Campsite>()

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

    val campsites: List<Campsite>
        get() = _campsites

    fun addCampsite(campsite: Campsite) {
        campsitesRef.document(campsite.name).set(campsite)
    }

    fun removeCampsite(campsite: Campsite) {
        campsitesRef.document(campsite.name).delete()
    }
}