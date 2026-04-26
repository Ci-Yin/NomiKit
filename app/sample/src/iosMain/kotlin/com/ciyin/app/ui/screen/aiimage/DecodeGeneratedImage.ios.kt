package com.ciyin.app.ui.screen.aiimage

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

internal actual fun decodeGeneratedImageBytes(
    bytes: ByteArray,
    @Suppress("UNUSED_PARAMETER") mimeType: String
): ImageBitmap? =
    runCatching {
        Image.makeFromEncoded(bytes).toComposeImageBitmap()
    }.getOrNull()
