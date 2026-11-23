# `:core:design-system-nomi`

## 用途与非目标

内置 Nomi 设计系统实现：默认 renderer、原生内容环境与 internal `DefaultNomiSystem` 装配；对外经
`NomiDesignSystem.Default` 暴露。 它把[中立核心](<../design-system/README.md>)的 SPI
落地为可直接使用的组件实现，并为需要原生工具箱外壳的组件预留依赖位。

非目标：不定义公共组件 API、主题数据或 SPI（均在核心）；不承担其它设计系统的原生适配（由各自 adapter
模块负责）； 不做应用装配或页面编排。

## 模块位置与依赖边界

位于 `core` 层，`api(projects.core.designSystem)` 与 `api(libs.compose.material3)`（后者用于导航套件的
`NavigationBar`/`NavigationRail`/`PermanentNavigationDrawer` 原生外壳与徽标锚点，其它组件仍只使用
Foundation）。 不依赖任何 adapter 模块，也不被核心反向依赖。 核心不再引用本模块：
`LocalNomiDesignSystem` 无隐式默认，应用根、测试与 Preview 必须显式提供实现。

## 从哪里开始

| 入口                                                                                                                                                                                                                                                                                                                                                                                                         | 职责                                                                                           |
|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------|
| [DefaultNomiSystem](<src/commonMain/kotlin/ciyin/designsystem/nomi/DefaultNomiDesignSystem.kt>)                                                                                                                                                                                                                                                                                                              | internal 内置实现，经 `NomiDesignSystem.Default` 暴露；发布传入主题并派生原生内容色与正文排版  |
| [LocalNomiNativeContentColor / LocalNomiNativeTextStyle](<src/commonMain/kotlin/ciyin/designsystem/nomi/NomiNativeContentStyle.kt>)                                                                                                                                                                                                                                                                          | Nomi 自身原生内容环境；与公共显式 slot 分离                                                    |
| [DefaultNomiTextRenderer](<src/commonMain/kotlin/ciyin/designsystem/nomi/text/DefaultNomiTextRenderer.kt>) / [Button / IconButton](<src/commonMain/kotlin/ciyin/designsystem/nomi/button/DefaultNomiActionRenderer.kt>) / [Surface](<src/commonMain/kotlin/ciyin/designsystem/nomi/surface/DefaultNomiSurfaceRenderer.kt>) / [Card](<src/commonMain/kotlin/ciyin/designsystem/nomi/card/DefaultNomiCard.kt>) | 四组件 Foundation 实现                                                                         |
| [DefaultNomiNavigationRenderer](<src/commonMain/kotlin/ciyin/designsystem/nomi/navigation/DefaultNomiNavigationRenderer.kt>)                                                                                                                                                                                                                                                                                 | 导航套件实现；Bar/Rail/Drawer 使用槽位式导航项，Drawer 为永久抽屉                              |
| [DefaultNomiScaffoldRenderer](<src/commonMain/kotlin/ciyin/designsystem/nomi/scaffold/DefaultNomiScaffoldRenderer.kt>)                                                                                                                                                                                                                                                                                       | 页面脚手架与顶栏实现；脚手架顶栏/主体/底栏顺序排布、主体占剩余高度，顶栏取 appBar 令牌（56dp） |
| [DefaultNomiListItemRenderer](<src/commonMain/kotlin/ciyin/designsystem/nomi/list/DefaultNomiListItemRenderer.kt>)                                                                                                                                                                                                                                                                                           | 列表项实现；透明行 + Nomi 令牌内边距、文本样式与尾部内容色                                     |

## 最小接入路径

```kotlin
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme

NomiTheme(designSystem = NomiDesignSystem.Default) {
    // 业务内容；NomiText/NomiButton 等读取显式提供的实现
}
```

应用根通常用 `NomiTheme(designSystem = NomiDesignSystem.Default)` 直接指定，默认跟随系统；扩展属性必须导入
`ciyin.designsystem.nomi.Default` 才能解析。固定明暗用 `darkMode = DarkMode.Dark`，局部覆盖主题时嵌套
`NomiTheme(designSystem = ..., theme = ...)`，每次进入都会重置显式内容 slot。 未进入主题边界时，组件由
`requireNomiDesignSystem()` 抛出带指引的 `IllegalStateException`。

## 主题与原生内容环境

内置实现的 `ProvideTheme(theme, content)` 直接发布调用方传入的完整 `NomiTheme`：组合局部
`LocalNomiTheme` 即为该值，`LocalNomiNativeContentColor` 取 `theme.colorScheme.textPrimary`，
`LocalNomiNativeTextStyle` 取 `theme.typography.bodyMedium`，不做字段级合并或明暗推断。
`NomiTheme.isDark` 是已解析布尔；跟随系统由主题入口在省略 `theme` 时解析。

## 导航套件与导航项槽位

导航项使用核心 `NomiNavigationItemScope.NavigationItem` 的槽位形状：图标、徽标与文案由调用方组合，
适配器只负责把它们放进 Nomi 原生 Bar/Rail/Drawer 项，并解析外壳容器色与稀疏颜色覆盖。

```kotlin
NavigationItem(
    selected = selection == 0,
    onClick = { onNavigate(0) },
    modifier = Modifier, // 支持项级修饰符
    icon = { NomiIcon(IconPack.Home, null, Modifier.size(24.dp)) }, // 图标尺寸与无障碍名由槽内容负责
    badge = { NomiText("9+") }, // 经 BadgedBox 叠加在图标槽上
    label = { NomiText("首页") }, // Bar 与 Drawer 直接渲染；Rail 作为右侧 tooltip 提示，不进入可见内容
    colors = NomiNavigationItemColors(), // 稀疏覆盖，Unspecified 回退 primary/onBackground
)
```

- Bar 只渲染主导航项：复刻既有底部栏在分组占位项处终止的行为，`footerItems` 不在 Bar 中展示，只由 Rail
  与 Drawer 承载；文案槽直接渲染，槽内容经 `ProvideNomiContentStyle` 继承导航项内容色，`NomiText`
  随选中状态变色。
- Rail 只显示图标与徽标：文案槽作为右侧 tooltip 在悬停时由原生 `TooltipBox` 展示，不进入可见内容，也不套用
  `NomiTheme` 排版。
- `colors.indicatorColor` 在 Rail 与 Drawer 的选中容器底色生效；Bar 没有独立指示器。
- 槽位 API 没有 `enabled`：本实现不提供禁用外观，`colors.disabledContentColor` 不生效；
  需要禁用语义时由调用方在点击回调与项级 `modifier` 上自行表达。
- Drawer 使用原生 `PermanentNavigationDrawer` 永久抽屉：文案槽与图标同排直接渲染，选中底色与内容色规则同上；
  抽屉不消费窗口 inset，宽度沿用原生永久抽屉范围（240–360dp）。
- 外壳容器色通过 `containerColor` 覆盖：Unspecified 回退 `surfaceLow`，`Transparent` 是有效覆盖；
  Bar/Rail/Drawer 三种布局都生效，项内颜色仍由 `NomiNavigationItemColors` 控制。
- Bar/Rail/Drawer 三种布局与容器色覆盖都可用；无法承载这些覆盖的其它系统由各自适配器降级，不影响 Nomi
  实现。

## 源码布局

- `src/commonMain/kotlin/ciyin/designsystem/nomi/`：装配、原生内容环境与十二个组件子包的
  renderer（text/button/surface/card/navigation/scaffold/list/icon/divider/control/feedback/menu）。
-

`src/commonTest`：[NomiNativeContentIsolationTest](<src/commonTest/kotlin/ciyin/designsystem/nomi/NomiNativeContentIsolationTest.kt>)
验证原生内容环境与公共显式 slot 隔离。

-

`src/desktopTest`：[NomiComponentsContractTest](<src/desktopTest/kotlin/ciyin/designsystem/NomiComponentsContractTest.kt>) / [NomiCustomizationContractTest](<src/desktopTest/kotlin/ciyin/designsystem/NomiCustomizationContractTest.kt>)
覆盖默认渲染、像素与定制契约（自核心迁入，包名不变）；[DefaultNomiNavigationTest](<src/desktopTest/kotlin/ciyin/designsystem/nomi/navigation/DefaultNomiNavigationTest.kt>)
覆盖导航套件的槽位渲染、选中语义、项级修饰符与 Drawer 抽屉渲染。

## 图标、分割线、控件与提示

- 图标使用 `Icon` 绘制，着色顺序为显式 tint > `LocalNomiNativeContentColor` >
  `NomiTheme.colorScheme.textPrimary`； 尺寸沿用矢量固有尺寸（内置 IconPack 统一 24dp）或调用方
  `Modifier.size`。
- 分割线按方向使用 `HorizontalDivider` / `VerticalDivider`：粗细默认 `NomiTheme.sizes.strokes.thin`，
  颜色默认 `NomiTheme.colorScheme.divider`。
- 选择控件与文本输入框都复用原生 `Switch` / `Checkbox` / `RadioButton` / `TextField` 作为交互载体，
  配色全部显式取自 `NomiTheme.colorScheme`（Nomi 不安装外层原生主题，不读宿主主题）；输入框属于文本族，
  `NomiTextFieldVariant.Outlined` 使用 `OutlinedTextField`，其余使用填充入口。
- 提示浮层使用原生 `TooltipBox` 与 `PlainTooltip`，容器 `surfaceHigher`、内容 `textPrimary`、 圆角
  `NomiTheme.shapes.small`。

## 图标按钮、开关按钮、下拉按钮、组合框与菜单

- 四个按钮入口共用同一套 Foundation 绘制：`Button` / `IconButton` / `ToggleButton` / `DropDownButton`
  只在状态色、 开关语义、是否追加下拉箭头与是否使用方形图标度量上分叉；容器、描边、内边距仍由同一份令牌决定。
- 开关按钮选中时切换到 `Primary` 配色并发布 `toggleableState` 语义；`iconOnly` 使用方形内边距与 48dp
  最小交互目标。
- 图标按钮使用 48dp 方形最小触摸目标、`spacings.medium` 内边距与 `shapes.small` 圆角；未指定内容色时
  继承当前槽位，禁用时回落 `textDisabled`。
- 下拉按钮在内容之后追加 `IconPack.ChevronDown`，着色跟随当前按钮内容色；不持有展开状态。
- 菜单浮层使用原生 `Popup` 锚定父布局：优先出现在锚点下方，空间不足时翻转到上方；容器 `surfaceHigher`、
  描边 `outline`、圆角 `NomiTheme.shapes.medium`，阴影 8dp。
- 菜单项是透明行 + 悬停/按下叠加，选中项在内容之前追加 `IconPack.Check`；`trailing` 提供时靠右。
- 组合框收起态是描边容器（`surface` + `outline`，最小高度取按钮最小交互目标），展开时复用同一个菜单浮层
  并按容器宽度对齐。

## 滑块与进度

- 滑块由 Nomi 自身用 Foundation 绘制，外观对齐应用滑块的既有形态：轨道高 16dp 的胶囊，已选段 `primary`、
  未选段 `surfaceHigher`；拇指为 15dp 圆形，`onPrimary` 填充 + `primary` 2dp 描边；轨道两端对齐拇指边缘，
  整体高度 30dp。刻度画刷透明（视觉上不显示刻度），`steps` 只影响点按吸附与进度语义。
- 拖拽、点按、刻度吸附与 RTL 位于 `nomi/control/slider/NomiSliderInteraction.kt`；尺寸与色板在
  `NomiSliderStyle.kt`；拇指与轨道绘制在 `NomiSliderDrawing.kt`；`SubcomposeLayout` 先测量拇指再定轨道，
  编排位于 `NomiSliderContent.kt`。
- 滑块发布 `ProgressBarRangeInfo` 与 `setProgress` 语义，读屏可以调整数值；禁用时所有颜色通道降到 38%
  透明度，只绘制、不安装手势。
- 进度指示器用 Canvas 绘制：线性是 16dp 胶囊轨道，与滑块轨道同高同色（进度 `primary`、轨道
  `surfaceHigher`）；环形保持 32dp / 描边 3dp，只用同一套配色。确定态发布进度值，不确定态使用循环动画
  并只发布进度条角色。
- 来源：滑块移植自 SmartToolFactory Compose-Colorful-Sliders（Apache-2.0，保留版权头）；画刷、描边与
  刻度的绘制路径保留在实现内部，公共入口不暴露颜色、尺寸、拇指槽位与标签变体；进度由原
  `core/ui-foundation` 的多段进度实现整理而来，多段模型不再保留。

## 平台差异与生命周期

沿用仓库 `multiplatform-lib-targets` 约定；文本、按钮与表面族仅使用 Compose Runtime/Foundation，导航套件使用
原生工具箱的 `NavigationBar`/`NavigationRail`/`PermanentNavigationDrawer` 作为原生外壳，无平台专属代码。
renderer 为 internal 单例，状态经组合本地提供，不持有跨组合状态。

## 依赖与扩展边界

新增 Nomi 组件实现放本模块对应组件子包；需要原生工具箱组件时直接依赖 `libs.compose.material3`，不依赖
任何 adapter 模块。公共 API 变更仍归核心；本模块只实现既有 SPI。

## 限制与常见误用

- 不要把 `LocalNomiNative*` 当作公共显式 slot；跨系统增量使用核心
  `LocalNomiContentColor/LocalNomiTextStyle`。
- 不要在核心模块引用本模块符号；核心保持零实现依赖。
- 未提供设计系统时读取组件会明确失败，不要用 try-catch 掩盖缺省装配问题。
- 不要尝试 `import ciyin.designsystem.nomi.DefaultNomiSystem`（internal）；统一通过
  `NomiDesignSystem.Default` 取得实例，并导入 `ciyin.designsystem.nomi.Default`。

## 验证入口

主题 API 迁移后请执行以下任务；Desktop 回归覆盖 `NomiComponentsContractTest`、
`NomiCustomizationContractTest`、`NomiNativeContentIsolationTest`、`DefaultNomiNavigationTest`
（含 Bar/Rail/Drawer）、`DefaultNomiListItemTest`、`DefaultNomiScaffoldTest`、`NomiAtomsTest`、
`NomiControlsTest` 与 `NomiTooltipTest`。模块结果不能以核心结果代替；本轮迁移由主集成流程统一验证。

```powershell
.\gradlew.bat :core:design-system-nomi:compileCommonMainKotlinMetadata --console=plain
.\gradlew.bat :core:design-system-nomi:desktopTest --console=plain
```

核心 `:core:design-system` 已不再包含 Nomi 默认渲染回归，只保留公共契约测试；两模块须分别验证，不能以核心结果代替本模块结果。

## 相关资料

- [模块 skill](<../../.agents/skills/core-design-system/SKILL.md>)
- [设计说明](<../../.docs/design/design-system-模块化方案.md>)