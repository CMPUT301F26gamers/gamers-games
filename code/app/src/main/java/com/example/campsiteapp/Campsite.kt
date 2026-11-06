package com.example.campsiteapp

import java.util.Date
import kotlin.math.roundToInt
/**
 * Campsite class that holds information about each campsite.
 *
 * Stores specific information such as the id, name, location, date, number of guests,
 * ID of the owner, maximum occupancy, site description, average site rating, and a list
 * of ratings.
 * @property id The unique identifier for the campsite
 * @property name The name of the campsite
 * @property location The location of the campsite
 * @property date The date for campsite availability
 * @property numGuests The number of guests
 * @property ownerId The ID of the owner of the site
 * @property occupancy The maximum occupancy of the site
 * @property description The description of the campsite
 * @property averageRating Average rating of all ratings left by users
 * @property tags A list of tags
 * @property ratings A list of ratings left by users
 */
data class Campsite(
    var id: String = "",
    var name: String = "",
    var location: String = "",
    var date: Date = Date(),
    var numGuests: Int = 0,
    var ownerId: String = "",
    var occupancy: Int = 0,
    var description: String = "",
    var averageRating: Double = 0.0,
    var tags: List<String> = listOf(),
    var ratings: MutableMap<String, Int> = mutableMapOf()
) {
    /**
     * Adds a rating to the list of user ratings when called.
     *
     * @param userId The ID of the user who left the rating
     * @param rating The rating left by the user
     */
    fun addRating(userId: String, rating: Int) {
        ratings[userId] = rating
        computeAverageRating()
    }

    /**
     * Computes the average rating from all the ratings left by users
     */
    fun computeAverageRating() {
        averageRating =
            if (ratings.isEmpty()) 0.0
            else ratings.values.average()
    }
}