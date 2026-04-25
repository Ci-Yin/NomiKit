package ciyin.ai.chat.openai.mapper

import ciyin.ai.core.error.AiEngineError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

/**
 * 把 Ktor / mapper 层异常统一折叠为 [AiEngineError]。
 */
internal fun Throwable.toAiEngineError(): AiEngineError = when (this) {
    is kotlin.coroutines.cancellation.CancellationException -> throw this
    is OpenAiMappingException -> error
    is IOException -> AiEngineError.Network(cause = this, message = message)
    is SerializationException -> AiEngineError.Protocol(
        message = message ?: "响应解析失败",
        cause = this
    )

    is ClientRequestException -> mapResponseException()
    is ServerResponseException -> AiEngineError.Protocol(
        message = response.status.description,
        cause = this
    )

    is ResponseException -> AiEngineError.Protocol(
        message = response.status.description,
        cause = this
    )

    else -> AiEngineError.Unknown(cause = this, message = message)
}

/**
 * 把 4xx 响应映射成更具体的错误类型。
 */
private fun ClientRequestException.mapResponseException(): AiEngineError = when (response.status) {
    HttpStatusCode.Unauthorized,
    HttpStatusCode.Forbidden,
        -> AiEngineError.Unauthorized(providerMessage = response.status.description)

    HttpStatusCode.TooManyRequests -> AiEngineError.RateLimited(
        retryAfterMs = response.headers["Retry-After"]?.toLongOrNull()?.times(1000),
        providerMessage = response.status.description,
    )

    else -> AiEngineError.Protocol(message = response.status.description, cause = this)
}
