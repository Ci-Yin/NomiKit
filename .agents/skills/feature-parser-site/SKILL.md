---
name: feature-parser-site
description: Use the feature/parser-site Kotlin Multiplatform concrete site parser module (package ciyin.parser.site.*). Covers PictureSiteId/ComicSiteId/MovieSiteId, ParserFactory, Danbooru/Gelbooru/Yande/Xbooru/Safebooru/Hypnohub/Zerochan/Hanime parser patterns, BaseBooruParser reuse, site DTO mapping, factory wiring, and parser-site tests. Use when 用户要在 NomiKit 中使用或新增具体站点解析器、调整站点 ID、通过 ParserFactory 构建多站点 parser、修改 Danbooru/Yande/Hanime 等站点实现，或排查 feature/parser-site 行为。
---

# feature/parser-site 使用指南

`feature/parser-site` 是 NomiKit 的具体站点解析器集合。它依赖 `feature/parser` 的通用 DSL，负责把 Danbooru、Yande、Hanime 等站点的 HTML/JSON 结构映射成通用 `Picture`、`Comic`、`Movie` 模型。

需要写通用解析 DSL、请求/响应作用域或聚合器规则时先看 `feature-parser` skill；本 skill 只覆盖具体站点层。

## 模块边界

- 站点 ID：`PictureSiteId`、`ComicSiteId`、`MovieSiteId`，每个枚举实现对应领域的 `ParserId`。
- 工厂入口：`ParserFactory.kt` 把站点 ID 映射为具体 `PictureParser`、`ComicParser`、`MovieParser`，并提供 `PictureMultiParser(...)`、`ComicMultiParser(...)`、`MovieMultiParser(...)` 便捷构造。
- 图片站点：`picture/*`，其中 Gelbooru/Xbooru/Safebooru/Hypnohub 等 Booru 族站点优先复用 `BaseBooruParser.superSetup()`。
- 漫画/影视站点：`comic/*`、`movie/*`，Hanime 族通用逻辑分别在 `HanimeComicBaseParser` 与 `HanimeMovieBaseParser`。
- 不要把具体站点 DTO、CSS selector、站点 URL 规则放回 `feature/parser`。

## 使用站点工厂

业务层通常通过站点 ID 构造聚合解析器，而不是直接依赖每个站点类。

```kotlin
val parser = PictureMultiParser(
    siteIds = PictureSiteId.entries,
    enabledSiteIds = listOf(PictureSiteId.Danbooru, PictureSiteId.Yande),
)
```

也可以从通用 `ParserId` 列表恢复到站点枚举：

```kotlin
val parser = PictureMultiParser(
    siteIds = savedSiteIds,
    enabledSiteIds = enabledSiteIds,
)
```

注意事项：

- `ParserFactory.kt` 中仍有部分 `TODO()` 站点；启用前必须先实现对应 parser，否则构造时会直接失败。
- `asSites` 会按 `site` 字符串匹配枚举，持久化站点配置时保持 `site` 稳定。
- 新增站点后同时更新站点枚举和对应 `factory()` 分支。

## 新增图片站点

普通站点继承 `PictureParser`；Booru 兼容站点优先继承 `BaseBooruParser` 并复用 `superSetup()`。

```kotlin
class ExampleBooruParser : BaseBooruParser() {
    override fun PictureParserScope.setup() {
        id = PictureSiteId.Example
        baseUrl = "https://example.com"
        superSetup()
    }
}
```

独立站点按现有 Danbooru/Yande 模式写：

```kotlin
class ExamplePictureParser : PictureParser() {
    override fun PictureParserScope.setup() {
        id = PictureSiteId.Example
        baseUrl = "https://example.com"

        onItemRevise {
            copy(
                site = configure.id.site,
                sampleUrl = sampleUrl.ifBlank { originalUrl },
            )
        }

        on(PictureParserType.Posts) {
            request { req ->
                val parameters = parametersOf("page" to req.page)
                html { url("/posts", parameters) }
                json { url("/posts.json", parameters) }
            }

            response { result ->
                result.copy(
                    tags = document.select(".tag").map { Tag(tag = it.text()) },
                    contents = bodyForJson<List<ExamplePost>>().map { it.toPicture() },
                )
            }
        }
    }
}
```

注意事项：

- 站点 DTO 放在 parser 类内部，默认 `private @Serializable data class`，用 `@SerialName` 对齐远端字段。
- 远端 JSON 字段类型不稳定时，优先复用本模块已有工具，例如 `NumberAsStringSerializer`。
- `Picture` 映射要尽量补齐 `id`、`site`、`originalUrl/sampleUrl/thumbnailUrl`、`postUrl`、`md5`、`fileExt`、`width/height`、`rating`、`tags`、时间戳。
- 图片结果修订优先放在 `onItemRevise`，让文件名、站点、URL 补全规则集中处理。
- 过滤不支持媒体类型时要在站点解析层明确处理，例如 Yande 当前过滤 `video`、`animated`、`animated_gif`。
- Danbooru/Yande 的 `Pools` 列表以 JSON 摘要作为画集集合、ID、标题和可空数量的权威来源，再合并 HTML/内嵌脚本首图；无封面条目仍须保留，`post_count` 缺失保持 `null`。
- Danbooru 通过 HTML 详情链接中的 pool ID 合并封面；Yande 列表 HTML 未提供 pool ID 时按 JSON 权威顺序关联首图。单个 `Pool` 详情继续解析帖子列表，不复用摘要数组解析器。
- Danbooru 的真实 DSL 注册必须保持 `Pools -> parsePoolsResult`、`Pool -> parsePoolResult`；离线测试要穿过 `request()` 的注册、请求与响应全链，不能只调用解析 helper。
- Danbooru 画集搜索参数使用原始 key `search[name_matches]` 交给 URLBuilder 编码；不要预编码为 `%5B/%5D`，否则最终 URL 会出现 `%255B/%255D`。
- Danbooru/Yande 热门榜单仅支持 `day`、`week`、`month`，路径和参数必须按站点显式映射；未知范围应明确失败，不能静默落到其他页面。
- Yande 热门 page 1 的 `day` 使用当天；后续页中 `day` 用 `DatePeriod(days = page - 1)`、`week` 用 `DatePeriod(days = 7 * (page - 1))`、`month` 用 `DatePeriod(months = page - 1)` 向前推进。测试通过显式 `LocalDate` 固定今天，生产入口再注入当前本地日期，避免时钟脆弱。

## 新增漫画或影视站点

漫画站点继承 `ComicParser` 或已有 `HanimeComicBaseParser`，影视站点继承 `MovieParser` 或 `HanimeMovieBaseParser`。

```kotlin
class ExampleComicParser : ComicParser() {
    override fun ComicParserScope.setup() {
        id = ComicSiteId.Example
        baseUrl = "https://example.com"
        onItemRevise { copy(site = configure.id.site) }
        on(ComicParserType.Posts) {
            request { req -> html { url("/comics", parametersOf("page" to req.page)) } }
            response { result -> result.copy(contents = document.select(".item").map { it.toComic() }) }
        }
    }
}
```

注意事项：

- 如果新站点与 Hanime 结构相同，只在子类中设置 `id`、`baseUrl` 并调用已有 base setup。
- 如果站点结构不同，不要把站点特殊 selector 硬塞进 Hanime base 类，另建清晰的 base parser 或独立 parser。
- `ComicParserType`、`MovieParserType` 只表示通用解析流程；不要为单个站点的临时页面随意新增通用类型。

## 工厂接入清单

新增站点后按这个顺序检查：

1. 在 `SiteId.kt` 的对应枚举中加入稳定 `site` 字符串。
2. 在对应目录新增 parser 实现。
3. 在 `ParserFactory.kt` 的对应 `factory()` 分支返回新 parser，移除相关 `TODO()`。
4. 如果要提供聚合入口，确认 `PictureMultiParser/ComicMultiParser/MovieMultiParser` 能从站点 ID 列表构造。
5. 增加至少一个不依赖网络的基础契约测试，验证 `configure.id`、`configure.baseUrl`、默认结果和 `enable`。

## 测试与验证

- 不依赖网络的契约测试放 `feature/parser-site/src/commonTest`。
- 依赖真实站点网络或 Android WebView 的测试放 `androidDeviceTest`，不要让普通 `commonTest` 依赖外网稳定性。
- 调整站点实现后优先运行 `.\gradlew.bat :feature:parser-site:compileCommonMainKotlinMetadata --console=plain`。
- 新增或改动基础契约测试后再运行 `.\gradlew.bat :feature:parser-site:desktopTest --console=plain`，若测试包含真实网络访问，需要在回复中说明风险。

## 代码约定

- 新增 Kotlin API 遵守项目规则补中文 KDoc。
- 站点解析失败时保留明确异常或失败事件，不要用空结果悄悄掩盖 selector/API 变化。
- URL、CSS selector、正则等站点知识尽量靠近对应 parser 私有函数，避免污染通用 DSL。
- 复用 `ciyin.parser.core.parametersOf`、`ciyin.parser.core.url`、`ResponseScope.bodyForJson<T>()`、`document` 等项目封装。
