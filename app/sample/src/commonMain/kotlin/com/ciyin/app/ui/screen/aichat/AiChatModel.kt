package com.ciyin.app.ui.screen.aichat

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * 聊天消息在界面中的角色。
 */
@Serializable
internal enum class AiChatMessageRole {
    /** 用户发送的消息。 */
    User,

    /** 助手返回的消息。 */
    Assistant,
}

/**
 * 聊天气泡的界面模型。
 *
 * @property id 进程内递增的消息标识，用于列表稳定 key。
 * @property role 消息角色，用于决定气泡对齐与颜色。
 * @property text 消息文本内容。
 */
@Immutable
@Serializable
internal data class AiChatMessageItem(
    val id: Long,
    val role: AiChatMessageRole,
    val text: String,
)

/**
 * OpenAI 兼容聊天端点配置。
 *
 * @property baseUrl OpenAI 兼容 `/v1` 根地址。
 * @property apiKey 可选鉴权密钥，本地 Ollama 通常可以留空。
 * @property model 模型名，例如 `llama3.1` 或 `gpt-4o-mini`。
 */
@Immutable
@Serializable
internal data class AiChatConnectionConfig(
    val baseUrl: String,
    val apiKey: String,
    val model: String,
)
