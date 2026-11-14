# Design System 模块化方案

> 状态：已实现（编译与 218 项回归已核；保留平台与发布验证缺口）；设计系统实现改由 `NomiDesignSystem`
> 伴生对象上的扩展属性暴露，旧顶层单例名称已删除。
> 范围：`NomiDesignSystem` 新增空 `companion object` 作为绑定命名空间；三个实现对象降为 internal（
> `DefaultNomiSystem`/`Material3System`/`FluentSystem`）；对外为
> `NomiDesignSystem.Default/Material3/Fluent` 扩展属性；29 个文件 128 处引用迁移，6 个 README、设计索引与
> 2 个 skill 同步。
> 相关模块：`:core:design-system`、`:core:design-system-nomi`、`:core:design-system-material3`、
> `:core:design-system-fluent`、`:core:material`、`:app:shared`、`:app:sample`。
> 最近核对：2026-11-14；master 未提交工作树（设计系统绑定命名空间改造）。
> 验证记录：本轮 metadata 编译、app 编译与 218 项 Desktop 回归通过；导航轮的 metadata、app 编译与 180 项
> Desktop 回归见“前轮：导航组件族与三适配”。

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
不一次性重写旧页面；不自动打开项目未启用的平台。首期仍只有四组件，二期列表见文末。

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
`IllegalStateException`；`NomiTheme`/`NomiThemeOverride` 入口与四组件读取共同遵守该契约。
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

## 各实现与拆分入口

公共 component 留在核心，内置实现 nomi、material3、fluent 各自模块根下按
text/button/surface/card/navigation 组织，package 与目录一致；nomi 为独立 Gradle 模块但包名不变。 SPI
和专属参数类型随所在组件文件；FluentButtonColors 归 button。共享
NomiBorder、NomiControlSize、NomiComponentCapabilities、NomiDesignSystemComponents 留在核心 component
根， NomiNativeContentStyle 与 internal DefaultNomiSystem 位于 nomi 模块根（经
`NomiDesignSystem.Default` 暴露），集中内置主题及组件装配；公共主题 API/数据留在 theme，通用请求/契约/显式
slot 留在 runtime。 LocalNomiDesignSystem 为可空且无隐式默认，应用根/测试/Preview 显式提供；M3/Fluent
的主题、转换、投影与装配位置不变。text/actions/surfaces/navigation 四族 SPI，Card 仍由 surfaces
的独立方法派发，导航套件由 navigation 族派发。 消费者直接更新 imports 与显式提供点，不增加旧 package
转发、typealias、兼容重载或隐式回退。

### Nomi

[DefaultNomiSystem](<../../core/design-system-nomi/src/commonMain/kotlin/ciyin/designsystem/nomi/DefaultNomiDesignSystem.kt>)
（internal）只组装主题和 renderer，经 `NomiDesignSystem.Default` 暴露。
组件实现为[Text](<../../core/design-system-nomi/src/commonMain/kotlin/ciyin/designsystem/nomi/text/DefaultNomiTextRenderer.kt>)、
[Button](<../../core/design-system-nomi/src/commonMain/kotlin/ciyin/designsystem/nomi/button/DefaultNomiActionRenderer.kt>)、
[Surface](<../../core/design-system-nomi/src/commonMain/kotlin/ciyin/designsystem/nomi/surface/DefaultNomiSurfaceRenderer.kt>)、
[Card](<../../core/design-system-nomi/src/commonMain/kotlin/ciyin/designsystem/nomi/card/DefaultNomiCard.kt>)
与
[Navigation](<../../core/design-system-nomi/src/commonMain/kotlin/ciyin/designsystem/nomi/navigation/DefaultNomiNavigationRenderer.kt>)；
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
[Navigation](<../../core/design-system-material3/src/commonMain/kotlin/ciyin/designsystem/material3/navigation/Material3NavigationRenderer.kt>)；
[聚合](<../../core/design-system-material3/src/commonMain/kotlin/ciyin/designsystem/material3/Material3Components.kt>)
只组装四个组件族。

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
[FluentNavigation](<../../core/design-system-fluent/src/commonMain/kotlin/ciyin/designsystem/fluent/navigation/FluentNavigationRenderer.kt>)；
[FluentComponents](<../../core/design-system-fluent/src/commonMain/kotlin/ciyin/designsystem/fluent/FluentComponents.kt>)
仅组装四族，
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
[局部覆盖](<../../app/sample/src/commonMain/kotlin/com/ciyin/app/presentation/screen/designsystem/component/DesignSystemOverrideShowcase.kt>)
与
[限制提示](<../../app/sample/src/commonMain/kotlin/com/ciyin/app/presentation/screen/designsystem/component/DesignSystemRestrictionShowcase.kt>)。
测试与宿主入口见[sample README](<../../app/sample/README.md>)
；前轮已运行回归和三系统原生默认/显式区截图，上一轮主题结果扁平化不改变此布局，也不据旧截图声明当前测试通过。

```text
Material3 应用根
└─ Sample 独立导航栈
   └─ Gallery：Nomi / Material3 / Fluent + 明暗模式 + 字体比例
      ├─ 原生默认：Text / Button / Surface / Card（不填视觉默认参数）
      ├─ 显式覆盖：只演示用户主动指定的颜色/变体/可支持参数
      └─ 能力限制提示：Fluent Button size/shape/padding；Card colors/border

窄/中窗口：控制区在上，展示区在下，整页滚动
宽窗口：  [紧凑控制区] | [带标签原生网格 / 横向按钮 / 可展开限制 / 单项排版预览]
```

后续布局优化保持原生风格：各展示区以原生 Card 组织，四默认示例和表面层级复用 foundation 自适应网格；
排版角色使用选择器与单项真实字号预览，避免十五种标题顺序撑长页面；限制说明默认折叠。
控制区不再按页面三分之一无限变宽，大字体时按可用空间回退单列。

只读投影可供说明和显式示例使用，但不能把全部投影自动填回“原生默认”区。局部覆盖展示必须验证切换新系统
使用其原生默认，显式增量继承且退出恢复。测试/预览不启动真实设备能力。

## 验证记录与验收缺口

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
  `:app:android:compileDebugKotlin` 通过；Desktop 回归共 218 项——核心 42、Nomi 18、M3 30、Fluent 46、旧门面
  19、sample 63（全量 `desktopTest`），零失败、零跳过；KDoc 扫描 0 硬错误；`git diff --check` 通过。
- 已知边界：扩展属性按模块分离，调用方必须逐模块导入；实现对象 internal 后外部只能依赖
  `NomiDesignSystem` 接口，不再能引用具体实现类型。
- 以下历史记录中出现的旧顶层 object 名称为当轮事实，仅作历史证据（含当时示例），不代表当前 API。

### 前轮：导航组件族与三适配，2026-11-13

-

核心新增中立导航组件族：[NomiNavigation](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/navigation/NomiNavigation.kt>)
提供 `NomiNavigationSuite`、`NomiNavigationSuiteType`（Bar/Rail/Drawer）、
`NomiNavigationItemScope.NavigationItem` 与
`NomiNavigationRenderer`；[NomiDesignSystemComponents](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/NomiDesignSystemComponents.kt>)
增加第四族 `navigation`。

- 导航项为槽位式：
  `NavigationItem(selected, onClick, modifier, icon, badge, label, colors, interactionSource)`
  ，icon/badge/label 由调用方组合，没有 enabled；`NomiNavigationItemColors` 只提供
  content/selected/disabled 内容色与指示器色的稀疏覆盖；图标尺寸与无障碍名称由调用方负责（IconPack 固有
  256dp）。
- 能力声明扩展为 `navigationBar`/`navigationRail`/`navigationDrawer` 与 `navigationItemModifier`
  ：默认支持 Bar/Rail 与项级 modifier，Drawer 需实现显式声明，Fluent 声明不支持项级
  modifier；入口/实现在原生渲染前按能力拒绝，并带设计系统标识。
- Nomi 适配器按既有 Navigation.kt 口径实现 Bar/Rail（M3 原生外壳 + NomiTheme 令牌，Rail 60dp），Drawer
  能力禁用；M3 适配器使用 NavigationBar/NavigationRail/PermanentNavigationDrawer；Fluent 适配器使用
  NavigationView 的 Top/Left/LeftCompact，并以“组合阶段记录声明 → 原生菜单 DSL 重放”桥接非
  @Composable 菜单。
- app/shared 的 Navigation.kt 改为只做 `windowAdaptive` → Bar/Rail 映射与 `NavId.Null` 分组，路由仍由
  App.kt 分发；AppViewModel/NavModel 图标改用中立 IconPack；AppPreview/ScreenScaffold/MainScreen 去掉旧
  AppTheme 与 ui-foundation M3 Title；app/shared 删除 `ciyin.material` 依赖与全部引用。
-

新增测试：核心 [NavigationContractTest](<../../core/design-system/src/commonTest/kotlin/ciyin/designsystem/NavigationContractTest.kt>)
（分组派发、能力拒绝、Drawer
声明后派发）、Nomi [DefaultNomiNavigationTest](<../../core/design-system-nomi/src/desktopTest/kotlin/ciyin/designsystem/nomi/navigation/DefaultNomiNavigationTest.kt>)
、M3 [Material3NavigationTest](<../../core/design-system-material3/src/commonTest/kotlin/ciyin/designsystem/material3/navigation/Material3NavigationTest.kt>)
、Fluent [FluentNavigationTest](<../../core/design-system-fluent/src/desktopTest/kotlin/ciyin/designsystem/fluent/navigation/FluentNavigationTest.kt>)。

- 验证（Windows / JDK 21）：五模块 `compileCommonMainKotlinMetadata`、`:app:shared` Android/Desktop 编译、
  `:app:sample:compileAndroidMain` 通过；Desktop 回归共 180 项——核心 42、Nomi 18、M3 30、Fluent 46、旧门面
  19、sample 25（`--tests "*DesignSystemDemo*"`），零失败、零跳过；KDoc 扫描 0 硬错误；`git diff --check`
  通过。
- 已知边界：Fluent 菜单为录制-重放桥接，选中刷新依赖调用方以组合可见状态驱动，且无项级 modifier；导航项无
  enabled，禁用态颜色只在原生提供状态位的实现上生效；Nomi Bar 不再固定高度，随原生导航默认；设备、iOS/Web
  与 Release 未验证。

### 前轮：Nomi 实现独立模块与移除隐式默认，2026-11-13

- 内置 Nomi 实现拆为独立 Gradle 模块
  `:core:design-system-nomi`（[工程声明](<../../settings.gradle.kts>)已 include），包名仍为
  `ciyin.designsystem.nomi`；当轮的顶层 Nomi 单例、`NomiNativeContentStyle` 与四组件 renderer 随之迁入。核心
  `api` 不引用该模块，也不依赖 M3/Fluent 适配器；本模块 `api(projects.core.designSystem)` 并保留 M3
  库预留。
-

核心移除隐式默认：[NomiDesignSystem.kt](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/runtime/NomiDesignSystem.kt>)
中 `LocalNomiDesignSystem` 改为 `staticCompositionLocalOf<NomiDesignSystem?> { null }`，新增 internal
`@Composable @ReadOnlyComposable requireNomiDesignSystem()`，未提供时以带指引的
`IllegalStateException` 失败。

- 两个主题入口的 `designSystem` 默认参数改为 `requireNomiDesignSystem()`，四组件读取同一函数；不存在无上下文时回退
  Nomi 的路径。
- 测试与 Preview 显式化：核心 `commonTest`
  新增 [NoImplicitDesignSystemContractTest](<../../core/design-system/src/commonTest/kotlin/ciyin/designsystem/NoImplicitDesignSystemContractTest.kt>)
  ，覆盖裸 `NomiTheme` 与四组件的缺省失败信息；Nomi 模块 `commonTest` 的
  `NomiNativeContentIsolationTest` 与 `desktopTest` 的 `NomiComponentsContractTest` /
  `NomiCustomizationContractTest`
  自核心迁入；[SamplePreviewTheme](<../../app/sample/src/commonMain/kotlin/com/ciyin/app/presentation/screen/designsystem/component/SamplePreviewTheme.kt>)
  在 sample commonMain 以 `CompositionLocalProvider(LocalNomiDesignSystem provides ...)` 显式提供当轮的
  Nomi 单例。
- Material 3 仅有测试边：[构建脚本](<../../core/design-system-material3/build.gradle.kts>)的
  `commonTest` 增加 `implementation(projects.core.designSystemNomi)`，生产 `commonMain` 无 Nomi 依赖；M3
  测试以显式 `designSystem` 传入当轮的 M3 单例。
-

旧门面契约保持：[旧 Theme](<../../core/material/src/commonMain/kotlin/ciyin/material/theme/Theme.kt>)
的 `AppTheme.typography` 以可空的当前设计系统 id 与当轮 M3 单例 id 比较来判定 M3
环境，未提供设计系统时不抛错，继续回退中立排版映射与旧默认值。

- 验证（Windows / JDK 21）：`:core:design-system`、`:core:design-system-nomi`、
  `:core:design-system-material3`、`:core:design-system-fluent`、`:core:material` 的
  `compileCommonMainKotlinMetadata` 与 sample `compileAndroidMain` 通过；Desktop 回归共 162 项——核心
  38、Nomi 13、M3 26、Fluent 41、旧门面 19、sample 25（`--tests "*DesignSystemDemo*"`），零失败、零跳过；KDoc
  扫描 63 文件、428 声明、0 硬错误（18 个 inferred-return 候选均为表达式体 `runComposeUiTest` 返回
  Unit）；`git diff --check` 通过。
- 未验证：设备、iOS/Web 与 Release 缺口保留；模块拆分本身不代表平台或发布验收完成。

### 前轮：删除主题聚合层，2026-11-12

- 删除独立主题聚合类型及默认工厂；已有 ResolvedNomiTheme 直接承载五组结果，核心以 request 规范化
  requestedMode/isDark/overrides，不将解析默认值反推为显式输入。
- Nomi/Material3/Fluent 在原生主题环境内回调扁平结果；组件只经 NomiTheme 读取。旧 AppTheme 对外 API
  不变，legacy 工厂改用颜色、原生 Typography、形状、间距、尺寸五个直接参数。
- Windows / JDK 21：核心、M3、Fluent 的 `compileCommonMainKotlinMetadata` 与 sample 的
  `compileAndroidMain` 通过。
- Desktop 回归重新执行 154 项：核心 45、M3 25、Fluent 40、旧门面 19、sample 25；零失败、零跳过，sample 限定
  `--tests "*DesignSystemDemo*"`。
- 新增回归覆盖浅色兜底/getter 身份、核心规范化错误 adapter metadata、M3/Fluent 直接回调来源、真实跨模块
  SPI 消费及父完整色板与子透明 accent 合并。原独立历史 golden、原生默认/slot/能力限制与旧门面测试继续通过。
- 集成中修复 M3 可空色板分支及跨层 accent 最终覆盖顺序；测试夹具修复组合内读取时机与重组跨时刻引用比较，保留严格身份断言。
- 未新增模块或兼容包装，构造与单组默认工厂不变；未运行设备、iOS/Web 或发布构建，既有缺口保留。

### 前轮：公开主题构造与生成成员 copy，2026-11-12

- 已批准范围：9 个主题 data class 公开构造，移除 `ConsistentCopyVisibility` 与 9 个手写核心复制扩展、8
  个旧门面复制转发；默认工厂及既有旧门面兼容保留。
- 默认数值、默认/必填参数、工厂新实例或共享实例身份，以及成员浅复制的未改字段引用均保持；原生默认、主题继承、SPI、gallery
  布局不变。
- Windows / JDK 21：核心、M3、Fluent 的 `compileCommonMainKotlinMetadata` 与 sample 的
  `compileAndroidMain` 通过。
- Desktop 回归共 148 项重新执行：核心 43、M3 23、Fluent 38、旧门面 19、sample 25，零失败、零跳过；sample 限定
  `--tests "*DesignSystemDemo*"`。
- 新增 19 项测试：Fluent 消费模块新增 11 项，覆盖 9 类直接构造/成员 copy、显式颜色哨兵及默认工厂身份；旧门面新增
  8 项类型别名直接构造/成员 copy 验证，均不导入复制扩展。
- 对照执行前快照，9 类参数列表与默认表达式逐字不变；24 个其余改动源码文件的非注释 token
  与批准的构造/扩展/import 转换一致，没有额外实现改动。
- 相关主题与消费测试的 KDoc 扫描 27 文件、206 声明，零错误与待核候选；工作区 `git diff --check` 通过。
- 下列所有前轮编译、测试数量和视觉资产仅作历史记录，不证明本轮通过；设备、iOS/Web 与 Release 缺口继续保留。

### 前轮：Nomi 内置实现独立包，2026-11-12

- Nomi 四组件实现及原生内容环境从 component 子包提至 `ciyin.designsystem.nomi`，当轮顶层 Nomi 单例也从
  runtime 迁入。
- 公共组件入口/SPI、主题 API/数据、运行时请求/契约/显式 slot 保持原位；默认 LocalNomiDesignSystem
  仍引用内置实现回退，不新增模块或兼容转发。
- 6 个文件移动，17 个 Kotlin 文件仅修改 package/import；逐文件对照迁移前快照，KDoc、实现正文与测试断言未变。
- Windows / JDK 21：核心 `compileCommonMainKotlinMetadata`、sample `compileAndroidMain` 通过。
- Desktop 测试重新执行：核心 43、M3 23、Fluent 27、旧门面 11、sample 25，共 129 项；零失败、零跳过。sample 使用
  `--tests "*DesignSystemDemo*"`。
- 迁移后 KDoc 扫描无格式错误；Surface.Card 的一个推断返回候选仍为 Unit，无须新增返回值说明。工作区
  `git diff --check` 通过，旧包路径/import 无残留。
- 未运行设备、发布构建或重拍截图；既有平台与 Release 缺口保持，不将该轮编译视为设备验收。

### 前轮：组件目录与 package 组织，2026-11-11

公共组件及 Nomi/M3/Fluent 实现按四组件子目录组织，SPI 与专属类型随文件迁移；共享类型、主题与装配位置不变。
三族 SPI、原生默认行为和 gallery 布局保持既有契约，不保留旧 package 转发。文档链接、import 示例和目录约定同步更新。

- Windows / JDK 21：核心、M3、Fluent 的 `compileCommonMainKotlinMetadata` 与 sample 的
  `compileAndroidMain` 通过。
- 该轮实际重跑 Desktop 测试：核心 43、M3 23、Fluent 27、旧门面 11、sample 25，共 129 项；XML
  报告均为当次运行生成，零失败、零跳过。sample 使用 `--tests "*DesignSystemDemo*"` 限定范围。
- 17 个文件按计划移动；对照该轮修改前的 61 个 Kotlin 文件快照，51 个文件仅有 package/import
  差异，组件实现和测试断言不变。
- 六个文档的 174 个本地文件链接存在，旧组件路径/import 扫描无残留；工作区 `git diff --check`
  通过。暂存区既有三处文件末尾空行告警未改动，也未更新索引。
- 该轮未新增设备运行、截图或发布验收；既有平台与 Release 缺口保留。

### 前轮：Nomi 公共 API 命名迁移，2026-11-11

新设计系统 App* 全部迁为 Nomi*，NomiDesignSystem 接口与当轮顶层默认实现分开； 默认 renderer 与原生内容环境另用
DefaultNomi* / LocalNomiNative*，未合并显式 slot 与原生默认值。 包名、设计系统 id、资源及导航标识不变；旧
material 公共 App* 门面和应用层名称不变。 该轮逐文件对照修改前快照，117 个候选源码仅发生批准的符号/import
迁移及文件移动，无额外实现逻辑变化。

- Desktop 回归：核心 43、M3 23、Fluent 27、旧门面 11、sample 25，共 129 项，无失败、无跳过。
- 核心与两适配器 metadata、sample compileAndroidMain、Desktop 宿主和 Android 仪器源码编译通过。
- 页面静态检查通过；该轮没有设备运行或截图刷新，前轮视觉资产仅作历史。
- 新核心与适配器不保留 App* 别名；外部消费者需更新 imports，源码兼容例外仅限旧 ciyin.material 门面。

### 已替代的历史方案：internal 构造与默认工厂复制，2026-11-11

**该可见性与扩展复制策略已被[前轮公开构造与生成成员 copy](<#前轮公开主题构造与生成成员-copy2026-11-12>)
替代，以下仅为历史证据，不是当前使用限制或当轮通过记录。**
当时主题数据类保持 internal constructor，外部需从默认工厂取对象并导入复制扩展；旧包提供薄工厂及同名复制转发。
公开构造改造时保留默认工厂、删除两层复制扩展并开放构造；上一轮再移除聚合默认工厂，单组默认值和主题行为不变。
核心 43、M3 23、Fluent 27、旧门面 11 项 Desktop 测试通过，合计 104 项，无失败或跳过。
包含[真正独立消费模块测试](<../../core/design-system-fluent/src/desktopTest/kotlin/consumer/ThemeFactoryCopyConsumptionTest.kt>)，
不借助核心 friend-path；双模块编译实验另确认直接内部构造、未导入扩展的内部成员 copy 均不可访问。
核心/两适配器 metadata、Android 依赖及 sample 编译、Desktop 宿主编译当时通过；该轮未运行设备或发布构建。

### 前轮：旧式 Theme 调用与数据组织恢复，2026-11-11

该轮只恢复源代码调用习惯与主题数据职责，原生组件默认和 gallery 布局保持不变。 基线为上述 HEAD +
当时未提交工作树；该轮文档整理未运行 Gradle，构建结果由 Lead 提供。

| 检查                                                    | 该轮记录                                                                                                |
|---------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| Color/ColorScheme/Type/Shape/Spacing/Size 及 Theme 签名 | 已静态核对落盘源码；无 Material 类型进入核心                                                            |
| 联合 Desktop 测试                                       | Lead 已实际执行并核对 XML：核心 42、M3 23、Fluent 24、旧门面 11、sample 25，合计 125 项，无失败、无跳过 |
| 核心历史数值与主题继承新增测试                          | 已纳入该轮核心 42 项，独立 golden 与 root/Override 契约复验通过                                         |
| 该轮平台编译                                            | 核心/M3/Fluent metadata、sample `compileAndroidMain`（包含所需依赖编译）、Desktop 宿主编译均通过        |
| 该轮 APK、设备与截图                                    | 该轮没有 APK 构建、真机/WSA 运行或重新截图验收声明；更早构建/原生对照截图不是该轮执行记录               |
| iOS/Web/Release                                         | 原有未验收与依赖兼容风险保留                                                                            |

该轮回归覆盖：旧 27 色、十五排版与形状/间距/尺寸独立基线；同名工厂具名参数及 default* 薄转发；
根不继承父颜色/accent、只继承非颜色明确组，Override 继承全部明确组；子完整色板清 inherited accent、 子
accent 叠 parent palette；direct/overrides 重复及同层完整色板+accent 拒绝；Fluent typography 映射和
full color/shapes 拒绝； M3 完整中立读模型与旧 Typography 身份保持。

### 前轮：原生默认优先重构，2026-11-11

以下为直接主题参数复刻之前，由 Lead 实际运行并核对 XML 报告的历史记录，基线为上述 HEAD + 当时未提交工作树；总计
99 项 Desktop 测试通过，无失败或跳过。
**它们不证明后续五组直接参数/工厂恢复或上一轮主题结果扁平化通过。**

| 检查                                                 | 该轮记录                                                                                                       |
|------------------------------------------------------|----------------------------------------------------------------------------------------------------------------|
| 核心 / M3 / Fluent metadata、Android 与 Desktop 编译 | 已通过；Desktop 宿主编译及 Android Debug/仪器 APK 构建通过                                                     |
| 核心契约测试                                         | 29 项通过（`:core:design-system:desktopTest`）                                                                 |
| M3 生产编译                                          | 已通过                                                                                                         |
| M3 测试                                              | 19 项通过；独立 MaterialTheme 原生对照、显式覆盖、slot 来源隔离与严格像素/布局断言通过                         |
| 旧门面测试                                           | 9 项复跑通过                                                                                                   |
| Fluent / sample                                      | Fluent 20 项 Desktop 测试通过；sample 22 项设计系统 Desktop 测试通过，生产组件已按组件拆分                     |
| 真正原生默认对照与截图                               | 三系统 Light/Dark 原生默认与显式区截图已生成；人工检查后的代表性原生风格和大字截图保存在下方链接               |
| Android 设备、iOS native、JS/Wasm、Release           | Android Debug 与 androidTest APK 已构建，未在设备运行；iOS/Web 未验收；Desktop Release 既有缺失 API 风险仍保留 |

该轮验收与回归覆盖：

- 零参数 API 请求仍为空视觉字段，真实原生组件与 adapter 在同配置下的样式/尺寸/状态一致。
- 单字段颜色覆盖不会顺带改其它原生状态；null、Unspecified、Transparent 与 Border 三态行为不同。
- Fluent 不支持的 Button/Card 覆盖快速失败；不能继续用重绘实现绕开失败。
- 跨系统局部覆盖只继承显式增量，原生默认投影不泄漏，显式 slot 和退出恢复正确。
- 旧包命名参数、默认值、Typography 身份和图标转发继续兼容。
- sample 原生默认/显式覆盖/限制提示与真实能力一致，大字体与明暗不裁剪内容。

该轮已读取的代表性截图（上一轮主题结果扁平化未据此重新验收）：

- [Nomi 原生浅色](<assets/design-system/native-default/native-nomi-Light.png>)、[Material 3 原生浅色](<assets/design-system/native-default/native-material3-Light.png>)。
- [Fluent 原生浅色](<assets/design-system/native-default/native-fluent-Light.png>)、[Fluent 原生深色](<assets/design-system/native-default/native-fluent-Dark.png>)。
- [窄屏两倍字号与长文本](<assets/design-system/native-default/compact-large-font-button.png>)。

原生对照使用独立第三方 Theme 与组件，不用 NomiTheme 构造参照。Fluent 透明像素比较预乘 RGB 与 alpha，
只容许两次 8-bit 存储量化误差；布局尺寸和文本样式、密度数值、约束、行数均严格比较。 评审发现的原生 slot
误入显式上下文、typed Fluent 深色板被 accent 请求切成浅色均已修复并回归。

### 前轮补充：展示排版优化，2026-11-11

只调整 sample 页面：收紧控制栏、标签网格、横向按钮变体、可展开限制、排版角色单项预览。 25 项 gallery
Desktop 测试、sample Android 编译与 Desktop 宿主编译通过；原生组件实现未修改。
已读取[桌面完整布局](<assets/design-system/layout-refinement/layout-fluent-desktop.png>)、
[Fluent 浅色](<assets/design-system/layout-refinement/native-fluent-Light.png>)、
[Fluent 深色](<assets/design-system/layout-refinement/native-fluent-Dark.png>)与
[窄屏大字体](<assets/design-system/layout-refinement/compact-large-font-button.png>)。

### 前轮：恢复跨平台时的结果（不是当前行为验收）

前轮将 Fluent 移入 commonMain 后，adapter metadata、Android/Desktop 编译与 **17 项 Desktop 测试**通过；
sample metadata、 **18 项 Desktop 测试**、Desktop 宿主编译、Android Debug 应用与仪器 APK 构建通过。 新增
Android Fluent 暗色/大字体及三系统禁用用例当时仅编译，没有执行设备安装/运行。
这些记录证明的是旧统一视觉实现的跨平台接入，不能证明后续原生按钮/Card、稀疏请求、新能力边界或上一轮主题结果扁平化通过。

### 更早阶段：Desktop-only 实现的历史证据

此前有 **61 项 Desktop 测试**（核心 12、M3 6、Fluent 17、旧门面 9、sample 17）通过； WSA Android 33 上真实
MainActivity → sample → gallery **2 项仪器测试**通过，覆盖当时 Nomi/M3， 不证明 Android Fluent 已运行。旧
Layer-based Fluent 的 hover、按下、点击边界像素检查仅为旧实现证据。

历史截图（此前已读取，本轮未据此验收）：

- [Desktop Fluent 浅色](<assets/design-system/desktop-fluent-light.png>) / [深色及计数](<assets/design-system/desktop-fluent-dark.png>)。
- [窄屏两倍字号](<assets/design-system/compact-large-font-button.png>) / [局部覆盖](<assets/design-system/compact-large-font-override.png>)。
- [M3 暗色布局](<assets/design-system/wide-material3-dark.png>)。

历史设备边界保持：K20 Pro USB 不在列表、网络超时；冲突 USB 与小米 15 未获使用许可，真机未验收。 WSA
曾在启动已安装宿主后通过身份核验并安装/运行；随后 transport 消失，一次自动恢复仍拒连后停止，
未反复重启或切换设备。不能把 WSA 视为真机，也不能因当前源码变更自动重用旧设备结论。

历史 Release 风险仍未关闭：Desktop `createReleaseDistributable` 在 `proguardReleaseJars` 因
Fluent/haze/FileKit 引用当前 Compose/Skia 缺失方法而失败，未使用 ignorewarnings；源码改回原生组件不等于依赖兼容问题已修复。
iOS 需 macOS native/link/runtime，JS/Wasm 沿项目开关，Android Release 独立验收。

### 验证入口

下列是按改动选择的入口，不是新的执行记录；实际任务受当前目标与配置影响。

```powershell
.\gradlew.bat :core:design-system:compileCommonMainKotlinMetadata :core:design-system:desktopTest --console=plain
.\gradlew.bat :core:design-system-nomi:compileCommonMainKotlinMetadata :core:design-system-nomi:desktopTest --console=plain
.\gradlew.bat :core:design-system-material3:desktopTest :core:material:desktopTest --console=plain
.\gradlew.bat :core:design-system-fluent:compileCommonMainKotlinMetadata :core:design-system-fluent:compileAndroidMain :core:design-system-fluent:desktopTest --console=plain
.\gradlew.bat :app:shared:compileCommonMainKotlinMetadata :app:shared:compileAndroidMain :app:desktop:compileKotlin --console=plain
.\gradlew.bat :app:sample:desktopTest --tests "*DesignSystemDemo*" --console=plain
.\gradlew.bat :app:sample:compileCommonMainKotlinMetadata :app:android:assembleDebug --console=plain
.\gradlew.bat :app:sample:compileCommonMainKotlinMetadata :app:desktop:compileKotlin :app:android:assembleDebug --console=plain
```

平台/签名条件见[构建指南](<../contributing/building.md>)，runner
与执行记录要求见[测试指南](<../contributing/testing.md>)。 截图必须读取并检查；不要把测试生成了图片、metadata
编译或 APK 构建当作视觉/设备/发布验收。

## 后续范围

IconButton、输入框、Checkbox/Switch/Radio/Slider、Dialog/Scaffold/TopBar、Tooltip/Menu/Progress、
Tabs/ListItem/Badge/InfoBar/Expander/DatePicker/ColorPicker/CommandBar 等仍未纳入当前四组件 +
导航套件范围。 导航已作为第四组件族落地；弹出层导航（ModalNavigationDrawer 的开关控制）与 Fluent
折叠态无障碍细化留给后续迭代。 更多截图基线、Fluent 扩展图标、系统材质、全应用设计系统持久化设置、UI
foundation 拆分及独立发布/ABI 策略另行演进。 本轮不凭“未来可能有用”增加空组件族，也不把用户已确认的原生优先改回统一外观。
