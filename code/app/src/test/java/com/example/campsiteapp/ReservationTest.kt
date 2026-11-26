package com.example.campsiteapp

import com.google.firebase.firestore.CollectionReference
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.Date

class ReservationTest {

    private val repo = LotteryRepository(mockk<CollectionReference>(relaxed = true))

    private fun date(d: Int, h: Int = 0, month: Int = 6): Date =
        Calendar.getInstance().apply { clear(); set(2099, month - 1, d, h, 0) }.time

    private fun day(d: Int) = date(d).toDay()
    private val inBooking = date(15)

    /** Registration ends Jun 9, booking Jun 10-19, stay Jun 20-25 (end of day, like the form) */
    private fun lottery(winners: List<String> = listOf("u1", "u2")) = Lottery(
        id = "L1", campsiteIds = mutableListOf("c1", "c2"),
        registrationEnd = date(9),
        bookingStart = date(10), bookingEnd = date(19),
        lotteryStart = date(20), lotteryEnd = date(25, 23),
        waitlist = mutableListOf("u1", "u2", "u3"),
        winners = winners.toMutableList()
    )

    private fun taken(campsite: String, from: Int, to: Int) =
        Reservation("r$from", "L1", campsite, "other", day(from), day(to))

    private fun check(
        first: Int, last: Int, user: String = "u1",
        all: List<Reservation> = emptyList(), now: Date = inBooking
    ) = checkReservation(lottery(), "c1", user, day(first), day(last), all, now)

    // ---- US 01.04.01: available dates ----

    @Test
    fun availableDays_removesReservedDaysOnlyForThatCampsite() {
        val all = listOf(taken("c1", 21, 22))
        val c1 = availableDays(lottery(), "c1", all)
        assertEquals(4, c1.size)
        assertFalse(day(21) in c1)
        assertEquals(6, availableDays(lottery(), "c2", all).size)
    }

    @Test
    fun availableDays_isEmptyWhenEveryDayIsReserved() {
        assertTrue(availableDays(lottery(), "c1", listOf(taken("c1", 20, 25))).isEmpty())
    }

    // ---- US 01.04.02: reserve rules ----

    @Test
    fun check_validRangeIsAllowed() = assertNull(check(21, 23))

    @Test
    fun check_nonWinnerIsRejected() =
        assertEquals(ReserveResult.NOT_WINNER, check(21, 22, user = "u3"))

    @Test
    fun check_daysOutsideStayPeriodAreRejected() {
        assertEquals(ReserveResult.OUT_OF_RANGE, check(19, 21))
        assertEquals(ReserveResult.OUT_OF_RANGE, check(24, 26))
    }

    @Test
    fun check_overlapIsTaken_butAdjacentDaysAreAllowed() {
        val all = listOf(taken("c1", 21, 22))
        assertEquals(ReserveResult.TAKEN, check(22, 24, all = all))
        assertNull(check(23, 24, all = all))
    }

    @Test
    fun check_outsideBookingWindowIsRejected() {
        assertEquals(ReserveResult.BOOKING_CLOSED, check(21, 22, now = date(5)))
        assertEquals(ReserveResult.BOOKING_CLOSED, check(21, 22, now = date(21)))
    }

    // ---- pending list ----

    @Test
    fun pendingLotteries_onlyDrawnUsers_andHiddenAfterBookingEnds() {
        val l = lottery(winners = listOf("u1"))
        repo.lotteries.add(l)
        assertEquals(listOf(l), repo.pendingLotteries("u1", inBooking))
        assertTrue(repo.pendingLotteries("u2", inBooking).isEmpty())
        assertTrue(repo.pendingLotteries("u1", date(20)).isEmpty())
    }

    // ---- US 01.04.03: decline ----

    @Test
    fun decline_movesUserToDeclinedAndDrawsReplacement() {
        val l = lottery(winners = listOf("u1"))
        repo.lotteries.add(l)

        repo.decline("L1", "u1", inBooking)

        assertTrue("u1" in l.declined)
        assertFalse("u1" in l.winners)
        assertTrue(l.winners.single() in setOf("u2", "u3"))   // never the declined user
    }

    @Test
    fun decline_withNobodyLeftLeavesNoWinner() {
        val l = lottery(winners = listOf("u1"))
        l.waitlist.removeAll(listOf("u2", "u3"))
        repo.lotteries.add(l)

        repo.decline("L1", "u1", inBooking)

        assertTrue(l.winners.isEmpty())
    }

    @Test
    fun decline_byNonWinnerOrOutsideBookingWindowIsRefused() {
        repo.lotteries.add(lottery(winners = listOf("u1")))
        var result: Boolean? = null
        repo.decline("L1", "u3", inBooking) { result = it }
        assertEquals(false, result)
        repo.decline("L1", "u1", date(21)) { result = it }
        assertEquals(false, result)
    }

    @Test
    fun decline_beforeRegistrationClosesDoesNotCrash() {
        // Hand-edited Firestore data can open booking before registration ends
        val l = lottery(winners = listOf("u1")).apply { registrationEnd = date(1, month = 7) }
        repo.lotteries.add(l)

        repo.decline("L1", "u1", inBooking)

        assertTrue("u1" in l.declined)
        assertTrue(l.winners.isEmpty())
    }
}