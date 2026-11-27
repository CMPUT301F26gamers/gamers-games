package com.example.campsiteapp

/**
 * Rating class that holds information about each Rating
 *
 * Stores specific information such as the ID of the User, The rating they gave,
 * and the optional review they gave.
 * @property userID The unique identifier of the user that left the review
 * @property rating The rating (Int between 0 and 5) that the user left
 * @property review The string review attached to the rating
 */
class Rating(
    var userID: String,
    var rating: Int,
    var review: String = ""
) {
}