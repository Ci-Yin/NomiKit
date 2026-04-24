# sdwebui 示例

可直接复制到 `:app:shared` 或 `feature/sdwebui` 内使用的最小可用样板。

## 1. 构造 SdWebUi 与基础调用

```kotlin
import ciyin.sdwebui.SdWebUi

val sdWebUi = SdWebUi.Builder()
    .host("127.0.0.1")
    .port(7860)
    .useHttps(false)
    .build()

suspend fun loadModels() {
    val result = sdWebUi.stableDiffusion.getModels()
    result
        .onSuccess { models -> println(models.map { it.name }) }
        .onFailure { e -> println("加载失败: ${e.message}") }
}
```

## 2. Process DSL：文生图

```kotlin
import ciyin.sdwebui.process.Process.Companion.runText2Image

suspend fun generateCat(): Result<String> = sdWebUi.runText2Image {
    prompt("a cat sitting on a sofa, masterpiece")
    negativePrompt("low quality, bad anatomy")
    samplerName("Euler a")
    steps(28)
    cfgScale(7.0f)
    width(768)
    height(768)
    seed(1234)
    enableHr(true)
    hrScale(2)
    hrUpscaler("Latent")
    denoisingStrength(0.5f)
}.map { resp -> resp.images.first() }
```

## 3. Process DSL：图生图 + ControlNet + ADetailer

```kotlin
import ciyin.sdwebui.extension.ADetailer.Companion.aDetailer
import ciyin.sdwebui.extension.ADetailer.Companion.aDetailer as applyADetailer
import ciyin.sdwebui.extension.ControlNet.Companion.controlNet
import ciyin.sdwebui.extension.ControlNet.Companion.controlNetUnit
import ciyin.sdwebui.extension.ControlNet.Companion.controlNet as applyControlNet
import ciyin.sdwebui.process.Process.Companion.image2Image

suspend fun img2imgWithControl(initImage: String): Result<String> {
    val cn = controlNet {
        addUnit(
            controlNetUnit {
                inputImage(initImage)
                module("openpose_full")
                model("control_v11p_sd15_openpose [cab727d4]")
                weight(1.0f)
                pixelPerfect(true)
            }
        )
    }
    val ad = aDetailer {
        model("face_yolov8n.pt")
        denoisingStrength(0.4f)
        confidence(0.3f)
    }
    val builder = sdWebUi.image2Image()
        .prompt("a girl, cinematic")
        .negativePrompt("worst quality")
        .steps(24)
        .denoisingStrength(0.55f)
        .applyControlNet(cn)
        .applyADetailer(ad)
    return builder.build().run().map { it.images.first() }
}
```

> `applyControlNet` / `applyADetailer` 是 `Process.Builder` 的扩展函数，原名也叫 `controlNet` / `aDetailer`，import 时用 `as` 改名能避免与构造 DSL 同名混淆。

## 4. 新增 endpoint 的最小骨架

假设要新增 `GET /sdapi/v1/styles` 返回结构化列表：

```kotlin
package ciyin.sdwebui.response

import kotlinx.serialization.Serializable

/**
 * AUTOMATIC1111 `/sdapi/v1/styles` 单条样式定义。
 */
@Serializable
data class StyleResponse(
    val name: String,
    val prompt: String,
    val negativePrompt: String = "",
)
```

```kotlin
package ciyin.sdwebui.service

import ciyin.sdwebui.response.StyleResponse

interface StyleService {
    /**
     * 获取 WebUI 所有提示词样式。
     */
    suspend fun getStyles(): Result<List<StyleResponse>>
}
```

```kotlin
package ciyin.sdwebui.service

import ciyin.sdwebui.client.Client
import ciyin.sdwebui.client.Client.Companion.get
import ciyin.sdwebui.response.StyleResponse
import kotlinx.serialization.json.Json

class StyleServiceImpl(
    override val baseUrl: String,
    override val client: Client,
    override val json: Json,
) : Service(), StyleService {

    override suspend fun getStyles(): Result<List<StyleResponse>> =
        client.get(json, baseUrl, "sdapi/v1/styles")
}
```

最后在 `SdWebUi.kt` 内加 `val style: StyleService by lazy { StyleServiceImpl(baseUrl, client, json) }`。

## 5. RecordingClient 测试模板

下面是为上面 `StyleServiceImpl` 写测试的完整样板，可直接放到 `commonTest/.../service/StyleServiceImplTest.kt`：

```kotlin
package ciyin.sdwebui.service

import ciyin.sdwebui.client.Client
import ciyin.sdwebui.support.RecordingClient
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StyleServiceImplTest {

    private val baseUrl = "http://127.0.0.1:7860"
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    private fun newService(client: RecordingClient) =
        StyleServiceImpl(baseUrl, client, json)

    @Test
    fun get_styles_should_issue_get_to_styles_endpoint() = runTest {
        val client = RecordingClient().apply {
            enqueueSuccess(
                """
                [
                    { "name": "Anime", "prompt": "anime style", "negativePrompt": "" }
                ]
                """.trimIndent()
            )
        }

        val result = newService(client).getStyles()

        val request = client.requests.single()
        assertEquals(baseUrl, request.baseUrl)
        assertEquals("sdapi/v1/styles", request.path)
        assertEquals(Client.Method.GET, request.method)
        assertNull(request.body)

        val styles = assertNotNull(result.getOrNull())
        assertEquals(1, styles.size)
        assertEquals("Anime", styles.single().name)
    }

    @Test
    fun get_styles_should_fail_when_response_is_not_success() = runTest {
        val client = RecordingClient().apply {
            enqueueFailure("server boom")
        }

        val result = newService(client).getStyles()

        assertTrue(result.isFailure)
        val error = assertNotNull(result.exceptionOrNull())
        assertEquals("server boom", error.message)
    }
}
```

## 6. 跑测试与跨平台编译

只针对本模块时：

```bash
./gradlew :feature:sdwebui:desktopTest :feature:sdwebui:iosSimulatorArm64Test
./gradlew :feature:sdwebui:compileKotlinDesktop \
          :feature:sdwebui:compileKotlinIosSimulatorArm64 \
          :feature:sdwebui:compileAndroidMain
```

完整 check（含 lint 等）：

```bash
./gradlew :feature:sdwebui:check
```

> `testAndroidHostTest` 当前因工程级 JDK 版本问题失败，**与本模块无关**，详见 `SKILL.md` 中"已知问题"。

## 7. 反模式（禁止）

```kotlin
// 反例 1：直接给 RequestBuilder.body 赋值，bodyType 为空导致 DefaultClient 不会发出请求体
client.request {
    baseUrl(baseUrl); path("sdapi/v1/txt2img").method(Client.Method.POST)
    this.body = payload     // 缺 bodyType；正确做法是使用 Companion.body(payload)
    this
}

// 反例 2：在 commonMain 直接 import 平台引擎
import io.ktor.client.engine.cio.CIO   // 破坏 KMP 平台中性

// 反例 3：在 ViewModel/Repository 里手拼 URL 调用 ktor，绕开 Service 抽象
HttpClient().get("http://127.0.0.1:7860/sdapi/v1/txt2img") { ... }

// 反例 4：用 try { service.xxx() } catch (_: Throwable) {} 静默吞错
// → 正确做法是消费 Result.onFailure { ... }，或在 Domain 层映射成场景错误
```
