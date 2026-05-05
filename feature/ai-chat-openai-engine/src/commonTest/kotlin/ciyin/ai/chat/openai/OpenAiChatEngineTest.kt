package ciyin.ai.chat.openai

import ciyin.ai.chat.openai.client.OpenAiChatClient
import ciyin.ai.chat.openai.client.OpenAiJson
import ciyin.ai.core.chat.ChatEvent
import ciyin.ai.core.chat.ChatMessage
import ciyin.ai.core.chat.ChatRequest
import ciyin.ai.core.engine.EngineId
import ciyin.ai.core.engine.EngineRuntime
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * [OpenAiChatEngine] 的单元测试。
 */
class OpenAiChatEngineTest {

    /**
     * SSE 流式响应应被解析为 `Started -> Delta* -> Completed`，并正确聚合最终文本。
     */
    @Test
    fun `stream 应解析 SSE 并聚合最终回复`() = runBlocking {
        val engine = engine(
            baseUrl = "http://localhost:11434/v1",
            mockEngine = MockEngine { request ->
                assertEquals(HttpMethod.Post, request.method)
                assertEquals("/v1/chat/completions", request.url.encodedPath)
                respond(
                    content = """
                        data: {"choices":[{"delta":{"content":"Hello "}}]}

                        data: {"choices":[{"delta":{"content":"world"},"finish_reason":"stop"}],"usage":{"prompt_tokens":1,"completion_tokens":2,"total_tokens":3}}

                        data: [DONE]

                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Text.EventStream.toString()
                    ),
                )
            },
        )

        val events = engine.stream(
            ChatRequest(
                messages = listOf(ChatMessage.User("hi")),
            ),
        ).toList()

        assertEquals(4, events.size)
        assertIs<ChatEvent.Started>(events[0])
        assertEquals("Hello ", (events[1] as ChatEvent.Delta).text)
        assertEquals("world", (events[2] as ChatEvent.Delta).text)
        val completed = assertIs<ChatEvent.Completed>(events[3])
        assertEquals("Hello world", completed.response.content)
        assertEquals(3, completed.response.usage?.totalTokens)
        assertEquals(EngineRuntime.RemoteSelfHosted, engine.runtime)
    }

    /**
     * 非流式响应应直接产出 `Started -> Completed`。
     */
    @Test
    fun `stream 非流式时应走 completeChat`() = runBlocking {
        val engine = engine(
            baseUrl = "https://api.openai.com/v1",
            mockEngine = MockEngine { request ->
                assertEquals("/v1/chat/completions", request.url.encodedPath)
                respond(
                    content = """
                        {
                          "choices":[
                            {
                              "index":0,
                              "message":{"role":"assistant","content":"done"},
                              "finish_reason":"stop"
                            }
                          ],
                          "usage":{"prompt_tokens":2,"completion_tokens":1,"total_tokens":3}
                        }
                    """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    ),
                )
            },
        )

        val events = engine.stream(
            ChatRequest(
                messages = listOf(ChatMessage.User("hi")),
                options = ciyin.ai.core.chat.ChatOptions(stream = false),
            ),
        ).toList()

        assertEquals(2, events.size)
        assertIs<ChatEvent.Started>(events[0])
        assertEquals("done", (events[1] as ChatEvent.Completed).response.content)
        assertEquals(EngineRuntime.RemoteCloud, engine.runtime)
    }

    /**
     * `/models` 返回值应映射为通用模型列表。
     */
    @Test
    fun `models 应返回通用 ChatModelInfo`() = runBlocking {
        val engine = engine(
            baseUrl = "https://api.openai.com/v1",
            mockEngine = MockEngine { request ->
                assertEquals(HttpMethod.Get, request.method)
                assertEquals("/v1/models", request.url.encodedPath)
                respond(
                    content = """{"data":[{"id":"gpt-4o-mini"},{"id":"deepseek-chat"}]}""",
                    status = HttpStatusCode.OK,
                    headers = headersOf(
                        HttpHeaders.ContentType,
                        ContentType.Application.Json.toString()
                    ),
                )
            },
        )

        val models = engine.models()

        assertEquals(listOf("gpt-4o-mini", "deepseek-chat"), models.map { it.model })
        assertEquals(EngineId("openai:test"), models.first().engineId)
    }

    /**
     * 构造一个绑定 `MockEngine` 的测试引擎。
     */
    private fun engine(
        baseUrl: String,
        mockEngine: MockEngine,
    ): OpenAiChatEngine {
        val config = OpenAiChatEngineConfig(
            id = EngineId("openai:test"),
            baseUrl = baseUrl,
            apiKey = "test-key",
            defaultModel = "gpt-4o-mini",
        )
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(OpenAiJson)
            }
        }
        return OpenAiChatEngine(
            config = config,
            client = OpenAiChatClient(config, httpClient),
        )
    }
}
