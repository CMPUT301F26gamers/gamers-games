package com.example.campsiteapp

import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlin.jvm.java

/**
 * Manages and loads Chats and ChatMessages in Firestore
 *
 * The repository will listen to the given chat in the constructor and update the chat screen depending
 * on changes to that chat. Each screen is responsible for closing this listener
 *
 * @property chat The chat that will be tracked
 * @property chatsRef The firebase collection that stores all chats
 * @property messages The locally stored list of messages for that chat
 */
class ChatRepository(
    private val chat: Chat,
    private val chatsRef: CollectionReference = Firebase.firestore.collection("chats")
) {
    private val _messages = mutableStateListOf<ChatMessage>()

    /**
     * Gets the locally stored messages for the specific chat
     */
    val messages: List<ChatMessage>
        get() = _messages

    /**
     * Appends a message to the firestore logs document
     *
     * @param senderUid ID representing the sender user
     * @param text String representing the message
     */
    fun appendMessage(senderUid: String, text: String){
        val receiverUid = chat.participantUids.first {it != senderUid}
        val chatRef = chatsRef.document(chat.reservationId)

        chatRef.set(chat, SetOptions.merge())
            .addOnSuccessListener {
                val messageRef = chatRef.collection("logs").document()

                messageRef.set(
                    mapOf(
                        "id" to messageRef.id,
                        "senderUid" to senderUid,
                        "receiverUid" to receiverUid,
                        "text" to text.trim(),
                        "sentAt" to FieldValue.serverTimestamp(),
                        "seen" to false
                    )
                )
            }
    }

    private val listener: ListenerRegistration

    init {
        listener = chatsRef.document(chat.reservationId).collection("logs").orderBy("sentAt").addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            _messages.clear()

            snapshot?.documents?.forEach { document ->
                val message = document.toObject(ChatMessage::class.java)
                if (message != null) {
                    _messages.add(message)
                }
            }
        }
    }

    /**
     * Removes the listener for that chat
     */
    fun close(){
        listener.remove()
    }
}