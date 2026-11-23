# AI 生图后端与接口扩展方案

> 状态：草案（尚在讨论，未采纳；确认后改为“待实现”）
> 范围：`ai-core` 生图契约的接口级调整、OpenAI Images 兼容后端的接入方式、ComfyUI
> 工作流后端的取舍；不含聊天侧、业务层页面与多实例配置改造。
> 相关模块：`:feature:ai-core`、`:feature:ai-integrate`、`:feature:ai-image-sdwebui-engine`、拟议
> `:feature:ai-image-openai-engine`、底层 `:feature:sdwebui`、样例 `:app:sample`。
> 最近核对：2026-11-23；工作区基线 `a01f47ee`（`master`，工作树干净）。
> 验证记录：仅做源码静态核对与官方参数查证；未运行 Gradle、未执行单测、未调用真实生图端点。

本文只记录取舍与拟议契约，不维护实现副本。文中所有代码块均为 **拟议 API**，尚不是当前源码。

## 背景

NomiKit 的生图链路已经分成三层：厂商无关抽象、聚合编排、单一后端适配。

-

抽象层 [ImageEngine.kt](../../feature/ai-core/src/commonMain/kotlin/ciyin/ai/core/engine/ImageEngine.kt)
只有 `generate`、`models`、`validate`
三个方法；一次请求的全部意图集中在 [ImageRequest.kt](../../feature/ai-core/src/commonMain/kotlin/ciyin/ai/core/image/ImageRequest.kt)：
`source` 表示输入模式，`controls` 表示条件控制，`postProcessors` 表示后处理流水线，`vendorOptions`
承载厂商专有参数。

-

聚合层 [AiImageIntegrate.kt](../../feature/ai-integrate/src/commonMain/kotlin/ciyin/ai/integrate/image/AiImageIntegrate.kt)
负责路由、默认模型补齐、重试与降级；配置以 [ImageEngineConfig.kt](../../feature/ai-integrate/src/commonMain/kotlin/ciyin/ai/integrate/image/ImageEngineConfig.kt)
的 `sealed` 子类表达。

-

唯一后端是 [SdWebUiImageEngine.kt](../../feature/ai-image-sdwebui-engine/src/commonMain/kotlin/ciyin/ai/image/sdwebui/SdWebUiImageEngine.kt)
，覆盖文生图、图生图、局部重绘、ControlNet、ADetailer、ReActor、RemBG、Upscale，并在生成期间轮询进度与实时预览。

由此产生三个需要明确回答的问题，本文给出结论与理由：

1. 是否引入 ComfyUI 工作流后端；
2. 是否引入 OpenAI Images 兼容后端；
3. 是否在 `ImageEngine` 上增加 `edit()`、`upscale()` 这类方法。

## 目标与非目标

**目标**

- 明确 `edit` / `upscale` 的表达方式，避免出现同义的第二套入口。
- 给出 OpenAI Images 后端的模块边界、配置形态、能力声明与参数映射。
- 说明 ComfyUI 当前不接入的判断依据，并写明重新评估的触发条件。
- 记录接入前的公共模型修正，使“第二个后端”不会被迫静默吞掉通用字段。

**非目标**

- 不实现 ComfyUI 工作流引擎、节点模型或工作流编辑器。
- 不引入显式任务对象（不新增 `getTask` / `cancelTask` 抽象）。
- 不做“统一参数矩阵”，不把某家后端的默认值写进通用模型。
- 不改造 `feature/sdwebui` 的底层 Service，不新增 cancel / interrupt 端点。
- 不解决同类后端多实例配置（两个 SD WebUI）；见“后续演进”。
- 不改动业务页面与示例交互，仅在实施阶段按能力修正示例请求。

## 关键决策

### D1：不新增 `edit()` / `upscale()` 方法

常见的服务端 SDK 会把生图拆成 `generate` / `edit` / `upscale` 三个
endpoint；本项目的抽象是客户端编排视角，一次请求已经能表达这三种意图。

| 外部 SDK 接口                | 当前表达                                              | 说明                       |
|------------------------------|-------------------------------------------------------|----------------------------|
| `generate()`                 | `ImageRequest(source = ImageSource.TextToImage)`      | 文生图                     |
| `edit()`                     | `ImageSource.ImageToImage` / `ImageSource.Inpainting` | 整图编辑 / 蒙版编辑        |
| `upscale()`                  | `ImagePostProcessor.Upscale(factor, model)`           | 生成后的超分流水线         |
| `getTask()` / `cancelTask()` | 不需要；`Flow<ImageEvent>` 即任务生命周期             | 取消语义另议，见“后续演进” |

新增顶层方法会让同一意图出现两条状态路径，取消、重试、降级、观测都要各写一遍。因此 **不新增**。

`edit()` 的等价写法：

```kotlin
ImageRequest(
    prompt = "把背景换成海边",
    source = ImageSource.ImageToImage(sourceImage = bytes, denoisingStrength = 0.6f),
)
```

### D2：补“仅后处理”输入意图，并把 `denoisingStrength` 改为可空

当前 `upscale()` 的缺口不是方法，而是模型：`postProcessors`
只在“已经生成出图”之后执行，无法表达“对用户导入的图只做超分/只去背景，不进行采样生成”。这是客户端的高频功能，需要一种输入意图，而不是一个新方法。

同时 [ImageSource.kt](../../feature/ai-core/src/commonMain/kotlin/ciyin/ai/core/image/ImageSource.kt)
把 `denoisingStrength` 默认值写死为 `0.75f`，等于把某家后端的口味写进通用模型；OpenAI Images 没有该参数，
`0.75` 会变成无意义的透传值。`steps` / `cfgScale` / `seed` 已经采用“`null` = 引擎默认”的写法，这里应保持一致。

拟议改动：

```kotlin
sealed interface ImageSource {
    data object TextToImage : ImageSource

    data class ImageToImage(
        val sourceImage: ByteArray,
        /** 去噪强度 `0.0..1.0`；`null` 表示沿用引擎默认。 */
        val denoisingStrength: Float? = null,
    ) : ImageSource

    data class Inpainting(
        val sourceImage: ByteArray,
        val mask: ByteArray,
        /** 重绘强度 `0.0..1.0`；`null` 表示沿用引擎默认。 */
        val denoisingStrength: Float? = null,
    ) : ImageSource

    /**
     * 只对已有图像执行 [ImageRequest.postProcessors]，不进行采样生成。
     *
     * 引擎必须在该源配合非空 `postProcessors` 时接受请求；无法只做后处理的后端应在 `validate` 中拒绝。
     */
    data class PostProcessOnly(val sourceImage: ByteArray) : ImageSource
}
```

影响面（`ImageSource` 是 `sealed`，新增分支会强制下游穷举）：

| 位置                                 | 需要处理                                                            |
|--------------------------------------|---------------------------------------------------------------------|
| `SdWebUiImageEngine.validate`        | 新增分支校验：`postProcessors` 非空、输入图非空                     |
| `ImageRequestToSdWebUiMapper`        | 映射为 `runExtraSingleImage` / `runRemBG`，不调用 txt2img / img2img |
| `AiImageIntegrate.primaryCapability` | 新增分支归为 `ImageCapability.Upscale` 或按后处理项判定             |
| `SdWebUiImageEngine` 日志摘要        | 新增分支输出模式名与字节数                                          |
| 样例 `AiImageRepository`             | 不需要改；现有文生图路径不受影响                                    |

行为兼容：SD WebUI mapper 在 `denoisingStrength == null` 时补 `0.75f`，现有调用结果不变。

### D3：接入 OpenAI Images 兼容后端

**结论：建议实施，作为第二个后端。**

理由：

1. **一端多实现**。`/v1/images/generations`、`/v1/images/edits` 已被
   LocalAI、stable-diffusion.cpp、各类中转与 Azure 兼容端点采用；接一次即可覆盖云端与部分本地推理，这与
   `:feature:ai-chat-openai-engine` 用一个端点覆盖多家聊天服务是同一逻辑。
2. **参数面小**。真正无法映射到通用模型的只有 `quality` / `background` / `output_format` /
   `input_fidelity`，走 `vendorOptions` 即可。
3. **能验证降级设计**。SD WebUI 有 steps、cfg、negative prompt、ControlNet，OpenAI Images 全没有；能力子集与
   `validate` 拒绝机制只有接第二个后端才会被真正压测。

拟议模块：`:feature:ai-image-openai-engine`，包名 `ciyin.ai.image.openai`，与聊天引擎同构（Ktor +
`expect/actual` HTTP 引擎 + `MockEngine` 单测）。依赖保持 `api(projects.feature.aiCore)`，不引入 DI 框架。

拟议配置子类（加入 `ImageEngineConfig` 的 `sealed` 分支）：

```kotlin
data class OpenAiImages(
    override val baseUrl: String = "https://api.openai.com",
    override val apiKey: String,
    override val defaultModel: String? = "gpt-image-1.5",
) : ImageEngineConfig() {
    override val engineId: EngineId = IntegrateImageEngineIds.openAiImages
}
```

拟议引擎能力与校验：

```kotlin
class OpenAiImagesImageEngine(
    override val id: EngineId,
    private val config: OpenAiImagesImageEngineConfig,
) : ImageEngine {
    override val provider: String = "openai-images"
    override val runtime: EngineRuntime = EngineRuntime.RemoteCloud

    override val capabilities: Set<AiCapability> = setOf(
        ImageCapability.TextToImage,
        ImageCapability.ImageToImage,
        ImageCapability.Inpainting,
    )

    override suspend fun validate(request: ImageRequest): Result<Unit> = runCatching {
        require(request.seed == null) { "OpenAI Images 不支持 seed" }
        require(request.steps == null) { "OpenAI Images 不支持 steps" }
        require(request.cfgScale == null) { "OpenAI Images 不支持 cfgScale" }
        require(request.negativePrompt.isNullOrBlank()) { "OpenAI Images 不支持 negativePrompt" }
        require(request.controls.isEmpty()) { "OpenAI Images 不支持 controls" }
        require(request.postProcessors.isEmpty()) { "OpenAI Images 不支持后处理流水线" }
        require(request.batch in 1..10) { "batch 必须在 1..10" }
        require(OpenAiImagesSizes.contains(request.size)) { "尺寸不在白名单内" }
    }
}
```

参数映射（依据 OpenAI image API quick reference，2026-11-23 查阅）：

| 通用字段                                                                         | `/v1/images/generations` | `/v1/images/edits` | 处理                                       |
|----------------------------------------------------------------------------------|--------------------------|--------------------|--------------------------------------------|
| `prompt`                                                                         | `prompt`                 | `prompt`           | 直接映射                                   |
| `model`                                                                          | `model`                  | `model`            | `null` 时用配置默认模型                    |
| `batch`                                                                          | `n`                      | `n`                | 1..10，超出在 `validate` 拒绝              |
| `size`                                                                           | `size`                   | `size`             | 仅 `1024x1024` / `1536x1024` / `1024x1536` |
| `source.ImageToImage`                                                            | —                        | `image`            | 无 mask 的整图编辑                         |
| `source.Inpainting`                                                              | —                        | `image` + `mask`   | 蒙版为提示引导，不保证精确形状             |
| `source.PostProcessOnly`                                                         | 拒绝                     | 拒绝               | 该后端没有独立后处理能力                   |
| `seed` / `steps` / `cfgScale` / `negativePrompt` / `controls` / `postProcessors` | 拒绝                     | 拒绝               | 无对应参数，不静默丢弃                     |

厂商参数键（沿用厂商前缀约定，与 `sdwebui.alwaysonScripts` 一致）：

| `vendorOptions` 键         | 取值                               | 说明                                |
|----------------------------|------------------------------------|-------------------------------------|
| `openai.quality`           | `low` / `medium` / `high` / `auto` | 生成质量                            |
| `openai.background`        | `transparent` / `opaque` / `auto`  | 输出透明度                          |
| `openai.outputFormat`      | `png` / `jpeg` / `webp`            | 输出格式，同时决定回填的 `mimeType` |
| `openai.outputCompression` | 0–100                              | 仅 jpeg / webp                      |
| `openai.inputFidelity`     | `low` / `high`                     | 仅 edits 端点                       |

响应映射：`data[].b64_json` 解码为字节，`mimeType` 由 `openai.outputFormat` 推导（默认 `image/png`）；
`ImageResult.info` 可回填 `model` / `size` / `quality`，不做强类型解析。

**失败与降级**：`AiEngineError.Unsupported`
默认不在 [FallbackPolicy.kt](../../feature/ai-integrate/src/commonMain/kotlin/ciyin/ai/integrate/image/FallbackPolicy.kt)
的 `triggerOn` 中，因此带 ControlNet 的请求打到 OpenAI 引擎会直接失败，而不是静默回落到 SD
WebUI。这是有意设计：业务层需要按 `ImageModelInfo.capabilities` 预先收敛参数，或捕获 `Unsupported`
后重新路由。

### D4：ComfyUI 工作流后端暂不接入

**结论：本次不实施，保留接入路径。**

决定性问题不是适配器工作量，而是 **工作流由谁提供**：

- 库内置固定工作流 → 该后端只支持内置功能子集，“ComfyUI 的灵活性”不成立；
- 要求调用方传入完整工作流 JSON → 适配器退化为 HTTP 代理，`ImageRequest` 的 prompt / size / steps
  全部失效，`ImageCapability` 与 `validate` 失去意义，UI 也无法校验。

两条路都不健康，且当前没有任何消费方：全仓仅在 [ImageRequest.kt](../../feature/ai-core/src/commonMain/kotlin/ciyin/ai/core/image/ImageRequest.kt)
与 [ImageControl.kt](../../feature/ai-core/src/commonMain/kotlin/ciyin/ai/core/image/ImageControl.kt)
的注释中提到 ComfyUI。

**重新评估的触发条件**（满足任一再进入设计）：

1. 出现必须走 ComfyUI 的模型需求（A1111 系列支持不佳的新架构）；
2. 产品需要“导入并运行社区工作流”，此时它是一项独立功能，而不是“第二个后端”。

若届时接入，形态只能是“透传优先 + 内置最小图兜底”，且候选工作流键为 `comfyui.workflow`（与
`ImageRequest` 注释一致）：

```kotlin
override fun generate(request: ImageRequest): Flow<ImageEvent> = flow {
    emit(ImageEvent.Started)
    val workflow = request.vendorOptions[ComfyVendorKeys.workflow] as? JsonObject
        ?: minimalTextToImageGraph(request)
    val promptId = client.queue(workflow).getOrThrow()
    client.progress(promptId).collect { p ->
        emit(ImageEvent.Progress(p.ratio, p.text))
    }
    emit(ImageEvent.Completed(client.history(promptId).getOrThrow()))
}
```

此代码仅为形态示意，未采纳、未实施。

### D5：`models()` 必须如实标注能力（前置修正）

[SdWebUiImageEngine.kt](../../feature/ai-image-sdwebui-engine/src/commonMain/kotlin/ciyin/ai/image/sdwebui/SdWebUiImageEngine.kt)
目前把引擎能力集合原样复制给每个模型：

```kotlin
capabilities = capabilities.filterIsInstance<ImageCapability>().toSet()
```

结果是 FLUX 等模型也被标为支持 `ControlNet`，业务层按 `ImageModelInfo.capabilities`
收敛参数必然误判。接入第二个后端时，这条会成为“按能力适配”路径上的直接障碍，因此列为前置修正。

拟议把 [ImageModelInfo.kt](../../feature/ai-core/src/commonMain/kotlin/ciyin/ai/core/image/ImageModelInfo.kt)
的语义改为“可空 = 未知”：

```kotlin
/**
 * 该模型实际具备的能力子集；`null` 表示未知，调用方应退回所属引擎的 capabilities，
 * 空集合表示明确不支持任何可选能力。
 */
val capabilities: Set<ImageCapability>? = null
```

SD WebUI 引擎在无法判断时返回 `null` 而不是复制引擎能力；OpenAI 引擎按模型名给出已知子集或 `null`。

## 实施步骤与验收

| 阶段 | 内容                                                                                                 | 验收条件                                                                                                                                              |
|------|------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------|
| 0    | D5 能力语义 + D2 `ImageSource` 调整与穷举分支                                                        | 所有 `when (ImageSource)` 编译通过；SD WebUI 默认去噪仍为 `0.75`；`:feature:ai-core:desktopTest`、`:feature:ai-image-sdwebui-engine:desktopTest` 通过 |
| 1    | 新建 `:feature:ai-image-openai-engine`（config、client、mapper、ErrorMapper、单测）                  | `MockEngine` 单测覆盖文生图 / 图生图 / 局部重绘 / 尺寸拒绝 / 非 2xx 与解析失败；`desktopTest` 与 `iosSimulatorArm64Test` 通过                         |
| 2    | 聚合装配：`ImageEngineConfig.OpenAiImages`、`IntegrateImageEngineIds.openAiImages`、工厂 `when` 分支 | `AiImageIntegrateTest` 覆盖 `Explicit` 路由到 OpenAI、默认仍路由 SD WebUI、`models()` 跨引擎去重                                                      |
| 3    | 示例按能力适配（尺寸白名单、negative prompt 收敛）                                                   | 示例在 SD WebUI 与 OpenAI 两类配置下均可生成；不再出现 `600x1000` 直传 OpenAI                                                                         |
| 4    | ComfyUI                                                                                              | 不在本版实施                                                                                                                                          |

最小验证命令（实施时按阶段执行，纯文档阶段不执行）：

```bash
./gradlew :feature:ai-core:desktopTest :feature:ai-image-sdwebui-engine:desktopTest
./gradlew :feature:ai-image-openai-engine:desktopTest :feature:ai-image-openai-engine:compileKotlinIosSimulatorArm64
./gradlew :feature:ai-integrate:desktopTest
```

## 风险与限制

-

**尺寸限制会直接冲击现有示例**：[AiImageRepository.kt](../../app/sample/src/commonMain/kotlin/com/ciyin/app/presentation/screen/aiimage/data/AiImageRepository.kt)
使用 `ImageSize(600, 1000)` 与固定 `negativePrompt`，接 OpenAI 引擎后会在 `validate`
失败；这是设计要求，不是缺陷，但必须同步示例与 UI。

- **模型与参数随版本漂移**：`gpt-image-1.5` / `gpt-image-1-mini`
  等型号的可选参数并不完全一致；适配层应宽容解析响应、严格校验请求，并保留厂商错误原文。
- **密钥与日志**：`ImageEngineConfig.apiKey` 目前是未接线字段，接入后必须保证不写入日志、不出现在错误
  message 中。
- **不自动降级**：`Unsupported` 不触发 `FallbackPolicy`，跨后端能力适配由业务层负责。
- **同类多实例仍不支持**：`mergeEngineConfigsWithDefaults` 按 `sealed` 子类合并，两个 SD WebUI（本机 +
  局域网）仍无法同时注册；如需支持，需要把合并键改为 `engineId`，不在本版范围。
- **未验证项**：OpenAI 端点未真实调用；SD WebUI 新分支的后处理路径未实现；iOS/Android 编译未执行。

## 后续演进（不在本版）

- **取消语义**：`ImageEngine` 无 `cancel()`，且 `feature/sdwebui` 未暴露 `/sdapi/v1/interrupt`
  ；当前取消只停客户端轮询，服务端仍在占用 GPU。
- **多实例**：合并键从 `KClass` 改为 `EngineId`。
- **ComfyUI**：按 D4 的触发条件重新评估。

## 待确认

1. 本文状态是否从“草案”转为“待实现”，即是否采纳 D1–D5。
2. 实施顺序是否维持 0 → 1 → 2 → 3。
3. ComfyUI 是否确认维持“不接入 + 保留触发条件”。

## 参考

- [OpenAI Images API reference](https://platform.openai.com/docs/api-reference/images)
- OpenAI image API quick
  reference（[openai/skills](https://github.com/openai/skills/blob/main/skills/.system/imagegen/references/image-api.md)
  ，2026-11-23 查阅；含 `gpt-image-1.5` / `gpt-image-1` / `gpt-image-1-mini` 的参数与限制）
- 相关模块 skill：`.agents/skills/ai-core/SKILL.md`、
  `.agents/skills/ai-image-sdwebui-engine/SKILL.md`、`.agents/skills/ai-integrate/SKILL.md`
