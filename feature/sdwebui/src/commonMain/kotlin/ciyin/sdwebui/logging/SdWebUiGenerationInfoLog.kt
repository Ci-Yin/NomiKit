package ciyin.sdwebui.logging

import ciyin.platform.logger
import ciyin.sdwebui.payload.Image2ImagePayload
import ciyin.sdwebui.payload.Text2ImagePayload
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

private val genLog = logger("StableDiffusion.Generation")

private val infoJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

/**
 * 在 `txt2img` / `img2img` 成功后打印与 WebUI 控制台相近的生成摘要，便于对照排查。
 */
internal object SdWebUiGenerationInfoLog {

    fun afterTxt2img(payload: Text2ImagePayload, responseInfo: String) {
        logBlock("sdapi/v1/txt2img", summarize(responseInfo) { formatFromTxtPayload(payload) })
    }

    fun afterImg2img(payload: Image2ImagePayload, responseInfo: String) {
        logBlock("sdapi/v1/img2img", summarize(responseInfo) { formatFromImgPayload(payload) })
    }

    private fun logBlock(endpoint: String, body: String) {
        if (body.isBlank()) {
            genLog.i { "[$endpoint] 生成完成（无摘要可打印）" }
            return
        }
        genLog.i { "[$endpoint] 生成摘要 ↓\n$body" }
    }

    private fun summarize(responseInfo: String, payloadFallback: () -> String): String {
        val trimmed = responseInfo.trim()
        if (trimmed.isEmpty()) return payloadFallback()
        val fromApi = formatFromResponseInfo(trimmed)
        if (fromApi.isNotBlank()) return fromApi
        return payloadFallback()
    }
}

private fun formatFromResponseInfo(info: String): String {
    val root = runCatching { infoJson.parseToJsonElement(info) }.getOrNull() ?: return ""
    val obj = root as? JsonObject ?: return ""

    obj["infotexts"]?.jsonArray?.let { arr ->
        val lines = arr.mapNotNull { it.asTrimmedString() }.filter { it.isNotBlank() }
        if (lines.isNotEmpty()) return lines.joinToString("\n")
    }

    obj["infotext"]?.jsonPrimitive?.contentOrNull?.trim()?.takeIf { it.isNotEmpty() }
        ?.let { return it }

    return formatFromInfoJsonObject(obj)
}

private fun formatFromInfoJsonObject(o: JsonObject): String {
    val prompt = o.str("prompt") ?: return ""
    val neg = o.str("negative_prompt").orEmpty()
    val steps = o.anyStr("steps")
    val sampler = o.str("sampler_name").orEmpty().ifBlank { o.str("sampler_index").orEmpty() }
    val schedule = o.str("schedule_type").orEmpty().ifBlank { o.str("sampler_scheduler").orEmpty() }
        .ifBlank { "Automatic" }
    val cfg = o.anyStr("cfg_scale")
    val seed = o.anyStr("seed")
    val w = o.anyStr("width")
    val h = o.anyStr("height")
    val size = if (w != null && h != null) "${w}x$h" else null
    val modelHash = o.str("model_hash").orEmpty().ifBlank { o.str("sd_model_hash").orEmpty() }
    val model = o.str("model_name").orEmpty().ifBlank { o.str("sd_model_name").orEmpty() }
    val clipSkip = o.anyStr("clip_skip")
    val loraHashes = o.str("lora_hashes")
    val version = o.str("version")
    val sourceId = o.str("source_identifier")

    val tail = buildList {
        add("Steps: ${steps ?: "—"}, Sampler: ${sampler.ifBlank { "—" }}, Schedule type: $schedule, CFG scale: ${cfg ?: "—"}, Seed: ${seed ?: "—"}, Size: ${size ?: "—"}")
        if (modelHash.isNotBlank()) add("Model hash: $modelHash")
        if (model.isNotBlank()) add("Model: $model")
        if (clipSkip != null) add("Clip skip: $clipSkip")
        if (!loraHashes.isNullOrBlank()) add("Lora hashes: $loraHashes")
        if (!version.isNullOrBlank()) add("Version: $version")
        if (!sourceId.isNullOrBlank()) add("Source Identifier: $sourceId")
    }

    return buildString {
        appendLine(prompt.trimEnd())
        append("Negative prompt: ")
        appendLine(neg.trimEnd())
        tail.forEach { appendLine(it) }
    }.trimEnd()
}

private fun formatFromTxtPayload(p: Text2ImagePayload): String {
    val sampler = p.samplerName.ifBlank { p.samplerIndex }.ifBlank { "—" }
    return buildString {
        appendLine(p.prompt.trimEnd())
        append("Negative prompt: ")
        appendLine(p.negativePrompt.trimEnd())
        appendLine(
            "Steps: ${p.steps}, Sampler: $sampler, Schedule type: —, CFG scale: ${p.cfgScale}, " +
                    "Seed: ${p.seed}, Size: ${p.width}x${p.height}（请求体；服务端未返回可解析的 info JSON）",
        )
    }.trimEnd()
}

private fun formatFromImgPayload(p: Image2ImagePayload): String {
    val sampler = p.samplerName.ifBlank { p.samplerIndex }.ifBlank { "—" }
    return buildString {
        appendLine(p.prompt.trimEnd())
        append("Negative prompt: ")
        appendLine(p.negativePrompt.trimEnd())
        appendLine(
            "Steps: ${p.steps}, Sampler: $sampler, Schedule type: —, CFG scale: ${p.cfgScale}, " +
                    "Seed: ${p.seed}, Size: ${p.width}x${p.height}, Denoising: ${p.denoisingStrength}（请求体；服务端未返回可解析的 info JSON）",
        )
    }.trimEnd()
}

private fun JsonObject.str(key: String): String? = this[key]?.asTrimmedString()

private fun JsonObject.anyStr(key: String): String? =
    when (val e = this[key]) {
        is JsonPrimitive -> e.contentOrNull?.trim()?.takeIf { it.isNotEmpty() }
        null, JsonNull -> null
        else -> e.toString().trim().removeSurrounding("\"").takeIf { it.isNotEmpty() }
    }

private fun JsonElement.asTrimmedString(): String? = when (this) {
    is JsonPrimitive -> contentOrNull?.trim()?.takeIf { it.isNotEmpty() }
    is JsonArray -> null
    is JsonObject -> null
    JsonNull -> null
}
