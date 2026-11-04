package com.example.campsiteapp

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import java.util.Date
import java.util.UUID

/**
 * A campsite can be in several lotteries
 * idk if this is the correct assumption tho
 *
 * Owners create a lottery with [createLottery]. Entrants join and leave a
 * campsite's waitlist (the active lottery's entrants) with [enter] and [leave].
 *
 * @property lotteries every lottery currently listed
 */
class LotteryRepository (
    private val ref: CollectionReference = Firebase.firestore.collection("lotteries")
) {
    val lotteries = mutableStateListOf<Lottery>()

    init {
        ref.addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            lotteries.clear()
            snapshot?.documents
                ?.mapNotNull { it.toObject(Lottery::class.java) }
                ?.let { lotteries.addAll(it) }
        }
    }

    // validates (throws IllegalArgumentException), then saves
    fun createLottery(
        campsiteIds: List<String>,
        regStart: Date, regEnd: Date,
        bookStart: Date, bookEnd: Date,
        start: Date, end: Date,
        numWinners: Int = 1,
        waitlistCapacity: Int? = null,
        onDone: (Boolean) -> Unit
    ) {
        if (campsiteIds.isEmpty()) {
            throw IllegalArgumentException("Select at least one campsite")
        }
        if (campsiteIds.toSet().size != campsiteIds.size) {
            throw IllegalArgumentException("Duplicate campsites selected")
        }
        val lottery = Lottery(
            id = UUID.randomUUID().toString(),
            campsiteIds = campsiteIds.toMutableList()
        )
        lottery.setPeriods(regStart, regEnd, bookStart, bookEnd, start, end)
        lottery.updateNumWinners(numWinners)
        lottery.updateWaitlistCapacity(waitlistCapacity)

        save(lottery, onDone)
    }

    private fun save(lottery: Lottery, onDone: (Boolean) -> Unit = {}) {
        ref.document(lottery.id).set(lottery)
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    /** @return every lottery this campsite is in */
    fun getLotteries(campsiteId: String): List<Lottery> {
        return lotteries.filter { it.hasCampsite(campsiteId) }
    }

    /** return a campsite's lottery that is open for registration at [now] */
    fun getActiveLottery(campsiteId: String, now: Date): Lottery? {
        for (lottery in lotteries) {
            if (lottery.hasCampsite(campsiteId) && lottery.isActive(now)) {
                return lottery
            }
        }
        return null
    }

    /**
     * Takes a campsite out of one lottery. If that leaves the lottery empty,
     * the lottery is removed too.
     *
     * @return true if the campsite was in that lottery
     */
    fun removeCampsite(lotteryId: String, campsiteId: String, onDone: (Boolean) -> Unit = {}) {
        val lottery = findLottery(lotteryId)
        if (lottery == null || !lottery.campsiteIds.remove(campsiteId)) {
            onDone(false)
            return
        }
        if (lottery.campsiteIds.isEmpty()) {
            removeLottery(lotteryId, onDone)
        } else {
            save(lottery, onDone)
        }
    }

    /** Removes a whole lottery. @return true if it was in the list. */
    fun removeLottery(lotteryId: String, onDone: (Boolean) -> Unit = {}) {
        if (findLottery(lotteryId) == null) {
            onDone(false)
            return
        }
        ref.document(lotteryId).delete()
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }

    // 2 Getter functions, delete if not needed
    /** Every lottery in the list. */
    fun allLotteries(): List<Lottery> {
        return lotteries.toList()
    }

    /** Every lottery whose registration is open right now. */
    fun allActiveLotteries(now: Date): List<Lottery> {
        return lotteries.filter { it.isActive(now) }
    }

    /** @return the lottery with this ID, or null */
    fun findLottery(lotteryId: String): Lottery? {
        return lotteries.firstOrNull { it.id == lotteryId }
    }

    fun enter(campsite: Campsite, userId: String, now: Date, onDone: (Boolean) -> Unit = {}) {
        val lottery = getActiveLottery(campsite.id, now)
        if (lottery == null || lottery.hasEntrant(userId) || lottery.isFull()) {
            onDone(false)
            return
        }
        ref.document(lottery.id)
            //arrayUnion prevent error when 2 users join at the same time
            .update("waitlist", FieldValue.arrayUnion(userId))
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }


    fun joinState(campsite: Campsite, userId: String, now: Date): JoinState {
        val lottery = getActiveLottery(campsite.id, now) ?: return JoinState.NOT_OPEN
        return when {
            lottery.hasEntrant(userId) -> JoinState.ON_WAITLIST
            lottery.isFull() -> JoinState.WAITLIST_FULL
            else -> JoinState.CAN_JOIN
        }
    }

    /**
     * @return true if the user was on the campsite's active waitlist and left it
     */
    fun leave(campsite: Campsite, userId: String, now: Date, onDone: (Boolean) -> Unit = {}) {
        val lottery = getActiveLottery(campsite.id, now)
        if (lottery == null || lottery.hasBeenDrawn(userId) || !lottery.hasEntrant(userId)) {
            onDone(false)
            return
        }
        ref.document(lottery.id)
            //arrayRemove prevent error when 2 users join at the same time
            .update("waitlist", FieldValue.arrayRemove(userId))
            .addOnSuccessListener { onDone(true) }
            .addOnFailureListener { onDone(false) }
    }
}

/**
 * The outcome of an entrant pressing the join button on a campsite's details
 * screen (US 01.01.06, US 01.02.01).
 */
enum class JoinState {
    CAN_JOIN,
    ON_WAITLIST,
    WAITLIST_FULL,
    NOT_OPEN
}