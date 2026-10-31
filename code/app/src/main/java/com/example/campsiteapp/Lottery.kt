package com.example.campsiteapp

import java.util.Date
import kotlin.random.Random

/**
 * A lottery for one or more campsites.
 * The owner configures the class with [setPeriods], [setLotteryPeriod],
 * [setRegistrationPeriod] and [updateNumWinners]. Entrants join with [join];
 * [isActive] and [hasEntrant] are the 2 methods for checking join validity.
 *
 * The registration period must end before the lottery period starts.
 *
 * @property campsiteIds IDs of the campsites listed in this lottery
 * @property lotteryStart start of the lottery period
 * @property lotteryEnd end of the lottery period
 * @property registrationStart start of the registration period
 * @property registrationEnd end of the registration period
 * @property numWinners number of winners to draw, always 1 or more
 * @property entrants IDs of the users who have joined this lottery
 */
class Lottery(
    val id: String = "",
    val campsiteIds: MutableList<String> = mutableListOf(),
    var lotteryStart: Date = Date(),
    var lotteryEnd: Date = Date(),
    var registrationStart: Date = Date(),   // US 02.04.03
    var registrationEnd: Date = Date(),
    var numWinners: Int = 1,                // US 02.04.05
    val entrants: MutableList<String> = mutableListOf(),
    val winners: MutableList<String> = mutableListOf()
) {
    /**
     * Sets the period the campsites are listed in the lottery (US 02.04.01)
     *
     * @param start date of the lottery
     * @param end date of the lottery
     */
    fun setLotteryPeriod(start: Date, end: Date, now: Date = Date()) {
        if (start.before(now)) {
            throw IllegalArgumentException("Start date cannot be in the past")
        }
        if (!start.before(end)) {
            throw IllegalArgumentException("Start date must be before end date")
        }
        if (registrationEnd.after(start)) {
            throw IllegalArgumentException("Registration must end before the lottery starts")
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

    fun setRegistrationPeriod(start: Date, end: Date, now: Date = Date()) {
        if (start.before(now)) {
            throw IllegalArgumentException("Start date cannot be in the past")
        }
        if (!start.before(end)) {
            throw IllegalArgumentException("Start date must be before end date")
        }
        if (end.after(lotteryStart)) {
            throw IllegalArgumentException("Registration must end before the lottery starts")
        }
        registrationStart = start
        registrationEnd = end
    }

    /**
     * Sets both periods at once, validating them together (used when creating a lottery).
     *
     * @param regStart start of the registration period
     * @param regEnd end of the registration period
     * @param start start of the lottery period
     * @param end end of the lottery period
     */
    fun setPeriods(
        regStart: Date, regEnd: Date, start: Date, end: Date,
        now: Date = Date()
    ) {
        if (regStart.before(now) || start.before(now)) {
            throw IllegalArgumentException("Start date cannot be in the past")
        }
        if (!regStart.before(regEnd) || !start.before(end)) {
            throw IllegalArgumentException("Start date must be before end date")
        }
        if (regEnd.after(start)) {
            throw IllegalArgumentException("Registration must end before the lottery starts")
        }
        registrationStart = regStart
        registrationEnd = regEnd
        lotteryStart = start
        lotteryEnd = end
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
     * Whether registration is open at the given time (US 01.01.06)
     *
     * @param now the time to check
     * @return true if [now] is within the registration period
     */
    fun isActive(now: Date): Boolean {
        return !now.before(registrationStart) && !now.after(registrationEnd)
    }

    /**
     * @param userId the user to look for
     * @return true if the user is already in the lottery
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
     * @param userId the user joining
     * @return true if the user was added, false if they were already in
     */
    fun join(userId: String): Boolean {
        if (hasEntrant(userId)) {
            return false
        }
        entrants.add(userId)
        return true
    }

    /**
     * @param campsiteId the campsite to look for
     * @return true if the campsite is already in this lottery
     */
    fun hasCampsite(campsiteId: String): Boolean {
        return campsiteIds.contains(campsiteId)
    }

    /**
     * Adds a campsite to this lottery (US 02.04.02)
     *
     * @param campsiteId the campsite to add
     * @return true if the campsite was added, false if it was already in the lottery
     */
    fun addCampsite(campsiteId: String): Boolean {
        if (hasCampsite(campsiteId)) {
            return false
        }
        campsiteIds.add(campsiteId)
        return true
    }

    /** @return true if winners have already been drawn */
    fun hasDrawn(): Boolean {
        return winners.isNotEmpty()
    }

    /**
     * Randomly picks [numWinners] winners from the entrants.
     * If there are fewer entrants than [numWinners], everyone wins.
     * Can only be done once, after registration has closed
     */
    fun drawWinners(now: Date = Date(), random: Random = Random.Default) {
        if (!now.after(registrationEnd)) {
            throw IllegalStateException("Registration has not closed yet")
        }
        if (hasDrawn()) {
            throw IllegalStateException("Winners have already been drawn")
        }
        winners.addAll(entrants.shuffled(random).take(numWinners))
    }
}