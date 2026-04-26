package com.ciyin.app.ui.screen.aiimage.data

import ciyin.ai.core.image.ImageEvent
import ciyin.ai.core.image.ImageRequest
import ciyin.ai.core.image.ImageSize
import ciyin.ai.core.image.ImageSource
import ciyin.ai.core.registry.DefaultChatEngineRegistry
import ciyin.ai.core.registry.DefaultImageEngineRegistry
import ciyin.ai.core.registry.EngineSelector
import ciyin.ai.facade.AiImage
import ciyin.ai.facade.DefaultAiImage
import ciyin.ai.image.sdwebui.SdWebUiImageEngine
import ciyin.ai.image.sdwebui.SdWebUiImageEngineConfig
import ciyin.ai.image.sdwebui.model.buildSdWebUiText2ImageExtras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * 文生图演示的数据与远程访问入口。
 *
 * 集中持有 [AiImage] 的构造与缓存、[ImageRequest] 的默认装配，以及 [AiImagePreferences] 的 DataStore 读写；
 * [com.ciyin.app.ui.screen.aiimage.AiImageViewModel] 只通过本类访问外部 API 与持久化。
 */
internal class AiImageRepository(
    private val dataStore: AiImageDataStore = AiImageDataStore(),
) {
    private var cachedAiImage: Pair<String, AiImage>? = null

    suspend fun loadPreferences(): AiImagePreferences = withContext(Dispatchers.IO) {
        dataStore.data.first()
    }

    suspend fun persistServerHostAndPrompt(serverHost: String, prompt: String) {
        dataStore.updateData { prefs ->
            prefs.copy(
                serverHost = serverHost.trim(),
                prompt = prompt,
            )
        }
    }

    /**
     * 与 [com.ciyin.app.ui.screen.aiimage.AiImageViewModel] 中原演示请求一致的正文生图 [ImageRequest]。
     */
    fun demoImageRequest(prompt: String): ImageRequest = ImageRequest(
        prompt = prompt,
        source = ImageSource.TextToImage,
        size = ImageSize(600, 1000),
        negativePrompt = "mosaic,fellatio,lowres,(bad),missing,worst quality,low quality,watermark,oldest,chromatic aberration,extra digits,artistic error,username,[abstract],",
        steps = 34,
        vendorOptions = mapOf(
            buildSdWebUiText2ImageExtras {
                copy(
                    samplerName = "Euler a",
                )
            }
        ),
    )

    /**
     * 对同一 [host]（trim 后）复用 [AiImage]，避免每次请求重复分配底层 HttpClient。
     */
    fun aiImage(host: String): AiImage {
        val h = host.trim()
        val existing = cachedAiImage
        if (existing != null && existing.first == h) {
            return existing.second
        }
        return createAiImageForDemo(h).also { newImg ->
            cachedAiImage = h to newImg
        }
    }

    fun generate(host: String, prompt: String): Flow<ImageEvent> =
        aiImage(host).generate(demoImageRequest(prompt))

    private fun createAiImageForDemo(host: String): AiImage {
        val sdEngine = SdWebUiImageEngine(
            SdWebUiImageEngineConfig(
                id = AiImageEngineIds.id,
                host = host,
                port = AiImageEngineIds.PORT,
                useHttps = false,
            ),
        )
        val selector = EngineSelector(
            chatRegistry = DefaultChatEngineRegistry(emptyList()),
            imageRegistry = DefaultImageEngineRegistry(listOf(sdEngine)),
        )
        return DefaultAiImage(
            selector = selector,
            preferences = AiImageEnginePreferences,
            listeners = emptyList(),
        )
    }
}
