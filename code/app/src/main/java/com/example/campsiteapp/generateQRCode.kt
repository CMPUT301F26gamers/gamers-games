package com.example.campsiteapp

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Generates a QR code Bitmap from the given content.
 *
 * @param content The text to encode (e.g., a campsite ID).
 * @param size The width and height of the resulting bitmap in pixels.
 * @return A Bitmap of the QR code, or null if generation fails.
 */

/* This function was made by Andrés Cruz at
https://www.desarrollolibre.net/blog/android/how-to-create-a-qr-code-reader-on-android-with-android-studio 2026 */
fun generateQRCode(content: String, size: Int = 512): Bitmap? {
    return try {
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

