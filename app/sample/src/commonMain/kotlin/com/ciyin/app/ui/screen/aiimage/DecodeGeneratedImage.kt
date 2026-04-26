package com.ciyin.app.ui.screen.aiimage

import androidx.compose.ui.graphics.ImageBitmap

/**
 * 将 [GeneratedImage] 原始字节解码为 [ImageBitmap] 供 Compose 展示。
 * Android 已实现；Desktop 使用 Skia；iOS 依赖 Skia 编码数据（失败时返回 null）。
 */
internal expect fun decodeGeneratedImageBytes(bytes: ByteArray, mimeType: String): ImageBitmap?
