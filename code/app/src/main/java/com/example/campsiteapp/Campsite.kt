package com.example.campsiteapp

class Campsite(
    val id: String = "",
    val name: String = "",
    val waitlist: MutableList<String> = mutableListOf(),   // US 01.02.01
    var waitlistCapacity: Int? = null                      // US 02.04.07
) {
    /**
     * @param userId the user to look for
     * @return true if the user is on the waitlist
     */
    fun isInWaitlist(userId: String): Boolean {
        return waitlist.contains(userId)
    }

    /** @return true if a capacity is set and the waitlist has reached it */
    fun waitlistIsFull(): Boolean {
        val capacity = waitlistCapacity ?: return false
        return waitlist.size >= capacity
    }

    /**
     * Adds a user to the waitlist (US 01.02.01)
     *
     * @param userId the user joining
     * @return true if added, false if already on it or the waitlist is full
     */
    fun joinWaitlist(userId: String): Boolean {
        if (isInWaitlist(userId) || waitlistIsFull()) {
            return false
        }
        waitlist.add(userId)
        return true
    }

    /**
     * Removes a user from the waitlist (US 01.02.02)
     * @param userId the user leaving
     * @return true if the user was on the waitlist and has been removed
     */
    fun leaveWaitlist(userId: String): Boolean {
        return waitlist.remove(userId)
    }

    /**
     * Sets the waitlist capacity (US 02.04.07)
     *
     * @param capacity a whole number of 1 or more, or null for no limit
     */
    fun updateWaitlistCapacity(capacity: Int?) {
        if (capacity != null && capacity < 1) {
            throw IllegalArgumentException("Capacity must be a whole number of 1 or more")
        }
        waitlistCapacity = capacity
    }
}