package com.example.campsiteapp

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// This testing was implemented via Codex (2026-11-26)
// "Make simple UI tests for ChatScreen similar to BrowseProfilesScreenTest"
@RunWith(AndroidJUnit4::class)
class ChatScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val messages = mutableStateListOf<ChatMessage>()
    private var sentMessage: String? = null

    @Before
    fun setUp() {
        composeTestRule.setContent {
            ChatConversation(
                messages = messages,
                currentUid = "entrant",
                reservationId = "reservation1",
                isNonParticipantAdmin = false,
                onBack = {},
                onSend = { sentMessage = it }
            )
        }
    }

    @Test
    fun emptyChatShowsPlaceholder() {
        composeTestRule.onNodeWithTag("emptyChat").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sendMessageButton").assertIsNotEnabled()
    }

    @Test
    fun incomingAndOutgoingMessagesAreDisplayed() {
        composeTestRule.runOnIdle {
            messages.add(ChatMessage(id = "1", senderUid = "owner", text = "Welcome!"))
            messages.add(ChatMessage(id = "2", senderUid = "entrant", text = "Thank you!"))
        }
        composeTestRule.onNodeWithTag("chatMessage_1").assertIsDisplayed().assertTextEquals("Welcome!")
        composeTestRule.onNodeWithTag("chatMessage_2").assertIsDisplayed().assertTextEquals("Thank you!")
        composeTestRule.onNodeWithTag("emptyChat").assertDoesNotExist()
    }

    @Test
    fun sendPassesMessageAndClearsInput() {
        composeTestRule.onNodeWithTag("messageField").performTextInput("Hello")
        composeTestRule.onNodeWithTag("sendMessageButton").performClick()
        composeTestRule.runOnIdle { assertEquals("Hello", sentMessage) }
        composeTestRule.onNodeWithTag("messageField").assert(
            SemanticsMatcher.expectValue(
                androidx.compose.ui.semantics.SemanticsProperties.EditableText,
                androidx.compose.ui.text.AnnotatedString("")
            )
        )
        composeTestRule.onNodeWithTag("sendMessageButton").assertIsNotEnabled()
    }

    @Test
    fun whitespaceCannotBeSent() {
        composeTestRule.onNodeWithTag("messageField").performTextInput("   ")
        composeTestRule.onNodeWithTag("sendMessageButton").assertIsNotEnabled()
        composeTestRule.runOnIdle { assertNull(sentMessage) }
    }
}