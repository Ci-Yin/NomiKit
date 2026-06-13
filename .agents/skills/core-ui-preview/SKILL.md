---
name: core-ui-preview
description: Use the core/ui-preview Compose Multiplatform preview annotation module (package org.jetbrains.compose.ui.tooling.preview). Covers commonMain Preview expect annotation, Android typealias actual, skiko/web actual annotations, Devices, Configuration, Wallpapers, AppPreview, PreviewScreenSizes, PreviewFontScale, PreviewLightDark, and PreviewDynamicColors. Use when 用户要在 NomiKit commonMain 中写 Compose Preview、多预览注解、跨平台预览兼容，或排查 core/ui-preview。
---

# core/ui-preview 使用指南

`core/ui-preview` 提供跨平台 Compose Preview 注解，包名刻意放在 `org.jetbrains.compose.ui.tooling.preview`，让 commonMain 可以使用类似 Android Preview 的 API。

## 基础 Preview

```kotlin
@Preview(
    name = "Compact",
    widthDp = 360,
    heightDp = 720,
    showBackground = true,
)
@Composable
private fun ScreenPreview() { ... }
```

注意事项：

- commonMain 中导入本模块的 `org.jetbrains.compose.ui.tooling.preview.Preview`，不要直接依赖 Android `androidx.compose.ui.tooling.preview.Preview`。
- Android actual 是 typealias；skiko/web actual 是本模块自定义 annotation class。
- Preview 参数复制自 Android Preview 常用字段，包括 `name`、`group`、`widthDp`、`heightDp`、`locale`、`fontScale`、`showSystemUi`、`showBackground`、`uiMode`、`device`、`wallpaper`。

## 多预览注解

```kotlin
@AppPreview
@Composable
private fun ComponentPreview() { ... }

@PreviewLightDark
@PreviewFontScale
@Composable
private fun TextPreview() { ... }
```

可用组合：

- `AppPreview`：项目常用组合，包含正常、深色、大字体、大屏幕。
- `PreviewScreenSizes`：Phone、横屏 Phone、Foldable、Tablet、Desktop。
- `PreviewFontScale`：多档字体缩放。
- `PreviewLightDark`：亮/暗模式。
- `PreviewDynamicColors`：动态颜色壁纸场景。

## 常量

- `Devices.PHONE`、`FOLDABLE`、`TABLET`、`DESKTOP`、`TV_720p`、`TV_1080p`
- `Configuration.UI_MODE_NIGHT_YES` 等 uiMode 常量
- `Wallpapers.RED_DOMINATED_EXAMPLE` 等动态颜色常量

注意事项：

- 这些注解只服务预览和 IDE tooling，不要在运行时逻辑中读取它们。
- 新增多预览注解时保持 `AnnotationRetention.BINARY`，并可由多个 `@Preview` 组合。
- build 脚本里已有 `-Xannotation-target-all`，不要随意移除。

## 修改注意

- 修改本模块后优先运行 `.\gradlew.bat :core:ui-preview:compileCommonMainKotlinMetadata --console=plain`。
