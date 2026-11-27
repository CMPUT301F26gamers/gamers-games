package com.example.campsiteapp

import com.google.firebase.Timestamp

/**
 * Chat class that represents one chat between an entrant and an owner for a specific reservation
 *
 * Chat stores information about its reservation id and the two participants ids in a list.
 * In firebase, a chat will contain these two parameters as well as another document called logs.
 * The logs will store multiple instances of ChatMessage which represents one message from either person.
 *
 * @property reservationId The unique id for a chat
 * @property participantUids The list of ids of each chatter
 */
class Chat(
    val reservationId: String = "",
    val participantUids: List<String> = emptyList()
)

/**
 * ChatMessage class representing one message sent from user to another
 *
 * ChatMessage contains the id identifying the message, sender ID, receiver ID,
 * test for the message, time sent at as a Timestamp, and a boolean if the message was seen to be used later.
 * This class will be in each Chat. Therefore, a ChatMessage cannot exist without a Chat.
 *
 * @property id This is the ID of the message
 * @property senderUid This is the ID of the sender
 * @property receiverUid This is the ID of the receiver
 * @property text This is the block of test being sent
 * @property sentAt This is the timestamp that is used to sort the Chat
 * @property seen This is the boolean that will be used for notifications
 */
class ChatMessage(
    val id: String = "",
    val senderUid: String = "",
    val receiverUid: String = "",
    val text: String = "",
    val sentAt: Timestamp? = null,
    val seen: Boolean = false
)