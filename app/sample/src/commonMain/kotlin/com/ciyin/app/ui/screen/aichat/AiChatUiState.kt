package com.ciyin.app.ui.screen.aichat

import androidx.compose.runtime.Immutable

/**
 * AI 聊天示例页面状态。
 *
 * @property baseUrl OpenAI 兼容接口根地址。
 * @property apiKey 接口密钥，本地端点可为空。
 * @property model 本次调用使用的模型名。
 * @property input 当前输入框文本。
 * @property messages 当前内存会话消息。
 * @property isStreaming 是否正在等待或接收流式回复。
 * @property errorMessage 最近一次可读错误文案。
 * @property nextMessageId 下一条消息使用的本地 ID。
 * @property activeRequestId 当前流式请求 ID。
 * @property activeAssistantMessageId 当前正在拼接的助手消息 ID。
 */
@Immutable
internal data class AiChatUiState(
    val baseUrl: String = "",
    val apiKey: String = "",
    val model: String = "",
    val input: String = "",
    val messages: List<AiChatMessageItem> = emptyList(),
    val isStreaming: Boolean = false,
    val errorMessage: String? = null,
    val nextMessageId: Long = 1L,
    val activeRequestId: Long? = null,
    val activeAssistantMessageId: Long? = null,
) {
    /** 当前配置是否具备发起请求的最小条件。 */
    val canSend: Boolean
        get() = input.isNotBlank() && baseUrl.isNotBlank() && model.isNotBlank() && !isStreaming
}
