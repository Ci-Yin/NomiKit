# :core:design-system-material3

Material 3 设计系统适配器，将中立主题、六类组件（含列表项）映射到官方 Compose Material 3。

## 职责与边界

- 依赖 [中立核心](<../design-system/README.md>)，不依赖旧 material、ui-foundation 或应用模块。
- Material 3 由 Version Catalog 的既有 `libs.compose.material3` 提供。
- 六个 SPI 族实现 Text、Button、Surface、独立 Card、导航套件、页面脚手架与列表项；各组件分别调用官方原生组件，不共享外观配方。
- 导航套件直接使用原生 NavigationBar、NavigationRail 与 PermanentNavigationDrawer，Bar/Rail/Drawer
  均声明支持；外壳容器色可通过 `containerColor` 覆盖（Bar/Rail 未指定回退 `surfaceLow`，Drawer 未指定保留
  原生 `DrawerDefaults.standardContainerColor`）。
- 输入框、图标按钮、弹窗等属于二期，不提供空实现或占位 SPI。
- 不引入 Fluent、不读取平台设置、不维护第二份设计令牌。

## 入口

- `NomiDesignSystem.Material3`（导入 `ciyin.designsystem.material3.Material3`）：原生默认实现，按明暗模式选择原生
  palette，并使用固定原生 Typography/Shapes 基线。 实现对象 `Material3System` 为
  internal，只经该扩展属性暴露；无兼容别名。
- `rememberMaterial3DesignSystem(...)`：配置完整 Material ColorScheme/Typography/Shapes，以及中立间距与尺寸。
- `rememberLegacyMaterial3DesignSystem(...)`：仅旧门面适配使用的直接颜色、原生
  Typography、形状、间距和尺寸配置，不是中立核心的聚合主题输入。
- [主题转换](<src/commonMain/kotlin/ciyin/designsystem/material3/Material3Conversions.kt>)
  ：公开颜色、圆角、排版转换，供适配器和旧包兼容层复用。
- 组件位于 material3
  根下的 [text](<src/commonMain/kotlin/ciyin/designsystem/material3/text/Material3Text.kt>)、[button](<src/commonMain/kotlin/ciyin/designsystem/material3/button/Material3Button.kt>)、[surface](<src/commonMain/kotlin/ciyin/designsystem/material3/surface/Material3Surface.kt>)、[card](<src/commonMain/kotlin/ciyin/designsystem/material3/card/Material3Card.kt>)、[navigation](<src/commonMain/kotlin/ciyin/designsystem/material3/navigation/Material3NavigationRenderer.kt>)、[scaffold](<src/commonMain/kotlin/ciyin/designsystem/material3/scaffold/Material3ScaffoldRenderer.kt>)、[list](<src/commonMain/kotlin/ciyin/designsystem/material3/list/Material3ListItemRenderer.kt>)
  子目录，package 同步细分；组件专属辅助函数随文件迁移。
  主题、转换、投影与[六族装配](<src/commonMain/kotlin/ciyin/designsystem/material3/Material3Components.kt>)
  留在原位置；Card 仍属于 surfaces SPI，导航套件归入 navigation 子目录，不保留旧包兼容转发。

```kotlin
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.material3.Material3 // 提供 NomiDesignSystem.Material3
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme

NomiTheme(designSystem = NomiDesignSystem.Material3) {
    NomiButton(onClick = {}) {
        NomiText("示例")
    }
}
```

## 原生默认与显式覆盖

NomiTheme 只接受 `NomiThemeOverrides` 作为本层显式输入：darkMode、designSystem、overrides 与 content。
五组可空 字段的 null 表示本层未指定；新根继承父层明确设置的排版/形状/间距/尺寸，但不继承父颜色，按当前系统和模式重选。
默认 designSystem 继承当前上下文，且必须已由祖先显式提供（无隐式回退，未提供时入口报错）；darkMode
同样继承当前上下文；不把父级 resolved 原生默认重新写成显式输入。 NomiThemeOverride 继承全部明确覆盖；子层完整色板整组
替换继承颜色。 颜色覆盖只经完整的 `NomiColorScheme` 表达，不提供单色强调色入口。

完整中立颜色/排版/形状覆盖作用于当前 Material 基线；颜色通过 base.copy 保留 tertiary、container
等未映射原生角色， 排版覆盖十五个公开角色并保留原生额外强调角色。投影最终原样保留显式完整组，不丢
warning/success/info 或九级形状。 不传覆盖时原生默认不变。完整原生配置仍通过
rememberMaterial3DesignSystem 传入，不从祖先 MaterialTheme 循环采样。

未传 variant 的 NomiButton 等价原生 Button；显式 Default 映射 FilledTonalButton，Text/Subtle 映射
TextButton， Destructive 在原生 Button 上选用 error/onError。四色均为可缺省覆盖，不填充中立默认。 只有显式
size 才应用中立 metrics；显式 shape、contentPadding 优先，textStyle 按字段覆盖 size 的排版，默认没有统一
48dp/padding/shape。 按钮当前状态显式 contentColor/disabledContentColor 优先于显式
textStyle.color，后者再优先原生实际内容色； 只有调用方明确指定的颜色和排版增量才写入中立
slot；未指定时不重造禁用态色，也不把原生默认值伪装成显式覆盖。

NomiText 的部分 TextStyle 合入原生 LocalTextStyle；null 保持原生 slot 排版。 Button/Surface/Card
保持原生实际内容环境；中立桥只传播显式 colors/textStyle/size 增量。 因此默认 Card/Surface 内的 raw
NativeButton 可自行提供禁用颜色和按钮排版，局部切换设计系统也不携带父原生默认。 Surface 默认矩形；Card
调用原生 Card，默认无内边距。 NomiBorder.Default/None/Stroke 区分默认、无描边、显式描边。组件级 shape
可为任意 Shape。

## 只读投影与兼容边界

实际 Material 主题先建立，再通过 `ProvideTheme` 回调投影为 `ResolvedNomiTheme`。颜色和十五个排版角色直接从原生读取；
warning/success/info、原生没有对应的形状等级、间距和尺寸是明确的核心扩展，不反向填回组件默认。 主题级
Shapes 的五个公开角色必须都是 RoundedCornerShape：中立 NomiShapes/旧门面读模型只能表示圆角， 其他形状会立即抛出
IllegalArgumentException，不静默将 CutCornerShape 变成圆角。组件级形状不受此限制。

旧门面保留既有颜色及 huge → Material extraLarge 的映射；新默认实现不使用这条旧映射。 公开
toMaterial3Typography (base) 通过 base.copy 保留额外内部角色；旧入口直接保留完整 Typography 原实例。

## 平台与依赖

使用仓库 `multiplatform-lib-targets` 约定，平台开关与核心保持一致。生产实现位于 commonMain， 不含
Android/Desktop/iOS 专有 API。api 导出中立核心与 Material 3；公开转换函数签名需要后者。

测试依赖是单向测试边：`commonTest` 额外 `implementation(projects.core.designSystemNomi)`，只在测试源码集生效；
生产 `commonMain` 仍只有核心与 Material 3，不依赖 Nomi 实现模块，也不形成 adapter 之间的生产依赖。
测试统一以 `NomiTheme(designSystem = NomiDesignSystem.Material3)` 或
`rememberMaterial3DesignSystem(...)` 显式传入实现（需导入 `ciyin.designsystem.material3.Material3`），
不依赖已移除的隐式默认。

## 验证

当前 Desktop 回归 33 项；本轮 metadata 编译通过，Desktop 回归零失败、零跳过。联合回归和未验证范围见
[设计说明](<../../.docs/design/design-system-模块化方案.md>)。

[映射测试](<src/commonTest/kotlin/ciyin/designsystem/material3/Material3ConversionsTest.kt>)
覆盖十五排版角色往返、表面层级及旧圆角映射。
[导航测试](<src/commonTest/kotlin/ciyin/designsystem/material3/navigation/Material3NavigationTest.kt>)
覆盖 Bar/Rail/Drawer 三种原生布局、选中与点击语义与分组顺序。
[脚手架测试](<src/commonTest/kotlin/ciyin/designsystem/material3/Material3ScaffoldTest.kt>)
覆盖顶栏/主体/底栏顺序与显式颜色透传。
[组件测试](<src/commonTest/kotlin/ciyin/designsystem/material3/Material3ComponentsTest.kt>)
覆盖五种按钮变体、三种尺寸、启用/禁用状态，以及原生 Material 按钮中的中立文字实际布局颜色； 同时检查
Surface 与 Card 的内容颜色继承。

建议由主集成流程执行：

```powershell
.\gradlew.bat :core:design-system-material3:desktopTest --console=plain
```

历史记录：2026-11-11 native-first 改造前的 commonMain/Desktop 编译及 6 项测试通过；不是本轮验证结果。
此前新增[原生主题测试](<src/commonTest/kotlin/ciyin/designsystem/material3/Material3NativeThemeTest.kt>)
与
[独立原生对照测试](<src/desktopTest/kotlin/ciyin/designsystem/material3/Material3NativeParityTest.kt>)
，验证默认主题、 跨系统稀疏覆盖、尺寸、像素及部分样式/颜色。原生参照使用独立 MaterialTheme，不经过
NomiTheme。 前一轮 native-first 的 19 项原生主题、组件对照、显式覆盖与 slot 来源回归已通过；不是主题覆盖输入的复验。
此前新增[主题覆盖参数测试](<src/commonTest/kotlin/ciyin/designsystem/material3/Material3ThemeParametersTest.kt>)，
覆盖五组映射、父明确排版继承与颜色重选、原生额外角色保留、完整组投影及局部恢复。此前主题参数改造的 23 项
Desktop 测试、metadata/Android 依赖与 Desktop 宿主编译记录见设计说明。

## 相关资料

- [模块设计方案](<../../.docs/design/design-system-模块化方案.md>)
- [旧包兼容模块](<../material/README.md>)
