package ciyin.ai.integrate.image

import ciyin.ai.facade.selection.ChatEngineSpec
import ciyin.ai.facade.selection.EnginePreferences
import ciyin.ai.facade.selection.FallbackPolicy
import ciyin.ai.facade.selection.ImageEngineSpec

/**
 * 仅供 [ciyin.ai.facade.impl.image.DefaultAiImage] 使用：默认生图路由固定到 [IntegrateImageEngineIds.sdWebUi]。
 *
 * [defaultChatSpec] 仅为满足 [EnginePreferences] 契约；首版不在本模块暴露 Chat 聚合能力。
 */
internal class IntegrateEnginePreferences : EnginePreferences {

    override suspend fun defaultChatSpec(): ChatEngineSpec = ChatEngineSpec.Default

    override suspend fun defaultImageSpec(): ImageEngineSpec =
        ImageEngineSpec.Explicit(engineId = IntegrateImageEngineIds.sdWebUi, model = null)

    override suspend fun chatFallback(): FallbackPolicy = FallbackPolicy()

    override suspend fun imageFallback(): FallbackPolicy = FallbackPolicy()
}
