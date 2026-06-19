---
name: ai-image-sdwebui-engine
description: Use the feature/ai-image-sdwebui-engine Kotlin Multiplatform module (package ciyin.ai.image.sdwebui) to expose AUTOMATIC1111 SD WebUI as an ai-core ImageEngine。Covers SdWebUiImageEngine 与 SdWebUiImageEngineConfig 的构造、ImageRequestToSdWebUiMapper / ControlMapper / PostProcessorMapper / ErrorMapper 四个映射器、生成后处理（RemBG / Upscale）的串行调用、`vendorOptions["sdwebui.alwaysonScripts"]` 透传、单测使用的 RecordingClient 模式。Use when 用户提到 ai-image-sdwebui-engine、SdWebUiImageEngine、SdWebUiImageEngineConfig、把 sdwebui 接入 ai-core / ai-integrate、给 ai-core ImageRequest 增加新的 control / postProcessor 并需要 SD WebUI 适配，或排查该模块的构建/测试错误。
---

# feature/ai-image-sdwebui-engine 模块协作指南

`feature/ai-image-sdwebui-engine` 是 `feature/sdwebui` vendor SDK 在 `ai-core` 抽象上的**薄适配层**
。所有 Kotlin 源码包名为 `ciyin.ai.image.sdwebui.*`，仅 `commonMain` + `commonTest`（无平台源集——平台细节归底层
`feature/sdwebui` 解决）。

它的角色：把 `ai-core.ImageRequest` 翻译成 `feature/sdwebui` 的 Process DSL，把 SD WebUI 响应折叠回
`ai-core.ImageEvent` / `ImageResult`，把异常折叠回 `AiEngineError`。**不**承担降级、观测、业务策略——这些由上层聚合或业务模块负责。

## 触发场景

任意一项命中即按本 skill 处理：

- 用户提到 `ai-image-sdwebui-engine`、`SdWebUiImageEngine`、`SdWebUiImageEngineConfig`
- 用户希望把 `feature/sdwebui` 接入 `ai-core` / `ai-integrate` 框架（而非直接调用 SD WebUI）
- 用户给 `ai-core.ImageControl` / `ImagePostProcessor` 增加了新分支，需要在本模块补 SD WebUI 适配
- 用户希望透传新的 `alwayson script` 给 SD WebUI，或者使用 `vendorOptions["sdwebui.alwaysonScripts"]`
- 用户想新增对应单测（用 `support/RecordingClient`）
- 用户排查本模块的构建 / 测试 / mapper 错误

不命中则忽略本 skill。

> 直接调用 SD WebUI（不经过 `ai-core`）的写法属于 `.agents/skills/sdwebui/SKILL.md` 的范畴，不要混用。

## 模块速览

```
feature/ai-image-sdwebui-engine/
├── build.gradle.kts                       # multiplatform-lib-targets
│                                          # api(project(":feature:ai-core"))
│                                          # implementation(project(":feature:sdwebui"))
└── src/
    ├── commonMain/kotlin/ciyin/ai/image/sdwebui/
    │   ├── SdWebUiImageEngine.kt          # ImageEngine 实现（薄适配 + 主流程）
    │   ├── SdWebUiImageEngineConfig.kt    # 便利构造配置（host / port / useHttps）
    │   └── mapper/
    │       ├── ImageRequestToSdWebUiMapper.kt   # 主映射器：txt2img / img2img / inpainting + alwaysonScripts 透传
    │       ├── ControlMapper.kt                 # ImageControl → ControlNet alwayson script
    │       ├── PostProcessorMapper.kt           # ADetailer/ReActor 前置 + RemBG/Upscale 后置
    │       └── ErrorMapper.kt                   # Throwable → AiEngineError + AiEngineErrorException
    └── commonTest/kotlin/ciyin/ai/image/sdwebui/
        ├── SdWebUiImageEngineTest.kt      # 5 个用例：txt2img / img2img / 后处理 / listModels / validate
        └── support/RecordingClient.kt     # 自研 mock，记录请求 + 回放响应
```

依赖：`api(project(":feature:ai-core"))` + `implementation(project(":feature:sdwebui"))` +
`kotlinx-coroutines-core`。**没有**任何 Ktor / 平台引擎依赖——HTTP 走 `feature/sdwebui` 的
`Client.Default`。

## 核心 API（外部使用顺序）

### 步骤 1：构造引擎实例

最简方式（推荐用于 `app:shared` 的 Koin module）：

```kotlin
import ciyin.ai.core.engine.EngineId
import ciyin.ai.image.sdwebui.SdWebUiImageEngine
import ciyin.ai.image.sdwebui.SdWebUiImageEngineConfig

val engine = SdWebUiImageEngine(
    SdWebUiImageEngineConfig(
        id = EngineId("sdwebui:local-7860"),
        host = "127.0.0.1",
        port = 7860,
        useHttps = false,
    ),
)
```

需要复用已有的 `SdWebUi` 实例时（如自定义 `Client`）：

```kotlin
val sdWebUi = SdWebUi.Builder().host("...").port(7860).build()
val engine = SdWebUiImageEngine(EngineId("sdwebui:custom"), sdWebUi)
```

### 步骤 2：注册到 Registry

```kotlin
val registry = DefaultImageEngineRegistry(listOf(engine /* , otherImageEngine */))
```

业务侧**不直接**持有 `SdWebUiImageEngine`：通常通过 `feature/ai-integrate` 的 `AiImageIntegrate`
由 `ImageEngineConfig.SdWebUi` 统一装配。

### 步骤 3：能力声明

`SdWebUiImageEngine.capabilities` 静态声明：

```
TextToImage / ImageToImage / Inpainting / ControlNet /
FaceDetailer / FaceSwap / BackgroundRemoval / Upscale
```

不包含 `IPAdapter`——`validate(...)` 与 `applyControls(...)` 都会拒绝它。

### 步骤 4：调用

```kotlin
engine.generate(
    ImageRequest(
        prompt = "a cat",
        controls = listOf(
            ImageControl.ControlNet(
                module = "openpose",
                model = "control_v11p_sd15_openpose",
                image = poseBytes
            ),
        ),
        postProcessors = listOf(
            ImagePostProcessor.FaceDetailer(model = "face_yolov8n.pt"),
            ImagePostProcessor.BackgroundRemoval,
            ImagePostProcessor.Upscale(factor = 2f, model = "R-ESRGAN 4x+"),
        ),
    ),
).collect { event -> /* Started / Completed / Failed */ }
```

## 关键映射规则速查

| 通用层概念                                                    | SD WebUI 落点                                                                                         | 处理位置                                                     |
|----------------------------------------------------------|-----------------------------------------------------------------------------------------------------|----------------------------------------------------------|
| `ImageSource.TextToImage`                                | `runText2Image { ... }`                                                                             | `ImageRequestToSdWebUiMapper.invoke`                     |
| `ImageSource.ImageToImage`                               | `runImage2Image { ... }` + `initImages`                                                             | 同上                                                       |
| `ImageSource.Inpainting`                                 | `runImage2Image { ... }` + `initImages` + `mask`                                                    | 同上                                                       |
| `ImageRequest.model`（非 null）                             | 调用前 `stableDiffusion.setModel(model)`                                                               | 同上                                                       |
| `ImageControl.ControlNet`                                | `controlNet { addUnit(controlNetUnit { ... }) }` 通过 `applyControlNet` 注入 alwayson scripts           | `ControlMapper`                                          |
| `ImageControl.IPAdapter`                                 | **拒绝**：`AiEngineError.Unsupported`                                                                  | `ControlMapper`                                          |
| `ImagePostProcessor.FaceDetailer`                        | `aDetailer { model / confidence }` 前置 alwayson                                                      | `PostProcessorMapper.applyPreGenerationPostProcessors`   |
| `ImagePostProcessor.FaceSwap`                            | `reActor { image }` 前置 alwayson                                                                     | 同上                                                       |
| `ImagePostProcessor.BackgroundRemoval`                   | `runRemBG { inputImage }` 二次调用                                                                      | `applyPostGenerationPostProcessors`                      |
| `ImagePostProcessor.Upscale`                             | `runExtraSingleImage { upscalingResize / upscaler1 }` 二次调用                                          | 同上                                                       |
| `vendorOptions["sdwebui.alwaysonScripts"]`（`JsonObject`） | 解析每个键为 `ScriptPayload`（识别 `ad_model` → ADetailer，`module+model` → ControlNet），再 `addAlwaysonScript` | `ImageRequestToSdWebUiMapper.applyVendorAlwaysOnScripts` |
| `ImageRequest.steps` 缺省                                  | txt2img 用 25，img2img/inpainting 用 20                                                                | `applyCommonTextSettings` / `applyCommonImageSettings`   |
| `ImageRequest.cfgScale` 缺省                               | 7.0f                                                                                                | 同上                                                       |
| `ImageRequest.seed`（`Long`）                              | 收缩到 `Int`（饱和）                                                                                       | `Long.toSdSeed()`                                        |
| 图像字节 ↔ base64                                            | `ByteArray.toSdWebUiBase64()` / `String.fromSdWebUiBase64()`（兼容 `data:image/...;base64,` 前缀）        | `SdWebUiResultToImageResultMapper`                       |

## 错误映射（`ErrorMapper.kt`）

```
CancellationException        → 原样上抛（永远不要吞）
AiEngineErrorException       → 解包内部 AiEngineError
IOException (kotlinx.io)     → AiEngineError.Network
Client.Error                 → AiEngineError.Protocol(message = body)
IllegalArgumentException     → AiEngineError.Unsupported
其他                         → AiEngineError.Unknown
```

mapper 内部需要在中途短路失败时，统一 `throw AiEngineErrorException(AiEngineError.Xxx(...))`，**禁止**
直接抛 `IllegalStateException`、用 `error("...")` 之类——那样会被错误地折叠到 `Unknown`。

## 工作流

### 工作流：在 `ai-core` 新增了 `ImageControl` / `ImagePostProcessor` 分支后补适配

按"从源头建模"原则，新增 sealed 子类后必须在所有 mapper 的 `when` 里给出真实映射，**禁止**
`else -> Unit` 静默忽略。

1. **更新能力声明**：若新分支表示新能力，先在 `capabilities` set 里加上对应 `ImageCapability`（或先在
   `ai-core` 加新 capability）。
2. **更新 `validate(...)`**：如果 SD WebUI 支持不了，提前在 `validate` 里
   `error("SdWebUiImageEngine 暂不支持 XXX")`，让 `AiEngineError.Unsupported` 在调用前就回到上层。
3. **写映射**：
    - 控制图（前置脚本）→ `ControlMapper`
    - 前置 alwayson 脚本 → `PostProcessorMapper.applyPreGenerationPostProcessors`
    - 生成后二次请求 → `PostProcessorMapper.applyPostGenerationPostProcessors`
4. **补单测**：按下面"工作流：写单元测试"模板加用例。
5. **跨平台编译验证**：

```bash
./gradlew :feature:ai-image-sdwebui-engine:compileKotlinDesktop ^
          :feature:ai-image-sdwebui-engine:compileKotlinIosSimulatorArm64 ^
          :feature:ai-image-sdwebui-engine:compileAndroidMain
```

### 工作流：用 `vendorOptions["sdwebui.alwaysonScripts"]` 透传脚本

业务侧需要某个 `feature/sdwebui` 已支持但 `ai-core` 还没抽象的脚本时（比如某个新 alwayson script），*
*禁止**改 `ai-core` 公共字段，正确做法：

1. 业务侧用 `kotlinx.serialization.json.buildJsonObject` 构造 `JsonObject`，结构形如：

```kotlin
buildJsonObject {
    putJsonObject("ControlNet") {
        putJsonArray("args") {
            addJsonObject {
                put("module", "openpose")
                put("model", "control_v11p_sd15_openpose")
                // ...
            }
        }
    }
}
```

2. 把它放进 `request.vendorOptions["sdwebui.alwaysonScripts"]`。
3. mapper 会自动识别 `ad_model` → ADetailer、`module + model` → ControlNet；其他结构会抛
   `AiEngineError.Unsupported`。
4. 如果是新脚本结构，在 `ImageRequestToSdWebUiMapper.toScriptArgs()` 里加识别分支（**不要**改
   `ai-core`）。

### 工作流：写单元测试

固定模式：

1. 用 `commonTest/support/RecordingClient`（继承 `ciyin.sdwebui.client.Client`）作为底层客户端，**不要
   **引入真实 HTTP / 不要用 `ktor-client-mock`。
2. 用 `kotlinx.coroutines.runBlocking` 包裹（与 `feature/sdwebui` 测试约定一致）。
3. 用 `client.enqueueSuccess(jsonBody)` / `enqueueFailure(body)` 按调用顺序入队响应；用
   `client.requests` / `client.lastRequest` 断言 path / body。
4. 构造引擎：

```kotlin
private fun engine(client: RecordingClient) = SdWebUiImageEngine(
    id = EngineId("sdwebui:test"),
    sdWebUi = SdWebUi.Builder().client(client).build(),
)
```

5. 验证命令：

```bash
./gradlew :feature:ai-image-sdwebui-engine:desktopTest ^
          :feature:ai-image-sdwebui-engine:iosSimulatorArm64Test
```

> 已知问题：`testAndroidHostTest` 因工程级 JVM target 21 / daemon JDK 17 不一致而失败，**这是工程级问题
**，不要为它加 hack。

## 硬性约束

- 任何新增 `class` / `interface` / `object` / 公开/扩展函数必须补**中文 KDoc**。
- **本模块只能依赖 `:feature:ai-core` 与 `:feature:sdwebui`**；**禁止**反向依赖上层聚合模块
  、任何 `app:*`、任何其他 `ai-xxx-engine`。
- **禁止**引入 DI 框架（Koin / Dagger 等）。`SdWebUiImageEngine` 只通过普通构造函数装配。
- **禁止**直接 `throw RuntimeException`：错误必须走 `AiEngineError` 经 `Failed` 事件流回上层；mapper
  内部短路用 `AiEngineErrorException` 包装。
- **禁止**吞 `CancellationException`（`ErrorMapper` 第一行就是 `throw this`）。
- **禁止**在 `ImageEvent.Completed` 之外再发 `Failed`，反之亦然——契约由 `ai-core.ImageEvent` 定义。
- **禁止**修改 `feature/sdwebui`（vendor SDK）来"配合"本适配层；新需求要么走 `vendorOptions`，要么先在
  `feature/sdwebui` 按它自己的 skill 落地，再来这里映射。
- 依赖版本一律走 `libs.versions.toml`。

## 与上下游模块的边界

| 模块                  | 与本模块的关系                                                                | 注意                                                                      |
|---------------------|------------------------------------------------------------------------|-------------------------------------------------------------------------|
| `feature/ai-core`   | 提供 `ImageEngine` 接口、`ImageRequest`、`ImageEvent`、`AiEngineError`        | `api` 依赖，可暴露在公共签名                                                       |
| `feature/sdwebui`   | 提供底层 `SdWebUi` + Process DSL + 各类 alwayson script payload              | `implementation` 依赖，**不应**出现在公共签名。新增 endpoint 走 `feature/sdwebui` skill |
| `feature/ai-integrate` | 通过配置装配本模块并向上提供生图聚合入口                                     | **绝不**反向依赖 integrate                                                       |
| `app:shared`        | 可通过 `ai-integrate` 使用本模块 | 业务代码默认不要散落 `SdWebUiImageEngine` 构造                    |

## 附加资源

- `ai-core` 抽象：`.agents/skills/ai-core/SKILL.md`
- 底层 SDK：`.agents/skills/sdwebui/SKILL.md`
- 上层聚合：`.agents/skills/ai-integrate/SKILL.md`
