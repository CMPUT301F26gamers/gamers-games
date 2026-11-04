package com.example.campsiteapp

import com.google.firebase.firestore.CollectionReference
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class LotteryRepositoryTest {

    private val ref = mockk<CollectionReference>(relaxed = true)
    private val repo = LotteryRepository(ref)

    private fun date(day: Int, month: Int, year: Int): Date =
        Date.from(
            LocalDate.of(year, month, day).
            atStartOfDay(ZoneId.systemDefault())
                .toInstant()
        )

    private val beforeReg = date(1, 1, 2099)
    private val inReg = date(5, 6, 2099)
    private val afterReg = date(10, 6, 2099)

    private fun campsite(id: String) = Campsite(id)

    private fun addLottery(id: String, ids: List<String>, capacity: Int? = null): Lottery {
        val l = Lottery(id = id, campsiteIds = ids.toMutableList())
        l.setPeriods(
            date(1, 6, 2099), date(9, 6, 2099),
            date(10, 6, 2099), date(19, 6, 2099),
            date(20, 6, 2099), date(25, 6, 2099)
        )
        l.updateWaitlistCapacity(capacity)
        repo.lotteries.add(l)
        return l
    }

    private fun create(ids: List<String>) = repo.createLottery(
        campsiteIds = ids,
        regStart = date(1, 6, 2099), regEnd = date(9, 6, 2099),
        bookStart = date(10, 6, 2099), bookEnd = date(19, 6, 2099),
        start = date(20, 6, 2099), end = date(25, 6, 2099),
        onDone = {}
    )

    // createLottery

    @Test(expected = IllegalArgumentException::class)
    fun createLotteryWithNoCampsitesThrows() = create(emptyList())

    @Test(expected = IllegalArgumentException::class)
    fun createLotteryWithDuplicateCampsitesThrows() = create(listOf("camp1", "camp1"))

    @Test
    fun createLotterySavesToFirestore() {
        create(listOf("camp1"))
        verify { ref.document(any()) }
    }

    @Test
    fun invalidLotteryNeverReachesFirestore() {
        try { create(emptyList()) } catch (_: IllegalArgumentException) {}
        verify(exactly = 0) { ref.document(any()) }
    }

    // queries

    @Test
    fun getLotteriesFiltersByCampsite() {
        addLottery("l1", listOf("camp1"))
        addLottery("l2", listOf("camp1", "camp2"))
        assertEquals(2, repo.getLotteries("camp1").size)
        assertEquals(1, repo.getLotteries("camp2").size)
        assertTrue(repo.getLotteries("camp3").isEmpty())
    }

    @Test
    fun getActiveLotteryOnlyDuringRegistration() {
        val l = addLottery("l1", listOf("camp1"))
        assertEquals(l, repo.getActiveLottery("camp1", inReg))
        assertNull(repo.getActiveLottery("camp1", beforeReg))
        assertNull(repo.getActiveLottery("camp1", afterReg))
        assertNull(repo.getActiveLottery("camp2", inReg))
    }

    @Test
    fun allActiveLotteriesRespectsTime() {
        addLottery("l1", listOf("camp1"))
        assertEquals(1, repo.allActiveLotteries(inReg).size)
        assertTrue(repo.allActiveLotteries(beforeReg).isEmpty())
    }

    // joinState (replaces the old EntryResult tests)

    @Test
    fun joinStateCanJoin() {
        addLottery("l1", listOf("camp1"))
        assertEquals(JoinState.CAN_JOIN,
            repo.joinState(campsite("camp1"),
                "u1", inReg)
        )
    }

    @Test
    fun joinStateNotOpen() {
        addLottery("l1", listOf("camp1"))
        assertEquals(JoinState.NOT_OPEN,
            repo.joinState(campsite("camp1"),
                "u1", beforeReg)
        )
        assertEquals(JoinState.NOT_OPEN,
            repo.joinState(campsite("camp1"),
                "u1",
                afterReg)
        )
        assertEquals(JoinState.NOT_OPEN,
            repo.joinState(campsite("camp2"),
                "u1",
                inReg)
        )
    }

    @Test
    fun joinStateOnWaitlistAndFull() {
        val l = addLottery("l1", listOf("camp1"), capacity = 1)
        l.waitlist.add("u1")
        assertEquals(JoinState.ON_WAITLIST,
            repo.joinState(campsite("camp1"),
                "u1",
                inReg)
        )
        assertEquals(JoinState.WAITLIST_FULL,
            repo.joinState(campsite("camp1"),
                "u2",
                inReg)
        )
    }

    // enter / leave

    @Test
    fun enterWritesToFirestore() {
        addLottery("l1", listOf("camp1"))
        repo.enter(campsite("camp1"), "u1", inReg)
        verify { ref.document("l1") }
    }

    @Test
    fun enterRefusedWhenNotOpenOrFull() {
        val l = addLottery("l1", listOf("camp1"), capacity = 1)
        l.waitlist.add("u1")
        var result: Boolean? = null
        repo.enter(campsite("camp1"), "u2", inReg) { result = it }   // full
        assertEquals(false, result)
        repo.enter(campsite("camp1"), "u2", beforeReg) { result = it } // closed
        assertEquals(false, result)
        verify(exactly = 0) { ref.document(any()) }
    }

    @Test
    fun leaveRefusedWhenNotOnWaitlist() {
        addLottery("l1", listOf("camp1"))
        var result: Boolean? = null
        repo.leave(campsite("camp1"), "u1", inReg) { result = it }
        assertEquals(false, result)
        verify(exactly = 0) { ref.document(any()) }
    }

    @Test
    fun leaveWritesToFirestoreWhenOnWaitlist() {
        val l = addLottery("l1", listOf("camp1"))
        l.waitlist.add("u1")
        repo.leave(campsite("camp1"), "u1", inReg)
        verify { ref.document("l1") }
    }

    // removeCampsite / removeLottery

    @Test
    fun removeCampsiteKeepsLotteryIfNotEmpty() {
        addLottery("l1", listOf("camp1", "camp2"))
        repo.removeCampsite("l1", "camp1")
        assertEquals(
            listOf("camp2"),
            repo.findLottery("l1")!!.campsiteIds
        )
        verify { ref.document("l1") }
    }

    @Test
    fun removeCampsiteReturnsFalseForUnknowns() {
        addLottery("l1", listOf("camp1"))
        var result: Boolean? = null
        repo.removeCampsite("l1", "nope") { result = it }
        assertEquals(false, result)
        repo.removeCampsite("badId", "camp1") { result = it }
        assertEquals(false, result)
    }

    @Test
    fun removeLotteryUnknownReturnsFalse() {
        var result: Boolean? = null
        repo.removeLottery("badId") { result = it }
        assertEquals(false, result)
    }
}