---
name: core-platform
description: Use the core/platform Kotlin Multiplatform platform abstraction module (package ciyin.platform). Covers Context/LocalContext/Context.files, ContextFiles/CommonContextFiles, Platform/currentPlatform/currentPlatformDesktop, Arch/PlatformType/SystemProvider/getPlatform, logger/thisLogger/Log, DateTime helpers, TaskSchedule, and URI-to-temp-file platform IO. Use when 用户要在 NomiKit 中获取平台上下文、判断平台、记录日志、使用平台目录抽象，或维护 core/platform expect/actual。
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

## 修改注意

- 新增平台能力优先添加 expect/actual，不要在 `commonMain` 中硬塞平台判断。
- `core/platform` 不应依赖 `app`、`business`、`feature` 或具体 UI 业务。
- 修改本模块后优先运行 `.\gradlew.bat :core:platform:compileCommonMainKotlinMetadata --console=plain`。
