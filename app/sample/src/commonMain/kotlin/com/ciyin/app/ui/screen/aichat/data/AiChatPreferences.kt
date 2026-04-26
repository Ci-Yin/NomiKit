package com.ciyin.app.ui.screen.aichat.data

import com.ciyin.app.ui.screen.aichat.AiChatMessageItem
import kotlinx.serialization.Serializable

/**
 * AI 聊天示例的持久化偏好数据，通过 DataStore 写入磁盘。
 *
 * 只保存配置与历史消息，不保存 UI 中间状态（isStreaming / input 等）。
 */
@Serializable
internal data class AiChatPreferences(
    val baseUrl: String = "https://api.deepseek.com",
    val apiKey: String = "",
    val model: String = "deepseek-v4-flash",
    val messages: List<AiChatMessageItem> = emptyList(),
)
