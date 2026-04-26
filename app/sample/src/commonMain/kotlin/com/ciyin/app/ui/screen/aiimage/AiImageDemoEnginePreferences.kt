package com.ciyin.app.ui.screen.aiimage

import ciyin.ai.facade.selection.ChatModelSpec
import ciyin.ai.facade.selection.EnginePreferences
import ciyin.ai.facade.selection.FallbackPolicy
import ciyin.ai.facade.selection.ImageModelSpec

/**
 * 装配用 [ciyin.ai.facade.selection.EnginePreferences]：默认生图指向本地 SD WebUI 引擎；聊天侧仅占位（本示例不生文）。
 */
internal object AiImageDemoEnginePreferences : EnginePreferences {

    override suspend fun defaultChatSpec(): ChatModelSpec = ChatModelSpec.Default

    override suspend fun defaultImageSpec(): ImageModelSpec =
        ImageModelSpec.Explicit(engineId = AiImageDemoEngineIds.id, model = null)

    override suspend fun chatFallback(): FallbackPolicy = FallbackPolicy()

    override suspend fun imageFallback(): FallbackPolicy = FallbackPolicy()
}
