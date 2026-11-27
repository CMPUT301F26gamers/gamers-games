package com.example.campsiteapp

import com.google.firebase.Timestamp
import org.junit.Assert.*
import org.junit.Test

class ChatTest {
    @Test
    fun testChatConstruction() {
        val chat = Chat("reservation1", listOf("entrant", "owner"))
        assertEquals("reservation1", chat.reservationId)
        assertEquals(listOf("entrant", "owner"), chat.participantUids)
    }

    @Test
    fun checkDefaultChat() {
        val chat = Chat()
        assertEquals("", chat.reservationId)
        assertTrue(chat.participantUids.isEmpty())
    }

    @Test
    fun testMessageConstruction() {
        val time = Timestamp(100, 0)
        val message = ChatMessage("message1", "entrant", "owner", "Hello", time, true)
        assertEquals("message1", message.id)
        assertEquals("entrant", message.senderUid)
        assertEquals("owner", message.receiverUid)
        assertEquals("Hello", message.text)
        assertEquals(time, message.sentAt)
        assertTrue(message.seen)
    }

    @Test
    fun checkDefaultMessage() {
        val message = ChatMessage()
        assertEquals("", message.id)
        assertEquals("", message.senderUid)
        assertEquals("", message.receiverUid)
        assertEquals("", message.text)
        assertNull(message.sentAt)
        assertFalse(message.seen)
    }
}