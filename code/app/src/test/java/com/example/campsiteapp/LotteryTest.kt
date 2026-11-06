package com.example.campsiteapp

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class LotteryTest {

    private fun date(day: Int, month: Int, year: Int): Date =
        Date.from(
            LocalDate.of(year, month, day)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
        )

    private val now = date(1, 1, 2026)
    private val afterClose = date(10, 6, 2026)

    private fun lottery() = Lottery(
        campsiteIds = mutableListOf("camp1"),
        registrationStart = date(1, 6, 2026),
        registrationEnd = date(9, 6, 2026),
        bookingStart = date(10, 6, 2026),
        bookingEnd = date(19, 6, 2026),
        lotteryStart = date(20, 6, 2026),
        lotteryEnd = date(25, 6, 2026)
    )

    @Test
    fun setLotteryPeriod_updatesAndRejectsInvalidDates() {
        val l = lottery()

        l.setLotteryPeriod(date(26, 6, 2026), date(30, 6, 2026), now)
        assertEquals(date(26, 6, 2026), l.lotteryStart)

        assertThrows(IllegalArgumentException::class.java) {
            l.setLotteryPeriod(date(30, 6, 2026), date(26, 6, 2026), now)
        }
        assertThrows(IllegalArgumentException::class.java) {
            l.setLotteryPeriod(date(26, 6, 2026), date(26, 6, 2026), now)
        }
        assertThrows(IllegalArgumentException::class.java) {
            l.setLotteryPeriod(date(1, 1, 2026), date(5, 1, 2026), now)
        }
        assertThrows(IllegalArgumentException::class.java) {
            l.setLotteryPeriod(date(18, 6, 2026), date(25, 6, 2026), now)
        }
    }

    @Test
    fun setRegistrationPeriod_updatesAndRejectsInvalidDates() {
        val l = lottery()

        l.setRegistrationPeriod(date(2, 6, 2026), date(8, 6, 2026), now)
        assertEquals(date(2, 6, 2026), l.registrationStart)

        assertThrows(IllegalArgumentException::class.java) {
            l.setRegistrationPeriod(date(8, 6, 2026), date(2, 6, 2026), now)
        }
        assertThrows(IllegalArgumentException::class.java) {
            l.setRegistrationPeriod(date(2, 6, 2026), date(11, 6, 2026), now)
        }
        assertThrows(IllegalArgumentException::class.java) {
            l.setRegistrationPeriod(
                date(1, 6, 2026),
                date(11, 6, 2026),
                now
            )
        }
    }

    @Test
    fun setBookingPeriod_updatesAndRejectsInvalidDates() {
        val l = lottery()

        l.setBookingPeriod(date(10, 6, 2026), date(19, 6, 2026))
        assertEquals(date(10, 6, 2026), l.bookingStart)

        assertThrows(IllegalArgumentException::class.java) {
            l.setBookingPeriod(date(19, 6, 2026), date(10, 6, 2026))
        }
        assertThrows(IllegalArgumentException::class.java) {
            l.setBookingPeriod(date(8, 6, 2026), date(15, 6, 2026))
        }
        assertThrows(IllegalArgumentException::class.java) {
            l.setBookingPeriod(date(10, 6, 2026), date(21, 6, 2026))
        }
    }

    @Test
    fun setPeriods_updatesAllPeriods() {
        val l = lottery()

        l.setPeriods(
            date(2, 6, 2026), date(8, 6, 2026),
            date(8, 6, 2026), date(19, 6, 2026),
            date(20, 6, 2026), date(25, 6, 2026), now
        )

        assertEquals(date(2, 6, 2026), l.registrationStart)
        assertEquals(date(8, 6, 2026), l.registrationEnd)
        assertEquals(date(8, 6, 2026), l.bookingStart)
        assertEquals(date(19, 6, 2026), l.bookingEnd)
        assertEquals(date(20, 6, 2026), l.lotteryStart)
        assertEquals(date(25, 6, 2026), l.lotteryEnd)
    }

    @Test
    fun updateNumWinners_validatesInput() {
        val l = lottery()

        l.updateNumWinners(4)
        assertEquals(4, l.numWinners)

        assertThrows(IllegalArgumentException::class.java) {
            l.updateNumWinners(0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            l.updateNumWinners(-1)
        }
    }

    @Test
    fun updateWaitlistCapacity_validatesInput() {
        val l = lottery()

        l.updateWaitlistCapacity(2)
        assertEquals(2, l.waitlistCapacity)

        assertThrows(IllegalArgumentException::class.java) {
            l.updateWaitlistCapacity(0)
        }

        l.updateWaitlistCapacity(null)
        assertEquals(null, l.waitlistCapacity)
    }

    @Test
    fun isActive_checksRegistrationPeriod() {
        val l = lottery()

        assertTrue(l.isActive(date(1, 6, 2026)))
        assertTrue(l.isActive(date(5, 6, 2026)))
        assertTrue(l.isActive(date(9, 6, 2026)))
        assertFalse(l.isActive(date(31, 5, 2026)))
        assertFalse(l.isActive(date(10, 6, 2026)))
    }

    @Test
    fun canJoinAndCanLeave_andCapacity_workCorrectly() {
        val l = lottery()
        l.updateWaitlistCapacity(2)

        assertTrue(l.canJoin("user1"))
        l.waitlist.add("user1")
        assertTrue(l.canJoin("user2"))
        l.waitlist.add("user2")

        assertFalse(l.canJoin("user1"))
        assertFalse(l.canJoin("user3"))
        assertTrue(l.isFull())

        assertTrue(l.canLeave("user1"))
        l.waitlist.remove("user1")
        assertFalse(l.isFull())
        assertEquals(1, l.waitlist.size)
    }

    @Test
    fun addCampsite_rejectsDuplicates() {
        val l = lottery()

        assertTrue(l.hasCampsite("camp1"))
        assertTrue(l.addCampsite("camp2"))
        assertFalse(l.addCampsite("camp2"))
        assertEquals(2, l.campsiteIds.size)
    }

    @Test
    fun drawWinners_drawsCorrectNumberAndIsReproducible() {
        val l = lottery()
        val other = lottery()

        listOf("u1", "u2", "u3", "u4").forEach {
            l.waitlist.add(it)
            other.waitlist.add(it)
        }

        assertThrows(IllegalStateException::class.java) {
            l.drawWinners(2, 1L, date(5, 6, 2026))
        }

        val drawn = l.drawWinners(2, 1L, afterClose)
        val otherDraw = other.drawWinners(2, 1L, afterClose)

        assertEquals(2, drawn.size)
        assertEquals(drawn, otherDraw)
        assertEquals(2, l.winners.size)
    }

    @Test
    fun drawWinners_canDrawAgainWithoutDuplicates() {
        val l = lottery()

        listOf("u1", "u2", "u3", "u4").forEach { l.waitlist.add(it) }

        val first = l.drawWinners(2, 1L, afterClose)
        val second = l.drawWinners(2, 2L, afterClose)

        assertEquals(2, second.size)
        assertTrue(first.toSet().intersect(second.toSet()).isEmpty())
    }

    @Test
    fun bookAndDecline_moveWinnersCorrectly() {
        val l = lottery()

        l.waitlist.add("u1")
        l.waitlist.add("u2")
        l.drawWinners(2, 1L, afterClose)

        val bookingDate = date(15, 6, 2026)

        assertTrue(l.book("u1", bookingDate))
        assertTrue(l.bookings.contains("u1"))
        assertFalse(l.winners.contains("u1"))

        assertTrue(l.decline("u2", bookingDate))
        assertTrue(l.declined.contains("u2"))
        assertFalse(l.winners.contains("u2"))

        assertTrue(l.hasBeenDrawn("u1"))
        assertTrue(l.hasBeenDrawn("u2"))
    }

    @Test
    fun canLeave_rejectsAlreadyDrawnUsers() {
        val l = lottery()

        l.waitlist.add("u1")
        l.drawWinners(1, 1L, afterClose)

        assertFalse(l.canLeave("u1"))
        assertTrue(l.hasEntrant("u1"))
    }

    @Test
    fun isDrawDue_onlyAfterRegistrationEndsAndNotDrawn() {
        val l = lottery()

        assertFalse(l.isDrawDue(date(5, 6, 2026)))
        assertFalse(l.isDrawDue(date(9, 6, 2026)))
        assertTrue(l.isDrawDue(afterClose))

        l.drawn = true
        assertFalse(l.isDrawDue(afterClose))
    }

    @Test
    fun drawAutomatically_drawsNumWinnersAfterRegistrationEnds() {
        val l = lottery()
        listOf("u1", "u2", "u3", "u4").forEach { l.waitlist.add(it) }
        l.updateNumWinners(2)

        assertTrue(l.drawAutomatically(date(5, 6, 2026)).isEmpty())
        assertFalse(l.drawn)

        val drawn = l.drawAutomatically(afterClose)

        assertEquals(2, drawn.size)
        assertEquals(2, l.winners.size)
        assertTrue(l.drawn)
    }

    @Test
    fun drawAutomatically_onlyDrawsOnce() {
        val l = lottery()
        listOf("u1", "u2", "u3", "u4").forEach { l.waitlist.add(it) }

        l.drawAutomatically(afterClose)
        val second = l.drawAutomatically(date(11, 6, 2026))

        assertTrue(second.isEmpty())
        assertEquals(1, l.winners.size)
    }

    @Test
    fun drawAutomatically_everyoneWinsIfFewerThanNumWinners() {
        val l = lottery()
        l.waitlist.add("u1")
        l.updateNumWinners(3)

        val drawn = l.drawAutomatically(afterClose)

        assertEquals(listOf("u1"), drawn)
        assertTrue(l.drawn)
    }

    @Test
    fun drawAutomatically_emptyWaitlistStillMarksDrawn() {
        val l = lottery()

        assertTrue(l.drawAutomatically(afterClose).isEmpty())
        assertTrue(l.drawn)
        assertFalse(l.isDrawDue(afterClose))
    }

    @Test
    fun drawAutomatically_sameLotteryGivesSameWinners() {
        val l = Lottery(id = "l1", registrationEnd = date(9, 6, 2026))
        val sameOnOtherDevice = Lottery(id = "l1", registrationEnd = date(9, 6, 2026))
        listOf("u1", "u2", "u3", "u4", "u5").forEach {
            l.waitlist.add(it)
            sameOnOtherDevice.waitlist.add(it)
        }
        l.updateNumWinners(2)
        sameOnOtherDevice.updateNumWinners(2)

        assertEquals(l.drawAutomatically(afterClose), sameOnOtherDevice.drawAutomatically(date(12, 6, 2026)))
    }

    @Test
    fun entrantStatus_pendingThenWonOrNotSelected() {
        val l = lottery()
        l.waitlist.add("u1")
        l.waitlist.add("u2")

        assertEquals("Pending", l.entrantStatus("u1"))

        val winner = l.drawAutomatically(afterClose).first()
        val loser = if (winner == "u1") "u2" else "u1"

        assertEquals("Won", l.entrantStatus(winner))
        assertEquals("Not selected", l.entrantStatus(loser))
    }

    @Test
    fun entrantStatus_bookedAndDeclinedWinnersStillShowWon() {
        val l = lottery()
        l.waitlist.add("u1")
        l.updateNumWinners(1)
        l.drawAutomatically(afterClose)

        assertTrue(l.book("u1", date(15, 6, 2026)))
        assertEquals("Won", l.entrantStatus("u1"))
    }
}