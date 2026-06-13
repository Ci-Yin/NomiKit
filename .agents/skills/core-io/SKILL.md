---
name: core-io
description: Use the core/io Kotlin Multiplatform Okio-based file helper module (package ciyin.io). Covers File, SystemFileSystem, toFile, readText/writeText/read/write, copy/copyTo/copyRecursively/deleteRecursively, walkTopDown/walkBottomUp, path/name/extension helpers, hashes, sources, and file size formatting. Use when 用户要在 NomiKit commonMain 中处理文件路径、跨平台文件读写、复制删除、遍历、hash 或排查 core/io 构建问题。
---

# core/io 使用指南

`core/io` 是 NomiKit 的跨平台文件 API，基于 Okio `FileSystem`，入口包名为 `ciyin.io`。在 `commonMain` 中优先使用本模块的 `File`，不要直接使用 `java.io.File`。

## File 与路径

```kotlin
val file = "config/app.json".toFile()
val child = file.parentFile?.resolve("cache")
val path = file.toPath()
```

注意事项：

- `File` 是项目自定义类型，不是 JDK `File`。
- `SystemFileSystem` 是 expect/actual，平台差异由模块内部处理。
- `absolutePath` / `canonicalPath` 基于 Okio path normalized；权限、符号链接等行为不等同于 JVM 文件系统完整能力。
- `setReadable`、`setWritable`、`setExecutable` 等是兼容接口，不要依赖它们真正修改权限。

## 读写文本

```kotlin
file.writeText("hello")
val text = file.readText()
file.write("more", append = true)
val safeText = file.read()
```

注意事项：

- `readText()` / `writeText()` 使用 UTF-8。
- `read()` 在文件不存在时返回空字符串；需要区分“空文件”和“不存在”时先检查 `exists()`。
- `write()` 会创建父目录，`writeText()` 不负责显式创建父目录。

## 复制、删除、遍历

```kotlin
source.copyTo(target, overwrite = true)
source.copyRecursively(targetDir, overwrite = true)
root.walkTopDown().forEach { file -> ... }
root.deleteRecursively()
```

注意事项：

- `copyTo` / `copyRecursively` 会抛明确异常，适合调用层处理错误。
- `copy` / `copyDir` / `delDir` 返回 `Boolean`，内部会吞掉部分 `IOException`；需要错误原因时不要只用布尔结果。
- `copyRecursively` 失败可能留下部分复制结果，调用层要自行做回滚或清理策略。
- `walkTopDown` / `walkBottomUp` 是惰性 `Sequence`；删除目录树时优先用 bottom-up。

## 文件名与相对路径

常用 helper：

- `extension`、`nameWithoutExtension`、`replaceExtension`
- `replaceName`、`addNameFirst`、`addNameLast`
- `relativeTo`、`relativeToOrSelf`、`relativeToOrNull`
- `resolve`、`resolveSibling`、`normalize`

注意事项：

- `toRelativeString(base)` 在不同根路径时会抛 `IllegalArgumentException`。
- Windows 路径也统一按模块实现处理，不要在业务层手写分隔符拼接。

## Source、hash 与大小

```kotlin
val md5 = file.md5()
val size = file.formatSize
file.source().use { source -> ... }
```

注意事项：

- hash helper 会读取整个文件流，避免在 UI 线程直接处理大文件。
- `formatFileSize()` 用于展示字节大小，不适合作为机器可解析格式。

## 修改注意

- 新增平台能力优先走 expect/actual 或 Okio 抽象，不要在 `commonMain` 中引入平台判断。
- 修改本模块后优先运行 `.\gradlew.bat :core:io:compileCommonMainKotlinMetadata --console=plain`。
