---
name: core-material
description: Use the core/material Kotlin Multiplatform Design System module (packages ciyin.material.theme and ciyin.material.theme.iconpack). Covers AppTheme, DarkMode, AppColorScheme, AppShapes, Spacings, AppSizes, Material 3 theme bridging, and built-in IconPack icons. Use when 用户要在 NomiKit 中使用或维护项目 Design System、主题令牌、Material 3 主题封装、内置图标，或排查 core/material 构建问题。
---

# core/material 使用指南

`core/material` 是 NomiKit 的 Design System 入口模块。当前实现以 Material 3 为默认设计语言，包名为 `ciyin.material.theme`，未来可以继续聚合 Compose Fluent 等其它 Design 实现。

## 主题入口

```kotlin
import ciyin.material.theme.AppTheme
import ciyin.material.theme.DarkMode

AppTheme(darkMode = DarkMode.System) {
    Text(
        text = "Hello",
        color = AppTheme.colorScheme.textPrimary,
        style = AppTheme.typography.bodyMedium,
    )
}
```

常用入口：

- `AppTheme(...)`：根主题包装器，内部桥接 Material 3 `MaterialTheme`。
- `AppTheme.colorScheme`：项目颜色令牌，类型为 `AppColorScheme`。
- `AppTheme.colorScheme.surfaceLower` / `surfaceLow` / `surface` / `surfaceHigh` / `surfaceHigher`：五级表面层级色，用于区分不同高度或突出程度的容器。
- `AppTheme.typography`：Material 3 `Typography`。
- `AppTheme.shapes`：项目圆角令牌，类型为 `AppShapes`。
- `AppTheme.spacings`：项目间距令牌，类型为 `Spacings`。
- `AppTheme.sizes`：项目尺寸令牌，类型为 `AppSizes`。
- `lightColorScheme(...)` / `darkColorScheme(...)`：默认亮色/暗色配色，所有颜色参数都可用命名参数局部覆盖。

注意事项：

- app 层不要继续在旧 app 主题包下新增主题令牌；Design System 令牌应放在 `core/material`。
- 调用侧导入 `ciyin.material.theme.*`，不要再使用旧 app 主题包。
- 主题当前只抽出 Material 3 封装，不要提前新增 Design Provider 抽象；等第二套 Design 真正接入时再抽象。
- `core/material` 通过 `api(projects.core.uiFoundation)` 获得 Compose Material 3、UI 与 Preview 能力，不重复声明底层 Compose 依赖。

## 图标入口

```kotlin
import ciyin.material.theme.iconpack.IconPack
import ciyin.material.theme.iconpack.Home

Icon(
    imageVector = IconPack.Home,
    contentDescription = null,
)
```

可用入口：

- `IconPack.Home`
- `IconPack.Settings`
- `IconPack.LightMode`
- `IconPack.ArrowRight`
- `IconPack.Null`

注意事项：

- 图标包名是 `ciyin.material.theme.iconpack`。
- `IconPack.Null` 用作占位图标，避免调用侧为可选图标建立额外分支。

## 修改注意

- 新增公开或内部 Kotlin 符号时按项目规则补中文 KDoc。
- 主题令牌应优先保持语义命名，不要让 app 业务直接依赖裸 dp 或裸色值。
- 修改本模块后优先运行 `.\gradlew.bat :core:material:compileCommonMainKotlinMetadata --console=plain`；如果改动影响 app 调用方，再运行 `.\gradlew.bat :app:shared:compileCommonMainKotlinMetadata --console=plain`。
