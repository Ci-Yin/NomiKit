# :core:design-system-fluent

## 用途与边界

为中立设计系统提供 Compose Fluent **v0.1.0** 的原生默认优先适配。 统一调用入口，不统一外观；当前覆盖
Text、Button、Surface、独立原生非点击 Card、导航套件与列表项。 不负责 Mica、窗口生命周期、输入控件或其它二期组件。

- 使用仓库 `multiplatform-lib-targets`，Android 与按项目开关注册的 Desktop/iOS/JS/Wasm 共用
  commonMain。
- 不为本模块打开项目关闭的平台；sample commonMain 默认候选包含 Nomi/M3/Fluent。
- [构建](<build.gradle.kts>) api 导出中立核心与 Fluent；公开映射函数签名使用 Fluent 类型。
- 原生类型不进入中立 SPI；普通业务只消费中立组件，专属原生映射留在装配层。
- 不添加全局双主题桥，不依据平台静默选择设计系统。

## 实现入口

- [FluentDesignSystem](<src/commonMain/kotlin/ciyin/designsystem/fluent/FluentDesignSystem.kt>)
  ：映射求值与原生主题安装，经 `NomiDesignSystem.Fluent` /
  `NomiDesignSystem.Fluent(map)`（导入 `ciyin.designsystem.fluent.Fluent`）暴露；只安装原生主题，不发布中立主题。
- [FluentTheme](<src/commonMain/kotlin/ciyin/designsystem/fluent/FluentTheme.kt>)：原生主题规格。
- [主题映射](<src/commonMain/kotlin/ciyin/designsystem/fluent/FluentMapper.kt>)：三个预置扩展
  `NomiTheme.toNativeFluentTheme()` / `toPrimaryFluentTheme()` / `toFullFluentTheme()`，以及强调色阶重建、
  中立色板/排版/圆角到原生主题的单向换算。
- [组件告警](<src/commonMain/kotlin/ciyin/designsystem/fluent/FluentWarnings.kt>)：原生无法承载的显式
  组件覆盖只记录一次日志。
- [组件装配](<src/commonMain/kotlin/ciyin/designsystem/fluent/FluentComponents.kt>)只组装六族。
- [Text](<src/commonMain/kotlin/ciyin/designsystem/fluent/text/FluentText.kt>)、[Button](<src/commonMain/kotlin/ciyin/designsystem/fluent/button/FluentButton.kt>)、[Surface](<src/commonMain/kotlin/ciyin/designsystem/fluent/surface/FluentSurface.kt>)、[Card](<src/commonMain/kotlin/ciyin/designsystem/fluent/card/FluentCard.kt>)
  分别实现原生组件调用。
- [按钮颜色](<src/commonMain/kotlin/ciyin/designsystem/fluent/button/FluentButtonColors.kt>)
  只覆盖原生五态中明确指定的通道。
- [导航套件](<src/commonMain/kotlin/ciyin/designsystem/fluent/navigation/FluentNavigationRenderer.kt>)
  把槽位式中立导航项与布局映射到原生 NavigationView，只在显式覆盖时改写原生五态颜色，不重建排版或尺寸配方。
- [列表项](<src/commonMain/kotlin/ciyin/designsystem/fluent/list/FluentListItemRenderer.kt>)
  使用原生 `CardExpanderItem` 的点击/无点击重载：标题注入 bodyStrong，尾部注入 caption 与 secondary
  内容色；默认不传 `dropdown`，不绘制展开箭头。展开槽、交互源与四态配色只经
  [FluentListItemSpec](<src/commonMain/kotlin/ciyin/designsystem/fluent/list/FluentListItemSpec.kt>)
  显式提供。

四组件分别归入 fluent 根下的 text/button/surface/card 子目录，package 与目录一致；FluentButtonColors 随
Button 进入 button。 主题、映射和四族装配留在原位置，Card 仍由 surfaces 派发，导航套件归入 fluent 根下的
navigation 子目录；不保留旧 package 转发或 typealias。

## 最小接入

```kotlin
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.card.NomiCard
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.fluent.Fluent // 提供 NomiDesignSystem.Fluent
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme

NomiTheme(designSystem = NomiDesignSystem.Fluent) {
    NomiCard {
        NomiText("Fluent")
        NomiButton(onClick = {}) { NomiText("Action") }
    }
}
```

示例文字仅展示 API，产品文案应走宿主资源系统。未指定 variant/size/shape/padding/colors/style
的调用保留原生默认， 不会把中立主题的排版、48dp、圆角或内边距反向填入原生组件。

## 主题映射函数模型

主题入口仍是核心的 `NomiTheme(designSystem, darkMode, theme, content)`；Fluent 只负责把收到的
`ciyin.designsystem.theme.NomiTheme` 翻译成原生主题并安装，不再发布或投影中立主题：

```kotlin
data class FluentTheme(
    val isDark: Boolean = false,
    val colors: Colors? = null,          // io.github.composefluent.Colors
    val typography: Typography? = null,  // 八角色原生排版
    val cornerRadius: CornerRadius? = null,
    val useAcrylicPopup: Boolean = true, // 弹层 acrylic
    val compactMode: Boolean = true,     // 紧凑密度
)
```

- `NomiDesignSystem.Fluent`：使用 `toNativeFluentTheme()`，只跟随 `isDark`，原生色板、排版与圆角沿用当前环境。
- `NomiDesignSystem.Fluent(map)`：按 `NomiTheme.() -> FluentTheme` 安装原生主题；`Fluent` 是扩展属性、
  `Fluent(map)` 是普通函数，都不是 `@Composable`；默认 `toNativeFluentTheme()` 与属性共用同一共享实例。
- `useAcrylicPopup` / `compactMode`：默认 `true` / `true`，对应原生 FluentTheme 默认；需要关闭 acrylic
  弹层或切换舒展密度时在映射里显式指定；嵌套 Fluent 边界的规格未指定时回到默认值。
- `toPrimaryFluentTheme()`：在原生 `lightColors()` / `darkColors()` 上只替换强调色阶。完整中立色板只取
  `primary` 重建 base 与三阶深浅色，其余角色和色板自带的 typed 明暗保持不变。
- `toFullFluentTheme()`：强调色、八角色排版与三档圆角都取中立主题。Fluent 没有完整色板入口，也没有九级圆角
  概念，因此颜色只走 `primary`，圆角按 tiny→intersectionEdge、small→control、large→overlay 取三个代表档位；
  其余颜色字段与档位不参与原生映射，也不被伪造。

安装顺序：先调用 `NomiTheme.() -> FluentTheme` 映射得到规格，再
`colors ?: lightColors()/darkColors()`、
`typography ?: 当前原生排版`、`cornerRadius ?: 当前原生圆角`，然后以规格的 `useAcrylicPopup` /
`compactMode`
建立原生 FluentTheme， 最后组合 `content()`。 中立主题由核心
`NomiTheme` 入口发布，伴生 getter 读到的始终是本层显式传入值，不是原生解析结果； spacings 与 sizes
只是入口主题里的页面度量，不用来重写原生控件默认。

未指定的排版与圆角继承当前原生 Fluent 环境，不再捕获首次进入基线； A-B-A 局部切换直接复用当前原生值。原生视觉由组件直接读取
`FluentTheme` 原生值，不做反向投影；
`toFullFluentTheme()` 把
bodySmall/bodyMedium/labelLarge/bodyLarge/titleLarge/headlineMedium/headlineLarge/displayLarge 分别映射到
caption/body/bodyStrong/bodyLarge/subtitle/title/titleLarge/display，其余中立角色保留在入口主题中。
`isDark` 是已解析布尔，适配器不读系统状态；System 由主题入口在 `theme` 省略时解析。

映射函数建议提升为稳定引用（顶层 `val` 或 `remember`）；内联 lambda 每次重组都会重建原生色板实例。
组件视觉直接读取 `FluentTheme.colors/typography/cornerRadius` 原生值，不读中立投影。 adapter 只把明确的
colors/textStyle 增量传给中立 slot，原生默认留在 native locals。 默认 Card 内的原生按钮因此仍能提供自己的禁用颜色与字号。

## 专属参数

各组件族的原生专属参数由独立 `data class` 实现核心对应的族契约，经入口的 `nativeSpec` 参数传入；
未传入或字段为 null 时全部回落原生默认：

| 族       | spec                   | 覆盖的原生能力                                                     |
|----------|------------------------|--------------------------------------------------------------------|
| 按钮     | `FluentButtonSpec`     | 原生五态 `ButtonColorScheme`（中立四字段表达不了悬停/按下/聚焦态） |
| 表面     | `FluentSurfaceSpec`    | 原生 `Layer` 的 `elevation`                                        |
| 导航套件 | `FluentNavigationSpec` | 侧栏 `title` 与 `backButton` 槽                                    |
| 列表项   | `FluentListItemSpec`   | `dropdown`、`shape`、`enabled`、`colors`、`interactionSource`      |

文本与导航项两族的族契约是核心预留插孔；Fluent 静态 Card 与页面外壳没有原生专属入口，一律忽略
`nativeSpec`。

## 列表项专属参数

`FluentListItemSpec` 实现核心的列表项族契约 `NomiListItemSpec`，承载未进入中立 API 的原生
`CardExpanderItem` 属性，经 `NomiListItem(nativeSpec = ...)` 传入：

```kotlin
import ciyin.designsystem.component.list.NomiListItem
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.fluent.list.FluentListItemSpec

NomiListItem(
    title = { NomiText("高级选项") },
    subtitle = { NomiText("点击展开") },
    onClick = { onExpand() },
    nativeSpec = FluentListItemSpec(dropdown = { NomiText("▾") }),
)
```

- `dropdown` 是尾部箭头槽，渲染在 trailing 之后；null 表示不占位，与原生默认一致。
- `shape` 默认 `FluentTheme.shapes.control`，`enabled` 默认 true，`colors` 默认
  `ExpanderDefaults.cardExpanderItemColors()`，`interactionSource` 默认由原生创建。
- 静态行（无 `onClick`）没有 `enabled` / `colors` / `interactionSource` 入口：携带时忽略并 记录一次警告，只应用
  `shape` 与 `dropdown`。
- 其它设计系统的 spec 与 null 一律静默忽略；多个 spec 用核心的 `+` 组合后一次传入。

## 原生限制与降级

| 中立调用                  | Fluent 原生调用与限制                                                                                                                                                                                                                             |
|---------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| NomiText                  | component.Text 双重载；部分 style 合入原生 LocalTextStyle；autoSize 无原生入口，忽略并记录警告                                                                                                                                                    |
| 默认 / Default Button     | component.Button                                                                                                                                                                                                                                  |
| Primary Button            | AccentButton                                                                                                                                                                                                                                      |
| Subtle Button             | SubtleButton                                                                                                                                                                                                                                      |
| Text Button               | HyperlinkButton                                                                                                                                                                                                                                   |
| Destructive Button        | Button + 显式 critical 配方                                                                                                                                                                                                                       |
| NomiSurface               | background.Layer；任意 shape、显式颜色与三态 border                                                                                                                                                                                               |
| NomiCard                  | surface.Card 非点击重载；显式 shape 和内容 padding 可用                                                                                                                                                                                           |
| NomiNavigationSuite       | 显示模式映射 Bar→LeftCollapsed、Rail→LeftCompact、Drawer→Left（Fluent 没有底部导航栏，三种布局都落在左置导航）；三者都用 component.NavigationView，只关闭内容面板描边。图标/徽标/文案为调用方槽位，颜色只覆盖显式通道，排版与折叠动效沿用原生默认 |
| NavigationItem modifier   | 原生没有项级修饰符入口；忽略 modifier 并记录警告，布局调整使用套件级 modifier                                                                                                                                                                     |
| Navigation containerColor | 原生没有导航容器色入口：`NavigationView` 的 `color` 是内容面板底色，导航容器来自窗口材质；显式 `containerColor` 被忽略并记录警告，不改写内容面板冒充支持                                                                                          |
| NomiScaffold              | 上游没有 Scaffold；用 FluentTheme 原生色板与 LocalContentColor/ProvideTextStyle 环境建立 Column 外壳，顶栏/主体/底栏顺序排布，主体占剩余高度                                                                                                      |

**不再 Layer + clickable 重绘按钮。** v0.1.0 原生 Button 不支持 size/shape/contentPadding， 非 null
覆盖会被忽略并记录警告，保留原生外观。原生普通按钮没有 Role.Button，adapter 不强加 role、不加第二个点击层；
语义和交互以独立原生调用为参照，而不是沿用旧重绘测试的 role/48dp 契约。

静态 Card 没有 colors/border 参数：显式颜色或 None/Stroke 描边被忽略并记录警告，需要这些能力时使用
NomiSurface。Default 表示保留原生描边，不等于 None。原生 Text 没有 autoSize 参数， 显式 autoSize 被忽略并记录
警告。 导航容器色同样没有原生入口：`NavigationView.color` 作用于内容面板，侧栏/顶栏底色来自窗口材质，因此显式
`containerColor` 被忽略并记录警告，而不是改错表面。这些限制由适配器降级和 `ciyin.platform.logger`
警告表达， 不静默丢弃用户输入。

导航项签名为
`NavigationItem(selected, onClick, modifier, icon, badge, label, colors, interactionSource)`
：icon、badge、label 为槽位，由调用方组合， 图标尺寸与无障碍名称也由调用方负责（中立 IconPack 固有尺寸为
256dp，槽内图标必须显式约束尺寸）。 Fluent 原生导航项没有 modifier 入口，项级 modifier 被忽略并记录警告；
颜色覆盖只作用于显式字段：未选中/选中内容色、禁用内容色与指示器色， 其余（填充色、悬停/按下/聚焦内容色、trailing
色）保留对应原生状态值。

按钮四色逐字段覆盖 default/hovered/pressed/focused/disabled；未指定通道及原生描边保持原样。
当前状态的显式内容色优先于 textStyle.color，再回退原生实际状态色；透明色是有效显式覆盖。

中立 9 个主题 data class 已公开构造；主题映射可直接构造，也可从 default 工厂取得实例后调用生成的成员
`copy`，无需 copy import。
默认值、必填参数与共享/新对象身份语义保持不变，详见[核心主题数据说明](<../design-system/README.md>)；
不恢复手写复制扩展。

## 验证入口

测试依赖按 [build.gradle.kts](<build.gradle.kts>)：`commonTest` 仅 `libs.kotlin.test`；`desktopTest`
额外 `compose.uiTest`、`compose.desktop.currentOs` 与 `compose.desktop.uiTestJUnit4`。 测试一律用
`NomiTheme(designSystem = NomiDesignSystem.Fluent, ...)` 或 `NomiDesignSystem.Fluent(map)`
显式提供实现（需导入 `ciyin.designsystem.fluent.Fluent`），不依赖已移除的隐式默认。

- [四组件原生对照](<src/desktopTest/kotlin/ciyin/designsystem/fluent/FluentComponentsTest.kt>)：独立
  raw FluentTheme，不经过 NomiTheme；精确尺寸、逐像素、完整 TextLayout、触摸启禁用、支持的显式覆盖。
- [五态颜色](<src/desktopTest/kotlin/ciyin/designsystem/fluent/FluentButtonColorsTest.kt>)
  ：全部变体和深浅色的逐通道覆盖、五态原生描边、文字颜色优先级。
- [组件降级渲染](<src/desktopTest/kotlin/ciyin/designsystem/fluent/FluentDegradationTest.kt>)：独立
  Compose 环境验证不支持的组件覆盖被忽略且不抛异常。
- [原生主题与继承](<src/desktopTest/kotlin/ciyin/designsystem/fluent/FluentNativeThemeTest.kt>)
  ：当前原生排版/圆角继承、A-B-A、typed 色板明暗、明暗切换与嵌套原生禁用按钮。
- [主题入口](<src/desktopTest/kotlin/ciyin/designsystem/fluent/FluentThemeEntryTest.kt>)
  验证零参数工厂安装、Primary 只换强调色、Full 八角色与三档圆角、自定义映射消费与入口主题原样发布。
- [主题映射](<src/commonTest/kotlin/ciyin/designsystem/fluent/FluentThemeMappingTest.kt>)
  覆盖中立到原生换算与三个预置映射的原生组声明；[外部 SPI 消费](<src/desktopTest/kotlin/consumer/ExternalApiConsumptionTest.kt>)
  在 adapter 外验证包装边界、公开映射函数与组件族替换。
- [独立主题数据消费](<src/desktopTest/kotlin/consumer/ThemeFactoryCopyConsumptionTest.kt>)：在非核心模块验证
  9 类公开构造、无扩展导入的成员 copy、默认值与身份语义。
- [导航套件](<src/desktopTest/kotlin/ciyin/designsystem/fluent/navigation/FluentNavigationTest.kt>)
  ：显示模式、三种布局的槽位组合（图标/徽标/文案）、页面内容位置、点击分发、折叠抽屉点击区域与项级 modifier
  忽略。

```powershell
.\gradlew.bat :core:design-system-fluent:compileCommonMainKotlinMetadata :core:design-system-fluent:compileAndroidMain --console=plain
.\gradlew.bat :core:design-system-fluent:desktopTest --console=plain
```

历史 internal 构造方案与 request/overrides 主题模型的结果不代表当前契约；当前映射函数模型须独立编译与回归。
设备、iOS/Web 与发布验收仍未完成。 Android Fluent 设备、iOS native/link、Web 构建仍需独立验收； Release 的
Fluent/haze/FileKit 与 Compose/Skia 缺失 API 问题仍保留，不使用 ignorewarnings。

## 相关资料

- [设计说明与验证矩阵](<../../.docs/design/design-system-模块化方案.md>)
- [设计系统技能](<../../.agents/skills/core-design-system/SKILL.md>)
- [上游 Fluent v0.1.0](<https://github.com/compose-fluent/compose-fluent-ui/tree/v0.1.0>)

未复用 ui-foundation 的 Material3 Button/Surface：它们会破坏 Fluent 主题隔离。这里直接调用 Fluent
原生组件，不复制手势或状态机。
