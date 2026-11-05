# `:feature:ai-image-sdwebui-engine`

## 用途与边界

将 AUTOMATIC1111 SD WebUI 适配为厂商无关的 `ai-core.ImageEngine`。
本模块负责请求映射、生成事件、进度与实时预览、串行后处理和统一引擎错误； 不负责产品策略、重试降级、页面状态或依赖注入。

模块声明见 [settings.gradle.kts](../../settings.gradle.kts)，
直接依赖见 [build.gradle.kts](build.gradle.kts)：

| 依赖                                  | 类型             | 职责                                     |
|---------------------------------------|------------------|------------------------------------------|
| `feature/ai-core`                     | `api`            | 通用请求、模型、事件和错误               |
| `feature/sdwebui`                     | `implementation` | REST Service、Process DSL 与 HTTP Client |
| `core/serialization`、`core/platform` | `implementation` | JSON 辅助能力与日志                      |
| `kotlinx-coroutines-core`             | `implementation` | 生成事件流与进度轮询                     |

HTTP 平台引擎由底层 SDK 提供，本模块没有直接声明 Ktor 引擎依赖。 公共特性模块不能反向依赖 `app`
、上层聚合模块或其他厂商引擎。

## 从哪里开始

| 入口                                                                                                 | 职责                                                 |
|------------------------------------------------------------------------------------------------------|------------------------------------------------------|
| [SdWebUiImageEngine](src/commonMain/kotlin/ciyin/ai/image/sdwebui/SdWebUiImageEngine.kt)             | `generate`、`models`、`validate` 与专属 `loras` 查询 |
| [SdWebUiImageEngineConfig](src/commonMain/kotlin/ciyin/ai/image/sdwebui/SdWebUiImageEngineConfig.kt) | 引擎 ID、主机、端口与 HTTPS 配置                     |
| [SdWebUiLoraInfo](src/commonMain/kotlin/ciyin/ai/image/sdwebui/SdWebUiLoraInfo.kt)                   | LoRA 条目；metadata 保留为 JSON 字符串               |

## 最小接入

```kotlin
import ciyin.ai.core.engine.EngineId
import ciyin.ai.image.sdwebui.SdWebUiImageEngine
import ciyin.ai.image.sdwebui.SdWebUiImageEngineConfig

val engine = SdWebUiImageEngine(
    SdWebUiImageEngineConfig(
        id = EngineId("sdwebui:local"),
        host = "127.0.0.1",
        port = 7860,
        useHttps = false,
    ),
)
```

生成入口为 `engine.generate(ImageRequest(...))`，消费 `ImageEvent`。 应用装配可通过
`feature/ai-integrate`，无需在本模块引入 DI。 模型查询在调用方的 suspend 上下文执行：

```kotlin
import ciyin.ai.core.error.AiEngineException
import kotlinx.coroutines.CancellationException

try {
    val models = engine.models()
    // 成功结果可为空；与请求失败是不同语义。
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (failure: AiEngineException) {
    // 用 failure.error 转译为调用方的错误模型，不依赖 internal mapper 类型。
    throw failure
}
```

## 错误契约

`models()` 不把失败转换成成功空列表：请求直接抛出的异常与 SDK `Result.failure`
都经 [ErrorMapper](src/commonMain/kotlin/ciyin/ai/image/sdwebui/mapper/ErrorMapper.kt)
分类后，以公共 `AiEngineException.error` 向调用方传播。

| 路径                        | 结果                                             |
|-----------------------------|--------------------------------------------------|
| 成功的模型数组，包括 `[]`   | 正常返回 `List<ImageModelInfo>`                  |
| 非 2xx / `Client.Error`     | `AiEngineException` 携带 `Protocol` 与原始响应体 |
| JSON 语法、类型或缺字段错误 | `AiEngineException` 携带 `Protocol` 与解析 cause |
| IO transport 异常           | `AiEngineException` 携带 `Network` 与原始 cause  |
| 其他参数 / 未分类异常       | 按 mapper 转为 `Unsupported` / `Unknown`         |
| 已类型化的公共或内部包装    | 保留原 `AiEngineError` 负载                      |
| `CancellationException`     | 同一个异常实例原样上抛                           |

`generate()` 仍用 `ImageEvent.Failed` 传递失败；`loras()` 保留底层原始异常契约。 响应反序列化错误必须先于一般
`IllegalArgumentException` 分类，避免误报为请求不支持。

## 源码、平台与扩展

- 实现位于 `src/commonMain`，测试位于 `src/commonTest`，本模块无平台专属源码。
- Android、Desktop、iOS 的实际 target 由工程约定与开关决定；HTTP 差异由 `feature/sdwebui` 处理。
- 请求、ControlNet 和后处理适配分别位于 `mapper`；厂商扩展走 `vendorOptions`。
- 支持文生图、图生图、重绘、ControlNet、FaceDetailer、FaceSwap、RemBG 和 Upscale；不支持 IPAdapter。

## 验证与相关资料

[SdWebUiImageEngineTest](src/commonTest/kotlin/ciyin/ai/image/sdwebui/SdWebUiImageEngineTest.kt)
使用 [RecordingClient](src/commonTest/kotlin/ciyin/ai/image/sdwebui/support/RecordingClient.kt)
回放响应与直接请求异常，不依赖真实网络；覆盖模型映射、成功空列表、HTTP / JSON / transport 失败、
错误包装负载保留和取消身份，以及既有生成与后处理路径。

启用 Desktop target 后，Windows 最窄验证命令：

```powershell
.\gradlew.bat :feature:ai-image-sdwebui-engine:desktopTest
```

iOS 测试需相应 target 与 macOS 环境；没有执行的编译或测试不能当作已通过。

- [模块 skill](../../.agents/skills/ai-image-sdwebui-engine/SKILL.md)
- [底层 SDK skill](../../.agents/skills/sdwebui/SKILL.md)
- [公共 AI 抽象 skill](../../.agents/skills/ai-core/SKILL.md)
- [架构约定](../../.docs/contributing/architecture.md)
