package com.example.campsiteapp

import java.util.Calendar
import java.util.Date
import kotlin.random.Random

/**
 * A lottery for one or more campsites.
 * registration (join the waitlist) -> draw -> booking (winners book/decline)
 *
 * Order of the periods: registration ends <= booking starts, booking ends <= stay starts.
 *
 * @property campsiteIds IDs of the campsites listed in this lottery
 * @property lotteryStart start of camping date (the date range that user can choose)
 * @property lotteryEnd end of camping date
 * @property registrationStart registration start date
 * @property registrationEnd end registration date, the draw happens after this
 * @property bookingStart start booking date
 * @property bookingEnd when the booking offer expires
 * @property numWinners number of winners for the first draw
 * @property waitlistCapacity max size of the waitlist
 * @property waitlist IDs of the users on the waitlist
 * @property winners drawn users who have not yet booked or declined
 * @property bookings winners who accepted and booked
 * @property declined winners who declined
 */
class Lottery(
    val id: String = "",
    val campsiteIds: MutableList<String> = mutableListOf(),
    var lotteryStart: Date = Date(),
    var lotteryEnd: Date = Date(),
    var registrationStart: Date = Date(),   // US 02.04.03
    var registrationEnd: Date = Date(),
    var bookingStart: Date = Date(),        // US 02.04.04
    var bookingEnd: Date = Date(),
    var numWinners: Int = 1,                // US 02.04.05
    var waitlistCapacity: Int? = null,      // US 02.04.07
    val waitlist: MutableList<String> = mutableListOf(),
    val winners: MutableList<String> = mutableListOf(),
    val bookings: MutableList<String> = mutableListOf(),
    val declined: MutableList<String> = mutableListOf()
) {
    /**
     * Sets the stay period the campsites are listed for (US 02.04.01)
     *
     * @param start first day of the stay
     * @param end last day of the stay
     */
    fun setLotteryPeriod(start: Date, end: Date, now: Date = Date()) {
        if (start.before(startOfToday(now))) {
            throw IllegalArgumentException("Start date cannot be in the past")
        }
        if (!start.before(end)) {
            throw IllegalArgumentException("Start date must be before end date")
        }
        if (bookingEnd.after(start)) {
            throw IllegalArgumentException("Stay must start after booking ends")
        }
        lotteryStart = start
        lotteryEnd = end
    }

    /**
     * Sets the period during which users can join the waitlist (US 02.04.03).
     *
     * @param start date of the registration period
     * @param end date of the registration period
     */
    fun setRegistrationPeriod(start: Date, end: Date, now: Date = Date()) {
        if (start.before(startOfToday(now))) {
            throw IllegalArgumentException("Start date cannot be in the past")
        }
        if (!start.before(end)) {
            throw IllegalArgumentException("Start date must be before end date")
        }
        if (end.after(bookingStart)) {
            throw IllegalArgumentException("Registration must end before booking starts")
        }
        registrationStart = start
        registrationEnd = end
    }

    /**
     * Sets when winners can book or decline (US 02.04.04).
     *
     * @param start when winners are notified and can book
     * @param end when the offer expires
     */
    fun setBookingPeriod(start: Date, end: Date) {
        if (!start.before(end)) {
            throw IllegalArgumentException("Start date must be before end date")
        }
        if (start.before(registrationEnd)) {
            throw IllegalArgumentException("Booking must start after registration ends")
        }
        if (end.after(lotteryStart)) {
            throw IllegalArgumentException("Booking must end before the stay starts")
        }
        bookingStart = start
        bookingEnd = end
    }

    /**
     * Sets all three periods at once, validating the order:
     * registration -> booking -> stay (used when creating a lottery).
     * Booking may start right when registration ends (draw day).
     */
    fun setPeriods(
        regStart: Date, regEnd: Date,
        bookStart: Date, bookEnd: Date,
        start: Date, end: Date,
        now: Date = Date()
    ) {
        if (regStart.before(startOfToday(now))) {
            throw IllegalArgumentException("Start date cannot be in the past")
        }
        if (!regStart.before(regEnd) || !bookStart.before(bookEnd) || !start.before(end)) {
            throw IllegalArgumentException("Start date must be before end date")
        }
        if (bookStart.before(regEnd)) {
            throw IllegalArgumentException("Booking must start after registration ends")
        }
        if (start.before(bookEnd)) {
            throw IllegalArgumentException("Stay must start after booking ends")
        }
        registrationStart = regStart
        registrationEnd = regEnd
        bookingStart = bookStart
        bookingEnd = bookEnd
        lotteryStart = start
        lotteryEnd = end
    }

    /**
     * Sets how many winners the lottery will draw (US 02.04.05).
     * @param n the number of winners
     */
    fun updateNumWinners(n: Int) {
        if (n < 1) {
            throw IllegalArgumentException(
                "Winners must be a whole number of 1 or more"
            )
        }
        if (waitlistCapacity != null && n > waitlistCapacity!!) {
            throw IllegalArgumentException(
                "Number of winners cannot exceed waitlist capacity"
            )
        }
        numWinners = n
    }

    /**
     * Sets the waitlist capacity (US 02.04.07)
     *
     * @param capacity a whole number of 1 or more, or null for no limit
     */
    fun updateWaitlistCapacity(capacity: Int?) {
        if (capacity != null && capacity < 1) {
            throw IllegalArgumentException(
                "Capacity must be a whole number of 1 or more"
            )
        }
        if (capacity != null && capacity < waitlist.size) {
            throw IllegalArgumentException(
                "Capacity cannot be lower than the current number of entrants"
            )
        }
        if (capacity != null && capacity < numWinners) {
            throw IllegalArgumentException(
                "Capacity cannot be lower than the number of winners"
            )
        }

        waitlistCapacity = capacity
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

    /** @return true if a capacity is set and the waitlist has reached it */
    fun isFull(): Boolean {
        val cap = waitlistCapacity ?: return false
        return waitlist.size >= cap
    }

    /**
     * @param userId the user to look for
     * @return true if the user is on the waitlist
     */
    fun hasEntrant(userId: String): Boolean {
        return waitlist.contains(userId)
    }

    /** @return true if this user may join the waitlist (not already on it, and it isn't full) */
    fun canJoin(userId: String): Boolean {
        return !hasEntrant(userId) && !isFull()
    }

    /** @return true if this user may leave the waitlist (on it, and not already drawn) */
    fun canLeave(userId: String): Boolean {
        return hasEntrant(userId) && !hasBeenDrawn(userId)
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

    /** @return true if this user was drawn at some point (pending, booked or declined) */
    fun hasBeenDrawn(userId: String): Boolean {
        return userId in winners || userId in bookings || userId in declined
    }

    /**
     * Randomly draws [count] new winners from the waitlist, skipping anyone
     * already drawn
     * If fewer users are left than [count], everyone left wins
     *
     * @param count how many to draw, 1 or more
     * @param seed seed for the random generator (same seed + same waitlist = same draw)
     * @return the newly drawn user IDs
     */
    fun drawWinners(count: Int, seed: Long, now: Date = Date()): List<String> {
        if (count < 1) {
            throw IllegalArgumentException("Draw count must be a whole number of 1 or more")
        }
        if (!now.after(registrationEnd)) {
            throw IllegalStateException("Registration has not closed yet")
        }
        val candidates = waitlist.filter { !hasBeenDrawn(it) }
        val drawn = candidates.shuffled(Random(seed)).take(count)
        winners.addAll(drawn)
        return drawn
    }

    /** @return true if winners can book or decline at the given time */
    fun isBookingOpen(now: Date): Boolean {
        return !now.before(bookingStart) && !now.after(bookingEnd)
    }

    /**
     * move user from [winners] to [bookings]
     * @return true if recorded, false if booking is closed or the user isn't a pending winner
     */
    fun book(userId: String, now: Date): Boolean {
        if (!isBookingOpen(now) || !winners.remove(userId)) return false
        bookings.add(userId)
        return true
    }

    /**
     * move user from [winners] to [declined] and
     * stay on the waitlist, but won't be drawn again
     */
    fun decline(userId: String, now: Date): Boolean {
        if (!isBookingOpen(now) || !winners.remove(userId)) return false
        declined.add(userId)
        return true
    }

    private fun startOfToday(now: Date): Date {
        val cal = Calendar.getInstance()
        cal.time = now
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }
}