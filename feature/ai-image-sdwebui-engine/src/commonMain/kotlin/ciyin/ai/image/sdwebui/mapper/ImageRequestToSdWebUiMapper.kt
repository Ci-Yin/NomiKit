package ciyin.ai.image.sdwebui.mapper

import ciyin.ai.core.error.AiEngineError
import ciyin.ai.core.image.ImageRequest
import ciyin.ai.core.image.ImageResult
import ciyin.ai.core.image.ImageSource
import ciyin.ai.image.sdwebui.model.SdWebUiImageVendorOptionKeys
import ciyin.ai.image.sdwebui.model.SdWebUiImg2ImgExtras
import ciyin.ai.image.sdwebui.model.SdWebUiText2ImageExtras
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive

private val txt2imgHiResFlatKeys: Set<String> = setOf(
    "enable_hr",
    "denoising_strength",
    "hr_scale",
    "hr_upscaler",
    "hr_second_pass_steps",
    "hr_resize_x",
    "hr_resize_y",
)

/**
 * WebUI 常将 Hi-res 相关键放在 extras JSON 顶层；[ciyin.ai.image.sdwebui.model.SdWebUiText2ImageExtras] 使用嵌套 `hi_res`。
 * 若无 `hi_res` 且存在任一扁平键，则合并为 `hi_res` 后再解码。
 */
private fun normalizeTxt2imgExtrasJson(element: JsonElement): JsonElement {
    val obj = element as? JsonObject ?: return element
    if (obj.containsKey("hi_res")) return element
    val nested = LinkedHashMap<String, JsonElement>()
    for (k in txt2imgHiResFlatKeys) {
        obj[k]?.let { nested[k] = it }
    }
    if (nested.isEmpty()) return element
    return buildJsonObject {
        obj.forEach { (k, v) ->
            if (k !in txt2imgHiResFlatKeys) put(k, v)
        }
        put("hi_res", JsonObject(nested))
    }
}

private val vendorJson = Json {
    ignoreUnknownKeys = true
}

/**
 * 把通用层 [ImageRequest] 调用到 SD WebUI，并在需要时串接生成后处理流水线。
 */
internal suspend operator fun SdWebUi.invoke(
    request: ImageRequest
): Result<ImageResult> = runCatching {
    request.model?.takeIf { it.isNotBlank() }?.let { model ->
        stableDiffusion.setModel(model).getOrThrow()
    }

    val generated = when (val source = request.source) {
        ImageSource.TextToImage -> runText2Image {
            applyCommonTextSettings(request)
            applyTxt2imgVendorExtras(request)
        }.getOrThrow()

        is ImageSource.ImageToImage -> runImage2Image {
            applyCommonImageSettings(request, source.sourceImage, source.denoisingStrength)
            applyImg2imgVendorExtras(request)
        }.getOrThrow()

        is ImageSource.Inpainting -> runImage2Image {
            applyCommonImageSettings(request, source.sourceImage, source.denoisingStrength)
            mask(source.mask.toSdWebUiBase64())
            applyImg2imgVendorExtras(request)
        }.getOrThrow()
    }

    applyPostGenerationPostProcessors(
        generated.toImageResult(),
        request.postProcessors
    ).getOrThrow()
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

private fun Text2Image.Builder.applyTxt2imgVendorExtras(request: ImageRequest) {
    val element = request.vendorOptions[SdWebUiImageVendorOptionKeys.txt2imgExtras] ?: return
    val extras = decodeVendorExtras(element, SdWebUiImageVendorOptionKeys.txt2imgExtras) { el ->
        vendorJson.decodeFromJsonElement(
            SdWebUiText2ImageExtras.serializer(),
            normalizeTxt2imgExtrasJson(el),
        )
    }
    extras.styles?.let { styles(it) }
    extras.subseed?.let { subseed(it) }
    extras.subseedStrength?.let { subseedStrength(it) }
    extras.seedResizeFromH?.let { seedResizeFromH(it) }
    extras.seedResizeFromW?.let { seedResizeFromW(it) }
    extras.samplerName?.let { samplerName(it) }
    extras.samplerIndex?.let { samplerIndex(it) }
    extras.nIter?.let { nIter(it) }
    extras.restoreFaces?.let { restoreFaces(it) }
    extras.tiling?.let { tiling(it) }
    extras.doNotSaveSamples?.let { doNotSaveSamples(it) }
    extras.doNotSaveGrid?.let { doNotSaveGrid(it) }
    extras.eta?.let { eta(it) }
    extras.sChurn?.let { sChurn(it) }
    extras.sTmax?.let { sTmax(it) }
    extras.sTmin?.let { sTmin(it) }
    extras.sNoise?.let { sNoise(it) }
    extras.overrideSettings?.let { overrideSettings(it) }
    extras.overrideSettingsRestoreAfterwards?.let { overrideSettingsRestoreAfterwards(it) }
    extras.comments?.let { comments(it) }
    extras.firstphaseWidth?.let { firstphaseWidth(it) }
    extras.firstphaseHeight?.let { firstphaseHeight(it) }
    extras.hiresFix?.enable?.let { enableHr(it) }
    extras.hiresFix?.denoisingStrength?.let { denoisingStrength(it) }
    extras.hiresFix?.scale?.let { hrScale(it) }
    extras.hiresFix?.upscaler?.let { hrUpscaler(it) }
    extras.hiresFix?.secondPassSteps?.let { hrSecondPassSteps(it) }
    extras.hiresFix?.resizeX?.let { hrResizeX(it) }
    extras.hiresFix?.resizeY?.let { hrResizeY(it) }
    extras.scriptName?.let { scriptName(it) }
    extras.scriptArgs?.let { scriptArgs(it) }
    extras.sendImages?.let { sendImages(it) }
    extras.saveImages?.let { saveImages(it) }
    extras.alwaysonScripts?.forEach { (key, payload) -> addAlwaysonScript(key, payload) }
}

private fun Image2Image.Builder.applyImg2imgVendorExtras(request: ImageRequest) {
    val element = request.vendorOptions[SdWebUiImageVendorOptionKeys.img2imgExtras] ?: return
    val extras = decodeVendorExtras(element, SdWebUiImageVendorOptionKeys.img2imgExtras) { el ->
        vendorJson.decodeFromJsonElement(SdWebUiImg2ImgExtras.serializer(), el)
    }
    extras.resizeMode?.let { resizeMode(it) }
    extras.maskBlur?.let { maskBlur(it) }
    extras.inpaintingFill?.let { inpaintingFill(it) }
    extras.inpaintFullRes?.let { inpaintFullRes(it) }
    extras.inpaintFullResPadding?.let { inpaintFullResPadding(it) }
    extras.inpaintingMaskInvert?.let { inpaintingMaskInvert(it) }
    extras.initialNoiseMultiplier?.let { initialNoiseMultiplier(it) }
    extras.styles?.let { styles(it) }
    extras.subseed?.let { subseed(it) }
    extras.subseedStrength?.let { subseedStrength(it) }
    extras.seedResizeFromH?.let { seedResizeFromH(it) }
    extras.seedResizeFromW?.let { seedResizeFromW(it) }
    extras.nIter?.let { nIter(it) }
    extras.imageCfgScale?.let { imageCfgScale(it) }
    extras.restoreFaces?.let { restoreFaces(it) }
    extras.tiling?.let { tiling(it) }
    extras.doNotSaveSamples?.let { doNotSaveSamples(it) }
    extras.eta?.let { eta(it) }
    extras.sChurn?.let { sChurn(it) }
    extras.sTmax?.let { sTmax(it) }
    extras.sTmin?.let { sTmin(it) }
    extras.sNoise?.let { sNoise(it) }
    extras.overrideSettings?.let { overrideSettings(it) }
    extras.overrideSettingsRestoreAfterwards?.let { overrideSettingsRestoreAfterwards(it) }
    extras.samplerName?.let { samplerName(it) }
    extras.samplerIndex?.let { samplerIndex(it) }
    extras.includeInitImages?.let { includeInitImages(it) }
    extras.scriptName?.let { scriptName(it) }
    extras.scriptArgs?.let { scriptArgs(it) }
    extras.sendImages?.let { sendImages(it) }
    extras.saveImages?.let { saveImages(it) }
    extras.alwaysonScripts?.forEach { (key, payload) -> addAlwaysonScript(key, payload) }
}

private inline fun <T> decodeVendorExtras(
    element: JsonElement,
    optionKey: String,
    crossinline decode: (JsonElement) -> T,
): T = try {
    decode(element)
} catch (e: Throwable) {
    throw AiEngineErrorException(
        AiEngineError.Unsupported("$optionKey 解码失败: ${e.message}"),
    )
}

/**
 * 透传 [SdWebUiImageVendorOptionKeys.alwaysOnScripts] 到底层 Process DSL。
 */
private fun Process.Builder.applyVendorAlwaysOnScripts(
    vendorOptions: Map<String, JsonElement>,
) {
    val scripts = vendorOptions[SdWebUiImageVendorOptionKeys.alwaysOnScripts] ?: return
    val scriptsObject = scripts as? JsonObject
        ?: throw AiEngineErrorException(
            AiEngineError.Unsupported("${SdWebUiImageVendorOptionKeys.alwaysOnScripts} 必须是 JsonObject"),
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