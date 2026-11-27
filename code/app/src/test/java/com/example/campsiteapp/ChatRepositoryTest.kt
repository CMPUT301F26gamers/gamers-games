package com.example.campsiteapp

import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import io.mockk.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

// This test was implemented via Codex (2026-11-26)
// "Implement ChatRepository tests in similar fashion to ProfileRepositoryTests"
class ChatRepositoryTest {
    private val ref = mockk<CollectionReference>(relaxed = true)

    private val listener = mockk<ListenerRegistration>(relaxed = true)
    private val updates = slot<EventListener<QuerySnapshot>>()
    private val chat = Chat("reservation1", listOf("entrant", "owner"))
    private var repo = ChatRepository(chat, ref)

    @Before
    fun setUp() {
        every {
            ref.document("reservation1").collection("logs").orderBy("sentAt")
                .addSnapshotListener(capture(updates))
        } returns listener
        repo = ChatRepository(chat, ref)
    }

    @Test
    fun messagesStartEmpty() {
        assertTrue(repo.messages.isEmpty())
    }

    @Test
    fun snapshotUpdatesMessagesAndEmptySnapshotClearsThem() {
        val message = ChatMessage(id = "message1", text = "Hello")
        val document = mockk<DocumentSnapshot>()
        val snapshot = mockk<QuerySnapshot>()
        every { document.toObject(ChatMessage::class.java) } returns message
        every { snapshot.documents } returns listOf(document)

        updates.captured.onEvent(snapshot, null)
        assertEquals(listOf(message), repo.messages)

        every { snapshot.documents } returns emptyList()
        updates.captured.onEvent(snapshot, null)
        assertTrue(repo.messages.isEmpty())
    }

    @Test
    fun appendMessageWritesAfterChatIsSaved() {
        val write = mockk<Task<Void>>(relaxed = true)
        val success = slot<OnSuccessListener<Void>>()
        val messageRef = mockk<DocumentReference>(relaxed = true)
        val contents = slot<Any>()
        every { ref.document("reservation1").set(chat, any<SetOptions>()) } returns write
        every { write.addOnSuccessListener(capture(success)) } returns write
        every { ref.document("reservation1").collection("logs").document() } returns messageRef
        every { messageRef.id } returns "message1"
        every { messageRef.set(capture(contents)) } returns mockk(relaxed = true)

        repo.appendMessage("entrant", " Hello ")
        verify(exactly = 0) { messageRef.set(any()) }
        success.captured.onSuccess(null)

        val saved = contents.captured as Map<*, *>
        assertEquals("message1", saved["id"])
        assertEquals("entrant", saved["senderUid"])
        assertEquals("owner", saved["receiverUid"])
        assertEquals("Hello", saved["text"])
        assertEquals(false, saved["seen"])
        assertNotNull(saved["sentAt"])
    }

    @Test
    fun closeRemovesListener() {
        repo.close()
        verify(exactly = 1) { listener.remove() }
    }
}