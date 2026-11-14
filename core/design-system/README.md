# `:core:design-system`

## 用途与非目标

本模块提供统一的 Compose 调用入口、设计系统 SPI 与语义只读投影；内置 Nomi
实现位于独立模块 [core/design-system-nomi](<../design-system-nomi/README.md>)，核心不引用它。
**统一的是调用方式，不是三套组件的默认外观。** 未指定视觉参数时，Material 3 / Fluent 应保留各自原生默认；
Nomi 默认只属于 Nomi，不能在核心派发前填入其他 adapter。

生产依赖仅为 Compose Runtime/Foundation/UI，不依赖 Material 3、Fluent、旧门面或 UI foundation。 当前提供
`NomiText`、`NomiButton`、`NomiSurface`、`NomiCard`、`NomiNavigationSuite` 与 `NomiScaffold`
；不负责窗口形态判断、应用路由、平台窗口、持久化设置或全部第三方控件。
原生默认组件保持不变；旧式主题文件职责、颜色工厂、五组直接主题参数与独立 nomi 包沿用此前实现。 当前 9
个主题 data class 公开构造，复制使用编译器生成的成员 `copy`；默认工厂、默认值、必填参数与身份语义不变。
上一轮删除主题聚合层时，核心/M3/Fluent metadata、sample Android 编译及 154 项 Desktop
回归通过，零失败、零跳过；本轮新增导航组件族与三适配后，五模块 `compileCommonMainKotlinMetadata`、app:
shared Android/Desktop、sample `compileAndroidMain` 与 180 项 Desktop 回归通过（核心 42、Nomi 18、M3
30、Fluent 46、旧门面 19、sample
25），零失败、零跳过。完整范围见[设计说明](<../../.docs/design/design-system-模块化方案.md>)。

## 命名边界

Nomi 表示 NomiKit 公共设计体系，不限定当前选用的视觉实现。公开入口统一为
NomiTheme/NomiText/NomiButton/NomiSurface/NomiCard， 主题数据与默认工厂同样使用 Nomi 前缀；新核心不保留
App* 别名。根包 ciyin.designsystem 和设计系统标识保持不变，组件 package 按下述子目录细分。
NomiDesignSystem 是实现契约；Nomi 默认实现与 Material 3/Fluent 适配实现都以扩展属性挂到其伴生对象上，对外只暴露
`NomiDesignSystem.Default`、`NomiDesignSystem.Material3`、`NomiDesignSystem.Fluent`，实现对象本身为
internal。 默认 renderer 使用 DefaultNomi* 名称，原生解析上下文使用 LocalNomiNative*，不得与公共显式
slot 的 LocalNomi* 混用。 应用层 App/AppPreview/AppBuildConfig 等名称不变；ciyin.material 的
AppTheme/AppColorScheme/AppShapes/AppSizes 仅作为旧兼容入口保留。

## 绑定命名空间

`NomiDesignSystem` 自带空 `companion object`，仅作为各 adapter 实现的绑定命名空间。adapter
用扩展属性把自身实现挂上去：

```kotlin
// :core:design-system-nomi
val NomiDesignSystem.Companion.Default: NomiDesignSystem get() = DefaultNomiSystem
// :core:design-system-material3
val NomiDesignSystem.Companion.Material3: NomiDesignSystem get() = Material3System
// :core:design-system-fluent
val NomiDesignSystem.Companion.Fluent: NomiDesignSystem get() = FluentSystem
```

调用方只依赖核心契约，按 `NomiDesignSystem.` 补全并导入对应扩展属性；扩展属性不是接口成员，
**必须显式导入**，否则 `NomiDesignSystem.Material3` 无法解析：

```kotlin
import ciyin.designsystem.material3.Material3 // 提供 NomiDesignSystem.Material3
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme

NomiTheme(designSystem = NomiDesignSystem.Material3) {
    // 业务内容
}
```

- 伴生对象没有状态和成员：用扩展属性而不是接口成员，可让核心不反向依赖任何 adapter，adapter
  之间也互不依赖；调用方仍以 `NomiDesignSystem.X` 统一书写。
- 实现对象 `DefaultNomiSystem` / `Material3System` / `FluentSystem` 为 internal；旧顶层 object
  名称已删除，不保留别名、typealias 或转发。
- 扩展属性是无 backing field 的只读 `val`：只有 getter，不能赋值或被覆盖；每次读取返回对应 adapter
  持有的同一实现实例。
- 命名空间只负责取得实例，不改变提供方式：`NomiTheme(designSystem = ...)` 与
  `CompositionLocalProvider(LocalNomiDesignSystem provides ...)` 的显式提供契约不变，未提供时
  `requireNomiDesignSystem()` 仍明确失败。

## 从哪里开始

| 入口                                                                                                                                                                                                                                                                                   | 职责                                                                                 |
|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------|
| [Theme](<src/commonMain/kotlin/ciyin/designsystem/theme/Theme.kt>)                                                                                                                                                                                                                     | `NomiTheme` / `NomiThemeOverride` 与只读 `NomiTheme` 对象                            |
| [ThemeOverrides](<src/commonMain/kotlin/ciyin/designsystem/theme/ThemeOverrides.kt>)                                                                                                                                                                                                   | 五组直接参数与 accent 归一化后的显式输入；未指定仍为空                               |
| [ColorScheme](<src/commonMain/kotlin/ciyin/designsystem/theme/ColorScheme.kt>) / [Type](<src/commonMain/kotlin/ciyin/designsystem/theme/Type.kt>)                                                                                                                                      | defaultLight/defaultDark 颜色工厂、NomiColorScheme 与中立 NomiTypography             |
| [NomiThemeRequest](<src/commonMain/kotlin/ciyin/designsystem/runtime/NomiThemeRequest.kt>) / [ResolvedNomiTheme](<src/commonMain/kotlin/ciyin/designsystem/runtime/ResolvedNomiTheme.kt>) / [NomiDesignSystem](<src/commonMain/kotlin/ciyin/designsystem/runtime/NomiDesignSystem.kt>) | 请求模式、实际明暗、五组结果及 overrides；adapter 在原生主题内回调 ResolvedNomiTheme |
| [NomiText](<src/commonMain/kotlin/ciyin/designsystem/component/text/NomiText.kt>) / [NomiButton](<src/commonMain/kotlin/ciyin/designsystem/component/button/NomiButton.kt>)                                                                                                            | 文本与按钮 API、对应 renderer 和参数类型                                             |
| [NomiSurface](<src/commonMain/kotlin/ciyin/designsystem/component/surface/NomiSurface.kt>) / [NomiCard](<src/commonMain/kotlin/ciyin/designsystem/component/card/NomiCard.kt>)                                                                                                         | Surface 与实际原生 Card 分开派发                                                     |
| [NomiBorder](<src/commonMain/kotlin/ciyin/designsystem/component/NomiBorder.kt>) / [Capabilities](<src/commonMain/kotlin/ciyin/designsystem/component/NomiComponentCapabilities.kt>)                                                                                                   | 描边意图三态、显式覆盖与导航布局能力                                                 |
| [NomiNavigation](<src/commonMain/kotlin/ciyin/designsystem/component/navigation/NomiNavigation.kt>)                                                                                                                                                                                    | 导航套件入口、Bar/Rail/Drawer 布局、导航项作用域与原生渲染契约                       |
| [NomiScaffold](<src/commonMain/kotlin/ciyin/designsystem/component/scaffold/NomiScaffold.kt>)                                                                                                                                                                                          | 页面脚手架入口、顶栏/主体/底栏三槽位、容器色与内容色稀疏覆盖                         |

[组件集合](<src/commonMain/kotlin/ciyin/designsystem/component/NomiDesignSystemComponents.kt>)组装
text/actions/surfaces/navigation/scaffold 五族； 表面族同时有 `Surface` 和 `Card`
方法，导航族提供套件入口，脚手架族提供页面外壳入口。实现不再集中在一个组件大文件，Nomi 也按组件子包拆分。

公共 `component` 保留公共入口、renderer SPI
与共享参数；内置实现已拆到独立模块 [core/design-system-nomi](<../design-system-nomi/README.md>)， 包名仍为
`ciyin.designsystem.nomi`，按 `text/button/surface/card` 组织。公共主题 API 与数据留在 `theme`
，设计系统契约、请求和显式 slot 留在 `runtime`。
`LocalNomiDesignSystem` 为可空且 **无隐式默认**：应用根、测试与 Preview 必须显式提供实现；未提供时组件与
`NomiTheme` 抛出带指引的 `IllegalStateException`。 消费者直接更新 import 与显式提供点，不保留旧
package 转发、typealias 或隐式回退。

## 导航套件

`NomiNavigationSuite(layoutType, modifier, items, footerItems, content)` 是所有设计系统共用的导航入口：
布局类型由调用方按窗口形态选择；主导航项与底部导航项用槽位式的
`NomiNavigationItemScope.NavigationItem(selected, onClick, modifier, icon, badge, label, colors, interactionSource)`
声明。 槽位由调用方组合，图标尺寸与无障碍名称也由调用方负责（中立 IconPack 的 ImageVector 固有尺寸为
256dp，槽内必须显式约束）； 组件本身不读取窗口、不持有选中状态、不补齐统一视觉。`NomiNavigationItemColors`
只提供内容色/选中内容色/禁用内容色/指示器色四类稀疏覆盖。

```kotlin
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import ciyin.designsystem.component.navigation.NomiNavigationSuite
import ciyin.designsystem.component.navigation.NomiNavigationSuiteType
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.icon.Home
import ciyin.designsystem.icon.IconPack
import ciyin.designsystem.theme.NomiTheme

@Composable
fun AppNavigationShell(content: @Composable () -> Unit) {
    NomiNavigationSuite(
        layoutType = NomiNavigationSuiteType.Bar,
        items = {
            NavigationItem(
                selected = true,
                onClick = {},
                icon = { Icon(IconPack.Home, null, Modifier.size(NomiTheme.sizes.icon.large)) },
                label = { NomiText(text = "首页") },
            )
        },
        content = content,
    )
}
```

能力声明 `navigationBar`/`navigationRail`/`navigationDrawer` 决定入口可用布局：默认支持 Bar 与 Rail，
Drawer 需实现显式声明；`navigationItemModifier` 声明是否支持项级 `modifier`（Fluent
原生菜单无项级修饰符，声明为不支持）。 不支持时入口或实现会在进入原生渲染前明确失败，不静默忽略。
载体语义、底部项固定方式与显示模式由各原生实现决定：M3 使用
NavigationBar/NavigationRail/PermanentNavigationDrawer， Fluent 使用 NavigationView 的
Top/Left/LeftCompact，Nomi 使用 Foundation/M3 混合外壳复刻既有视觉。 导航实现位于各 adapter
模块，核心不引用任何内置实现。

## 最小接入：不要主动补齐默认参数

KMP 调用方在自己的构建脚本中声明 `implementation(projects.core.designSystem)`。 下面的调用不传
size、variant、颜色、形状或 padding，保留当前设计系统的默认风格：

```kotlin
import androidx.compose.runtime.Composable
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.card.NomiCard
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme

/** 使用指定设计系统的原生默认卡片与按钮，文案由调用方提供。 */
@Composable
fun ExampleContent(
    designSystem: NomiDesignSystem,
    title: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    NomiTheme(designSystem = designSystem) {
        NomiCard {
            NomiText(text = title)
            NomiButton(onClick = onAction) {
                NomiText(text = actionLabel)
            }
        }
    }
}
```

装配层可依赖[Material 3 adapter](<../design-system-material3/README.md>)或
[Fluent adapter](<../design-system-fluent/README.md>)并通过 `NomiDesignSystem.Material3` /
`NomiDesignSystem.Fluent` 传入对应实现（需导入对应扩展属性）；`designSystem` 默认取当前上下文，
**没有隐式回退**： 未显式提供时 `requireNomiDesignSystem()` 抛出带指引的 `IllegalStateException`。
[sample](<../../app/sample/README.md>)的共享候选目录为 Nomi/M3/Fluent，宿主仍可覆盖，不需要 Desktop
专属注入。

## 请求输入与只读投影分离

`NomiThemeRequest` 携带 requestedMode、核心边界解析后的 isDark 与 `NomiThemeOverrides`。
`NomiDesignSystem.ProvideTheme(request: NomiThemeRequest, content: @Composable (ResolvedNomiTheme) -> Unit)`
先建立原生主题，再通过回调发布直接包含五组结果的只读 `ResolvedNomiTheme`。 核心收到 adapter 回调后以
request 规范化 requestedMode、isDark、overrides，并保留 adapter 返回的
colorScheme、typography、shapes、spacings、sizes 五组结果；不信任 adapter metadata，也不从结果反推请求字段。
`NomiTheme` 仅公开 `darkMode` 及 `colorScheme/typography/shapes/spacings/sizes` 六项只读
getter。五组数据是 **结果投影**，不是下一套设计系统的默认输入；组件必须通过 `NomiTheme` 读取。
主题聚合类型、聚合默认工厂与主题包装均不属于当前 API；不新增 ThemeData/ThemeSnapshot 别名或兼容入口。

- `ResolvedNomiTheme` 的前两个构造参数 requestedMode 与 isDark 必填，其余五组带当前契约默认值，overrides
  默认为空；LocalResolvedNomiTheme 无主题时使用 System + false 的浅色兜底。
- `NomiTheme` 默认 `darkMode = NomiTheme.darkMode`、`designSystem = requireNomiDesignSystem()`；
  `LocalNomiDesignSystem` 可空且无隐式默认，五组直接参数均可空。
- 根重置显式 slot， **不继承父 colorScheme/accent**；仅继承父层明确设置的
  typography/shapes/spacings/sizes，不复制 resolved defaults。
- `NomiThemeOverride` 继承全部显式组、父模式与明确 slot，再合并本层输入；换系统仍不带父系统解析出的默认投影。
- 直接参数与 overrides 同组重复时 require；同层完整 colorScheme 与 accent 并用也 require，不能猜优先级。
- 子层完整 colorScheme 替换会清除继承的 accent；仅传子 accent 则叠加在继承色板上。每个非空对象是完整显式组，不推断
  copy 改了哪些字段。
- 不提供主题聚合输入。五组中立参数可直接传入；库专属完整原生类型配置仍使用 adapter 工厂。页面度量不自动改写控件内边距。
- 默认 Nomi 单组工厂只服务自身/缺省读取，不给 M3 或 Fluent 填充默认样式。

## 旧式直接主题参数与默认颜色工厂

当前主要参数顺序为
`darkMode, colorScheme?, typography?, shapes?, spacings?, sizes?, designSystem, overrides, content`。
`NomiThemeOverride` 也接受五组直接参数；它和根主题的颜色继承语义不同。无需把五组数据包装成新模型。

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.defaultNomiTypography
import ciyin.designsystem.theme.defaultLightNomiColorScheme

/** 显式选择旧默认色板与排版；未传的其他主题组仍按继承规则解析。 */
@Composable
fun ExplicitTheme(content: @Composable () -> Unit) {
    NomiTheme(
        colorScheme = defaultLightNomiColorScheme().copy(primary = Color(0xFF4466AA)),
        typography = defaultNomiTypography(),
        designSystem = NomiDesignSystem.Default,
        content = content,
    )
}
```

[Color](<src/commonMain/kotlin/ciyin/designsystem/theme/Color.kt>)只维护 internal 明暗默认对象；
[ColorScheme](<src/commonMain/kotlin/ciyin/designsystem/theme/ColorScheme.kt>)只提供
`defaultLightNomiColorScheme` / `defaultDarkNomiColorScheme` 两个颜色工厂，各保留 27 个具名参数和默认值。
旧 `ciyin.material.theme` 的 `lightColorScheme` / `darkColorScheme` 作为兼容入口转发到它们；中立包不再提供同名别名。

[Type](<src/commonMain/kotlin/ciyin/designsystem/theme/Type.kt>)取代原 Typography 文件，保留十五角色中立
`NomiTypography`， 不能恢复成 Material Typography
依赖。[Shape](<src/commonMain/kotlin/ciyin/designsystem/theme/Shape.kt>)、
[Spacing](<src/commonMain/kotlin/ciyin/designsystem/theme/Spacing.kt>)、[Size](<src/commonMain/kotlin/ciyin/designsystem/theme/Size.kt>)
与 Type 使用 private Default*Tokens 单一默认来源；旧数值不变。以下 9 个主题 data class
均可跨模块直接构造，并使用生成的成员 `copy`：
`NomiColorScheme`、`NomiTypography`、`NomiShapes`、`Spacings`、`NomiSizes`、`SizeScale`、`Strokes`、
`ComponentHeights`、`LayoutConstraints`。

公开工厂包括
defaultLightNomiColorScheme/defaultDarkNomiColorScheme、defaultNomiTypography、defaultNomiShapes、defaultSpacings、
defaultNomiSizes，以及尺寸子组
defaultIconSizeScale/defaultAvatarSizeScale/defaultStrokes/defaultComponentHeights/defaultLayoutConstraints。
9 个手写核心复制扩展与旧门面的 8 个同名转发已删除，不保留兼容入口；两包调用成员 `copy` 均无需导入。
数据类的相等性、解构与不可变浅复制语义不变；曾显式导入扩展的调用方应删除对应 import。 这些默认工厂仅在调用方明确选用或
Nomi 自身使用时生效，不会覆盖 M3/Fluent 未指定的原生默认。

主题级支持：Nomi/M3 接受五组；M3 映射原生角色并保留中立读模型的完整显式值。 Fluent 只在明确 typography
时映射到八个原生角色，剩余角色仍保留在中立读模型；拒绝完整 colorScheme 和九级 shapes， 需要其原生
Colors/CornerRadius 时使用 `rememberFluentDesignSystem`。零参数 Fluent 不调用中立排版映射。

### 默认对象与复制

默认工厂仍可作为定制基线，也可直接构造；复制始终调用生成的成员，无需 copy import：

```kotlin
import ciyin.designsystem.theme.Spacings
import ciyin.designsystem.theme.defaultSpacings

val spacings = defaultSpacings().copy(large = sectionSpacing)
val directSpacings = Spacings(large = sectionSpacing)
```

构造参数不变：`NomiColorScheme()` 使用原浅色默认；`NomiShapes()`、`Spacings()`、`NomiSizes()` 保留既有默认参数。
`NomiTypography` 的十五角色，以及 `SizeScale`、`Strokes`、`ComponentHeights`、`LayoutConstraints`
的全部字段仍必填，不补零参默认。 颜色/形状/间距/完整尺寸工厂仍返回新外层对象；默认排版与五个尺寸子组工厂仍返回共享不可变对象，
`NomiSizes` 默认子组仍复用这些对象。 成员 `copy`
返回新外层对象，未覆盖字段保留原值/引用，不修改原对象或做深复制。跨模块构造、复制与身份契约见[独立模块测试](<../design-system-fluent/src/desktopTest/kotlin/consumer/ThemeFactoryCopyConsumptionTest.kt>)。

## 未指定、显式覆盖与不支持

| 参数                                                                               | 未指定语义                         | 显式语义                                 |
|------------------------------------------------------------------------------------|------------------------------------|------------------------------------------|
| Text style；Button variant/size/shape/contentPadding/textStyle；Surface/Card shape | null 保留 native default           | 按当前系统能力消费                       |
| `NomiButtonColors` / `NomiSurfaceColors` 的单个字段                                | Color.Unspecified 保留该字段原生值 | Transparent 也是有效覆盖；只覆盖指定字段 |
| `NomiBorder`                                                                       | Default 保留原生描边               | None 移除；Stroke 指定描边               |
| Card contentPadding                                                                | null 保留原生 slot 默认            | 明确值只控制内容 slot，不重绘 Card       |

Defaults 工厂仍可被调用方主动选用，但普通组件调用不自动调用它们；不能为三个系统预填同一
metrics、圆角或最小高度。
`NomiCard` 不再是统一 `Surface + padding + Column` 配方；它是非点击卡片，交给表面族的真实 `Card` 方法。
描边只使用 NomiBorder.Default/None/Stroke，不提供 nullable BorderStroke 兼容重载；不保留本会话生成的短名转发入口。

当前能力边界：

- Nomi/M3 支持当前公开的视觉覆盖；Nomi 保留自己的 Foundation 行为。
- Fluent Button 拒绝非 null 的 size、shape、contentPadding；保留原生按钮，不用包装/重绘伪装支持。
- Fluent 静态 Card 拒绝指定颜色和非 Default border；需要这些能力使用 NomiSurface。Card 支持 shape 和内容
  slot padding。
- [能力声明](<src/commonMain/kotlin/ciyin/designsystem/component/NomiComponentCapabilities.kt>)用于
  UI 提示和调用边界快速失败； 不支持的输入不能静默丢弃，也不能被自动过滤为 null。

## 文本与 slot

[NomiText](<src/commonMain/kotlin/ciyin/designsystem/component/text/NomiText.kt>)与 renderer SPI 提供
String 与 AnnotatedString 两个重载，
逐字段覆盖（fontSize/fontStyle/fontWeight/fontFamily/letterSpacing/textDecoration/textAlign/lineHeight）、onTextLayout、autoSize
与 inlineContent 原样透传； 未指定（null 或 Unspecified）沿用 style
与原生文本环境，保持原生优先级"显式字段 > style > 原生 LocalTextStyle/LocalContentColor"。

[ContentStyle](<src/commonMain/kotlin/ciyin/designsystem/runtime/ContentStyle.kt>)只表示显式 slot
增量。 颜色优先级为显式 color > 本次 style.color > 显式 slot 色 > 继承样式色；仍未指定则把 Unspecified
留给原生 renderer。 没有显式样式时保持 null，不在核心合并统一 bodyMedium。原生按钮中的文本依赖原生真实内容色/排版，
不得把主题默认投影为显式 slot，覆盖其 disabled、hover 或其它原生状态。

## 实现、扩展与平台

Nomi 的 Text/Button/Surface/Card 与导航实现位于独立模块 `:core:design-system-nomi`，由
internal [DefaultNomiSystem](<../design-system-nomi/src/commonMain/kotlin/ciyin/designsystem/nomi/DefaultNomiDesignSystem.kt>)
组装并经 `NomiDesignSystem.Default` 暴露；核心不引用该模块。
[IconPack](<src/commonMain/kotlin/ciyin/designsystem/icon/IconPack.kt>)
仍为矢量单一来源，旧入口由[门面](<../material/README.md>)转发。

renderer 是跨模块公开 SPI。新增能力同时更新 adapter、能力声明和外部消费测试，不增空二期接口。
直接参数调用恢复沿用既有请求、SPI 和 locals，不另增模型；中立 `NomiTypography` 与原生 Material 类型仍不同。
旧 `ciyin.material` 源码兼容及原始 Typography 身份由门面和 M3 特定配置保留。

[构建](<build.gradle.kts>)
沿用[目标约定](<../../buildSrc/src/main/kotlin/multiplatform-lib-targets.gradle.kts>)； Android
与项目开启的 Desktop/iOS/JS/Wasm 目标分别验证，不擅自更改平台开关。Fluent 仍为跨平台 adapter。

## 验证入口

```powershell
.\gradlew.bat :core:design-system:compileCommonMainKotlinMetadata --console=plain
.\gradlew.bat :core:design-system:desktopTest --console=plain
```

Nomi 默认渲染与组件契约已随实现迁至 [core/design-system-nomi](<../design-system-nomi/README.md>)
，由该模块 `desktopTest` 独立验证；核心只保留公共契约与 adapter 无关测试。

验证零参数请求不被填默认、单字段颜色覆盖、三态 border、能力拒绝、跨系统 Override 只继承显式值和 slot 恢复。
无显式设计系统时 `NomiTheme`、四组件与导航套件入口必须抛出带指引的 `IllegalStateException`
，不能被隐式回退掩盖（[契约测试](<src/commonTest/kotlin/ciyin/designsystem/NoImplicitDesignSystemContractTest.kt>)）。
原生 adapter 还须对照真实库组件测量/截图，不能用旧统一外观测试证明 native parity。 还需验证旧 27 色/15
排版/形状/间距/尺寸独立基线、root 与 Override 的显式组继承、同层重复拒绝、Fluent 支持边界。 9
类跨模块公开构造、无扩展导入的成员复制、默认工厂身份等测试在上一轮（主题聚合层删除）继续通过：核心共 45 项
Desktop 回归。 本轮新增导航组件族与三适配后：核心 42、Nomi 18、M3 30、Fluent 46、旧门面 19、sample 25（
`--tests "*DesignSystemDemo*"`）共 180 项 Desktop 回归通过，零失败、零跳过；此前核心 43 项及联合 148 项
Desktop 结果仅作历史记录，历史截图仍只作参考，当前编译/单测不代表设备或发布验收。

## 相关资料

- [核心与 adapter 技能](<../../.agents/skills/core-design-system/SKILL.md>)
- [当前设计说明、历史证据及验收缺口](<../../.docs/design/design-system-模块化方案.md>)
- [Material 3 adapter](<../design-system-material3/README.md>) / [Fluent adapter](<../design-system-fluent/README.md>)
- [旧包兼容](<../material/README.md>) / [sample](<../../app/sample/README.md>)
