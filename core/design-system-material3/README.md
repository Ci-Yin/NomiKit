# :core:design-system-material3

Material 3 设计系统适配器：把中立 `NomiTheme` 经映射函数安装为官方 Compose Material 3 原生主题，
并实现文本、按钮、表面、导航、脚手架与列表项六族组件 SPI。

## 职责与边界

- 依赖[中立核心](<../design-system/README.md>)，不依赖旧 material、ui-foundation 或应用模块。
- Material 3 由 Version Catalog 的既有 `libs.compose.material3` 提供。
- 六个 SPI 族分别调用官方原生组件，不共享外观配方；输入框、图标按钮、弹窗等属于二期，不提供占位实现。
- 不读取系统明暗或平台设置，不维护第二份设计令牌，也不从祖先 MaterialTheme 采样原生配置。
- 导航套件直接使用原生 NavigationBar、NavigationRail 与 PermanentNavigationDrawer；外壳容器色可通过
  `containerColor` 覆盖（Bar/Rail 未指定回退 `MaterialTheme.colorScheme.surfaceContainerLow`，Drawer
  未指定 保留原生 `DrawerDefaults.standardContainerColor`）。

## 主题入口

主题输入只有中立 `NomiTheme`，适配器用纯函数把它翻译成 Material 3 原生组。主题入口
`NomiTheme(designSystem, darkMode, theme, content)` 自己发布 `LocalNomiTheme` 并重置显式内容 slot；
`NomiTheme.colorScheme` 等伴生 getter 始终读取本层显式传入值，不是原生主题的解析结果。适配器只安装原生主题，原生视觉由组件
直接读取 `MaterialTheme`，不做 native 到中立的反向投影。

```kotlin
@Immutable
data class Material3Theme(
    val isDark: Boolean = false,
    val colorScheme: ColorScheme? = null,
    val typography: Typography? = null,
    val shapes: Shapes? = null,
)
```

各可空组为 null 时：色板按 `isDark` 取原生默认，排版与形状继承当前 `MaterialTheme`；不回退到中立主题的
同组字段。映射函数因此显式决定"哪些中立组参与原生主题"。`isDark` 是已解析布尔，系统状态由应用层提前解析。

预置映射：

| 映射                        | 原生色板                                          | 原生排版 | 原生形状     | 说明                               |
|-----------------------------|---------------------------------------------------|----------|--------------|------------------------------------|
| `toNativeMaterial3Theme()`  | 按模式取 `lightColorScheme()`/`darkColorScheme()` | 当前原生 | 当前原生     | 只传递明暗模式                     |
| `toPrimaryMaterial3Theme()` | 原生默认色板只替换 `primary`                      | 当前原生 | 当前原生     | onPrimary、tertiary 等保持原生默认 |
| `toFullMaterial3Theme()`    | 整组映射可对应的中立颜色角色                      | 十五角色 | 五个标准等级 | 未映射的扩展角色不参与             |

工厂：

- `NomiDesignSystem.Material3`：使用原生默认映射的共享单例，可反复读取且引用相同。
- `NomiDesignSystem.Material3(map: NomiTheme.() -> Material3Theme)`：按给定接收者映射返回实现；默认
  `toNativeMaterial3Theme()` 与属性共用同一共享实例，其他映射新建。工厂不组合、不记忆映射结果，需要稳定身份时
  由调用方在组合外保存或使用 `remember`。

`ProvideTheme(theme, content)` 依次执行：

1. 调用 `map(theme)` 得到 `Material3Theme`；
2. 缺省组回退：色板按 `isDark` 选择原生默认，排版与形状继承当前 `MaterialTheme`；
3. 安装 `MaterialTheme(colorScheme, typography, shapes)`；
4. 建立内容环境：显式内容 slot 色优先，未指定时不重造原生默认；显式排版增量按字段合入；
5. 组合 `content`，不发布或回写中立主题。

```kotlin
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.material3.Material3
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme

val designSystem = NomiDesignSystem.Material3
NomiTheme(designSystem = designSystem) {
    NomiButton(onClick = {}) {
        NomiText("示例")
    }
}
```

自定义映射只在需要组合原生配置时使用，不提供旧版参数化工厂：

```kotlin
import ciyin.designsystem.material3.Material3Theme
import ciyin.designsystem.material3.toMaterial3ColorScheme
import ciyin.designsystem.material3.toMaterial3Typography

val branded: NomiTheme.() -> Material3Theme = {
    Material3Theme(
        isDark = isDark,
        colorScheme = colorScheme.toMaterial3ColorScheme(isDark),
        typography = typography.toMaterial3Typography(),
    )
}
val designSystem = NomiDesignSystem.Material3(branded)
```

## 映射与转换

[映射与转换](<src/commonMain/kotlin/ciyin/designsystem/material3/Material3Mapper.kt>)包含三个预置映射扩展与
中立 → Material 3 的转换：

- `NomiColorScheme.toMaterial3ColorScheme(isDark: Boolean)`：以对应原生默认色板为基线覆盖可映射角色；
  `(base)` 只覆盖角色，保留 base 的 tertiary、container 等未映射配置。
- `NomiShapes.toMaterial3Shapes()`：五个标准等级，最大等级 `extraLarge` 取中立 `huge`。
- `NomiTypography.toMaterial3Typography(base = Typography())`：十五个公开角色，base 的原生专属角色保留。

同文件保留纯转换 `Typography.toNomiTypography()`：只把原生十五个公开角色导入中立排版，供旧 material
门面等调用方显式使用；它不读取主题环境、不自动发布结果，因此不是反向投影。`ExtraLarge ↔ huge` 互为逆映射；
中立九级中未参与映射的等级、颜色扩展与文本派生色不会进入原生组件默认。适配器不再提供中立色板与形状的
反向投影函数，也没有 `LocalNomiTheme` 发布或回写。

## 组件适配

- `NomiText` 的部分 `TextStyle` 合入原生 `LocalTextStyle`；null 保持原生 slot 排版，未指定颜色交给原生内容环境。
- `NomiButton` 未传 variant 等价原生 Button；`Default` 映射 FilledTonalButton，`Text`/`Subtle` 映射
  TextButton，`Destructive` 在原生 Button 上选用 error/onError。四色均为可缺省覆盖，不填充中立默认；
  只有显式 `size` 才应用中立 metrics，显式 shape、contentPadding 优先，textStyle 按字段覆盖 size 排版。
  当前状态显式 contentColor/disabledContentColor 优先于 textStyle.color，后者再优先原生实际内容色。
- `NomiSurface` 默认矩形表面并保留原生内容色推导；`NomiCard` 调用原生 Card，默认无额外内边距；
  `NomiBorder` 区分默认、无描边与显式描边；组件级 shape 可为任意 Shape。
- Button/Surface/Card 保持原生实际内容环境，中立桥只传播显式 colors/textStyle/size 增量；
  默认容器不会把原生禁用色或原生排版伪装成显式覆盖。
- 导航套件使用原生 NavigationBar、NavigationRail、PermanentNavigationDrawer，Bar/Rail/Drawer 均声明支持；
  列表项使用原生 ListItem，行高、文本样式、内容色与两行/三行推断由原生组件决定，仅在有 @BT@onClick@BT@
  时包装 @BT@Modifier.clickable@BT@，不附加长按或 Role；脚手架使用原生 Scaffold，外壳与内容让位由原生实现承担。

## 列表项专属参数

`Material3ListItemSpec` 承载未进入中立 API 的原生列表项属性，经 `NomiListItem(nativeSpec = ...)` 传入：

```kotlin
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.list.NomiListItem
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.material3.list.Material3ListItemSpec

NomiListItem(
    title = { NomiText("高级选项") },
    subtitle = { NomiText("点击展开") },
    nativeSpec = Material3ListItemSpec(
        overlineContent = { NomiText("分组") },
        tonalElevation = 0.dp,
    ),
)
```

- 字段默认值等于原生默认（`ListItemDefaults.colors()` 与 `ListItemDefaults.Elevation`），未指定的字段
  回落，传空 spec 不改变外观。
- `overlineContent` 是 Material 3 独有的第三行槽位；中立 API 只有
  headline/supporting/leading/trailing 四槽。
- `colors` / `tonalElevation` / `shadowElevation` 直接对应原生参数，不做中立换算。
- 其它设计系统的 spec 与 null 一律静默忽略；多个 spec 用核心的 `+` 组合后一次传入。

## 平台与依赖

使用仓库 `multiplatform-lib-targets` 约定，平台开关与核心保持一致。生产实现位于 commonMain， 不含
Android/Desktop/iOS 专有 API。`api` 导出中立核心与 Material 3；公开转换签名需要后者。

测试依赖是单向测试边：`commonTest` 额外 `implementation(projects.core.designSystemNomi)`，只在测试源码集生效；
生产 `commonMain` 仍只有核心与 Material 3，不依赖 Nomi 实现模块，也不形成 adapter 之间的生产依赖。
测试统一以 `NomiTheme(designSystem = NomiDesignSystem.Material3)` 显式传入实现 （需导入
`ciyin.designsystem.material3.Material3`），不依赖隐式默认。

## 验证

[映射测试](<src/commonTest/kotlin/ciyin/designsystem/material3/Material3ThemeMappingTest.kt>)
锁定三种预置映射、工厂身份与原生安装基线；
[转换测试](<src/commonTest/kotlin/ciyin/designsystem/material3/Material3ConversionsTest.kt>)
覆盖十五排版角色往返、表面层级、legacy `extraLarge ← huge` 映射与颜色映射；
[主题参数测试](<src/commonTest/kotlin/ciyin/designsystem/material3/Material3ThemeParametersTest.kt>)
覆盖 Primary/Full 安装与嵌套主题整组替换恢复；
[原生主题测试](<src/commonTest/kotlin/ciyin/designsystem/material3/Material3NativeThemeTest.kt>)
覆盖原生默认、自定义原生实例、跨设计系统稀疏切换与恢复；
[显式 slot 测试](<src/commonTest/kotlin/ciyin/designsystem/material3/Material3ExplicitSlotTest.kt>)
覆盖原生内容环境与中立显式 slot 的隔离；
[组件测试](<src/commonTest/kotlin/ciyin/designsystem/material3/Material3ComponentsTest.kt>)
覆盖按钮变体/尺寸/启用态、原生 Material 按钮中的中立文字颜色以及 Surface/Card 内容色；
[导航测试](<src/commonTest/kotlin/ciyin/designsystem/material3/navigation/Material3NavigationTest.kt>)、
[脚手架测试](<src/commonTest/kotlin/ciyin/designsystem/material3/Material3ScaffoldTest.kt>)、
[列表项测试](<src/commonTest/kotlin/ciyin/designsystem/material3/list/Material3ListItemTest.kt>)
覆盖三种原生导航布局、顶栏/主体/底栏顺序与列表项槽位；
[独立原生对照测试](<src/desktopTest/kotlin/ciyin/designsystem/material3/Material3NativeParityTest.kt>)
使用不经过 NomiTheme 的独立 MaterialTheme 做几何、像素与排版对照。

本轮适配器改动未在此环境运行 Gradle，编译与测试由主集成流程执行：

```powershell
.\gradlew.bat :core:design-system-material3:desktopTest --console=plain
```

## 相关资料

- [模块设计方案](<../../.docs/design/design-system-模块化方案.md>)
- [旧包兼容模块](<../material/README.md>)
