package com.example.campsiteapp

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore

class ProfileRepository(
    private val profilesRef: CollectionReference = Firebase.firestore.collection("profiles")
) {
    private val _profiles = mutableStateListOf<Profile>()

    val profiles: List<Profile>
        get() = _profiles.toList()

    fun addProfile(profile: Profile) {
        profilesRef.document(profile.uid).set(profile)
    }

    // This photo implementation was provided via Codex (2026-11-05):
    // "Provide implementation for profile viewing and saving a profile picture using BitMap, LocalContext, and the Gallery"
    fun saveContactInformation(profile: Profile) {
        profilesRef.document(profile.uid).update(
            mapOf("name" to profile.name, "email" to profile.email, "phoneNumber" to profile.phoneNumber)
        )
    }

    // This photo implementation was provided via Codex (2026-11-05):
    // "Provide implementation for profile viewing and saving a profile picture using BitMap, LocalContext, and the Gallery"
    fun saveProfilePicture(profile: Profile, picture: Blob, onComplete: (Boolean) -> Unit) {
        profilesRef.document(profile.uid).update("profilePicture", picture)
            .addOnSuccessListener {
                profile.profilePicture = picture
                onComplete(true)
            }
            .addOnFailureListener { onComplete(false) }
    }

    fun removeProfile(profile: Profile) {
        profilesRef.document(profile.uid).delete()
    }

    fun cascadeDeleteProfile(profile: Profile, campsiteRepo: CampsiteRepo, lotteryRepo: LotteryRepository) {
        for (campsite in campsiteRepo.getCampsitesByOwner(profile.uid)) {
            for (lottery in lotteryRepo.getLotteries(campsite.id)) {
                lotteryRepo.removeCampsite(lottery.id, campsite.id)
            }
            campsiteRepo.removeCampsite(campsite)
        }
        removeProfile(profile)
    }

    fun deleteProfile(profile: Profile) {
        removeProfile(profile)
        _profiles.removeAll { it.uid == profile.uid }

        if (AppUser.profile?.uid == profile.uid) {
            AppUser.profile = null
        }
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