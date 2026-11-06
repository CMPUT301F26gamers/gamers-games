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
            LocalDate.of(year, month, day)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
        )

    private val beforeReg = date(1, 1, 2099)
    private val inReg = date(5, 6, 2099)
    private val afterReg = date(10, 6, 2099)

    private fun addLottery(
        id: String,
        ids: List<String>,
        capacity: Int? = null,
        ownerId: String = "owner1"
    ): Lottery {
        val l = Lottery(id = id, ownerId = ownerId, campsiteIds = ids.toMutableList())
        l.setPeriods(
            date(1, 6, 2099), date(9, 6, 2099),
            date(10, 6, 2099), date(19, 6, 2099),
            date(20, 6, 2099), date(25, 6, 2099)
        )
        l.updateWaitlistCapacity(capacity)
        repo.lotteries.add(l)
        return l
    }

    private fun create(ids: List<String>, ownerId: String = "owner1") = repo.createLottery(
        ownerId = ownerId,
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

    @Test(expected = IllegalArgumentException::class)
    fun createLotteryWithBlankOwnerThrows() = create(listOf("camp1"), ownerId = "")

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
    fun getLotteriesByOwnerFiltersByOwner() {
        addLottery("l1", listOf("camp1"), ownerId = "ownerA")
        addLottery("l2", listOf("camp2"), ownerId = "ownerA")
        addLottery("l3", listOf("camp3"), ownerId = "ownerB")
        assertEquals(2, repo.getLotteriesByOwner("ownerA").size)
        assertEquals(1, repo.getLotteriesByOwner("ownerB").size)
        assertTrue(repo.getLotteriesByOwner("nobody").isEmpty())
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

    // joinState

    @Test
    fun joinStateCanJoin() {
        val l = addLottery("l1", listOf("camp1"))
        assertEquals(JoinState.CAN_JOIN, repo.joinState(l, "u1", inReg))
    }

    @Test
    fun joinStateNotOpen() {
        val l = addLottery("l1", listOf("camp1"))
        assertEquals(JoinState.NOT_OPEN, repo.joinState(l, "u1", beforeReg))
        assertEquals(JoinState.NOT_OPEN, repo.joinState(l, "u1", afterReg))
    }

    @Test
    fun joinStateOnWaitlistAndFull() {
        val l = addLottery("l1", listOf("camp1"), capacity = 1)
        l.waitlist.add("u1")
        assertEquals(JoinState.ON_WAITLIST, repo.joinState(l, "u1", inReg))
        assertEquals(JoinState.WAITLIST_FULL, repo.joinState(l, "u2", inReg))
    }

    // enter / leave

    @Test
    fun enterWritesToFirestore() {
        val l = addLottery("l1", listOf("camp1"))
        repo.enter(l, "u1", inReg)
        verify { ref.document("l1") }
    }

    @Test
    fun enterRefusedWhenNotOpenOrFull() {
        val l = addLottery("l1", listOf("camp1"), capacity = 1)
        l.waitlist.add("u1")
        var result: Boolean? = null
        repo.enter(l, "u2", inReg) { result = it }      // full
        assertEquals(false, result)
        repo.enter(l, "u2", beforeReg) { result = it }  // closed
        assertEquals(false, result)
        verify(exactly = 0) { ref.document(any()) }
    }

    @Test
    fun leaveRefusedWhenNotOnWaitlist() {
        val l = addLottery("l1", listOf("camp1"))
        var result: Boolean? = null
        repo.leave(l, "u1") { result = it }
        assertEquals(false, result)
        verify(exactly = 0) { ref.document(any()) }
    }

    @Test
    fun leaveWritesToFirestoreWhenOnWaitlist() {
        val l = addLottery("l1", listOf("camp1"))
        l.waitlist.add("u1")
        repo.leave(l, "u1")
        verify { ref.document("l1") }
    }

    // removeCampsite / removeLottery

    @Test
    fun removeCampsiteKeepsLotteryIfNotEmpty() {
        addLottery("l1", listOf("camp1", "camp2"))
        repo.removeCampsite("l1", "camp1")
        assertEquals(listOf("camp2"), repo.findLottery("l1")!!.campsiteIds)
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

    @Test
    fun drawDueLotteriesDrawsAndSavesClosedLottery() {
        val l = addLottery("l1", listOf("camp1"))
        l.waitlist.addAll(listOf("u1", "u2", "u3"))
        l.updateNumWinners(2)

        repo.drawDueLotteries(afterReg)

        assertTrue(l.drawn)
        assertEquals(2, l.winners.size)
        verify { ref.document("l1").set(l) }
    }

    @Test
    fun drawDueLotteriesSkipsLotteryStillInRegistration() {
        val l = addLottery("l1", listOf("camp1"))
        l.waitlist.add("u1")

        repo.drawDueLotteries(inReg)

        assertFalse(l.drawn)
        assertTrue(l.winners.isEmpty())
        verify(exactly = 0) { ref.document("l1") }
    }

    @Test
    fun drawDueLotteriesSkipsAlreadyDrawnLottery() {
        val l = addLottery("l1", listOf("camp1"))
        l.waitlist.add("u1")
        l.drawn = true

        repo.drawDueLotteries(afterReg)

        assertTrue(l.winners.isEmpty())
        verify(exactly = 0) { ref.document("l1") }
    }

    @Test
    fun drawDueLotteriesOnlyDrawsTheClosedOnes() {
        val closed = addLottery("l1", listOf("camp1"))
        closed.waitlist.add("u1")
        val drawnAlready = addLottery("l2", listOf("camp2"))
        drawnAlready.waitlist.add("u2")
        drawnAlready.drawn = true

        repo.drawDueLotteries(afterReg)

        assertEquals(listOf("u1"), closed.winners)
        assertTrue(drawnAlready.winners.isEmpty())
        verify { ref.document("l1") }
        verify(exactly = 0) { ref.document("l2") }
    }

    @Test
    fun waitlistEntriesShowNamesAndStatus() {
        val l = addLottery("l1", listOf("camp1"))
        l.waitlist.addAll(listOf("u1", "u2"))
        l.updateNumWinners(1)

        val before = repo.waitlistEntries(l, mapOf("u1" to "Alice", "u2" to "Bob"))
        assertEquals(listOf("Alice", "Bob"), before.map { it.name })
        assertTrue(before.all { it.status == "Pending" })

        repo.drawDueLotteries(afterReg)
        val after = repo.waitlistEntries(l, mapOf("u1" to "Alice", "u2" to "Bob"))

        assertEquals(1, after.count { it.status == "Won" })
        assertEquals(1, after.count { it.status == "Not selected" })
    }

    @Test
    fun waitlistEntriesFallBackToUserIdWithoutName() {
        val l = addLottery("l1", listOf("camp1"))
        l.waitlist.add("u9")

        val entries = repo.waitlistEntries(l, mapOf())

        assertEquals("u9", entries.first().name)
    }
}