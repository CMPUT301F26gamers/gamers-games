package com.example.campsiteapp

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.util.Date
import java.util.UUID

/**
 * Saves and listens to reservations.
 * The snapshot listener feeds a state list, so any calendar that reads
 * [reservations] refreshes when availability changes (US 01.04.01).
 */
class ReservationRepository(
    private val db: FirebaseFirestore = Firebase.firestore
) {
    private val ref = db.collection("reservations")
    val reservations = mutableStateListOf<Reservation>()

    init {
        ref.addSnapshotListener { snap, err ->
            if (err != null) return@addSnapshotListener
            reservations.clear()
            snap?.documents
                ?.mapNotNull { it.toObject(Reservation::class.java) }
                ?.let { reservations.addAll(it) }
        }
    }

    /** Days the user can pick for this campsite right now */
    fun availableDays(lottery: Lottery, campsiteId: String): List<Int> =
        availableDays(lottery, campsiteId, reservations)

    /**
     * US 01.04.02: checks the rules, then saves the reservation and moves the user
     * from the lottery's winners to bookings in one batch (both writes or neither).
     */
    fun reserve(
        lottery: Lottery, campsiteId: String, userId: String,
        first: Int, last: Int, now: Date = Date(),
        onDone: (ReserveResult) -> Unit
    ) {
        checkReservation(lottery, campsiteId, userId, first, last, reservations, now)
            ?.let { onDone(it); return }

        val reservation = Reservation(
            id = UUID.randomUUID().toString(), lotteryId = lottery.id, campsiteId = campsiteId,
            userId = userId, startDay = first, endDay = last
        )
        db.batch().apply {
            set(ref.document(reservation.id), reservation)
            update(
                db.collection("lotteries").document(lottery.id),
                "winners", FieldValue.arrayRemove(userId),
                "bookings", FieldValue.arrayUnion(userId)
            )
        }.commit()
            .addOnSuccessListener { onDone(ReserveResult.SAVED) }
            .addOnFailureListener { onDone(ReserveResult.INVALID) }
    }
}