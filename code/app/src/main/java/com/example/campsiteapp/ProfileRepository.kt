package com.example.campsiteapp

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class ProfileRepository {
    private val db = Firebase.firestore
    private val profilesRef = db.collection("profiles")
    private val _profiles = mutableStateListOf<Profile>()

    val profiles: List<Profile>
        get() = _profiles.toList()

    fun addProfile(profile: Profile) {
        profilesRef.document(profile.name).set(profile)
    }

    fun removeProfile(profile: Profile) {
        profilesRef.document(profile.name).delete()
    }

    init {
        profilesRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _profiles.clear()

            snapshot?.documents?.forEach { document ->
                val profile = document.toObject(Profile::class.java)
                if (profile != null) {
                    _profiles.add(profile)
                }
            }
        }
    }
}