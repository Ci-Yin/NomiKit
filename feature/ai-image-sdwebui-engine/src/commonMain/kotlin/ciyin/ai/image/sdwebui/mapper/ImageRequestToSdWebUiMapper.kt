package ciyin.ai.image.sdwebui.mapper

import ciyin.ai.core.error.AiEngineError
import ciyin.ai.core.image.ImageRequest
import ciyin.ai.core.image.ImageResult
import ciyin.ai.core.image.ImageSource
import ciyin.sdwebui.SdWebUi
import ciyin.sdwebui.payload.script.ADetailerScriptArgs
import ciyin.sdwebui.payload.script.ControlNetScriptArgs
import ciyin.sdwebui.payload.script.ScriptArgs
import ciyin.sdwebui.payload.script.ScriptPayload
import ciyin.sdwebui.process.Image2Image
import ciyin.sdwebui.process.Process
import ciyin.sdwebui.process.Process.Companion.runImage2Image
import ciyin.sdwebui.process.Process.Companion.runText2Image
import ciyin.sdwebui.process.Text2Image
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val ALWAYS_ON_SCRIPTS_OPTION = "sdwebui.alwaysonScripts"

private val vendorJson = Json {
    ignoreUnknownKeys = true
}

/**
 * 把通用层 [ImageRequest] 调用到 SD WebUI，并在需要时串接生成后处理流水线。
 */
internal suspend operator fun SdWebUi.invoke(request: ImageRequest): Result<ImageResult> =
    runCatching {
        request.model?.takeIf { it.isNotBlank() }?.let { model ->
            stableDiffusion.setModel(model).getOrThrow()
        }

        val generated = when (val source = request.source) {
            ImageSource.TextToImage -> runText2Image {
                applyCommonTextSettings(request)
            }.getOrThrow().toImageResult()

            is ImageSource.ImageToImage -> runImage2Image {
                applyCommonImageSettings(request, source.sourceImage, source.denoisingStrength)
            }.getOrThrow().toImageResult()

            is ImageSource.Inpainting -> runImage2Image {
                applyCommonImageSettings(request, source.sourceImage, source.denoisingStrength)
                mask(source.mask.toSdWebUiBase64())
            }.getOrThrow().toImageResult()
        }

        applyPostGenerationPostProcessors(generated, request.postProcessors).getOrThrow()
    }

/**
 * 把通用请求映射到 `txt2img` builder。
 */
private fun Text2Image.Builder.applyCommonTextSettings(request: ImageRequest) {
    prompt(request.prompt)
    negativePrompt(request.negativePrompt.orEmpty())
    width(request.size.width)
    height(request.size.height)
    batchSize(request.batch)
    request.steps?.let { steps(it) }
    request.cfgScale?.let { cfgScale(it) }
    request.seed?.let { seed(it.toInt()) }
    applyControls(request.controls)
    applyPreGenerationPostProcessors(request.postProcessors)
    applyVendorAlwaysOnScripts(request.vendorOptions)
}

/**
 * 把通用请求映射到 `img2img` / inpainting builder。
 */
private fun Image2Image.Builder.applyCommonImageSettings(
    request: ImageRequest,
    sourceImage: ByteArray,
    denoisingStrength: Float,
) {
    initImages(listOf(sourceImage.toSdWebUiBase64()))
    prompt(request.prompt)
    negativePrompt(request.negativePrompt.orEmpty())
    width(request.size.width)
    height(request.size.height)
    batchSize(request.batch)
    denoisingStrength(denoisingStrength)
    request.steps?.let { steps(it) }
    request.cfgScale?.let { cfgScale(it) }
    request.seed?.let { seed(it.toInt()) }
    applyControls(request.controls)
    applyPreGenerationPostProcessors(request.postProcessors)
    applyVendorAlwaysOnScripts(request.vendorOptions)
}

/**
 * 透传 `vendorOptions["sdwebui.alwaysonScripts"]` 到底层 Process DSL。
 */
private fun Process.Builder.applyVendorAlwaysOnScripts(
    vendorOptions: Map<String, JsonElement>,
) {
    val scripts = vendorOptions[ALWAYS_ON_SCRIPTS_OPTION] ?: return
    val scriptsObject = scripts as? JsonObject
        ?: throw AiEngineErrorException(
            AiEngineError.Unsupported("sdwebui.alwaysonScripts 必须是 JsonObject"),
        )
    scriptsObject.forEach { (key, value) ->
        addAlwaysonScript(key, value.toScriptPayload())
    }
}

/**
 * 把厂商透传的 JSON 转回 `feature/sdwebui` 可接受的 [ScriptPayload]。
 */
private fun JsonElement.toScriptPayload(): ScriptPayload {
    val payloadObject = this as? JsonObject
        ?: throw AiEngineErrorException(AiEngineError.Unsupported("alwayson script payload 必须是 JsonObject"))
    val args = payloadObject["args"]
        ?: throw AiEngineErrorException(AiEngineError.Unsupported("alwayson script payload 缺少 args"))

    return when (args) {
        is JsonObject -> ScriptPayload.Single(args.toScriptArgs())
        is JsonArray -> when {
            args.all { it is JsonObject } -> ScriptPayload.Multiple(args.map { (it as JsonObject).toScriptArgs() })
            args.all { it -> it is kotlinx.serialization.json.JsonPrimitive } ->
                ScriptPayload.Array(args.map { it.jsonPrimitive })

            else -> throw AiEngineErrorException(
                AiEngineError.Unsupported("alwayson script args 仅支持对象数组、单对象或基础类型数组"),
            )
        }

        else -> throw AiEngineErrorException(
            AiEngineError.Unsupported("alwayson script args 仅支持 JsonObject 或 JsonArray"),
        )
    }
}

/**
 * 把脚本参数对象识别并解码成当前 `feature/sdwebui` 已支持的脚本类型。
 */
private fun JsonObject.toScriptArgs(): ScriptArgs = when {
    containsKey("ad_model") -> vendorJson.decodeFromJsonElement(
        ADetailerScriptArgs.serializer(),
        this
    )

    containsKey("module") && containsKey("model") ->
        vendorJson.decodeFromJsonElement(ControlNetScriptArgs.serializer(), this)

    else -> throw AiEngineErrorException(
        AiEngineError.Unsupported("无法识别的 sdwebui script args 结构"),
    )
}