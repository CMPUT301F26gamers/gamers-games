package com.example.campsiteapp

class CampsiteValidator {

    fun isValidName(name: String): Boolean {
        return name.isNotBlank()
    }

    fun isValidLocation(location: String): Boolean {
        return location.isNotBlank()
    }

    fun isValidMaxGuests(maxGuests: String): Boolean {
        val number = maxGuests.trim().toIntOrNull() ?: return false
        return number >= 1
    }

    fun parseTags(tags: String): List<String> {
        return tags.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }
    }
}
