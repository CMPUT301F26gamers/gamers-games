package com.example.campsiteapp

import java.util.Date
import java.util.UUID

/**
 * A campsite can be in several lotteries
 * idk if this is the correct assumption tho
 *
 * Owners create a lottery with [createLottery]
 *
 * [registerWaitlisted] signs up everyone waiting on the lottery's campsites.
 * also something that needs to be checked?
 *
 * @property lotteries every lottery currently listed
 */
class LotteryList(
    val lotteries: MutableList<Lottery> = mutableListOf()
) {
    /** @return every lottery this campsite is in */
    fun getLotteries(campsiteId: String): List<Lottery> {
        return lotteries.filter { it.hasCampsite(campsiteId) }
    }

    /** return a campsite's lottery that is open in a certain period */
    fun getActiveLottery(campsiteId: String, now: Date): Lottery? {
        for (lottery in lotteries) {
            if (lottery.hasCampsite(campsiteId) && lottery.isActive(now)) {
                return lottery
            }
        }
        return null
    }

    /** Creates a lottery containing the given campsites */
    fun createLottery(
        campsiteIds: List<String>,
        regStart: Date, regEnd: Date,
        start: Date, end: Date
    ): Lottery {
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
        lottery.setPeriods(regStart, regEnd, start, end)
        lotteries.add(lottery)
        return lottery
    }

    /**
     * Moves everyone on the campsite's waitlist into the campsite's open lottery,
     * then empties the waitlist
     * nothing calls this yet
     */
    fun registerWaitlisted(campsite: Campsite, now: Date): Boolean {
        val lottery = getActiveLottery(campsite.id, now) ?: return false

        for (userId in campsite.waitlist.toList()) {
            lottery.join(userId)
            campsite.leaveWaitlist(userId)
        }
        return true
    }

    /**
     * Takes a campsite out of one lottery. If that leaves the lottery empty,
     * the lottery is removed too.
     *
     * @return true if the campsite was in that lottery
     */
    fun removeCampsite(lotteryId: String, campsiteId: String): Boolean {
        val lottery = findLottery(lotteryId) ?: return false
        if (!lottery.campsiteIds.remove(campsiteId)) {
            return false
        }
        if (lottery.campsiteIds.isEmpty()) {
            lotteries.remove(lottery)
        }
        return true
    }

    /** Removes a whole lottery. @return true if it was in the list. */
    fun removeLottery(lotteryId: String): Boolean {
        val lottery = findLottery(lotteryId) ?: return false
        return lotteries.remove(lottery)
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

}