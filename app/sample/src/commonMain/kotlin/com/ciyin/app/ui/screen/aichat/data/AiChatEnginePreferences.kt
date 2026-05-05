package com.ciyin.app.ui.screen.aichat.data

import ciyin.ai.core.engine.EngineId
import ciyin.ai.facade.selection.ChatEngineSpec
import ciyin.ai.facade.selection.EnginePreferences
import ciyin.ai.facade.selection.FallbackPolicy
import ciyin.ai.facade.selection.ImageEngineSpec

/**
 * AI 聊天示例使用的内存偏好实现。
 *
 * @property engineId 当前演示注册的聊天引擎 ID。
 * @property model 当前演示使用的模型名。
 */
internal class AiChatEnginePreferences(
    private val engineId: EngineId,
    private val model: String,
) : EnginePreferences {

    /** 返回当前 UI 配置指定的聊天模型。 */
    override suspend fun defaultChatSpec(): ChatEngineSpec = ChatEngineSpec.Explicit(
        engineId = engineId,
        model = model,
    )

    /** 本示例不提供生图能力，因此保持默认选择。 */
    override suspend fun defaultImageSpec(): ImageEngineSpec = ImageEngineSpec.Default

    /** Demo 中不自动重试，避免失败时重复请求本地或云端接口。 */
    override suspend fun chatFallback(): FallbackPolicy = FallbackPolicy(maxRetries = 0)

    /** 本示例不提供生图降级策略。 */
    override suspend fun imageFallback(): FallbackPolicy = FallbackPolicy(maxRetries = 0)
}
