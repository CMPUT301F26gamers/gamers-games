package com.example.campsiteapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class LotteryTest {

    /** Builds a Date for the given day, month (1-12) and year. */
    private fun date(day: Int, month: Int, year: Int): Date =
        Date.from(
            LocalDate.of(year, month, day)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
        )

    private val lottery = Lottery(
        campsiteId = "camp1",
        lotteryStart = date(10, 6, 2026),
        lotteryEnd = date(20, 6, 2026)
    )

    // setLotteryPeriod

    @Test
    fun setLotteryPeriod_validDates_updatesBoth() {
        lottery.setLotteryPeriod(date(1, 7, 2026), date(5, 7, 2026))
        assertEquals(date(1, 7, 2026), lottery.lotteryStart)
        assertEquals(date(5, 7, 2026), lottery.lotteryEnd)
    }

    @Test(expected = IllegalArgumentException::class)
    fun setLotteryPeriod_startAfterEnd_throws() {
        lottery.setLotteryPeriod(date(5, 7, 2026), date(1, 7, 2026))
    }

    @Test(expected = IllegalArgumentException::class)
    fun setLotteryPeriod_startEqualsEnd_throws() {
        lottery.setLotteryPeriod(date(5, 7, 2026), date(5, 7, 2026))
    }

    @Test
    fun setLotteryPeriod_invalid_leavesOldDatesUnchanged() {
        try {
            lottery.setLotteryPeriod(date(5, 7, 2026), date(1, 7, 2026))
        } catch (e: IllegalArgumentException) {
            // expected
        }
        assertEquals(date(10, 6, 2026), lottery.lotteryStart)
        assertEquals(date(20, 6, 2026), lottery.lotteryEnd)
    }

    // setRegistrationPeriod

    @Test
    fun setRegistrationPeriod_validDates_updatesBoth() {
        lottery.setRegistrationPeriod(date(2, 6, 2026), date(8, 6, 2026))
        assertEquals(date(2, 6, 2026), lottery.registrationStart)
        assertEquals(date(8, 6, 2026), lottery.registrationEnd)
    }

    @Test(expected = IllegalArgumentException::class)
    fun setRegistrationPeriod_startAfterEnd_throws() {
        lottery.setRegistrationPeriod(date(8, 6, 2026), date(2, 6, 2026))
    }

    @Test
    fun setRegistrationPeriod_invalid_leavesDatesNull() {
        try {
            lottery.setRegistrationPeriod(date(8, 6, 2026), date(2, 6, 2026))
        } catch (e: IllegalArgumentException) {
            // expected
        }
        assertNull(lottery.registrationStart)
        assertNull(lottery.registrationEnd)
    }

    @Test(expected = IllegalArgumentException::class)
    fun setRegistrationPeriod_startAfterLotteryStart_throws() {
        lottery.setRegistrationPeriod(date(11, 6, 2026), date(15, 6, 2026))
    }

    @Test(expected = IllegalArgumentException::class)
    fun setRegistrationPeriod_endAfterLotteryEnd_throws() {
        lottery.setRegistrationPeriod(date(5, 6, 2026), date(21, 6, 2026))
    }

    @Test
    fun setRegistrationPeriod_sameDatesAsLottery_isAccepted() {
        lottery.setRegistrationPeriod(date(10, 6, 2026), date(20, 6, 2026))
        assertEquals(date(10, 6, 2026), lottery.registrationStart)
    }

    @Test(expected = IllegalArgumentException::class)
    fun setLotteryPeriod_registrationWouldStartAfterLottery_throws() {
        lottery.setRegistrationPeriod(date(2, 6, 2026), date(8, 6, 2026))
        lottery.setLotteryPeriod(date(1, 6, 2026), date(20, 6, 2026))  // lottery now starts before registration
    }

    @Test
    fun setLotteryPeriod_registrationOutside_leavesOldDatesUnchanged() {
        lottery.setRegistrationPeriod(date(2, 6, 2026), date(8, 6, 2026))
        try {
            lottery.setLotteryPeriod(date(1, 6, 2026), date(20, 6, 2026))
        } catch (_: IllegalArgumentException) {
            // expected
        }
        assertEquals(date(10, 6, 2026), lottery.lotteryStart)
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
    fun isActive_noRegistrationPeriod_usesLotteryDates() {
        assertTrue(lottery.isActive(date(15, 6, 2026)))
        assertFalse(lottery.isActive(date(5, 6, 2026)))
        assertFalse(lottery.isActive(date(25, 6, 2026)))
    }

    @Test
    fun isActive_boundariesAreInclusive() {
        assertTrue(lottery.isActive(date(10, 6, 2026)))
        assertTrue(lottery.isActive(date(20, 6, 2026)))
    }

    @Test
    fun isActive_registrationPeriodSet_usesRegistrationDates() {
        lottery.setRegistrationPeriod(date(2, 6, 2026), date(8, 6, 2026))
        // inside registration, before the lottery starts
        assertTrue(lottery.isActive(date(5, 6, 2026)))
        // inside the lottery, after registration closed
        assertFalse(lottery.isActive(date(15, 6, 2026)))
    }

    @Test
    fun isActive_onlyOneRegistrationDateSet_fallsBackToLotteryDates() {
        lottery.registrationStart = date(2, 6, 2026)   // end left null
        assertFalse(lottery.isActive(date(5, 6, 2026)))
        assertTrue(lottery.isActive(date(15, 6, 2026)))
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
}