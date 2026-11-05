package com.example.campsiteapp

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

object AppUser {
    var profile by mutableStateOf<Profile?>(null)
}