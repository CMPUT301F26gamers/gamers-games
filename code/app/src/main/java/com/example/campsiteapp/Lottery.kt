package com.example.campsiteapp

import java.util.Date

/**
 * A lottery for a campsite
 * The owner configures the class with [setLotteryPeriod], [setRegistrationPeriod],
 * [updateNumWinners]. Entrants join with [join], [isActive] and
 * [hasEntrant] are 2 methods for checking join validity
 *
 * @property campsiteId ID of the campsite of this lottery
 * @property lotteryStart start of the lottery period
 * @property lotteryEnd end of the lottery period
 * @property registrationStart start of the registration period, or null if the owner hasn't set one
 * @property registrationEnd end of the registration period, or null if the owner hasn't set one
 * @property numWinners number of winners to draw, always 1 or more
 * @property entrants IDs of the users who have joined this lottery.
 */
class Lottery(
    val campsiteId: String = "",
    var lotteryStart: Date = Date(),
    var lotteryEnd: Date = Date(),
    var registrationStart: Date? = null,   // US 02.04.03
    var registrationEnd: Date? = null,
    var numWinners: Int = 1,               // US 02.04.05
    val entrants: MutableList<String> = mutableListOf()
) {
    // !!!WARNING!!!
    // Registration end date can be after lottery start date which can cause some problem
    /**
     * Sets the period the campsite is listed in the lottery (US 02.04.01)
     *
     * @param start date of the lottery
     * @param end date of the lottery
     */
    fun setLotteryPeriod(start: Date, end: Date) {
        if (!start.before(end)) {
            throw IllegalArgumentException("Start date must be before end date")
        }
        val regStart = registrationStart
        val regEnd = registrationEnd
        if (regStart != null && regStart.after(start)) {
            throw IllegalArgumentException("Registration can't start after the lottery starts")
        }
        if (regEnd != null && regEnd.after(end)) {
            throw IllegalArgumentException("Registration can't end after the lottery ends")
        }
        lotteryStart = start
        lotteryEnd = end
    }

    /**
     * Sets the period during which entrants can register (US 02.04.03).
     *
     * @param start date of the registration period
     * @param end date of the registration period
     */
    fun setRegistrationPeriod(start: Date, end: Date) {
        if (!start.before(end)) {
            throw IllegalArgumentException("Start date must be before end date")
        }
        if (start.after(lotteryStart)) {
            throw IllegalArgumentException("Registration can't start after the lottery starts")
        }
        if (end.after(lotteryEnd)) {
            throw IllegalArgumentException("Registration can't end after the lottery ends")
        }
        registrationStart = start
        registrationEnd = end
    }

    /**
     * Sets how many winners the lottery will draw (US 02.04.05).
     * @param n the number of winners
     */
    fun updateNumWinners(n: Int) {
        if (n < 1) {
            throw IllegalArgumentException("Winners must be a whole number of 1 or more")
        }
        numWinners = n
    }



    /**
     * Whether entrants can join at the given time (US 01.01.06)
     *
     * @param now: the time to check
     * @return Boolean
     */
    fun isActive(now: Date): Boolean {
        val start: Date
        val end: Date

        if (registrationStart != null && registrationEnd != null) {
            start = registrationStart!!
            end = registrationEnd!!
        } else {
            start = lotteryStart
            end = lotteryEnd
        }
        if (now.before(start)) {
            return false
        }
        if (now.after(end)) {
            return false
        }
        return true
    }

    /**
     * @param userId the user to look for
     * @return true if the user is already in lottery
     */
    fun hasEntrant(userId: String): Boolean {
        return entrants.contains(userId)
    }

    /**
     * Adds a user to the lottery (US 01.01.06)
     *
     * !!!!WARNING!!!!
     * This does not check [isActive]
     * the caller decides whether joining is allowed
     *
     * @param userId the user joining.
     * @return true if the user was added, false if not
     */
    fun join(userId: String): Boolean { // true if success
        if (hasEntrant(userId)) {       // false if user already in
            return false
        }
        entrants.add(userId)
        return true
    }
}