package com.example.campsiteapp

import androidx.compose.runtime.mutableStateListOf
import com.example.campsiteapp.AppUser.profile
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlin.jvm.java

class ChatRepository(
    private val chat: Chat,
    private val chatsRef: CollectionReference = Firebase.firestore.collection("chats")
) {
    private val _messages = mutableStateListOf<ChatMessage>()

    val messages: List<ChatMessage>
        get() = _messages

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

    fun close(){
        listener.remove()
    }
}