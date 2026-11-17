# :core:material

旧 `ciyin.material.theme` 包的源码兼容层。新的设计系统入口位于
[中立核心](<../design-system/README.md>)；Material 3 渲染位于
[独立适配器](<../design-system-material3/README.md>)。

## 职责与依赖方向

本模块依赖中立核心和 Material3 adapter，不允许它们反向依赖本模块。 保留
`api(projects.core.uiFoundation)`，避免旧消费模块因传递依赖骤然消失而无法编译。 新模块不应为了使用中立令牌而依赖此兼容包。

新 API 统一采用 Nomi 前缀，旧包仍保留 AppTheme/AppColorScheme/AppShapes/AppSizes
与旧颜色工厂；旧包默认工厂转发已删除，默认入口统一使用中立核心工厂。 本模块只转发到 Nomi 实现，不为新核心保留
App* 别名。

## 兼容范围

| 旧入口                                                 | 当前实现                                                            |
|--------------------------------------------------------|---------------------------------------------------------------------|
| `AppTheme(...)`                                        | 保留全部参数名、参数顺序、默认选择行为和 Material `Typography` 参数 |
| `AppTheme.typography`                                  | 在 M3 环境读取实际 `MaterialTheme.typography`，仍为 Material 类型   |
| `AppTheme.colorScheme/shapes/spacings/sizes`           | 通过 NomiTheme 只读 getter 读取当前五组结果，不维护第二套 locals    |
| `AppTheme.darkMode`                                    | 返回已解析明暗映射的旧枚举；不会返回 `System`                       |
| `DarkMode.Light/Dark/System`                           | 保留真实枚举，支持旧的枚举项 import                                 |
| `AppColorScheme/AppShapes/Spacings/AppSizes`           | 中立数据类型别名；公开构造与生成的成员 copy，默认入口为中立核心工厂 |
| `SizeScale/Strokes/ComponentHeights/LayoutConstraints` | 中立嵌套类型别名；公开构造仍需全部参数，成员 copy 无需导入          |
| `lightColorScheme/darkColorScheme`                     | 保留全部命名参数，默认颜色只从中立工厂取得                          |
| 圆角 `enable/disable/top/bottom/start/end`             | 旧包扩展转发到中立实现                                              |
| `iconpack.IconPack` 与五个内置图标                     | 旧 namespace/扩展入口保留，图形只由中立核心维护                     |

既有 AppTheme、类型别名、旧颜色工厂、形状与图标入口继续承诺源码兼容，不承诺旧编译二进制的 ABI 兼容。
此前已删除新建的 8 个冗余 copy 转发；本轮按用户授权删除旧包 8
个默认工厂转发（defaultAppShapes/defaultAppSizes/defaultSpacings/defaultStrokes/defaultComponentHeights/defaultLayoutConstraints/defaultIconSizeScale/defaultAvatarSizeScale），不留
Deprecated 或兼容入口。使用过它们的调用方改用中立核心工厂后重编译。

## 旧主题最小调用

```kotlin
import ciyin.material.theme.AppTheme
import ciyin.material.theme.DarkMode.Dark
import ciyin.material.theme.lightColorScheme

AppTheme(
    darkMode = Dark,
    colorScheme = lightColorScheme(),
    typography = AppTheme.typography,
    shapes = AppTheme.shapes,
    spacings = AppTheme.spacings,
    sizes = AppTheme.sizes,
) {
    // 原调用继续使用 Material 3 组件或新的中立组件。
}
```

旧函数先把旧 `DarkMode` 解析为实际明暗，再用完整参数组装中立 `NomiTheme`，并通过
`NomiTheme.() -> Material3Theme` 接收者映射把中立色板、九级形状与完整 Material Typography 映射为原生主题；
`NomiDesignSystem.Material3(map)` 不读取系统设置。显式颜色不会因 darkMode 被改写；省略颜色仍按
darkMode 选择旧的浅色/深色默认；其他参数继续继承当前值。旧默认参数先在父环境求值，因此省略的排版、形状、间距与尺寸
继承父主题当前值。适配器不把原生主题投影回中立值；`LocalNomiTheme` 由入口发布，读取的即旧参数原值。旧
colorScheme/shapes/spacings/sizes getter 通过 `NomiTheme` 读取入口值；typography 在 Material3 环境保留原生
`MaterialTheme.typography` 实例，在其他环境从 `NomiTheme.typography` 映射。未建立主题时的旧默认排版由中立
默认排版转换产生，不回退到不同数值的 M3 原生默认。M3 环境判定读取可空
`LocalNomiDesignSystem.current?.id` 并与 `NomiDesignSystem.Material3.id` 比较；需导入扩展函数
`ciyin.designsystem.material3.Material3` 与核心 `NomiDesignSystem`。未提供设计系统时走中立排版映射，不抛错。

## 完整 Typography 兼容

Material3 1.9 除十五个公开角色外还包含十五个内部强调角色。 旧 `AppTheme(typography = ...)`
保留调用方的完整实例，不以十五角色往返转换取代该实例；原生专属角色不会回写中立主题。 间距与尺寸原样保留，
颜色与圆角按 Material 3 可映射角色投影。 该配置只作用于旧函数，不是应用根的全局 Legacy 双主题桥，也不要求
gate。 新 Material3 根下旧令牌 getter 直接读取中立主题，旧排版 getter 读取实际 MaterialTheme。

## 默认对象与复制

主题数据对象可通过旧类型别名公开构造，或从中立核心默认工厂获取；旧包不再提供默认工厂转发。 复制直接使用中立
data class 生成的成员 `copy`，无需导入中立包或旧包的 copy；原手写转发已删除。
默认数值、构造必填参数与工厂身份语义不变：颜色/形状/间距/完整尺寸工厂返回新外层对象，尺寸子组工厂复用核心共享对象。
`copy` 是浅复制，只替换指定字段，未改字段保留原值/引用，不修改默认实例。完整 Material Typography
仍是原生类型，沿用其原生 copy。

```kotlin
import ciyin.designsystem.theme.defaultSpacings
import ciyin.material.theme.Spacings

val spacings = defaultSpacings().copy(large = sectionSpacing)
val directSpacings = Spacings(large = sectionSpacing)
```

## 测试与平台

[源码兼容测试](<src/commonTest/kotlin/ciyin/material/compat/LegacySourceCompatibilityTest.kt>)
从独立消费包验证保留的旧 import、命名参数、中立默认工厂、通过类型别名公开构造和无扩展导入的成员
copy、形状和图标。
[主题兼容测试](<src/commonTest/kotlin/ciyin/material/compat/LegacyThemeCompatibilityTest.kt>)
验证完整旧函数命名参数、原 Typography 实例、嵌套默认行为、父主题隔离与新 Material3 根下的旧 getter。

使用仓库 `multiplatform-lib-targets` 平台约定；生产兼容 API 位于 commonMain， 不新增平台资源、生命周期或设备调用。

建议由主集成流程执行：

```powershell
.\gradlew.bat :core:material:desktopTest --console=plain
.\gradlew.bat :app:shared:compileKotlinDesktop --console=plain
```

迁移到映射函数模型后，Desktop 回归覆盖 legacy factory、getter、原生 Typography 实例身份、父主题隔离
与旧类型别名；本轮由主集成流程统一验证，不在文档记录未经运行的通过数。 旧浅深色 27 参数工厂继续直接转发中立
defaultLightNomiColorScheme/defaultDarkNomiColorScheme，默认值来源只有一处。

## 相关资料

- [主题入口](<src/commonMain/kotlin/ciyin/material/theme/Theme.kt>)
- [颜色工厂](<src/commonMain/kotlin/ciyin/material/theme/ColorScheme.kt>)
- [设计系统模块化方案](<../../.docs/design/design-system-模块化方案.md>)
- [模块技能](<../../.agents/skills/core-material/SKILL.md>)
