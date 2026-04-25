package ciyin.sdwebui.service

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
import ciyin.sdwebui.response.MemoryResponse
import ciyin.sdwebui.response.ModelResponse
import ciyin.sdwebui.response.ProgressResponse
import ciyin.sdwebui.response.RealesrganModelResponse
import ciyin.sdwebui.response.RemBGResponse
import ciyin.sdwebui.response.ScriptsResponse
import ciyin.sdwebui.response.UpscalerResponse
import ciyin.sdwebui.response.VaeResponse

/**
 * Stable Diffusion WebUI 核心 `sdapi/v1/` 与 RemBG 等生成、查询能力。
 */
interface StableDiffusionService {

    /** 文生图：`sdapi/v1/txt2img`。 */
    suspend fun text2Image(payload: Text2ImagePayload): Result<GenerateProcessResponse>

    /** 图生图：`sdapi/v1/img2img`。 */
    suspend fun image2Image(payload: Image2ImagePayload): Result<GenerateProcessResponse>

    /** 单张后期处理：`sdapi/v1/extra-single-image`。 */
    suspend fun extraSingleImage(payload: ExtraSingleImagePayload): Result<ExtraSingleImageResponse>

    /** 批量后期处理：`sdapi/v1/extra-batch-images`。 */
    suspend fun extraBatchImages(payload: ExtraBatchImagesPayload): Result<ExtraBatchImagesResponse>

    /** 查询已安装 checkpoint 列表：`sdapi/v1/sd-models`。 */
    suspend fun getModels(): Result<List<ModelResponse>>

    /** 查询采样器列表：`sdapi/v1/samplers`。 */
    suspend fun getSamplers(): Result<String>

    /** 查询词嵌入：`sdapi/v1/embeddings`。 */
    suspend fun getEmbeddings(): Result<String>

    /** 查询 VAE 列表：`sdapi/v1/sd-vae`。 */
    suspend fun getVae(): Result<List<VaeResponse>>

    /** 查询 LoRA：`sdapi/v1/loras`。 */
    suspend fun getLoras(): Result<String>

    /** 查询全局选项 JSON：`sdapi/v1/options`。 */
    suspend fun getOptions(): Result<String>

    /** 查询启动命令行参数：`sdapi/v1/cmd-flags`。 */
    suspend fun getCmdFlags(): Result<String>

    /** 查询扩展列表：`sdapi/v1/extensions`。 */
    suspend fun getExtensions(): Result<List<ExtensionResponse>>

    /** 查询超网络：`sdapi/v1/hypernetworks`。 */
    suspend fun getHypernetworks(): Result<String>

    /** 查询面部修复器：`sdapi/v1/face-restorers`。 */
    suspend fun getFaceRestorers(): Result<List<FaceRestorerResponse>>

    /** 查询 Real-ESRGAN 模型：`sdapi/v1/realesrgan-models`。 */
    suspend fun getRealesrganModels(): Result<List<RealesrganModelResponse>>

    /** 查询提示词风格：`sdapi/v1/prompt-styles`。 */
    suspend fun getPromptStyles(): Result<String>

    /** 查询放大器：`sdapi/v1/upscalers`。 */
    suspend fun getUpscalers(): Result<List<UpscalerResponse>>

    /** 查询潜空间放大模式：`sdapi/v1/latent-upscale-modes`。 */
    suspend fun getLatentUpscaleModes(): Result<List<LatentUpscaleModeResponse>>

    /** 查询可用脚本名：`sdapi/v1/scripts`。 */
    suspend fun getScripts(): Result<ScriptsResponse>

    /** 查询脚本参数详情：`sdapi/v1/script-info`。 */
    suspend fun getScriptInfo(): Result<String>

    /** 查询当前生成进度：`sdapi/v1/progress`。 */
    suspend fun getProgress(): Result<ProgressResponse>

    /** 查询内存与显存占用：`sdapi/v1/memory`。 */
    suspend fun getMemory(): Result<MemoryResponse>

    /** 通过写入 options 切换当前模型：`sdapi/v1/options`（POST）。 */
    suspend fun setModel(model: String): Result<Unit>

    /** 刷新 checkpoint 列表：`sdapi/v1/refresh-checkpoints`（POST）。 */
    suspend fun refreshCheckpoints(): Result<Unit>

    /** 调用 RemBG 扩展移除背景：`rembg`（POST）。 */
    suspend fun remBG(payload: RemBGPayload): Result<RemBGResponse>
}
