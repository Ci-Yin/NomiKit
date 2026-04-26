---
name: ai-facade
description: Use the feature/ai-facade Kotlin Multiplatform module (package ciyin.ai.facade) as 业务侧调用 AI 能力的**唯一统一入口**。Covers AiChat / AiImage 接口与 DefaultAiChat / DefaultAiImage 默认实现、ChatModelSpec / ImageModelSpec / EnginePreferences / FallbackPolicy 选择与降级策略、AiInvocationListener / InvocationMetadata 观测埋点、internal Fallbacks（EngineAttempt + collectWithFallback + buildAttempts）调度核心，以及单测使用的 RecordingChatEngine / RecordingImageEngine / FakeEnginePreferences 模式。Use when 用户提到 ai-facade、AiChat、AiImage、DefaultAiChat、DefaultAiImage、ChatModelSpec、ImageModelSpec、EnginePreferences、FallbackPolicy、AiInvocationListener、InvocationMetadata，需要在业务侧（app:shared/data 或 domain）调用 AI 能力，需要配置默认模型 / 降级策略 / 埋点，或调整 fallback 调度逻辑。
---

# feature/ai-facade 模块协作指南

`feature/ai-facade` 是 NomiKit AI 能力的**业务统一入口**。所有 Kotlin 源码包名为 `ciyin.ai.facade.*`
，仅 `commonMain` + `commonTest`（无平台源集——平台细节由各引擎模块解决）。

它是上层（`app:shared/data` Repository、UseCase、跨模块工具）唯一应该 import
的入口；具体走哪个引擎、用哪个模型、是否降级、要不要重试、怎么打日志埋点——**全部由本模块内部决定**。

铁律：**业务代码只 import `ciyin.ai.facade.*` 与 `ciyin.ai.core.*`；绝对不直接 import
任何 `ciyin.ai.image.sdwebui.*` / `ciyin.ai.chat.openai.*` / 其他 `ai-xxx-engine` 类。**

## 触发场景

任意一项命中即按本 skill 处理：

- 用户提到 `ai-facade`、`AiChat`、`AiImage`、`DefaultAiChat`、`DefaultAiImage`
- 用户提到 `ChatModelSpec`、`ImageModelSpec`、`EnginePreferences`、`FallbackPolicy`
- 用户提到 `AiInvocationListener`、`InvocationMetadata`、调用埋点 / 日志 / 计费 / Sentry 上报
- 用户希望在业务侧（`app:shared/data`、`app:shared/domain`、Repository、UseCase）调用 AI 能力
- 用户希望配置默认模型、引擎降级链路、单引擎重试次数、Retry-After 行为
- 用户希望写 `KermitAiInvocationListener` / `AppEnginePreferences` 类的应用层胶水
- 用户调整 fallback 调度逻辑（`internal/Fallbacks.kt`、`InvocationIds`、`Listeners`）
- 用户排查本模块的构建 / 单测错误

不命中则忽略本 skill。

## 模块速览

```
feature/ai-facade/
├── build.gradle.kts                       # multiplatform-lib-targets，仅依赖 :feature:ai-core + coroutines
└── src/
    ├── commonMain/kotlin/ciyin/ai/facade/
    │   ├── AiChat.kt                      # 公共接口：stream(...) / listAvailableModels()
    │   ├── AiImage.kt                     # 公共接口：generate(...) / listAvailableModels()
    │   ├── DefaultAiChat.kt               # 默认实现：spec 解析 + 主引擎选择 + 降级 + 观测
    │   ├── DefaultAiImage.kt              # 与 DefaultAiChat 完全对称
    │   ├── selection/
    │   │   ├── ChatModelSpec.kt           # sealed: Default / Explicit(engineId, model?) / ByCapability(required)
    │   │   ├── ImageModelSpec.kt          # 与 ChatModelSpec 形态对称
    │   │   ├── EnginePreferences.kt       # 业务侧实现 4 个 suspend 方法注入偏好
    │   │   └── FallbackPolicy.kt          # maxRetries / backupEngines / triggerOn
    │   ├── observability/
    │   │   ├── AiInvocationListener.kt    # onStart / onCompleted / onFailed
    │   │   └── InvocationMetadata.kt      # invocationId / capability / engineId / model / attempt
    │   └── internal/
    │       ├── Fallbacks.kt               # EngineAttempt + collectWithFallback + buildAttempts（共用调度核心）
    │       ├── InvocationIds.kt           # 进程内单调递增 id 生成器（启动随机前缀 + 计数）
    │       └── Listeners.kt               # ListenerDispatcher（吞异常的安全分发）
    └── commonTest/kotlin/ciyin/ai/facade/
        ├── DefaultAiChatTest.kt           # ByCapability / fallback / Refused 不降级 / 重试 listener 顺序 / listAvailableModels 去重
        ├── DefaultAiImageTest.kt          # 与上对称
        └── support/TestDoubles.kt         # RecordingChatEngine / RecordingImageEngine / FakeEnginePreferences / RecordingListener
```

依赖：仅 `api(:feature:ai-core)` + `kotlinx-coroutines-core`。**没有**任何引擎实现的依赖——这是 facade
不耦合具体厂商的关键。

## 核心 API（业务侧使用顺序）

### 步骤 1：在 `app:shared` 装配点构造 Facade

```kotlin
// app:shared/.../data/ai/AiModule.kt
val openai: ChatEngine = OpenAiChatEngine(...)
val ollama: ChatEngine = OpenAiChatEngine(...)
val sdwebui: ImageEngine = SdWebUiImageEngine(...)

val selector = EngineSelector(
    chatRegistry = DefaultChatEngineRegistry(listOf(openai, ollama)),
    imageRegistry = DefaultImageEngineRegistry(listOf(sdwebui)),
)

val aiChat: AiChat = DefaultAiChat(
    selector = selector,
    preferences = AppEnginePreferences(),                      // 业务侧实现
    listeners = listOf(KermitAiInvocationListener()),          // 0..N 个
)

val aiImage: AiImage = DefaultAiImage(
    selector = selector,
    preferences = AppEnginePreferences(),
    listeners = listOf(KermitAiInvocationListener()),
)
```

> 装配点（`app:shared`）才允许 import `OpenAiChatEngine` / `SdWebUiImageEngine`；业务代码（Repository /
> UseCase）**只 import `AiChat` / `AiImage`**。

### 步骤 2：业务侧调用

```kotlin
class ChatRepositoryImpl(private val aiChat: AiChat) {
    fun stream(messages: List<ChatMessage>): Flow<ChatEvent> =
        aiChat.stream(ChatRequest(messages = messages))                           // 用偏好默认模型

    fun streamWith(spec: ChatModelSpec, messages: List<ChatMessage>): Flow<ChatEvent> =
        aiChat.stream(spec, ChatRequest(messages = messages))                     // 用户在 UI 上选了某个模型

    suspend fun availableModels(): Result<List<ChatModelInfo>> =
        aiChat.listAvailableModels()                                              // 失败降级到部分成功
}
```

### 步骤 3：业务侧实现 `EnginePreferences`

```kotlin
class AppEnginePreferences : EnginePreferences {
    override suspend fun defaultChatSpec() = ChatModelSpec.Explicit(
        engineId = EngineId("openai-compatible:local-ollama"),
        model = "llama3.1",
    )
    override suspend fun defaultImageSpec() = ImageModelSpec.Explicit(
        engineId = EngineId("sdwebui:local-7860"),
        model = null,
    )
    override suspend fun chatFallback() = FallbackPolicy(
        maxRetries = 1,
        backupEngines = listOf(EngineId("openai:default")),
        // triggerOn 用默认（Network / RateLimited / Unknown）
    )
    override suspend fun imageFallback() = FallbackPolicy()
}
```

> Facade **不**自行持久化偏好；业务侧 `AppEnginePreferences` 才负责读 DataStore / Room。

## ChatModelSpec / ImageModelSpec 三种语义

`ChatModelSpec` 与 `ImageModelSpec` 形态完全对称：

| Spec                         | 何时用                           | DefaultAiChat 解析行为                                                                                 |
|------------------------------|-------------------------------|----------------------------------------------------------------------------------------------------|
| `Default`                    | 刚启动 / 用户未做任何选择                | 调 `preferences.defaultChatSpec()`；若再次返回 `Default` 则等价于 `ByCapability(emptySet())`（防止递归）            |
| `Explicit(engineId, model?)` | 用户在 UI 上明确选了某模型               | `selector.selectChat(preferredId = engineId)` + 把 `model ?: request.model` 注入到 `ChatRequest.model` |
| `ByCapability(required)`     | 业务只关心能力（"找一个能 ToolCalling 的"） | `selector.selectChat(required = required)` + 沿用 `request.model`                                    |

`DefaultAiChat.primaryCapability(request)` 自动推断本次调用的主能力（用于 metadata）：

- 有 `tools` → `ToolCalling`
- 有任意 `User.attachments` 或请求级 `attachments` → `VisionInput`
- 否则 → `Streaming`

`DefaultAiImage.primaryCapability(request)` 类似：按 `source` + `postProcessors` + `controls` 推断
`TextToImage` / `ImageToImage` / `Inpainting` / `ControlNet` / `BackgroundRemoval` / `Upscale` /
`FaceSwap` / `FaceDetailer`。

## FallbackPolicy 行为合同

```kotlin
data class FallbackPolicy(
    val maxRetries: Int = 1,                                     // 单引擎内重试次数（不含首次）
    val backupEngines: List<EngineId> = emptyList(),             // 主引擎失败后按顺序尝试的备用引擎
    val triggerOn: Set<KClass<out AiEngineError>> = setOf(
        // 哪些错误才触发"切下一个"
        AiEngineError.Network::class,
        AiEngineError.RateLimited::class,
        AiEngineError.Unknown::class,
    ),
)
```

调度规则（`internal/Fallbacks.kt::collectWithFallback` 实现）：

1. 按 `attempts` 顺序逐个尝试；首个永远是"主引擎"。
2. 单个引擎内最多 `1 + maxRetries` 次调用（含首次）。
3. **同一引擎内**的失败若不命中 `triggerOn`，立即停止重试并跳出（**不再切下一个**）——这就是为什么
   `Refused` / `Cancelled` 默认不触发降级。
4. 单引擎彻底失败后，若错误命中 `triggerOn`，才切到下一个引擎。
5. 全部失败时，**透传最后一次的失败事件**给下游（已经被下游收到的 `Started` 等事件不撤销）。
6. 任何一次成功（命中 `isCompleted`）后立即返回，不再尝试后续引擎。
7. 备用引擎在 Registry 中找不到（用户配置漂移）→ **直接跳过**，不报错。
8. `backupEngines` 中包含主引擎 ID → **自动去重**。

## AiInvocationListener 观测合同

```kotlin
interface AiInvocationListener {
    fun onStart(metadata: InvocationMetadata)
    fun onCompleted(metadata: InvocationMetadata, durationMs: Long)
    fun onFailed(metadata: InvocationMetadata, error: AiEngineError)
}
```

实现约束：

- **不应抛异常**：`Fallbacks.kt::notify(...)` 会**吞掉**所有非 `CancellationException`
  的异常——业务故障不该被一个失败的埋点上报打翻。
- **不应阻塞 / 不应 IO**：默认在调用线程上同步触发；要 IO 自己切线程。
- 同一次尝试的三个回调严格按 `onStart → (onCompleted | onFailed)` 顺序触发。
- `metadata.invocationId` 在主引擎 + 重试 + 降级期间**保持不变**；用 `metadata.attempt`（从 1 开始）区分。

典型实现：日志（`KermitAiInvocationListener`）、Sentry / Crashlytics 上报、计费、业务埋点。

## listAvailableModels 行为

`DefaultAiChat.listAvailableModels()` / `DefaultAiImage.listAvailableModels()`：

- 按 `selector.allChat()` / `allImage()` 注册顺序聚合。
- 用 `model.lowercase()` 作为 key 去重，**保留第一次出现**（注册顺序优先）。
- 单家失败不影响整体：用 `LinkedHashMap` 累计成功，只要有任何成功结果就返回 `Result.success(...)`。
- 全部失败才返回 `Result.failure(<最后一次异常>)`。

## 工作流

### 工作流：在业务侧新增对 AI 的调用

按"业务只与 Facade 交互"原则：

1. 在 `app:shared/data/ai/AiModule.kt`（或装配点）注入 `AiChat` / `AiImage`。
2. 在 Repository / UseCase 持有该接口；构造时**禁止** import 任何 `ai-xxx-engine`。
3. 用 `aiChat.stream(request)` 走偏好默认；用户切换模型时用
   `aiChat.stream(ChatModelSpec.Explicit(...), request)`。
4. 错误转译：在 Repository 把 `ChatEvent.Failed.error: AiEngineError` 映射为业务 `DataError`，再由
   domain 翻译为场景错误（参考 `.docs/contributing/layered.md`）。

### 工作流：调整 fallback 调度逻辑

`internal/Fallbacks.kt` 是 Chat/Image 共用的调度核心。修改时遵守：

1. **不要**把 ChatEngine / ImageEngine 类型硬编码进文件——它通过 `engineIdOf` / `errorOf` /
   `isCompleted` 三个 lambda 解耦，新增能力族（如未来 `EmbeddingEngine`）应该能复用同一份调度。
2. 新增策略字段（如"是否在 5xx 也降级"）：先在 `FallbackPolicy` 加字段并保留默认值，再在
   `collectWithFallback` 用。**禁止**让现有调用方破坏。
3. `notify { ... }` 的异常吞掉是**有意的**：观察者副作用不应阻断业务，**禁止**改成 throw。
4. `CancellationException` 在所有循环里都必须 `throw ce` 原样上抛，**禁止**捕获。
5. 任何改动必须配套补 `DefaultAiChatTest` / `DefaultAiImageTest` 用例覆盖新场景。

### 工作流：写 `EnginePreferences` 实现

1. 用 `suspend` 而非 `flow`：偏好读取一次性即可，不需要持续订阅；要"换模型立即生效"用 `ChatModelSpec`
   显式传入。
2. `defaultChatSpec()` 返回 `Default` 等价于"兜底 ByCapability(emptySet())"；想真正"任意一个" 引擎就直接返回
   `Default`。
3. `chatFallback()` / `imageFallback()` 在每次 `stream(...)` 调用时都会被读一次：可以基于网络环境 /
   用户付费状态动态返回不同策略。
4. 业务侧的 DataStore 读取放这里，**禁止**让 Facade 自己持有 DataStore。

### 工作流：写 `AiInvocationListener` 实现

参考 `app/shared/.../data/ai/KermitAiInvocationListener.kt`：

```kotlin
class KermitAiInvocationListener : AiInvocationListener {
    override fun onStart(metadata: InvocationMetadata) =
        Log.info(
            "AiInvocation",
            "start id=${metadata.invocationId} engine=${metadata.engineId.value} attempt=${metadata.attempt}"
        )
    override fun onCompleted(metadata: InvocationMetadata, durationMs: Long) =
        Log.info("AiInvocation", "completed id=${metadata.invocationId} durationMs=$durationMs")
    override fun onFailed(metadata: InvocationMetadata, error: AiEngineError) =
        Log.error(
            "AiInvocation",
            "failed id=${metadata.invocationId} error=${error::class.simpleName}"
        )
}
```

要点：

- 不抛异常、不调 IO（参考上面"观测合同"）。
- `invocationId` 是关键关联键——把它写入日志，跨重试 / 跨备用引擎都能拼回同一次业务调用。
- 想做"主引擎失败 → 通知用户已切到备用"的 UI：在 `onStart` 里检查 `metadata.attempt > 1`。

### 工作流：写单元测试

固定模式（详见 `DefaultAiChatTest.kt` / `DefaultAiImageTest.kt`）：

1. 用 `commonTest/support/TestDoubles.kt` 里的：
    - `RecordingChatEngine` / `RecordingImageEngine`：可编排 `plannedEvents`，记录 `receivedRequests`
      ；实现了所有 `ChatEngine` / `ImageEngine` 接口方法。
    - `FakeEnginePreferences`：构造时注入 `chatSpec` / `imageSpec` / `chatFallbackPolicy` /
      `imageFallbackPolicy`。
    - `RecordingListener`：把回调按 `start:<id>:<attempt>` / `completed:...` /
      `failed:...:<errorClass>` 字符串记录到 `records` 列表，方便断言时序。
2. 用 `kotlinx.coroutines.runBlocking + Flow.toList()` 收集 `aiChat.stream(...).toList()`。
3. 用 `selector(chats = listOf(...))` 构造 selector（测试里有同名 helper）。
4. 验证命令：

```bash
./gradlew :feature:ai-facade:desktopTest :feature:ai-facade:iosSimulatorArm64Test
```

> 已知问题：`testAndroidHostTest` 因工程级 JVM target 21 / daemon JDK 17 不一致而失败，**这是工程级问题
**，不要为它加 hack。

## 硬性约束

- 任何新增 `class` / `interface` / `object` / 公开/扩展函数必须补**中文 KDoc**。
- **本模块只能依赖 `:feature:ai-core` + coroutines**；**禁止**反向依赖任何 `ai-xxx-engine`、`app:*`、
  `business:*`。这是 facade 与厂商解耦的关键。
- **禁止**引入 DI 框架（Koin 等）。`DefaultAiChat` / `DefaultAiImage` 通过普通构造函数装配。
- **禁止**自行持久化用户偏好（DataStore / Room）；偏好通过 `EnginePreferences` 由业务侧注入。
- **禁止**自行实现错误翻译为业务文案；翻译归 `app:shared/data` Repository 与 domain UseCase。
- **禁止**让 `AiInvocationListener` 阻断主流程：`Listeners.kt` / `Fallbacks.kt` 已经在 `runCatching`
  里吞掉了，不要回滚这层防护。
- **禁止**吞 `CancellationException`：所有调度逻辑里必须 `throw ce` 原样上抛。
- **禁止**让 fallback 调度自动重试 `Refused` / `Cancelled`：拒绝是引擎/模型的明确信号，重试只是浪费配额。
- 依赖版本一律走 `libs.versions.toml`。

## 与上下游模块的边界

| 模块                                              | 与本模块的关系                                                                | 注意                                                    |
|-------------------------------------------------|------------------------------------------------------------------------|-------------------------------------------------------|
| `feature/ai-core`                               | 提供 `ChatEngine` / `ImageEngine` 接口、Spec 转换的目标类型、`AiEngineError` 等      | `api` 依赖，可暴露在公共签名                                     |
| `ai-xxx-engine`（OpenAI / SD WebUI / 未来其他）       | **不**直接依赖；引擎实例通过 `EngineSelector` 构造时由调用方注入                            | 编译期完全解耦，新增引擎不需要改 facade                               |
| `app:shared`（业务层）                               | 装配 `DefaultAiChat` / `DefaultAiImage`，业务代码 import `AiChat` / `AiImage` | 业务代码**禁止**绕开 Facade 直接持有 `ChatEngine` / `ImageEngine` |
| `app:shared/data/ai/AppEnginePreferences`       | 提供 `EnginePreferences` 实现，读 DataStore                                  | 业务侧负责，不进 facade                                       |
| `app:shared/data/ai/KermitAiInvocationListener` | 提供 `AiInvocationListener` 实现，落 Kermit 日志                               | 同上                                                    |

## 附加资源

- `ai-core` 抽象：`.agents/skills/ai-core/SKILL.md`
- 引擎实现：`.agents/skills/ai-image-sdwebui-engine/SKILL.md`、
  `.agents/skills/ai-chat-openai-engine/SKILL.md`
  上层入口（Facade）"**）
- 应用层 Facade 装配示例：`app/shared/src/commonMain/kotlin/com/ciyin/app/data/ai/`
- 分层错误流转：[`.docs/contributing/layered.md`](../../../.docs/contributing/layered.md)
