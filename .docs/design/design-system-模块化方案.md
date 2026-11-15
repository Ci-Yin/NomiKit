# Design System 模块化方案

> 状态：已实现（编译与 203 项桌面回归已核；保留平台与发布验证缺口）；设计系统实现改由 `NomiDesignSystem`
> 伴生对象上的扩展属性暴露，旧顶层单例名称已删除；新增列表项第六族，旧 `SettingItem` 已删除。
> 范围：`NomiDesignSystem` 新增空 `companion object` 作为绑定命名空间；三个实现对象降为 internal（
> `DefaultNomiSystem`/`Material3System`/`FluentSystem`）；对外为
> `NomiDesignSystem.Default/Material3/Fluent` 扩展属性；29 个文件 128 处引用迁移，6 个 README、设计索引与
> 2 个 skill 同步；列表项族新增 `NomiListItem` 与 `listItems` renderer，三端原生映射落地，
> `:core:ui-foundation` 旧 `SettingItem` 删除。
> 相关模块：`:core:design-system`、`:core:design-system-nomi`、`:core:design-system-material3`、
> `:core:design-system-fluent`、`:core:ui-foundation`、`:core:material`、`:app:shared`、`:app:sample`。
> 最近核对：2026-11-15；master 未提交工作树（列表项第六族与三适配）。
> 验证记录：本轮 metadata 编译、app 编译与 203 项 Desktop 回归通过（核心 47、M3 34、Fluent 53、Nomi 24、
> 旧门面 19、sample 26）；命名空间轮 218 项与导航轮 180 项见下方历史条目。

本文沿原路径演进，不新增分支、版本号或归档副本。原设计有 Git 历史可追溯； 此前确立原生默认优先、旧式主题调用、单组默认工厂、Nomi
品牌命名、独立 nomi 包及公开构造/生成成员
copy；上一轮删除主题聚合包装，保留现有运行时结果模型并将五组字段扁平化。源码存在、测试通过、视觉和平台发布是不同结论。

## 背景与目标

早期主题封装直接暴露 Material Typography；第一次模块化将中立 tokens 与四组件拆出， 但把统一 token、按钮
metrics 和 Card 表面组合预先施加给三个系统，Fluent 按钮还以 Layer + clickable 重绘。 这些做法隔离了依赖，却不能保留
Material / Fluent 的原生默认视觉。

已确认原则仍是： **调用 API 统一，默认外观和交互由各设计系统自身决定；只应用调用方明确指定的覆盖。**
此前已在此前提下恢复旧 Theme 的文件职责、工厂名称和直接参数调用，并整理内置实现包职责。
上一轮按授权删除主题聚合类型和聚合默认工厂；已有 ResolvedNomiTheme
直接持有五组结果，适配器回调随之迁移，不新增替代包装、CompositionLocal 或兼容入口。

- 零视觉参数调用应对应当前库的真实原生组件，而不是把 Nomi 默认颜色、字体、形状、padding 投给所有
  adapter。
- 未指定值与显式值必须区分；只覆盖一个字段时，其余字段保留原生状态。
- 原生 API 无法表达的覆盖声明为不支持并快速失败，不静默忽略、不重绘组件伪造支持。
- 主题输入与只读投影分开；局部换系统不携带父系统已经解析的默认样式。
- API、主题、每个组件实现分文件维护，聚合对象仅组装。

非目标：不复刻全部第三方组件，不统一所有系统尺寸；不把窗口 Mica/Acrylic、导航、业务状态或持久化设置放入本模块；
不一次性重写旧页面；不自动打开项目未启用的平台。首期只有四组件，后续按轮次扩展为六族，后续列表见文末。

## Nomi 公共命名

NomiKit 是主体设计体系，Nomi 前缀标识 API 归属，不强制当前渲染风格。 新核心的
AppTheme/AppText/AppButton/AppSurface/AppCard 已迁为
NomiTheme/NomiText/NomiButton/NomiSurface/NomiCard， 主题数据、辅助参数、renderer SPI、CompositionLocal
和 default 工厂同步迁移；不保留新核心 App* 别名。 NomiDesignSystem 是公共接口；Nomi 默认实现与原生
M3/Fluent 实现对象均为 internal，经伴生对象扩展属性 `NomiDesignSystem.Default` /
`NomiDesignSystem.Material3` / `NomiDesignSystem.Fluent` 暴露，设计系统 id 不变。 内部默认 renderer 以
DefaultNomi* 区分，LocalNomiNative* 保存默认实现的解析环境，与公共显式 slot 不混合。 根包
ciyin.designsystem、Gradle 模块、组件行为、默认数值、主题继承和 gallery 布局保持；组件子包已按
text/button/surface/card 组织。 公开构造和生成成员 copy 沿用前轮契约；上一轮修改主题结果的传递与存储，组件通过
NomiTheme 读取，不改字段值或原生渲染。 旧 ciyin.material 的 App* 名称及 app/ 真正应用层 App*
名称保留；消费者迁移新包时需要同步 imports。

## 模块与依赖

模块已在[工程声明](<../../settings.gradle.kts>)注册。核心只依赖 Runtime/Foundation/UI，不引用任何内置实现；
内置 Nomi 实现独立为 `:core:design-system-nomi`（api 核心 + M3 原生外壳），M3 / Fluent 分别持有原生库，
旧门面单向依赖核心和 M3；不能把旧 UI foundation 的 Material API 拉回核心。

| 模块                                                                                                           | 入口与职责                                                                                                       |
|----------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------|
| [核心](<../../core/design-system/README.md>)                                                                   | 请求、只读投影、四组件 + 导航套件 API、四族 SPI、能力声明、图标；无隐式默认实现                                  |
| [Nomi 实现](<../../core/design-system-nomi/README.md>)                                                         | internal DefaultNomiSystem（经 `NomiDesignSystem.Default` 暴露）、原生内容环境、四组件 Foundation 实现与导航外壳 |
| [M3 adapter](<../../core/design-system-material3/README.md>)                                                   | 原生 M3 默认、原生配置、投影、组件与原生导航；特殊 legacy 配置服务旧门面                                         |
| [Fluent adapter](<../../core/design-system-fluent/README.md>)                                                  | commonMain 原生 Fluent 主题、组件与 NavigationView 导航，不再重绘按钮                                            |
| [旧门面](<../../core/material/README.md>)                                                                      | 旧包参数/类型/默认行为的源码兼容，保留旧 UI foundation 传递依赖                                                  |
| [sample](<../../app/sample/README.md>)                                                                         | 原生默认、显式覆盖、能力限制展示，默认 Nomi/M3/Fluent                                                            |
| [app/shared](<../../app/shared/src/commonMain/kotlin/com/ciyin/app/presentation/app/navigation/Navigation.kt>) | 只做窗口适配、导航数据分组与路由分发；经中立入口调用当前设计系统                                                 |

```text
app/shared -> design-system + design-system-material3（导航经中立入口 → M3 原生）
           -> app/sample -> design-system + nomi impl + M3 adapter + Fluent adapter
app/desktop -> app/shared（无 Fluent 专属注入）
material -> design-system-material3 -> design-system
         -> ui-foundation（旧 Material UI，不进入中立核心）
design-system-nomi -> design-system + M3（导航外壳）
design-system-fluent -> design-system + commonMain Fluent api（仅 adapter 工厂暴露原生配置类型）
```

Fluent 继续跨平台，复用[版本目录](<../../gradle/libs.versions.toml>)的 `v0.1.0`，不新增重复 alias 或
snapshot 仓库。
[目标约定](<../../buildSrc/src/main/kotlin/multiplatform-lib-targets.gradle.kts>)
与[项目开关](<../../gradle.properties>)
决定 Android、Desktop、iOS、JS/Wasm 的注册；本轮不改开关。上游有平台产物不等于本仓全部实测。

## 主题：稀疏输入、原生建立、只读投影

| 实际入口                                                                                                              | 职责                                                                                                   |
|-----------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| [NomiThemeRequest](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/runtime/NomiThemeRequest.kt>)   | requestedMode、已解析 isDark、显式 overrides                                                           |
| [ThemeOverrides](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/theme/ThemeOverrides.kt>)         | accentColor 与可空 colorScheme/typography/shapes/spacings/sizes；直接参数归一化到既有模型              |
| [NomiDesignSystem](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/runtime/NomiDesignSystem.kt>)   | `ProvideTheme(request: NomiThemeRequest, content: @Composable (ResolvedNomiTheme) -> Unit)` 与能力声明 |
| [Theme](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/theme/Theme.kt>)                           | 根/局部边界，系统模式解析，规范化请求并发布回调结果                                                    |
| [ResolvedNomiTheme](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/runtime/ResolvedNomiTheme.kt>) | 五组原生结果、请求模式、实际明暗与显式 overrides 来源；前两个构造参数必填                              |
| [ContentStyle](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/runtime/ContentStyle.kt>)           | 显式 slot 颜色与文本样式增量                                                                           |

```text
NomiTheme / NomiThemeOverride
  -> NomiThemeRequest（核心规范化的 requestedMode/isDark/overrides）
  -> adapter 在原生环境内建立自己的主题
  -> content(ResolvedNomiTheme) 回调五组当前原生结果
  -> 核心以 request 规范化请求字段，组件通过 NomiTheme.colorScheme / typography / shapes / spacings / sizes 只读访问
```

当前根签名按旧式习惯排列：`darkMode = NomiTheme.darkMode`，随后是可空
`colorScheme/typography/shapes/spacings/sizes`， 再到 `designSystem = requireNomiDesignSystem()`、
`overrides = NomiThemeOverrides()` 和 content。
`LocalNomiDesignSystem` 为可空且 **无隐式默认**：未显式提供时 `requireNomiDesignSystem()` 抛出带指引的
`IllegalStateException`；`NomiTheme`/`NomiThemeOverride` 入口与各组件读取共同遵守该契约。
`NomiThemeOverride` 同样接收五组直接参数；null 表示未指定，不直接以 `NomiTheme` 的完整投影作默认。

| 规则                                | NomiTheme 根                                             | NomiThemeOverride 局部                     |
|-------------------------------------|----------------------------------------------------------|--------------------------------------------|
| 默认模式 / 系统                     | 继承当前请求模式与已显式提供的设计系统，边界解析系统明暗 | 继承父已解析模式；显式 darkMode 时重新解析 |
| 父完整 colorScheme / accent         | 不继承，未指定时用目标系统当前模式的默认色               | 继承明确颜色组和 accent                    |
| 父 typography/shapes/spacings/sizes | 仅继承明确设置组                                         | 继承明确设置组                             |
| 父 resolved 默认投影                | 不继承                                                   | 不继承                                     |
| 明确 slot                           | 新根重置                                                 | 保留，退出后恢复父级                       |

直接参数与 overrides 在同层重复同一组时 require；同层完整 colorScheme 与 accent 同时指定也 require。
子层完整 colorScheme 清除继承的 accent；子层仅 accent 则叠加父明确色板。 每个非空对象作为完整明确组处理，不猜测
copy 只改变了哪些字段。

`NomiTheme` 仅公开 darkMode 与五组主题属性，不公开 isDark 或任何主题聚合包装。核心运行时保留请求字段与五组结果；不提供聚合主题输入，
**五组中立主题允许直接传入**。组件只通过 NomiTheme 读取结果。 原生特有类型的完整配置继续使用 adapter
工厂。页面尺寸不强制统一所有原生按钮内边距或高度。 Nomi 默认工厂仍只服务自身、无主题缺省读取或调用方显式选择，不能自动给
M3/Fluent 填默认。

### 旧文件职责与默认颜色工厂

此前已按旧主题职责恢复组织与公开构造；上一轮直接使用单组工厂，不新增或改写默认数值：

| 文件                                                                                                                                                                                                                                                                                      | 当前职责                                                                                                                                                           |
|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [Color](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/theme/Color.kt>)                                                                                                                                                                                               | internal LightThemeColors / DarkThemeColors，各 27 个旧默认颜色                                                                                                    |
| [ColorScheme](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/theme/ColorScheme.kt>)                                                                                                                                                                                   | NomiColorScheme 与 defaultLightNomiColorScheme / defaultDarkNomiColorScheme 两个工厂，各保留 27 个具名参数；中立包不再提供 lightColorScheme / darkColorScheme 别名 |
| [Type](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/theme/Type.kt>)                                                                                                                                                                                                 | private DefaultTypographyTokens、十五角色中立 NomiTypography、defaultNomiTypography；取代原 Typography 文件，不依赖 M3                                             |
| [Shape](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/theme/Shape.kt>) / [Spacing](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/theme/Spacing.kt>) / [Size](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/theme/Size.kt>) | private Default*Tokens 单一默认来源，保留原数值；公开 data class 构造与生成成员 copy，默认工厂继续保留                                                             |

`defaultLightNomiColorScheme` / `defaultDarkNomiColorScheme` 保留完整明暗色板默认入口；
`defaultNomiTypography`、`defaultIconSizeScale` / `defaultAvatarSizeScale` 保留默认来源的薄入口。 旧包
lightColorScheme/darkColorScheme 转发到中立 default* 工厂；默认值等价需独立历史基线测试，不拿新工厂对新工厂自证。
当前 9 个主题 data class 为 `NomiColorScheme`、`NomiTypography`、`NomiShapes`、`Spacings`、`NomiSizes`、
`SizeScale`、`Strokes`、`ComponentHeights`、`LayoutConstraints`；全部公开构造，使用编译器生成的成员
`copy`。 原 9 个核心手写复制扩展与 8 个旧门面转发已删除，不保留兼容入口，也无需中立包/旧包的 copy
import；调用方删除旧 import 即可调用成员。 内部构造配合 `ConsistentCopyVisibility` 的旧策略已替代，不使用
`ExposedCopyVisibility` 或遮蔽抑制补救可见性。

构造参数与身份语义保持：

- `NomiColorScheme()` 仍使用原浅色默认；`NomiShapes()`、`Spacings()`、`NomiSizes()` 保留既有默认参数。
- `NomiTypography` 十五角色与四个尺寸子类型的全部字段仍为必填，不为开放构造补默认参数。
- 明暗颜色/形状/间距/完整尺寸工厂返回新外层对象；默认排版和五个尺寸子组工厂仍返回共享不可变对象，
  `NomiSizes` 的默认子组继续复用它们。
- 成员 `copy` 返回新外层对象，未覆盖字段保留原值/引用；相等性、解构和不可变浅复制语义不变，不修改原实例或深复制。

公开构造、默认工厂及无 copy import
的消费示例见[核心 README](<../../core/design-system/README.md#默认对象与复制>)
和[独立消费模块测试](<../../core/design-system-fluent/src/desktopTest/kotlin/consumer/ThemeFactoryCopyConsumptionTest.kt>)
；该轮验证结果见下方记录。

直接调用示例（显式使用旧默认，非全系统默认注入）：

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.defaultLightNomiColorScheme as appLightColorScheme

/** 通过旧式颜色命名参数显式配置 Nomi，其他组按主题继承规则处理。 */
@Composable
fun ExplicitPaletteTheme(content: @Composable () -> Unit) {
    NomiTheme(
        colorScheme = appLightColorScheme(primary = Color(0xFF4466AA)),
        designSystem = NomiDesignSystem.Default,
        content = content,
    )
}
```

| 主题级输入       | Nomi             | M3                               | Fluent                                       |
|------------------|------------------|----------------------------------|----------------------------------------------|
| 完整 colorScheme | 支持             | 映射原生并保留完整中立读模型     | 拒绝；用 accent 或 typed Colors              |
| NomiTypography   | 十五角色完整消费 | 映射原生角色并保留完整中立读模型 | 明确传入时映射八角色，完整中立组仍保留作读取 |
| NomiShapes       | 支持             | 映射原生角色并保留九级中立组     | 拒绝；用 typed CornerRadius                  |
| spacings / sizes | 支持             | 支持页面布局扩展                 | 支持页面布局扩展                             |

本表是主题输入边界，和下方 Button/Card 的组件级能力独立；尤其不能将 Fluent 拒绝主题 shapes 误读为所有
Surface shape 都不支持。

## 组件 API：不补齐未指定的视觉参数

| 入口                                                                                                                | 默认与覆盖契约                                                                                                                                                      |
|---------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [NomiText](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/text/NomiText.kt>)          | String/AnnotatedString 双重载；style? 保留原生文本环境；逐字段覆盖、onTextLayout、autoSize、inlineContent 透传；颜色 Unspecified 不预填正文色；Fluent 拒绝 autoSize |
| [NomiButton](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/button/NomiButton.kt>)    | variant/size/shape/contentPadding/textStyle 可空；颜色逐字段 Unspecified；原生组件处理默认视觉与交互                                                                |
| [NomiSurface](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/surface/NomiSurface.kt>) | shape?、稀疏颜色和 NomiBorder，派发原生表面                                                                                                                         |
| [NomiCard](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/card/NomiCard.kt>)          | 真正非点击 Card，表面族独立 Card 方法；contentPadding? 仅显式覆盖 slot                                                                                              |
| [NomiBorder](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/NomiBorder.kt>)           | Default 保留原生描边；None 明确无描边；Stroke 明确指定                                                                                                              |

Color.Transparent 是有效覆盖，不能当作 Unspecified。描边只接受 NomiBorder 三态：Default 保留原生描边，None
明确移除； 不保留本会话生成的 nullable BorderStroke 兼容重载。`NomiButtonDefaults` /
`NomiSurfaceDefaults` 仍可作为调用方主动选择的预设，但组件零参路径不自动调用。 统一 metrics 不再是全部
adapter 的强制默认，Card 不再是核心统一 `Surface + padding + Column` 配方。

文本颜色顺序保持显式 color > 本次 style.color > 显式 slot 色 > 继承样式色；无值留给原生 renderer。 无显式
style 时保持 null，不在核心合并统一 bodyMedium；不要将原生主题默认反向投影为“显式 slot”， 压过原生按钮的
disabled/hover/pressed 内容样式。

### 已确认的覆盖能力

[NomiComponentCapabilities](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/NomiComponentCapabilities.kt>)
供 UI 提示与核心边界校验；UI 不显示不支持控件，不代表调用边界可以静默忽略非法输入。

| 参数/能力                                  | Nomi             | Material 3           | Fluent v0.1.0                    |
|--------------------------------------------|------------------|----------------------|----------------------------------|
| 零视觉参数调用                             | Nomi 自身默认    | M3 原生默认          | Fluent 原生默认                  |
| Button variant、稀疏颜色、textStyle        | 支持             | 支持                 | 按原生按钮入口/颜色方案映射      |
| Button 非 null size、shape、contentPadding | 支持             | 支持公开的组件级覆盖 | **拒绝**，不包装或重绘以伪造支持 |
| Surface 显式 shape、颜色、border           | 支持             | 支持                 | 使用原生 Layer                   |
| 静态 Card shape / slot contentPadding      | 支持             | 支持                 | 支持                             |
| 静态 Card 指定颜色或非 Default border      | 支持             | 支持                 | **拒绝，改用 NomiSurface**       |
| 导航 Bar / Rail                            | 支持             | 支持                 | 支持                             |
| 导航 Drawer                                | **能力声明禁用** | 支持（永久抽屉）     | 支持（LeftCompact）              |
| 导航底部项分组                             | 支持             | 支持                 | 支持（原生 footerItems）         |
| 导航项级 modifier                          | 支持             | 支持                 | **拒绝**（原生菜单无项级修饰符） |

M3 “支持 shape”指组件级任意 Shape。主题级 `Shapes` 的标准角色受现有中立 `NomiShapes` 读模型限制， 不是
RoundedCornerShape 时明确拒绝；不能写成任意主题形状都能无损投影。

## 导航套件

导航是第四个中性组件族；入口、作用域与 SPI 在同一文件，能力声明决定可用布局。

| 实际入口                                                                                                                                  | 职责                                                                                                                                                                                                                                                          |
|-------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [NomiNavigation](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/navigation/NomiNavigation.kt>)              | `NomiNavigationSuite(layoutType, modifier, items, footerItems, content)`；`NomiNavigationSuiteType` 为 Bar/Rail/Drawer；`NomiNavigationItemScope.NavigationItem(selected, onClick, modifier, icon, badge, label, colors, interactionSource)` 以槽位声明导航项 |
| [NomiDesignSystemComponents](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/NomiDesignSystemComponents.kt>) | 新增 `navigation: NomiNavigationRenderer`；与 text/actions/surfaces 并列                                                                                                                                                                                      |
| [NomiComponentCapabilities](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/NomiComponentCapabilities.kt>)   | 新增 `navigationBar`/`navigationRail`/`navigationDrawer` 与 `navigationItemModifier`；默认支持 Bar/Rail 与项级 modifier，Drawer 与不支持项级 modifier 的实现必须显式声明                                                                                      |

调用契约：

- 选中状态、点击回调与路由由调用方持有；导航项的图标、徽标与文案都是调用方组合的槽位，核心不限定类型，也不替调用方设置尺寸或无障碍名称。
- `items` 与 `footerItems` 两个分组按声明顺序渲染；是否把底部组固定到底部由原生实现决定。
- 布局类型由调用方按窗口形态选择；核心与适配器都不读取窗口或平台状态。
- 颜色只通过 `NomiNavigationItemColors` 的
  contentColor/selectedContentColor/disabledContentColor/indicatorColor 稀疏覆盖；
  未指定字段保留原生导航项颜色。槽内内容应读取原生内容环境，使悬停/按下/禁用状态色继续生效。
- 中立 IconPack 的 ImageVector 固有尺寸为 256dp；槽位交给调用方组合，因此
  **图标尺寸由调用方在槽内显式约束**
  （app/shared 使用 `Modifier.size(NomiTheme.sizes.icon.large)`）；适配器不读取也不改写槽内容的尺寸。
- 项级 `modifier` 与 `interactionSource` 由支持原生项入口的实现消费；Fluent 原生菜单无项级
  modifier，能力置 false， 传入非默认 modifier 时实现明确 require 失败，不静默忽略。
- 未提供设计系统时入口抛出带指引的 `IllegalStateException`；当前设计系统不支持所选布局时在原生渲染前抛出带系统标识的
  `IllegalArgumentException`。

三种适配：

| 布局   | Nomi                                                        | Material 3                                            | Fluent v0.1.0                   |
|--------|-------------------------------------------------------------|-------------------------------------------------------|---------------------------------|
| Bar    | 自定义 M3 `NavigationBar` 外壳，复刻既有 Navigation.kt 视觉 | `NavigationBar` + `NavigationBarItem`                 | `NavigationView` 的 Top         |
| Rail   | 自定义 M3 `NavigationRail` 外壳（栏宽 60dp）                | `NavigationRail` + `NavigationRailItem`               | `NavigationView` 的 Left        |
| Drawer | 能力声明禁用，renderer 仅保留防御性失败                     | `PermanentNavigationDrawer` + `NavigationDrawerItem`  | `NavigationView` 的 LeftCompact |
| 底部组 | Rail 用弹性间隔推底，Bar 与主导航同排                       | Rail 用 weight spacer，Bar 同排，Drawer 抽屉内 spacer | 原生 `footerItems`              |

Nomi/M3 的导航项默认颜色与标签排版读取 `NomiTheme`（选中 primary、未选中 onBackground、指示器 primary
低透明）； 显式 `NomiNavigationItemColors` 覆盖映射到各原生颜色入口。Fluent 以原生 PentaVisualScheme
为基线，覆盖 contentColor/indicatorColor/disabled 内容色。 app/shared 只负责 `windowAdaptive` →
Bar/Rail 映射、`NavId.Null` 分组与槽位内容组合（M3 Icon + NomiText）， 不再直接使用 M3 导航外壳或旧
`AppTheme`。

## 列表项组件族

第六个组件族把“设置行”泛化为列表项原语：
`NomiListItem(title, modifier, onClick, leading, subtitle, trailing)`
只负责把槽位交给当前设计系统的原生列表项，行高、容器、圆角、悬停/按下/点击外观与文本样式全部保持原生默认。
槽位为 null 时不占位；`onClick = null` 表示静态行，实现不得附加点击语义。入口与 SPI 见
[NomiListItem](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/list/NomiListItem.kt>)，
[组件集合](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/NomiDesignSystemComponents.kt>)
新增 `listItems` 族；本版没有“可能不被支持的显式覆盖”，因此不新增能力字段。

### 三端原生映射与决策

| 能力      | Material 3                     | Fluent                              | Nomi                                 |
|-----------|--------------------------------|-------------------------------------|--------------------------------------|
| 原生基元  | `ListItem`                     | `CardExpanderItem` 点击/无点击重载  | 透明 `Row` + NomiTheme 令牌          |
| 标题      | `headlineContent`              | `heading` + 注入 bodyStrong         | `titleSmall` + SemiBold、textPrimary |
| 副标题    | `supportingContent`            | 原生 caption 槽                     | `bodySmall`、textSecondary           |
| 前导      | `leadingContent`               | 48dp icon 槽                        | Row 起始槽                           |
| 尾部      | `trailingContent`              | `trailing` + 注入 caption/secondary | textSecondary                        |
| 点击      | `Modifier.clickable`           | 原生 Card 五态（含 hover/pressed）  | 12dp 圆角 `clickable`                |
| 行高/容器 | 一行 56dp、两行 72dp，无容器色 | 62dp 卡片行、1dp 描边               | 由令牌内边距与内容决定               |

- 选 `CardExpanderItem` 而非 Fluent `ListItem`/`ExpanderItem`：前者原生带
  heading/caption/icon/trailing 与卡片五态；`ListItem` 是单文本槽、16dp 图标的密集列表行，
  `ExpanderItem` 背景不一致。`dropdown` 保持 null，静态行使用无点击重载，两种状态外观一致且不出现展开箭头。
- v1 不提供 `enabled`：M3 1.9.0 的 `ListItem` 内部固定 `enabled = true` 取色，没有原生禁用内容色；
  `onClick`/`enabled` 到 1.10.0-alpha05 的 `InteractiveListItem` 才出现。适配器自行降透明属于伪造原生状态，
  因此不可点统一由 `onClick = null` 表达，禁用视觉留待 M3 升级或控件族统一。
- v1 不提供 `selected`、展开、`overline`、分组标题、分隔线、颜色/形状/内边距/高度覆盖：这些能力只有一侧
  原生支持，跨系统统一需要重绘；按既有原则不提前增加空参数。
- Fluent 上游只为 caption 提供原生文本样式，适配器为标题注入 bodyStrong、为尾部注入 caption 与
  secondary 内容色（与 Fluent `ListItem` 的尾部处理一致）；这与 Nomi 通过 `ProvideNomiContentStyle`
  建立槽位环境 同类，不是重绘组件。
- 旧 `:core:ui-foundation` `SettingItem` 全仓零调用点，直接删除：`SettingItem` → `NomiListItem`，
  `content`/`trailingIcon` 组合进 `trailing` 槽，`SettingSwitch`/`SettingRadio`/`SettingCheckbox`
  变体留待 控件族，`isWeight`/`contentPadding`/`useImageCompose` 与 `SettingStyle`/`SettingDefaults`
  删除；
  `ScrollChoose` 保留。
- 行为差异是原生默认，不做跨系统对齐：M3 点击只有 ripple、无悬停容器色；Fluent 有 hover/pressed 变色；
  Nomi 使用主题圆角与令牌内边距。行内含 `NomiButton` 等交互槽时由各自原生组件处理点击。

```text
M3（透明行，两行 72dp）          Fluent（62dp 卡片行、1dp 描边）      Nomi（透明行、令牌内边距）
──────────────────────────       ┌──────────────────────────┐       ──────────────────────────
 [leading] 标题         值 >     │ [leading] 标题     值 >  │        [leading] 标题         值 >
           副标题                │           副标题          │                 副标题
──────────────────────────       └──────────────────────────┘       ──────────────────────────
```

实现入口：[Material3ListItemRenderer](<../../core/design-system-material3/src/commonMain/kotlin/ciyin/designsystem/material3/list/Material3ListItemRenderer.kt>)、
[FluentListItemRenderer](<../../core/design-system-fluent/src/commonMain/kotlin/ciyin/designsystem/fluent/list/FluentListItemRenderer.kt>)、
[DefaultNomiListItemRenderer](<../../core/design-system-nomi/src/commonMain/kotlin/ciyin/designsystem/nomi/list/DefaultNomiListItemRenderer.kt>)；
展示见 [DesignSystemListItemShowcase](<../../app/sample/src/commonMain/kotlin/com/ciyin/app/presentation/screen/designsystem/component/DesignSystemListItemShowcase.kt>)，
契约测试见 [ListItemDispatchContractTest](<../../core/design-system/src/commonTest/kotlin/ciyin/designsystem/ListItemDispatchContractTest.kt>)。

## 各实现与拆分入口

公共 component 留在核心，内置实现 nomi、material3、fluent 各自模块根下按
text/button/surface/card/navigation/scaffold/list 组织，package 与目录一致；nomi 为独立 Gradle
模块但包名不变。 SPI 和专属参数类型随所在组件文件；FluentButtonColors 归 button。共享
NomiBorder、NomiControlSize、NomiComponentCapabilities、NomiDesignSystemComponents 留在核心 component
根， NomiNativeContentStyle 与 internal DefaultNomiSystem 位于 nomi 模块根（经
`NomiDesignSystem.Default` 暴露），集中内置主题及组件装配；公共主题 API/数据留在 theme，通用请求/契约/显式
slot 留在 runtime。 LocalNomiDesignSystem 为可空且无隐式默认，应用根/测试/Preview 显式提供；M3/Fluent
的主题、转换、投影与装配位置不变。text/actions/surfaces/navigation/scaffold/listItems 六族 SPI，Card 仍由
surfaces 的独立方法派发，导航套件由 navigation 族派发。 消费者直接更新 imports 与显式提供点，不增加旧
package 转发、typealias、兼容重载或隐式回退。

### Nomi

[DefaultNomiSystem](<../../core/design-system-nomi/src/commonMain/kotlin/ciyin/designsystem/nomi/DefaultNomiDesignSystem.kt>)
（internal）只组装主题和 renderer，经 `NomiDesignSystem.Default` 暴露。
组件实现为[Text](<../../core/design-system-nomi/src/commonMain/kotlin/ciyin/designsystem/nomi/text/DefaultNomiTextRenderer.kt>)、
[Button](<../../core/design-system-nomi/src/commonMain/kotlin/ciyin/designsystem/nomi/button/DefaultNomiActionRenderer.kt>)、
[Surface](<../../core/design-system-nomi/src/commonMain/kotlin/ciyin/designsystem/nomi/surface/DefaultNomiSurfaceRenderer.kt>)、
[Card](<../../core/design-system-nomi/src/commonMain/kotlin/ciyin/designsystem/nomi/card/DefaultNomiCard.kt>)
与
[Navigation](<../../core/design-system-nomi/src/commonMain/kotlin/ciyin/designsystem/nomi/navigation/DefaultNomiNavigationRenderer.kt>)
与 [List](<../../core/design-system-nomi/src/commonMain/kotlin/ciyin/designsystem/nomi/list/DefaultNomiListItemRenderer.kt>)；
导航外壳以 M3 `NavigationBar`/`NavigationRail` 为原生载体，复刻既有 Navigation.kt 的视觉，Drawer
由能力声明禁用。 Nomi 可以使用自己的 Foundation 样式和交互反馈，它们不再充当其他 adapter 的原生默认。

### Material 3

[主题配置](<../../core/design-system-material3/src/commonMain/kotlin/ciyin/designsystem/material3/Material3DesignSystem.kt>)
默认建立 Material 原生调色板、Typography 和 Shapes；`rememberMaterial3DesignSystem` 提供专属显式配置。
[Material3Projection](<../../core/design-system-material3/src/commonMain/kotlin/ciyin/designsystem/material3/Material3Projection.kt>)
负责把当前原生主题转换为中立只读值，而不是把 Nomi defaults 反向灌入原生主题。 明确传入
colorScheme/typography/shapes 后，M3 映射其原生角色，同时保留完整中立组作读取， 不能让未映射角色经原生往返丢失。旧门面的原始
Material Typography 身份仍由 legacy 配置保持。

实际渲染拆为[Text](<../../core/design-system-material3/src/commonMain/kotlin/ciyin/designsystem/material3/text/Material3Text.kt>)、
[Button](<../../core/design-system-material3/src/commonMain/kotlin/ciyin/designsystem/material3/button/Material3Button.kt>)、
[Surface](<../../core/design-system-material3/src/commonMain/kotlin/ciyin/designsystem/material3/surface/Material3Surface.kt>)、
[Card](<../../core/design-system-material3/src/commonMain/kotlin/ciyin/designsystem/material3/card/Material3Card.kt>)
与
[Navigation](<../../core/design-system-material3/src/commonMain/kotlin/ciyin/designsystem/material3/navigation/Material3NavigationRenderer.kt>)
与 [List](<../../core/design-system-material3/src/commonMain/kotlin/ciyin/designsystem/material3/list/Material3ListItemRenderer.kt>)；
[聚合](<../../core/design-system-material3/src/commonMain/kotlin/ciyin/designsystem/material3/Material3Components.kt>)
只组装六个组件族。

### Fluent

既有实现继续采用真正的 `Button` / `AccentButton` / `SubtleButton` / `HyperlinkButton` 与
`surface.Card`。 Surface 仍可使用原生 Layer，但 **不得继续 Layer + clickable 重绘 Button**。 过去重绘的理由是统一
size/padding/role；现在用户选择保留原生默认，代价通过能力声明表达， 不再为达到统一视觉契约覆盖库自身实现。当前真实实现已拆为
[FluentText](<../../core/design-system-fluent/src/commonMain/kotlin/ciyin/designsystem/fluent/text/FluentText.kt>)、
[FluentButton](<../../core/design-system-fluent/src/commonMain/kotlin/ciyin/designsystem/fluent/button/FluentButton.kt>)、
[FluentSurface](<../../core/design-system-fluent/src/commonMain/kotlin/ciyin/designsystem/fluent/surface/FluentSurface.kt>)
与
[FluentCard](<../../core/design-system-fluent/src/commonMain/kotlin/ciyin/designsystem/fluent/card/FluentCard.kt>)
与
[FluentNavigation](<../../core/design-system-fluent/src/commonMain/kotlin/ciyin/designsystem/fluent/navigation/FluentNavigationRenderer.kt>)
与 [FluentList](<../../core/design-system-fluent/src/commonMain/kotlin/ciyin/designsystem/fluent/list/FluentListItemRenderer.kt>)；
[FluentComponents](<../../core/design-system-fluent/src/commonMain/kotlin/ciyin/designsystem/fluent/FluentComponents.kt>)
仅组装六族，
[主题配置](<../../core/design-system-fluent/src/commonMain/kotlin/ciyin/designsystem/fluent/FluentDesignSystem.kt>)
负责原生主题和 `rememberFluentDesignSystem`。
完整边界见[Fluent README](<../../core/design-system-fluent/README.md>)。

无强调色请求时保留 Fluent 自身默认色阶；只有明确 accent 才应用覆盖。 Fluent 接受明确的中立 typography
并映射八个原生角色，未传时不运行该映射；拒绝中立完整 colorScheme 与九级 shapes， 分别使用 accent 或
`rememberFluentDesignSystem(colors = ..., cornerRadius = ...)` 配置原生对象。
原生字体、形状和状态不从父系统的中立投影回填。原生组件自身的能力限制应显式记录， 不能为通过旧统一外观测试而重新增加
clickable、强制尺寸或统一 token。

## 旧包兼容与迁移

[旧 Theme](<../../core/material/src/commonMain/kotlin/ciyin/material/theme/Theme.kt>)
仍保留旧参数名、顺序、颜色默认选择、 Material Typography 类型、DarkMode
真枚举、类型别名、旧颜色工厂、形状扩展和图标入口；旧包默认工厂转发已删除，矢量仍由中立 IconPack 单一维护。
此前已删除冗余 copy 转发，旧类型别名继续使用核心公开构造和生成成员
copy。上一轮只改旧门面的内部主题配置与读取路径，保留既有对外签名及原生 Typography 身份。

旧函数将颜色、原始 Material Typography、形状、间距和尺寸直接交给 `rememberLegacyMaterial3DesignSystem`
，由 M3 特定配置保留完整旧读模型， 然后调用新主题入口；旧门面不向核心传入主题聚合对象。旧 Typography
的额外角色/实例不能通过十五角色往返丢失。 这是局部源码兼容，不承诺旧二进制 ABI，也不是全局双主题桥。

[应用根](<../../app/shared/src/commonMain/kotlin/com/ciyin/app/presentation/app/App.kt>)保持显式 M3；
[sample 候选](<../../app/sample/src/commonMain/kotlin/com/ciyin/app/presentation/screen/sample/SampleDesignSystems.kt>)
保持共享 Nomi/M3/Fluent，provider 可选覆盖，不恢复 Desktop 特殊注入。

迁移现有新 API 调用方时：

1. 删除为了“统一外观”补入的默认 size、variant、colors、shape、padding；真有产品意图时才保留为显式覆盖。
2. 使用五组直接参数或 NomiThemeOverrides 的明确字段，同层同组只保留一个来源；库专属完整配置仍用
   adapter 工厂，不重新引入主题聚合输入。
3. 用 NomiBorder 三态表达意图；显式 null 不是原生默认。
4. 查能力后展示可用覆盖与限制，不过滤不支持输入掩盖问题。需要 Fluent 卡片颜色/描边时选择 NomiSurface。
5. 既有请求+回调与真实 Card SPI 保持；旧工厂命名冲突用 import alias，不增加新的主题模型或 locals。
6. 删除中立包与旧门面的 copy 扩展 import；直接构造或默认工厂后调用生成成员 copy，不保留旧扩展的别名、转发或兼容重载。

## Sample 展示与 UI 结构

[展示页入口](<../../app/sample/src/commonMain/kotlin/com/ciyin/app/presentation/screen/designsystem/DesignSystemDemoScreen.kt>)
继续是纯 UI MVI gallery，不引入 Repository、网络、数据库或新 DI。
[DesignSystemShowcase](<../../app/sample/src/commonMain/kotlin/com/ciyin/app/presentation/screen/designsystem/component/DesignSystemShowcase.kt>)
仅编排原生默认区和拆分后的展示组件：
[按钮](<../../app/sample/src/commonMain/kotlin/com/ciyin/app/presentation/screen/designsystem/component/DesignSystemButtonShowcase.kt>)、
[文本](<../../app/sample/src/commonMain/kotlin/com/ciyin/app/presentation/screen/designsystem/component/DesignSystemTextShowcase.kt>)、
[表面](<../../app/sample/src/commonMain/kotlin/com/ciyin/app/presentation/screen/designsystem/component/DesignSystemSurfaceShowcase.kt>)、
[卡片](<../../app/sample/src/commonMain/kotlin/com/ciyin/app/presentation/screen/designsystem/component/DesignSystemCardShowcase.kt>)、
[列表项](<../../app/sample/src/commonMain/kotlin/com/ciyin/app/presentation/screen/designsystem/component/DesignSystemListItemShowcase.kt>)、
[局部覆盖](<../../app/sample/src/commonMain/kotlin/com/ciyin/app/presentation/screen/designsystem/component/DesignSystemOverrideShowcase.kt>)
与
[限制提示](<../../app/sample/src/commonMain/kotlin/com/ciyin/app/presentation/screen/designsystem/component/DesignSystemRestrictionShowcase.kt>)。
测试与宿主入口见[sample README](<../../app/sample/README.md>)
；前轮已运行回归和三系统原生默认/显式区截图，上一轮主题结果扁平化不改变此布局，也不据旧截图声明当前测试通过。

```text
Material3 应用根
└─ Sample 独立导航栈
   └─ Gallery：Nomi / Material3 / Fluent + 明暗模式 + 字体比例
      ├─ 原生默认：Text / Button / Surface / Card / ListItem（不填视觉默认参数）
      ├─ 显式覆盖：只演示用户主动指定的颜色/变体/可支持参数
      └─ 能力限制提示：Fluent Button size/shape/padding；Card colors/border

窄/中窗口：控制区在上，展示区在下，整页滚动
宽窗口：  [紧凑控制区] | [带标签原生网格 / 横向按钮 / 可展开限制 / 单项排版预览]
```

后续布局优化保持原生风格：各展示区以原生 Card 组织，四默认示例和表面层级复用 foundation 自适应网格；
排版角色使用选择器与单项真实字号预览，避免十五种标题顺序撑长页面；限制说明默认折叠。
列表项分区展示可点击行（前导图标、标题/副标题、值文本与箭头）与静态行（尾部设计系统按钮），不传颜色、形状或内边距参数。
控制区不再按页面三分之一无限变宽，大字体时按可用空间回退单列。

只读投影可供说明和显式示例使用，但不能把全部投影自动填回“原生默认”区。局部覆盖展示必须验证切换新系统
使用其原生默认，显式增量继承且退出恢复。测试/预览不启动真实设备能力。

## 验证记录与验收缺口

### 本轮：列表项组件族与三适配，2026-11-15

范围：`:core:design-system` 新增第六族 `NomiListItem` 与 `listItems` renderer；M3/Fluent/Nomi 三端原生映射；
`:core:ui-foundation` 删除旧 `SettingItem`；sample 增加列表项分区；README、skill 与设计索引同步。

- 核心 47、M3 34、Fluent 53、Nomi 24、旧门面 19、sample 26（`--tests "*DesignSystemDemo*"`）共 203 项
  Desktop 回归通过，零失败、零跳过；五模块 `compileCommonMainKotlinMetadata`、sample metadata、 app
  shared 与 app desktop 编译通过。
- 契约测试覆盖默认稀疏派发、槽位 null 不占位、显式参数透传、点击回调、静态行无点击动作、无设计系统时
  带指引失败；Fluent 标题槽 bodyStrong 与尾部 secondary 内容色、Nomi 标题槽 titleSmall+SemiBold 断言通过。
- 未执行：Android/iOS/Web、设备运行、截图对比与 Release 验收。M3 1.9.0 `ListItem` 无原生禁用内容色，
  `enabled` 留待 M3 升级 `InteractiveListItem` 或控件族统一后补充；选择控件族尚未落地，列表项尾部目前只演示
  文本、图标与设计系统按钮。
- 原生行高差异（M3 56/72dp、Fluent 62dp、Nomi 内容推断）是预期差异，不作为回归断言语义。

### 本轮：设计系统绑定命名空间与旧单例迁移，2026-11-14

-

核心 [NomiDesignSystem](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/runtime/NomiDesignSystem.kt>)
新增空 `companion object`，仅作为 adapter 实现的绑定命名空间；核心不依赖任何 adapter，adapter 之间也互不依赖。

- 三个实现对象降为 internal：`DefaultNomiSystem`（nomi）、`Material3System`（material3）、`FluentSystem`
  （fluent）；旧顶层 object `DefaultNomiDesignSystem`、`Material3DesignSystem`、`FluentDesignSystem`
  已删除，不保留别名、typealias 或转发。
- 对外只暴露伴生对象上的扩展属性：`val NomiDesignSystem.Companion.Default: NomiDesignSystem`（
  `ciyin.designsystem.nomi`）、`.Material3`（`ciyin.designsystem.material3`）、`.Fluent`（
  `ciyin.designsystem.fluent`）。扩展属性不是接口成员，必须导入对应声明，IDE 才会在 `NomiDesignSystem.`
  后补全。
- 典型用法 `NomiTheme(designSystem = NomiDesignSystem.Material3)`；
  `CompositionLocalProvider(LocalNomiDesignSystem provides NomiDesignSystem.Default)`
  等显式提供方式与无隐式默认契约不变。
- 扩展属性是无 backing field 的只读 `val`：只有 getter，不能赋值或被覆盖；每次读取返回对应 adapter
  持有的同一实现实例，`NomiDesignSystem.X.id`/`.capabilities` 可作为稳定判断与能力查询依据。
- 旧门面兼容判断同步改为
  `LocalNomiDesignSystem.current?.id == NomiDesignSystem.Material3.id`（[旧 Theme](<../../core/material/src/commonMain/kotlin/ciyin/material/theme/Theme.kt>)
  导入 `ciyin.designsystem.material3.Material3`），未提供设计系统时的回退语义不变。
- 本轮迁移 29 个文件共 128 处引用（3 处实现声明改名，其余为 import 与调用点），并同步 6 个
  README、设计索引与两个设计系统 skill；伴生对象保持无状态、无成员。
- Nomi Rail 文案槽采用 tooltip 语义（以用户手改实现为准）：Rail 项只显示图标与徽标，`label` 悬停时以原生
  `TooltipBox` 在右侧提示、不进入可见内容，也不套用 `NomiTheme` 排版；Bar 仍直接渲染文案。Nomi 的 rail
  单测、README 与该实现 KDoc 已按此对齐（用例断言选中/点击/项级修饰符，并断言文案不进入可见语义树）。
- 验证（Windows / JDK 21）：`:core:design-system`、`:core:design-system-nomi`、
  `:core:design-system-material3`、`:core:design-system-fluent`、`:core:material` 的
  `compileCommonMainKotlinMetadata` 通过；`:app:desktop:compileKotlin` 与