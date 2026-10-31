package com.example.campsiteapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import kotlin.random.Random

class LotteryTest {

    /** Builds a Date for the given day, month (1-12) and year. */
    private fun date(day: Int, month: Int, year: Int): Date =
        Date.from(
            LocalDate.of(year, month, day)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
        )

    // "today" for the tests: before every date used below
    private val now = date(1, 1, 2026)

    // a time after registration has closed
    private val afterClose = date(10, 6, 2026)

    // registration 1 Jun - 9 Jun, lottery 10 Jun - 20 Jun
    private val lottery = Lottery(
        campsiteIds = mutableListOf("camp1"),
        registrationStart = date(1, 6, 2026),
        registrationEnd = date(9, 6, 2026),
        lotteryStart = date(10, 6, 2026),
        lotteryEnd = date(20, 6, 2026)
    )

    // setLotteryPeriod

    @Test
    fun setLotteryPeriod_validDates_updatesBoth() {
        lottery.setLotteryPeriod(date(1, 7, 2026), date(5, 7, 2026), now)
        assertEquals(date(1, 7, 2026), lottery.lotteryStart)
        assertEquals(date(5, 7, 2026), lottery.lotteryEnd)
    }

    @Test(expected = IllegalArgumentException::class)
    fun setLotteryPeriod_startAfterEnd_throws() {
        lottery.setLotteryPeriod(date(5, 7, 2026), date(1, 7, 2026), now)
    }

    @Test(expected = IllegalArgumentException::class)
    fun setLotteryPeriod_startEqualsEnd_throws() {
        lottery.setLotteryPeriod(date(5, 7, 2026), date(5, 7, 2026), now)
    }

    @Test
    fun setLotteryPeriod_invalid_leavesOldDatesUnchanged() {
        try {
            lottery.setLotteryPeriod(date(5, 7, 2026), date(1, 7, 2026), now)
        } catch (e: IllegalArgumentException) {
            // expected
        }
        assertEquals(date(10, 6, 2026), lottery.lotteryStart)
        assertEquals(date(20, 6, 2026), lottery.lotteryEnd)
    }

    @Test(expected = IllegalArgumentException::class)
    fun setLotteryPeriod_startInPast_throws() {
        lottery.setLotteryPeriod(date(1, 7, 2026), date(5, 7, 2026), date(2, 7, 2026))
    }

    // setRegistrationPeriod

    @Test
    fun setRegistrationPeriod_validDates_updatesBoth() {
        lottery.setRegistrationPeriod(date(2, 6, 2026), date(8, 6, 2026), now)
        assertEquals(date(2, 6, 2026), lottery.registrationStart)
        assertEquals(date(8, 6, 2026), lottery.registrationEnd)
    }

    @Test(expected = IllegalArgumentException::class)
    fun setRegistrationPeriod_startAfterEnd_throws() {
        lottery.setRegistrationPeriod(date(8, 6, 2026), date(2, 6, 2026), now)
    }

    @Test
    fun setRegistrationPeriod_invalid_leavesDatesUnchanged() {
        try {
            lottery.setRegistrationPeriod(date(8, 6, 2026), date(2, 6, 2026), now)
        } catch (e: IllegalArgumentException) {
            // expected
        }
        assertEquals(date(1, 6, 2026), lottery.registrationStart)
        assertEquals(date(9, 6, 2026), lottery.registrationEnd)
    }

    @Test(expected = IllegalArgumentException::class)
    fun setRegistrationPeriod_startAfterLotteryStart_throws() {
        lottery.setRegistrationPeriod(date(11, 6, 2026), date(15, 6, 2026), now)
    }

    @Test(expected = IllegalArgumentException::class)
    fun setRegistrationPeriod_endAfterLotteryEnd_throws() {
        lottery.setRegistrationPeriod(date(5, 6, 2026), date(21, 6, 2026), now)
    }

    @Test(expected = IllegalArgumentException::class)
    fun setRegistrationPeriod_endsAfterLotteryStarts_throws() {
        lottery.setRegistrationPeriod(date(10, 6, 2026), date(20, 6, 2026), now)
    }

    @Test
    fun setRegistrationPeriod_endsExactlyWhenLotteryStarts_isAccepted() {
        lottery.setRegistrationPeriod(date(5, 6, 2026), date(10, 6, 2026), now)
        assertEquals(date(10, 6, 2026), lottery.registrationEnd)
    }

    @Test(expected = IllegalArgumentException::class)
    fun setRegistrationPeriod_startInPast_throws() {
        lottery.setRegistrationPeriod(date(2, 6, 2026), date(8, 6, 2026), date(3, 6, 2026))
    }

    @Test(expected = IllegalArgumentException::class)
    fun setLotteryPeriod_registrationWouldStartAfterLottery_throws() {
        lottery.setRegistrationPeriod(date(2, 6, 2026), date(8, 6, 2026), now)
        lottery.setLotteryPeriod(date(1, 6, 2026), date(20, 6, 2026), now)  // lottery now starts before registration ends
    }

    @Test
    fun setLotteryPeriod_registrationOutside_leavesOldDatesUnchanged() {
        lottery.setRegistrationPeriod(date(2, 6, 2026), date(8, 6, 2026), now)
        try {
            lottery.setLotteryPeriod(date(1, 6, 2026), date(20, 6, 2026), now)
        } catch (_: IllegalArgumentException) {
            // expected
        }
        assertEquals(date(10, 6, 2026), lottery.lotteryStart)
    }

    // setPeriods

    @Test
    fun setPeriods_validDates_updatesAll() {
        lottery.setPeriods(date(2, 6, 2026), date(8, 6, 2026), date(9, 6, 2026), date(19, 6, 2026), now)
        assertEquals(date(2, 6, 2026), lottery.registrationStart)
        assertEquals(date(8, 6, 2026), lottery.registrationEnd)
        assertEquals(date(9, 6, 2026), lottery.lotteryStart)
        assertEquals(date(19, 6, 2026), lottery.lotteryEnd)
    }

    @Test(expected = IllegalArgumentException::class)
    fun setPeriods_startInPast_throws() {
        lottery.setPeriods(date(1, 6, 2026), date(9, 6, 2026), date(10, 6, 2026), date(20, 6, 2026), date(2, 6, 2026))
    }

    // updateNumWinners

    @Test
    fun updateNumWinners_one_isAccepted() {
        lottery.updateNumWinners(1)
        assertEquals(1, lottery.numWinners)
    }

    @Test(expected = IllegalArgumentException::class)
    fun updateNumWinners_zero_throws() {
        lottery.updateNumWinners(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun updateNumWinners_negative_throws() {
        lottery.updateNumWinners(-3)
    }

    @Test
    fun updateNumWinners_invalid_leavesOldValueUnchanged() {
        lottery.updateNumWinners(4)
        try {
            lottery.updateNumWinners(0)
        } catch (e: IllegalArgumentException) {
            // expected
        }
        assertEquals(4, lottery.numWinners)
    }

    // isActive

    @Test
    fun isActive_onlyDuringRegistration() {
        assertTrue(lottery.isActive(date(5, 6, 2026)))     // inside registration
        assertFalse(lottery.isActive(date(31, 5, 2026)))   // before registration
        assertFalse(lottery.isActive(date(15, 6, 2026)))   // lottery running, registration closed
        assertFalse(lottery.isActive(date(25, 6, 2026)))   // after everything
    }

    @Test
    fun isActive_boundariesAreInclusive() {
        assertTrue(lottery.isActive(date(1, 6, 2026)))
        assertTrue(lottery.isActive(date(9, 6, 2026)))
    }

    @Test
    fun isActive_registrationPeriodSet_usesRegistrationDates() {
        lottery.setRegistrationPeriod(date(2, 6, 2026), date(8, 6, 2026), now)
        // inside registration, before the lottery starts
        assertTrue(lottery.isActive(date(5, 6, 2026)))
        // inside the lottery, after registration closed
        assertFalse(lottery.isActive(date(15, 6, 2026)))
    }

    // hasEntrant / join

    @Test
    fun hasEntrant_newLottery_isFalse() {
        assertFalse(lottery.hasEntrant("user1"))
    }

    @Test
    fun join_newUser_returnsTrueAndAddsThem() {
        assertTrue(lottery.join("user1"))
        assertTrue(lottery.hasEntrant("user1"))
        assertEquals(1, lottery.entrants.size)
    }

    @Test
    fun join_sameUserTwice_returnsFalseAndDoesNotDuplicate() {
        lottery.join("user1")
        assertFalse(lottery.join("user1"))
        assertEquals(1, lottery.entrants.size)
    }

    @Test
    fun join_multipleUsers_allAdded() {
        lottery.join("user1")
        lottery.join("user2")
        assertTrue(lottery.hasEntrant("user1"))
        assertTrue(lottery.hasEntrant("user2"))
        assertEquals(2, lottery.entrants.size)
    }

    // drawWinners

    @Test(expected = IllegalStateException::class)
    fun drawWinners_registrationStillOpen_throws() {
        lottery.join("user1")
        lottery.drawWinners(date(5, 6, 2026))
    }

    @Test(expected = IllegalStateException::class)
    fun drawWinners_calledTwice_throws() {
        lottery.join("user1")
        lottery.drawWinners(afterClose)
        lottery.drawWinners(afterClose)
    }

    @Test
    fun drawWinners_moreEntrantsThanWinners_picksNumWinnersFromEntrants() {
        listOf("u1", "u2", "u3", "u4", "u5").forEach { lottery.join(it) }
        lottery.updateNumWinners(2)
        lottery.drawWinners(afterClose, Random(1))
        assertEquals(2, lottery.winners.size)
        assertEquals(2, lottery.winners.toSet().size)
        assertTrue(lottery.entrants.containsAll(lottery.winners))
    }

    @Test
    fun drawWinners_fewerEntrantsThanWinners_everyoneWins() {
        lottery.join("u1")
        lottery.join("u2")
        lottery.updateNumWinners(5)
        lottery.drawWinners(afterClose)
        assertEquals(setOf("u1", "u2"), lottery.winners.toSet())
    }

    @Test
    fun drawWinners_noEntrants_winnersEmpty() {
        lottery.drawWinners(afterClose)
        assertTrue(lottery.winners.isEmpty())
    }

    @Test
    fun drawWinners_sameSeed_sameWinners() {
        listOf("u1", "u2", "u3", "u4").forEach { lottery.join(it) }
        val other = Lottery(
            registrationEnd = date(9, 6, 2026),
            entrants = lottery.entrants.toMutableList()
        )
        lottery.drawWinners(afterClose, Random(42))
        other.drawWinners(afterClose, Random(42))
        assertEquals(lottery.winners, other.winners)
    }

    // hasCampsite / addCampsite

    @Test
    fun hasCampsite_listedCampsite_isTrue() {
        assertTrue(lottery.hasCampsite("camp1"))
    }

    @Test
    fun hasCampsite_unknownCampsite_isFalse() {
        assertFalse(lottery.hasCampsite("camp2"))
    }

    @Test
    fun addCampsite_newCampsite_returnsTrueAndAddsIt() {
        assertTrue(lottery.addCampsite("camp2"))
        assertTrue(lottery.hasCampsite("camp2"))
        assertEquals(2, lottery.campsiteIds.size)
    }

    @Test
    fun addCampsite_sameCampsiteTwice_returnsFalseAndDoesNotDuplicate() {
        assertFalse(lottery.addCampsite("camp1"))
        assertEquals(1, lottery.campsiteIds.size)
    }

    @Test
    fun addCampsite_multipleCampsites_allAdded() {
        lottery.addCampsite("camp2")
        lottery.addCampsite("camp3")
        assertTrue(lottery.hasCampsite("camp1"))
        assertTrue(lottery.hasCampsite("camp2"))
        assertTrue(lottery.hasCampsite("camp3"))
        assertEquals(3, lottery.campsiteIds.size)
    }
}