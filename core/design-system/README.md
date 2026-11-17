# `:core:design-system`

## 用途与非目标

本模块提供统一的 Compose 调用入口、设计系统 SPI 与语义只读投影；内置 Nomi
实现位于独立模块 [core/design-system-nomi](<../design-system-nomi/README.md>)，核心不引用它。
**统一的是调用方式，不是三套组件的默认外观。** 未指定视觉参数时，Material 3 / Fluent 应保留各自原生默认；
Nomi 默认只属于 Nomi，不能在核心派发前填入其他 adapter。

生产依赖仅为 Compose Runtime/Foundation/UI，不依赖 Material 3、Fluent、旧门面或 UI foundation。 当前提供
`NomiText`、`NomiButton`、`NomiSurface`、`NomiCard`、`NomiNavigationSuite`、`NomiScaffold` 与
`NomiListItem`；不负责窗口形态判断、应用路由、平台窗口、持久化设置或全部第三方控件。
原生默认组件保持不变；旧式主题文件职责、颜色工厂与独立 nomi 包沿用此前实现，主题入口只接受
`NomiTheme` 值加映射契约。 当前 9 个主题 data class 公开构造，复制使用编译器生成的成员 `copy`
；默认工厂、默认值、必填参数与身份语义不变。 上一轮删除主题聚合层时，核心/M3/Fluent metadata、sample
Android 编译及 154 项 Desktop 回归通过，零失败、零跳过；导航轮 180 项与命名空间轮 218 项为历史记录。本轮主题改为
NomiTheme 值 + 映射函数后，四模块
`compileCommonMainKotlinMetadata`、app shared/desktop 与 sample 编译通过，核心 30、M3 35、Fluent 52、Nomi
23、sample 25 共 165 项 Desktop 回归零失败、零跳过；`core:material` 19 项同样通过。完整范围见
[设计说明](<../../.docs/design/design-system-模块化方案.md>)。

## 命名边界

Nomi 表示 NomiKit 公共设计体系，不限定当前选用的视觉实现。公开入口统一为
NomiTheme/NomiText/NomiButton/NomiSurface/NomiCard， 主题数据与默认工厂同样使用 Nomi 前缀；新核心不保留
App* 别名。根包 ciyin.designsystem 和设计系统标识保持不变，组件 package 按下述子目录细分。
NomiDesignSystem 是映射契约；Nomi 默认实现以扩展属性暴露，Material 3 / Fluent 以同名扩展属性与工厂
`NomiDesignSystem.Material3` / `NomiDesignSystem.Fluent`、`Material3(map)` / `Fluent(map)` 暴露，实现对象本身为
internal。 默认 renderer 使用 DefaultNomi* 名称，原生解析上下文使用 LocalNomiNative*，不得与公共显式
slot 的 LocalNomi* 混用。 应用层 App/AppPreview/AppBuildConfig 等名称不变；ciyin.material 的
AppTheme/AppColorScheme/AppShapes/AppSizes 仅作为旧兼容入口保留。

## 绑定命名空间与映射工厂

`NomiDesignSystem` 自带空 `companion object`，只作为 adapter 的绑定命名空间。adapter 用扩展属性或扩展函数把实现挂上去：

```kotlin
// :core:design-system-nomi
val NomiDesignSystem.Companion.Default: NomiDesignSystem
// :core:design-system-material3
val NomiDesignSystem.Companion.Material3: NomiDesignSystem
fun NomiDesignSystem.Companion.Material3(map: NomiTheme.() -> Material3Theme): NomiDesignSystem
// :core:design-system-fluent
val NomiDesignSystem.Companion.Fluent: NomiDesignSystem
fun NomiDesignSystem.Companion.Fluent(map: NomiTheme.() -> FluentTheme): NomiDesignSystem
```

调用方只依赖核心契约，按 `NomiDesignSystem.` 补全并导入对应扩展声明；扩展声明不是接口成员，
**必须显式导入**：

```kotlin
import ciyin.designsystem.material3.Material3
import ciyin.designsystem.material3.toPrimaryMaterial3Theme
import ciyin.designsystem.theme.NomiTheme

NomiTheme(designSystem = NomiDesignSystem.Material3) {
    // 原生默认主题
}
NomiTheme(designSystem = NomiDesignSystem.Material3 { toPrimaryMaterial3Theme() }) {
    // 只让 Nomi 主色覆盖原生主色
}
```

- 伴生对象没有状态：用扩展声明挂实现，核心不反向依赖 adapter，adapter 之间也互不依赖。
- `Default` 是恒等单例；无参 `Material3` / `Fluent` 返回使用原生默认映射的共享实例，带 `map`
  的工厂返回配置实例。
- `NomiTheme(designSystem = ...)` 与 `CompositionLocalProvider(LocalNomiDesignSystem provides ...)`
  的显式提供契约不变；未提供时 `requireNomiDesignSystem()` 明确失败。

## 从哪里开始

| 入口                                                                                                                                                                           | 职责                                                                              |
|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------|
| [NomiTheme](<src/commonMain/kotlin/ciyin/designsystem/theme/NomiTheme.kt>)                                                                                                     | 不可变主题值、`LocalNomiTheme`、伴生只读 getter 与唯一入口                        |
| [ColorScheme](<src/commonMain/kotlin/ciyin/designsystem/theme/ColorScheme.kt>) / [Type](<src/commonMain/kotlin/ciyin/designsystem/theme/Type.kt>)                              | defaultLight/defaultDark 颜色工厂、NomiColorScheme 与中立 NomiTypography          |
| [NomiDesignSystem](<src/commonMain/kotlin/ciyin/designsystem/runtime/NomiDesignSystem.kt>)                                                                                     | 映射契约、`ProvideTheme(theme, content)`、`LocalNomiDesignSystem` 与 require 入口 |
| [NomiText](<src/commonMain/kotlin/ciyin/designsystem/component/text/NomiText.kt>) / [NomiButton](<src/commonMain/kotlin/ciyin/designsystem/component/button/NomiButton.kt>)    | 文本与按钮 API、对应 renderer 和参数类型                                          |
| [NomiSurface](<src/commonMain/kotlin/ciyin/designsystem/component/surface/NomiSurface.kt>) / [NomiCard](<src/commonMain/kotlin/ciyin/designsystem/component/card/NomiCard.kt>) | Surface 与实际原生 Card 分开派发                                                  |
| [NomiBorder](<src/commonMain/kotlin/ciyin/designsystem/component/NomiBorder.kt>)                                                                                               | 描边意图三态                                                                      |
| [NomiNavigation](<src/commonMain/kotlin/ciyin/designsystem/component/navigation/NomiNavigation.kt>)                                                                            | 导航套件入口、Bar/Rail/Drawer 布局、导航项作用域与原生渲染契约                    |
| [NomiScaffold](<src/commonMain/kotlin/ciyin/designsystem/component/scaffold/NomiScaffold.kt>)                                                                                  | 页面脚手架入口、顶栏/主体/底栏三槽位、容器色与内容色稀疏覆盖                      |
| [NomiListItem](<src/commonMain/kotlin/ciyin/designsystem/component/list/NomiListItem.kt>)                                                                                      | 列表项入口、前导/标题/副标题/尾部槽位与原生列表渲染契约                           |

[组件集合](<src/commonMain/kotlin/ciyin/designsystem/component/NomiDesignSystemComponents.kt>)组装
text/actions/surfaces/navigation/scaffold/listItems 六族； 表面族同时有 `Surface` 和 `Card`
方法，导航族提供套件入口， 脚手架族提供页面外壳入口，列表项族提供原生列表行入口。实现不再集中在一个组件大文件，Nomi
也按组件子包拆分。

公共 `component` 保留公共入口、renderer SPI
与共享参数；内置实现已拆到独立模块 [core/design-system-nomi](<../design-system-nomi/README.md>)， 包名仍为
`ciyin.designsystem.nomi`，按 `text/button/surface/card` 组织。公共主题 API 与数据留在 `theme`
，设计系统契约与显式 slot 留在 `runtime`。
`LocalNomiDesignSystem` 为可空且 **无隐式默认**：应用根、测试与 Preview 必须显式提供实现；未提供时组件与
`NomiTheme` 抛出带指引的 `IllegalStateException`。 消费者直接更新 import 与显式提供点，不保留旧
package 转发、typealias 或隐式回退。

## 导航套件

`NomiNavigationSuite(layoutType, modifier, containerColor, items, footerItems, content)`
是所有设计系统共用的导航入口： 布局类型由调用方按窗口形态选择；主导航项与底部导航项用槽位式的
`NomiNavigationItemScope.NavigationItem(selected, onClick, modifier, icon, badge, label, colors, interactionSource)`
声明。 槽位由调用方组合，图标尺寸与无障碍名称也由调用方负责（中立 IconPack 的 ImageVector 固有尺寸为
256dp，槽内必须显式约束）； 组件本身不读取窗口、不持有选中状态、不补齐统一视觉。 外壳容器色通过
`containerColor: Color = Color.Unspecified` 稀疏覆盖：Unspecified 保留当前设计系统的原生 导航容器色，
`Color.Transparent` 是有效覆盖；`NomiNavigationItemColors` 只提供导航项的内容色/选中内容色/
禁用内容色/指示器色四类稀疏覆盖。

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

入口不按布局或容器色拦截：Bar/Rail/Drawer 与 `containerColor` 原样交给当前实现。实现无法承载时自行降级
（保留原生默认外观）并通过 `ciyin.platform.logger` 记录警告，不重绘伪造；因此运行时切换设计系统不会因能力差异
抛异常。Fluent 的容器色与项级 `modifier` 属于这种降级范围：其 `NavigationView.color` 作用于内容面板而不是
导航容器。 载体语义、底部项固定方式与显示模式由各原生实现决定：M3 使用
NavigationBar/NavigationRail/PermanentNavigationDrawer， Fluent 使用 NavigationView 的
LeftCollapsed/LeftCompact/Left，三者都走原生 NavigationView；Nomi 使用 Foundation/M3 混合外壳复刻既有视觉
（Bar/Rail 用 M3 原生导航栏，Drawer 用 M3 永久抽屉 + Nomi 令牌抽屉项）；Nomi 的 Bar 只渲染主导航项（复刻旧底部栏
在分组占位项处终止），底部组只在 Rail/Drawer 展示。导航实现位于各 adapter 模块，核心不引用任何内置实现。

## 列表项

`NomiListItem(title, modifier, onClick, leading, subtitle, trailing)` 是第六个组件族：槽位为 null
时不占位，行高、容器、圆角、悬停/按下/点击状态与文本样式全部沿用当前设计系统的原生列表项；核心不补视觉参数，
也不接受颜色、形状、内边距或高度覆盖。

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import ciyin.designsystem.component.list.NomiListItem
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.icon.ArrowRight
import ciyin.designsystem.icon.IconPack
import ciyin.designsystem.icon.LightMode
import ciyin.designsystem.theme.NomiTheme

NomiListItem(
    title = { NomiText("深色模式") },
    subtitle = { NomiText("跟随系统") },
    leading = { Icon(IconPack.LightMode, null, Modifier.size(NomiTheme.sizes.icon.large)) },
    trailing = {
        Row(horizontalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small)) {
            NomiText("跟随系统")
            Icon(IconPack.ArrowRight, null, Modifier.size(NomiTheme.sizes.icon.small))
        }
    },
    onClick = { onOpenAppearance() },
)
```

- `onClick = null` 表示静态行，不注册点击语义，实现不得添加悬停/按下外观。
- 标题与副标题建议使用 `NomiText` 以继承原生列表文本样式与内容色；图标尺寸与无障碍名称由调用方负责，
  中立 `IconPack` 的固有尺寸为 256dp，槽内必须显式约束。
- 尾部只有一个原生槽位，“值文本 + 箭头”等组合由调用方在槽内完成；行内含 `NomiButton` 等交互内容时，
  各自保留原生点击语义。
- 本版不提供 `enabled`、选中、展开、`overline`、分组标题、分隔线及颜色/形状/内边距覆盖；不可点统一用
  `onClick = null` 表达，选择控件（Switch/Radio/Checkbox）待控件族落地后在尾部槽组合。
- 三端映射：Material 3 使用原生 `ListItem`，仅在有 `onClick` 时包装 `Modifier.clickable`；Fluent 使用
  `CardExpanderItem` 的点击/静态重载，标题槽注入 bodyStrong、尾部槽注入 caption 与 secondary 内容色，
  不绘制展开箭头；Nomi 使用透明行与 `NomiTheme` 令牌。
- 删除 `:core:ui-foundation` 旧 `SettingItem` 后，设置行由本族组合：标题/副标题/前导/尾部槽 + 行点击。

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
`NomiDesignSystem.Fluent` 取得实现（需导入对应扩展属性/工厂）；主题入口的 `designSystem` 是必填参数，
**没有隐式回退**；组件在主题边界之外由 `requireNomiDesignSystem()` 抛出带指引的
`IllegalStateException`。
[sample](<../../app/sample/README.md>)的共享候选目录为 Nomi/M3/Fluent，宿主仍可覆盖，不需要 Desktop
专属注入。

## 主题值与映射分离

`NomiTheme` 是不可变主题值（isDark、colorScheme、typography、shapes、spacings、sizes），由唯一入口安装：
`NomiTheme(designSystem, darkMode = DarkMode.System, theme = rememberNomiTheme(darkMode), content)`。
省略 `theme` 时按 `darkMode` 解析一次模式并使用浅/深默认主题；显式传入 `theme` 时按原值安装，
`darkMode` 不再生效。 入口自己发布 `LocalNomiTheme`，
`NomiDesignSystem.ProvideTheme(theme, content)`
只把该值映射为原生主题；`NomiTheme.colorScheme` 等伴生 getter 读取的是本层显式传入值，不是原生解析结果。

```kotlin
// 根入口：跟随系统
NomiTheme(designSystem = NomiDesignSystem.Default) { ... }
// 固定浅色/深色
NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Dark) { ... }
// 局部/自定义主题；继承父主题时显式传当前值
NomiTheme(designSystem = NomiDesignSystem.Material3, theme = LocalNomiTheme.current) { ... }
```

- 五组均为完整值，不做字段级合并；`isDark` 是已解析布尔，`DarkMode.System` 由入口在 `theme` 省略时解析一次。
- 入口每次进入都重置显式内容 slot；`designSystem` 必填，`darkMode` 只在 `theme` 省略时选择默认主题，
  嵌套继承需显式传 `theme = LocalNomiTheme.current`。
- 适配器不读系统状态；Material3 / Fluent 的原生色板基线由映射函数返回的 `isDark` 决定。
- 不做 native 到中立的反向投影；原生视觉由原生组件读原生主题，中立读取值不回填原生默认。
- `NomiThemeRequest`、`ResolvedNomiTheme`、`NomiThemeOverrides`、`NomiThemeOverride` 与主题聚合包装都不是当前
  API。

## 主题映射与默认颜色工厂

Material3 / Fluent 工厂接收 `NomiTheme.() -> 原生主题值` 接收者映射函数；不传映射时使用各自原生默认。
映射函数 是普通 lambda，可以只接管关心的角色：

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.material3.lightColorScheme
import ciyin.designsystem.material3.Material3
import ciyin.designsystem.material3.Material3Theme
import ciyin.designsystem.material3.toPrimaryMaterial3Theme
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme

@Composable
fun PrimaryMapped(content: @Composable () -> Unit) {
    NomiTheme(
        darkMode = DarkMode.Dark,
        designSystem = NomiDesignSystem.Material3 { toPrimaryMaterial3Theme() },
        content = content,
    )
}

// 自定义：只让 Nomi 主色与错误色覆盖原生主题
val map: NomiTheme.() -> Material3Theme = {
    Material3Theme(
        isDark = isDark,
        colorScheme = lightColorScheme().copy(
            primary = colorScheme.primary,
            error = colorScheme.error,
        ),
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

主题级映射：Nomi 恒等；M3 可映射整组或只映射指定角色；Fluent 的颜色只映射 primary、排版映射八个角色、
圆角映射三个原生值，其余保留原生主题。映射函数只能访问真实存在的原生角色，因此不需要运行时降级告警。
预设扩展
`toNativeMaterial3Theme` / `toPrimaryMaterial3Theme` / `toFullMaterial3Theme` 与
`toNativeFluentTheme` / `toPrimaryFluentTheme` / `toFullFluentTheme` 覆盖常见用法；自定义 lambda
应是稳定引用或 `remember` 后的值。

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
| Text style；Button variant/size/shape/contentPadding/textStyle；Surface/Card shape | null 保留 native default           | 按当前实现消费；无法承载时降级为原生默认 |
| `NomiButtonColors` / `NomiSurfaceColors` 的单个字段                                | Color.Unspecified 保留该字段原生值 | Transparent 也是有效覆盖；只覆盖指定字段 |
| `NomiBorder`                                                                       | Default 保留原生描边               | None 移除；Stroke 指定描边               |
| Card contentPadding                                                                | null 保留原生 slot 默认            | 明确值只控制内容 slot，不重绘 Card       |

Defaults 工厂仍可被调用方主动选用，但普通组件调用不自动调用它们；不能为三个系统预填同一
metrics、圆角或最小高度。
`NomiCard` 不再是统一 `Surface + padding + Column` 配方；它是非点击卡片，交给表面族的真实 `Card` 方法。
描边只使用 NomiBorder.Default/None/Stroke，不提供 nullable BorderStroke 兼容重载；不保留本会话生成的短名转发入口。

适配器降级边界：

- Nomi/M3 支持当前公开的视觉覆盖；Nomi 保留自己的 Foundation 行为。
- Fluent 无法表达 Button size/shape/contentPadding、静态 Card colors/border、Text autoSize、导航容器色与
  导航项 modifier：忽略覆盖、保留原生默认，并通过 `ciyin.platform.logger` 记录一次警告；不包装/重绘伪装支持。
- 核心不提供能力声明类型，也不按能力拦截参数；调用方无需查询能力即可切换设计系统。

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

renderer 是跨模块公开 SPI。新增参数同时更新 adapter 与外部消费测试，不增空二期接口。 主题值统一经
`NomiTheme` 传入，原生角色由 adapter 映射函数决定；中立 `NomiTypography` 与原生 Material 类型仍不同。 旧
`ciyin.material` 源码兼容及原始 Typography 身份由门面和 M3 特定配置保留。

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

验证零参数请求不被填默认、单字段颜色覆盖、三态 border、适配器降级渲染、跨系统 Override 只继承显式值和
slot 恢复。 无显式设计系统时 `NomiTheme`、各组件与导航套件入口必须抛出带指引的 `IllegalStateException`
，不能被隐式回退掩盖（[契约测试](<src/commonTest/kotlin/ciyin/designsystem/NoImplicitDesignSystemContractTest.kt>)）。
列表项由 [ListItemDispatchContractTest](<src/commonTest/kotlin/ciyin/designsystem/ListItemDispatchContractTest.kt>)
验证默认稀疏派发、槽位不占位与显式参数透传；三端适配分别验证点击/静态行与原生文本环境。 原生 adapter
还须对照真实库组件测量/截图，不能用旧统一外观测试证明 native parity。 还需验证旧 27 色/15
排版/形状/间距/尺寸独立基线、root 与 Override 的显式组继承、适配器降级不抛异常。 本轮移除
旧式直接参数主题入口后：核心 40、M3 33、Fluent 46、Nomi 23、sample 24 共 166 项 Desktop 回归通过，零失败、零跳过；
导航轮 180 项、命名空间轮 218 项、此前的核心 43 项与联合 148 项结果仅作历史记录，历史截图仍只作参考，当前
编译/单测不代表设备或发布验收。

## 相关资料

- [核心与 adapter 技能](<../../.agents/skills/core-design-system/SKILL.md>)
- [当前设计说明、历史证据及验收缺口](<../../.docs/design/design-system-模块化方案.md>)
- [Material 3 adapter](<../design-system-material3/README.md>) / [Fluent adapter](<../design-system-fluent/README.md>)
- [旧包兼容](<../material/README.md>) / [sample](<../../app/sample/README.md>)