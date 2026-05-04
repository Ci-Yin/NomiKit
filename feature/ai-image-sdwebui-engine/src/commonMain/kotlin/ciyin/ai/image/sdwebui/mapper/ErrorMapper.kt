package ciyin.ai.image.sdwebui.mapper

import ciyin.ai.core.error.AiEngineError
import ciyin.sdwebui.client.Client
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * 把 `feature/sdwebui` 层抛出的失败统一映射为 [AiEngineError]。
 *
 * 映射原则：
 * - 协程取消必须原样上抛，不能吞；
 * - IO 异常视为网络错误；
 * - SD WebUI 返回的非成功响应体统一落到协议错误；
 * - 参数校验失败落到不支持错误；
 * - 其余未知异常统一收口到 [AiEngineError.Unknown]。
 */
internal fun Throwable.toAiEngineError(): AiEngineError = when (this) {
    is CancellationException -> throw this
    is AiEngineErrorException -> error
    is IOException -> AiEngineError.Network(cause = this, message = message)
    is Client.Error -> AiEngineError.Protocol(message = body, cause = this)
    is IllegalArgumentException -> AiEngineError.Unsupported(message = message ?: "请求参数不合法")
    else -> AiEngineError.Unknown(cause = this, message = message)
}

/**
 * 在 mapper 内部需要以异常形式短路时使用的轻量包装。
 */
internal class AiEngineErrorException(
    val error: AiEngineError,
) : RuntimeException(error.toString())
