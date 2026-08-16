---
name: feature-parser
description: Use the feature/parser Kotlin Multiplatform parser DSL library (package ciyin.parser.*). Covers BaseParser, PictureParser/ComicParser/MovieParser, ParserScope/TypeScope request-response DSL, HttpRequestBuilder.url, ResponseScope HTML/JSON helpers, ParserEvent/MultiParserEvent, MultiParser aggregation, parser IDs/types/models, and filename helpers. Use when 用户要在 NomiKit 中使用或扩展 feature/parser、写通用解析器 DSL、调用 Picture/Comic/Movie parser、聚合多解析器、解析文件名，或排查 parser 模块 API 使用问题。
---

# feature/parser 使用指南

`feature/parser` 是 NomiKit 的通用站点解析 DSL 与模型层。它负责抽象解析器骨架、请求/响应 DSL、图片/漫画/影视三类通用模型，以及多站点聚合执行；不要在这里放具体站点实现，具体站点放到 `feature/parser-site`。

优先从 `ciyin.parser.*` 包使用本模块提供的类型，不要在业务代码里绕过 `BaseParser` 自己拼网络请求和解析流程。

## 模块边界

- 通用抽象：`BaseParser<TType, TRequest, TResult>`、`ParserType`、`ParserRequest`、`ParserResult`、`ParserId`。
- 类型 DSL：`ParserScope` 配站点，`TypeScope` 配某个解析类型，`RequestScope` 配 `html/json/xml` 请求，`ResponseScope` 读取响应并解析结果。
- 领域骨架：`PictureParser`、`ComicParser`、`MovieParser` 以及对应的 `PictureRequest`、`ComicRequest`、`MovieRequest`、`PictureResult`、`ComicResult`、`MovieResult`。
- 图片画集摘要：`Picture.poolSummary` 使用可空不可变 `PoolSummary(poolId, title, postCount, url)`；普通帖子保持 `null`，远端未返回数量时 `postCount` 必须保持 `null`，不得伪造为 `0`。
- 聚合器：`MultiParser` 负责并发执行启用的解析器，具体使用 `PictureMultiParser`、`ComicMultiParser`、`MovieMultiParser`。
- 站点实现不属于本模块；新增 Danbooru/Yande/Hanime 等具体站点时看 `feature-parser-site` skill。

## 写解析器 DSL

继承对应领域父类，在 `setup` 中设置 `id`、`baseUrl`，再通过 `on(type)` 注册请求和响应解析。

```kotlin
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

        on(PictureParserType.Posts) {
            request { req ->
                val parameters = parametersOf(
                    "page" to req.page,
                    "tags" to req.tags.joinToString(" "),
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
```

注意事项：

- `id` 不能是 `EmptyParserId`，`baseUrl` 不能为空；否则解析器初始化会失败。
- 需要离线契约测试、镜像站点或显式替代端点时，让具体解析器构造参数传入 `PictureParser(baseUrlOverride)`；`BaseParser` 会在 `setup` 执行前保存该值，避免子类属性尚未初始化的时序问题。
- `on(type)` 中至少注册一个 `html`、`json` 或 `xml` 请求；否则 `request(...)` 时会报“未注册任何请求”。
- `html/json/xml` 默认 key 分别是 `ResultType.Html/Json/Xml`；同一 key 重复注册会覆盖前一次。
- `response { result -> ... }` 必须返回新的或修订后的 `TResult`，不要只修改临时变量。
- HTML 用 `document` 或 `bodyForHtml()`；JSON 用 `bodyForJson<T>()` 或 `bodyForJson()`；XML 解析目前 `bodyForXml<T>()` 仍是 TODO，不要假设可用。
- JSON 目标类型必须能被 `kotlinx.serialization` 处理，站点 DTO 通常用 private `@Serializable data class`。

## 构建请求 URL

`HttpRequestBuilder.url` 会基于当前 `baseUrl` 生成请求地址。

```kotlin
html {
    url(
        path = "/index.php",
        parameters = parametersOf(
            "page" to "post",
            "tags" to req.tags.joinToString(" "),
        ),
    )
}
```

注意事项：

- 单值参数用 `parametersOf("page" to req.page)`；多值参数用 `url(path, mapOf("tag" to listOf("a", "b")))`。
- 需要自定义 pathSegments、encodedPath 或复杂 query 时，使用 `url { ... }` 的 `URLBuilder` DSL。
- `HttpRequestBuilder.url` 会保留 `baseUrl` 的非默认端口；本地离线服务和镜像端点不得另行手拼端口。
- 请求头用 `header(name, value)` 或 `headers(...)`，不要在响应解析阶段补请求信息。

## 调用解析器

单站点解析返回冷 `Flow<ParserEvent<TResult>>`，每次 collect 会发起一次解析。

```kotlin
parser.request(
    PictureRequest(
        type = PictureParserType.Posts,
        page = 1,
        tags = listOf("qys3"),
    )
).collect { event ->
    when (event) {
        is ParserEvent.Success -> use(event.result)
        is ParserEvent.Failure -> handle(event.errors)
    }
}
```

注意事项：

- `ParserEvent.Failure.errors` 来自请求阶段收集到的异常；不要静默吞掉。
- `enable = false` 时不会发起网络请求，会返回该 parser 的默认结果。
- `BaseParser` 内部并发执行同一类型注册的多路请求；调用侧不需要再为 html/json 请求手动并发。

## 多解析器聚合

`MultiParser` 会按 `enabledParserIds` 过滤站点，并发请求所有启用 parser，然后合并成功结果。

```kotlin
val multiParser = PictureMultiParser(
    parsers = listOf(parserA, parserB),
    enabledParserIds = listOf(parserA.configure.id, parserB.configure.id),
)
```

注意事项：

- 普通图片聚合按 `md5` 去重；`PictureParserType.Pools` 按规范化站点 + 正数 `poolId` 去重，使同站点同画集合并、跨站点相同 ID 共存，空封面的多个画集也不会互相吞掉。画集缺少站点、摘要或正数 `poolId` 时应明确失败，不能伪造 `md5`。
- 漫画/影视聚合以对应 `MultiParser` 实现为准。
- 全部失败或无启用 parser 时返回 `onFallback()` 的默认结果。
- `MultiParserEvent.Failure` 的 key 是失败站点的 `ParserId`，适合向上层做明确错误提示或降级。

## 文件名辅助

`FileNameParser` 约定文件名格式为 `site_id_md5.ext`。

```kotlin
val info = "danbooru_123_abcd.jpg".toFileName()
val valid = "danbooru_123_abcd.jpg".isValidFileName()
val name = FileNameInfo("danbooru", 123, "abcd", "jpg").buildFileName()
```

注意事项：

- 正则只接受 `\w+` 站点、数字 id、`\w*` md5 和 `\w+` 扩展名；带连字符等字符会解析失败。
- `PictureParser.onItemRevise` 已经会按站点、id、md5、扩展名补全常见图片文件名，站点实现优先复用它。

## 代码约定

- 新增 Kotlin API 遵守项目规则补中文 KDoc。
- 公共模型保持不可变 `data class` + 默认值，避免把网络 DTO 泄露到通用模型。
- 画集列表可用 `Picture` 承载首图与 `PoolSummary`，但摘要 ID、标题、数量和详情 URL 必须来自站点权威数据；封面缺失不能导致摘要条目丢失。
- 错误要转成明确事件或向上抛出，不要用空列表、空字符串掩盖上游解析问题。
- 修改本模块后优先运行 `.\gradlew.bat :feature:parser:compileCommonMainKotlinMetadata --console=plain`。
