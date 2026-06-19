package ciyin.sdwebui.service

import ciyin.platform.logger
import ciyin.sdwebui.client.Client
import ciyin.sdwebui.client.Client.Companion.body
import ciyin.sdwebui.client.Client.Companion.get
import ciyin.sdwebui.client.Client.Companion.post
import ciyin.sdwebui.payload.ExtraBatchImagesPayload
import ciyin.sdwebui.payload.ExtraSingleImagePayload
import ciyin.sdwebui.payload.Image2ImagePayload
import ciyin.sdwebui.payload.RemBGPayload
import ciyin.sdwebui.payload.Text2ImagePayload
import ciyin.sdwebui.response.ExtensionResponse
import ciyin.sdwebui.response.ExtraBatchImagesResponse
import ciyin.sdwebui.response.ExtraSingleImageResponse
import ciyin.sdwebui.response.FaceRestorerResponse
import ciyin.sdwebui.response.GenerateProcessResponse
import ciyin.sdwebui.response.LatentUpscaleModeResponse
import ciyin.sdwebui.response.LoraResponse
import ciyin.sdwebui.response.MemoryResponse
import ciyin.sdwebui.response.ModelResponse
import ciyin.sdwebui.response.ProgressResponse
import ciyin.sdwebui.response.RealesrganModelResponse
import ciyin.sdwebui.response.RemBGResponse
import ciyin.sdwebui.response.ScriptsResponse
import ciyin.sdwebui.response.UpscalerResponse
import ciyin.sdwebui.response.VaeResponse
import io.ktor.util.logging.debug
import kotlinx.serialization.json.Json

/**
 * [StableDiffusionService] 的默认实现，路径与官方 WebUI REST 文档对齐。
 */
class StableDiffusionServiceImpl(
    override val baseUrl: String,
    override val client: Client,
    override val json: Json,
) : Service(), StableDiffusionService {

    private val logger = logger("StableDiffusion.Service")

    override suspend fun text2Image(payload: Text2ImagePayload): Result<GenerateProcessResponse> {
        return client.post<GenerateProcessResponse>(json) {
            baseUrl(baseUrl)
            path("sdapi/v1/txt2img")
            body(payload)
        }.map {
            logger.d { "text2Image response: ${it.info}" }
            it
        }
    }

    override suspend fun image2Image(payload: Image2ImagePayload): Result<GenerateProcessResponse> {
        return client.post<GenerateProcessResponse>(json) {
            baseUrl(baseUrl)
            path("sdapi/v1/img2img")
            body(payload)
        }.map {
            logger.d { "image2Image response: ${it.info}" }
            it
        }
    }

    override suspend fun extraSingleImage(payload: ExtraSingleImagePayload): Result<ExtraSingleImageResponse> {
        return client.post<ExtraSingleImageResponse>(json) {
            baseUrl(baseUrl)
            path("sdapi/v1/extra-single-image")
            body(payload)
        }.map {
            logger.d { "extraSingleImage response: ${it.htmlInfo}" }
            it
        }
    }

    override suspend fun extraBatchImages(payload: ExtraBatchImagesPayload): Result<ExtraBatchImagesResponse> {
        return client.post<ExtraBatchImagesResponse>(json) {
            baseUrl(baseUrl)
            path("sdapi/v1/extra-batch-images")
            body(payload)
        }.map {
            logger.d { "extraBatchImages response: ${it.htmlInfo}" }
            it
        }
    }

    override suspend fun getModels(): Result<List<ModelResponse>> {
        return client.get(json, baseUrl, "sdapi/v1/sd-models")
    }

    override suspend fun getSamplers(): Result<String> {
        return client.get(json, baseUrl, "sdapi/v1/samplers")
    }

    override suspend fun getEmbeddings(): Result<String> {
        return client.get(json, baseUrl, "sdapi/v1/embeddings")
    }

    override suspend fun getVae(): Result<List<VaeResponse>> {
        return client.get(json, baseUrl, "sdapi/v1/sd-vae")
    }

    /** 查询 LoRA 列表，并把 metadata 对象保留为响应模型中的 JSON 字符串。 */
    override suspend fun getLoras(): Result<List<LoraResponse>> {
        return client.get(json, baseUrl, "sdapi/v1/loras")
    }

    override suspend fun getOptions(): Result<String> {
        return client.get(json, baseUrl, "sdapi/v1/options")
    }

    override suspend fun getCmdFlags(): Result<String> {
        return client.get(json, baseUrl, "sdapi/v1/cmd-flags")
    }

    override suspend fun getExtensions(): Result<List<ExtensionResponse>> {
        return client.get(json, baseUrl, "sdapi/v1/extensions")
    }

    override suspend fun getHypernetworks(): Result<String> {
        return client.get(json, baseUrl, "sdapi/v1/hypernetworks")
    }

    override suspend fun getFaceRestorers(): Result<List<FaceRestorerResponse>> {
        return client.get(json, baseUrl, "sdapi/v1/face-restorers")
    }

    override suspend fun getRealesrganModels(): Result<List<RealesrganModelResponse>> {
        return client.get(json, baseUrl, "sdapi/v1/realesrgan-models")
    }

    override suspend fun getPromptStyles(): Result<String> {
        return client.get(json, baseUrl, "sdapi/v1/prompt-styles")
    }

    override suspend fun getUpscalers(): Result<List<UpscalerResponse>> {
        return client.get(json, baseUrl, "sdapi/v1/upscalers")
    }

    override suspend fun getLatentUpscaleModes(): Result<List<LatentUpscaleModeResponse>> {
        return client.get(json, baseUrl, "sdapi/v1/latent-upscale-modes")
    }

    override suspend fun getScripts(): Result<ScriptsResponse> {
        return client.get(json, baseUrl, "sdapi/v1/scripts")
    }

    override suspend fun getScriptInfo(): Result<String> {
        return client.get(json, baseUrl, "sdapi/v1/script-info")
    }

    override suspend fun getProgress(): Result<ProgressResponse> {
        return client.get(json, baseUrl, "sdapi/v1/progress")
    }

    override suspend fun getMemory(): Result<MemoryResponse> {
        return client.get(json, baseUrl, "sdapi/v1/memory")
    }

    override suspend fun setModel(model: String): Result<Unit> {
        return client.post(json) {
            baseUrl(baseUrl)
            path("sdapi/v1/options")
            body(mapOf("sd_model_checkpoint" to model))
        }
    }

    override suspend fun refreshCheckpoints(): Result<Unit> {
        return client.post(json) {
            baseUrl(baseUrl)
            path("sdapi/v1/refresh-checkpoints")
        }
    }

    override suspend fun remBG(payload: RemBGPayload): Result<RemBGResponse> {
        return client.post(json) {
            baseUrl(baseUrl)
            path("rembg")
            body(payload)
        }
    }
}
