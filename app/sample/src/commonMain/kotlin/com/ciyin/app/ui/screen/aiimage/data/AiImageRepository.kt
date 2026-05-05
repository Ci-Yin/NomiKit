package com.ciyin.app.ui.screen.aiimage.data

import ciyin.ai.core.image.ImageEvent
import ciyin.ai.core.image.ImageRequest
import ciyin.ai.core.image.ImageSize
import ciyin.ai.core.image.ImageSource
import ciyin.ai.integrate.image.AiImageIntegrate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 文生图演示的数据与远程访问入口。
 *
 * 通过 [AiImageIntegrat] 注册 SD WebUI 并发起 [ImageRequest]；[com.ciyin.app.ui.screen.aiimage.AiImageViewModel] 只通过本类访问外部 API 与持久化。
 */
internal class AiImageRepository(
    private val aiImageIntegrate: AiImageIntegrate = AiImageIntegrate(),
) {

    private val dataStore = AiImageDataStore()

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

    fun generate(prompt: String): Flow<ImageEvent> {
        return aiImageIntegrate.generate(demoImageRequest(prompt))
    }

    /**
     * 与 [com.ciyin.app.ui.screen.aiimage.AiImageViewModel] 中原演示请求一致的正文生图 [ImageRequest]。
     */
    private fun demoImageRequest(prompt: String): ImageRequest = ImageRequest(
        prompt = prompt,
        source = ImageSource.TextToImage,
        size = ImageSize(600, 1000),
        negativePrompt = "mosaic,fellatio,lowres,(bad),missing,worst quality,low quality,watermark,oldest,chromatic aberration,extra digits,artistic error,username,[abstract],",
        steps = 34,
    )

}

@Serializable
private data class DemoTxt2ImgExtras(
    @SerialName("sampler_name") val samplerName: String? = null,
)
