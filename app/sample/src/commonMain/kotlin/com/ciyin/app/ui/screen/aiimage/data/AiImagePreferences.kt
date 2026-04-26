package com.ciyin.app.ui.screen.aiimage.data

import kotlinx.serialization.Serializable

/**
 * 文生图演示的持久化偏好，通过 DataStore 写入磁盘。
 *
 * 不保存加载态、进度与图像字节等纯运行时数据。
 */
@Serializable
internal data class AiImagePreferences(
    val serverHost: String = "127.0.0.1",
    val prompt: String = "",
)
