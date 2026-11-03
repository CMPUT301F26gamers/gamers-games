package com.example.campsiteapp

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class LotteryListTest {

    private val list = LotteryList()

    private fun date(day: Int, month: Int, year: Int): Date =
        Date.from(
            LocalDate.of(year, month, day)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
        )

    private val beforeReg = date(1, 1, 2099)
    private val inReg = date(5, 6, 2099)
    private val afterReg = date(10, 6, 2099)

    private fun campsite(id: String) = Campsite(id) // adjust to your constructor

    private fun create(ids: List<String>, capacity: Int? = null) =
        list.createLottery(
            campsiteIds = ids,
            regStart = date(1, 6, 2099), regEnd = date(9, 6, 2099),
            bookStart = date(10, 6, 2099), bookEnd = date(19, 6, 2099),
            start = date(20, 6, 2099), end = date(25, 6, 2099),
            numWinners = 1,
            waitlistCapacity = capacity
        )

    @Test
    fun createLotteryAddsToList() {
        val lottery = create(listOf("camp1", "camp2"))
        assertEquals(1, list.allLotteries().size)
        assertEquals(lottery, list.findLottery(lottery.id))
    }

    @Test(expected = IllegalArgumentException::class)
    fun createLotteryWithNoCampsitesThrows() {
        create(emptyList())
    }

    @Test(expected = IllegalArgumentException::class)
    fun createLotteryWithDuplicateCampsitesThrows() {
        create(listOf("camp1", "camp1"))
    }

    @Test
    fun getLotteriesFiltersByCampsite() {
        create(listOf("camp1"))
        create(listOf("camp1", "camp2"))
        assertEquals(2, list.getLotteries("camp1").size)
        assertEquals(1, list.getLotteries("camp2").size)
        assertTrue(list.getLotteries("camp3").isEmpty())
    }

    @Test
    fun getActiveLotteryOnlyDuringRegistration() {
        val lottery = create(listOf("camp1"))
        assertEquals(lottery, list.getActiveLottery("camp1", inReg))
        assertNull(list.getActiveLottery("camp1", beforeReg))
        assertNull(list.getActiveLottery("camp1", afterReg))
        assertNull(list.getActiveLottery("camp2", inReg))
    }

    @Test
    fun allActiveLotteriesRespectsTime() {
        create(listOf("camp1"))
        assertEquals(1, list.allActiveLotteries(inReg).size)
        assertTrue(list.allActiveLotteries(beforeReg).isEmpty())
        assertTrue(list.allActiveLotteries(afterReg).isEmpty())
    }

    @Test
    fun removeCampsiteKeepsLotteryIfNotEmpty() {
        val lottery = create(listOf("camp1", "camp2"))
        assertTrue(list.removeCampsite(lottery.id, "camp1"))
        assertNotNull(list.findLottery(lottery.id))
    }

    @Test
    fun removeLastCampsiteRemovesLottery() {
        val lottery = create(listOf("camp1"))
        assertTrue(list.removeCampsite(lottery.id, "camp1"))
        assertNull(list.findLottery(lottery.id))
    }

    @Test
    fun removeCampsiteReturnsFalseForUnknowns() {
        val lottery = create(listOf("camp1"))
        assertFalse(list.removeCampsite(lottery.id, "nope"))
        assertFalse(list.removeCampsite("badId", "camp1"))
    }

    @Test
    fun removeLottery() {
        val lottery = create(listOf("camp1"))
        assertTrue(list.removeLottery(lottery.id))
        assertFalse(list.removeLottery(lottery.id))
    }

    @Test
    fun enterJoinsWaitlist() {
        create(listOf("camp1"))
        assertEquals(EntryResult.JOINED_WAITLIST, list.enter(campsite("camp1"), "u1", inReg))
    }

    @Test
    fun enterTwiceIsAlreadyOnWaitlist() {
        create(listOf("camp1"))
        list.enter(campsite("camp1"), "u1", inReg)
        assertEquals(EntryResult.ALREADY_ON_WAITLIST, list.enter(campsite("camp1"), "u1", inReg))
    }

    @Test
    fun enterFullWaitlist() {
        create(listOf("camp1"), capacity = 1)
        list.enter(campsite("camp1"), "u1", inReg)
        assertEquals(EntryResult.WAITLIST_FULL, list.enter(campsite("camp1"), "u2", inReg))
    }

    @Test
    fun enterWhenNotOpen() {
        create(listOf("camp1"))
        assertEquals(EntryResult.NOT_OPEN, list.enter(campsite("camp1"), "u1", beforeReg))
        assertEquals(EntryResult.NOT_OPEN, list.enter(campsite("camp1"), "u1", afterReg))
        assertEquals(EntryResult.NOT_OPEN, list.enter(campsite("camp2"), "u1", inReg))
    }

    @Test
    fun leaveRemovesEntrant() {
        create(listOf("camp1"))
        list.enter(campsite("camp1"), "u1", inReg)
        assertTrue(list.leave(campsite("camp1"), "u1", inReg))
        assertFalse(list.leave(campsite("camp1"), "u1", inReg))
    }

    @Test
    fun leaveWhenNotOpenReturnsFalse() {
        create(listOf("camp1"))
        assertFalse(list.leave(campsite("camp1"), "u1", afterReg))
    }
}