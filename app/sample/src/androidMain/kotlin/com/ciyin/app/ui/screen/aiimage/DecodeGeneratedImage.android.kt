package com.ciyin.app.ui.screen.aiimage

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

internal actual fun decodeGeneratedImageBytes(
    bytes: ByteArray,
    @Suppress("UNUSED_PARAMETER") mimeType: String
): ImageBitmap? {
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
    return bitmap.asImageBitmap()
}
