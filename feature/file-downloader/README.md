# `:feature:file-downloader`

## 用途与非目标

`feature:file-downloader` 提供跨平台的文件下载能力，覆盖单任务下载、多任务队列、断点续传、分块并发、
重试、限速、进度状态和临时文件清理。公共 API 位于 `ciyin.file.downloader` 包，调用方只需要提供下载地址、
目标路径和任务配置。

模块不负责以下内容：

- 产品页面、通知、下载历史或业务状态持久化；
- 登录鉴权、业务协议、URL 生成和业务错误到 UI 文案的映射；
- 应用层的任务恢复策略、网络条件判断和依赖组装；
- 替代 `core:io` 的文件抽象，或让调用方直接依赖平台文件 API。

## 模块位置与依赖边界

这是 `feature` 层的独立 Gradle 模块，项目路径为 `:feature:file-downloader`。按照项目依赖方向，应用层或
其他上层 feature 可以依赖它；它只能依赖更底层的公共模块，不能依赖 `app/*`、产品资源或产品专属协议。

当前 `build.gradle.kts` 的依赖边界为：

- `api(projects.core.io)`：公开使用 Okio `Path`、`FileSystem` 和项目文件系统抽象；
- `implementation(projects.core.platform)`：使用项目平台日志和时间能力；
- `implementation(libs.ktor.client.core)`、`ktor.client.logging`：实现跨平台 HTTP 下载流程；
- `jvmMain` 使用 Ktor OkHttp 引擎，`iosMain` 使用 Ktor Darwin 引擎；
- 测试使用 Ktor `MockEngine`、`FakeFileSystem` 和协程测试工具，不依赖外网或真实文件系统。

调用方应在应用或业务层决定产品语义，并通过公共入口传入配置；不要在调用方复制临时文件、Range 请求、
分块合并、重试或错误状态处理。

## 从哪里开始

| 入口                      | 用途                        | 源码                                                                                         |
|-------------------------|---------------------------|--------------------------------------------------------------------------------------------|
| `fileDownloader(scope)` | 创建一个单文件下载器                | [`Downloader.kt`](src/commonMain/kotlin/ciyin/file/downloader/Downloader.kt)               |
| `FileDownloader`        | 控制单任务的开始、暂停、恢复、取消、重启和清理   | [`FileDownloader.kt`](src/commonMain/kotlin/ciyin/file/downloader/core/FileDownloader.kt)  |
| `DownloadConfig`        | 描述 URL、目标路径、续传、重试、限速和分块策略 | [`DownloadConfig.kt`](src/commonMain/kotlin/ciyin/file/downloader/model/DownloadConfig.kt) |
| `DownloadState`         | 对外暴露下载生命周期和进度             | [`DownloadState.kt`](src/commonMain/kotlin/ciyin/file/downloader/core/DownloadState.kt)    |
| `downloadManager(...)`  | 创建带队列、优先级和并发上限的多任务管理器     | [`Downloader.kt`](src/commonMain/kotlin/ciyin/file/downloader/Downloader.kt)               |

生产代码应从 `fileDownloader()` 或 `downloadManager()` 开始，不要从模块外直接依赖内部的
`CommonFileDownloader`。后者注入 HTTP 引擎、`FileSystem` 和 `HttpClient`，只用于模块实现和确定性测试。

## 最小接入路径

单任务的接入顺序是：创建下载器 → 创建 `DownloadConfig` → 订阅 `state` → 调用 `download` → 在宿主销毁阶段
调用 `cleanup`。`download`、`resume` 和 `restart` 返回 `Job`，需要等待任务结束时可以调用 `join()`。

```kotlin
val downloader = fileDownloader()
val config = DownloadConfig(
    url = "https://example.com/archive.zip",
    savePath = targetPath.toString(),
    enableResume = true,
    maxRetries = 3,
)

scope.launch {
    downloader.state.collect { state ->
        when (state) {
            is DownloadState.Downloading -> renderProgress(state.progress, state.speed)
            is DownloadState.Complete -> openFile(state.filePath)
            is DownloadState.Error -> showDownloadError(state.exception)
            else -> Unit
        }
    }
}

val downloadJob = downloader.download(config)
downloadJob.join()
downloader.cleanup()
```

`DownloadState.Downloading` 提供 `progress`、`downloaded`、`total` 和 `speed`；完成状态提供 Okio
`Path`，错误状态同时提供可选消息和原始异常。调用方应把这些状态映射为自己的 UI 或业务状态，不要修改
模块内部状态流。

## 多任务队列

需要管理多个下载任务时，使用 `DownloadManager`，而不是在应用层自行创建并发控制器：

```kotlin
val manager = downloadManager(
    maxConcurrentDownloads = 3,
    scope = managerScope,
)

manager.addToQueue(config, priority = 10)
manager.startQueue()

managerScope.launch {
    manager.taskEvents.collect { event -> handleTaskEvent(event) }
}
```

`addToQueue` 按优先级维护队列，任务 ID 来自 `DownloadConfig.id`。`startQueue` 按
`maxConcurrentDownloads` 调度队列，`taskEvents` 汇报任务创建、入队、开始、状态变化、暂停、恢复、取消和
完成事件，`queueState` 提供当前队列快照。也可以先入队后调用 `start(taskId)`、`pause(taskId)`、
`resume(taskId)`、`restart(taskId)` 或 `cancel(taskId)` 控制单个任务。

队列和手动启动共用同一个并发闸门；不要在外层再包一套会绕过 manager 的并发调度。`DownloadManager` 的
`cleanup()` 应由拥有它的生命周期统一调用，以取消任务和释放下载器资源。

## 源码布局

- `src/commonMain/kotlin/ciyin/file/downloader/Downloader.kt`：公共工厂函数和模块入口文档示例。
- `src/commonMain/kotlin/ciyin/file/downloader/core/`：`FileDownloader`、`DownloadManager`、单任务状态和
  队列事件模型。
- `src/commonMain/kotlin/ciyin/file/downloader/model/`：`DownloadConfig` 和队列任务模型。
- `src/commonMain/kotlin/ciyin/file/downloader/CommonFileDownloader.kt`：单连接、续传、重试、限速和分块合并的
  共享实现；该实现为内部 API。
- `src/commonMain/kotlin/ciyin/file/downloader/ChunkCalculator.kt`：分块范围和临时文件计算。
- `src/commonMain/kotlin/ciyin/file/downloader/util/`：临时文件、分块目录和 Okio `FileSystem` 清理扩展。
- `src/commonMain/kotlin/ciyin/file/downloader/exceptions/`：目标已存在、HTTP 状态、Range 偏移和分块失败等
  下载异常。
- `src/jvmMain/`：JVM 与 Android 复用的 OkHttp `actual` 下载器入口。
- `src/iosMain/`：iOS 使用 Darwin 引擎的 `actual` 下载器入口。
- `src/commonTest/`：MockEngine、FakeFileSystem、续传、覆盖策略、临时文件清理、分块计算和并发控制测试。

当前模块没有 `webMain`、JS 或 Wasm 的下载器 `actual` 实现；启用 Web 目标前需要先补齐平台引擎和 source
set
边界，不能把 JVM 的 OkHttp 实现直接搬到 Web。

## 平台差异与生命周期

- `commonMain` 只依赖 Ktor client 抽象和 Okio 文件系统，负责下载状态机、文件写入、响应校验和并发逻辑。
- `jvmMain` 通过 OkHttp 引擎提供实际 HTTP 请求，当前构建约定使 Android 和 Desktop 复用该 JVM source
  set。
- `iosMain` 通过 Darwin 引擎执行网络请求，公共 API 与 JVM 保持一致。
- `fileDownloader(scope)` 可以接收调用方作用域；如果传入作用域，`cleanup()` 会取消该作用域，因此应传入
  下载器专用或可由该下载器拥有的作用域，不要无意中传入整个应用的根作用域。
- `pause()`、`cancel()` 和 `cleanup()` 都会停止当前会话；`cleanup()` 还会关闭内部 HTTP 客户端，并删除残留
  的 `.tmp` 文件和 `.chunks` 目录。

## 下载策略与公共边界

### 单连接与断点续传

- 默认使用单连接流式写入，目标文件先写入 `<savePath>.tmp`，完成后再移动到正式路径。
- `enableResume = true` 时，已有 `.tmp` 文件大小会作为 Range 起点；服务端返回的 `Content-Range` 起点必须
  与本地偏移一致。
- `overwriteExisting = false` 时，正式目标已存在会进入 `DownloadState.Error`，不会发起网络请求；
  `restart()` 会清理旧目标和临时文件后重新下载。
- HEAD 请求无法得到文件大小或 Range 能力时会降级为单连接下载，不能据此假定服务端支持断点续传。

### 分块下载

- `enableChunkedDownload = true` 时，模块按 `chunkSize` 计算范围并使用 `maxConcurrentChunks` 并行下载，
  完成后合并分块文件。
- 分块模式要求服务端支持 Range，并且不支持与 `.tmp` 续传共用；开启分块时 `enableResume` 会被忽略。
- 单个分块失败会按 `chunkMaxRetries` 和 `chunkRetryDelayMs` 重试；最终失败会清理分块临时目录并发布错误。
- `speedLimitBytesPerSecond` 限制整个任务的吞吐，不是每个分块各自的限速。

### 文件和错误模型

- 公共路径使用 Okio `Path` 和项目 `SystemFileSystem`，不要在公共代码中引入 `java.io.File`、
  `kotlinx-io` 或手写路径分隔符。
- HTTP 非成功响应、目标文件冲突、覆盖失败、Range 偏移不一致和分块失败会通过 `DownloadState.Error` 暴露；
  应用层负责把这些技术异常映射为产品语义。
- 临时路径规则是 `<savePath>.tmp` 和 `<savePath>.chunks`。需要在异常退出后清理时，优先复用
  `DownloadConfig.deleteTempFile()` 和 `deleteChunkTempDir()`，不要复制路径拼接逻辑。

## 测试事实

`src/commonTest` 使用 `MockEngine` 和 `FakeFileSystem` 构造确定性场景，当前覆盖：正常响应写入正式文件、已有
`.tmp` 时发送正确 Range、覆盖策略、HTTP 错误、临时文件清理、分块计算，以及队列和手动入口叠加时不超过
`maxConcurrentDownloads`。这些测试是行为边界的事实来源；修改下载策略时应先对照对应测试和实现。

## 相关资料

- [File downloader skill](../../.agents/skills/file-downloader/SKILL.md)
- [Module documentation skill](../../.agents/skills/module-docs/SKILL.md)
- [Architecture](../../.docs/contributing/architecture.md)
- [Kotlin Multiplatform](../../.docs/contributing/kmp.md)
- [Building](../../.docs/contributing/building.md)
