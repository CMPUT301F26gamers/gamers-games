package com.example.campsiteapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.campsiteapp.ui.theme.*

/**
 * Shared entrant/owner conversation screen.
 *
 * The reservation screen supplies its ID, participant UIDs and whether messaging is
 * allowed for its current status. Database permissions are handled separately.
 */

// This chat implementation was provided via Codex (2026-11-24):
// "Provide implementation for chat between users following this:
// The messages will be stored in firebase using the following idea: a "chat" will be stored
// between two people based on their uid and reservation.
// Therefore, each chat stored in firebase must be unique in terms of combination
// of 2 UIDs and reservation. That chat will contain logs.
// The logs will contain the messages in order that they were sent.
// For the communication part then, when someone wants to send a message, they will send it to this log.
// Then, the screen will always be loading from this log."

@Composable
fun ChatScreen(
    chat: Chat,
    currentUid: String,
    otherName: String,
    canSend: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    repository: ChatRepository? = if (LocalInspectionMode.current) null else remember { ChatRepository() }
) {
    key(chat.reservationId, currentUid) {
        var messages by remember { mutableStateOf(emptyList<ChatMessage>()) }
        var loading by remember { mutableStateOf(true) }
        var loadError by remember { mutableStateOf<String?>(null) }
        var sendError by remember { mutableStateOf<String?>(null) }
        var draft by rememberSaveable { mutableStateOf("") }
        var sending by remember { mutableStateOf(false) }
        var retry by remember { mutableIntStateOf(0) }
        val listState = rememberLazyListState()
        val isParticipant = currentUid in chat.participantUids

        DisposableEffect(chat.reservationId, currentUid, repository, retry) {
            loading = isParticipant && repository != null
            loadError = null
            val listener = if (isParticipant) repository?.listen(
                chat = chat,
                onMessages = {
                    messages = it
                    loading = false
                    loadError = null
                },
                onError = {
                    loadError = it
                    loading = false
                }
            ) else null
            onDispose { listener?.remove() }
        }

        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
        }

        Column(
            modifier.fillMaxSize().background(CampBackground)
                .safeDrawingPadding().imePadding().padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("Back", color = CampGreenLight) }
                Column(Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(otherName.ifBlank { "Conversation" }, color = Color.White,
                        style = MaterialTheme.typography.titleLarge)
                    Text("Reservation: ${chat.reservationId}", color = CampGreenLight,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
            if (!isParticipant) {
                Text("This conversation is only available to its participants.", color = CampError)
                return@Column
            }

            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            loadError?.let { error ->
                Text(error, color = CampError)
                TextButton(onClick = { retry++ }) { Text("Retry", color = CampGreenLight) }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (!loading && loadError == null && messages.isEmpty()) {
                    item { Text("No messages yet.", color = Color.LightGray) }
                }
                items(messages, key = { it.id }) { message ->
                    val mine = message.senderUid == currentUid
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            color = if (mine) CampGreen else CampAvatarGrey,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.widthIn(max = 300.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(if (mine) "You" else otherName.ifBlank { "Participant" },
                                    color = CampGreenLight, style = MaterialTheme.typography.labelSmall)
                                Text(message.text, color = Color.White)
                                if (message.pending) {
                                    Text("Sending…", color = Color.LightGray,
                                        style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
            sendError?.let { Text(it, color = CampError) }
            if (!canSend) {
                Text("Messaging is unavailable for this reservation.", color = Color.LightGray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    label = { Text("Message") },
                    enabled = canSend && !sending,
                    modifier = Modifier.weight(1f),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CampGreenLight,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = CampGreenLight,
                        unfocusedLabelColor = Color.LightGray
                    )
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    enabled = canSend && !sending && draft.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = CampGreen),
                    onClick = {
                        sending = true
                        sendError = null
                        repository?.send(chat, currentUid, draft) { error ->
                            sending = false
                            sendError = error
                            if (error == null) draft = ""
                        }
                    }
                ) { Text(if (sending) "Sending…" else "Send") }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    MaterialTheme {
        ChatScreen(
            chat = Chat("reservation-1", listOf("entrant", "owner")),
            currentUid = "entrant",
            otherName = "Campsite Owner",
            canSend = true,
            onBack = {}
        )
    }
}