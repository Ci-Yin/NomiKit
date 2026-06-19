---
name: sdwebui
description: Use the feature/sdwebui Kotlin Multiplatform module (package ciyin.sdwebui) to call AUTOMATIC1111 Stable Diffusion WebUI REST API. Covers SdWebUi.Builder construction, the four services (core / stableDiffusion / controlNet / reActor), the Process DSL (text2Image / image2Image / extraSingleImage / extraBatchImages / remBG), the Extension DSL (ControlNet / ADetailer / ReActor), the expect/actual HTTP engine wiring, and the RecordingClient testing pattern. Use when the user mentions sdwebui, SD WebUI, AUTOMATIC1111, Stable Diffusion, 文生图, 图生图, ControlNet, ADetailer, ReActor, when the user wants to consume image-generation APIs from :app:shared, when adding a new endpoint / service / payload to feature/sdwebui, or when writing tests for that module.
---

# feature/sdwebui 模块协作指南

`feature/sdwebui` 是 NomiKit 内的 Kotlin Multiplatform 模块，封装了 AUTOMATIC1111 Stable Diffusion WebUI 的 REST API。所有 Kotlin 源码包名为 `ciyin.sdwebui`，平台覆盖 `commonMain` + `androidMain` / `desktopMain` / `iosMain`。

## 触发场景

任意一项命中即按本 skill 处理：

- 用户提到 SD WebUI、AUTOMATIC1111、Stable Diffusion、文生图、图生图、ControlNet、ADetailer、ReActor、RemBG
- 用户希望在 `:app:shared` 或其他业务模块调用图像生成能力
- 用户要给 `feature/sdwebui` 增加 endpoint / Service / Payload / Response / Process DSL / Extension DSL
- 用户要为 `feature/sdwebui` 写测试或排查它的构建/运行错误

不命中则忽略本 skill。

## 模块速览

```
feature/sdwebui/
├── build.gradle.kts                # multiplatform-lib-targets + kotlin.serialization
└── src/
    ├── commonMain/kotlin/ciyin/sdwebui/
    │   ├── SdWebUi.kt              # 入口 Builder，组装四个 Service
    │   ├── client/                 # Client 抽象 + DefaultClient(expect)
    │   ├── service/                # 4 个接口 + 4 个 Impl
    │   ├── payload/                # @Serializable 请求体
    │   ├── response/               # @Serializable 响应体
    │   ├── process/                # 高级业务 DSL（推荐外部使用）
    │   ├── extension/              # ControlNet / ADetailer / ReActor 等扩展 DSL
    │   └── internal/extension/     # buildUrl 等内部工具
    ├── androidMain/.../DefaultClient.android.kt   # actual: OkHttp
    ├── desktopMain/.../DefaultClient.desktop.kt   # actual: CIO
    ├── iosMain/.../DefaultClient.ios.kt           # actual: Darwin
    └── commonTest/kotlin/ciyin/sdwebui/
        ├── support/RecordingClient.kt # 自研 mock，记录请求 + 回放响应
        └── ...                        # 各类单元测试
```

依赖：Ktor 3.4.0（core / content-negotiation / serialization-kotlinx-json）+ `kotlinx-serialization-json`，平台引擎 OkHttp / CIO / Darwin。所有 Ktor / serialization 版本通过 `gradle/libs.versions.toml` 的 Version Catalog 引用，**禁止**在 `build.gradle.kts` 里硬写版本号。

## 核心 API（外部使用顺序）

1. 构造 `SdWebUi`：

```kotlin
val sdWebUi = SdWebUi.Builder()
    .host("127.0.0.1")
    .port(7860)
    .useHttps(false)
    .build()
```

2. 直接调 Service（细粒度）：`sdWebUi.core` / `sdWebUi.stableDiffusion` / `sdWebUi.controlNet` / `sdWebUi.reActor`，所有方法都是 `suspend fun ...: Result<T>`；例如 `stableDiffusion.getLoras()` 返回 `Result<List<LoraResponse>>`，其中 `metadata` 保留为紧凑 JSON 字符串。

3. 推荐用 `Process` 高级 DSL（来自 `ciyin.sdwebui.process.Process.Companion`）：

```kotlin
import ciyin.sdwebui.process.Process.Companion.runText2Image

val result = sdWebUi.runText2Image {
    prompt("a cat")
    negativePrompt("low quality")
    steps(28)
    cfgScale(7.0f)
}
```

4. 需要 ControlNet / ADetailer 等扩展时，用 `extension/` 下的 DSL 组合到 `Process.Builder`：

```kotlin
import ciyin.sdwebui.extension.ControlNet.Companion.controlNet
import ciyin.sdwebui.extension.ControlNet.Companion.controlNetUnit
import ciyin.sdwebui.extension.ControlNet.Companion.controlNet as applyControlNet

val cn = controlNet { addUnit(controlNetUnit { model("control_v11p_sd15_openpose") }) }
val builder = sdWebUi.text2Image().prompt("a girl").applyControlNet(cn)
val resp = builder.build().run()
```

完整 Service 与方法清单见 [reference.md](reference.md)，更多调用 / 测试样板见 [examples.md](examples.md)。

## 工作流：新增一个 endpoint

按"从源头建模"的原则，**禁止**在 ViewModel 或调用方手拼字符串、绕开 Service。标准流程：

1. **建模请求/响应**：在 `commonMain/.../payload/` 或 `response/` 新增 `data class`，加 `@Serializable` 与中文 KDoc。字段命名遵循 SD WebUI 官方 JSON（必要时用 `@SerialName`）。
2. **声明接口**：在对应 `service/XxxService.kt` 新增 `suspend fun ...: Result<T>`，方法名与领域一致，不要直接暴露 HTTP 词汇。
3. **实现接口**：在 `service/XxxServiceImpl.kt`，使用 `client.get(path)` / `client.post(path) { setBody(payload) }`，返回值通过 `response.load<T>()` 反序列化；空响应用 `Result<Unit>` + `load<Unit>()`。
4. **加高级 DSL（可选）**：复杂参数集合写一个 `process/Xxx.kt`（带 `Builder`），并在 `Process.Companion` 加 `runXxx { ... }` 扩展函数。
5. **写测试**：在 `commonTest/.../service/` 增 `RecordingClient` 用例，断言 path、method、body 与解析结果。
6. **跨平台编译验证**：

```bash
./gradlew :feature:sdwebui:compileKotlinDesktop \
          :feature:sdwebui:compileKotlinIosSimulatorArm64 \
          :feature:sdwebui:compileAndroidMain
```

## 工作流：扩展一个新平台 / 替换 HTTP 引擎

`client/DefaultClient.kt` 通过 `internal expect fun defaultHttpClientEngineFactory(): HttpClientEngineFactory<*>` 路由引擎。新增平台 / 替换引擎的唯一正确做法：

1. 在对应平台源集（如 `wasmJsMain/.../DefaultClient.wasmJs.kt`）补 `internal actual fun defaultHttpClientEngineFactory()`，返回该平台支持的 Ktor 引擎。
2. 在 `gradle/libs.versions.toml` 增加 `ktor-client-xxx` 别名，并在 `build.gradle.kts` 的对应平台源集 `dependencies` 块引用。
3. **不要**在 `commonMain` 直接 import 任何平台引擎。

## 工作流：写单元测试

固定模式（详细模板见 [examples.md](examples.md)）：

1. 用 `support/RecordingClient`（已存在）作为 `Client`，**不要**引入 `ktor-client-mock` 或真实 HTTP。
2. 用 `kotlinx.coroutines.test.runTest { ... }` 包裹。
3. 用 `client.enqueue(statusCode, jsonBody)` 注入响应；用 `client.lastRecorded()` / `recorded` 列表断言 path / method / body。
4. 反序列化失败应作为失败用例存在（验证 `Result.isFailure`）。
5. 验证命令：

```bash
./gradlew :feature:sdwebui:desktopTest :feature:sdwebui:iosSimulatorArm64Test
```

> 已知问题：`testAndroidHostTest` 因 NomiKit 工程级 `multiplatform-lib-targets` 把 Android `jvmTarget` 设为 21、而 Gradle daemon 跑在 JDK 17 而失败。**这是工程级问题，与本模块无关**，不要为它写 hack。等工程统一升级 JDK 21 或下调 jvmTarget 后即可恢复。

## 硬性约束

- 任何新增 `class` / `interface` / `object` / 公开/扩展函数必须补**中文 KDoc**。
- Payload / Response **必须** `@Serializable`；字段默认值要保留，避免破坏向后兼容。
- 平台特定代码只能放在 `androidMain` / `desktopMain` / `iosMain`，`commonMain` 必须保持平台中性。
- 依赖版本一律走 `libs.versions.toml`，不要在 `build.gradle.kts` 里写裸版本号。
- 模块归属为 `feature/`，**禁止**反向依赖 `:app:*`、`:business:*`。

## 附加资源

- 完整 Service / endpoint / DSL 清单见 [reference.md](reference.md)
- 调用与测试代码样板见 [examples.md](examples.md)
