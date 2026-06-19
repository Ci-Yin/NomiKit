---
name: ai-core
description: Use the feature/ai-core Kotlin Multiplatform module (package ciyin.ai.core) as the厂商无关 AI 抽象层。Covers AiEngine / ChatEngine / ImageEngine 接口契约、ChatRequest / ChatEvent / ImageRequest / ImageEvent 通用模型、AiCapability / ChatCapability / ImageCapability 能力声明、AiEngineError 错误模型，以及 ChatEngineRegistry / ImageEngineRegistry / EngineSelector 治理组件。Use when 用户提到 ai-core、AiEngine、ChatEngine、ImageEngine、EngineId、EngineRuntime、AiCapability、ChatCapability、ImageCapability、ChatRequest、ChatEvent、ImageRequest、ImageEvent、AiEngineError、Registry、EngineSelector，需要新增/修改 AI 能力的通用抽象、新增厂商无关字段、调整通用错误分类，或在新增引擎实现 / Facade 实现时需要确认接口契约。
---

# feature/ai-core 模块协作指南

`feature/ai-core` 是 NomiKit AI 引擎抽象层的**根模块**。所有 Kotlin 源码包名为 `ciyin.ai.core.*`，目前只有
`commonMain` + `commonTest`，没有任何平台源集——刻意保持平台中性，确保任何 KMP 目标都能复用同一份抽象。

它只做四件事：**统一引擎抽象 / 统一请求响应模型 / 统一能力声明 / 统一注册与选择**。**不**
包含任何厂商命名（OpenAI / SD WebUI / Anthropic / Ollama 等），也**不**实现任何具体调用逻辑。

## 触发场景

任意一项命中即按本 skill 处理：

- 用户提到 `ai-core`、`AiEngine`、`ChatEngine`、`ImageEngine`、`EngineId`、`EngineRuntime`
- 用户提到 `AiCapability`、`ChatCapability`、`ImageCapability` 能力声明
- 用户提到 `ChatRequest` / `ChatResponse` / `ChatEvent` / `ChatMessage` / `ChatOptions` /
  `ChatToolSpec` / `ChatAttachment`
- 用户提到 `ImageRequest` / `ImageResult` / `ImageEvent` / `ImageSource` / `ImageSize` /
  `ImageControl` / `ImagePostProcessor` / `GeneratedImage`
- 用户提到 `AiEngineError` / `AiEngineException` / `UnsupportedCapabilityException`
- 用户提到 `ChatEngineRegistry` / `ImageEngineRegistry` / `EngineSelector`
- 用户要在 `ai-core` 内新增 capability、新增字段、新增错误分支
- 在新增 `ai-xxx-engine` 或调整 `ai-integrate` 时需要确认 `ai-core` 接口契约
- 修改 `feature/ai-core/build.gradle.kts` 或 `settings.gradle.kts` 中 `:feature:ai-core` 的依赖

第三节。

## 模块速览

```
feature/ai-core/
├── build.gradle.kts                      # multiplatform-lib-targets，仅 commonMain
└── src/
    ├── commonMain/kotlin/ciyin/ai/core/
    │   ├── engine/                        # AiEngine / ChatEngine / ImageEngine / EngineId / EngineRuntime
    │   ├── chat/                          # ChatRequest / ChatResponse / ChatEvent / ChatMessage / ChatOptions / ChatToolSpec / ChatAttachment / ChatModelInfo
    │   ├── image/                         # ImageRequest / ImageResult / ImageEvent / ImageSource / ImageSize / ImageControl / ImagePostProcessor / GeneratedImage / ImageModelInfo
    │   ├── capability/                    # AiCapability + ChatCapability + ImageCapability
    │   ├── registry/                      # ChatEngineRegistry / ImageEngineRegistry + Default 实现 + EngineSelector
    │   └── error/                         # AiEngineError / AiEngineException / UnsupportedCapabilityException
    └── commonTest/kotlin/ciyin/ai/core/
        ├── registry/                      # Default*RegistryTest / EngineSelectorTest
        └── support/                       # FakeChatEngine / FakeImageEngine
```

依赖：仅 `kotlinx-coroutines-core` + `kotlinx-serialization-json`，全部走 `gradle/libs.versions.toml`。
**不依赖**任何 HTTP 引擎、任何 vendor SDK、任何 DI 框架、任何 Ktor。`build.gradle.kts` 里两条 `api(...)`
是有意的——`Flow` 与 `JsonElement` 出现在公共签名上。

## 核心抽象速查

### 引擎层（`engine/`）

```kotlin
interface AiEngine {
    val id: EngineId                          // 全局唯一，建议格式 "<provider>:<instance>"
    val provider: String                      // "openai" / "sdwebui" / "ollama"，仅展示用
    val runtime: EngineRuntime                // RemoteCloud / RemoteSelfHosted / LocalEmbedded
    val capabilities: Set<AiCapability>       // 静态声明，不保证运行时一定可用
}

interface ChatEngine : AiEngine {
    fun stream(request: ChatRequest): Flow<ChatEvent>
    suspend fun listModels(): Result<List<ChatModelInfo>>
    suspend fun validate(request: ChatRequest): Result<Unit>
}

interface ImageEngine : AiEngine {
    fun generate(request: ImageRequest): Flow<ImageEvent>
    suspend fun listModels(): Result<List<ImageModelInfo>>
    suspend fun validate(request: ImageRequest): Result<Unit>
}
```

铁律：

- **绝不**搞"万能 AiEngine + 所有 capability 都往里塞"，能力按引擎子接口分。
- 任何引擎层错误都通过 `Flow` 的 `Failed` 事件传递，**禁止**直接 `throw RuntimeException`。
  `CancellationException` 例外，必须原样上抛。
- `EngineId` 是 `value class`，业务侧持久化偏好时存 `EngineId.value`。

### 通用模型（`chat/` + `image/`）

- `ChatRequest` / `ImageRequest` 公共字段只覆盖**主流厂商都稳定支持**的能力；厂商专有字段一律走
  `vendorOptions: Map<String, JsonElement>`，键名加厂商前缀（`openai.response_format` /
  `sdwebui.alwaysonScripts`）。
- `ChatEvent` / `ImageEvent` 是 `sealed interface`，必须满足三条契约：**首个为 Started**、**末尾为
  Completed 或 Failed 之一**、**Completed 与 Failed 同次调用不能同时出现**。
- `ChatMessage` / `ImageSource` / `ImageControl` / `ImagePostProcessor` 也都是 `sealed interface`
  ，新增分支会强制下游 `when` 适配——这是有意的，避免静默漏适配。
- 二进制（图像、音频、文档、ControlNet 控制图）一律用 `ByteArray` + `mimeType`，**不**依赖任何平台位图类型（
  `Bitmap` / `UIImage` / `BufferedImage` 都不行）。

### 能力声明（`capability/`）

```kotlin
sealed interface AiCapability
sealed interface ChatCapability : AiCapability {
    data object Streaming / ToolCalling / VisionInput / JsonOutput / SystemPrompt / PromptCaching
}
sealed interface ImageCapability : AiCapability {
    data object TextToImage / ImageToImage / Inpainting / ControlNet / IPAdapter /
    FaceDetailer / FaceSwap / BackgroundRemoval / Upscale
}
```

只追加、不删除；新增分支不要破坏 `Set<AiCapability>` 现有持久化值。

### 错误模型（`error/`）

`AiEngineError` 是 `sealed interface`，分支：`Network` / `Unauthorized` / `RateLimited` / `Protocol` /
`Refused` / `Unsupported` / `Cancelled` / `Unknown`。

- **不**直接依赖业务层 `DataError`：`feature` 不允许反向依赖业务，错误转译留给 `app:shared/data` 的
  Repository。
- **不**用 `Throwable` 子类作为传输模型——`AiEngineException` 仅在"必须抛"的边界场景用。
- `EngineSelector` 找不到引擎用 `UnsupportedCapabilityException`：这是装配/编排错误，应在开发期暴露，不走
  `Failed` 事件。

### 注册与选择（`registry/`）

```kotlin
class DefaultChatEngineRegistry(engines: List<ChatEngine>) :
    ChatEngineRegistry  // 重复 ID 直接 require 失败
class DefaultImageEngineRegistry(engines: List<ImageEngine>) : ImageEngineRegistry

class EngineSelector(chatRegistry, imageRegistry) {
    fun selectChat(
        preferredId: EngineId? = null,
        required: Set<ChatCapability> = emptySet()
    ): ChatEngine
    fun selectImage(
        preferredId: EngineId? = null,
        required: Set<ImageCapability> = emptySet()
    ): ImageEngine
}
```

选择策略（Chat / Image 完全同构）：**先看 preferredId** → **否则按 required 过滤后取注册顺序首个** → *
*找不到抛 UnsupportedCapabilityException**。`Registry` 不负责降级、重试、观测——这些归
上层聚合或业务模块。

## 工作流

### 工作流：新增一个引擎能力（capability）

按"从源头建模"原则，**禁止**在引擎实现里硬编码"这个引擎其实也支持 X 但 ai-core 没声明"。标准流程：

1. **判断归属**：聊天相关 → `ChatCapability`；生图相关 → `ImageCapability`；都不属于（如未来的 ASR /
   TTS / Embedding）→ 在 `capability/` 下新增 `XxxCapability : AiCapability` 接口。
2. **追加 `data object`**：在对应能力 sealed 接口下加 `data object Xxx : XxxCapability`，附上中文 KDoc
   说明何时算"具备该能力"。
3. **不要删旧能力**：所有 `ChatModelInfo.capabilities` / `ImageModelInfo.capabilities` 都可能被持久化，删
   `data object` 等于破坏向后兼容。
4. **跑测试 + 跨平台编译验证**：

```bash
./gradlew :feature:ai-core:check ^
          :feature:ai-core:compileKotlinDesktop ^
          :feature:ai-core:compileKotlinIosSimulatorArm64 ^
          :feature:ai-core:compileAndroidMain
```

### 工作流：新增 / 修改通用请求字段

1. **优先走 `vendorOptions`**：如果只有一两家厂商有的字段（OpenAI 的 `response_format`、SD WebUI 的
   `alwaysonScripts`），**绝不**进 `ChatRequest` / `ImageRequest` 公共字段；让对应 `ai-xxx-engine` 通过
   `vendorOptions[key]` 自己解释。
2. **真要进公共字段时**：必须满足"主流厂商都稳定支持"。新字段一律 `null` 默认值，避免破坏既有调用方。
3. **同步 mapper**：每个 `ai-xxx-engine` 的 `mapper/` 目录里的 `XxxRequestToYyyMapper` 都要补对应解释；
   **禁止**在 mapper 里悄悄 ignore 新字段。
4. **补单测**：在 `commonTest` 下补测试，用 `support/FakeChatEngine` 或 `FakeImageEngine`
   验证字段透传 / fallback 行为。

### 工作流：新增 `AiEngineError` 分支

1. **真有新错误类别才加**：先看现有 8 个分支能否覆盖。`Refused` 与 `Unsupported`、`Network` 与`Unknown`
   之间的边界要看清：
    - 引擎主动拒绝、内容过滤 → `Refused`（不要降级）
    - 参数 / capability / 模型不在线 → `Unsupported`（业务侧应改请求）
    - 网络 / IO / DNS / SSL → `Network`（fallback 默认会触发）
    - 其余兜底 → `Unknown`
2. 新分支记得在上层聚合的降级策略默认值里思考是否要加。
3. 通知所有 `ai-xxx-engine` 的 `ErrorMapper` 把对应异常映射到新分支。

### 工作流：写单元测试

固定模式：

1. 用 `commonTest/support/FakeChatEngine` 或 `FakeImageEngine`（`ai-core` 自带）作为可编排 fake，**不要
   **引入 Mockito / MockK。
2. 用 `kotlinx.coroutines.runBlocking` 包裹（`commonTest` 不强依赖 `runTest`）。
3. 验证 Registry / Selector 时直接断言 `selectChat(...)` 返回的是哪个引擎；找不到 capability 时断言抛
   `UnsupportedCapabilityException`。
4. 验证命令：

```bash
./gradlew :feature:ai-core:desktopTest :feature:ai-core:iosSimulatorArm64Test
```

> 已知问题：`testAndroidHostTest` 因工程级 `multiplatform-lib-targets` 把 Android `jvmTarget` 设为
> 21、而 Gradle daemon 跑在 JDK 17 而失败。**这是工程级问题**，不要为它打 hack；等工程统一升级 JDK 21
> 即可恢复。

## 硬性约束

- 任何新增 `class` / `interface` / `object` / 公开/扩展函数必须补**中文 KDoc**。
- **禁止**在 `ai-core` 出现 `OpenAi` / `SdWebUi` / `ControlNet`（作为厂商命名）/ `Anthropic` /`Ollama`
  等任何厂商命名（`ImageCapability.ControlNet` 是已抽象的通用能力命名，不算厂商命名，但新增此类命名要谨慎）。
- **禁止**依赖 `feature/sdwebui` / `ai-xxx-engine` / `ai-integrate` / 任何 `app:*` / `business:*`：
  `ai-core` 是依赖图的最底层。
- **禁止**依赖任何 DI 框架（Koin 等）。引擎实例的装配在 `app:shared` 完成。
- **禁止**用 `Throwable` / `Exception` 作为通用错误传输模型；只用 `AiEngineError` + `Result` +
  `Failed` 事件。
- 平台特定代码完全不存在于本模块：仅 `commonMain`，没有 `androidMain` / `desktopMain` / `iosMain`。
- 依赖版本一律走 `libs.versions.toml`，**不要**在 `build.gradle.kts` 里写裸版本号。
- `ChatEvent` / `ImageEvent` 流契约必须遵守：`Started` 开头 + `Completed`/`Failed` 结尾 + 二选一不并存。

## 与下游模块的边界

| 下游模块                              | 它能依赖 ai-core 的什么                                                                        | 它**不能**做什么                                                 |
|-----------------------------------|-----------------------------------------------------------------------------------------|------------------------------------------------------------|
| `ai-integrate`                    | 所有 public 抽象，尤其 `EngineSelector` / `Registry` / `ChatEvent` / `ImageEvent`              | 反向依赖任何 `app:*`                                             |
| `ai-xxx-engine`（聊天 / 生图 / 未来 ASR） | 实现对应 `ChatEngine` / `ImageEngine`；用 `AiEngineError` 折叠错误                                | 直接 import 其他 `ai-xxx-engine`；引入 DI 框架                      |
| `app:shared`                      | 可通过 `ai-integrate` 的 `api(projects.feature.aiCore)` 获得公共模型                             | 默认不要散落具体引擎实现类构造                                      |

## 附加资源

- 分层错误流转：[`.docs/contributing/layered.md`](../../../.docs/contributing/layered.md)
- 相关 skill：`.agents/skills/ai-integrate/SKILL.md`、`.agents/skills/ai-image-sdwebui-engine/SKILL.md`、
  `.agents/skills/ai-chat-openai-engine/SKILL.md`
