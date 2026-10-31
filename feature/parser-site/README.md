# `:feature:parser-site`

## 用途与非目标

`feature/parser-site` 是 NomiKit 的具体站点解析器模块，负责把图片、漫画和影视站点的 HTML/JSON 响应转换为
`feature/parser` 定义的通用模型：`Picture`、`Comic` 和 `Movie`。

本模块负责站点协议、请求路径、查询参数、CSS selector、站点 DTO 和字段映射；不负责通用解析
DSL、跨站点结果聚合规则、页面 UI 或产品层业务编排。通用解析器抽象位于 [`:feature:parser`](../parser)
模块。

## 模块位置与依赖边界

本模块位于 `feature` 层，是一个 Kotlin Multiplatform 叶子模块。模块路径由根目录 [
`settings.gradle.kts`](../../settings.gradle.kts) 声明，构建配置位于 [
`build.gradle.kts`](build.gradle.kts)。

`commonMain` 当前依赖：

- [`:core:io`](../../core/io)：文件名、文件路径等跨平台 IO 辅助能力；
- [`:core:platform`](../../core/platform)：平台时间和日志等能力；
- [`:feature:parser`](../parser)：Parser DSL、请求/响应模型、图片/漫画/影视通用模型。

站点实现可以被上层业务或聚合入口依赖，但不应反向把具体站点 DTO、selector 或 URL 规则放入
`feature/parser`。

## 从哪里开始

- [`SiteId.kt`](src/commonMain/kotlin/ciyin/parser/site/SiteId.kt)：图片、漫画和影视站点
  ID。未实现站点仍保留在枚举中，便于持久化配置和后续接入。
- [`ParserFactory.kt`](src/commonMain/kotlin/ciyin/parser/site/ParserFactory.kt)：根据站点 ID
  创建具体解析器，并提供三类领域的多站点聚合入口。
- [`BaseBooruParser.kt`](src/commonMain/kotlin/ciyin/parser/site/picture/BaseBooruParser.kt)：Booru
  族图片站点的共用请求和解析骨架。
- [
  `HanimeComicBaseParser.kt`](src/commonMain/kotlin/ciyin/parser/site/comic/HanimeComicBaseParser.kt)
  与 [
  `HanimeMovieBaseParser.kt`](src/commonMain/kotlin/ciyin/parser/site/movie/HanimeMovieBaseParser.kt)
  ：Hanime 漫画和影视站点的共用解析骨架。

当前已接入的具体实现包括 Danbooru、Gelbooru、Yande、Hypnohub、Xbooru、Safebooru、Zerochan、Hanime Comic 和
Hanime Movie；未实现的站点 ID 仍由工厂分支显式抛出未实现错误，不使用 `TODO()` 静默占位。

## 最小接入路径

业务层通常通过工厂创建聚合解析器，并只启用已经实现的站点：

```kotlin
import ciyin.parser.core.MultiParserEvent
import ciyin.parser.core.picture.PictureParserType
import ciyin.parser.core.picture.model.PictureRequest
import ciyin.parser.site.PictureMultiParser
import ciyin.parser.site.PictureSiteId
import kotlinx.coroutines.flow.collect

val parser = PictureMultiParser(
    siteIds = listOf(PictureSiteId.Danbooru, PictureSiteId.Yande),
    enabledSiteIds = listOf(PictureSiteId.Danbooru, PictureSiteId.Yande),
)

val request = PictureRequest(
    type = PictureParserType.Feed,
    page = 1,
    tags = listOf("landscape"),
)

parser.request(request).collect { event ->
    when (event) {
        is MultiParserEvent.Success -> {
            val pictures = event.result.contents
            val page = event.result.pagination.page
        }
        is MultiParserEvent.Failure -> {
            val errorsBySite = event.errors
        }
    }
}
```

站点列表中的每一项都会经过对应的 `factory()`
；不要把尚未实现的枚举项传入聚合器。直接使用单站点解析器时，可参考 [
`DanbooruParser.kt`](src/commonMain/kotlin/ciyin/parser/site/picture/DanbooruParser.kt) 和 [
`HanimeComicParser.kt`](src/commonMain/kotlin/ciyin/parser/site/comic/HanimeComicParser.kt)。

## 业务动作与分页边界

通用动作由 `feature/parser` 定义，不按站点页面名称扩展：

- 图片：`Feed`、`Detail`、`CollectionList`、`Collection`、`Ranking`；
- 漫画：`Feed`、`Detail`、`Chapter`；
- 影视：`Feed`、`Detail`。

请求模型使用从 `1` 开始的 `page`，不包含通用 `pageSize` 或 `cursor`。结果通过 `pagination.page`、可空的
`pagination.totalPages` 和可空的 `pagination.hasNext` 表达分页状态；站点无法可靠判断总页数时必须保留
`null`，不能填充固定值。

列表解析器必须把请求页码映射到站点真实分页参数。仅 HTTP 200 或请求中出现 `page` 参数不能证明分页有效；应验证
page 1 和 page 2 的内容非空、集合不同且没有交集。Danbooru 的离线 DSL 契约测试覆盖了这一判据，见 [
`DanbooruParserDslContractTest.kt`](src/desktopTest/kotlin/ciyin/parser/site/picture/DanbooruParserDslContractTest.kt)
。需要 keyset/cursor 的站点不能被当前页码模型伪装接入，应先扩展通用分页能力模型。

## 源码布局

- `src/commonMain/kotlin/ciyin/parser/site/SiteId.kt`：站点身份枚举。
- `src/commonMain/kotlin/ciyin/parser/site/ParserFactory.kt`：站点工厂和多站点入口。
- `src/commonMain/kotlin/ciyin/parser/site/picture`：图片站点实现、Booru 基类和图片站点请求契约。
- `src/commonMain/kotlin/ciyin/parser/site/comic`：漫画站点实现和 Hanime 漫画基类。
- `src/commonMain/kotlin/ciyin/parser/site/movie`：影视站点实现和 Hanime 影视基类。
- `src/commonMain/kotlin/ciyin/parser/site/util`：站点层复用的序列化和工具函数。
- `src/commonTest`：不依赖网络的站点 ID、工厂和数据映射契约测试。
- `src/desktopTest`：桌面 JVM 本地 HTTP 服务契约测试，验证真实 DSL 注册、请求和响应链路。
- `src/androidDeviceTest`：需要 Android 设备或真实站点环境的测试和请求日志测试。

## 平台差异与生命周期

站点解析器和 DTO 位于 `commonMain`，使用 KMP 共享逻辑；本模块没有单独的 `androidMain`、`desktopMain` 或
`iosMain` 站点实现。平台测试分别位于 `desktopTest` 和 `androidDeviceTest`。

解析器的请求通过 `feature/parser` 的 Ktor/DSL 抽象执行。调用方负责提供和管理解析请求所需的解析器实例及协程生命周期；本模块不创建全局作用域，也不负责
UI 生命周期。

## 依赖与扩展边界

新增站点时按以下顺序修改：

1. 在 [`SiteId.kt`](src/commonMain/kotlin/ciyin/parser/site/SiteId.kt) 对应领域加入稳定的 `site`
   字符串。
2. 在对应的 `picture`、`comic` 或 `movie` 目录新增 parser 实现。
3. 在 [`ParserFactory.kt`](src/commonMain/kotlin/ciyin/parser/site/ParserFactory.kt) 增加
   `factory()` 分支；暂未实现的 ID 保留枚举项并抛出明确错误。
4. 站点结构相同时复用 `BaseBooruParser` 或 Hanime 基类；结构差异较大时新增独立 parser，不把特殊
   selector 塞进通用基类。
5. 增加不依赖网络的基础契约测试；列表分页还要验证 page 1/page 2 的跨页去重。

站点 DTO 应保持在具体 parser 或站点层契约文件中，并使用 `@Serializable`、`@SerialName`
对齐远端字段。公共请求模型和通用结果聚合规则应回到 [`:feature:parser`](../parser) 设计，而不是在本模块复制一份。

## 限制与常见误用

- 不要把未实现站点从枚举删除；持久化配置可能依赖稳定的 `site` 标识。
- 不要把未实现站点传给 `PictureMultiParser`、`ComicMultiParser` 或 `MovieMultiParser`；工厂会明确抛错。
- 不要在请求中添加通用 `pageSize` 或 `cursor`，也不要用 `totalPages = 1000` 等固定值掩盖未知元数据。
- 不要只断言 HTTP 200、`totalPages` 或 query 中存在 `page`；分页必须有跨页内容差异和无交集证据。
- 不要把站点 selector、远端字段和 URL 规则复制到通用 parser 模块。
- 需要真实网络的测试不应放入普通 `commonTest`，避免测试依赖外部站点稳定性。

## 测试与验证

优先运行最窄的模块验证任务：

```text
.\gradlew.bat :feature:parser-site:compileCommonMainKotlinMetadata --console=plain
.\gradlew.bat :feature:parser-site:desktopTest --console=plain
```

当前 Windows 环境曾出现 Gradle 进程无输出并以退出码 `3221225794`
结束的情况；遇到该现象时只能记录环境阻塞，不能将编译或测试描述为已通过。

## 相关资料

- [模块 skill](../../.agents/skills/feature-parser-site/SKILL.md)
- [通用 parser skill](../../.agents/skills/feature-parser/SKILL.md)
- [项目架构说明](../../.docs/contributing/architecture.md)
- [`:feature:parser` 模块](../parser)
