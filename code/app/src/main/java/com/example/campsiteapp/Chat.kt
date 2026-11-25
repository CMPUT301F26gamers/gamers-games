package com.example.campsiteapp

import com.google.firebase.Timestamp

class Chat(
    val reservationId: String = "",
    val participantUids: List<String> = emptyList()
)

/** One entry in a chat's message log. */
class ChatMessage(
    val id: String = "",
    val senderUid: String = "",
    val receiverUid: String = "",
    val text: String = "",
    val sentAt: Timestamp? = null,
    val seen: Boolean = false
)