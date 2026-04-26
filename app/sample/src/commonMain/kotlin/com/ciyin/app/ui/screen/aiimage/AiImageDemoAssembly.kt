package com.ciyin.app.ui.screen.aiimage

import ciyin.ai.core.registry.DefaultChatEngineRegistry
import ciyin.ai.core.registry.DefaultImageEngineRegistry
import ciyin.ai.core.registry.EngineSelector
import ciyin.ai.facade.AiImage
import ciyin.ai.facade.DefaultAiImage
import ciyin.ai.image.sdwebui.SdWebUiImageEngine
import ciyin.ai.image.sdwebui.SdWebUiImageEngineConfig

internal fun createAiImageForDemo(host: String): AiImage {
    val sdEngine = SdWebUiImageEngine(
        SdWebUiImageEngineConfig(
            id = AiImageDemoEngineIds.id,
            host = host,
            port = AiImageDemoEngineIds.PORT,
            useHttps = false,
        ),
    )
    val selector = EngineSelector(
        chatRegistry = DefaultChatEngineRegistry(emptyList()),
        imageRegistry = DefaultImageEngineRegistry(listOf(sdEngine)),
    )
    return DefaultAiImage(
        selector = selector,
        preferences = AiImageDemoEnginePreferences,
        listeners = emptyList(),
    )
}

/**
 * 演示用 [ciyin.ai.facade.AiImage] 提供方，不注册全局 Koin，避免污染正式装配。
 * 对同一 [host] 复用实例，避免每次请求重复分配底层 HttpClient。
 */
internal object AiImageDemoGraph {
    private var cached: Pair<String, AiImage>? = null

    fun aiImage(host: String): AiImage {
        val h = host.trim()
        val existing = cached
        if (existing != null && existing.first == h) {
            return existing.second
        }
        return createAiImageForDemo(h).also { newImg ->
            cached = h to newImg
        }
    }
}
