package com.example.campsiteapp

/**
 * Roles for users
 */
enum class Roles {
    ENTRANT,
    OWNER,
    ADMIN
}

/**
 * Profile class that represents a user profile
 *
 * Profile stores information regarding a specific user including their name, email,
 * phone number, profile picture, notification setting, and roles.
 * A profile can have multiple roles, but they start as an entrant.
 * Afterward, they can assign themselves as an owner as well.
 * An admin profile will be created on firebase and only that one will
 * have the ability to assign other users as an admin.
 * Profile can also modify its own information and check if it has a certain role.
 *
 * @property uid The unique device identifier for the user
 * @property name The user's name
 * @property email The user's email
 * @property phoneNumber The user's phone number
 * @property profilePictureUrl The user's profile picture as a string
 * @property enableNotifications Whether the user wants to receive notifications
 */
class Profile (val uid: String, var name: String = "", var email: String = "",
               var phoneNumber: String = "", var profilePictureUrl: String = "",
               var enableNotifications: Boolean = true) {

    /**
     * The user's roles. Always start as entrant
     */
    private val roles = mutableSetOf<Roles>(Roles.ENTRANT)

    /**
     * Updates the profile information
     *
     * You can either pass in new information or NULL to leave that information unchanged.
     *
     * @param name The user's name as a string or NULL
     * @param email The user's email as a string or NULL
     * @param phoneNumber The user's phone number as a string or NULL
     * @param profilePictureUrl The user's profile picture as a string or NULL
     */
    fun updateProfileInformation(name: String? = null, email: String? = null,
                                 phoneNumber: String? = null, profilePictureUrl: String? = null, enableNotifications: Boolean? = null){
        if (name != null) {
            this.name = name
        }
        if (email != null) {
            this.email = email
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber
        }
        if (profilePictureUrl != null) {
            this.profilePictureUrl = profilePictureUrl
        }
        if (enableNotifications != null) {
            this.enableNotifications = enableNotifications
        }
    }

    /**
     * Checks if the user has a certain role
     *
     * @param role The role to check for
     * @return True if the user has the role, false otherwise
     */
    fun hasRole(role: Roles): Boolean {
        return roles.contains(role)
    }

    /**
     * Adds a campsite owner role to the user
     */
    fun addRole(role: Roles){
        roles.add(role)
    }

}