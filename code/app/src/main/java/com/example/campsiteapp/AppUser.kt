package com.example.campsiteapp

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * Holds the current user's profile
 *
 * The app uses this shared state to access the profile and observe when it changes.
 *
 * @property profile The current profile, or null if none is loaded
 */
object AppUser {
    var profile by mutableStateOf<Profile?>(null)
}
