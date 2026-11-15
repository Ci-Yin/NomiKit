---
name: core-design-system
description: Use or maintain NomiKit native-default-first design-system APIs, Material3 and multiplatform Fluent adapters. Applies to direct theme parameters and inheritance, semantic token factories, NomiText/NomiButton/NomiSurface/NomiCard, renderer capabilities, slot styling, and sample integration. Legacy ciyin.material APIs route to core-material.
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
NomiDesignSystem 是接口；三个实现对象均 internal，对外通过伴生对象上的扩展属性
`NomiDesignSystem.Default` / `NomiDesignSystem.Material3` / `NomiDesignSystem.Fluent`
取得，不要把接口与具体实现机械互换。 DefaultNomi *Renderer 是默认实现，LocalNomiNative* 保存其原生解析环境；公共
LocalNomi* 只表示显式 slot 增量。 模块和根包保持原路径；组件 package 按
text/button/surface/card/navigation/scaffold/list 细分，消费者直接迁移 imports，不增加旧包转发或
typealias。 default* 中原 App 段改为 Nomi；无 App 段的通用工厂名称不变。 设计系统实例统一写作
`NomiDesignSystem.Default` /
`NomiDesignSystem.Material3` / `NomiDesignSystem.Fluent`；扩展属性不是核心成员，必须导入对应模块声明（
`ciyin.designsystem.nomi.Default` 等）才能解析，IDE 在 `NomiDesignSystem.` 后补全。旧顶层 object
名称已删除，不恢复别名或转发。

## 按改动定位

-

主题： [Theme](<../../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/theme/Theme.kt>)、
[ThemeOverrides](<../../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/theme/ThemeOverrides.kt>)、
[NomiThemeRequest](<../../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/runtime/NomiThemeRequest.kt>)。
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
fluent/button。NomiBorder/NomiControlSize/NomiComponentCapabilities/NomiDesignSystemComponents 留在核心
component 根。 DefaultNomiSystem（internal，经 `NomiDesignSystem.Default` 暴露）与
NomiNativeContentStyle 位于 nomi 模块；默认 renderer 位于其组件子包。公共主题 API/数据仍在
theme，通用请求/契约/显式 slot 仍在 runtime。 核心不引用 nomi：LocalNomiDesignSystem 可空且无隐式默认，应用根、测试与
Preview 必须显式提供（CompositionLocalProvider 或 NomiTheme (designSystem=...)
）；未提供时读取组件明确失败。text/actions/surfaces/navigation/scaffold/listItems 六族 SPI，Card 仍归
surfaces，导航套件归 navigation，页面外壳归 scaffold，列表项归 listItems。 导航套件入口
NomiNavigationSuite (layoutType, modifier, items, footerItems, content)
：布局类型由调用方按窗口形态选择，核心与实现都不读取窗口/平台；items 与 footerItems 两个声明组必须按顺序渲染。
页面外壳入口 NomiScaffold (modifier, colors, topBar, bottomBar, content)：content 必须位于顶栏与底栏之间，
空槽不占位；M3 委托原生 Scaffold（innerPadding 让位），Fluent/Nomi 用 Column 权重排布，标题/最大宽度/滚动仍归调用方。
列表项入口 NomiListItem (title, modifier, onClick, leading, subtitle, trailing)：槽位为 null
时不占位，onClick=null 表示静态行； 行高、容器、圆角、悬停/按下/点击外观与文本样式全部由原生实现决定。
本版不提供 enabled、selected、展开、 overline、分组标题、分隔线与颜色/形状/内边距覆盖；尾部只有一个原生槽位，值文本与箭头由调用方组合；
副标题建议使用 NomiText 继承原生 supporting/caption 样式，图标尺寸与无障碍名称由槽内容负责（IconPack
固有 256dp，必须显式 size）。 导航项是槽位式 NavigationItem (selected, onClick, modifier, icon, badge,
label, colors, interactionSource)：icon/badge/label 由调用方组合，没有
enabled；NomiNavigationItemColors 只覆盖
contentColor/selectedContentColor/disabledContentColor/indicatorColor。槽内的图标尺寸与无障碍名称由调用方负责（IconPack
固有 256dp，必须显式 size）；适配器不得替槽内容定尺寸。 能力
navigationBar/navigationRail/navigationDrawer 默认 Bar/Rail 支持、Drawer
需实现显式声明；navigationItemModifier 声明是否支持项级 modifier（Fluent 为 false，非默认值必须明确
require 失败）。入口按能力提前拒绝，不静默降级。 Nomi 复刻既有 Navigation.kt 视觉（Bar/Rail，M3 外壳 +
NomiTheme 令牌，Drawer 禁用）；M3 用 NavigationBar/NavigationRail/PermanentNavigationDrawer；Fluent 的
Bar/Drawer 用 NavigationView 的 Top/LeftCompact，Rail 改用公开 SideNav 组合左置外壳（上游 Left 布局会固定插入
footer 分割线并给内容面板加 1dp 描边且都无关闭入口，底部项因此由适配层贴在 SideNav 下方自绘）；原生菜单
DSL 非 @Composable，采用组合阶段记录声明再重放，选中刷新依赖调用方以组合可见状态驱动。

## 主题输入与结果不要混用

1. 核心提交 `NomiThemeRequest(requestedMode, isDark, overrides)`，由核心主题边界规范化请求字段；adapter
   回调可携带原请求元数据，但无权改变核心最终发布的模式或显式来源。adapter 先建自己的原生主题，
   在原生主题环境内通过
   `ProvideTheme(request: NomiThemeRequest, content: @Composable (ResolvedNomiTheme) -> Unit)`
   回调五组只读结果；不先生成另一套系统的默认主题数据。
2. `ResolvedNomiTheme` 直接承载
   requestedMode、isDark、colorScheme、typography、shapes、spacings、sizes、overrides 八项运行时契约。核心收到
   adapter 结果后以 request 规范化 requestedMode、isDark、overrides 并保留五组结果，不从 adapter 的结果或
   metadata 反推请求。
3. `NomiTheme` 继续接受五组直接参数：colorScheme?/typography?/shapes?/spacings?/sizes?；组件只通过
   `NomiTheme` 的只读 getter 读取五组，不读取聚合包装。darkMode 默认 NomiTheme.darkMode；designSystem
   默认 requireNomiDesignSystem ()，必须由祖先显式提供，未提供时明确失败，没有隐式回退。
   根不继承父颜色/accent，只继承父明确的非颜色四组并重置 slot；Override 继承全部明确组与 slot。都不复制
   resolved defaults。
4. 直接参数归一化到既有 NomiThemeOverrides（追加 colorScheme/typography/shapes）。同层 direct+overrides
   同组重复 require， 同层完整色板+accent 也 require。子完整色板清除继承 accent，子 accent
   叠父明确色板；非空对象是完整组，不猜 copy 修改字段。
5. Nomi 支持五组；M3 映射并保留完整中立读模型。Fluent 明确 typography 时映射八角色，缺省不映射； 拒绝中立完整
   colorScheme/九级 shapes，调用方改用 accent 或 typed Colors/CornerRadius，不能静默降级。
6. 主题聚合文件、聚合类型、聚合默认工厂和 `.tokens` 包装不属于当前 API；不新增 ThemeData/ThemeSnapshot
   别名或兼容入口。默认单组工厂继续保留，公开构造与编译器生成的成员 copy 契约继续保留。 旧门面用 M3
   legacy 配置保完整旧读模型/原始 Typography，不增加新 SPI、模型或 locals。

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

## 默认值与能力

- Text style、Button variant/size/shape/contentPadding/textStyle、Surface/Card shape 的 null 表示未指定；
  颜色逐字段 Unspecified 才是未指定，Transparent 是覆盖。不要默认调用统一 Defaults/metrics 或合并
  bodyMedium。
- [NomiBorder](<../../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/NomiBorder.kt>)
  ：Default 保留原生、None 移除、Stroke 指定。 不提供 nullable BorderStroke 兼容重载，调用方直接使用三态
  API。
- Card 通过表面族实际 `Card` 方法使用原生非点击 Card，不再用统一 Surface + padding 配方替代。
- Fluent 直接使用原生 Button/AccentButton/SubtleButton/HyperlinkButton 与 `surface.Card`。 Button 非
  null size/shape/contentPadding 必须拒绝；Card 指定颜色或非 Default border 必须拒绝并提示
  NomiSurface，shape/slot padding 可用；Text 非 null autoSize 在 Fluent 必须拒绝。 不能重新
  Layer+clickable 绘制按钮、包第二个点击层，或静默忽略不支持参数来伪造统一能力。
- M3 支持当前公开的组件级参数（包括任意 Shape）；主题级 Shapes 受旧中立读模型限制，标准角色不是
  RoundedCornerShape 时明确拒绝。
- UI
  使用[能力声明](<../../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/NomiComponentCapabilities.kt>)
  展示限制； 核心边界也执行 require，不以自动过滤用户输入代替报错。导航布局同样走能力校验，不要用运行期
  if 静默降级到其它布局。
- 导航底部项用 footerItems 分组表达（Fluent 有原生 footerItems；M3/Nomi 用原生布局/弹性间隔），不要用伪造的空导航项或
  padding 硬撑位置。
- 应用侧只做窗口→布局映射与数据分组；不要恢复 AppTheme/旧 iconpack/M3 导航直连。
- 文本 SPI 为 String/AnnotatedString 双重载，逐字段覆盖、onTextLayout、autoSize、inlineContent
  原样透传；未指定沿用 style 与原生环境。
- 文本颜色优先级为显式 color > 本次 style.color > 显式 slot 色 > 继承样式色；仍未指定交原生环境。
  不能将投影默认值变成显式 slot，压过原生禁用/交互内容色。Nomi 自身解析环境使用独立 LocalNomi*
  ，M3/Fluent 留在原生 locals；中立 slot 仅携带显式增量。
- Fluent typed Colors 与显式 accent 并用时保留 typed Colors.darkMode，不能因为只改强调色就切换整个非强调色调色板。

核心不依赖 Material/Fluent/UI foundation。Fluent commonMain 复用既有 **v0.1.0** alias 和 KMP 目标约定；
不新建相同 alias、不擅自打开项目关闭的平台。应用根保持显式 M3，不加全局双主题桥或切根 gate。

## 验证

原生默认与明确覆盖分开测试：零参对照真实原生组件；单字段覆盖保留其它原生状态； 能力不支持应失败；跨系统
Override 不携带父默认，显式 slot 继承并恢复。不要拿旧统一尺寸/样式断言证明原生一致。 主题回归还覆盖独立历史
golden、五组直接调用、root/Override 颜色继承差异、同层重复拒绝和各 adapter 主题支持边界。 公开构造/成员
copy 变更须在真正外部模块验证全部 9 类，不借助核心 friend-path；测试不能导入复制扩展。
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