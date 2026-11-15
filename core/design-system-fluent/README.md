# :core:design-system-fluent

## 用途与边界

为中立设计系统提供 Compose Fluent **v0.1.0** 的原生默认优先适配。 统一调用入口，不统一外观；当前覆盖
Text、Button、Surface、独立原生非点击 Card、导航套件与列表项。 不负责 Mica、窗口生命周期、输入控件或其它二期组件。

- 使用仓库 `multiplatform-lib-targets`，Android 与按项目开关注册的 Desktop/iOS/JS/Wasm 共用
  commonMain。
- 不为本模块打开项目关闭的平台；sample commonMain 默认候选包含 Nomi/M3/Fluent。
- [构建](<build.gradle.kts>) api 导出中立核心与 Fluent；公开 typed 配置工厂签名使用 Fluent 类型。
- 原生类型不进入中立 SPI；普通业务只消费中立组件，专属原生配置留在装配层。
- 不添加全局双主题桥，不依据平台静默选择设计系统。

## 实现入口

- [FluentSystem](<src/commonMain/kotlin/ciyin/designsystem/fluent/FluentDesignSystem.kt>)：internal
  原生主题基线、typed 配置及能力声明，经 `NomiDesignSystem.Fluent`（导入
  `ciyin.designsystem.fluent.Fluent`）暴露。
- [主题投影](<src/commonMain/kotlin/ciyin/designsystem/fluent/FluentThemeMapping.kt>)
  ：稀疏强调色、原生八角色排版和中立只读投影。
- [组件装配](<src/commonMain/kotlin/ciyin/designsystem/fluent/FluentComponents.kt>)只组装六族。
- [Text](<src/commonMain/kotlin/ciyin/designsystem/fluent/text/FluentText.kt>)、[Button](<src/commonMain/kotlin/ciyin/designsystem/fluent/button/FluentButton.kt>)、[Surface](<src/commonMain/kotlin/ciyin/designsystem/fluent/surface/FluentSurface.kt>)、[Card](<src/commonMain/kotlin/ciyin/designsystem/fluent/card/FluentCard.kt>)
  分别实现原生组件调用。
- [按钮颜色](<src/commonMain/kotlin/ciyin/designsystem/fluent/button/FluentButtonColors.kt>)
  只覆盖原生五态中明确指定的通道。
- [导航套件](<src/commonMain/kotlin/ciyin/designsystem/fluent/navigation/FluentNavigationRenderer.kt>)
  把槽位式中立导航项与布局映射到原生 NavigationView，只在显式覆盖时改写原生五态颜色，不重建排版或尺寸配方。
- [列表项](<src/commonMain/kotlin/ciyin/designsystem/fluent/list/FluentListItemRenderer.kt>)
  使用原生 `CardExpanderItem` 的点击/无点击重载：标题注入 bodyStrong，尾部注入 caption 与 secondary
  内容色；
  `dropdown` 保持 null，不绘制展开箭头，也不映射 enabled/selected。

四组件分别归入 fluent 根下的 text/button/surface/card 子目录，package 与目录一致；FluentButtonColors 随
Button 进入 button。 主题、投影和四族装配留在原位置，Card 仍由 surfaces 派发，导航套件归入 fluent 根下的
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
的调用保留原生默认， 不会从中立投影反向填入 Nomi 排版、48dp、圆角或内边距。

## 主题输入与只读投影

新主题入口恢复 `colorScheme/typography/shapes/spacings/sizes` 直接参数，内部统一归入
`NomiThemeRequest` 的显式组；未指定时仍采用原生默认。 无 accent 时使用原生 `lightColors()` /
`darkColors()`；明确 accent 才生成完整 Shades，避免上游未知主色回落蓝色。
`rememberFluentDesignSystem(colors, typography, cornerRadius)` 可配置完整原生对象。 显式请求 accent
优先颜色配置中的强调色，但保留 typed Colors.darkMode；只改 accent
不会切换非强调色的深浅色。未指定时保留配置色板，排版与圆角不受强调色请求改写。

默认排版与圆角来自首次进入分支的真实上游 Fluent 环境，分支保存原生基线； A-B-A
局部切换不会从已覆盖的主题反复采样。八角色排版按语义投影为中立角色； spacings/sizes
是中立页面度量扩展，不用来重写原生控件默认。 直接 `typography` 将
bodySmall/bodyMedium/labelLarge/bodyLarge/titleLarge/headlineMedium/headlineLarge/displayLarge 分别映射到
caption/body/bodyStrong/bodyLarge/subtitle/title/titleLarge/display；十五角色读模型完整保留。 完整
`colorScheme` 与九级 `shapes` 无法由原生 API 表达，明确拒绝；使用专属 colors/cornerRadius 配置或
accent 增量。

根 NomiTheme 重置显式 slot 与继承颜色，只继承父层明确的非颜色主题组；NomiThemeOverride 继承全部明确覆盖，不传父
resolved 默认结果。组件通过 NomiTheme 只读 getter 读取五组结果。 adapter 只把明确的 colors/textStyle
增量传给中立 slot，原生默认留在 native locals。 默认 Card 内的原生按钮因此仍能提供自己的禁用颜色与字号。

## 原生能力与限制

| 中立调用                | Fluent 原生调用与限制                                                                                                                                                                                                                                                            |
|-------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| NomiText                | component.Text 双重载；部分 style 合入原生 LocalTextStyle；不支持 autoSize，非 null 明确拒绝                                                                                                                                                                                     |
| 默认 / Default Button   | component.Button                                                                                                                                                                                                                                                                 |
| Primary Button          | AccentButton                                                                                                                                                                                                                                                                     |
| Subtle Button           | SubtleButton                                                                                                                                                                                                                                                                     |
| Text Button             | HyperlinkButton                                                                                                                                                                                                                                                                  |
| Destructive Button      | Button + 显式 critical 配方                                                                                                                                                                                                                                                      |
| NomiSurface             | background.Layer；任意 shape、显式颜色与三态 border                                                                                                                                                                                                                              |
| NomiCard                | surface.Card 非点击重载；显式 shape 和内容 padding 可用                                                                                                                                                                                                                          |
| NomiNavigationSuite     | Bar/Drawer 用 component.NavigationView（Top/LeftCompact）；Rail 用公开 SideNav 组合左置外壳，底部项贴在 SideNav 下方自绘，因为上游 Left 布局会固定插入 footer 分割线并给内容面板加描边且都无关闭入口。图标/徽标/文案为调用方槽位，颜色只覆盖显式通道，排版与折叠动效沿用原生默认 |
| NavigationItem modifier | 原生没有项级修饰符入口，capabilities.navigationItemModifier = false；非默认 Modifier 明确拒绝，布局调整使用套件级 modifier                                                                                                                                                       |
| NomiScaffold            | 上游没有 Scaffold；用 FluentTheme 原生色板与 LocalContentColor/ProvideTextStyle 环境建立 Column 外壳，顶栏/主体/底栏顺序排布，主体占剩余高度                                                                                                                                     |

**不再 Layer + clickable 重绘按钮。** v0.1.0 原生 Button 不支持 size/shape/contentPadding， 任何非
null 覆盖都会明确失败。原生普通按钮没有 Role.Button，adapter 不强加 role、不加第二个点击层；
语义和交互以独立原生调用为参照，而不是沿用旧重绘测试的 role/48dp 契约。

静态 Card 没有 colors/border 参数：任何颜色字段非 Unspecified、border 为 None 或 Stroke 都明确失败，
需要这些能力时使用 NomiSurface。Default 表示保留原生描边，不等于 None。原生 Text 没有 autoSize 参数， 显式
autoSize 由文本 renderer 明确拒绝并体现在 capabilities.textAutoSize = false。 这些限制同时由
`capabilities` 和参数校验表达，不静默丢弃用户输入。

导航项签名为
`NavigationItem(selected, onClick, modifier, icon, badge, label, colors, interactionSource)`
：icon、badge、label 为槽位，由调用方组合， 图标尺寸与无障碍名称也由调用方负责（中立 IconPack 固有尺寸为
256dp，槽内图标必须显式约束尺寸）。 Fluent 原生导航项没有 modifier 入口，因此项级 modifier
明确失败；颜色覆盖只作用于显式字段：未选中/选中内容色、禁用内容色与指示器色，
其余（填充色、悬停/按下/聚焦内容色、trailing 色）保留对应原生状态值。

按钮四色逐字段覆盖 default/hovered/pressed/focused/disabled；未指定通道及原生描边保持原样。
当前状态的显式内容色优先于 textStyle.color，再回退原生实际状态色；透明色是有效显式覆盖。

中立 9 个主题 data class 已公开构造；主题投影可直接构造，也可从 default 工厂取得实例后调用生成的成员
`copy`，无需 copy import。
默认值、必填参数与共享/新对象身份语义保持不变，详见[核心主题数据说明](<../design-system/README.md#默认对象与复制>)
；不恢复手写复制扩展。

## 验证入口

上一轮删除主题聚合层时，metadata、Android 依赖编译及 40 项 Desktop
回归通过；新增直接适配器回调与真实外部模块扁平主题发布测试。联合回归与未验证范围见[设计说明](<../../.docs/design/design-system-模块化方案.md>)。

测试依赖按 [build.gradle.kts](<build.gradle.kts>)：`commonTest` 仅 `libs.kotlin.test`；`desktopTest`
额外 `projects.core.designSystemMaterial3`、`compose.uiTest`、`compose.desktop.currentOs` 与
`compose.desktop.uiTestJUnit4`。 测试一律用 `NomiTheme(designSystem = NomiDesignSystem.Fluent, ...)`
或 `NomiThemeOverride(designSystem = ...)` 显式提供实现（需导入 `ciyin.designsystem.fluent.Fluent`），
不依赖已移除的隐式默认。导航项槽位化后 metadata 编译通过，Desktop 回归 46 项通过（零失败、零跳过）。

- [四组件原生对照](<src/desktopTest/kotlin/ciyin/designsystem/fluent/FluentComponentsTest.kt>)：独立
  raw FluentTheme，不经过 NomiTheme；精确尺寸、逐像素、完整 TextLayout、触摸启禁用、支持的显式覆盖。
- [五态颜色](<src/desktopTest/kotlin/ciyin/designsystem/fluent/FluentButtonColorsTest.kt>)
  ：全部变体和深浅色的逐通道覆盖、五态原生描边、文字颜色优先级。
- [能力拒绝](<src/desktopTest/kotlin/ciyin/designsystem/fluent/FluentCapabilitiesTest.kt>)：独立
  Compose 环境验证每种不支持参数。
- [原生主题及嵌套](<src/desktopTest/kotlin/ciyin/designsystem/fluent/FluentNativeThemeTest.kt>)
  ：上游真实排版基线、A-B-A、typed 配置优先级、嵌套原生禁用按钮。
- [直接主题参数](<src/desktopTest/kotlin/ciyin/designsystem/fluent/FluentDirectThemeTest.kt>)
  验证八角色映射、完整读模型、嵌套规则及不支持组拒绝。
- [主题映射](<src/commonTest/kotlin/ciyin/designsystem/fluent/FluentThemeMappingTest.kt>)
  与[外部 SPI 消费](<src/desktopTest/kotlin/consumer/ExternalApiConsumptionTest.kt>)补充公开契约回归。
- [独立主题数据消费](<src/desktopTest/kotlin/consumer/ThemeFactoryCopyConsumptionTest.kt>)：在非核心模块验证
  9 类公开构造、无扩展导入的成员 copy、默认值与身份语义。
- [导航套件](<src/desktopTest/kotlin/ciyin/designsystem/fluent/navigation/FluentNavigationTest.kt>)
  ：显示模式与能力声明、三种布局的槽位组合（图标/徽标/文案）、页面内容位置、点击分发、折叠抽屉点击区域与项级
  modifier 拒绝。

```powershell
.\gradlew.bat :core:design-system-fluent:compileCommonMainKotlinMetadata :core:design-system-fluent:compileAndroidMain --console=plain
.\gradlew.bat :core:design-system-fluent:desktopTest --console=plain
```

历史 internal 构造方案当时有 27 项 Desktop 测试及 metadata、Android 依赖和 Desktop
宿主编译通过；该方案现已被公开构造与生成成员 copy 替代，旧测试结果不是本轮验证。设备、iOS/Web 与发布验收仍未完成。

历史记录：native-first 之前的跨平台迁移曾有 17 项 Desktop 测试及 sample/APK 构建通过；更早
Desktop-only 阶段也有 17 项测试。 这些不是当前原生默认重构的验证结果。前轮 WSA 仅覆盖 Nomi/M3，不证明
Android Fluent 已实机运行。 Android Fluent 设备、iOS native/link、Web 构建仍需独立验收；Release 的
Fluent/haze/FileKit 与 Compose/Skia 缺失 API 问题仍保留，不使用 ignorewarnings。

## 相关资料

- [设计说明与验证矩阵](<../../.docs/design/design-system-模块化方案.md>)
- [设计系统技能](<../../.agents/skills/core-design-system/SKILL.md>)
- [上游 Fluent v0.1.0](<https://github.com/compose-fluent/compose-fluent-ui/tree/v0.1.0>)

未复用 ui-foundation 的 Material3 Button/Surface：它们会破坏 Fluent 主题隔离。这里直接调用 Fluent
原生组件，不复制手势或状态机。