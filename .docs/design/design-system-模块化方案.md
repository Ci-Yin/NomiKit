# Design System 模块化方案

## 背景

当前 `core/material` 已经承担了 NomiKit 的 Design System 入口职责，提供
`AppTheme`、`AppColorScheme`、`AppShapes`、`Spacings`、`AppSizes` 和内置图标。 但当前实现仍以 Material 3
为默认桥接方式，`AppTheme.typography` 也直接暴露
`androidx.compose.material3.Typography`。

后续目标是：

- NomiKit 拥有自己的设计系统抽象和默认实现。
- 业务侧组件调用保持 Compose 函数风格，例如 `AppButton(...)`、`AppText(...)`。
- 能够切换为 Material 3、Compose Fluent 等具体设计系统。
- 允许未来继续扩展其它设计系统实现。
- 第三方设计系统依赖隔离在独立模块中，避免污染核心抽象。

Compose Fluent 采用
[`compose-fluent/compose-fluent-ui`](https://github.com/compose-fluent/compose-fluent-ui)。 该库
README 中说明 `FluentTheme()` 是应用上下文入口，组件位于 `component`
包，`Mica` 和 `Layer` 位于 `background` 包；当前库状态为 experimental，API 未来可能变化，因此应作为独立
adapter 模块接入。

## 目标

- 从 `core/material` 演进出更中性的 `core/design-system`。
- 抽象主题令牌、组件 API 和组件渲染器。
- NomiKit 自己的设计系统作为默认实现保留在核心模块内。
- Material 3、Compose Fluent 作为独立扩展模块。
- 调用侧不直接依赖 `MaterialTheme`、`FluentTheme`、Material 组件或 Fluent 组件。
- 组件 API 保持 Compose 函数风格，而不是面向对象调用风格。
- 支持按应用入口、Preview、测试或局部 UI 树切换设计系统。

## 非目标

- 第一版不完整复刻 Material 3 或 Fluent 的所有组件。
- 第一版不承诺 Material 3 和 Fluent 的所有能力都能通过同一个参数集表达。
- 不把第三方设计系统的专有概念强塞进通用组件 API。
- 不把 `compose-fluent-ui` 直接加到核心抽象模块。
- 不一次性迁移所有业务 UI；先迁移高频基础组件。

## 模块拆分

建议新增模块：

```text
:core:design-system
  设计系统核心抽象、主题令牌、公开 App* 组件函数、NomiKit 默认实现。

:core:design-system-material3
  Material 3 adapter。依赖 :core:design-system 和 Material 3。

:core:design-system-fluent
  Compose Fluent adapter。依赖 :core:design-system 和 compose-fluent-ui。
```

现有模块处理：

```text
:core:material
  第一阶段保留为兼容层。
  可以 re-export :core:design-system，或保留旧 API 并逐步迁移到新 API。
  待 app 调用侧迁移完成后，再决定删除、改名或降级为 Material3 adapter。
```

`settings.gradle.kts` 需要新增：

```kotlin
include(":core:design-system")
include(":core:design-system-material3")
include(":core:design-system-fluent")
```

`gradle/libs.versions.toml` 建议新增：

```toml
[versions]
compose-fluent = "0.1.0"

[libraries]
compose-fluent = { module = "io.github.compose-fluent:fluent", version.ref = "compose-fluent" }
compose-fluent-icons-extended = { module = "io.github.compose-fluent:fluent-icons-extended", version.ref = "compose-fluent" }
```

如果未来需要 snapshot 版，再单独评估是否在仓库 repositories 中加入 Sonatype snapshot
repository；默认第一版使用 Maven Central release。

## 依赖方向

```text
app/shared
  -> core/design-system
  -> core/ui-foundation

app/desktop 或 app/shared 装配层
  -> core/design-system-material3
  -> core/design-system

app/desktop 或 app/shared 装配层
  -> core/design-system-fluent
  -> core/design-system
```

核心原则：

- `core/design-system` 不依赖 Material 3 和 Fluent。
- `core/design-system-material3` 可以依赖 Material 3。
- `core/design-system-fluent` 可以依赖 Compose Fluent。
- 业务 UI 只 import `ciyin.designsystem.*` 风格 API。

需要注意当前 `core/ui-foundation` 已经 `api(libs.compose.material3)`，所以第一阶段 Material 3 仍可能经由基础
UI 模块暴露出来。设计系统拆分后，可以后续再评估是否把
`core/ui-foundation` 中的 Material 3 依赖下沉到 Material 3 adapter，避免长期泄漏。

## 包结构

建议核心包名使用中性命名：

```text
ciyin.designsystem.theme
ciyin.designsystem.component
ciyin.designsystem.icon
ciyin.designsystem.runtime
```

Material 3 adapter：

```text
ciyin.designsystem.material3
```

Fluent adapter：

```text
ciyin.designsystem.fluent
```

## 主题令牌

第一版应把 Material 3 类型从核心主题令牌中拿掉。尤其是当前
`AppTheme.typography: Typography`，需要替换为项目自己的 `AppTypography`。

```kotlin
package ciyin.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle

/**
 * 应用排版令牌。
 *
 * 使用项目自己的排版模型承载业务侧语义，避免核心设计系统依赖 Material 3 或 Fluent
 * 的排版类型。
 */
@Immutable
data class AppTypography(
    /** 页面级大标题。 */
    val displayLarge: TextStyle,
    /** 页面级中标题。 */
    val displayMedium: TextStyle,
    /** 区块标题。 */
    val titleLarge: TextStyle,
    /** 正文主要文本。 */
    val bodyLarge: TextStyle,
    /** 正文默认文本。 */
    val bodyMedium: TextStyle,
    /** 辅助说明文本。 */
    val bodySmall: TextStyle,
    /** 标签文本。 */
    val labelMedium: TextStyle,
)
```

统一主题令牌：

```kotlin
package ciyin.designsystem.theme

import androidx.compose.runtime.Immutable

/**
 * 应用设计系统主题令牌集合。
 *
 * 该类型只表达 NomiKit 自身的设计语义，不绑定任意第三方设计系统。
 */
@Immutable
data class AppThemeTokens(
    /** 当前颜色方案。 */
    val colorScheme: AppColorScheme,
    /** 当前排版方案。 */
    val typography: AppTypography,
    /** 当前圆角方案。 */
    val shapes: AppShapes,
    /** 当前间距方案。 */
    val spacings: Spacings,
    /** 当前尺寸方案。 */
    val sizes: AppSizes,
)
```

`AppColorScheme`、`AppShapes`、`Spacings`、`AppSizes` 可以先从 `core/material`
迁移或复制到新模块，再逐步改包名。为了降低迁移风险，第一阶段可以保留 typealias 兼容旧导入。

## 运行时设计系统选择

核心抽象：

```kotlin
package ciyin.designsystem.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import ciyin.designsystem.component.AppDesignSystemComponents
import ciyin.designsystem.theme.AppThemeTokens
import ciyin.designsystem.theme.DarkMode

/**
 * 应用设计系统实现。
 *
 * 每个具体设计系统负责提供主题环境，并提供一组组件渲染器。
 */
@Stable
interface AppDesignSystem {
    /** 设计系统稳定标识。 */
    val id: AppDesignSystemId

    /** 组件族渲染器集合。 */
    val components: AppDesignSystemComponents

    /**
     * 提供当前设计系统对应的主题环境。
     *
     * @param darkMode 深色模式设置。
     * @param tokens NomiKit 统一主题令牌。
     * @param content 使用主题环境的内容。
     */
    @Composable
    fun ProvideTheme(
        darkMode: DarkMode,
        tokens: AppThemeTokens,
        content: @Composable () -> Unit,
    )
}
```

```kotlin
package ciyin.designsystem.runtime

import androidx.compose.runtime.Immutable

/**
 * 设计系统稳定标识。
 *
 * 使用 value class 避免调用侧传递裸字符串。
 */
@Immutable
@JvmInline
value class AppDesignSystemId(
    /** 标识值，例如 `nomi`、`material3`、`fluent`。 */
    val value: String,
)
```

`CompositionLocal`：

```kotlin
package ciyin.designsystem.runtime

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 当前组件树使用的设计系统。
 */
val LocalAppDesignSystem = staticCompositionLocalOf<AppDesignSystem> {
    NomiDesignSystem
}
```

主题入口：

```kotlin
package ciyin.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import ciyin.designsystem.runtime.AppDesignSystem
import ciyin.designsystem.runtime.LocalAppDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystem

/**
 * 应用设计系统主题入口。
 *
 * 业务侧使用该函数建立设计系统上下文，并可以通过 [designSystem] 切换具体实现。
 */
@Composable
fun AppTheme(
    designSystem: AppDesignSystem = NomiDesignSystem,
    darkMode: DarkMode = DarkMode.System,
    tokens: AppThemeTokens = defaultAppThemeTokens(darkMode),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAppDesignSystem provides designSystem,
        LocalDarkMode provides darkMode,
        LocalThemeTokens provides tokens,
    ) {
        designSystem.ProvideTheme(
            darkMode = darkMode,
            tokens = tokens,
            content = content,
        )
    }
}
```

`AppTheme` 对象保留只读令牌访问：

```kotlin
package ciyin.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

/**
 * 应用主题只读访问入口。
 */
object AppTheme {
    /** 当前颜色方案。 */
    val colorScheme: AppColorScheme
        @Composable @ReadOnlyComposable get() = LocalThemeTokens.current.colorScheme

    /** 当前排版方案。 */
    val typography: AppTypography
        @Composable @ReadOnlyComposable get() = LocalThemeTokens.current.typography

    /** 当前圆角方案。 */
    val shapes: AppShapes
        @Composable @ReadOnlyComposable get() = LocalThemeTokens.current.shapes

    /** 当前间距方案。 */
    val spacings: Spacings
        @Composable @ReadOnlyComposable get() = LocalThemeTokens.current.spacings

    /** 当前尺寸方案。 */
    val sizes: AppSizes
        @Composable @ReadOnlyComposable get() = LocalThemeTokens.current.sizes
}
```

## 组件抽象方式

业务侧必须使用 Compose 函数，不直接拿 renderer 调用。renderer 只作为内部派发层。

不要把所有组件函数都塞进单个 `AppComponentRenderer`。随着 `AppButton`、
`AppTextField`、`AppDialog`、`AppScaffold`、`AppMenu` 等组件不断增加，单个接口会
快速变成“上帝接口”，任意新增组件都会迫使所有设计系统实现同时改一个巨大文件。

第一版应按组件族拆分 renderer，再用一个轻量聚合类型统一挂到 `AppDesignSystem` 上：

```kotlin
package ciyin.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

/**
 * 应用设计系统组件族集合。
 *
 * 该接口只负责聚合组件族，不直接声明具体组件函数，避免组件数量增长后形成巨大接口。
 */
@Stable
interface AppDesignSystemComponents {
    /** 文本组件渲染器。 */
    val text: AppTextRenderer

    /** 操作类组件渲染器。 */
    val actions: AppActionRenderer

    /** 输入类组件渲染器。 */
    val inputs: AppInputRenderer

    /** 容器与表面组件渲染器。 */
    val surfaces: AppSurfaceRenderer

    /** 导航类组件渲染器。 */
    val navigation: AppNavigationRenderer

    /** 反馈类组件渲染器。 */
    val feedback: AppFeedbackRenderer

    /** 浮层类组件渲染器。 */
    val overlays: AppOverlayRenderer
}
```

按钮属于操作类组件：

```kotlin
package ciyin.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

/**
 * 操作类组件渲染器。
 *
 * 包含按钮、图标按钮、分裂按钮、命令按钮等用于触发动作的组件。
 */
@Stable
interface AppActionRenderer {
    /**
     * 渲染按钮。
     *
     * @param onClick 点击回调。
     * @param modifier 修饰符。
     * @param enabled 是否启用。
     * @param variant 按钮视觉类型。
     * @param size 按钮尺寸。
     * @param content 按钮内容。
     */
    @Composable
    fun Button(
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        variant: AppButtonVariant,
        size: AppControlSize,
        content: @Composable RowScope.() -> Unit,
    )

    /**
     * 渲染图标按钮。
     *
     * @param onClick 点击回调。
     * @param modifier 修饰符。
     * @param enabled 是否启用。
     * @param size 按钮尺寸。
     * @param content 图标内容。
     */
    @Composable
    fun IconButton(
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        size: AppControlSize,
        content: @Composable () -> Unit,
    )
}
```

其它组件族先定义为空接口或按第一阶段组件逐步填充。新增组件时只改对应组件族， 例如 `AppTextField` 进入
`AppInputRenderer`，`AppDialog` 进入 `AppOverlayRenderer`。

```kotlin
package ciyin.designsystem.component

import androidx.compose.runtime.Stable

/**
 * 文本组件渲染器。
 */
@Stable
interface AppTextRenderer

/**
 * 输入类组件渲染器。
 */
@Stable
interface AppInputRenderer

/**
 * 容器与表面组件渲染器。
 */
@Stable
interface AppSurfaceRenderer

/**
 * 导航类组件渲染器。
 */
@Stable
interface AppNavigationRenderer

/**
 * 反馈类组件渲染器。
 */
@Stable
interface AppFeedbackRenderer

/**
 * 浮层类组件渲染器。
 */
@Stable
interface AppOverlayRenderer
```

公开组件函数仍然保持 Compose 函数风格：

```kotlin
package ciyin.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.runtime.LocalAppDesignSystem

/**
 * 应用按钮组件。
 *
 * 该函数是业务侧唯一需要使用的按钮入口。实际视觉实现由当前 [LocalAppDesignSystem]
 * 决定。
 */
@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: AppButtonVariant = AppButtonVariant.Primary,
    size: AppControlSize = AppControlSize.Medium,
    content: @Composable RowScope.() -> Unit,
) {
    LocalAppDesignSystem.current.components.actions.Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        variant = variant,
        size = size,
        content = content,
    )
}
```

不建议用 `Map<ComponentKey, Renderer>` 或字符串 key 注册表作为核心方案。它看起来更 灵活，但会牺牲类型安全、IDE
跳转、参数约束和重构可靠性。通用组件进入组件族接口； 某个设计系统独有能力放在对应 adapter 模块自己的扩展包里。

参数枚举：

```kotlin
package ciyin.designsystem.component

/**
 * 按钮视觉类型。
 */
enum class AppButtonVariant {
    /** 主要按钮，用于页面主操作。 */
    Primary,

    /** 普通按钮，用于默认操作。 */
    Default,

    /** 轻量按钮，用于低强调操作。 */
    Subtle,

    /** 文本按钮，用于链接式操作。 */
    Text,

    /** 危险按钮，用于删除等高风险操作。 */
    Destructive,
}

/**
 * 控件尺寸。
 */
enum class AppControlSize {
    /** 紧凑尺寸。 */
    Small,

    /** 默认尺寸。 */
    Medium,

    /** 大尺寸。 */
    Large,
}
```

## NomiKit 默认实现

`core/design-system` 内置自己的默认实现，避免没有 adapter 时无法启动。

```kotlin
package ciyin.designsystem.runtime

import androidx.compose.runtime.Composable
import ciyin.designsystem.component.AppDesignSystemComponents
import ciyin.designsystem.theme.AppThemeTokens
import ciyin.designsystem.theme.DarkMode

/**
 * NomiKit 默认设计系统。
 *
 * 该实现承载项目自己的视觉风格，不依赖 Material 3 或 Compose Fluent。
 */
object NomiDesignSystem : AppDesignSystem {
    override val id: AppDesignSystemId = AppDesignSystemId("nomi")

    override val components: AppDesignSystemComponents = NomiDesignSystemComponents

    @Composable
    override fun ProvideTheme(
        darkMode: DarkMode,
        tokens: AppThemeTokens,
        content: @Composable () -> Unit,
    ) {
        content()
    }
}
```

Nomi 默认组件可以使用 `foundation` 和 `ui` 自己绘制：

```kotlin
package ciyin.designsystem.runtime

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.component.AppActionRenderer
import ciyin.designsystem.component.AppButtonVariant
import ciyin.designsystem.component.AppControlSize
import ciyin.designsystem.component.AppDesignSystemComponents
import ciyin.designsystem.component.AppFeedbackRenderer
import ciyin.designsystem.component.AppInputRenderer
import ciyin.designsystem.component.AppNavigationRenderer
import ciyin.designsystem.component.AppOverlayRenderer
import ciyin.designsystem.component.AppSurfaceRenderer
import ciyin.designsystem.component.AppTextRenderer
import ciyin.designsystem.theme.AppTheme

/**
 * NomiKit 默认组件族集合。
 */
object NomiDesignSystemComponents : AppDesignSystemComponents {
    override val text: AppTextRenderer = NomiTextRenderer
    override val actions: AppActionRenderer = NomiActionRenderer
    override val inputs: AppInputRenderer = NomiInputRenderer
    override val surfaces: AppSurfaceRenderer = NomiSurfaceRenderer
    override val navigation: AppNavigationRenderer = NomiNavigationRenderer
    override val feedback: AppFeedbackRenderer = NomiFeedbackRenderer
    override val overlays: AppOverlayRenderer = NomiOverlayRenderer
}

/**
 * NomiKit 默认操作类组件渲染器。
 */
object NomiActionRenderer : AppActionRenderer {
    @Composable
    override fun Button(
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        variant: AppButtonVariant,
        size: AppControlSize,
        content: @Composable RowScope.() -> Unit,
    ) {
        val background = when (variant) {
            AppButtonVariant.Primary -> AppTheme.colorScheme.primary
            AppButtonVariant.Destructive -> AppTheme.colorScheme.error
            AppButtonVariant.Default,
            AppButtonVariant.Subtle,
            AppButtonVariant.Text -> AppTheme.colorScheme.surface
        }
        Box(
            modifier = modifier
                .background(
                    color = background,
                    shape = AppTheme.shapes.small,
                )
                .clickable(
                    enabled = enabled,
                    onClick = onClick,
                )
                .padding(
                    horizontal = AppTheme.spacings.large,
                    vertical = AppTheme.spacings.small,
                ),
        ) {
            AppButtonContent(content = content)
        }
    }

    @Composable
    override fun IconButton(
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        size: AppControlSize,
        content: @Composable () -> Unit,
    ) {
        Box(
            modifier = modifier
                .clickable(
                    enabled = enabled,
                    onClick = onClick,
                )
                .padding(AppTheme.spacings.small),
        ) {
            content()
        }
    }
}
```

上面只是示意。实际实现时应处理 disabled、focus、hover、pressed、semantics、
minimumInteractiveComponentSize 等状态，避免只做静态视觉。

## Material 3 adapter

Material 3 adapter 负责把 NomiKit 令牌映射到 Material 3。

```kotlin
package ciyin.designsystem.material3

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import ciyin.designsystem.component.AppDesignSystemComponents
import ciyin.designsystem.runtime.AppDesignSystem
import ciyin.designsystem.runtime.AppDesignSystemId
import ciyin.designsystem.theme.AppThemeTokens
import ciyin.designsystem.theme.DarkMode

/**
 * Material 3 设计系统实现。
 */
object Material3DesignSystem : AppDesignSystem {
    override val id: AppDesignSystemId = AppDesignSystemId("material3")

    override val components: AppDesignSystemComponents = Material3DesignSystemComponents

    @Composable
    override fun ProvideTheme(
        darkMode: DarkMode,
        tokens: AppThemeTokens,
        content: @Composable () -> Unit,
    ) {
        MaterialTheme(
            colorScheme = tokens.colorScheme.toMaterial3ColorScheme(darkMode),
            typography = tokens.typography.toMaterial3Typography(),
            shapes = tokens.shapes.toMaterial3Shapes(),
            content = content,
        )
    }
}
```

按钮映射：

```kotlin
package ciyin.designsystem.material3

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.component.AppActionRenderer
import ciyin.designsystem.component.AppButtonVariant
import ciyin.designsystem.component.AppControlSize
import ciyin.designsystem.component.AppDesignSystemComponents
import ciyin.designsystem.component.AppFeedbackRenderer
import ciyin.designsystem.component.AppInputRenderer
import ciyin.designsystem.component.AppNavigationRenderer
import ciyin.designsystem.component.AppOverlayRenderer
import ciyin.designsystem.component.AppSurfaceRenderer
import ciyin.designsystem.component.AppTextRenderer

/**
 * Material 3 组件族集合。
 */
object Material3DesignSystemComponents : AppDesignSystemComponents {
    override val text: AppTextRenderer = Material3TextRenderer
    override val actions: AppActionRenderer = Material3ActionRenderer
    override val inputs: AppInputRenderer = Material3InputRenderer
    override val surfaces: AppSurfaceRenderer = Material3SurfaceRenderer
    override val navigation: AppNavigationRenderer = Material3NavigationRenderer
    override val feedback: AppFeedbackRenderer = Material3FeedbackRenderer
    override val overlays: AppOverlayRenderer = Material3OverlayRenderer
}

/**
 * Material 3 操作类组件渲染器。
 */
object Material3ActionRenderer : AppActionRenderer {
    @Composable
    override fun Button(
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        variant: AppButtonVariant,
        size: AppControlSize,
        content: @Composable RowScope.() -> Unit,
    ) {
        when (variant) {
            AppButtonVariant.Text,
            AppButtonVariant.Subtle -> TextButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                content = content,
            )

            AppButtonVariant.Primary,
            AppButtonVariant.Default,
            AppButtonVariant.Destructive -> Button(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                content = content,
            )
        }
    }

    @Composable
    override fun IconButton(
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        size: AppControlSize,
        content: @Composable () -> Unit,
    ) {
        IconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            content = content,
        )
    }
}
```

## Compose Fluent adapter

Fluent adapter 单独依赖：

```kotlin
kotlin {
    sourceSets.commonMain.dependencies {
        api(projects.core.designSystem)
        implementation(libs.compose.fluent)
    }
}
```

如果需要完整 Fluent 图标，再增加：

```kotlin
implementation(libs.compose.fluent.icons.extended)
```

实现示意：

```kotlin
package ciyin.designsystem.fluent

import androidx.compose.runtime.Composable
import ciyin.designsystem.component.AppDesignSystemComponents
import ciyin.designsystem.runtime.AppDesignSystem
import ciyin.designsystem.runtime.AppDesignSystemId
import ciyin.designsystem.theme.AppThemeTokens
import ciyin.designsystem.theme.DarkMode
import io.github.composefluent.FluentTheme

/**
 * Compose Fluent 设计系统实现。
 */
object FluentDesignSystem : AppDesignSystem {
    override val id: AppDesignSystemId = AppDesignSystemId("fluent")

    override val components: AppDesignSystemComponents = FluentDesignSystemComponents

    @Composable
    override fun ProvideTheme(
        darkMode: DarkMode,
        tokens: AppThemeTokens,
        content: @Composable () -> Unit,
    ) {
        FluentTheme {
            content()
        }
    }
}
```

按钮映射示意：

```kotlin
package ciyin.designsystem.fluent

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.component.AppActionRenderer
import ciyin.designsystem.component.AppButtonVariant
import ciyin.designsystem.component.AppControlSize
import ciyin.designsystem.component.AppDesignSystemComponents
import ciyin.designsystem.component.AppFeedbackRenderer
import ciyin.designsystem.component.AppInputRenderer
import ciyin.designsystem.component.AppNavigationRenderer
import ciyin.designsystem.component.AppOverlayRenderer
import ciyin.designsystem.component.AppSurfaceRenderer
import ciyin.designsystem.component.AppTextRenderer
import io.github.composefluent.component.AccentButton
import io.github.composefluent.component.Button
import io.github.composefluent.component.HyperlinkButton
import io.github.composefluent.component.IconButton
import io.github.composefluent.component.SubtleButton

/**
 * Compose Fluent 组件族集合。
 */
object FluentDesignSystemComponents : AppDesignSystemComponents {
    override val text: AppTextRenderer = FluentTextRenderer
    override val actions: AppActionRenderer = FluentActionRenderer
    override val inputs: AppInputRenderer = FluentInputRenderer
    override val surfaces: AppSurfaceRenderer = FluentSurfaceRenderer
    override val navigation: AppNavigationRenderer = FluentNavigationRenderer
    override val feedback: AppFeedbackRenderer = FluentFeedbackRenderer
    override val overlays: AppOverlayRenderer = FluentOverlayRenderer
}

/**
 * Compose Fluent 操作类组件渲染器。
 */
object FluentActionRenderer : AppActionRenderer {
    @Composable
    override fun Button(
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        variant: AppButtonVariant,
        size: AppControlSize,
        content: @Composable RowScope.() -> Unit,
    ) {
        when (variant) {
            AppButtonVariant.Primary -> AccentButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                content = content,
            )

            AppButtonVariant.Subtle -> SubtleButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                content = content,
            )

            AppButtonVariant.Text -> HyperlinkButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                content = content,
            )

            AppButtonVariant.Default,
            AppButtonVariant.Destructive -> Button(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                content = content,
            )
        }
    }

    @Composable
    override fun IconButton(
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        size: AppControlSize,
        content: @Composable () -> Unit,
    ) {
        IconButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            content = content,
        )
    }
}
```

Fluent 的 `Mica`、`Layer` 不建议直接进入通用 `AppScaffold` 第一版参数。可以额外提供 Fluent 专属扩展：

```kotlin
package ciyin.designsystem.fluent

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.composefluent.background.Mica

/**
 * Fluent Mica 背景容器。
 *
 * 该组件属于 Fluent 专属能力，调用侧需要显式依赖 Fluent adapter 模块。
 */
@Composable
fun FluentMicaSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Mica(
        modifier = modifier,
        content = content,
    )
}
```

## 组件覆盖范围

第一阶段建议覆盖高频且跨设计系统可表达的组件：

- `AppText`
- `AppButton`
- `AppIconButton`
- `AppTextField`
- `AppCheckbox`
- `AppSwitch`
- `AppRadioButton`
- `AppSlider`
- `AppSurface`
- `AppCard`
- `AppDialog`
- `AppScaffold`
- `AppTopBar`
- `AppNavigation`
- `AppTooltip`
- `AppMenu`
- `AppProgressIndicator`

第二阶段再补：

- `AppSegmentedButton`
- `AppTabs`
- `AppListItem`
- `AppBadge`
- `AppInfoBar`
- `AppExpander`
- `AppDatePicker`
- `AppColorPicker`
- `AppCommandBar`

规则：

- 只把 NomiKit 业务真正需要的能力纳入通用组件 API。
- 某个设计系统独有能力放到对应 adapter 的扩展包中。
- 通用组件参数要表达语义，而不是复刻 Material 3 或 Fluent 参数名。
- 参数数量过多时拆成 `Defaults`、`Colors`、`Size`、`Variant` 等稳定模型。

## 组件 API 风格

业务侧调用示例：

```kotlin
AppTheme(
    designSystem = Material3DesignSystem,
) {
    AppScaffold(
        topBar = {
            AppTopBar(
                title = { AppText("NomiKit") },
            )
        },
    ) {
        AppButton(
            onClick = onConfirm,
            variant = AppButtonVariant.Primary,
        ) {
            AppText("确认")
        }
    }
}
```

切换 Fluent：

```kotlin
AppTheme(
    designSystem = FluentDesignSystem,
) {
    AppScaffold {
        AppButton(
            onClick = onConfirm,
            variant = AppButtonVariant.Primary,
        ) {
            AppText("确认")
        }
    }
}
```

局部覆盖：

```kotlin
AppTheme(
    designSystem = Material3DesignSystem,
) {
    SettingsScreen()

    AppTheme(
        designSystem = FluentDesignSystem,
    ) {
        DesktopOnlyPreviewPanel()
    }
}
```

## Defaults 设计

每个组件可以提供自己的 Defaults，避免参数不断膨胀。

```kotlin
package ciyin.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import ciyin.designsystem.theme.AppTheme

/**
 * 按钮颜色配置。
 */
@Immutable
data class AppButtonColors(
    /** 容器颜色。 */
    val containerColor: Color,
    /** 内容颜色。 */
    val contentColor: Color,
    /** 禁用容器颜色。 */
    val disabledContainerColor: Color,
    /** 禁用内容颜色。 */
    val disabledContentColor: Color,
)

/**
 * 应用按钮默认值。
 */
object AppButtonDefaults {
    /**
     * 创建按钮颜色配置。
     */
    @Composable
    fun colors(
        containerColor: Color = AppTheme.colorScheme.primary,
        contentColor: Color = AppTheme.colorScheme.onPrimary,
        disabledContainerColor: Color = AppTheme.colorScheme.surfaceLow,
        disabledContentColor: Color = AppTheme.colorScheme.textDisabled,
    ): AppButtonColors = AppButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor,
    )
}
```

`AppActionRenderer.Button(...)` 是否接收 `AppButtonColors`，可以等真实迁移按钮时决定。 原则是：公开 App
组件参数稳定，组件族 renderer 参数可以跟随内部需要调整。

## 图标策略

核心模块保留项目图标入口：

```text
ciyin.designsystem.icon.IconPack
```

Material 3 adapter 可继续使用 Material Icons 做缺省映射。

Fluent adapter 可选引入 `fluent-icons-extended`，但不建议让核心 `IconPack` 直接依赖 Fluent 图标包。可以提供：

```kotlin
/**
 * Fluent 图标包扩展入口。
 */
object FluentIconPack
```

需要跨设计系统稳定显示的图标，应优先放入 NomiKit 自己的 `IconPack`。

## Preview 和测试

Preview 应能显式指定设计系统：

```kotlin
/**
 * 使用 NomiKit 默认设计系统的预览容器。
 */
@Composable
fun NomiPreviewTheme(
    content: @Composable () -> Unit,
) {
    AppTheme(
        designSystem = NomiDesignSystem,
        content = content,
    )
}
```

```kotlin
/**
 * 使用 Material 3 设计系统的预览容器。
 */
@Composable
fun Material3PreviewTheme(
    content: @Composable () -> Unit,
) {
    AppTheme(
        designSystem = Material3DesignSystem,
        content = content,
    )
}
```

```kotlin
/**
 * 使用 Compose Fluent 设计系统的预览容器。
 */
@Composable
fun FluentPreviewTheme(
    content: @Composable () -> Unit,
) {
    AppTheme(
        designSystem = FluentDesignSystem,
        content = content,
    )
}
```

组件单测可以优先测试：

- `LocalAppDesignSystem` 能正确覆盖。
- `AppTheme` 能提供 tokens。
- 每个 adapter 的 `ProvideTheme` 能编译。
- 基础组件能在不同设计系统下渲染。

## 迁移步骤

1. 新增 `:core:design-system` 模块。
2. 将 `core/material` 的中性令牌迁移到 `ciyin.designsystem.theme`。
3. 新增 `AppTypography`，替代核心 API 中的 Material 3 `Typography`。
4. 新增 `AppThemeTokens`、`AppDesignSystem`、`AppDesignSystemId`、`LocalAppDesignSystem`。
5. 新增 `NomiDesignSystem`、`NomiDesignSystemComponents` 和 `NomiActionRenderer`。
6. 先实现 `AppText`、`AppButton`、`AppSurface`、`AppCard`。
7. 新增 `:core:design-system-material3`，迁移当前 Material 3 bridge。
8. 新增 `:core:design-system-fluent`，接入 `compose-fluent-ui`。
9. 在 app 根主题处显式选择设计系统。
10. 将少量页面或 sample 页面迁移到 `App*` 组件，验证 API 形状。
11. 更新 `.agents/skills/core-material/SKILL.md`，或新增
    `.agents/skills/core-design-system/SKILL.md`。
12. 逐步处理 `core/material` 兼容层，避免一次性破坏现有调用。

## 验证计划

最小验证：

```powershell
.\gradlew.bat :core:design-system:compileCommonMainKotlinMetadata --console=plain
.\gradlew.bat :core:design-system-material3:compileCommonMainKotlinMetadata --console=plain
.\gradlew.bat :core:design-system-fluent:compileCommonMainKotlinMetadata --console=plain
```

如果 app/shared 接入新主题：

```powershell
.\gradlew.bat :app:shared:compileCommonMainKotlinMetadata --console=plain
```

如果 sample 接入设计系统切换页面：

```powershell
.\gradlew.bat :app:sample:compileCommonMainKotlinMetadata --console=plain
.\gradlew.bat :app:sample:compileKotlinDesktop --console=plain
```

需要人工检查：

- Material 3、Fluent、Nomi 默认设计系统下基础组件视觉是否可接受。
- disabled / hover / focus / pressed / loading 等状态是否完整。
- 文本、按钮、输入框在桌面和移动端尺寸是否稳定。
- Fluent adapter 在 Android 低版本是否需要 desugar 配置。

## 风险与注意事项

- Compose Fluent 当前 experimental，adapter 模块需要把 API 波动隔离住。
- 如果 `core/ui-foundation` 长期 `api` 暴露 Material 3，核心抽象仍会存在间接泄漏。
- 通用组件 API 不应追求覆盖所有第三方组件能力，否则会变成最大公约数 API。
- Fluent 的 Mica/Acrylic、窗口层能力和平台窗口能力有关，不应直接塞入通用 `AppSurface`。
- 第一版迁移应以 sample 或少量页面试点，确认调用体验后再扩大范围。

## 开放问题

- `core/material` 是最终重命名为 `core/design-system-material3`，还是长期作为兼容层保留？
- NomiKit 默认实现是否要完全不用 Material 3，还是允许第一阶段内部借用 Material 3 再逐步替换？
- 设计系统选择来自用户设置、构建配置，还是平台默认策略？
- Fluent adapter 是否只在 desktop/sample 里启用，还是 app/shared 默认可选？
- 是否需要为每个组件建立截图回归测试？
