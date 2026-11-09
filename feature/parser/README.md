# `:feature:parser`

## 用途与非目标

`feature/parser` 是 NomiKit 的通用 Kotlin Multiplatform 解析器 DSL 与模型层。它提供统一的解析器执行骨架、站点配置
DSL、HTTP 请求/响应封装、图片/漫画/影视领域模型，以及多站点结果聚合能力。

本模块不负责任何具体站点的 URL、CSS selector、远端 DTO 或站点字段兼容逻辑；Danbooru、Yande、Gelbooru、Hanime
等具体实现位于 [`:feature:parser-site`](../parser-site)。本模块也不负责页面 UI、产品层业务编排或全局协程生命周期。

## 模块位置与依赖边界

本模块位于 `feature` 层，是一个 Kotlin Multiplatform 叶子模块。模块路径由根目录 [
`settings.gradle.kts`](../../settings.gradle.kts) 声明，构建配置位于 [
`build.gradle.kts`](build.gradle.kts)。

`commonMain` 当前依赖：

- [`:core:io`](../../core/io)：文件名和跨平台 IO 辅助能力；
- [`:core:platform`](../../core/platform)：平台日志及时间等能力；
- [`:core:serialization`](../../core/serialization)：通用序列化支持；
- Ksoup：HTML 文档解析；
- Ktor Client Core：HTTP 客户端抽象。

平台 HTTP 引擎由 `ParserEngine` 的 `expect/actual` 提供：Android 和 Desktop 使用 OkHttp，iOS 使用
Darwin。Android 还提供基于 WebView 的动态页面请求实现。上层具体站点模块可以依赖本模块，但本模块不依赖
`feature/parser-site` 或任何产品层模块。

## 从哪里开始

- [`BaseParser.kt`](src/commonMain/kotlin/ciyin/parser/core/BaseParser.kt)
  ：通用解析执行骨架，负责站点配置、类型注册、请求并发、响应解析、结果修订和事件发送。
- [`ParserScope.kt`](src/commonMain/kotlin/ciyin/parser/scope/ParserScope.kt)：配置站点 ID、基础
  URL、HTTP 客户端和结果修订逻辑。
- [`TypeScope.kt`](src/commonMain/kotlin/ciyin/parser/scope/TypeScope.kt)：为一个业务动作注册请求构建和响应解析
  DSL。
- [`HttpRequest.kt`](src/commonMain/kotlin/ciyin/parser/core/HttpRequest.kt)：请求 URL、请求头、请求类型和响应封装。
- [`PictureParser.kt`](src/commonMain/kotlin/ciyin/parser/core/picture/PictureParser.kt)、[
  `ComicParser.kt`](src/commonMain/kotlin/ciyin/parser/core/comic/ComicParser.kt)、[
  `MovieParser.kt`](src/commonMain/kotlin/ciyin/parser/core/movie/MovieParser.kt)：三类领域解析器父类。
- [`MultiParser.kt`](src/commonMain/kotlin/ciyin/parser/core/MultiParser.kt)：多站点并发执行和分页状态归并。

## 最小解析器接入路径

继承对应领域父类，在 `setup` 中设置站点身份和基础 URL，再使用 `on(type)` 注册请求与响应：

```kotlin
import ciyin.parser.core.ParserEvent
import ciyin.parser.core.parametersOf
import ciyin.parser.core.url
import ciyin.parser.core.picture.PictureParser
import ciyin.parser.core.picture.PictureParserType
import ciyin.parser.core.picture.model.PictureRequest
import ciyin.parser.model.Tag
import ciyin.parser.util.PictureParserScope
import kotlinx.coroutines.flow.collect

class ExamplePictureParser : PictureParser() {
    override fun PictureParserScope.setup() {
        id = ExamplePictureSiteId
        baseUrl = "https://example.com"

        onItemRevise {
            copy(
                site = configure.id.site,
                sampleUrl = sampleUrl.ifBlank { originalUrl },
            )
        }

        on(PictureParserType.Feed) {
            request { request ->
                val parameters = parametersOf(
                    "page" to request.page,
                    "tags" to request.tags.joinToString(" "),
                )
                html { url("/posts", parameters) }
                json { url("/posts.json", parameters) }
            }

            response { result ->
                result.copy(
                    tags = document.select(".tag").map { Tag(it.text()) },
                    contents = bodyForJson<List<ExamplePost>>().map { it.toPicture() },
                )
            }
        }
    }
}

val parser = ExamplePictureParser()
parser.request(
    PictureRequest(
        type = PictureParserType.Feed,
        page = 1,
    ),
).collect { event ->
    when (event) {
        is ParserEvent.Success -> use(event.result)
        is ParserEvent.Failure -> handle(event.errors)
    }
}
```

示例中的 `ExamplePictureSiteId`、`ExamplePost`、`toPicture()`、`use()` 和 `handle()` 需要由调用方或具体站点实现提供。
`request()` 返回冷 `Flow`，每次收集都会执行一次解析。

## DSL 分层

解析 DSL 按以下职责分层：

1. `ParserScope`：设置 `id`、`baseUrl`、`enable`、HTTP 引擎和结果修订块。
2. `TypeScope`：通过 `on(type)` 绑定一个稳定的业务动作。
3. `RequestScope`：通过 `html`、`json`、`xml` 声明请求；每个结果类型 key 默认只保留一条请求配置。
4. `HttpRequestBuilder`：通过 `url`、`header`、`headers` 构建完整 URL 和请求头。
5. `ResponseScope`：通过 `document`、`bodyForHtml()`、`bodyForJson<T>()` 和响应访问器读取响应。

同一个业务动作至少要注册一个 `html`、`json` 或 `xml` 请求；否则执行时会抛出未注册请求错误。HTML 使用
Ksoup 解析，JSON 使用 kotlinx.serialization；当前泛型 `bodyForXml<T>()` 尚未实现，不应在站点实现中依赖它。

## 业务动作与通用模型

三类领域使用稳定的业务动作，而不是站点页面名称：

- 图片：`Feed`、`Detail`、`CollectionList`、`Collection`、`Ranking`；
- 漫画：`Feed`、`Detail`、`Chapter`；
- 影视：`Feed`、`Detail`。

请求统一实现 [`ParserRequest`](src/commonMain/kotlin/ciyin/parser/model/ParserRequest.kt)，通过从 `1`
开始的 `page` 表达页码；需要 keyset/连续分页的站点可使用尾部可选的 opaque `cursor`，但不引入通用
`pageSize`。结果统一实现 [`ParserResult`](src/commonMain/kotlin/ciyin/parser/model/ParserResult.kt)
，通过
[`Pagination.kt`](src/commonMain/kotlin/ciyin/parser/model/Pagination.kt) 表达 `page`、可空
`totalPages`、可空 `hasNext` 和可空 `nextCursor`。`nextCursor` 非空时必须表示存在下一页；游标为空时不能伪造下一页状态。

站点无法可靠判断总页数或下一页状态时必须保留 `null`，不得使用 `0`、`1`、`1000` 等固定值伪造信息。普通页码列表仍必须验证
page 1 和 page 2 的实际内容不同且无交集；keyset 站点必须验证首批与使用 `nextCursor` 的后续批次不同且无交集，不能把
HTTP 200 或被忽略的 `page` 参数当作分页证据。

`Picture.id` 和 `Picture.parentId` 使用字符串表达站点原始 ID，缺失时为空字符串；不要再为非数字站点额外增加
ID 字段。`FileNameInfo.id` 同样使用字符串类型，文件名中的 ID 允许 `\w` 字符；
`PictureParser.onItemRevise` 对空 ID 使用 `"0"` 兼容占位值，非空 ID 原样保留。

图片画集摘要使用 `Picture.poolSummary` 和 `PoolSummary` 表达；摘要数量缺失时保持 `null`，普通帖子不应伪造画集身份。

## 多站点聚合

[`MultiParser.kt`](src/commonMain/kotlin/ciyin/parser/core/MultiParser.kt)
会并发执行启用的解析器，并把成功结果交给领域聚合器。通用领域聚合器包括：

- [`PictureMultiParser.kt`](src/commonMain/kotlin/ciyin/parser/core/picture/PictureMultiParser.kt)；
- [`ComicMultiParser.kt`](src/commonMain/kotlin/ciyin/parser/core/comic/ComicMultiParser.kt)；
- [`MovieMultiParser.kt`](src/commonMain/kotlin/ciyin/parser/core/movie/MovieMultiParser.kt)。

```kotlin
import ciyin.parser.core.MultiParserEvent
import ciyin.parser.core.picture.PictureMultiParser
import kotlinx.coroutines.flow.collect

val multiParser = PictureMultiParser(
    parsers = listOf(parserA, parserB),
    enabledParserIds = listOf(parserA.configure.id, parserB.configure.id),
)

multiParser.request(request).collect { event ->
    when (event) {
        is MultiParserEvent.Success -> use(event.result)
        is MultiParserEvent.Failure -> handle(event.errors)
    }
}
```

普通图片结果优先按非空 `md5` 去重；空白 `md5` 时按规范化站点和原始字符串帖子 ID
去重，缺少任一回退身份字段则明确抛错。两类身份互不混用。`CollectionList` 结果仍按规范化站点和正数画集
ID 去重。没有启用解析器时发送带请求页码的默认成功结果；存在启用解析器但全部失败时发送
`MultiParserEvent.Failure`，内部兜底结果不会作为成功事件发送；部分站点失败时仍合并成功结果。

聚合总页数只有全部成功站点都已知时才取最大值；任一站点未知则保持 `null`
，避免已知站点末页提前截断未知站点的续页。单站总页数保持原值。

## 平台差异与生命周期

- `commonMain`：Parser DSL、通用模型、请求/响应封装、领域父类和聚合器。
- `androidMain`：OkHttp 默认引擎及 WebView 动态页面请求实现。
- `desktopMain`：OkHttp 默认引擎。
- `iosMain`：Darwin 默认引擎。
- `androidDeviceTest`：Android WebView 引擎相关测试。
- `commonTest`：分页模型、请求模型、请求 URL、画集摘要和聚合规则测试。

`build.gradle.kts` 为 `webMain` 声明了 CIO 依赖，但当前模块源码中未发现对应的 `ParserEngine` actual
实现；因此本文只确认上述已有 source set，不将 Web 目标支持视为已验证能力。

解析器不会创建应用级全局作用域。调用方负责持有解析器实例、收集返回的 `Flow`，并管理调用所在的协程生命周期。需要替换站点基础
URL 时，具体解析器可以使用 `BaseParser`/领域父类提供的 `baseUrlOverride` 构造参数，以支持镜像端点和离线契约测试。

## 扩展边界与常见误用

- 新增具体站点时应放入 [`:feature:parser-site`](../parser-site)，不要把站点 DTO、selector 或 URL
  规则加入本模块。
- `id` 不能保持 `EmptyParserId`，`baseUrl` 不能为空；否则解析器初始化会失败。
- `response { result -> ... }` 必须返回修订后的结果，不要只修改临时变量。
- 请求参数和请求头在 request DSL 中构建，不要在 response DSL 中补发请求信息。
- 不要为某个站点临时页面新增通用 `ParserType`；先确认它是否属于已有业务动作。
- 不要把 cursor/keyset 站点伪装成普通 page 分页；需要新分页能力时先设计兼容的通用模型。
- 不要用空列表、空字符串或固定总页数掩盖上游接口或 selector 变化；应发送明确失败事件或抛出异常。

## 源码布局

- `src/commonMain/kotlin/ciyin/parser/core`：Parser 基类、领域父类、聚合器、请求构建和事件模型。
- `src/commonMain/kotlin/ciyin/parser/model`：请求、结果、分页、标签、媒体和文件名相关通用模型。
- `src/commonMain/kotlin/ciyin/parser/scope`：Parser/Type/Request/Response DSL 作用域。
- `src/commonMain/kotlin/ciyin/parser/util`：领域作用域别名和文件名解析辅助函数。
- `src/commonMain/kotlin/ciyin/parser/core/engine`：跨平台 HTTP 引擎 expect/actual 定义。
- `src/androidMain`、`src/desktopMain`、`src/iosMain`：平台 HTTP 引擎实现；Android 另含 WebView 实现。
- `src/commonTest`：通用模型、请求构建和聚合契约测试。
- `src/androidDeviceTest`：Android WebView 测试。

## 测试与验证

优先运行最窄的模块任务：

```text
.\gradlew.bat :feature:parser:compileCommonMainKotlinMetadata --console=plain
.\gradlew.bat :feature:parser:desktopTest --console=plain
```

如果改动涉及 Android WebView，再运行对应的 `:feature:parser:connectedAndroidTest` 或项目约定的
Android device test 任务。当前 Windows 环境曾出现 Gradle 进程无输出并以退出码 `3221225794`
结束的情况；遇到该现象时只能记录环境阻塞，不能将编译或测试描述为已通过。

## 相关资料

- [模块 skill](../../.agents/skills/feature-parser/SKILL.md)
- [具体站点模块](../parser-site)
- [项目架构说明](../../.docs/contributing/architecture.md)
- [`settings.gradle.kts`](../../settings.gradle.kts)
