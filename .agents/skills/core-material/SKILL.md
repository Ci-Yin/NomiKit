---
name: core-material
description: Maintain NomiKit core/material as the ciyin.material.theme source-compatibility facade. Use for legacy AppTheme, Material Typography, DarkMode imports, token factories, old IconPack extensions, or migration failures. New native-default-first components and adapters belong to core-design-system.
---

# core/material 兼容门面

新增、重构或修复 Kotlin 声明时，先读取 [kotlin-kdoc](<../kotlin-kdoc/SKILL.md>)
；仅有中文摘要不代表参数、返回值和边界契约已完整。

保留旧 `ciyin.material.theme` 与旧图标
API，新增中立能力转[core-design-system](<../core-design-system/SKILL.md>)。
完整矩阵见[模块 README](<../../../core/material/README.md>)；修改前读
[旧 Theme](<../../../core/material/src/commonMain/kotlin/ciyin/material/theme/Theme.kt>)、
[M3 配置入口](<../../../core/design-system-material3/src/commonMain/kotlin/ciyin/designsystem/material3/Material3DesignSystem.kt>)
和真实旧调用方。

## 原生默认重构后的兼容方式

- 旧 `AppTheme(...)` 保留参数名、顺序和默认选择行为；省略颜色按请求模式选择旧默认，其余参数继承当前值。
- `AppTheme` 内部组装 `NomiTheme` 值，再用 `Material3Map` 把旧颜色/原生 Typography/形状映射为
  `Material3Theme`，交给 `NomiDesignSystem.Material3(map)`；不把主题聚合对象或 request/overrides
  重新引入核心入口。
- `AppTheme.typography` 仍是 Material 类型；M3 环境读取实际实例，不能用十五角色往返转换丢失库专属角色。
- 旧 colorScheme/shapes/spacings/sizes getter 通过 `NomiTheme` 读取；typography 在 Material3 环境保留原生
  `MaterialTheme.typography` 实例，其余环境从中立排版映射。分支判定读取可空
  `LocalNomiDesignSystem.current?.id == NomiDesignSystem.Material3.id`（需导入扩展工厂
  `ciyin.designsystem.material3.Material3`），未提供时走中立映射而非抛错。不要复制一套独立
  CompositionLocal；组件通过 `NomiTheme` 读取中立主题。
- `DarkMode` 保留真实旧枚举与显式映射，不能仅 typealias 而破坏枚举项 import。
- 五组旧类型别名、颜色工厂 27 个命名参数和形状扩展保留；中立主题 data class 公开构造，旧别名同样可直接构造。
  使用生成的成员 copy，无需导入任何 copy 扩展；删除此前新建的 8 个复制转发，不留 Deprecated/兼容入口，不误删既有
  AppTheme/别名/工厂/图标兼容。 旧 lightColorScheme/darkColorScheme 直接转发到中立
  defaultLightNomiColorScheme/defaultDarkNomiColorScheme，不复制默认值。
  构造默认值与必填参数不变；单组默认工厂的新外层/共享子组身份不变，成员 copy 是保留未覆盖引用的浅复制。
- 图标保留旧 namespace，数据只在中立核心；扩展转发使用显式 import alias，防止递归。
- 既有兼容入口只承诺源码兼容，不承诺旧二进制 ABI；使用过新增复制转发的调用方须删除旧 copy import 后重编译。

[构建](<../../../core/material/build.gradle.kts>)保留核心、M3 adapter 和 UI foundation 的 api 依赖；
不能让核心/adapter 反向依赖旧门面，也不把 Fluent 依赖塞进兼容包。 应用根已选新 M3，legacy
配置只服务旧入口；不添加全局双主题桥或延后根迁移 gate。 app/shared 已迁移到中立导航、NomiTheme 与中立
IconPack，不再 import ciyin.material；旧门面消费者只剩 sample 等显式使用旧 AppTheme 的页面。 不要为
app/shared 的导航/预览/脚手架恢复 AppTheme、旧 iconpack 或 ui-foundation 的 M3 Title。 新 `NomiTheme`
是包含五组与 darkMode 的值类型，typography 是 Type 中的 NomiTypography，不是旧 Material 类型。 只有旧
ciyin.material 门面继续保留 AppTheme/AppColorScheme/AppShapes/AppSizes
名称；旧包默认工厂转发已删除，默认入口统一为中立核心工厂。新 API 不提供 App* 别名。 这保留旧函数自身的颜色默认选择
与完整原生 Typography 身份契约，不引入根/Override 双入口。

## 验证

检查[源码兼容测试](<../../../core/material/src/commonTest/kotlin/ciyin/material/compat/LegacySourceCompatibilityTest.kt>)
与
[主题兼容测试](<../../../core/material/src/commonTest/kotlin/ciyin/material/compat/LegacyThemeCompatibilityTest.kt>)：
保留的旧 import、命名参数、别名公开构造、无扩展导入的成员 copy、中立默认工厂/共享子组身份、 完整 Material
Typography 对象身份、形状/图标转发、嵌套默认与新 M3 根下旧 getter。 新原生 M3
默认和旧门面默认是不同契约，不为统一外观改掉旧默认数值。

```powershell
.\gradlew.bat :core:material:desktopTest --console=plain
.\gradlew.bat :app:shared:compileCommonMainKotlinMetadata --console=plain
```

当前实施与验证记录见[设计说明](<../../../.docs/design/design-system-模块化方案.md>)。
公开构造、中立单组默认工厂和旧类型别名仍是当前契约；旧包默认工厂转发已按用户授权删除。主题结果扁平化后的
legacy factory 与 getter 已复验，后续修改仍须独立验证，不以历史原生默认、主题恢复和包迁移记录替代。
修改公共兼容行为时同步 README 与本技能。
