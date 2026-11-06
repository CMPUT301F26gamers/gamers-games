package com.example.campsiteapp

import java.util.Date

interface CampsiteRepo {
    val campsites: List<Campsite>
    var searchQuery: String
    var minGuests: Int
    var selectedLocation: String
    var selectedDate: Date

    fun addCampsite(campsite: Campsite)
    fun removeCampsite(campsite: Campsite)
    fun getCampsitesByOwner(ownerId: String): List<Campsite>
}