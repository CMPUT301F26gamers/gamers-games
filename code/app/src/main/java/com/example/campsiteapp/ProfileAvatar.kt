package com.example.campsiteapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.example.campsiteapp.ui.theme.CampAvatarGrey
import com.google.firebase.firestore.Blob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// This photo implementation was provided via Codex (2026-11-05):
// "Provide implementation for profile viewing and saving a profile picture using BitMap, LocalContext, and the Gallery"
@Composable
internal fun ProfileAvatar(picture: Blob?, modifier: Modifier = Modifier) {
    Box(modifier.clip(CircleShape).background(CampAvatarGrey)) {
        if (picture != null) {
            key(picture) {
                val bitmap by produceState<Bitmap?>(null) {
                    value = withContext(Dispatchers.IO) {
                        val bytes = picture.toBytes()
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
                }
                bitmap?.let { image ->
                    Image(
                        bitmap = image.asImageBitmap(),
                        contentDescription = "Profile picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
