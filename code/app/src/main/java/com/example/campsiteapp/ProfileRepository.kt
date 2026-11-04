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
        profilesRef.document(profile.uid).set(profile)
    }

    fun removeProfile(profile: Profile) {
        profilesRef.document(profile.uid).delete()
    }

    fun createOrLoadProfile(uid: String, onSuccessfulLoad: (Profile) -> Unit) {
        profilesRef.document(uid).get().addOnSuccessListener { document ->
            val exisingProfile = document.toObject(Profile::class.java)

            if (exisingProfile != null) {
                _profiles.add(exisingProfile)
                onSuccessfulLoad(exisingProfile)
            } else {
                val newProfile = Profile(uid)
                addProfile(newProfile)
                _profiles.add(newProfile)
                onSuccessfulLoad(newProfile)
            }
        }
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