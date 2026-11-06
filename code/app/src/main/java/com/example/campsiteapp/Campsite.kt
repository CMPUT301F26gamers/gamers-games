package com.example.campsiteapp

import java.util.Date

data class Campsite(
    var id: String = "",
    var name: String = "",
    var location: String = "",
    var date: Date = Date(),
    var numGuests: Int = 0,
    var ownerId: String = "",
    var description: String = "",
    var tags: List<String> = listOf()
) { }