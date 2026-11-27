package com.example.campsiteapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.campsiteapp.ui.theme.CampBackground
import com.example.campsiteapp.ui.theme.CampFieldGrey
import com.example.campsiteapp.ui.theme.CampGreen
import com.example.campsiteapp.ui.theme.CampsiteAppTheme

// This screen implementation was provided via Codex (2026-11-25):
// Using the other screens like profileScreen and others as basis,
// copy them as closely as possible to be a chatScreen where it is literally just
// the messages like right is yourself in blue and left is in grey from other
// with sending message option at the bottom. Follow other screens as much as you can

@Composable
fun ChatScreen(
    chat: Chat,
    currentUid: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentUid !in chat.participantUids) {
        Column(modifier.fillMaxSize().background(CampBackground)) {
            ScreenHeader(chat.reservationId, onBack)
            Text(
                "This chat is only available to its participants.",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    val repository = remember(chat.reservationId) { ChatRepository(chat) }
    DisposableEffect(repository) {
        onDispose { repository.close() }
    }

    ChatConversation(
        messages = repository.messages,
        currentUid = currentUid,
        reservationId = chat.reservationId,
        onBack = onBack,
        onSend = { repository.appendMessage(currentUid, it) },
        modifier = modifier
    )
}

@Composable
private fun ChatConversation(
    messages: List<ChatMessage>,
    currentUid: String,
    reservationId: String,
    onBack: () -> Unit,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CampBackground)
            .imePadding()
    ) {
        ScreenHeader(reservationId, onBack)
        val listState = rememberLazyListState()
        var draft by rememberSaveable(reservationId) { mutableStateOf("") }

        LaunchedEffect(messages.size) {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.lastIndex)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Text("No messages yet.", color = Color.LightGray)
                }
            }

            items(messages, key = { it.id }) { message ->
                val isMine = message.senderUid == currentUid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                ) {
                    Text(
                        text = message.text,
                        color = if (isMine) Color.White else Color.Black,
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .background(
                                if (isMine) Color(0xFF1976D2) else CampFieldGrey,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = draft,
                onValueChange = { draft = it },
                placeholder = { Text("Message") },
                colors = fieldColors(),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(8.dp))

            Button(
                onClick = {
                    onSend(draft)
                    draft = ""
                },
                enabled = draft.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CampGreen,
                    contentColor = Color.White
                )
            ) {
                Text("Send")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    CampsiteAppTheme {
        ChatConversation(
            messages = listOf(
                ChatMessage(id = "1", senderUid = "owner", text = "Hi! Your reservation is confirmed."),
                ChatMessage(id = "2", senderUid = "entrant", text = "Thanks, see you soon!")
            ),
            currentUid = "entrant",
            reservationId = "preview-reservation",
            onBack = {},
            onSend = {}
        )
    }
}