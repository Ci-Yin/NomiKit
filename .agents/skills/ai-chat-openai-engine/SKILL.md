---
name: ai-chat-openai-engine
description: Use the feature/ai-chat-openai-engine Kotlin Multiplatform module (package ciyin.ai.chat.openai) to expose any OpenAI 兼容 `/v1/chat/completions` 端点（OpenAI、OpenRouter、DeepSeek、Together、vLLM、Ollama 等）as an ai-core ChatEngine。Covers OpenAiChatEngine 与 OpenAiChatEngineConfig 的构造、OpenAiChatClient（基于 Ktor）、SSE 流（SseStream.readSseDataFrames）、ChatRequestToOpenAiMapper / OpenAiToChatEventMapper / ErrorMapper 三个映射器、expect/actual HTTP 引擎工厂（OkHttp / CIO / Darwin），以及测试用 ktor MockEngine 模式。Use when 用户提到 ai-chat-openai-engine、OpenAiChatEngine、OpenAiChatEngineConfig、需要接 OpenAI 兼容协议（包括本地 Ollama / vLLM / OpenRouter / DeepSeek）、给 ChatEngine 添加新 vendorOptions、调整 SSE 解析或 mapper，或排查该模块的构建/测试错误。
---

# feature/ai-chat-openai-engine 模块协作指南

`feature/ai-chat-openai-engine` 是基于 Ktor 的 **OpenAI 兼容协议** `ChatEngine` 实现。所有 Kotlin
源码包名为 `ciyin.ai.chat.openai.*`，平台覆盖 `commonMain` + `androidMain` / `desktopMain` /
`iosMain`（HTTP 引擎按平台 actual 化）。

它的角色：把 `ai-core.ChatRequest` 映射成 `/v1/chat/completions` 请求体，处理流式（SSE）与非流式两种返回方式，把响应折叠为
`ai-core.ChatEvent`。覆盖任何"声称兼容 OpenAI"的端点：OpenAI
官方、OpenRouter、DeepSeek、Together、vLLM、Ollama 等。**不**做降级、观测、业务策略——那是
`feature/ai-facade` 的事。

## 触发场景

任意一项命中即按本 skill 处理：

- 用户提到 `ai-chat-openai-engine`、`OpenAiChatEngine`、`OpenAiChatEngineConfig`、`OpenAiChatClient`
- 用户希望接入 OpenAI 兼容协议端点（OpenAI / OpenRouter / DeepSeek / Together / vLLM / Ollama
  等本地或云端服务）
- 用户希望复用本模块给 `ai-core` 适配新的 OpenAI 兼容字段，或调整流式 / 非流式聚合逻辑
- 用户在写 SSE 解析（`SseStream.readSseDataFrames`）相关代码
- 用户给 `ChatEngine` 适配新的 `vendorOptions` 或 `ChatAttachment` 类型
- 用户排查本模块的构建 / 单测 / 平台引擎错误

不命中则忽略本 skill。

> 接 Anthropic（非 OpenAI 兼容协议）/ 接本地嵌入式模型，应另起 `ai-chat-anthropic-engine` /
`ai-chat-llamacpp-engine` 等模块；本 skill 只覆盖"OpenAI 协议"那一族。

## 模块速览

```
feature/ai-chat-openai-engine/
├── build.gradle.kts                       # multiplatform-lib-targets + kotlin.serialization
└── src/
    ├── commonMain/kotlin/ciyin/ai/chat/openai/
    │   ├── OpenAiChatEngine.kt            # ChatEngine 实现（流式 / 非流式 / model 路由）
    │   ├── OpenAiChatEngineConfig.kt      # baseUrl / apiKey / organization / defaultModel / 超时 / customHeaders
    │   ├── client/
    │   │   ├── OpenAiChatClient.kt        # Ktor 客户端：completeChat / streamChat / listModels
    │   │   ├── DefaultHttpClient.kt       # createDefaultHttpClient + expect defaultHttpClientEngineFactory + OpenAiJson
    │   │   └── SseStream.kt               # ByteReadChannel.readSseDataFrames(): Flow<String>
    │   ├── dto/                           # @Serializable 请求体 / 响应体 / chunk DTO
    │   └── mapper/
    │       ├── ChatRequestToOpenAiMapper.kt   # ChatRequest → JsonObject（含 vendorOptions 合并）
    │       ├── OpenAiToChatEventMapper.kt     # ChatResponseAccumulator：流式 chunk + 非流式 absorb
    │       └── ErrorMapper.kt                 # Throwable → AiEngineError（含 401/403/429 细分）
    ├── androidMain/.../client/DefaultHttpClient.android.kt   # actual: OkHttp
    ├── desktopMain/.../client/DefaultHttpClient.desktop.kt   # actual: CIO
    ├── iosMain/.../client/DefaultHttpClient.ios.kt           # actual: Darwin
    └── commonTest/kotlin/ciyin/ai/chat/openai/
        └── OpenAiChatEngineTest.kt        # 用 ktor MockEngine 验证 SSE / 非流式 / listModels
```

依赖：

- 公共：`api(:feature:ai-core)` + `kotlinx-coroutines-core` + `kotlinx-serialization-json` +
  `ktor-client-core / content-negotiation / serialization-kotlinx-json / logging`
- Android：`ktor-client-okhttp`
- Desktop：`ktor-client-cio`
- iOS：`ktor-client-darwin`
- 测试：`kotlin-test` + `ktor-client-mock`

所有版本走 `gradle/libs.versions.toml`。**不要**在 `build.gradle.kts` 里写裸版本号。

## 核心 API（外部使用顺序）

### 步骤 1：构造引擎实例

最简方式（推荐用于 `app:shared` 的 Koin module）：

```kotlin
import ciyin.ai.core.engine.EngineId
import ciyin.ai.chat.openai.OpenAiChatEngine
import ciyin.ai.chat.openai.OpenAiChatEngineConfig

// OpenAI 官方
val openai = OpenAiChatEngine(
    OpenAiChatEngineConfig(
        id = EngineId("openai:default"),
        baseUrl = "https://api.openai.com/v1",
        apiKey = BuildConfig.OPENAI_KEY,
        defaultModel = "gpt-4o-mini",
    ),
)

// OpenRouter
val router = OpenAiChatEngine(
    OpenAiChatEngineConfig(
        id = EngineId("openai-compatible:openrouter-prod"),
        baseUrl = "https://openrouter.ai/api/v1",
        apiKey = BuildConfig.OPENROUTER_KEY,
        customHeaders = mapOf("HTTP-Referer" to "https://nomikit.app"),
    ),
)

// 本地 Ollama / vLLM
val ollama = OpenAiChatEngine(
    OpenAiChatEngineConfig(
        id = EngineId("openai-compatible:local-ollama"),
        baseUrl = "http://localhost:11434/v1",
        defaultModel = "llama3.1",
    ),
)
```

> `runtime` 由 `baseUrl` 推断：host 是 `localhost` / `127.0.0.1` / `::1` → `RemoteSelfHosted`；其他 →
`RemoteCloud`。

### 步骤 2：注册到 Registry

```kotlin
val registry = DefaultChatEngineRegistry(listOf(openai, router, ollama))
```

业务侧**不直接**持有 `OpenAiChatEngine`：装配点把它丢给 `Registry` 与 `EngineSelector`，后续通过
`AiChat` 间接使用。

### 步骤 3：能力声明

`OpenAiChatEngine.capabilities` 静态声明：

```
Streaming / ToolCalling / VisionInput / JsonOutput / SystemPrompt
```

不包含 `PromptCaching`（OpenAI 协议本身没有暴露通用 cache_control 字段；上游有需要可走
`vendorOptions`）。

### 步骤 4：调用

```kotlin
engine.stream(
    ChatRequest(
        model = "gpt-4o-mini",                     // null 时用 config.defaultModel
        messages = listOf(
            ChatMessage.System("你是助手"),
            ChatMessage.User("hi", attachments = listOf(ChatAttachment.Image(pngBytes, "image/png"))),
        ),
        options = ChatOptions(temperature = 0.7f, stream = true),
        tools = listOf(ChatToolSpec(name = "get_weather", description = "...", parametersJsonSchema = schema)),
        vendorOptions = mapOf("response_format" to buildJsonObject { put("type", "json_object") }),
    ),
).collect { event -> /* Started / Delta / ToolCall / Completed / Failed */ }
```

## 关键映射规则速查

### 请求映射（`ChatRequestToOpenAiMapper.kt`）

| 通用层字段                                                           | OpenAI 协议落点                                                                                                   |
|-----------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------|
| `ChatRequest.model` `?: config.defaultModel`                    | `model`（都为 null 时抛 `AiEngineError.Unsupported`）                                                               |
| `ChatMessage.System`                                            | `{ role: "system", content: <text> }`                                                                         |
| `ChatMessage.User`（无附件）                                         | `{ role: "user", content: <text> }`                                                                           |
| `ChatMessage.User`（带 `ChatAttachment.Image`）                    | `content` 变成 `[{ type:"text",text:... }, { type:"image_url", image_url: { url: "data:<mime>;base64,..." } }]` |
| `ChatMessage.User`（带 `Document` / `Audio`）                      | **拒绝**：`AiEngineError.Unsupported`（OpenAI 兼容暂不支持）                                                             |
| `ChatMessage.Assistant`                                         | `{ role:"assistant", content?, tool_calls? }`，`content` 为空时省略；`toolCalls` 自动写入 `index`                        |
| `ChatMessage.Tool`                                              | `{ role:"tool", content, tool_call_id }`                                                                      |
| `ChatOptions.temperature/topP/maxOutputTokens/stop/seed/stream` | `temperature/top_p/max_tokens/stop/seed/stream`                                                               |
| `ChatRequest.tools` 非空                                          | 自动追加 `tool_choice = "auto"`                                                                                   |
| `ChatRequest.vendorOptions`                                     | 直接 `JsonObject + vendorOptions`，会**覆盖**前面字段；用 `openai.xxx` 等带前缀键避免冲突                                          |
| `ChatRequest.attachments`（请求级）                                  | `validate(...)` 阶段就会拒绝（OpenAI 兼容仅支持消息级附件）                                                                     |

> Mapper 内部短路失败一律 `throw OpenAiMappingException(AiEngineError.Xxx)`，**禁止**直接抛
`IllegalStateException`。

### 响应聚合（`OpenAiToChatEventMapper.kt`）

`ChatResponseAccumulator` 同时支持流式与非流式：

- **流式**：`append(chunk)` 逐 chunk 处理，并产出 `ChatEvent.Delta(text)` /
  `ChatEvent.ToolCall(id,name,arguments)`；`tool_calls` 按 `index` 缓冲合并，`id` / `name` 取首个非空，
  `arguments` 累积拼接。
- **非流式**：`absorb(response)` 直接吸收完整结果，覆盖 `content` / `toolCalls` / `usage` /
  `finishReason`。
- **build()**：返回最终 `ChatResponse`，由 `OpenAiChatEngine.stream` 包进 `ChatEvent.Completed`。

### 错误映射（`ErrorMapper.kt`）

```
CancellationException        → 原样上抛（永远不要吞）
OpenAiMappingException       → 解包内部 AiEngineError
IOException (kotlinx.io)     → AiEngineError.Network
SerializationException       → AiEngineError.Protocol
ClientRequestException 401/403 → AiEngineError.Unauthorized
ClientRequestException 429   → AiEngineError.RateLimited（解析 Retry-After 头转毫秒）
ClientRequestException 其他 4xx → AiEngineError.Protocol
ServerResponseException / ResponseException → AiEngineError.Protocol
其他                         → AiEngineError.Unknown
```

## SSE 解析（`SseStream.readSseDataFrames`）

只实现 OpenAI 兼容协议所需的最小语义：

- 累积同一事件内的多行 `data:`；
- 遇到空行产出一帧；
- 注释行 `:` 与未识别字段一律忽略；
- `[DONE]` 由 `OpenAiChatClient.streamChat` 在上层过滤掉。

新增其他协议字段（如 `event:` / `id:` / `retry:`）时，要在这里扩展，**禁止**用 `try-catch` 在调用方过滤异常包装。

## 平台 HTTP 引擎（`expect/actual`）

```kotlin
internal expect fun defaultHttpClientEngineFactory(): HttpClientEngineFactory<*>
```

| 平台            | actual   | 依赖                        |
|---------------|----------|---------------------------|
| `androidMain` | `OkHttp` | `libs.ktor.client.okhttp` |
| `desktopMain` | `CIO`    | `libs.ktor.client.cio`    |
| `iosMain`     | `Darwin` | `libs.ktor.client.darwin` |

新增平台（如 `wasmJsMain`）的唯一正确做法：

1. 在该平台源集补 `internal actual fun defaultHttpClientEngineFactory()`。
2. 在 `gradle/libs.versions.toml` 加对应 `ktor-client-xxx` 别名，并在 `build.gradle.kts` 对应平台
   `dependencies` 块引用。
3. **不要**在 `commonMain` 直接 import 任何平台引擎。

## 工作流

### 工作流：新增 `vendorOptions` 透传字段

业务侧需要 `response_format = json_object` / `seed` / `logit_bias` 等"协议有但 ai-core 没抽象"的字段时，
**禁止**改 `ai-core`：

1. 业务侧用 `kotlinx.serialization.json.buildJsonObject` 构造值，作为 `vendorOptions` 传入。
2. 键名建议带前缀（`openai.response_format`）；本 mapper 用 `JsonObject + vendorOptions` 直接合并到顶层，
   **会覆盖**通用字段——如果想覆盖，键名就不带前缀。
3. **不要**在 `OpenAiChatEngine` 里加专属字段；保持 mapper 的"透传"语义。

### 工作流：给 `ai-core` 新增 `ChatAttachment` 分支后补适配

1. 在 `ChatRequestToOpenAiMapper.toContentPart()` 的 `when (this)` 里加分支，要么映射成对应 OpenAI
   多模态结构，要么 `throw OpenAiMappingException(AiEngineError.Unsupported(...))`。**禁止**
   `else -> Unit` 静默忽略。
2. 在 `OpenAiChatEngine.validate()` 里更新对应校验规则。
3. 补单测（见下文模板）。

### 工作流：写单元测试

固定模式（详见 `OpenAiChatEngineTest.kt`）：

1. 用 `io.ktor.client.engine.mock.MockEngine` + `respond(...)` 模拟响应（与 `feature/sdwebui` 用
   `RecordingClient` 不同，本模块直接复用 ktor 官方 mock）。
2. 用 `io.ktor.client.engine.mock.MockEngine { request -> ... }` 在 lambda 里断言 `request.method` /
   `request.url.encodedPath`。
3. 用 `OpenAiChatEngine` 的 `internal` 主构造（`config + client`）注入定制 client：

```kotlin
private fun engine(baseUrl: String, mockEngine: MockEngine): OpenAiChatEngine {
    val config = OpenAiChatEngineConfig(EngineId("openai:test"), baseUrl, apiKey = "test-key", defaultModel = "gpt-4o-mini")
    val httpClient = HttpClient(mockEngine) {
        install(ContentNegotiation) { json(OpenAiJson) }
    }
    return OpenAiChatEngine(config, OpenAiChatClient(config, httpClient))
}
```

4. SSE 测试要在 `respond` 的 `headers` 里塞 `Content-Type: text/event-stream`，正文用多行字符串 +
   空行 + `data: [DONE]` 结尾。
5. 用 `kotlinx.coroutines.runBlocking + Flow.toList()` 收集事件并按 index 断言（
   `Started → Delta → Delta → Completed`）。
6. 验证命令：

```bash
./gradlew :feature:ai-chat-openai-engine:desktopTest ^
          :feature:ai-chat-openai-engine:iosSimulatorArm64Test
```

> 已知问题：`testAndroidHostTest` 因工程级 JVM target 21 / daemon JDK 17 不一致而失败，**这是工程级问题
**，不要为它加 hack。

### 工作流：调整 / 修复 SSE 解析

1. **不要**在 `OpenAiChatClient.streamChat` 里捕异常做"宽容解析"；解析失败应通过
   `SerializationException` 经 `ErrorMapper` 折叠到 `AiEngineError.Protocol`。
2. **不要**把 `[DONE]` 写进 DTO；统一在 `streamChat` 的 `mapNotNull` 里返回 `null`。
3. 新增协议级字段（如 OpenAI 新版的 `usage` 在 chunk 中按行返回），先在 `ChatCompletionChunkDto` 加
   `@Serializable` 字段，再在 `ChatResponseAccumulator.append()` 里聚合。

## 硬性约束

- 任何新增 `class` / `interface` / `object` / 公开/扩展函数必须补**中文 KDoc**。
- **本模块只能依赖 `:feature:ai-core` + Ktor**；**禁止**反向依赖 `:feature:ai-facade`、任何 `app:*`
  、任何其他 `ai-xxx-engine`。
- **禁止**引入 DI 框架（Koin / Dagger 等）。`OpenAiChatEngine` 通过普通构造函数装配。
- **禁止**直接 `throw RuntimeException`：错误必须走 `AiEngineError` 经 `Failed` 事件流回上层；mapper
  内部短路用 `OpenAiMappingException` 包装。
- **禁止**吞 `CancellationException`（`ErrorMapper` 第一行就是 `throw this`）。
- **禁止**在 `ChatEvent.Completed` 之后还发 `Failed`，反之亦然——契约由 `ai-core.ChatEvent` 定义。
- **平台特定代码只能放在 `androidMain` / `desktopMain` / `iosMain`**；`commonMain` 必须保持平台中性。
- **DTO 必须 `@Serializable`**，字段默认值要保留以防上游协议演进；`OpenAiJson` 已开
  `ignoreUnknownKeys = true` + `explicitNulls = false`。
- 依赖版本一律走 `libs.versions.toml`。

## 与上下游模块的边界

| 模块                  | 与本模块的关系                                                             | 注意                                                |
|---------------------|---------------------------------------------------------------------|---------------------------------------------------|
| `feature/ai-core`   | 提供 `ChatEngine` 接口、`ChatRequest`、`ChatEvent`、`AiEngineError`        | `api` 依赖，可暴露在公共签名                                 |
| Ktor                | 通过 `expect/actual` 各平台引擎 + `ContentNegotiation` + `Logging` 调用上游    | `implementation` 依赖，**不要**出现在公共签名                 |
| `feature/ai-facade` | 通过 `Registry` + `Selector` 间接调用本模块                                  | **绝不**反向依赖 facade                                 |
| `app:shared`        | 在装配点 `new OpenAiChatEngine(config)` 并丢给 `DefaultChatEngineRegistry` | 业务代码**不直接** import `OpenAiChatEngine`，只用 `AiChat` |

## 附加资源

- `ai-core` 抽象：`.agents/skills/ai-core/SKILL.md`
- 上层 Facade：`.agents/skills/ai-facade/SKILL.md`
- 顶层设计稿：[`AI_ENGINES_DESIGN.md`](../../../AI_ENGINES_DESIGN.md)（看第四节"
  `ai-chat-openai-engine` 引擎契约"）
