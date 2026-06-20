---
name: core-system
description: Use the core/system Kotlin Multiplatform system utilities module (package ciyin.system.*). Covers Dispatchers.IO/runBlockingCrossPlatform, Charset/Charsets, Stopwatch, MIME helpers getMime/getExtensionFromMime/isImageFile/isVideoFile/File.mime/File.type, MimeTypeManager custom registrations, ImageUtils.resizeImageFile, and desktop AppFolderResolver/AppInfo/AppDataDirectories. Use when 用户要在 NomiKit 中使用系统工具、MIME 判断、图片缩放、跨平台 IO dispatcher 或桌面应用目录解析。
---

# core/system 使用指南

`core/system` 收纳跨平台系统工具，包名为 `ciyin.system.*`。它依赖 `core/platform` 与 `core/io`，用于补齐系统级能力，不放业务逻辑。

## 协程与阻塞桥接

```kotlin
withContext(Dispatchers.IO) { ... }
val value = runBlockingCrossPlatform { load() }
```

注意事项：

- 使用 `ciyin.system.coroutines.Dispatchers.IO` 获取跨平台 IO dispatcher。
- `runBlockingCrossPlatform` 是 expect/actual，适合测试或平台桥接；业务流程优先保持 suspend/Flow。

## MIME 与文件类型

```kotlin
val mime = "image.jpg".getMime()
val ext = "image/jpeg".getExtensionFromMime()
val isImage = file.isImage()
val type = file.type()
```

注意事项：

- `getMime()` 会清理首尾空格、URL query 和 fragment，然后按自定义类型、内置类型、平台 API、默认值顺序判断。
- 默认 MIME 是 `application/octet-stream`。
- `MimeTypeManager.register(...)` / `registerAll(...)` 是全局可变注册，测试或临时覆盖后记得 `unregister` 或 `clearCustomTypes`。
- `File.isVideo()` 特判 `m3u8`，其它类型通过 MIME 前缀判断。

## 图片工具

```kotlin
ImageUtils.resizeImageFile(
    input = source,
    output = target,
    width = 512,
    height = 512,
)
```

注意事项：

- `format` 默认取输出文件扩展名，扩展名为空时用 `png`。
- 平台 actual 负责真实缩放能力；新增格式或平台时先确认底层实现支持。
- 大图处理不要放在 UI 线程。

## 文本、计时与桌面目录

```kotlin
val charset = Charsets.UTF_8
val stopwatch = Stopwatch()
val elapsed = stopwatch.elapsed()

val dirs = AppFolderResolver.resolve(AppInfo.ApplicationId("com.ciyin.nomikit"))
val brandedDirs = AppFolderResolver.resolve(
    AppInfo.OrganizationName(
        qualifier = "com",
        organization = "CiYin",
        name = "NomiKit",
    )
)
```

注意事项：

- `Charset` / `Charsets` 是项目自己的轻量兼容类型，不等同于 JVM `java.nio.charset.Charset`。
- `Stopwatch` 基于 `TimeSource.Monotonic`，适合统计耗时，不适合作为墙钟时间。
- `AppInfo.ApplicationId` 使用完整应用 ID 作为应用目录名；Windows 下会得到类似
  `%APPDATA%/com.ciyin.nomikit/data` 的路径。
- `AppInfo.OrganizationName` 使用组织名 + 应用名两级目录；Windows 下会得到类似
  `%APPDATA%/CiYin/NomiKit/data` 的路径。
- `AppFolderResolver` 在 desktop 源集下：Windows 通过 JNA 解析 Roaming/Local AppData，Unix 通过 `dev.dirs.ProjectDirectories`。
- `AppFolderResolver.resolve` 会创建或访问目录；在启动路径使用时要处理可能的环境异常。

## 修改注意

- 新增系统能力时先判断应放 `core/system` 还是 `core/platform`：平台抽象放 `platform`，系统工具/算法放 `system`。
- 修改本模块后优先运行 `.\gradlew.bat :core:system:compileCommonMainKotlinMetadata --console=plain`。
