package ciyin.ai.chat.openai.client

import ciyin.ai.chat.openai.OpenAiChatEngineConfig
import ciyin.ai.chat.openai.dto.ChatCompletionChunkDto
import ciyin.ai.chat.openai.dto.ChatCompletionResponseDto
import ciyin.ai.chat.openai.dto.ModelListResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.JsonObject

/**
 * 基于 Ktor 的 OpenAI 兼容协议客户端。
 */
internal class OpenAiChatClient(
    private val config: OpenAiChatEngineConfig,
    private val httpClient: HttpClient = createDefaultHttpClient(config, OpenAiJson),
) {

    /**
     * 发起非流式聊天请求。
     */
    suspend fun completeChat(body: JsonObject): ChatCompletionResponseDto =
        httpClient.post {
            url("${config.baseUrl.trimEnd('/')}/chat/completions")
            applyHeaders(stream = false)
            setBody(body)
        }.body()

    /**
     * 发起流式聊天请求。
     */
    suspend fun streamChat(body: JsonObject): Flow<ChatCompletionChunkDto> {
        val response = httpClient.post {
            url("${config.baseUrl.trimEnd('/')}/chat/completions")
            applyHeaders(stream = true)
            setBody(body)
        }
        if (!response.status.isSuccess()) {
            throw io.ktor.client.plugins.ResponseException(response, "SSE chat request failed")
        }
        return response.bodyAsChannel()
            .readSseDataFrames()
            .mapNotNull { frame ->
                if (frame == "[DONE]") {
                    null
                } else {
                    OpenAiJson.decodeFromString(ChatCompletionChunkDto.serializer(), frame)
                }
            }
    }

    /**
     * 列出可用模型。
     */
    suspend fun listModels(): ModelListResponseDto = httpClient.get {
        url("${config.baseUrl.trimEnd('/')}/models")
        applyHeaders(stream = false)
    }.body()

    /**
     * 统一应用鉴权与自定义请求头。
     */
    private fun io.ktor.client.request.HttpRequestBuilder.applyHeaders(stream: Boolean) {
        contentType(ContentType.Application.Json)
        accept(if (stream) ContentType.Text.EventStream else ContentType.Application.Json)
        config.apiKey?.takeIf { it.isNotBlank() }
            ?.let { header(HttpHeaders.Authorization, "Bearer $it") }
        config.organization?.takeIf { it.isNotBlank() }?.let { header("OpenAI-Organization", it) }
        config.customHeaders.forEach { (key, value) -> header(key, value) }
    }
}
