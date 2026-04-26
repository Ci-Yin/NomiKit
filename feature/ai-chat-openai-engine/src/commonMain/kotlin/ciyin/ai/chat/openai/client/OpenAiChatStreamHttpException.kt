package ciyin.ai.chat.openai.client

import io.ktor.http.HttpStatusCode

/**
 * 流式请求在收到非成功 HTTP 状态后抛出，携带已读取的响应正文，供 [ciyin.ai.chat.openai.mapper.toAiEngineError] 映射为 [ciyin.ai.core.error.AiEngineError]。
 *
 * 不依赖 [io.ktor.client.plugins.ResponseException] 的 `message` 内嵌正文格式（JSON 内含引号时不可靠）。
 */
internal class OpenAiChatStreamHttpException(
    val status: HttpStatusCode,
    val bodyText: String,
) : Exception("OpenAI chat stream HTTP ${status.value}: ${bodyText.take(200)}")
