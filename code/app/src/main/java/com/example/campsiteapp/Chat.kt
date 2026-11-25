package com.example.campsiteapp

import com.google.firebase.Timestamp

/** One conversation for a reservation and its two participants. */

// This chat implementation was provided via Codex (2026-11-24):
// "Provide implementation for chat between users following this:
// The messages will be stored in firebase using the following idea: a "chat" will be stored
// between two people based on their uid and reservation.
// Therefore, each chat stored in firebase must be unique in terms of combination
// of 2 UIDs and reservation. That chat will contain logs.
// The logs will contain the messages in order that they were sent.
// For the communication part then, when someone wants to send a message, they will send it to this log.
// Then, the screen will always be loading from this log."
class Chat(
    val reservationId: String,
    val participantUids: List<String>
) {
    init {
        require(reservationId.isNotBlank()) { "A reservation is required." }
        require(participantUids.size == 2 && participantUids.all { it.isNotBlank() } &&
            participantUids.distinct().size == 2) { "Two different participants are required." }
    }
}

/** One entry in a chat's message log. */
class ChatMessage(
    val id: String = "",
    val senderUid: String = "",
    val text: String = "",
    val sentAt: Timestamp? = null,
    val pending: Boolean = false
)