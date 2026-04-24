# sdwebui 参考手册

完整 API 与 endpoint 映射，按 Service / Process DSL / Extension DSL / 关键类型 四块组织。

## 四个 Service 与 endpoint 映射

所有方法签名都是 `suspend fun ...: Result<T>`；`baseUrl` 由 `SdWebUi.Builder` 拼成（`http(s)://host:port`）。

### CoreService（路径前缀无）

| 方法 | HTTP | 路径 | 返回 |
| --- | --- | --- | --- |
| `getQueue()` | GET | `queue/status` | `QueueResponse` |

### StableDiffusionService（路径前缀 `sdapi/v1/`，少数特例）

| 方法 | HTTP | 路径 | 返回 |
| --- | --- | --- | --- |
| `text2Image(payload)` | POST | `sdapi/v1/txt2img` | `GenerateProcessResponse` |
| `image2Image(payload)` | POST | `sdapi/v1/img2img` | `GenerateProcessResponse` |
| `extraSingleImage(payload)` | POST | `sdapi/v1/extra-single-image` | `ExtraSingleImageResponse` |
| `extraBatchImages(payload)` | POST | `sdapi/v1/extra-batch-images` | `ExtraBatchImagesResponse` |
| `getModels()` | GET | `sdapi/v1/sd-models` | `List<ModelResponse>` |
| `getSamplers()` | GET | `sdapi/v1/samplers` | `String` |
| `getEmbeddings()` | GET | `sdapi/v1/embeddings` | `String` |
| `getVae()` | GET | `sdapi/v1/sd-vae` | `List<VaeResponse>` |
| `getLoras()` | GET | `sdapi/v1/loras` | `String` |
| `getOptions()` | GET | `sdapi/v1/options` | `String` |
| `getCmdFlags()` | GET | `sdapi/v1/cmd-flags` | `String` |
| `getExtensions()` | GET | `sdapi/v1/extensions` | `List<ExtensionResponse>` |
| `getHypernetworks()` | GET | `sdapi/v1/hypernetworks` | `String` |
| `getFaceRestorers()` | GET | `sdapi/v1/face-restorers` | `List<FaceRestorerResponse>` |
| `getRealesrganModels()` | GET | `sdapi/v1/realesrgan-models` | `List<RealesrganModelResponse>` |
| `getPromptStyles()` | GET | `sdapi/v1/prompt-styles` | `String` |
| `getUpscalers()` | GET | `sdapi/v1/upscalers` | `List<UpscalerResponse>` |
| `getLatentUpscaleModes()` | GET | `sdapi/v1/latent-upscale-modes` | `List<LatentUpscaleModeResponse>` |
| `getScripts()` | GET | `sdapi/v1/scripts` | `ScriptsResponse` |
| `getScriptInfo()` | GET | `sdapi/v1/script-info` | `String` |
| `getProgress()` | GET | `sdapi/v1/progress` | `ProgressResponse` |
| `getMemory()` | GET | `sdapi/v1/memory` | `MemoryResponse` |
| `setModel(model)` | POST | `sdapi/v1/options` | `Unit`（body=`{"sd_model_checkpoint": model}`） |
| `refreshCheckpoints()` | POST | `sdapi/v1/refresh-checkpoints` | `Unit`（无 body） |
| `remBG(payload)` | POST | `rembg`（**无前缀**） | `RemBGResponse` |

### ControlNetService（路径前缀 `controlnet/`）

| 方法 | HTTP | 路径 | 返回 |
| --- | --- | --- | --- |
| `getVersion()` | GET | `controlnet/version` | `ControlNetVersionResponse` |
| `getModels()` | GET | `controlnet/model_list` | `ControlNetModelsResponse` |
| `getModules()` | GET | `controlnet/module_list` | `ControlNetModulesResponse` |
| `getControlTypes()` | GET | `controlnet/control_types` | `ControlNetControlTypesResponse` |
| `getSettings()` | GET | `controlnet/settings` | `String`（原始 JSON） |

### ReActorService（路径前缀 `reactor/`）

| 方法 | HTTP | 路径 | 返回 |
| --- | --- | --- | --- |
| `getModels()` | GET | `reactor/models` | `ReActorModelsResponse` |
| `getUpscalers()` | GET | `reactor/upscalers` | `ReActorUpscalersResponse` |

> 凡返回 `Result<String>` 的方法都是当前未做强类型建模的 endpoint。如需结构化访问，按"工作流：新增一个 endpoint"步骤补 `Response` 数据类。

## Process 高级 DSL

来自 `ciyin.sdwebui.process.Process.Companion`，以 `SdWebUi` 的扩展形式提供。`runXxx` 立即执行；`xxx()` 返回 `Builder` 用于延迟构建。

| 入口 | Builder | Payload | Response |
| --- | --- | --- | --- |
| `SdWebUi.runText2Image { ... }` | `Text2Image.Builder` | `Text2ImagePayload` | `GenerateProcessResponse` |
| `SdWebUi.runImage2Image { ... }` | `Image2Image.Builder` | `Image2ImagePayload` | `GenerateProcessResponse` |
| `SdWebUi.runExtraSingleImage { ... }` | `ExtraSingleImage.Builder` | `ExtraSingleImagePayload` | `ExtraSingleImageResponse` |
| `SdWebUi.runExtraBatchImages { ... }` | `ExtraBatchImages.Builder` | `ExtraBatchImagesPayload` | `ExtraBatchImagesResponse` |
| `SdWebUi.runRemBG { ... }` | `RemBG.Builder` | `RemBGPayload` | `RemBGResponse` |

`Text2Image.Builder` 常用配置（节选）：`prompt` / `negativePrompt` / `styles` / `seed` / `samplerName` / `steps` / `cfgScale` / `width` / `height` / `enableHr` / `hrUpscaler` / `hrScale` / `denoisingStrength` / `restoreFaces` / `tiling` / `overrideSettings` / `alwaysonScripts`。完整字段以 `Text2Image.kt` 为准。

`Image2Image.Builder` 在文生图基础上多了 `initImages` / `mask` / `inpaintingFill` / `inpaintFullRes` / `resizeMode` / `imageCfgScale` 等图生图专属字段。

> 所有 `Builder` 都实现 `Process.Builder`，因此可直接用 `addAlwaysonScript(key, payload)` 注入任意 `ScriptPayload`。

## Extension DSL

放在 `ciyin.sdwebui.extension/`，统一通过 `Process.Builder.addAlwaysonScript(...)` 写入 `alwaysonScripts`。每个扩展提供两个工具：

- 顶层 DSL（`controlNet { ... }` / `aDetailer { ... }` / `reActor { ... }`）构造扩展配置。
- `Process.Builder` 扩展函数（同名）把扩展配置追加到当前 builder。

| Extension | 顶层入口 | 注入 key | Script 形态 |
| --- | --- | --- | --- |
| `ControlNet` | `controlNet { addUnit(controlNetUnit { ... }) }` | `"ControlNet"` | `ScriptPayload.Multiple(units.args)` |
| `ADetailer` | `aDetailer { ... }` | `"ADetailer"` | `ScriptPayload.Multiple(listOf(args))` |
| `ReActor` | `reActor { ... }` | `"reactor"`（小写） | `ScriptPayload.Array(args)` |

`ControlNet.Unit.Builder` 关键字段：`inputImage`（base64）/ `module` / `model` / `weight` / `resizeMode` / `controlMode` / `pixelPerfect` / `processorRes` / `thresholdA/B` / `guidanceStart/End` / `lowVRam`。

`ADetailer.Builder` 关键字段：`model` / `prompt` / `negativePrompt` / `confidence` / `dilateErode` / `maskBlur` / `denoisingStrength` / `inpaintOnlyMasked` / 与 ControlNet 联动的 `controlNetModel/Module/Weight/GuidanceStart/End`。

`ReActor.Builder` 关键字段：`image`（base64 源脸）/ `model`（如 `inswapper_128.onnx`）/ `faceRestorerName`（如 `CodeFormer`）/ `upscalerName` / `device` / `maskFace`。注意 ReActor 的 `args` 是按位置序列化的 `JsonPrimitive` 列表，**字段顺序不可调整**。

## Client 抽象与 RequestBuilder

`ciyin.sdwebui.client.Client`（commonMain）的真实签名：

```kotlin
abstract class Client {
    abstract suspend fun request(builder: RequestBuilder.() -> RequestBuilder): Response

    class RequestBuilder {
        var body: Any? = null
        var bodyType: TypeInfo? = null
        fun baseUrl(baseUrl: String): RequestBuilder
        fun path(path: String): RequestBuilder
        fun method(method: Method): RequestBuilder
        fun build(): Request
    }

    data class Request(val baseUrl: String, val path: String, val method: Method, val body: Any?, val bodyType: TypeInfo?)
    data class Response(val isSuccess: Boolean, val body: String)
    data class Error(val body: String) : Throwable(body)
    enum class Method { GET, POST }
}
```

`Client.Companion` 提供的辅助：

- `client.get(json, baseUrl, path)` —— 发起 GET 并 `load<T>()`。
- `client.post(json) { baseUrl(...); path(...); body(payload) }` —— DSL 构造 POST，自动 `method(Method.POST)`。
- `RequestBuilder.body(value)` —— 同时设置 `body` 与 `bodyType = typeInfo<T>()`，**必须**通过它写入请求体，不要直接给 `body` 赋值，否则 `DefaultClient` 不会真正发出 body。
- `Response.load<T>(json)` 的判定顺序：
  1. `T == Unit` → 直接 `Result.success(Unit)`，**不看 `isSuccess`**（`refreshCheckpoints / setModel` 等空响应方法因此即使服务端返回非 2xx 也算成功，编写新 endpoint 时需理解这一点）。
  2. `!isSuccess` → `Result.failure(Client.Error(body))`。
  3. 其它 → `runCatching { json.decodeFromString<T>(body) }`，反序列化失败也是 `Result.failure`。

`DefaultClient` 的真实签名：

```kotlin
class DefaultClient(private val json: Json) : Client()
```

- 内部通过 `defaultHttpClientEngineFactory()` 这一 `internal expect` 拿到 Ktor 引擎，三个平台的 `actual` 分别返回 `OkHttp` / `CIO` / `Darwin`。
- 不要在调用方传入或替换平台引擎；如需自定义网络栈或注入测试替身，**实现 `Client` 抽象类**并通过 `SdWebUi.Builder.client(...)` 注入（如 `RecordingClient`）。

## SdWebUi.Builder 默认值

| Builder 方法 | 默认 | 说明 |
| --- | --- | --- |
| `host(...)` | `SdWebUi.DEFAULT_HOST = "127.0.0.1"` | API 主机 |
| `port(...)` | `SdWebUi.DEFAULT_PORT = 7860` | AUTOMATIC1111 默认端口 |
| `useHttps(...)` | `false` | 是否走 TLS |
| `client(...)` | `DefaultClient(json)` | 可注入 `RecordingClient` 等 `Client` 实现 |
| 内部 `json` | `Json { isLenient = false; ignoreUnknownKeys = true; encodeDefaults = true; explicitNulls = false }` | Builder 私有 lazy，调用方不可替换 |

`build()` 后所有 Service 都是 `lazy` 单例。`baseUrl` 由 `internal/extension/Extensions.kt::buildUrl(host, port, useHttps)` 计算（具体格式以 `BuildUrlTest` 为准）。`SdWebUi.DEFAULT_TIMEOUT = 50 * 60 * 1000` 用于 `DefaultClient` 的 `HttpTimeout.requestTimeoutMillis`。

## 模块依赖（gradle/libs.versions.toml）

> 仅供查找版本别名时参考，**禁止**在模块 `build.gradle.kts` 内硬编码版本号。

| 别名 | 用途 |
| --- | --- |
| `ktor-client-core` | commonMain HTTP 抽象 |
| `ktor-client-content-negotiation` | commonMain JSON 协商 |
| `ktor-client-serialization-kotlinx-json` | commonMain JSON 序列化 |
| `ktor-client-okhttp` | androidMain 引擎 |
| `ktor-client-cio` | desktopMain 引擎 |
| `ktor-client-darwin` | iosMain 引擎 |
| `kotlinx-serialization-json` | commonMain JSON 数据类 |
