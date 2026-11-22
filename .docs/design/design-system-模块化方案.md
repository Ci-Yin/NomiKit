# Design System 模块化方案

> 状态：已实现（编译与桌面回归已核；保留平台与发布验证缺口）；设计系统实现改由 `NomiDesignSystem`
> 伴生对象上的扩展声明（属性/工厂）暴露，旧顶层单例名称已删除；新增列表项第六族，旧 `SettingItem`
> 已删除；Nomi 导航
> 新增永久抽屉支持；导航套件新增外壳容器色覆盖；移除能力声明与核心能力拦截，不支持的覆盖改由适配器降级并记录日志；
> 主题 API 改为 `NomiTheme` 值 + 适配器映射函数：单入口接收 `designSystem`、`darkMode` 与可选 `theme`
> ，Material3 /
> Fluent 工厂接收
> `(NomiTheme) -> 原生主题值`；`NomiThemeOverrides`、`NomiThemeRequest`、`ResolvedNomiTheme` 与
> Override 入口全部删除。
> 范围：`NomiDesignSystem` 新增空 `companion object` 作为绑定命名空间；三个实现对象降为 internal（
> `DefaultNomiSystem`/`Material3System`/`FluentSystem`）；对外为
> `NomiDesignSystem.Default` 属性与 `NomiDesignSystem.Material3/Fluent` 工厂；29 个文件 128 处引用迁移，6
> 个 README、设计索引与
> 2 个 skill 同步；列表项族新增 `NomiListItem` 与 `listItems` renderer，三端原生映射落地，
> `:core:ui-foundation` 旧 `SettingItem` 删除；Nomi 导航解除 Drawer 能力禁用，新增 M3 永久抽屉 + Nomi
> 令牌抽屉项；
> 导航入口新增 `containerColor` 外壳容器色覆盖；删除 `NomiComponentCapabilities`，核心入口不再按能力拦截，
> Fluent 对无法表达的覆盖降级为原生默认并通过 `ciyin.platform.logger` 记录警告。
> 相关模块：`:core:design-system`、`:core:design-system-nomi`、`:core:design-system-material3`、
> `:core:design-system-fluent`、`:core:ui-foundation`、`:core:material`、`:app:shared`、`:app:sample`。
> 最近核对：2026-11-19；master 未提交工作树（新增图标/分割线/控件/反馈四族、组件级 spec 契约与 IconPack
> 24dp）。
> 验证记录：六模块 Desktop 回归 273 项（核心 56、M3 42、Fluent 60、Nomi 29、material 19、sample 67）、
> 四模块 metadata 与 `:app:shared`、`:app:sample` 编译通过；上一轮 226 项、四模块与 sample 165 项、
> 命名空间轮 218 项、导航轮 180 项见下方历史条目。
> 2026-11-18 增补「后续组件缺口清单」：只做现状盘点与优先级记录；2026-11-19 落地其中的图标、分割线、
> 控件与提示浮层四族，盘点计数口径为 `import androidx.compose.material3.<符号>` 的源码文件数。

本文沿原路径演进，不新增分支、版本号或归档副本。原设计有 Git 历史可追溯； 此前确立原生默认优先、单组默认工厂、Nomi
品牌命名、独立 nomi 包及公开构造/生成成员 copy；上一轮删除主题聚合包装并将五组字段扁平化；本轮把主题输入收敛为
`NomiTheme` 值，原生映射交给
`NomiDesignSystem.Material3` / `NomiDesignSystem.Fluent` 属性与 `Material3(map)` / `Fluent(map)`
函数，不再保留 request/overrides/Override 入口。 源码存在、测试通过、视觉和平台发布是不同结论。

## 背景与目标

早期主题封装直接暴露 Material Typography；第一次模块化将中立 tokens 与四组件拆出， 但把统一 token、按钮
metrics 和 Card 表面组合预先施加给三个系统，Fluent 按钮还以 Layer + clickable 重绘。 这些做法隔离了依赖，却不能保留
Material / Fluent 的原生默认视觉。

已确认原则仍是： **调用 API 统一，默认外观和交互由各设计系统自身决定；只应用调用方明确指定的覆盖。**
此前已在此前提下恢复旧 Theme 的文件职责、工厂名称和直接参数调用，并整理内置实现包职责。
本轮按授权删除主题聚合类型、请求模型与覆盖入口；`NomiTheme` 直接持有五组结果，适配器通过映射函数建立
原生主题，不新增替代包装、CompositionLocal 或兼容入口。

- 零视觉参数调用应对应当前库的真实原生组件，而不是把 Nomi 默认颜色、字体、形状、padding 投给所有
  adapter。
- 未指定值与显式值必须区分；只覆盖一个字段时，其余字段保留原生状态。
- 原生 API 无法表达的覆盖声明为不支持并快速失败，不静默忽略、不重绘组件伪造支持。
- 主题输入与只读投影分开；局部换系统不携带父系统已经解析的默认样式。
- API、主题、每个组件实现分文件维护，聚合对象仅组装。

非目标：不复刻全部第三方组件，不统一所有系统尺寸；不把窗口 Mica/Acrylic、导航、业务状态或持久化设置放入本模块；
不一次性重写旧页面；不自动打开项目未启用的平台。首期只有四组件，后续按轮次扩展；当前十一族（文本、操作、表面、导航、
页面外壳、列表项、图标、分割线、控件、反馈、菜单），剩余组件与优先级见「后续组件缺口清单」。

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
| [核心](<../../core/design-system/README.md>)                                                                   | 请求、只读投影、四组件 + 导航套件 API、四族 SPI、图标；无隐式默认实现                                            |
| [Nomi 实现](<../../core/design-system-nomi/README.md>)                                                         | internal DefaultNomiSystem（经 `NomiDesignSystem.Default` 暴露）、原生内容环境、四组件 Foundation 实现与导航外壳 |
| [M3 adapter](<../../core/design-system-material3/README.md>)                                                   | 原生 M3 默认、原生配置、投影、组件与原生导航；特殊 legacy 配置服务旧门面                                         |
| [Fluent adapter](<../../core/design-system-fluent/README.md>)                                                  | commonMain 原生 Fluent 主题、组件与 NavigationView 导航，不再重绘按钮                                            |
| [旧门面](<../../core/material/README.md>)                                                                      | 旧包参数/类型/默认行为的源码兼容，保留旧 UI foundation 传递依赖                                                  |
| [sample](<../../app/sample/README.md>)                                                                         | 原生默认、显式覆盖、适配器降级展示，默认 Nomi/M3/Fluent                                                          |
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

## 主题：值 + 映射函数

| 实际入口                                                                                                            | 职责                                                                        |
|---------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------|
| [NomiTheme](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/theme/NomiTheme.kt>)                 | 不可变主题值、`LocalNomiTheme`、伴生只读 getter 与唯一入口                  |
| [NomiDesignSystem](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/runtime/NomiDesignSystem.kt>) | `ProvideTheme(theme, content)` 映射契约、`LocalNomiDesignSystem` 与 require |
| [ContentStyle](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/runtime/ContentStyle.kt>)         | 显式 slot 颜色与文本样式增量                                                |

```text
NomiTheme(designSystem, darkMode, theme, content)
  -> theme 省略时：resolveIsDark() -> 选择浅色/深色默认 NomiTheme
  -> theme 显式传入时：原样使用，忽略 darkMode
  -> 入口发布 LocalNomiTheme 与 LocalNomiDesignSystem
  -> designSystem.ProvideTheme(theme, content)

  -> adapter 调用映射函数得到原生组并安装原生主题
  -> 原生组件直接读取原生主题；NomiTheme.* 读取入口输入值
```

- `NomiTheme` 五组均为完整值，不做字段级合并；`isDark` 是已解析布尔，`DarkMode` 只作为应用层请求枚举。
- 每次进入 `NomiTheme` 边界都重置显式内容 slot；`designSystem` 必填，`darkMode` 只在 `theme`
  省略时选择默认主题， 嵌套继承需显式传 `theme = LocalNomiTheme.current`。
- 适配器函数接收 `(NomiTheme) -> 原生主题值` 映射函数；无参访问用同名扩展属性，返回原生默认映射的共享实例，不把中立值自动写入原生主题。
- `LocalNomiDesignSystem` 可空且 **无隐式默认**：未显式提供时 `requireNomiDesignSystem()` 抛出带指引的
  `IllegalStateException`。
- 不做 native 到中立的反向投影；伴生分组 getter 读取本层显式输入值，原生视觉由原生主题负责。
- `NomiThemeRequest`、`ResolvedNomiTheme`、`NomiThemeOverrides`、`NomiThemeOverride` 与任何聚合包装都不属于当前
  API。

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

直接调用示例（显式使用默认值，非全系统默认注入）：

```kotlin
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.defaultLightNomiColorScheme

/** 通过 NomiTheme 值显式配置 Nomi 色板，其他组继承当前主题。 */
@Composable
fun ExplicitPaletteTheme(content: @Composable () -> Unit) {
    NomiTheme(
        theme = NomiTheme.current.copy(
            colorScheme = defaultLightNomiColorScheme().copy(primary = Color(0xFF4466AA)),
        ),
        designSystem = NomiDesignSystem.Default,
        content = content,
    )
}
```

| 映射来源              | Nomi     | M3                                                       | Fluent                        |
|-----------------------|----------|----------------------------------------------------------|-------------------------------|
| NomiTheme.colorScheme | 原样使用 | 可由预置映射函数整组映射或只映射角色，未映射角色保留原生 | 只能把 primary 映射为强调色阶 |
| NomiTheme.typography  | 原样使用 | 十五角色映射                                             | 映射八个原生角色              |
| NomiTheme.shapes      | 原样使用 | 五个标准角色映射                                         | 映射三个原生圆角值            |
| spacings / sizes      | 原样使用 | 只发布到中立读模型，不写原生                             | 只发布到中立读模型，不写原生  |

本表是主题输入边界，和下方 Button/Card 的组件级降级独立；主题 shapes 被 Fluent 忽略不等于所有 Surface
shape 都不支持。

## 组件 API：不补齐未指定的视觉参数

| 入口                                                                                                                | 默认与覆盖契约                                                                                                                                                               |
|---------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [NomiText](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/text/NomiText.kt>)          | String/AnnotatedString 双重载；style? 保留原生文本环境；逐字段覆盖、onTextLayout、autoSize、inlineContent 透传；颜色 Unspecified 不预填正文色；Fluent 忽略 autoSize 并记警告 |
| [NomiButton](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/button/NomiButton.kt>)    | variant/size/shape/contentPadding/textStyle 可空；颜色逐字段 Unspecified；原生组件处理默认视觉与交互                                                                         |
| [NomiSurface](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/surface/NomiSurface.kt>) | shape?、稀疏颜色和 NomiBorder，派发原生表面                                                                                                                                  |
| [NomiCard](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/card/NomiCard.kt>)          | 真正非点击 Card，表面族独立 Card 方法；contentPadding? 仅显式覆盖 slot                                                                                                       |
| [NomiBorder](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/NomiBorder.kt>)           | Default 保留原生描边；None 明确无描边；Stroke 明确指定                                                                                                                       |

Color.Transparent 是有效覆盖，不能当作 Unspecified。描边只接受 NomiBorder 三态：Default 保留原生描边，None
明确移除； 不保留本会话生成的 nullable BorderStroke 兼容重载。`NomiButtonDefaults` /
`NomiSurfaceDefaults` 仍可作为调用方主动选择的预设，但组件零参路径不自动调用。 统一 metrics 不再是全部
adapter 的强制默认，Card 不再是核心统一 `Surface + padding + Column` 配方。

文本颜色顺序保持显式 color > 本次 style.color > 显式 slot 色 > 继承样式色；无值留给原生 renderer。 无显式
style 时保持 null，不在核心合并统一 bodyMedium；不要将原生主题默认反向投影为“显式 slot”， 压过原生按钮的
disabled/hover/pressed 内容样式。

### 覆盖的降级规则

核心不提供能力声明类型，入口也不按能力拦截参数；每种视觉覆盖按“能否用原生入口表达”处理：

| 参数                                       | Nomi             | Material 3           | Fluent v0.1.0                        |
|--------------------------------------------|------------------|----------------------|--------------------------------------|
| 零视觉参数调用                             | Nomi 自身默认    | M3 原生默认          | Fluent 原生默认                      |
| Button variant、稀疏颜色、textStyle        | 支持             | 支持                 | 按原生按钮入口/颜色方案映射          |
| Button 非 null size、shape、contentPadding | 支持             | 支持公开的组件级覆盖 | 忽略并记警告，保留原生按钮           |
| Surface 显式 shape、颜色、border           | 支持             | 支持                 | 使用原生 Layer                       |
| 静态 Card shape / slot contentPadding      | 支持             | 支持                 | 支持                                 |
| 静态 Card 指定颜色或非 Default border      | 支持             | 支持                 | 忽略并记警告，需要时改用 NomiSurface |
| 导航 Bar / Rail                            | 支持             | 支持                 | 支持                                 |
| 导航 Drawer                                | 支持（永久抽屉） | 支持（永久抽屉）     | 支持（Left）                         |
| 导航容器色覆盖                             | 支持             | 支持                 | 忽略并记警告（原生无容器色入口）     |
| 导航底部项分组                             | 支持             | 支持                 | 支持（原生 footerItems）             |
| 导航项级 modifier                          | 支持             | 支持                 | 忽略并记警告（原生菜单无项级修饰符） |

降级统一通过 `ciyin.platform.logger` 记录一次警告（同值不重复），不重绘伪造外观；因此运行时切换设计系统
不会因能力差异抛异常。

M3 “支持 shape”指组件级任意 Shape。主题级 `Shapes` 的标准角色受现有中立 `NomiShapes` 读模型限制， 不是
RoundedCornerShape 时明确拒绝；不能写成任意主题形状都能无损投影。

## 导航套件

导航是第四个中性组件族；入口、作用域与 SPI 在同一文件，布局与容器色原样交给实现，无法承载时由适配器降级并记录警告。

| 实际入口                                                                                                                                  | 职责                                                                                                                                                                                                                                                                          |
|-------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [NomiNavigationSuite](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/navigation/NomiNavigationSuite.kt>)    | `NomiNavigationSuite(layoutType, modifier, containerColor, items, footerItems, content)`；`NomiNavigationSuiteType` 为 Bar/Rail/Drawer；`NomiNavigationItemScope.NavigationItem(selected, onClick, modifier, icon, badge, label, colors, interactionSource)` 以槽位声明导航项 |
| [NomiDesignSystemComponents](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/NomiDesignSystemComponents.kt>) | 新增 `navigation: NomiNavigationRenderer`；与 text/actions/surfaces 并列                                                                                                                                                                                                      |

调用契约：

- 选中状态、点击回调与路由由调用方持有；导航项的图标、徽标与文案都是调用方组合的槽位，核心不限定类型，也不替调用方设置尺寸或无障碍名称。
- `items` 与 `footerItems` 两个分组按声明顺序渲染组内项目；底部组是否参与当前布局、是否固定到底部由原生实现
  决定（Nomi 的 Bar 只渲染主导航项，底部组只在 Rail/Drawer 展示）。
- 布局类型由调用方按窗口形态选择；核心与适配器都不读取窗口或平台状态。
- 导航外壳容器色通过 `containerColor: Color = Color.Unspecified` 稀疏覆盖；Unspecified 保留当前设计系统的
  原生容器色，`Color.Transparent` 是有效覆盖。无法覆盖的实现忽略该值、保留原生容器色并记录警告，核心不拦截。
- 导航项颜色只通过 `NomiNavigationItemColors` 的
  contentColor/selectedContentColor/disabledContentColor/indicatorColor 稀疏覆盖；
  未指定字段保留原生导航项颜色。槽内内容应读取原生内容环境，使悬停/按下/禁用状态色继续生效。
- 中立 IconPack 的 ImageVector 固有尺寸为 256dp；槽位交给调用方组合，因此
  **图标尺寸由调用方在槽内显式约束**
  （app/shared 使用 `Modifier.size(NomiTheme.sizes.icon.large)`）；适配器不读取也不改写槽内容的尺寸。
- 项级 `modifier` 与 `interactionSource` 由支持原生项入口的实现消费；Fluent 原生菜单无项级
  modifier，因此忽略该项 modifier 并记录警告。
- 未提供设计系统时入口抛出带指引的 `IllegalStateException`；布局与容器色不再由核心抛错，由适配器降级处理。

三种适配：

| 布局   | Nomi                                                        | Material 3                                            | Fluent v0.1.0                                   |
|--------|-------------------------------------------------------------|-------------------------------------------------------|-------------------------------------------------|
| Bar    | 自定义 M3 `NavigationBar` 外壳，复刻既有 Navigation.kt 视觉 | `NavigationBar` + `NavigationBarItem`                 | `NavigationView` 的 LeftCollapsed               |
| Rail   | 自定义 M3 `NavigationRail` 外壳（栏宽 60dp）                | `NavigationRail` + `NavigationRailItem`               | `NavigationView` 的 LeftCompact                 |
| Drawer | `PermanentNavigationDrawer` + Nomi 令牌抽屉项               | `PermanentNavigationDrawer` + `NavigationDrawerItem`  | `NavigationView` 的 Left                        |
| 底部组 | Rail 用弹性间隔推底；Bar 只渲染主导航项，忽略底部组         | Rail 用 weight spacer，Bar 同排，Drawer 抽屉内 spacer | 原生 `footerItems`                              |
| 容器色 | 覆盖 Bar/Rail/Drawer 容器，Unspecified 用 surfaceLow        | 覆盖 Bar/Rail，Drawer 未覆盖时保留原生默认            | 忽略并记警告，`NavigationView` 无导航容器色入口 |

Fluent 没有底部导航栏，三种布局都映射到左置导航：Bar 折叠侧边、Rail 紧凑侧边、Drawer 完整侧边； 三者都交给原生
`NavigationView`，只关闭内容面板描边。Fluent 的 `NavigationView.color` 作用于内容面板，
导航底色来自原生窗口材质，因此显式 `containerColor` 被忽略并记录警告，不把 `containerColor` 冒充成内容面板色。

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
新增 `listItems` 族；本版不新增显式覆盖参数。

### 三端原生映射与决策

| 映射      | Material 3                     | Fluent                              | Nomi                                 |
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

## 图标、分割线与控件族

补上"任何页面都会用到、但中立层此前缺失"的原子能力：图标、分割线、选择控件与提示浮层四个组件族，
以及作为文本族新成员的文本输入框。它们沿用同一契约：入口只接收调用方明确给出的值，零参数调用落到当前
设计系统的真实原生组件；实现无法承载的覆盖降级并记录一次警告，不重绘伪造外观。

| 族           | 公共入口                                          | 契约要点                                                                                        |
|--------------|---------------------------------------------------|-------------------------------------------------------------------------------------------------|
| 图标         | `NomiIcon`                                        | 没有尺寸参数；`tint` 的 Unspecified 保留原生内容色，透明色是有效覆盖                            |
| 分割线       | `NomiDivider`                                     | `NomiDividerOrientation` 切方向；`thickness = null`、`color = Unspecified` 保留原生粗细与描边色 |
| 控件         | `NomiSwitch` / `NomiCheckbox` / `NomiRadioButton` | 状态由调用方持有；回调非空，禁用只由 `enabled` 表达                                             |
| 文本（扩展） | `NomiTextField`                                   | 与 `NomiText` 共用 `text` renderer；另有 `variant`、`isError` 与键盘配置                        |
| 反馈         | `NomiTooltip`                                     | 包裹式入口：`tooltip` 槽放进原生浮层，`content` 是触发内容；不暴露浮层状态、位置或富提示        |

三个选择控件共用 [控件族](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/control/NomiControlRenderer.kt>)
的
renderer；文本输入框归入 [文本族](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/text/NomiText.kt>)
的 renderer，使显示与编辑共用同一个实现替换点（自定义文本策略不会只覆盖显示）。 **专属参数仍按入口各自
独立**：`NomiSwitchSpec`、`NomiCheckboxSpec`、`NomiRadioButtonSpec`、`NomiTextFieldSpec` 互不兼容，
跨入口传入编译失败；这与 `surfaces` 族内 `Surface` / `Card` 分属两个 spec 族的既有做法一致。

### 三端映射与降级

| 入口              | Nomi                                | Material 3                                        | Fluent v0.1.0                                                                     |
|-------------------|-------------------------------------|---------------------------------------------------|-----------------------------------------------------------------------------------|
| `NomiIcon`        | `Icon` + Nomi 内容色令牌            | 原生 `Icon`，Unspecified 回落 `LocalContentColor` | 原生 `Icon`，回落内容色与内容透明度                                               |
| `NomiDivider`     | 原生水平/垂直分割线 + Nomi 线条令牌 | 原生 `HorizontalDivider` / `VerticalDivider`      | 仅横向：原生 `ListItemSeparator`；纵向、粗细、颜色忽略并告警                      |
| `NomiSwitch`      | 原生 `Switch` + Nomi 令牌配色       | 原生 `Switch`                                     | 原生 `Switcher`；上游无 modifier 参数，修饰符作用在包装节点                       |
| `NomiCheckbox`    | 原生 `Checkbox` + Nomi 令牌配色     | 原生 `Checkbox`                                   | 原生 `CheckBox`，label 为 null                                                    |
| `NomiRadioButton` | 原生 `RadioButton` + Nomi 令牌配色  | 原生 `RadioButton`                                | 原生 `RadioButton`，label 为 null                                                 |
| `NomiTextField`   | 填充/描边入口 + Nomi 令牌配色       | `Outlined` → `OutlinedTextField`，否则填充入口    | 原生 `TextField`：label→header、trailingIcon→trailing；variant/isError 忽略并告警 |
| `NomiTooltip`     | 原生 `TooltipBox` + Nomi 容器色     | 原生 `TooltipBox` + `PlainTooltip`                | 原生 `TooltipBox`（实验 API），触发与时长沿用原生默认                             |

- Nomi 不安装外层原生主题，因此控件、输入框与提示浮层的配色全部显式取自 `NomiTheme.colorScheme`，
  不读取宿主主题；这与导航外壳复用原生工具箱组件的既有做法一致。
- 内置 `IconPack` 的 8 个矢量固有尺寸统一为 24dp：调用方不再需要为了尺寸显式约束每个槽位，需要其它
  尺寸时仍用 `Modifier.size`。
- 本版不提供图标按钮、顶栏、徽标、页签、浮层对话框、列表分组容器；不提供 `NomiTextField`
  的密码遮罩、最大行数、辅助说明与前后缀；选择控件不提供只读态。上述能力列入「后续组件缺口清单」。

## 开关按钮、下拉按钮、组合框与菜单族

在操作族补第二、三个按钮入口，在控件族补组合框，并新增第十一族 `menus`。三个按钮入口共用同一条原生
绘制路径，只在状态语义、状态色与尾部指示上分叉；浮层位置、菜单项外观与宽度对齐全部交给实现。

| 族   | 公共入口                    | 契约要点                                                                                                               |
|------|-----------------------------|------------------------------------------------------------------------------------------------------------------------|
| 操作 | `NomiToggleButton`          | `checked` / `onCheckedChange` 两态由调用方持有；`colors` 是两组 `NomiButtonColors`；`iconOnly` 收敛为方形并忽略 `size` |
| 操作 | `NomiDropDownButton`        | 普通按钮 + 实现追加的下拉箭头；不持有展开状态，菜单由调用方用 `NomiMenu` 组合                                          |
| 控件 | `NomiComboBox`              | `expanded` / `onExpandedChange` 由调用方持有；`content` 收起态 + `menuItems` 菜单项；浮层与宽度对齐归实现              |
| 菜单 | `NomiMenu` / `NomiMenuItem` | `NomiMenu` 以父布局为锚点；`NomiMenuItem` 是行原语，可放进菜单浮层或组合框自带浮层                                     |

专属参数仍按入口独立：`NomiToggleButtonSpec`、`NomiDropDownButtonSpec`、`NomiComboBoxSpec`、
`NomiMenuSpec`、
`NomiMenuItemSpec` 互不兼容。

### 三端映射与降级

| 入口                 | Nomi                                         | Material 3                                                                           | Fluent v0.1.0                                    |
|----------------------|----------------------------------------------|--------------------------------------------------------------------------------------|--------------------------------------------------|
| `NomiToggleButton`   | Foundation 绘制 + 开关语义 + 令牌            | 文字形态 `FilledTonalButton`/`Button` 切态；图标形态 `IconButton`/`FilledIconButton` | 原生 `ToggleButton`（含 `iconOnly`）             |
| `NomiDropDownButton` | Foundation 绘制 + 内置 `ChevronDown`         | 原生 `Button` 族 + `ChevronDown` 图标                                                | 原生 `DropDownButton`（自带动画箭头）            |
| `NomiComboBox`       | 描边容器 + 菜单浮层，宽度对齐                | `OutlinedButton` + 原生 `DropdownMenu`，宽度对齐                                     | 原生 `DropDownButton` + `DropdownMenu`，宽度对齐 |
| `NomiMenu`           | `Popup` + 令牌容器（下方优先、空间不足翻转） | 原生 `DropdownMenu`                                                                  | 原生 `DropdownMenu`                              |
| `NomiMenuItem`       | Foundation 行 + 悬停/按下叠加 + 勾选指示     | 原生 `DropdownMenuItem`                                                              | 原生 `SubtleButton` 行                           |

- Material 3 1.9.0 没有原生 `ToggleButton` 与 `SplitButton`：开关按钮由原生填充按钮按状态切换，未重绘外观；
  `SplitButton` / `ToggleSplitButton` 因此不在本轮范围。
- `iconOnly = true` 时三端都忽略 `size`（原生图标按钮固定方形尺寸），实现记录一次警告。
- 菜单项 `selected` 三端统一用内置 `Check` 矢量表达；Fluent 的 `ComboBoxItem` 只接收 `String label`，
  无法承载槽内容，因此不复用。
- 菜单分隔线沿用通用 `NomiDivider`，本版不新增菜单专用分隔项。

## 滑块与进度族

控件族补连续取值入口，反馈族补进度入口；两者都沿用同一契约：入口只接收显式值与形态，轨道、拇指、
刻度、配色与动画交给实现。

| 族   | 公共入口                | 契约要点                                                                                                                                                                                                                             |
|------|-------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 控件 | `NomiSlider`            | `value`/`onValueChange` 由调用方持有；`valueRange`、`steps`（负值按 0）、`onValueChangeFinished`；默认外观对齐应用滑块（16dp 胶囊轨道、15dp 描边拇指、轨道两端 coerce、透明刻度、禁用 38%）；不暴露颜色、尺寸、拇指/轨道槽与竖直方向 |
| 反馈 | `NomiProgressIndicator` | `progress = null` 表示不确定；`NomiProgressIndicatorType` 切线性/环形；线性为 16dp 胶囊轨道，配色与滑块轨道一致（进度 `primary`、轨道 `surfaceHigher`）；环形 32dp / 3dp 仅统一配色；不暴露颜色、尺寸与轨道槽                        |

专属参数仍按入口独立：`NomiSliderSpec` 与 `NomiProgressIndicatorSpec` 互不兼容。

### 三端映射与来源

| 入口         | Nomi                                                      | Material 3                       | Fluent v0.1.0                                |
|--------------|-----------------------------------------------------------|----------------------------------|----------------------------------------------|
| `NomiSlider` | Foundation 绘制（移植并整理 SmartToolFactory 版本）+ 令牌 | 原生 `Slider`                    | 原生 `Slider`（丢弃 `(Float) -> Unit` 参数） |
| 线性进度     | Canvas 轨道 + 进度段 + 循环短段                           | 原生 `LinearProgressIndicator`   | 原生 `ProgressBar`                           |
| 环形进度     | Canvas 整圈轨道 + 顶部起弧 + 循环旋转弧                   | 原生 `CircularProgressIndicator` | 原生 `ProgressRing`（默认 32dp）             |

- 滑块保留拖拽、点按、点按吸附、RTL、禁用与 `ProgressBarRangeInfo`/`setProgress` 语义；默认外观对齐应用
  滑块的既有形态（16dp 胶囊轨道、15dp 描边拇指、轨道两端 coerce、透明刻度、禁用 38% 透明度），画刷、
  描边、刻度与拇指测量的绘制路径全部保留在实现内部；公共入口不暴露尺寸、颜色、拇指槽位与标签变体
  （标签仍属应用层组合），不依赖旧 Material 门面。
- 进度由原 `core/ui-foundation` 的多段进度实现整理而来：保留 Canvas 绘制与目标值动画，新增不确定态； 多段
  `ProgressData` 模型与旧入口一并删除。线性轨道与滑块一致（16dp 胶囊高度、`primary`/`surfaceHigher`
  配色），环形保持 32dp / 3dp 并只统一配色。
- 滑块版权头保留 Apache-2.0 与来源说明。

## 各实现与拆分入口

公共 component 留在核心，内置实现 nomi、material3、fluent 各自模块根下按
text/button/surface/card/navigation/scaffold/list/icon/divider/control/feedback 组织，package
与目录一致；nomi 为独立 Gradle 模块但包名不变。 核心 component 只把 SPI 接口拆成 `NomiXxxRenderer.kt`
，其余公共声明都随组件文件：入口函数与文件同名 （如 `NomiButton.kt`、`NomiSwitch.kt`、
`NomiNavigationSuite.kt`），颜色/枚举/默认值 object 与导航项作用域同在 该文件内，族标记与 `XxxSpec`
紧随入口；FluentButtonColors 归 button。共享 NomiBorder、NomiControlSize、NomiDesignSystemComponents
留在核心 component 根， NomiNativeContentStyle 与 internal DefaultNomiSystem 位于 nomi 模块根（经
`NomiDesignSystem.Default` 暴露），集中内置主题及组件装配；公共主题 API/数据留在 theme，通用请求/契约/显式
slot 留在 runtime。 LocalNomiDesignSystem 为可空且无隐式默认，应用根/测试/Preview 显式提供；M3/Fluent
的主题、转换与装配位置不变。text/actions/surfaces/navigation/scaffold/listItems/icons/decorations/controls/feedback
十族 SPI，Card 仍由 surfaces 的独立方法派发，导航套件由 navigation 族派发，文本输入框由 text 族派发。
消费者直接更新 imports 与显式提供点，不增加旧 package 转发、typealias、兼容重载或隐式回退。

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
导航外壳以 M3 `NavigationBar`/`NavigationRail`/`PermanentNavigationDrawer` 为原生载体：Bar/Rail 复刻既有
Navigation.kt 视觉，Drawer 为永久抽屉并使用 Nomi 令牌抽屉项（文案与图标同排、选中 primary 轻量底色）。
外壳容器色可由调用方 `containerColor` 覆盖，Unspecified 回退 `surfaceLow`，`Transparent` 是有效覆盖。
Nomi 可以使用自己的 Foundation 样式和交互反馈，它们不再充当其他 adapter 的原生默认。

### Material 3

[主题配置](<../../core/design-system-material3/src/commonMain/kotlin/ciyin/designsystem/material3/Material3DesignSystem.kt>)
默认建立 Material 原生调色板、Typography 和 Shapes；`NomiDesignSystem.Material3` 属性提供原生默认实例，
`NomiDesignSystem.Material3(map)` 接收
`(NomiTheme) -> Material3Theme` 映射函数，预置映射是 `toNative/toPrimary/toFullMaterial3Theme()` 三个扩展
函数。适配器只安装原生主题，不再把原生主题投影回中立值；映射后的原生角色参与原生主题，未映射角色保留原生
默认；旧门面的原始 Material Typography 身份仍由 legacy 配置保持。

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
size/padding/role；现在用户选择保留原生默认，代价通过适配器降级与日志表达，
不再为达到统一视觉契约覆盖库自身实现。当前真实实现已拆为
[FluentText](<../../core/design-system-fluent/src/commonMain/kotlin/ciyin/designsystem/fluent/text/FluentText.kt>)、
[FluentButton](<../../core/design-system-fluent/src/commonMain/kotlin/ciyin/designsystem/fluent/button/FluentButton.kt>)、
[FluentSurface](<../../core/design-system-fluent/src/commonMain/kotlin/ciyin/designsystem/fluent/surface/FluentSurface.kt>)
与
[FluentCard](<../../core/design-system-fluent/src/commonMain/kotlin/ciyin/designsystem/fluent/card/FluentCard.kt>)
与
[FluentNavigation](<../../core/design-system-fluent/src/commonMain/kotlin/ciyin/designsystem/fluent/navigation/FluentNavigationRenderer.kt>)
与 [FluentList](<../../core/design-system-fluent/src/commonMain/kotlin/ciyin/designsystem/fluent/list/FluentListItemRenderer.kt>)；
[FluentComponents](<../../core/design-system-fluent/src/commonMain/kotlin/ciyin/designsystem/fluent/FluentComponents.kt>)
仅组装十族，
[主题配置](<../../core/design-system-fluent/src/commonMain/kotlin/ciyin/designsystem/fluent/FluentDesignSystem.kt>)
负责原生主题与 `NomiDesignSystem.Fluent` 属性、`NomiDesignSystem.Fluent(map)` 函数。
完整边界见[Fluent README](<../../core/design-system-fluent/README.md>)。

`toPrimaryFluentTheme()` 只把 primary 生成强调色阶；`toFullFluentTheme()` 额外映射八个排版角色与三个圆角值，其余角色保留原生。
Fluent 没有完整色板入口，因此不提供 FullColors，也没有运行时降级告警；映射函数只能访问真实存在的原生角色。
原生字体、形状和状态不从父系统的中立投影回填。原生组件自身的限制应显式记录并降级， 不能为通过旧统一外观测试而重新增加
clickable、强制尺寸或统一 token。导航容器色同理：`NavigationView` 的 `color` 是内容面板底色，导航容器来自窗口
材质，因此显式 `containerColor` 被忽略并记录警告，不改写内容面板冒充支持。

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
2. 主题值统一经 `NomiTheme` 构造；原生角色由 adapter 映射函数决定，不重新引入
   request/overrides/Override 入口 或主题聚合输入。
3. 用 NomiBorder 三态表达意图；显式 null 不是原生默认。
4. 无需查询能力：不支持的覆盖由当前适配器降级并记录警告；需要 Fluent 卡片颜色/描边时选择 NomiSurface。
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
与
[局部覆盖](<../../app/sample/src/commonMain/kotlin/com/ciyin/app/presentation/screen/designsystem/component/DesignSystemOverrideShowcase.kt>)。
测试与宿主入口见[sample README](<../../app/sample/README.md>)
；前轮已运行回归和三系统原生默认/显式区截图，上一轮主题结果扁平化不改变此布局，也不据旧截图声明当前测试通过。

```text
Material3 应用根
└─ Sample 独立导航栈
   └─ Gallery：Nomi / Material3 / Fluent + 明暗模式 + 字体比例
      ├─ 原生默认：Text / Button / Surface / Card / ListItem（不填视觉默认参数）
      ├─ 显式覆盖：只演示用户主动指定的颜色/变体/可支持参数
      └─ 降级示例：不支持的覆盖保留原生默认并在日志告警（如 Fluent Button size/shape/padding）

窄/中窗口：控制区在上，展示区在下，整页滚动
宽窗口：  [紧凑控制区] | [带标签原生网格 / 横向按钮 / 可展开限制 / 单项排版预览]
```

后续布局优化保持原生风格：各展示区以原生 Card 组织，四默认示例和表面层级复用 foundation 自适应网格；
排版角色使用选择器与单项真实字号预览，避免十五种标题顺序撑长页面。
列表项分区展示可点击行（前导图标、标题/副标题、值文本与箭头）与静态行（尾部设计系统按钮），不传颜色、形状或内边距参数。
控制区不再按页面三分之一无限变宽，大字体时按可用空间回退单列。

只读投影可供说明和显式示例使用，但不能把全部投影自动填回“原生默认”区。局部覆盖展示必须验证切换新系统
使用其原生默认，显式增量继承且退出恢复。测试/预览不启动真实设备能力。

## 后续组件缺口清单

> 本节记录 2026-11-18 的盘点结果与优先级建议， **不是已采纳的排期**，也不改变本文「已实现」状态。
> 2026-11-19 已落地图标、分割线、控件与反馈四族，并在文本族新增文本输入框；下表只保留未落地项。
> 落地任一族前须重新核对调用点计数、目标平台与三端原生能力。

### 已落地范围

| 族       | 公共入口                                                                                                                                                                                               |
|----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 文本     | `NomiText` / `NomiTextField`（显示与编辑共用同一 renderer）                                                                                                                                            |
| 操作     | `NomiButton`（Primary / Default / Subtle / Text / Destructive）/ `NomiToggleButton` / `NomiDropDownButton`                                                                                             |
| 表面     | `NomiSurface` / `NomiCard`                                                                                                                                                                             |
| 导航     | `NomiNavigationSuite`（Bar / Rail / Drawer）                                                                                                                                                           |
| 页面外壳 | `NomiScaffold` / `NomiTopAppBar`                                                                                                                                                                       |
| 列表项   | `NomiListItem`                                                                                                                                                                                         |
| 图标     | `NomiIcon`                                                                                                                                                                                             |
| 分割线   | `NomiDivider`（`NomiDividerOrientation.Horizontal` / `Vertical`）                                                                                                                                      |
| 控件     | `NomiSwitch` / `NomiCheckbox` / `NomiRadioButton` / `NomiComboBox` / `NomiSlider`                                                                                                                      |
| 反馈     | `NomiTooltip` / `NomiProgressIndicator`                                                                                                                                                                |
| 菜单     | `NomiMenu` / `NomiMenuItem`                                                                                                                                                                            |
| 辅助类型 | `NomiBorder` / `NomiControlSize` / `NomiTextFieldVariant` / `NomiDividerOrientation` / `NomiToggleButtonColors`                                                                                        |
| 图标资源 | [IconPack](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/icon/IconPack.kt>) 共 8 个：Home、ArrowRight、LightMode、DarkMode、Settings、Null、ChevronDown、Check；固有尺寸统一 24dp |

十一族由 [NomiDesignSystemComponents](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/component/NomiDesignSystemComponents.kt>)
组装。新增一族须同步三个生产实现（[DefaultNomiDesignSystem](<../../core/design-system-nomi/src/commonMain/kotlin/ciyin/designsystem/nomi/DefaultNomiDesignSystem.kt>)、
[Material3Components](<../../core/design-system-material3/src/commonMain/kotlin/ciyin/designsystem/material3/Material3Components.kt>)、
[FluentComponents](<../../core/design-system-fluent/src/commonMain/kotlin/ciyin/designsystem/fluent/FluentComponents.kt>)）
与三个测试替身（[RecordingDesignSystem](<../../core/design-system/src/commonTest/kotlin/ciyin/designsystem/RecordingDesignSystem.kt>)、
`NomiNativeContentIsolationTest`、`ExternalApiConsumptionTest` 内的匿名实现）。新增一个 **入口**还要补齐
core 族契约（sealed 族标记 + typealias）、renderer 方法参数与 adapter 专属 spec；spec 族按入口计， 同一
SPI 族内多个入口不共享 spec 类型。

### 缺口与优先级

计数口径：`import androidx.compose.material3.<符号>` 的源码文件数，含 `app/sample` 演示页，不含
`build/`、 测试文件与文档；同一文件多处使用只计一次。未标注计数的行表示当前无外部调用方。

| 优先级 | 组件                                              | 现有调用方                      | 依据                                                                                                                                 |
|--------|---------------------------------------------------|---------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| P0     | `NomiIconButton`                                  | 14（sample 13、video-player 1） | 纯图标操作无法复用 `NomiButton` 的 variant 与尺寸                                                                                    |
| P1     | `NomiBadge`                                       | 适配器内部 2（无外部调用方）    | 导航项已有 `badge` 槽，调用方缺中立徽标可组合                                                                                        |
| P1     | `NomiTabs` / `NomiChip`                           | 仅 `core/ui-foundation` 旧实现  | 旧 `ScrollableTabRow`、`FilterChip` 属 Material 门面，不进中立核心                                                                   |
| P1     | 列表分组容器（`NomiListHeader` / 分组标题）       | 无                              | 列表项族只有单行；分组标题与容器未提供                                                                                               |
| P2     | `NomiDialog` / `NomiBottomSheet` / `NomiSnackbar` | 旧实现与 sample 各 1            | 旧 `AlertDialog2`、`BottomSheetDialog`、`ExposedMenu`、`Toast` 均属 Material 门面，且涉及窗口或宿主状态；`NomiMenu` 已随按钮轮次落地 |

### 待迁移调用点（记录）

以下调用点仍直连原生 Material 3 组件；中立入口已经就位，迁移留给后续轮次，避免本轮混入页面行为变更：

| 入口            | 数量与位置                                      |
|-----------------|-------------------------------------------------|
| `NomiIcon`      | 18（sample 15、shared 1、video-player 2）       |
| `NomiDivider`   | 横向 10（sample 9、shared 1）+ 纵向 1（shared） |
| `NomiSwitch`    | 2（sample）                                     |
| `NomiTextField` | 4（sample，均为 `OutlinedTextField`）           |
| `NomiTooltip`   | 1（sample）                                     |

迁移时删除为「统一外观」补入的参数，只保留调用方真正需要的显式覆盖；分割线的显式 `thickness` / `color`
与输入框的 `variant` / `isError` 在不支持的系统上会降级并记录警告。

### 非组件缺口

- **图标资源**：`IconPack` 目前 8 个矢量，不够基础页面使用；`NomiIcon` 已就位，图标先用
  `valkyrie-imagevector` 补充。固有尺寸在 2026-11-19 统一为 24dp（此前 `DarkMode` 1024dp、`Home` 与
  `ArrowRight` 200dp、`LightMode` 与 `Settings` 256dp、`Null` 1dp），
  [Home.kt](<../../core/design-system/src/commonMain/kotlin/ciyin/designsystem/icon/Home.kt>) 的
  KDoc 同步修正为 24dp，槽位不再必须显式 `Modifier.size`。
- **列表容器**：列表项族只有单行；分组标题、分隔线与容器未提供，列表项文档已列为暂不支持。
- **Fluent 纵向分割线**：`NomiDivider` 已提供纵向方向，但 Fluent v0.1.0 没有纵向原语，该实现降级为不渲染
  并记录警告；横向分割线的显式粗细与颜色同样被忽略。

### 依赖顺序与建议轮次

```text
第 1 轮  原子层      NomiIcon（已落地） -> NomiIconButton -> NomiDivider（已落地，含纵向）
第 2 轮  控件族      Switch / Checkbox / RadioButton / TextField / Slider（已落地）
第 3 轮  反馈与结构  ProgressIndicator（已落地） -> TopAppBar（已落地） -> Badge -> Tabs / Chip（Tooltip 已落地）
第 4 轮  浮层        Dialog -> BottomSheet -> Menu -> Snackbar
```

- 图标族与控件族已就位：列表项尾部槽、设置行与表单可以用中立入口组合，不再直连原生控件。
- 每个入口的固定成本是 `NomiDesignSystemComponents` 扩展 + 三个 adapter + 六个实现点 + 契约测试， 外加
  core 组件级 spec 契约（sealed 族标记 + typealias）；Fluent 无法表达时按既有策略降级并记录 警告，不重绘伪造支持。
- 优先级不只看计数：当前 `app/shared` 只有导航与设置占位页，多数调用点在 `app/sample` 演示页；图标族与控件族
  是任何页面的前置能力，因此不因计数低而下调。

## 验证记录与验收缺口

### 本轮：新增顶栏 NomiTopAppBar，2026-11-22

范围：`scaffold` 族新增第二个入口 `NomiTopAppBar`（标题、导航图标、操作槽与四种颜色稀疏覆盖）， 复用
`NomiScaffoldRenderer` 增加 `TopAppBar` 方法；Nomi 用 appBar 令牌组装 56dp 行，Material 3 使用 原生
`TopAppBar`，Fluent 用原生色板组装 48dp 行（与原生命令栏同高）。

- 验证：核心 73、Nomi 42、Material 3 45、Fluent 63，共 293 项 Desktop 回归零失败；sample 70 项 Desktop
  测试零失败；四模块 `compileCommonMainKotlinMetadata` 与
  `:core:design-system:compileTestKotlinDesktop` 通过。
- 覆盖：顶栏派发（默认稀疏、显式颜色与 spec 透传）、Nomi 56dp 高度与槽位顺序、Fluent 48dp 高度、 Material 3
  三槽渲染。
- 未执行：Android / iOS / Web 与真机运行；sample 的 11 处直连顶栏未迁移（需要先补 `NomiIconButton`）。

### 本轮：进度指示器外观对齐滑块，2026-11-22

范围：`core/design-system-nomi` 的线性进度改为与滑块一致的 16dp 胶囊轨道与 `primary`/`surfaceHigher`
配色，环形保持 32dp / 3dp 并统一配色；轨道高度与配色分别定义在滑块与进度自己的实现里。
`NomiProgressIndicator` 公共签名与核心模块未改动。

- 验证：核心 71、Nomi 40、Material 3 43、Fluent 61，共 285 项 Desktop 回归零失败；sample 70 项 Desktop
  测试零失败；四模块 `compileCommonMainKotlinMetadata` 通过。
- 覆盖：线性 16dp 高度、环形 32dp 尺寸、确定/不确定语义。
- 未执行：Android / iOS / Web 与真机运行；进度外观截图对比。

### 本轮：滑块外观对齐应用滑块，2026-11-22

范围：`core/design-system-nomi` 的滑块实现按应用滑块的既有外观重写（16dp 胶囊轨道、15dp 描边拇指、 轨道两端
coerce、透明刻度、禁用 38% 透明度），恢复 `SubcomposeLayout` 拇指测量与画刷/描边/刻度绘制 路径；
`NomiSlider` 公共签名、`NomiSliderSpec` 预留状态与核心模块均未改动。

- 验证：核心 71、Nomi 38、Material 3 43、Fluent 61，共 283 项 Desktop 回归零失败；sample 70 项 Desktop
  测试零失败；四模块 `compileCommonMainKotlinMetadata` 通过。
- 覆盖：默认 30dp 高度、启用/禁用色板与 38% 透明度、RTL 拖拽、点按吸附到 1/3 刻度、进度语义。
- 未执行：Android / iOS / Web 与真机运行；与重组前或应用滑块的截图对比。

### 本轮：滑块与进度族，2026-11-22

范围：控件族新增 `NomiSlider`，反馈族新增 `NomiProgressIndicator`（线性/环形 × 确定/不确定）；共两个入口、
两个组件级 spec 契约。Nomi 实现由两处既有实现整理而来（`core/ui-foundation` 的多段进度、应用层
SmartToolFactory 滑块移植版），并删除旧 `core/ui-foundation/widget/progress` 包；三端实现落地；sample
控件分区补滑块与进度演示。

- 验证：核心 71、Nomi 34、Material 3 43、Fluent 61，共 209 项 Desktop 回归零失败；sample 70 项 Desktop
  测试零失败；四模块 `compileCommonMainKotlinMetadata` 通过。
- 覆盖：滑块默认稀疏派发与显式参数透传、拖拽改变值、禁用不响应、`ProgressBarRangeInfo` 语义；确定/不确定
  进度与环形形态、三端渲染不崩溃。
- 破坏性变更：删除 `core/ui-foundation` 的 `ProgressData` / `LinearProgressBar` / `ArcProgressBar`
  （仓库内 0 调用方），多段进度能力不再保留。
- 未执行：Android / iOS / Web 与真机运行；滑块触摸手感与进度动画的截图对比；sample 与 video-player 现有
  Material 3 进度/滑块调用点仍待迁移。

### 本轮：开关 / 下拉按钮、组合框与菜单族，2026-11-19

范围：操作族新增 `NomiToggleButton` / `NomiDropDownButton`，控件族新增 `NomiComboBox`，新增第十一族
`menus`（`NomiMenu` / `NomiMenuItem`）；共五个入口、五个组件级 spec 契约；三端实现落地；`IconPack` 补
`ChevronDown` 与 `Check`（共 8 个矢量）；sample 按钮分区补开关按钮/图标开关/下拉按钮与菜单，控件分区补
组合框。

- 验证：核心 65、Nomi 29、Material 3 42、Fluent 60，共 196 项 Desktop 回归零失败；sample 的 28 项
  `DesignSystemDemo*` 桌面测试零失败；四模块 `compileCommonMainKotlinMetadata` 与
  `:core:design-system:compileTestKotlinDesktop` 通过。
- 覆盖：开关按钮两态色与 `iconOnly`、下拉按钮不追加内容、组合框收起/展开时菜单项的组合时机、菜单收起不组合
  内容、菜单项槽位与 `selected`、三端原生渲染与 Fluent 的 `size` 降级告警。
- 未执行：Android / iOS / Web 与真机运行、三端浮层位置与宽度对齐的截图对比；`SplitButton` /
  `ToggleSplitButton`、`NomiIconButton` 仍保留在缺口清单。

### 本轮：补齐图标 / 分割线 / 控件 / 提示四族，IconPack 统一 24dp，2026-11-19

范围：新增 `icons`（`NomiIcon`）、`decorations`（`NomiDivider`，含横竖方向枚举）、`controls`
（`NomiSwitch` / `NomiCheckbox` / `NomiRadioButton`）与 `feedback`（`NomiTooltip`）四个族，以及 文本族新成员
`NomiTextField`：共七个入口、七个组件级 spec 契约；三端实现落地，Fluent 对纵向分割线、分割线
粗细/颜色、输入框变体与错误态降级并记录警告；`IconPack` 6 个矢量固有尺寸统一为 24dp 并修正
`Home` 的 KDoc；sample 新增图标/分割线/控件分区，核心与三个 adapter 补契约测试。

- 验证：六模块 Desktop 回归 273 项零失败（核心 56、M3 42、Fluent 60、Nomi 29、material 19、sample 67）； 四模块
  `compileCommonMainKotlinMetadata`、`:app:sample:compileKotlinDesktop` 与
  `:app:shared:compileKotlinDesktop` 通过。
- 覆盖：图标着色回落与稀疏派发、横竖分割线、三个选择控件的状态/回调、文本族输入框的槽位与键盘参数、
  提示浮层双槽组合、三端原生渲染与 Fluent 降级不崩溃、sample 分区可见与开关可点击。
- 结构：文本输入框归属文本族而非控件族，替换 `NomiTextRenderer` 会同时接管显示与输入；`controls` 只保留
  三个选择控件，二者 gate 各自的 spec 契约不变。
- 未执行：Android / iOS / Web 与真机运行、截图对比；`app/shared`、feature 模块与 sample 既有页面仍直连
  原生 Material 3 组件，迁移清单见「待迁移调用点」。

### 本轮：增补后续组件缺口清单，2026-11-18

范围：仅在本文件新增「后续组件缺口清单」一节，并在设计索引补摘要；未改动任何组件、测试或构建配置。

- 依据：当日工作区源码盘点——六族入口与 `NomiDesignSystemComponents` 实现点、`IconPack` 图标数量与固有尺寸、
  `androidx.compose.material3` 符号 import 计数。
- 未执行：Gradle 编译与测试。本轮无代码改动，文档编辑不触发构建；清单计数需在落地前复核。

### 本轮：Nomi Bar 只渲染主导航项，2026-11-16

范围：`DefaultNomiNavigationRenderer.BarNavigationSuite` 不再渲染 `footerItems`，只渲染主导航项，复刻既有底部栏
在分组占位项处终止的视觉；Rail/Drawer 的底部组行为不变，核心导航契约文档与 Nomi 测试同步。

- Nomi Desktop 回归 23 项通过，零失败；`:core:design-system-nomi` 与依赖它的 sample 编译通过。
- 覆盖：Bar 只展示主导航项与页面内容、底部组声明确实被忽略、选中/点击语义保持；Rail tooltip 与 Drawer
  底部组不回归。
- 未执行：Android/iOS/Web 与设备运行、截图对比；Bar 的最终视觉仍以真机/桌面截图验收。

### 本轮：NomiTheme 双入口，2026-11-17

范围：`NomiTheme` 拆为两个重载——`darkMode` 根入口解析一次明暗并使用浅/深默认主题；显式 `theme` 入口
安装调用方给定的主题。嵌套继承改为显式 `theme = LocalNomiTheme.current`；`darkMode` 只在根入口读取系统状态。

- 验证：core 30、Material3 35、Fluent 52、nomi 23、material 19、sample 25，共 184 项零失败。
- 覆盖：模式入口解析、显式主题安装、slot 重置、嵌套显式继承、适配器原生映射。

### 本轮：删除 NomiTheme.current，适配器继承当前原生主题，2026-11-17

范围：删除 `NomiTheme.current`，主题入口默认值改为读取 `LocalNomiTheme.current`，伴生分组 getter 保留；
文档与测试不再通过整主题 getter 读取。Material3 / Fluent 对未映射的排版与形状/圆角改为继承当前原生主题：
M3 使用当前 `MaterialTheme.typography/shapes`，Fluent 使用当前 `FluentTheme.typography/cornerRadius`；
Fluent 删除已无用的首次进入基线捕获（`LocalFluentNativeBaseline`、`FluentNativeBaseline`）。 同时把
`NomiTheme`、`Material3Theme`、`FluentTheme` 的 `darkMode: DarkMode` 字段改为 `isDark: Boolean`；
`DarkMode` 只保留为应用层请求枚举与旧门面 `AppTheme.darkMode` 的映射来源。

- 验证：core 30、Material3 35、Fluent 52、nomi 23、material 19、sample 25，共 184 项通过。
- 覆盖：主题发布/继承、slot 重置、三个主题数据的布尔明暗、M3/Fluent 当前原生组继承、A-B-A 复用当前值。

### 本轮：合并映射文件，2026-11-17

范围：`Material3Conversions.kt` 的全部转换函数并入 `Material3ThemeMapping.kt`，文件更名为
`Material3Mapper.kt`；
`FluentThemeMapping.kt` 更名为 `FluentMapper.kt`。包名、公开函数与调用契约不变，只调整文件组织与 README
链接。

- 验证：Material3 35 项、Fluent 52 项、core:material 19 项通过；全量回归 184 项零失败；改动文件 KDoc 0
  错误。

### 本轮：预置映射改为接收者扩展函数，2026-11-17

范围：删除 `Material3Map`、`FluentMap` 两个 typealias 与 `Material3Maps`、`FluentMaps` 两个预置对象；
映射工厂统一接收 `NomiTheme.() -> XxxTheme` 接收者函数，预置映射改为 `toNativeMaterial3Theme` /
`toPrimaryMaterial3Theme` / `toFullMaterial3Theme` 与 Fluent 同名的三个扩展函数。调用点、测试、README 与
设计说明同步；M3 与 Fluent 的预设 API 形状保持一致。

- 验证：Material3 35 项、Fluent 52 项、core:material 19 项通过；全量回归见下。
- 覆盖：预置扩展只声明各自原生组、工厂默认共享实例、自定义映射通过接收者 lambda 组合预置。

### 本轮：Fluent 主题规格开放 acrylic/compactMode，2026-11-17

范围：`FluentTheme` 规格新增 `useAcrylicPopup` 与 `compactMode`，默认取原生 FluentTheme 默认值
`true` / `true`；
`ProvideTheme` 不再硬编码 `false` / `true`，而是透传规格字段。开发者可通过自定义 `FluentMap` 单独关闭
acrylic 弹层或切换舒展密度，`FluentMaps.Native/Primary/Full` 保持默认值。

- 验证：`:core:design-system-fluent:desktopTest` 全绿；新增规格默认值与覆盖用例。
- 说明：原生 `LocalAcrylicPopupEnabled` / `LocalCompactMode` 为 internal，不能读取父级值，因此两字段是显式布尔，
  不做 null 继承；嵌套边界未指定时回到默认值。

### 本轮：无参访问改为扩展属性，2026-11-17

范围：Material3 / Fluent 适配器新增同名扩展属性 `NomiDesignSystem.Material3` /
`NomiDesignSystem.Fluent`， 无参调用点统一改为属性读取；带映射仍用 `Material3(map)` / `Fluent(map)`
普通函数。属性与函数默认分支共用
`Material3Maps.Native` / `FluentMaps.Native` 的共享实例，避免逐次组合更换 `LocalNomiDesignSystem` 身份。

- 验证：`:core:design-system-material3:desktopTest` 35 项、`:core:design-system-fluent:desktopTest` 51
  项通过， identity 用例继续锁定属性与默认函数返回同一实例。
- 覆盖：属性读取、同名函数共存、默认 Native 共享实例、带映射实例独立。

### 本轮：删除 native 反向投影，2026-11-16

范围：`NomiTheme` 入口自行发布 `LocalNomiTheme`；Material3 / Fluent 适配器不再把原生主题投影回中立值，只安装原生主题。
删除 `Material3Projection.kt`、Fluent 的 native→中立转换与相关投影测试；M3 导航回退色改读
`MaterialTheme`； sample 的排版/表面/主色展示改读各系统原生主题（`NativeShowcaseTokens.kt`）。
`NomiTheme.current` 语义收敛为“本层显式输入值”。

- 四模块与 sample 共 165 项 Desktop 回归通过（核心 30、M3 35、Fluent 52、Nomi 23、sample 25），另
  `core:material`
  19 项通过；四模块 metadata 与 `:app:shared` 编译通过，零失败。
- 覆盖：入口发布主题、适配器不回写中立值、M3/Fluent 原生映射与基线、sample 原生 token 展示与像素恢复。
- 未执行：Android/iOS/Web 与设备运行、截图对比。

### 本轮：主题 API 改为值 + 映射函数，2026-11-16

范围：删除 `NomiThemeOverride`、`NomiThemeOverrides`、`NomiThemeRequest`、`ResolvedNomiTheme`、
`LocalResolvedNomiTheme`
与请求合并/回调投影；`NomiTheme` 变为不可变值（五组 + darkMode），入口只接收 `theme` 与 `designSystem`。
Material3 / Fluent 工厂接收 `(NomiTheme) -> 原生主题值` 映射函数，无参工厂使用原生默认；适配器把生效原生值投影为
`LocalNomiTheme` 供读取。`AppTheme` 门面、app/shared 与 sample 同步迁移。

- 四模块与 sample 共 165 项 Desktop 回归通过（核心 30、M3 35、Fluent 52、Nomi 23、sample 25），另
  `core:material`
  19 项通过，全部零失败；四模块 metadata 与 `:app:shared` 编译通过。
- 覆盖：NomiTheme 值与 light/dark、伴生 getter、入口 slot 重置、嵌套继承、原生默认不映射、Primary 单角色、
  Full 整组、Fluent 色阶与基线、外部自定义实现、legacy 门面与 sample 像素恢复。
- 未执行：Android/iOS/Web 与设备运行、截图对比；平台与发布缺口保留。

### 本轮：移除 accentColor 单色覆盖，2026-11-16

范围：删除 `NomiThemeOverrides.accentColor` 与主题入口的“同层完整色板+accent”校验；`merge` 简化为 逐组
`child ?: parent`；Nomi/M3/Fluent 适配器同步删除 accent 分支。颜色覆盖只剩完整
`NomiColorScheme`：M3 映射到原生色板，Fluent 只取 primary 生成强调色阶。`core:material` 兼容测试、
sample 展示页（删除 customTokens 开关）与 README/skill/设计说明同步。

- 四模块与 sample 共 167 项 Desktop 回归通过（核心 39、M3 33、Fluent 47、Nomi 23、sample 25）， 另
  `core:material` 19 项通过，全部零失败；核心/nomi/fluent metadata 与 `:app:shared` 编译通过。
- 覆盖：主题入口的整组继承与完整色板替换、透明色仍是有效覆盖、M3/Fluent 颜色映射与降级、 legacy
  门面读取新根投影、sample 局部设计系统覆盖与覆盖前后像素恢复。
- 未执行：Android/iOS/Web 与设备运行、截图对比；单色强调色替代方案留待后续设计。

### 本轮：移除旧式直接参数主题入口，2026-11-16

范围：`NomiTheme` 与 `NomiThemeOverride` 只保留 `designSystem`、`darkMode`、`overrides` 与 content；
删除五组直接参数与 `explicitThemeOverrides` 归一化/同组重复来源校验，同层完整色板与 accent 冲突仍由
主题入口 require；五组覆盖与 accent 统一经 `NomiThemeOverrides` 输入，调用方、测试与 README/skill 同步。

- 四模块与 sample 共 166 项 Desktop 回归通过（核心 40、M3 33、Fluent 46、Nomi 23、sample 24），零失败； 四模块
  metadata、`:app:shared` 与 sample 编译通过。
- 覆盖：根/局部入口的整组继承、颜色替换、显式组保真与同层色板+accent 拒绝；M3/Fluent 主题映射不回归。
  核心主题契约测试改名为 ThemeEntryContractTest，Fluent 主题入口测试改名为 FluentThemeEntryTest。
- 未执行：Android/iOS/Web 与设备运行、截图对比；主题输入收敛不改变原生渲染结果。

### 本轮：删除能力声明并改为适配器降级，2026-11-16

范围：删除 `NomiComponentCapabilities` 与 `NomiDesignSystem.capabilities`；核心入口不再按能力拦截；
