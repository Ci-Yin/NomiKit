---
name: core-platform
description: Use the core/platform Kotlin Multiplatform platform abstraction module (package ciyin.platform). Covers Context/LocalContext/Context.files, ContextFiles/CommonContextFiles, Platform/currentPlatform/currentPlatformDesktop, Arch/PlatformType/SystemProvider/getPlatform, logger/thisLogger/Log, DateTime helpers, TaskSchedule, URI-to-temp-file platform IO, and text/single-file/multi-file system sharing. Use when 用户要在 NomiKit 中获取平台上下文、判断平台、记录日志、使用平台目录抽象、调用系统分享，或维护 core/platform expect/actual。
---

# core/platform 使用指南

`core/platform` 提供 NomiKit 的平台抽象层，包名主要是 `ciyin.platform`。它位于 core 层底部，向上提供 Context、平台识别、日志和时间等基础能力。

## Context 与文件目录

```kotlin
val context = LocalContext.current
val dataDir = context.files.dataDir
val cacheDir = context.files.cacheDir
```

注意事项：

- `Context`、`LocalContext`、`Context.files` 都是 expect/actual；调用侧只消费抽象，不手动判断平台。
- `ContextFiles` 包含 `cacheDir`、`dataDir`、`defaultBaseMediaCacheDir`。
- `CommonContextFiles` 只是通用 data class；平台入口应负责填入正确目录。
- URI 复制到临时文件使用 `ciyin.platform.io.String.copyUriToTempFile(context)`。

## 平台判断

```kotlin
when (val platform = currentPlatform()) {
    is Platform.Android -> ...
    is Platform.Windows -> ...
    is Platform.Ios -> ...
    else -> ...
}
```

注意事项：

- `currentPlatform()` 会缓存 `currentPlatformImpl()` 的 `runCatching` 结果；不支持的平台在取值时抛出。
- `currentPlatformDesktop()` 会检查当前平台必须是 desktop。
- 常用 helper 有 `isAndroid()`、`isWindows()`、`isMacOS()`、`isLinux()`、`isIos()`、`isMobile()`、`isDesktop()`、`is64bit()`、`isAArch()`。
- 不要把平台分支散落在业务层；能通过 expect/actual 或已有抽象解决时优先使用抽象。

## SystemProvider

```kotlin
val provider = platform
val appData = provider.getAppDataDir()
```

注意事项：

- `getPlatform()` 返回 `SystemProvider`，包含系统名、平台类型、Java home、应用数据目录、计划任务、自启动、exe 图标提取等能力。
- 部分能力只在特定平台有真实实现，跨平台调用前先确认 actual 行为。
- `packageName` 有默认值，不要把它当成业务应用 ID 的权威来源。

### Windows exe 图标提取

```kotlin
val outputPng = java.io.File(cacheDirectory, "application.png")
ExeIconExtractor.extractExeIcon(
    executablePath = executablePath,
    outputFile = outputPng,
    size = 256,
)
```

注意事项：

- `ExeIconExtractor` 的输出格式是 PNG；`outputFile` 必须是目标文件路径，不是目录路径。
- 提取器优先通过 `SHDefExtractIconW` 读取最匹配请求尺寸的原始图标，失败时才回退系统大图标，避免先取低分辨率图标再放大。
- 提取器会创建目标文件的父目录，并在调用结束前释放 `HICON`、`HBITMAP` 和设备上下文句柄。
- 文件没有图标、目标尺寸非法或 PNG 写入失败时会抛出异常；上层必须转换成自己的错误模型，不要把失败当成成功缓存。
- 批量尺寸使用 `extractAndSaveExeIcons(...)`，输出文件名为 `<exe-name>_icon_<size>.png`。

## 日志与时间

```kotlin
private val logger = thisLogger()
logger.i { "loaded" }
Log.debug("Tag", "message")
```

注意事项：

- `logger()` / `thisLogger()` 返回 Kermit `Logger.withTag(...)`。
- `Log` 是项目自带的简单打印与 `StateFlow` 日志流，适合轻量调试；结构化日志优先用 Kermit。
- 时间 helper 在 `ciyin.platform.time` 下，提供 `currentTimeMillis()`、`currentTimeSecond()`、`LocalDateTime.format(...)`、`Clock.nowLocal()` 等。

## 系统分享

```kotlin
val result = sharePlatformContent(
    context = context,
    payload = PlatformSharePayload.File(
        value = PlatformShareFile(
            source = PlatformShareFileSource.LocalFile(file),
            mimeType = "image/png",
            displayName = file.name,
        ),
        title = "分享图片",
    ),
)
```

注意事项：

- API 位于 `ciyin.platform.share`，支持 `Text`、单个 `File` 和非空 `Files`。
- `Text`、`File`、`Files` 均可通过 `title` 提供内容标题；显式空白标题和空白文本会报告 `InvalidPayload`。
- 本地文件使用 `ciyin.io.File`；已有平台 URI 使用 `PlatformShareFileSource.Uri`，调用方必须先取得读取权限。
- Android 本地文件必须位于应用 `cache`、`files` 或 `external-files` 目录，模块 FileProvider 不开放外部存储根目录。
- Android 使用 `ACTION_SEND` / `ACTION_SEND_MULTIPLE`、`ClipData` 和临时 URI 读取授权；混合 MIME 会收敛为同主类型通配符或 `*/*`。
- iOS 使用前台 `UIWindowScene` 的活动 window 展示 `UIActivityViewController`，并为 iPad 配置 popover anchor。
- Windows Desktop 使用当前活动 AWT/Compose 窗口的 HWND，通过 `IDataTransferManagerInterop` 打开系统 Share Sheet；同一 HWND 在窗口生命周期内复用唯一的 `DataTransferManager`，每次分享只持有自己的事件 token 和载荷。
- Windows 文件使用 `StorageFile` 和 `DataPackage.SetStorageItems` 保序提交；窗口关闭时才释放 manager、interop 和对应的 WinRT apartment 引用。
- Windows 的 `Uri` 文件来源只支持 `file:` URI；`LocalFile` 和 URI 都会在打开面板前校验为存在、可读的普通文件。
- Windows 未提供 Share Contract，以及 macOS、Linux Desktop 时返回 `PlatformShareResult.Unsupported`；不会用复制路径、剪贴板或文件管理器代替分享。
- 文件、URI、权限或展示控制器异常会抛出 `PlatformShareException`；Data 层应根据 `reason` 映射通用错误，UI 不应直接消费技术异常。
- 模块不显示 Toast、不包含用户文案，也不改变页面导航状态。
- 可运行示例位于 `app/sample/src/commonMain/kotlin/com/ciyin/app/ui/screen/platformshare`，覆盖文本、缓存目录单文件和缓存目录多文件分享，并在 Sample Hub 注册入口。

## 修改注意

- 新增平台能力优先添加 expect/actual，不要在 `commonMain` 中硬塞平台判断。
- `core/platform` 不应依赖 `app`、`business`、`feature` 或具体 UI 业务。
- 修改本模块后优先运行 `.\gradlew.bat :core:platform:compileCommonMainKotlinMetadata --console=plain`。
