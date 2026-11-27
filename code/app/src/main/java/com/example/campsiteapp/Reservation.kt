package com.example.campsiteapp

import java.util.Date

/** A saved reservation. Days are day numbers (see Date.toDay) and both ends are inclusive. */
class Reservation(
    val id: String = "",
    val lotteryId: String = "",
    val campsiteId: String = "",
    val userId: String = "",
    val startDay: Int = 0,
    val endDay: Int = 0
)

enum class ReserveResult {
    SAVED,
    TAKEN,           // a chosen day was reserved by someone else first
    NOT_WINNER,
    BOOKING_CLOSED,
    OUT_OF_RANGE,    // a chosen day is outside the lottery's stay period
    INVALID          // end before start, or campsite not in this lottery
}

/**
 * US 01.04.01: days in the lottery's stay period with no reservation on this campsite.
 * To change the capacity rule later, this is the only function that needs to change.
 */
fun availableDays(lottery: Lottery, campsiteId: String, all: List<Reservation>): List<Int> {
    val taken = all.filter { it.campsiteId == campsiteId }
        .flatMap { it.startDay..it.endDay }
        .toSet()
    return (lottery.lotteryStart.toDay()..lottery.lotteryEnd.toDay()).filter { it !in taken }
}

/** @return null if the reservation is allowed, otherwise the reason it isn't */
fun checkReservation(
    lottery: Lottery, campsiteId: String, userId: String,
    first: Int, last: Int, all: List<Reservation>, now: Date
): ReserveResult? {
    if (userId !in lottery.winners) return ReserveResult.NOT_WINNER
    if (!lottery.hasCampsite(campsiteId) || last < first) return ReserveResult.INVALID
    if (!lottery.isBookingOpen(now)) return ReserveResult.BOOKING_CLOSED
    if (first < lottery.lotteryStart.toDay() || last > lottery.lotteryEnd.toDay()) {
        return ReserveResult.OUT_OF_RANGE
    }
    val free = availableDays(lottery, campsiteId, all).toSet()
    if ((first..last).any { it !in free }) return ReserveResult.TAKEN
    return null
}