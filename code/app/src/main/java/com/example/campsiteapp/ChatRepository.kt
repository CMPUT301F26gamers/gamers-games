package com.example.campsiteapp

import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore

/** Stores chats and observes their message logs using the existing Firebase connection. */

// This chat implementation was provided via Codex (2026-11-24):
// "Provide implementation for chat between users following this:
// The messages will be stored in firebase using the following idea: a "chat" will be stored
// between two people based on their uid and reservation.
// Therefore, each chat stored in firebase must be unique in terms of combination
// of 2 UIDs and reservation. That chat will contain logs.
// The logs will contain the messages in order that they were sent.
// For the communication part then, when someone wants to send a message, they will send it to this log.
// Then, the screen will always be loading from this log."

class ChatRepository(
    private val chatsRef: CollectionReference = Firebase.firestore.collection("chats")
) {
    /** The caller removes this listener when the chat screen closes. */
    fun listen(
        chat: Chat,
        onMessages: (List<ChatMessage>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        return chatsRef.document(chat.reservationId).collection("logs")
            .orderBy("sentAt")
            .addSnapshotListener(com.google.firebase.firestore.MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    onError(error.localizedMessage ?: "Could not load messages.")
                    return@addSnapshotListener
                }
                onMessages(snapshot?.documents.orEmpty().map { document ->
                    ChatMessage(
                        id = document.id,
                        senderUid = document.getString("senderUid").orEmpty(),
                        text = document.getString("text").orEmpty(),
                        sentAt = document.getTimestamp("sentAt"),
                        pending = document.metadata.hasPendingWrites()
                    )
                })
            }
    }

    /** Creates the chat if necessary and appends one message, together in a batch. */
    fun send(
        chat: Chat,
        senderUid: String,
        text: String,
        onComplete: (String?) -> Unit
    ) {
        if (senderUid !in chat.participantUids || text.isBlank()) {
            onComplete("A participant and a non-empty message are required.")
            return
        }
        val chatRef = chatsRef.document(chat.reservationId)
        val messageRef = chatRef.collection("logs").document()
        val batch = chatsRef.firestore.batch()
        batch.set(chatRef, mapOf(
            "reservationId" to chat.reservationId,
            "participantUids" to chat.participantUids.sorted()
        ), SetOptions.merge())
        batch.set(messageRef, mapOf(
            "senderUid" to senderUid,
            "text" to text.trim(),
            "sentAt" to FieldValue.serverTimestamp()
        ))
        batch.commit()
            .addOnSuccessListener { onComplete(null) }
            .addOnFailureListener {
                onComplete(it.localizedMessage ?: "Could not send message. Please try again.")
            }
    }
}