package com.example.campsiteapp

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campsiteapp.ui.theme.CampBackground
import com.example.campsiteapp.ui.theme.CampError
import com.example.campsiteapp.ui.theme.CampLabelGrey
import com.example.campsiteapp.ui.theme.CampFieldGrey
import com.example.campsiteapp.ui.theme.CampGreen
import com.example.campsiteapp.ui.theme.CampGreenLight
import com.example.campsiteapp.ui.theme.CampRed
import com.example.campsiteapp.ui.theme.CampsiteAppTheme
import com.google.firebase.firestore.Blob
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(
    name: String,
    email: String,
    phone: String,
    onSave: (String, String, String) -> Unit,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onViewPolicy: () -> Unit,
    modifier: Modifier = Modifier,
    // This photo implementation was provided via Codex (2026-11-05):
    // "Provide implementation for profile viewing and saving a profile picture using BitMap, LocalContext, and the Gallery"
    profilePicture: Blob? = null,
    onSavePicture: ((Blob, (Boolean) -> Unit) -> Unit)? = null
) {
    val validator = ProfileValidator()

    var nameInput by remember { mutableStateOf(name) }
    var emailInput by remember { mutableStateOf(email) }
    var phoneInput by remember { mutableStateOf(phone) }
    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    // This photo implementation was provided via Codex (2026-11-05):
    // "Provide implementation for profile viewing and saving a profile picture using BitMap, LocalContext, and the Gallery"
    val contentResolver = LocalContext.current.contentResolver
    var pendingPhoto by remember { mutableStateOf<Uri?>(null) }
    var savedPicture by remember(profilePicture) { mutableStateOf(profilePicture) }
    var preview by remember { mutableStateOf<Blob?>(null) }
    var savingPicture by remember { mutableStateOf(false) }
    var photoMessage by remember { mutableStateOf("") }
    var photoError by remember { mutableStateOf(false) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            pendingPhoto = uri
            photoMessage = ""
            photoError = false
        }
    }

    LaunchedEffect(pendingPhoto) {
        val uri = pendingPhoto ?: return@LaunchedEffect
        val picture = withContext(Dispatchers.IO) {
            try {
                decodeProfilePreview(contentResolver, uri)?.let { bitmap ->
                    val output = ByteArrayOutputStream()
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)) {
                        Blob.fromBytes(output.toByteArray())
                    } else {
                        null
                    }
                }
            } catch (_: IOException) {
                null
            } catch (_: SecurityException) {
                null
            }
        }
        if (picture != null) {
            preview = picture
        } else {
            photoError = true
            photoMessage = "Couldn't open that photo."
        }
        pendingPhoto = null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CampBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CampGreen)
                .statusBarsPadding()
                .height(56.dp)
        ) {
            TextButton(
                onClick = { onBack() },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .testTag("backButton")
            ) {
                Text(text = "←", color = Color.White, fontSize = 22.sp)
            }

            Text(
                text = "Profile",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // This photo implementation was provided via Codex (2026-11-05):
            // "Provide implementation for profile viewing and saving a profile picture using BitMap, LocalContext, and the Gallery"
            ProfileAvatar(
                picture = preview ?: savedPicture,
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.CenterHorizontally)
            )

            TextButton(
                onClick = { photoPicker.launch("image/*") },
                enabled = pendingPhoto == null && !savingPicture,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Choose photo")
            }

            val selectedPicture = preview
            if (selectedPicture != null && onSavePicture != null) {
                TextButton(
                    onClick = {
                        savingPicture = true
                        photoError = false
                        photoMessage = ""
                        onSavePicture(selectedPicture) { success ->
                            savingPicture = false
                            photoError = !success
                            if (success) {
                                savedPicture = selectedPicture
                                preview = null
                            }
                            photoMessage = if (success) "Photo saved" else "Couldn't save that photo. Try again."
                        }
                    },
                    enabled = pendingPhoto == null && !savingPicture,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(if (savingPicture) "Saving photo…" else "Save photo")
                }
            }

            if (photoMessage.isNotEmpty()) {
                Text(
                    text = photoMessage,
                    color = if (photoError) CampError else CampGreenLight,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = nameInput,
                onValueChange = {
                    nameInput = it
                    saved = false
                },
                label = { Text("Name") },
                colors = fieldColors(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("nameInput")
            )
            if (nameError.isNotEmpty()) {
                Text(
                    text = nameError,
                    color = CampError,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = emailInput,
                onValueChange = {
                    emailInput = it
                    saved = false
                },
                label = { Text("Email") },
                colors = fieldColors(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("emailInput")
            )
            if (emailError.isNotEmpty()) {
                Text(
                    text = emailError,
                    color = CampError,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = phoneInput,
                onValueChange = {
                    phoneInput = it
                    saved = false
                },
                label = { Text("Phone (optional)") },
                colors = fieldColors(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("phoneInput")
            )
            if (phoneError.isNotEmpty()) {
                Text(
                    text = phoneError,
                    color = CampError,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    nameError = ""
                    emailError = ""
                    phoneError = ""

                    if (!validator.isValidName(nameInput)) {
                        nameError = "Name can't be empty"
                    }
                    if (!validator.isValidEmail(emailInput)) {
                        emailError = "Enter a valid email"
                    }
                    if (!validator.isValidPhone(phoneInput)) {
                        phoneError = "Enter a valid phone number"
                    }

                    if (nameError.isEmpty() && emailError.isEmpty() && phoneError.isEmpty()) {
                        onSave(nameInput.trim(), emailInput.trim(), phoneInput.trim())
                        saved = true
                    } else {
                        saved = false
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CampGreen,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("saveButton")
            ) {
                Text(text = "Save", fontWeight = FontWeight.Bold)
            }

            if (saved) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Profile saved",
                    color = CampGreenLight,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

        }

        TextButton(
            onClick = { onViewPolicy() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("View App Policy & Guidelines", color = CampGreen)
        }

        Button(
            onClick = { showDeleteDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = CampRed,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .height(48.dp)
                .testTag("deleteProfileButton")
        ) {
            Text(text = "Delete profile", fontWeight = FontWeight.Bold)
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Profile") },
            text = {
                Text("Are you sure you want to delete your profile? This is permanent and cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    modifier = Modifier.testTag("confirmDelete"),
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = CampRed)
                }
            },
            dismissButton = {
                TextButton(
                    modifier = Modifier.testTag("cancelDelete"),
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

// This photo implementation was provided via Codex (2026-11-05):
// "Provide implementation for profile viewing and saving a profile picture using BitMap, LocalContext, and the Gallery"
private fun decodeProfilePreview(contentResolver: ContentResolver, uri: Uri): Bitmap? {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    if (options.outWidth <= 0 || options.outHeight <= 0) return null

    // Read only enough pixels for the avatar preview.
    val longestSide = maxOf(options.outWidth, options.outHeight)
    options.inSampleSize = 1
    while (longestSide / options.inSampleSize > 512) {
        options.inSampleSize *= 2
    }
    options.inJustDecodeBounds = false
    return contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
}

@Composable
fun fieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = CampFieldGrey,
    unfocusedContainerColor = CampFieldGrey,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    focusedLabelColor = CampGreen,
    unfocusedLabelColor = CampLabelGrey,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    cursorColor = CampGreen
)

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    CampsiteAppTheme {
        ProfileScreen(
            name = "Alex P.",
            email = "alex@example.com",
            phone = "780-555-0123",
            onSave = { _, _, _ -> },
            onBack = {},
            onDelete = {},
            onViewPolicy = {}
        )
    }
}