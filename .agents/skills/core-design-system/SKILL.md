---
name: core-design-system
description: Use or maintain NomiKit native-default-first design-system APIs, Material3 and multiplatform Fluent adapters. Applies to NomiTheme values, adapter mapping functions, semantic token factories, NomiText/NomiButton/NomiSurface/NomiCard, renderer contracts, slot styling, and sample integration. Legacy ciyin.material APIs route to core-material.
---

# 原生默认优先 Design System

新增、重构或修复 Kotlin 声明时，先读取 [kotlin-kdoc](<../kotlin-kdoc/SKILL.md>)
；仅有中文摘要不代表参数、返回值和边界契约已完整。

统一调用，不统一外观。未指定视觉参数时保留当前设计系统的原生默认；只应用明确覆盖。
先读[核心 README](<../../../core/design-system/README.md>)
和[设计说明](<../../../.docs/design/design-system-模块化方案.md>)。

## 新生成代码的演进

本会话或后续会话由 AI 新生成、尚未发布的接口，变更时直接删除旧入口并迁移调用方； 未经用户明确要求，不增加
Deprecated、旧名转发或兼容重载。已有项目的兼容入口不因此自动删除。

## 命名边界

Nomi* 是公共设计系统品牌 API，App* 留给应用层；旧 ciyin.material 门面保留旧 App* 名称作为兼容例外。
NomiDesignSystem 是映射契约；实现对象均 internal。Nomi 以扩展属性 `NomiDesignSystem.Default` 暴露，
Material3 / Fluent 以扩展属性 `NomiDesignSystem.Material3` / `NomiDesignSystem.Fluent` 与同名映射函数暴露；
不要把接口与具体实现机械互换。 DefaultNomi *Renderer 是默认实现，LocalNomiNative* 保存其原生解析环境；公共
LocalNomi* 只表示显式 slot 增量。 模块和根包保持原路径；组件 package 按
text/button/surface/card/navigation/scaffold/list 细分，消费者直接迁移 imports，不增加旧包转发或
typealias。 default* 中原 App 段改为 Nomi；无 App 段的通用工厂名称不变。 设计系统实例统一写作
`NomiDesignSystem.Default` /
`NomiDesignSystem.Material3` / `NomiDesignSystem.Fluent`；扩展声明不是核心成员，必须导入对应模块声明（
`ciyin.designsystem.nomi.Default` 等）才能解析，IDE 在 `NomiDesignSystem.` 后补全。旧顶层 object
名称已删除，不恢复别名或转发。

## 按改动定位

-

主题： [NomiTheme](<../../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/theme/NomiTheme.kt>)、
[NomiDesignSystem](<../../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/runtime/NomiDesignSystem.kt>)。
旧式数据职责见[ColorScheme](<../../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/theme/ColorScheme.kt>)
与
[Type](<../../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/theme/Type.kt>)；旧
Typography 文件已移除。

-

公共组件： [NomiText](<../../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/text/NomiText.kt>)、
[NomiButton](<../../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/button/NomiButton.kt>)、
[NomiSurface](<../../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/surface/NomiSurface.kt>)、
[NomiCard](<../../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/card/NomiCard.kt>)、
[NomiNavigationSuite](<../../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/navigation/NomiNavigation.kt>)、
[NomiScaffold](<../../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/scaffold/NomiScaffold.kt>)、
[NomiListItem](<../../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/list/NomiListItem.kt>)
。每个文件维护真实 API/SPI，不重建组件大文件。

- 原生适配：读[M3 README](<../../../core/design-system-material3/README.md>)
  或[Fluent README](<../../../core/design-system-fluent/README.md>)； adapter
  组件独立实现，聚合文件只组装。旧包问题转[core-material](<../core-material/SKILL.md>)。
- 展示：读[sample README](<../../../app/sample/README.md>)；共享默认 Nomi/M3/Fluent，provider 可选覆盖，纯
  UI 页面不新增业务数据层。

公共 component 留在核心，内置实现 nomi、material3、fluent 各自模块根下均按
text/button/surface/card/navigation/scaffold/list 组织；nomi 是独立 Gradle 模块（:core:
design-system-nomi）但包名 ciyin.designsystem.nomi 不变。 公共入口、SPI 和专属参数类型留在核心
component，FluentButtonColors 属于
fluent/button。NomiBorder/NomiControlSize/NomiDesignSystemComponents 留在核心 component 根。
DefaultNomiSystem（internal，经 `NomiDesignSystem.Default` 暴露）与 NomiNativeContentStyle 位于 nomi
模块；默认 renderer 位于其组件子包。公共主题值与 getter 在 theme，映射契约与显式 slot 在 runtime。 核心不引用
nomi：LocalNomiDesignSystem 可空且无隐式默认，应用根、测试与 Preview 必须显式提供（CompositionLocalProvider
或 NomiTheme (designSystem=...)
）；未提供时读取组件明确失败。text/actions/surfaces/navigation/scaffold/listItems 六族 SPI，Card 仍归
surfaces，导航套件归 navigation，页面外壳归 scaffold，列表项归 listItems。 导航套件入口
NomiNavigationSuite (layoutType, modifier, containerColor, items, footerItems, content)
：布局类型由调用方按窗口形态选择，核心与实现都不读取窗口/平台；items 与 footerItems 按声明顺序渲染组内项目，
底部组是否参与布局由实现决定（Nomi Bar 只渲染主导航项，底部组只在 Rail/Drawer 展示）。 页面外壳入口
NomiScaffold (modifier, colors, topBar, bottomBar, content)：content 必须位于顶栏与底栏之间， 空槽不占位；M3
委托原生 Scaffold（innerPadding 让位），Fluent/Nomi 用 Column 权重排布，标题/最大宽度/滚动仍归调用方。
列表项入口 NomiListItem (title, modifier, onClick, leading, subtitle, trailing)：槽位为 null
时不占位，onClick=null 表示静态行； 行高、容器、圆角、悬停/按下/点击外观与文本样式全部由原生实现决定。
本版不提供 enabled、selected、展开、 overline、分组标题、分隔线与颜色/形状/内边距覆盖；尾部只有一个原生槽位，值文本与箭头由调用方组合；
副标题建议使用 NomiText 继承原生 supporting/caption 样式，图标尺寸与无障碍名称由槽内容负责（IconPack
固有 256dp，必须显式 size）。 导航项是槽位式 NavigationItem (selected, onClick, modifier, icon, badge,
label, colors, interactionSource)：icon/badge/label 由调用方组合，没有
enabled；NomiNavigationItemColors 只覆盖
contentColor/selectedContentColor/disabledContentColor/indicatorColor。槽内的图标尺寸与无障碍名称由调用方负责（IconPack
固有 256dp，必须显式 size）；适配器不得替槽内容定尺寸。containerColor: Color = Color.Unspecified 只覆盖
Bar/Rail/Drawer 外壳容器，Unspecified 保留原生值、Transparent 是有效覆盖；项色仍用
NomiNavigationItemColors。Bar/Rail/Drawer 与 containerColor 原样交给当前实现：实现无法承载时自行忽略并
通过 ciyin.platform.logger 记录警告，核心入口不按能力拦截，切系统不抛异常。 Nomi 复刻既有 Navigation.kt
视觉（Bar/Rail 用 M3 外壳 + NomiTheme 令牌，Drawer 用 PermanentNavigationDrawer + Nomi 令牌抽屉项）；M3
用 NavigationBar/NavigationRail/PermanentNavigationDrawer；Fluent 的显示模式映射为
Bar→LeftCollapsed、Rail→LeftCompact、Drawer→Left，三者都用 NavigationView 并只关闭内容面板描边（不覆盖导航
容器色，因为其 color 是内容面板底色）； 原生菜单 DSL 非 @Composable，采用组合阶段记录声明再重放，选中刷新依赖调用方以组合可见状态驱动。

## 主题值与映射函数

1. `NomiTheme` 是不可变值：colorScheme、typography、shapes、spacings、sizes、isDark。`isDark` 只接受 已解析布尔；
   `DarkMode` 只是入口请求枚举。
2. `NomiTheme` 只有一个入口：
   `NomiTheme(designSystem, darkMode = DarkMode.System, theme = 按 darkMode 解析的默认值, content)`；
   `designSystem` 必填，`theme` 显式传入时忽略 `darkMode`；每次进入都重置显式内容 slot， 嵌套继承需显式传
   `theme = LocalNomiTheme.current`。
3. `NomiDesignSystem` 只暴露 id、components 与 `ProvideTheme(theme, content)`；入口发布
   `LocalNomiTheme`， adapter 只安装原生主题。
4. 适配器函数接收映射函数：`NomiDesignSystem.Material3(map: (NomiTheme) -> Material3Theme)`、
   `NomiDesignSystem.Fluent(map: (NomiTheme) -> FluentTheme)`；无参访问用同名扩展属性，返回原生默认映射的共享实例。映射函数只给
   显式角色赋值，不做字段级猜测。
5. `LocalNomiTheme.current` 与伴生分组 getter 读取本层显式输入值；原生视觉由原生组件直接读原生主题，不做
   native 到中立的 反向投影。
6. `NomiThemeRequest`、`ResolvedNomiTheme`、`NomiThemeOverrides`、`NomiThemeOverride` 已删除；不新增
   Deprecated 或兼容转发。默认单组工厂、公开构造与生成成员 copy 契约继续保留。

数据组织沿旧职责：Color 内部明暗默认对象，ColorScheme 仅有
defaultLightNomiColorScheme/defaultDarkNomiColorScheme 两个工厂，各 27 具名参数； Type 保中立
NomiTypography，不导入 M3。Type/Shape/Spacing/Size 的 private Default *Tokens 只维护一份旧数值。 9 个主题
data
class（NomiColorScheme、NomiTypography、NomiShapes、Spacings、NomiSizes、SizeScale、Strokes、ComponentHeights、LayoutConstraints）
采用公开构造与编译器生成的成员 copy；不再保留 ConsistentCopyVisibility、9 个手写核心复制扩展或旧门面的
8 个复制转发。 跨模块既可直接构造，也可 default*().copy (...)；两种包都无需 copy import，删除旧
import，不增加 Deprecated/兼容转发。 保留所有默认参数与必填参数：NomiTypography 十五角色和四个尺寸子类型的字段仍全部必填。
颜色/形状/间距/完整尺寸工厂返回新外层，默认排版及尺寸子组工厂复用共享对象，NomiSizes 默认子组身份不变。
成员 copy 为浅复制，未覆盖字段保留原值/引用；不得顺便修改默认值、补零参默认、深复制或改变身份契约。 默认入口为
defaultNomiTypography/defaultNomiShapes/defaultSpacings/defaultNomiSizes，以及
defaultIconSizeScale/defaultAvatarSizeScale/defaultStrokes/defaultComponentHeights/defaultLayoutConstraints。
旧 material 浅深色工厂转发到中立 default* 工厂，不重复维护默认常量；旧包不再保留 defaultApp*
/defaultSpacings 等默认工厂转发，默认入口统一为中立核心工厂。测试须含真正跨模块消费者，不能只靠核心
friend tests。

## 默认值与降级

- Text style、Button variant/size/shape/contentPadding/textStyle、Surface/Card shape 的 null 表示未指定；
  颜色逐字段 Unspecified 才是未指定，Transparent 是覆盖。不要默认调用统一 Defaults/metrics 或合并
  bodyMedium。
- [NomiBorder](<../../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/NomiBorder.kt>)
  ：Default 保留原生、None 移除、Stroke 指定。 不提供 nullable BorderStroke 兼容重载，调用方直接使用三态
  API。
- Card 通过表面族实际 `Card` 方法使用原生非点击 Card，不再用统一 Surface + padding 配方替代。
- Fluent 直接使用原生 Button/AccentButton/SubtleButton/HyperlinkButton 与 `surface.Card`。 Button 的
  size/shape/contentPadding、Card 的 colors/border、Text 的 autoSize 无原生入口：忽略覆盖、保留原生默认，
  并通过 ciyin.platform.logger 记录一次警告；Card 需要颜色/描边覆盖时改用 NomiSurface。 不能重新
  Layer+clickable 绘制按钮、包第二个点击层，也不能用重绘伪造支持。
- M3 支持当前公开的组件级参数（包括任意 Shape）；主题级 Shapes 受旧中立读模型限制，标准角色不是
  RoundedCornerShape 时明确拒绝。
- 核心不提供能力声明类型，也不按能力拦截参数；导航布局与容器色原样交给实现，无法承载时由适配器降级并记录
  警告。调用方无需查询能力即可切换设计系统。
- 导航底部项用 footerItems 分组表达（Fluent 有原生 footerItems；M3/Nomi 用原生布局/弹性间隔），不要用伪造的空导航项或
  padding 硬撑位置。
- 应用侧只做窗口→布局映射与数据分组；不要恢复 AppTheme/旧 iconpack/M3 导航直连。
- 文本 SPI 为 String/AnnotatedString 双重载，逐字段覆盖、onTextLayout、autoSize、inlineContent
  原样透传；未指定沿用 style 与原生环境。
- 文本颜色优先级为显式 color > 本次 style.color > 显式 slot 色 > 继承样式色；仍未指定交原生环境。
  不能将投影默认值变成显式 slot，压过原生禁用/交互内容色。Nomi 自身解析环境使用独立 LocalNomi*
  ，M3/Fluent 留在原生 locals；中立 slot 仅携带显式增量。
- Fluent `Primary` 映射保留原生 `Colors.darkMode` 与非强调色角色，不能因为只提供 primary 就切换整套色板。

核心不依赖 Material/Fluent/UI foundation。Fluent commonMain 复用既有 **v0.1.0** alias 和 KMP 目标约定；
不新建相同 alias、不擅自打开项目关闭的平台。应用根保持显式 M3，不加全局双主题桥或切根 gate。

## 验证

原生默认与映射分开测试：无参工厂对照真实原生组件；带 map 时逐个断言被映射角色与未映射角色；跨系统嵌套
不携带父投影默认，显式 slot 继承并恢复。不要拿旧统一尺寸/样式断言证明原生一致。 主题回归还覆盖
NomiTheme 值 默认与 light/dark、伴生 getter、入口 slot 重置、嵌套继承，以及适配器只安装原生主题、不回写中立值。
公开构造/成员 copy 变更须在真正外部模块验证全部 9 类，不借助核心 friend-path；测试不能导入复制扩展。
核对指定字段覆盖、原对象与未改字段、相等性/解构、默认工厂和尺寸子组身份，以及旧 AppTheme/Material
Typography/图标兼容。

```powershell
.\gradlew.bat :core:design-system:desktopTest --console=plain
.\gradlew.bat :core:design-system-nomi:compileCommonMainKotlinMetadata :core:design-system-nomi:desktopTest --console=plain
.\gradlew.bat :core:design-system-material3:desktopTest :core:material:desktopTest --console=plain
.\gradlew.bat :core:design-system-fluent:compileCommonMainKotlinMetadata :core:design-system-fluent:desktopTest --console=plain
.\gradlew.bat :app:sample:desktopTest --tests "*DesignSystemDemo*" --console=plain
```

编译、单测、原生对照截图、设备和 Release 各自记录；历史 internal 构造+扩展 copy 方案已被替代，不再作为当前使用限制。
公开构造与生成成员 copy 仍是当前契约；主题结果扁平化已通过跨模块回归，后续修改仍须独立验证，不以历史主题恢复、包迁移回归或
WSA 截图代替当前结果。 实际结果统一见设计说明，Release 缺失 API 问题不能用 ignorewarnings 掩盖。