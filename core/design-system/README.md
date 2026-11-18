# `:core:design-system`

## 用途与非目标

本模块提供统一的 Compose 调用入口、设计系统 SPI 与语义只读投影；内置 Nomi 实现位于独立模块
[core/design-system-nomi](<../design-system-nomi/README.md>)，核心不引用它。
**统一的是调用方式，不是各套组件的默认外观。** 未指定视觉参数时，各实现应保留各自原生默认； Nomi 默认只属于
Nomi，不能在核心派发前填入其他 adapter。

生产依赖仅为 Compose Runtime/Foundation/UI，不依赖任何原生 UI 库、旧门面或 UI foundation。 当前提供
`NomiText`、`NomiButton`、`NomiSurface`、`NomiCard`、`NomiNavigationSuite`、
`NomiScaffold` 与 `NomiListItem`；不负责窗口形态判断、应用路由、平台窗口、持久化设置或全部第三方控件。
原生默认组件保持不变；旧式主题文件职责、颜色工厂与独立 nomi 包沿用此前实现，主题入口只接受
`NomiTheme` 值加映射契约。 当前 9 个主题 data class 公开构造，复制使用编译器生成的成员 `copy`；
默认工厂、默认值、必填参数与身份语义不变。完整范围与验收记录见
[设计说明](<../../.docs/design/design-system-模块化方案.md>)。

## 命名边界

Nomi 表示 NomiKit 公共设计体系，不限定当前选用的视觉实现。公开入口统一为
NomiTheme/NomiText/NomiButton/NomiSurface/NomiCard， 主题数据与默认工厂同样使用 Nomi 前缀；新核心不保留
App* 别名。根包 ciyin.designsystem 和设计系统标识保持不变，组件 package 按下述子目录细分。
NomiDesignSystem 是映射契约；内置 Nomi 实现以扩展属性 `NomiDesignSystem.Default` 暴露，其它 adapter 以
各自模块声明的同形状扩展属性与工厂暴露，实现对象本身为 internal。 默认 renderer 使用 DefaultNomi* 名称，
原生解析上下文使用 LocalNomiNative*，不得与公共显式 slot 的 LocalNomi* 混用。 应用层
App/AppPreview/AppBuildConfig 等名称不变；ciyin.material 的
AppTheme/AppColorScheme/AppShapes/AppSizes 仅作为旧兼容入口保留。

## 绑定命名空间

`NomiDesignSystem` 自带空 `companion object`，只作为 adapter 的绑定命名空间：核心不声明任何具体
实现，adapter 在自己的模块里用扩展属性或扩展函数把实现挂上去。

```kotlin
// :core:design-system-nomi
val NomiDesignSystem.Companion.Default: NomiDesignSystem
// 其它 adapter 模块各自声明同形状的扩展属性与工厂；
// 签名、默认映射与是否提供共享实例由该模块决定
```

调用方只依赖核心契约，按 `NomiDesignSystem.` 补全并导入对应扩展声明；扩展声明不是接口成员，
**必须显式导入**。

- 伴生对象没有状态：用扩展声明挂实现，核心不反向依赖 adapter，adapter 之间也互不依赖。
- `Default` 是恒等单例；其它 adapter 的无参实例与带映射工厂的身份语义由各自模块文档说明。
- `NomiTheme(designSystem = ...)` 与 `CompositionLocalProvider(LocalNomiDesignSystem provides ...)`
  的显式提供契约不变；未提供时 `requireNomiDesignSystem()` 明确失败。

## 从哪里开始

| 入口                                                                                                                                                                           | 职责                                                                              |
|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------|
| [NomiTheme](<src/commonMain/kotlin/ciyin/designsystem/theme/NomiTheme.kt>)                                                                                                     | 不可变主题值、`LocalNomiTheme`、伴生只读 getter 与唯一入口                        |
| [ColorScheme](<src/commonMain/kotlin/ciyin/designsystem/theme/ColorScheme.kt>) / [Type](<src/commonMain/kotlin/ciyin/designsystem/theme/Type.kt>)                              | defaultLight/defaultDark 颜色工厂、NomiColorScheme 与中立 NomiTypography          |
| [NomiDesignSystem](<src/commonMain/kotlin/ciyin/designsystem/runtime/NomiDesignSystem.kt>)                                                                                     | 映射契约、`ProvideTheme(theme, content)`、`LocalNomiDesignSystem` 与 require 入口 |
| [NomiNativeSpec](<src/commonMain/kotlin/ciyin/designsystem/runtime/NomiNativeSpec.kt>)                                                                                         | 专属参数族标记、泛型契约、`NomiNativeSpecs` 组合与 `findSpec` 取用                |
| [NomiText](<src/commonMain/kotlin/ciyin/designsystem/component/text/NomiText.kt>) / [NomiButton](<src/commonMain/kotlin/ciyin/designsystem/component/button/NomiButton.kt>)    | 文本与按钮 API、对应 renderer 和参数类型                                          |
| [NomiSurface](<src/commonMain/kotlin/ciyin/designsystem/component/surface/NomiSurface.kt>) / [NomiCard](<src/commonMain/kotlin/ciyin/designsystem/component/card/NomiCard.kt>) | Surface 与实际原生 Card 分开派发                                                  |
| [NomiBorder](<src/commonMain/kotlin/ciyin/designsystem/component/NomiBorder.kt>)                                                                                               | 描边意图三态                                                                      |
| [NomiNavigation](<src/commonMain/kotlin/ciyin/designsystem/component/navigation/NomiNavigation.kt>)                                                                            | 导航套件入口、Bar/Rail/Drawer 布局、导航项作用域与原生渲染契约                    |
| [NomiScaffold](<src/commonMain/kotlin/ciyin/designsystem/component/scaffold/NomiScaffold.kt>)                                                                                  | 页面脚手架入口、顶栏/主体/底栏三槽位、容器色与内容色稀疏覆盖                      |
| [NomiListItem](<src/commonMain/kotlin/ciyin/designsystem/component/list/NomiListItem.kt>)                                                                                      | 列表项入口、前导/标题/副标题/尾部槽位、`nativeSpec` 与原生列表渲染契约            |

[组件集合](<src/commonMain/kotlin/ciyin/designsystem/component/NomiDesignSystemComponents.kt>)组装
text/actions/surfaces/navigation/scaffold/listItems 六族； 表面族同时有 `Surface` 和 `Card`
方法，导航族提供套件入口， 脚手架族提供页面外壳入口，列表项族提供原生列表行入口。实现不再集中在一个组件大文件，Nomi
也按组件子包拆分。

公共 `component` 保留公共入口、renderer SPI
与共享参数；内置实现已拆到独立模块 [core/design-system-nomi](<../design-system-nomi/README.md>)， 包名仍为
`ciyin.designsystem.nomi`，按 `text/button/surface/card` 组织。公共主题 API 与数据留在 `theme`
，设计系统契约、专属参数与显式 slot 留在 `runtime`。
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
                icon = {
                    Image(
                        painter = rememberVectorPainter(IconPack.Home),
                        contentDescription = null,
                        modifier = Modifier.size(NomiTheme.sizes.icon.large),
                    )
                },
                label = { NomiText(text = "首页") },
            )
        },
        content = content,
    )
}
```

入口不按布局或容器色拦截：Bar/Rail/Drawer 与 `containerColor` 原样交给当前实现。实现无法承载时自行降级
（保留原生默认外观）并通过 `ciyin.platform.logger` 记录警告，不重绘伪造；因此运行时切换设计系统不会因能力差异
抛异常。 底部组是否参与布局、导航项修饰符与容器色能否承载等载体语义，由各实现决定并在其模块 README 中列出；
核心只约定"按声明顺序渲染、无法承载时降级告警"。

## 列表项

`NomiListItem(title, modifier, leading, subtitle, trailing, onClick, nativeSpec)` 是第六个组件族：槽位为
null 时不占位，行高、容器、圆角、悬停/按下/点击状态与文本样式全部沿用当前设计系统的原生列表项；核心不补视觉
参数，中立参数也不接受颜色、形状、内边距或高度覆盖。原生独有属性通过 `nativeSpec` 透传。

```kotlin
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import ciyin.designsystem.component.list.NomiListItem
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.icon.ArrowRight
import ciyin.designsystem.icon.IconPack
import ciyin.designsystem.icon.LightMode
import ciyin.designsystem.theme.NomiTheme

NomiListItem(
    title = { NomiText("深色模式") },
    subtitle = { NomiText("跟随系统") },
    leading = {
        Image(
            painter = rememberVectorPainter(IconPack.LightMode),
            contentDescription = null,
            modifier = Modifier.size(NomiTheme.sizes.icon.large),
        )
    },
    trailing = {
        Row(horizontalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small)) {
            NomiText("跟随系统")
            Image(
                painter = rememberVectorPainter(IconPack.ArrowRight),
                contentDescription = null,
                modifier = Modifier.size(NomiTheme.sizes.icon.small),
            )
        }
    },
    onClick = { onOpenAppearance() },
)
```

- `onClick = null` 表示静态行，不注册点击语义，实现不得添加悬停/按下外观。
- 标题与副标题建议使用 `NomiText` 以继承原生列表文本样式与内容色；图标尺寸与无障碍名称由调用方负责，
  中立 `IconPack` 的固有尺寸为 256dp，槽内必须显式约束。
- 尾部只有一个原生槽位，"值文本 + 箭头"等组合由调用方在槽内完成；行内含 `NomiButton` 等交互内容时，
  各自保留原生点击语义。
- 中立 API 不提供 `enabled`、选中、展开、`overline`、分组标题、分隔线与颜色/形状/内边距覆盖；其中当前
  设计系统原生存在、而中立签名没有入口的属性改由 `nativeSpec` 透传。不可点统一用 `onClick = null` 表达，
  选择控件（Switch/Radio/Checkbox）待控件族落地后在尾部槽组合。
- 删除 `:core:ui-foundation` 旧 `SettingItem` 后，设置行由本族组合：标题/副标题/前导/尾部槽 + 行点击。

### 原生透传

`nativeSpec` 承载"当前设计系统原生存在、但没有进入中立 API"的属性。具体 spec 类型由 adapter 模块
定义，核心不引用也不解析任何原生类型；实现按类型取用自己认识的那个，其余静默忽略，共享页面不需要按设计系统分支。

参数类型是 **族契约**而不是通用桶：每个入口对应一个族标记与类型别名，其它族的 spec 传进来会在
编译期失败，不需要运行时兜底。各 adapter 的 spec 字段、组合方式与降级行为分别见对应模块的 README。

| 入口                                                                                                         | 族契约                                                             |
|--------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------|
| [NomiText](<src/commonMain/kotlin/ciyin/designsystem/component/text/NomiTextSpec.kt>)                        | `NomiTextSpec`（预留：中立 API 已 1:1 暴露原生 Text 参数）         |
| [NomiButton](<src/commonMain/kotlin/ciyin/designsystem/component/button/NomiButtonSpec.kt>)                  | `NomiButtonSpec`                                                   |
| [NomiSurface](<src/commonMain/kotlin/ciyin/designsystem/component/surface/NomiSurfaceSpec.kt>)               | `NomiSurfaceSpec`                                                  |
| [NomiCard](<src/commonMain/kotlin/ciyin/designsystem/component/card/NomiCardSpec.kt>)                        | `NomiCardSpec`                                                     |
| [NomiNavigationSuite](<src/commonMain/kotlin/ciyin/designsystem/component/navigation/NomiNavigationSpec.kt>) | `NomiNavigationSpec`                                               |
| [NavigationItem](<src/commonMain/kotlin/ciyin/designsystem/component/navigation/NomiNavigationItemSpec.kt>)  | `NomiNavigationItemSpec`（预留：三种布局的原生项颜色类型互不相同） |
| [NomiScaffold](<src/commonMain/kotlin/ciyin/designsystem/component/scaffold/NomiScaffoldSpec.kt>)            | `NomiScaffoldSpec`                                                 |
| [NomiListItem](<src/commonMain/kotlin/ciyin/designsystem/component/list/NomiListItemSpec.kt>)                | `NomiListItemSpec`                                                 |

"预留"表示参数、族标记与类型别名都已就位，但 adapter 暂不定义具体 spec：调用方现在无法构造它们， 未来
adapter 补实现时公共签名不变。

- 单个 spec 直接作为 `nativeSpec` 传入；同族多个 spec 用成员 `+` 组合，两侧都是集合时展平，同类型多个
  参数以先声明者为准且不去重。
- `nativeSpec` 统一放在尾部 `content` lambda 之前（无尾随 lambda 的入口放最后），因此现有尾随 lambda
  调用无需改动。
- 每个字段的默认值等于原生默认值，传空 spec 不改变外观；未指定的字段一律回落原生默认。
- 已由槽位或中立参数覆盖的属性不再在 spec 暴露，避免同一外观有两条覆盖路径。
- 实现无法承载某个已携带的字段时忽略并记录一次警告，不重绘伪造外观。

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

装配层可依赖任意外置适配器模块（与本模块同级），通过其声明的扩展属性或工厂取得实现，并显式导入该扩展；
主题入口的 `designSystem` 是必填参数， **没有隐式回退**；组件在主题边界之外由
`requireNomiDesignSystem()` 抛出带指引的 `IllegalStateException`。
[sample](<../../app/sample/README.md>)的候选设计系统目录由宿主装配层决定，不需要 Desktop 专属注入。

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
NomiTheme(designSystem = NomiDesignSystem.Default, theme = LocalNomiTheme.current) { ... }
```

- 五组均为完整值，不做字段级合并；`isDark` 是已解析布尔，`DarkMode.System` 由入口在 `theme` 省略时解析一次。
- 入口每次进入都重置显式内容 slot；`designSystem` 必填，`darkMode` 只在 `theme` 省略时选择默认主题，
  嵌套继承需显式传 `theme = LocalNomiTheme.current`。
- adapter 不读系统状态；原生色板基线由映射函数返回的 `isDark` 决定。
- 不做 native 到中立的反向投影；原生视觉由原生组件读原生主题，中立读取值不回填原生默认。
- `NomiThemeRequest`、`ResolvedNomiTheme`、`NomiThemeOverrides`、`NomiThemeOverride` 与主题聚合包装都不是当前
  API。

## 默认颜色、排版、形状与尺寸工厂

[Color](<src/commonMain/kotlin/ciyin/designsystem/theme/Color.kt>)只维护 internal 明暗默认对象；
[ColorScheme](<src/commonMain/kotlin/ciyin/designsystem/theme/ColorScheme.kt>)只提供
`defaultLightNomiColorScheme` / `defaultDarkNomiColorScheme` 两个颜色工厂，各保留 27 个具名参数和默认值。
旧 `ciyin.material.theme` 的 `lightColorScheme` / `darkColorScheme` 作为兼容入口转发到它们；中立包不再提供同名别名。

[Type](<src/commonMain/kotlin/ciyin/designsystem/theme/Type.kt>)取代原 Typography 文件，保留十五角色中立
`NomiTypography`，不能恢复成任何原生 Typography
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
Nomi 自身使用时生效，不会覆盖各 adapter 未指定的原生默认。

主题级映射由各 adapter 模块定义：接收 `NomiTheme.() -> 原生主题值`，未指定的组保留当前原生环境，
映射函数只能访问真实存在的原生角色，因此不需要运行时降级告警。具体签名、预置映射与降级行为见各 adapter
模块 README；自定义 lambda 应是稳定引用或 `remember` 后的值。

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
返回新外层对象，未覆盖字段保留原值/引用，不修改原对象或做深复制。跨模块构造、复制与身份契约由 adapter
模块的 独立消费测试锁定。

## 未指定、显式覆盖与不支持

| 参数                                                                               | 未指定语义                         | 显式语义                                 |
|------------------------------------------------------------------------------------|------------------------------------|------------------------------------------|
| Text style；Button variant/size/shape/contentPadding/textStyle；Surface/Card shape | null 保留 native default           | 按当前实现消费；无法承载时降级为原生默认 |
| `NomiButtonColors` / `NomiSurfaceColors` 的单个字段                                | Color.Unspecified 保留该字段原生值 | Transparent 也是有效覆盖；只覆盖指定字段 |
| `NomiBorder`                                                                       | Default 保留原生描边               | None 移除；Stroke 指定描边               |
| Card contentPadding                                                                | null 保留原生 slot 默认            | 明确值只控制内容 slot，不重绘 Card       |

Defaults 工厂仍可被调用方主动选用，但普通组件调用不自动调用它们；不能为所有设计系统预填同一
metrics、圆角或最小高度。
`NomiCard` 不再是统一 `Surface + padding + Column` 配方；它是非点击卡片，交给表面族的真实 `Card` 方法。
描边只使用 NomiBorder.Default/None/Stroke，不提供 nullable BorderStroke 兼容重载；不保留本会话生成的短名转发入口。

各 adapter 的降级边界在其模块 README 中列出，共同约束是：

- 无法承载的显式覆盖一律降级为原生默认，并通过 `ciyin.platform.logger` 记录一次警告；不包装/重绘伪装支持。
- 未携带覆盖时不得补任何视觉值，原生默认始终优先于中立令牌。
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
`NomiTheme` 传入，原生角色由 adapter 映射函数决定；中立 `NomiTypography` 与各原生排版类型仍不同。 旧
`ciyin.material` 源码兼容及原始 Typography 身份由门面和对应 adapter 配置保留。

[构建](<build.gradle.kts>)
沿用[目标约定](<../../buildSrc/src/main/kotlin/multiplatform-lib-targets.gradle.kts>)； Android
与项目开启的 Desktop/iOS/JS/Wasm 目标分别验证，不擅自更改平台开关。各 adapter 的平台开关跟随同一约定。

## 验证入口

```powershell
.\gradlew.bat :core:design-system:compileCommonMainKotlinMetadata --console=plain
.\gradlew.bat :core:design-system:desktopTest --console=plain
```

Nomi 默认渲染与组件契约已随实现迁至 [core/design-system-nomi](<../design-system-nomi/README.md>)
，由该模块 `desktopTest` 独立验证；核心只保留公共契约与 adapter 无关测试。

核心侧验证零参数请求不被填默认、单字段颜色覆盖、三态 border、跨系统 Override 只继承显式值和 slot 恢复。
无显式设计系统时 `NomiTheme`、各组件与导航套件入口必须抛出带指引的 `IllegalStateException`，
不能被隐式回退掩盖（[契约测试](<src/commonTest/kotlin/ciyin/designsystem/NoImplicitDesignSystemContractTest.kt>)）。
列表项由 [ListItemDispatchContractTest](<src/commonTest/kotlin/ciyin/designsystem/ListItemDispatchContractTest.kt>)
验证默认稀疏派发、槽位不占位、显式参数透传与 `nativeSpec` 原样派发及按类型取用。

原生 adapter 还须对照真实库组件测量/截图，不能用旧统一外观测试证明 native parity；各 adapter 的用例矩阵
在其模块 README 中维护。历史轮次的核心 43 项、联合 148 项、导航 180 项与命名空间 218 项结果仅作历史记录，
历史截图仍只作参考，当前编译/单测不代表设备或发布验收。

## 相关资料

- [核心与 adapter 技能](<../../.agents/skills/core-design-system/SKILL.md>)
- [当前设计说明、历史证据及验收缺口](<../../.docs/design/design-system-模块化方案.md>)
- [旧包兼容](<../material/README.md>)
- [sample](<../../app/sample/README.md>)
