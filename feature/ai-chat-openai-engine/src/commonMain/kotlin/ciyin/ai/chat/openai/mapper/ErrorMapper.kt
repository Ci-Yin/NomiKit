package ciyin.ai.chat.openai.mapper

import ciyin.ai.chat.openai.client.OpenAiChatStreamHttpException
import ciyin.ai.chat.openai.client.OpenAiJson
import ciyin.ai.core.error.AiEngineError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * 把 Ktor / mapper 层异常统一折叠为 [AiEngineError]。
 */
internal fun Throwable.toAiEngineError(): AiEngineError = when (this) {
    is kotlin.coroutines.cancellation.CancellationException -> throw this
    is OpenAiChatStreamHttpException -> mapOpenAiStreamHttpFailure(this)
    is OpenAiMappingException -> error
    is IOException -> AiEngineError.Network(cause = this, message = message)
    is SerializationException -> AiEngineError.Protocol(
        message = message ?: "响应解析失败",
        cause = this
    )

    is ClientRequestException -> mapResponseException()
    is ServerResponseException -> protocolErrorFromResponseException(this)

    is ResponseException -> protocolErrorFromResponseException(this)

    else -> AiEngineError.Unknown(cause = this, message = message)
}

private fun mapOpenAiStreamHttpFailure(ex: OpenAiChatStreamHttpException): AiEngineError {
    val parsed = parseOpenAiStyleErrorMessage(ex.bodyText)
    val detail = parsed ?: ex.bodyText.trim().ifBlank { null }
    ?: ex.status.description.trim().ifBlank { null }
    ?: "HTTP ${ex.status.value}"
    return when (ex.status) {
        HttpStatusCode.Unauthorized,
        HttpStatusCode.Forbidden,
            -> AiEngineError.Unauthorized(providerMessage = detail)

        HttpStatusCode.TooManyRequests -> AiEngineError.RateLimited(
            retryAfterMs = null,
            providerMessage = detail,
        )

        else -> AiEngineError.Protocol(message = detail, cause = ex)
    }
}

/**
 * 把 4xx 响应映射成更具体的错误类型。
 */
private fun ClientRequestException.mapResponseException(): AiEngineError {
    val detail = responseExceptionDetailMessage(this)
    return when (response.status) {
        HttpStatusCode.Unauthorized,
        HttpStatusCode.Forbidden,
            -> AiEngineError.Unauthorized(providerMessage = detail)

        HttpStatusCode.TooManyRequests -> AiEngineError.RateLimited(
            retryAfterMs = response.headers["Retry-After"]?.toLongOrNull()?.times(1000),
            providerMessage = detail,
        )

        else -> AiEngineError.Protocol(message = detail, cause = this)
    }
}

/**
 * 从 Ktor [ResponseException.message] 里取出 `Text: "..."` 段（与 [io.ktor.client.plugins.ResponseException] 构造一致），
 * 并尽量解析 OpenAI 兼容 JSON 的 `error.message`。
 */
private fun responseExceptionDetailMessage(ex: ResponseException): String {
    val body = extractKtorCachedResponseText(ex.message)
    val parsed = parseOpenAiStyleErrorMessage(body)
    val trimmedBody = body.trim()
    val fallbackStatus = ex.response.status.description.trim().ifBlank { null }
        ?: "HTTP ${ex.response.status.value}"
    return parsed ?: trimmedBody.ifBlank { null } ?: fallbackStatus
}

private fun protocolErrorFromResponseException(ex: ResponseException): AiEngineError.Protocol =
    AiEngineError.Protocol(message = responseExceptionDetailMessage(ex), cause = ex)

/**
 * Ktor 把响应正文嵌进异常 `message` 的 `Text: "..."` 里；正文内的引号会转义为 `\"`。
 */
internal fun extractKtorCachedResponseText(exceptionMessage: String?): String {
    if (exceptionMessage.isNullOrEmpty()) return ""
    val prefix = "Text: \""
    val startIdx = exceptionMessage.indexOf(prefix)
    if (startIdx < 0) return ""
    var i = startIdx + prefix.length
    val sb = StringBuilder()
    while (i < exceptionMessage.length) {
        when (val c = exceptionMessage[i]) {
            '\\' -> {
                if (i + 1 >= exceptionMessage.length) {
                    sb.append(c)
                    i++
                } else {
                    sb.append(exceptionMessage[i + 1])
                    i += 2
                }
            }

            '"' -> return sb.toString()
            else -> {
                sb.append(c)
                i++
            }
        }
    }
    return sb.toString()
}

internal fun parseOpenAiStyleErrorMessage(responseBody: String): String? {
    val trimmed = responseBody.trim()
    if (trimmed.isEmpty()) return null
    return runCatching {
        val root = OpenAiJson.parseToJsonElement(trimmed).jsonObject
        root["error"]?.jsonObject?.get("message")?.jsonPrimitive?.contentOrNull
    }.getOrNull()
}
