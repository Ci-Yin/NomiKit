---
name: file-downloader
description: Use the feature/file-downloader Kotlin Multiplatform module (package ciyin.file.downloader) for single-file downloads, resume, chunked downloads, throttling, retries, queues, and concurrency control. Covers FileDownloader, DownloadManager, DownloadConfig, DownloadState, Okio paths, platform engines, cleanup, and deterministic MockEngine plus FakeFileSystem tests. Use when 用户要在 NomiKit 中新增或维护文件下载、断点续传、分块下载、下载队列，或排查该模块行为与构建问题。
---

# File Downloader 使用指南

`feature/file-downloader` 提供跨平台单任务下载器与多任务管理器。公共包名统一为 `ciyin.file.downloader`，文件 API 使用 Okio `Path` / `FileSystem` 和 `ciyin.io.SystemFileSystem`。

## 创建与监听单任务

```kotlin
val downloader = fileDownloader(scope)
val config = DownloadConfig(
    url = "https://example.com/archive.zip",
    savePath = targetPath.toString(),
    enableResume = true,
    maxRetries = 3,
)

scope.launch {
    downloader.state.collect { state ->
        when (state) {
            is DownloadState.Downloading -> updateProgress(state.progress, state.speed)
            is DownloadState.Complete -> openFile(state.filePath)
            is DownloadState.Error -> showError(state.message)
            else -> Unit
        }
    }
}
downloader.download(config)
```

控制操作统一调用 `FileDownloader`：`pause()`、`resume()`、`cancel()`、`restart()`、`idle()`、`cleanup()`。`download`、`resume`、`restart` 返回 `Job`；长期持有下载器时必须在宿主销毁阶段调用 `cleanup()`。

## 配置与状态

`DownloadConfig` 的关键选项：

- `enableResume`：通过 `.tmp` 文件与 `Range` 请求续传；分块模式下忽略。
- `enableChunkedDownload`、`chunkSize`、`maxConcurrentChunks`：服务器支持 Range 且文件不小于分块大小时启用并行分块。
- `overwriteExisting`：正式目标已存在时是否覆盖；`restart()` 会先清理旧文件。
- `maxRetries`、`retryDelayMs`：单连接请求重试策略。
- `chunkMaxRetries`、`chunkRetryDelayMs`：单个分块重试策略。
- `speedLimitBytesPerSecond`：整个任务的字节每秒上限，`0` 表示不限速。
- `enableDownloadProgress`、`progressUpdateInterval`：进度和速度状态的上报策略。

状态顺序通常为 `Idle -> Start -> Downloading -> Complete/Error`。暂停与恢复分别发布 `Paused`、`Resumed`，取消发布 `Cancelled`；完成路径类型是 `okio.Path`。

## 队列与并发

```kotlin
val manager = downloadManager(maxConcurrentDownloads = 3, scope = scope)
manager.addToQueue(config, priority = 10)
manager.startQueue()

scope.launch {
    manager.taskEvents.collect { event -> handleTaskEvent(event) }
}
```

`DownloadManager` 负责优先队列、手动启动、暂停、恢复、取消、重启与最大并发控制。用 `taskEvents` 消费 `DownloadTaskState`，用 `queueState` 观察待执行任务；宿主销毁时调用 `cleanup()`。

## Okio 与平台约束

- 公共源码不得重新引入 `kotlinx-io`、`java.io.File` 或手写路径分隔符。
- 正式文件、临时文件和分块目录均通过同一个 `FileSystem` 操作；清理扩展允许显式传入 `FileSystem`。
- JVM 与 Android 复用 `jvmMain` 的 OkHttp actual，iOS 使用 Darwin actual。
- 模块当前不提供 Web actual；启用 Web 目标前必须补齐引擎和平台实现。
- 生产入口使用 `fileDownloader()`，不要从模块外直接依赖内部 `CommonFileDownloader`。

## 确定性测试

内部测试可构造 `CommonFileDownloader` 并注入 `HttpClient(MockEngine)` 与 `FakeFileSystem`。至少验证：

- 200 响应生成完整正式文件与 `Complete`。
- 已有 `.tmp` 文件时请求头包含正确 Range，206 内容被追加。
- `overwriteExisting=false` 不发请求且保留旧文件，开启后替换。
- 非成功状态产生 `Error`，不依赖外网或系统临时目录。
- 分块计算、临时文件清理和 `DownloadManager` 并发上限保持单测覆盖。

最窄验证命令：

```powershell
.\gradlew.bat :feature:file-downloader:desktopTest
.\gradlew.bat :feature:file-downloader:compileAndroidMain :feature:file-downloader:compileKotlinIosSimulatorArm64
```
