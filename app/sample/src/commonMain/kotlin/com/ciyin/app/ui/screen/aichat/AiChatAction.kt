package com.ciyin.app.ui.screen.aichat

import ciyin.ai.core.chat.ChatResponse
import ciyin.ai.core.error.AiEngineError
import com.ciyin.app.ui.screen.aichat.data.AiChatPreferences

/**
 * AI 聊天示例页面动作。
 */
internal sealed interface AiChatAction {
    /**
     * 从 DataStore 恢复的配置与历史消息（启动时由 ViewModel 派发一次）。
     *
     * @property prefs 磁盘中的偏好快照。
     */
    data class PrefsLoaded(val prefs: AiChatPreferences) : AiChatAction

    /** 点击返回按钮。 */
    data object BackClick : AiChatAction

    /**
     * 修改接口根地址。
     *
     * @property value 新的 baseUrl。
     */
    data class BaseUrlChange(val value: String) : AiChatAction

    /**
     * 修改 API Key。
     *
     * @property value 新的密钥文本。
     */
    data class ApiKeyChange(val value: String) : AiChatAction

    /**
     * 修改模型名。
     *
     * @property value 新的模型名。
     */
    data class ModelChange(val value: String) : AiChatAction

    /**
     * 修改输入框内容。
     *
     * @property value 新的输入文本。
     */
    data class InputChange(val value: String) : AiChatAction

    /** 点击发送按钮。 */
    data object SendClick : AiChatAction

    /**
     * AI 引擎确认本轮请求已经开始。
     *
     * @property requestId 本轮请求 ID。
     */
    data class ChatStarted(val requestId: Long) : AiChatAction

    /**
     * AI 引擎返回一段流式文本。
     *
     * @property requestId 本轮请求 ID。
     * @property text 本次新增文本。
     */
    data class ChatDelta(val requestId: Long, val text: String) : AiChatAction

    /**
     * AI 引擎完成本轮回复。
     *
     * @property requestId 本轮请求 ID。
     * @property response 完整聚合后的回复。
     */
    data class ChatCompleted(val requestId: Long, val response: ChatResponse) : AiChatAction

    /**
     * AI 引擎以结构化错误结束本轮回复。
     *
     * @property requestId 本轮请求 ID。
     * @property error 引擎错误模型。
     */
    data class ChatFailed(val requestId: Long, val error: AiEngineError) : AiChatAction

    /**
     * 调用链抛出了非结构化异常。
     *
     * @property requestId 本轮请求 ID。
     * @property message 可读异常文案。
     */
    data class ChatException(val requestId: Long, val message: String) : AiChatAction
}
