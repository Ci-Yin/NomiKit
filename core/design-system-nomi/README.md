# `:core:design-system-nomi`

## 用途与非目标

内置 Nomi 设计系统实现：默认 renderer、原生内容环境与 internal `DefaultNomiSystem` 装配；对外经
`NomiDesignSystem.Default` 暴露。 它把[中立核心](<../design-system/README.md>)的 SPI
落地为可直接使用的组件实现，并为后续基于 Material 3 的 Nomi 组件预留依赖位。

非目标：不定义公共组件 API、主题数据或 SPI（均在核心）；不承担 Material 3 / Fluent 的原生适配
（见 [M3 适配器](<../design-system-material3/README.md>)
与 [Fluent 适配器](<../design-system-fluent/README.md>)）； 不做应用装配或页面编排。

## 模块位置与依赖边界

位于 `core` 层，`api(projects.core.designSystem)` 与 `api(libs.compose.material3)`（后者用于导航套件的
`NavigationBar`/`NavigationRail`/`PermanentNavigationDrawer` 原生外壳与徽标锚点，其它组件仍只使用
Foundation）。 不依赖 M3/Fluent 适配器模块，也不被核心反向依赖。 核心不再引用本模块：
`LocalNomiDesignSystem` 无隐式默认，应用根、测试与 Preview 必须显式提供实现。

## 从哪里开始

| 入口                                                                                                                                                                                                                                                                                                                                                                                            | 职责                                                                                      |
|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------|
| [DefaultNomiSystem](<src/commonMain/kotlin/ciyin/designsystem/nomi/DefaultNomiDesignSystem.kt>)                                                                                                                                                                                                                                                                                                 | internal 内置实现，经 `NomiDesignSystem.Default` 暴露；按请求解析五组默认并只应用显式覆盖 |
| [LocalNomiNativeContentColor / LocalNomiNativeTextStyle](<src/commonMain/kotlin/ciyin/designsystem/nomi/NomiNativeContentStyle.kt>)                                                                                                                                                                                                                                                             | Nomi 自身解析环境；与公共显式 slot 分离                                                   |
| [DefaultNomiTextRenderer](<src/commonMain/kotlin/ciyin/designsystem/nomi/text/DefaultNomiTextRenderer.kt>) / [Button](<src/commonMain/kotlin/ciyin/designsystem/nomi/button/DefaultNomiActionRenderer.kt>) / [Surface](<src/commonMain/kotlin/ciyin/designsystem/nomi/surface/DefaultNomiSurfaceRenderer.kt>) / [Card](<src/commonMain/kotlin/ciyin/designsystem/nomi/card/DefaultNomiCard.kt>) | 四组件 Foundation 实现                                                                    |
| [DefaultNomiNavigationRenderer](<src/commonMain/kotlin/ciyin/designsystem/nomi/navigation/DefaultNomiNavigationRenderer.kt>)                                                                                                                                                                                                                                                                    | 导航套件实现；Bar/Rail/Drawer 使用槽位式导航项，Drawer 为永久抽屉                         |
| [DefaultNomiScaffoldRenderer](<src/commonMain/kotlin/ciyin/designsystem/nomi/scaffold/DefaultNomiScaffoldRenderer.kt>)                                                                                                                                                                                                                                                                          | 页面脚手架实现；顶栏/主体/底栏顺序排布，主体占剩余高度                                    |
| [DefaultNomiListItemRenderer](<src/commonMain/kotlin/ciyin/designsystem/nomi/list/DefaultNomiListItemRenderer.kt>)                                                                                                                                                                                                                                                                              | 列表项实现；透明行 + Nomi 令牌内边距、文本样式与尾部内容色                                |

## 最小接入路径

```kotlin
import androidx.compose.runtime.CompositionLocalProvider
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.runtime.LocalNomiDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme

CompositionLocalProvider(LocalNomiDesignSystem provides NomiDesignSystem.Default) {
    NomiTheme {
        // 业务内容；NomiText/NomiButton 等读取显式提供的实现
    }
}
```

应用根通常用 `NomiTheme(designSystem = NomiDesignSystem.Default)` 直接指定；扩展属性必须导入
`ciyin.designsystem.nomi.Default` 才能解析。 上例用于测试、Preview 或需要整棵子树固定 Nomi
实现的场景。未提供任何实现时，组件与 `NomiTheme` 抛出带指引的 `IllegalStateException`。

## 导航套件与导航项槽位

导航项使用核心 `NomiNavigationItemScope.NavigationItem` 的槽位形状：图标、徽标与文案由调用方组合，
适配器只负责把它们放进 Nomi 原生 Bar/Rail/Drawer 项，并解析外壳容器色与稀疏颜色覆盖。

```kotlin
NavigationItem(
    selected = selection == 0,
    onClick = { onNavigate(0) },
    modifier = Modifier, // 支持项级修饰符
    icon = { Icon(IconPack.Home, null, Modifier.size(24.dp)) }, // 图标尺寸与无障碍名由槽内容负责
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
- Drawer 使用 M3 `PermanentNavigationDrawer` 永久抽屉：文案槽与图标同排直接渲染，选中底色与内容色规则同上；
  抽屉不消费窗口 inset，宽度沿用 M3 原生永久抽屉范围（240–360dp）。
- 外壳容器色通过 `containerColor` 覆盖：Unspecified 回退 `surfaceLow`，`Transparent` 是有效覆盖；
  Bar/Rail/Drawer 三种布局都生效，项内颜色仍由 `NomiNavigationItemColors` 控制。
- Bar/Rail/Drawer 三种布局与容器色覆盖都可用；无法承载这些覆盖的其它系统由各自适配器降级，不影响 Nomi
  实现。

## 源码布局

- `src/commonMain/kotlin/ciyin/designsystem/nomi/`：装配、原生内容环境与七个组件子包的
  renderer（text/button/surface/card/navigation/scaffold/list）。
-

`src/commonTest`：[NomiNativeContentIsolationTest](<src/commonTest/kotlin/ciyin/designsystem/nomi/NomiNativeContentIsolationTest.kt>)
验证原生内容环境与公共显式 slot 隔离。

-

`src/desktopTest`：[NomiComponentsContractTest](<src/desktopTest/kotlin/ciyin/designsystem/NomiComponentsContractTest.kt>) / [NomiCustomizationContractTest](<src/desktopTest/kotlin/ciyin/designsystem/NomiCustomizationContractTest.kt>)
覆盖默认渲染、像素与定制契约（自核心迁入，包名不变）；[DefaultNomiNavigationTest](<src/desktopTest/kotlin/ciyin/designsystem/nomi/navigation/DefaultNomiNavigationTest.kt>)
覆盖导航套件的槽位渲染、选中语义、项级修饰符与 Drawer 抽屉渲染。

## 平台差异与生命周期

沿用仓库 `multiplatform-lib-targets` 约定；文本、按钮与表面族仅使用 Compose Runtime/Foundation，导航套件使用
`material3` 的 `NavigationBar`/`NavigationRail`/`PermanentNavigationDrawer` 作为原生外壳，无平台专属代码。
renderer 为 internal 单例，状态经组合本地提供，不持有跨组合状态。

## 依赖与扩展边界

新增 Nomi 组件实现放本模块对应组件子包；需要 M3 原生组件时直接依赖 `libs.compose.material3`， 不依赖
M3 适配器模块。公共 API 变更仍归核心；本模块只实现既有 SPI。

## 限制与常见误用

- 不要把 `LocalNomiNative*` 当作公共显式 slot；跨系统增量使用核心
  `LocalNomiContentColor/LocalNomiTextStyle`。
- 不要在核心模块引用本模块符号；核心保持零实现依赖。
- 未提供设计系统时读取组件会明确失败，不要用 try-catch 掩盖缺省装配问题。
- 不要尝试 `import ciyin.designsystem.nomi.DefaultNomiSystem`（internal）；统一通过
  `NomiDesignSystem.Default` 取得实例，并导入 `ciyin.designsystem.nomi.Default`。

## 验证入口

本轮 `compileCommonMainKotlinMetadata` 通过，Desktop 回归 23 项（`NomiComponentsContractTest`、
`NomiCustomizationContractTest`、`NomiNativeContentIsolationTest`、`DefaultNomiNavigationTest`
（含 Bar/Rail/Drawer）、`DefaultNomiListItemTest` 与 `DefaultNomiScaffoldTest`），零失败、零跳过；
与其它模块的联合回归见设计说明。

```powershell
.\gradlew.bat :core:design-system-nomi:compileCommonMainKotlinMetadata --console=plain
.\gradlew.bat :core:design-system-nomi:desktopTest --console=plain
```

核心 `:core:design-system` 已不再包含 Nomi 默认渲染回归，只保留公共契约测试；两模块须分别验证，不能以核心结果代替本模块结果。

## 相关资料

- [模块 skill](<../../.agents/skills/core-design-system/SKILL.md>)
- [设计说明](<../../.docs/design/design-system-模块化方案.md>)