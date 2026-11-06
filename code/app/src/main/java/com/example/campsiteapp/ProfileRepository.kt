package com.example.campsiteapp

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.firestore

/**
 * Loads and manages user profiles in Firestore
 *
 * The repository listens for Firestore changes to keep its local profile list up to date.
 *
 * @property profilesRef The Firestore collection that stores profiles
 * @property profiles The locally tracked profiles
 */
class ProfileRepository(
    private val profilesRef: CollectionReference = Firebase.firestore.collection("profiles")
) {
    private val _profiles = mutableStateListOf<Profile>()

    /**
     * Gets the locally tracked profiles
     */
    val profiles: List<Profile>
        get() = _profiles.toList()

    /**
     * Saves a profile to Firestore
     *
     * @param profile The profile to save
     */
    fun addProfile(profile: Profile) {
        profilesRef.document(profile.uid).set(profile)
    }

    // This photo implementation was provided via Codex (2026-11-05):
    // "Provide implementation for profile viewing and saving a profile picture using BitMap, LocalContext, and the Gallery"
    /**
     * Saves the user's name, email, and phone number
     *
     * @param profile The profile with the contact information to save
     */
    fun saveContactInformation(profile: Profile) {
        profilesRef.document(profile.uid).update(
            mapOf("name" to profile.name, "email" to profile.email, "phoneNumber" to profile.phoneNumber)
        )
    }

    // This photo implementation was provided via Codex (2026-11-05):
    // "Provide implementation for profile viewing and saving a profile picture using BitMap, LocalContext, and the Gallery"
    /**
     * Saves the user's profile picture
     *
     * @param profile The profile to update
     * @param picture The picture data to save
     * @param onComplete Receives true if the save succeeds, false otherwise
     */
    fun saveProfilePicture(profile: Profile, picture: Blob, onComplete: (Boolean) -> Unit) {
        profilesRef.document(profile.uid).update("profilePicture", picture)
            .addOnSuccessListener {
                profile.profilePicture = picture
                onComplete(true)
            }
            .addOnFailureListener { onComplete(false) }
    }

    /**
     * Removes a profile from Firestore
     *
     * @param profile The profile to remove
     */
    fun removeProfile(profile: Profile) {
        profilesRef.document(profile.uid).delete()
    }

    /**
     * Removes the user's campsites from their lotteries, then removes the campsites and profile
     *
     * @param profile The profile to remove
     * @param campsiteRepo The repository used to find and remove the user's campsites
     * @param lotteryRepo The repository used to unlink the campsites from lotteries
     */
    fun cascadeDeleteProfile(profile: Profile, campsiteRepo: CampsiteRepo, lotteryRepo: LotteryRepository) {
        for (campsite in campsiteRepo.getCampsitesByOwner(profile.uid)) {
            for (lottery in lotteryRepo.getLotteries(campsite.id)) {
                lotteryRepo.removeCampsite(lottery.id, campsite.id)
            }
            campsiteRepo.removeCampsite(campsite)
        }
        removeProfile(profile)
    }

    /**
     * Removes the profile from Firestore and the local list, and clears it if it is the current profile
     *
     * @param profile The profile to delete
     */
    fun deleteProfile(profile: Profile) {
        removeProfile(profile)
        _profiles.removeAll { it.uid == profile.uid }

        if (AppUser.profile?.uid == profile.uid) {
            AppUser.profile = null
        }
    }

    /**
     * Loads the profile for the given ID or creates one if needed
     *
     * @param uid The ID of the user whose profile to load or create
     * @param onSuccessfulLoad Receives the loaded or newly created profile
     */
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