package com.example.campsiteapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
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

    @Test
    fun setLotteryPeriod_updatesWhenValid_rejectsAndKeepsOldDatesWhenInvalid() {
        lottery.setLotteryPeriod(
            date(1, 7, 2026),
            date(5, 7, 2026),
            now
        )
        assertEquals(date(1, 7, 2026), lottery.lotteryStart)
        assertEquals(date(5, 7, 2026), lottery.lotteryEnd)

        // start after end
        assertThrows(IllegalArgumentException::class.java) {
            lottery.setLotteryPeriod(date(5, 7, 2026),
                date(1, 7, 2026),
                now
            )
        }
        // start equals end
        assertThrows(IllegalArgumentException::class.java) {
            lottery.setLotteryPeriod(date(5, 7, 2026),
                date(5, 7, 2026),
                now
            )
        }
        // start in past
        assertThrows(IllegalArgumentException::class.java) {
            lottery.setLotteryPeriod(date(1, 7, 2026),
                date(5, 7, 2026),
                date(2, 7, 2026)
            )
        }
        // starts before registration ends
        assertThrows(IllegalArgumentException::class.java) {
            lottery.setLotteryPeriod(date(1, 6, 2026),
                date(20, 6, 2026),
                now
            )
        }

        assertEquals(date(1, 7, 2026), lottery.lotteryStart)
        assertEquals(date(5, 7, 2026), lottery.lotteryEnd)
    }

    @Test
    fun setRegistrationPeriod_updatesWhenValid_rejectsAndKeepsOldDatesWhenInvalid() {
        lottery.setRegistrationPeriod(date(2, 6, 2026),
            date(8, 6, 2026),
            now
        )
        assertEquals(date(2, 6, 2026), lottery.registrationStart)
        assertEquals(date(8, 6, 2026), lottery.registrationEnd)

        // ending exactly when the lottery starts is allowed
        lottery.setRegistrationPeriod(date(5, 6, 2026),
            date(10, 6, 2026),
            now
        )
        assertEquals(date(10, 6, 2026), lottery.registrationEnd)

        // start after end
        assertThrows(IllegalArgumentException::class.java) {
            lottery.setRegistrationPeriod(date(8, 6, 2026),
                date(2, 6, 2026),
                now
            )
        }
        // start after lottery start
        assertThrows(IllegalArgumentException::class.java) {
            lottery.setRegistrationPeriod(date(11, 6, 2026),
                date(15, 6, 2026),
                now
            )
        }
        // end after lottery end
        assertThrows(IllegalArgumentException::class.java) {
            lottery.setRegistrationPeriod(date(5, 6, 2026),
                date(21, 6, 2026),
                now
            )
        }
        // ends after lottery starts
        assertThrows(IllegalArgumentException::class.java) {
            lottery.setRegistrationPeriod(date(10, 6, 2026),
                date(20, 6, 2026),
                now
            )
        }
        // start in past
        assertThrows(IllegalArgumentException::class.java) {
            lottery.setRegistrationPeriod(date(2, 6, 2026),
                date(8, 6, 2026),
                date(3, 6, 2026)
            )
        }

        assertEquals(date(5, 6, 2026), lottery.registrationStart)
        assertEquals(date(10, 6, 2026), lottery.registrationEnd)
    }

    @Test
    fun setPeriods_updatesAllWhenValid_throwsWhenStartInPast() {
        lottery.setPeriods(date(2, 6, 2026),
            date(8, 6, 2026),
            date(9, 6, 2026),
            date(19, 6, 2026),
            now)
        assertEquals(date(2, 6, 2026), lottery.registrationStart)
        assertEquals(date(8, 6, 2026), lottery.registrationEnd)
        assertEquals(date(9, 6, 2026), lottery.lotteryStart)
        assertEquals(date(19, 6, 2026), lottery.lotteryEnd)

        assertThrows(IllegalArgumentException::class.java) {
            lottery.setPeriods(date(1, 6, 2026),
                date(9, 6, 2026),
                date(10, 6, 2026),
                date(20, 6, 2026),
                date(2, 6, 2026)
            )
        }
    }

    @Test
    fun updateNumWinners_acceptsPositive_rejectsZeroAndNegative() {
        lottery.updateNumWinners(1)
        assertEquals(1, lottery.numWinners)

        lottery.updateNumWinners(4)
        assertThrows(IllegalArgumentException::class.java) {
            lottery.updateNumWinners(0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            lottery.updateNumWinners(-3)
        }
        assertEquals(4, lottery.numWinners)
    }

    @Test
    fun isActive_onlyDuringRegistration_boundariesInclusive() {
        assertTrue(lottery.isActive(date(5, 6, 2026)))
        assertTrue(lottery.isActive(date(1, 6, 2026)))     // start boundary
        assertTrue(lottery.isActive(date(9, 6, 2026)))     // end boundary
        assertFalse(lottery.isActive(date(31, 5, 2026)))   // before registration
        assertFalse(lottery.isActive(date(15, 6, 2026)))   // lottery running
        assertFalse(lottery.isActive(date(25, 6, 2026)))   // after everything

        // follows the registration dates once they change
        lottery.setRegistrationPeriod(date(2, 6, 2026),
            date(8, 6, 2026),
            now
        )
        assertFalse(lottery.isActive(date(1, 6, 2026)))
        assertTrue(lottery.isActive(date(5, 6, 2026)))
        assertFalse(lottery.isActive(date(9, 6, 2026)))
    }

    @Test
    fun join_addsNewUsers_andRejectsDuplicates() {
        assertFalse(lottery.hasEntrant("user1"))
        assertTrue(lottery.join("user1"))
        assertTrue(lottery.join("user2"))
        assertFalse(lottery.join("user1"))
        assertTrue(lottery.hasEntrant("user1"))
        assertTrue(lottery.hasEntrant("user2"))
        assertEquals(2, lottery.entrants.size)
    }

    @Test
    fun addCampsite_addsNewCampsites_andRejectsDuplicates() {
        assertTrue(lottery.hasCampsite("camp1"))
        assertFalse(lottery.hasCampsite("camp2"))
        assertTrue(lottery.addCampsite("camp2"))
        assertTrue(lottery.addCampsite("camp3"))
        assertFalse(lottery.addCampsite("camp1"))
        assertTrue(lottery.hasCampsite("camp3"))
        assertEquals(3, lottery.campsiteIds.size)
    }

    @Test
    fun drawWinners_throwsWhileRegistrationOpenOrIfAlreadyDrawn() {
        lottery.join("user1")
        assertThrows(IllegalStateException::class.java)
        {
            lottery.drawWinners(date(5, 6, 2026))
        }
        lottery.drawWinners(afterClose)
        assertThrows(IllegalStateException::class.java) {
            lottery.drawWinners(afterClose)
        }
    }

    @Test
    fun drawWinners_picksCorrectWinners() {
        // more entrants than winners: exactly numWinners distinct entrants
        listOf("u1", "u2", "u3", "u4", "u5").forEach { lottery.join(it) }
        lottery.updateNumWinners(2)
        lottery.drawWinners(afterClose, Random(1))
        assertEquals(2, lottery.winners.toSet().size)
        assertEquals(2, lottery.winners.size)
        assertTrue(lottery.entrants.containsAll(lottery.winners))

        // same seed gives same winners
        val other = Lottery(registrationEnd = date(9, 6, 2026),
            entrants = mutableListOf("u1", "u2", "u3", "u4", "u5")
        )
        other.updateNumWinners(2)
        other.drawWinners(afterClose, Random(1))
        assertEquals(lottery.winners, other.winners)

        // fewer entrants than winners: everyone wins
        val few = Lottery(registrationEnd = date(9, 6, 2026),
            entrants = mutableListOf("u1", "u2")
        )
        few.updateNumWinners(5)
        few.drawWinners(afterClose)
        assertEquals(setOf("u1", "u2"), few.winners.toSet())

        // no entrants: no winners
        val none = Lottery(registrationEnd = date(9, 6, 2026))
        none.drawWinners(afterClose)
        assertTrue(none.winners.isEmpty())
    }
}