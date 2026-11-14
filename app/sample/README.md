# `:app:sample`

## 用途与边界

本模块承载可由应用宿主打开的跨平台功能示例，入口为
[SampleModuleRoot](src/commonMain/kotlin/com/ciyin/app/presentation/screen/sample/SampleModuleRoot.kt)。
它维护独立 Navigation 3 子栈，不依赖 `:app:shared`，避免 shared → sample → shared 循环。

现有示例包括媒体库、媒体目录、文件选择/下载、权限、分享、AI、视频播放器和运行环境信息。 功能模块负责平台能力，sample
只负责示例 UI、用户交互和展示状态。

## 设计系统展示页

[DesignSystemDemoScreen](src/commonMain/kotlin/com/ciyin/app/presentation/screen/designsystem/DesignSystemDemoScreen.kt)
通过 [DesignSystemDemoRouter](src/commonMain/kotlin/com/ciyin/app/presentation/screen/sample/NavRouters.kt)
进入样例子栈。首期只展示 Text、Button、Surface 和非点击 Card；其余控件属于后续范围。

- Text：十五种排版角色以紧凑选择器呈现，默认 BodyMedium，每次只预览一个真实字号；保留长中文和显式多行。
- 原生默认区：带独立标签的 Text/Button/Surface/Card 自适应网格，不给被测组件传视觉参数；行为 enabled
  与页面同步。
- Button：同一尺寸的五种语义横向排列；支持尺寸覆盖时展示三档，不支持时仅一行原生尺寸变体。size/variant 的
  null 表示原生默认。
- Surface：五级表面色以自适应网格展示，保留隐式内容色继承。
- Card：标题、正文与内部按钮；卡片容器不带点击语义。
- 配置：三种设计系统、System/Light/Dark、1/1.3/2 倍字体、仅强调色的稀疏覆盖、局部主题覆盖与恢复默认。
- 能力限制：按 NomiComponentCapabilities 隐藏不支持的尺寸选项；摘要默认收起、可展开具体限制，切换系统在
  VM 动作边界清除不支持尺寸。
- Compact/Medium 上下排列；Expanded 的控制栏按卡片宽度令牌收紧、右侧填充示例。大字体下按剩余可用宽度退回单列，所有模式保持全页纵向滚动。
- 分区容器使用原生 NomiCard，只控制页面留白；网格复用 foundation VerticalGrid，不引入新的公共布局或改写原生组件样式。

[ViewModel](src/commonMain/kotlin/com/ciyin/app/presentation/screen/designsystem/DesignSystemDemoViewModel.kt)
使用 FlowRedux2 同步管理配置，UiState 只存标识、枚举、布尔与数值； renderer、ScrollState、主题对象均不进入
UiState。返回使用 Effect。 没有加载、失败、网络、数据库或新的 DI 模块。

## 默认设计系统与可选宿主覆盖

[ProvideSampleDesignSystems](src/commonMain/kotlin/com/ciyin/app/presentation/screen/sample/SampleDesignSystems.kt)
提供 `List<NomiDesignSystem>` 候选目录，commonMain 缺省为 Nomi + Material 3 + Fluent（对应
`NomiDesignSystem.Default` / `NomiDesignSystem.Material3` / `NomiDesignSystem.Fluent`，需分别导入
`ciyin.designsystem.nomi.Default`、`ciyin.designsystem.material3.Material3`、
`ciyin.designsystem.fluent.Fluent`）。 所有已启用目标共用此默认目录，不使用 expect/actual 或 Desktop
特殊包装维护不同列表。 provider 保留为可选覆盖入口；需要覆盖时，在样例导航栈上方传入稳定、非空且 ID
唯一的完整列表， 首项为页面默认值和重置目标。候选覆盖不改变应用根的 Material 3 主题。

公开 provider 的签名依赖设计系统接口，因此 [构建配置](<build.gradle.kts>) 使用
`api(projects.core.designSystem)`；Material 3 / Fluent adapter 都是 commonMain 的 implementation 依赖。
Fluent 原生类型仍留在 adapter 内，sample 只选择其公开扩展属性 `NomiDesignSystem.Fluent`（导入
`ciyin.designsystem.fluent.Fluent`）；实现对象 internal，不使用旧顶层 object 名称。 既有样例仍保留旧
Material 组件，不把本页试点扩展为全部样例重写。

每个新增 UI 文件有同文件 `@AppPreview`，预览直接使用新 NomiTheme 与纯 Content， 不引用 shared
AppPreview/ScreenScaffold，不启动真实导航或 ViewModel。
所有新增用户文案来自 [sample 字符串资源](src/commonMain/composeResources/values/strings.xml)。

## 验证入口

原生默认优先改造及后续布局优化已完成。25 项设计系统 Desktop 回归通过，覆盖角色切换、限制说明折叠、按钮横排及宽窄屏/大字体；sample
Android 编译通过。三系统 native/explicit
截图已生成，布局代表图见[桌面布局](<../../.docs/design/assets/design-system/layout-refinement/layout-fluent-desktop.png>)
。前轮 Android APK 仅构建，设备执行仍未进行。

跨平台目录扩展阶段曾通过 sample metadata、18 项 Desktop 回归、Desktop 宿主编译与 Android 应用/仪器 APK
构建；Android Fluent 仪器测试仅编译，未运行设备。该记录不覆盖之后的原生默认优先改造。

前轮记录（2026-11-11，Fluent 仍为 Desktop-only）：metadata/Desktop 编译与 17 项展示页测试通过， Desktop
宿主 Fluent 切换已检查，WSA 的 2 项端到端测试通过。该 WSA 记录只覆盖当时 Nomi/M3， 不能当作 Android
Fluent 已运行，更不等于真机验收。复验入口：

```powershell
.\gradlew.bat :app:sample:compileCommonMainKotlinMetadata :app:sample:compileKotlinDesktop --console=plain
.\gradlew.bat :app:sample:desktopTest --tests '*DesignSystemDemo*' --console=plain
```

- [ViewModel 测试](src/commonTest/kotlin/com/ciyin/app/presentation/screen/designsystem/DesignSystemDemoViewModelTest.kt)
  ：配置独立更新、重置、禁用计数、非法参数与返回 Effect。
- [导航恢复测试](src/commonTest/kotlin/com/ciyin/app/presentation/screen/sample/DesignSystemDemoNavigationTest.kt)
  ：使用真实 SavedStateConfiguration 做 NavKey 多态往返。
- [Content 测试](<src/desktopTest/kotlin/com/ciyin/app/presentation/screen/designsystem/DesignSystemDemoContentTest.kt>)
  ：三套默认候选、控件动作、disabled 语义和非点击 Card。
- [Android 仪器测试](<../android/src/androidTest/java/com/ciyin/app/DesignSystemDemoInstrumentedTest.kt>)
  ：真实入口导航、三系统禁用计数和 Fluent 暗色/大字体；本轮已编译，设备执行未进行。
- [布局与截图测试](src/desktopTest/kotlin/com/ciyin/app/presentation/screen/designsystem/DesignSystemDemoLayoutTest.kt)
  ：宽窄布局、大字体、Nomi/M3/Fluent 深浅色，先保存 native 首屏再保存 explicit 区域，以及局部主题恢复。

截图先写系统临时目录并在 finally 删除本次创建的目录。需要保留时设置
`NOMIKIT_DESIGN_SYSTEM_ARTIFACTS` 环境变量或测试 JVM 系统属性
`nomikit.designSystem.artifacts`，结果复制至该指定目录；默认不在仓库写截图。

[稳定标签](src/commonMain/kotlin/com/ciyin/app/presentation/screen/designsystem/DesignSystemDemoTags.kt)
统一使用 `design-system-demo-` 前缀。Android 仪器测试可通过字符串复用 root、controls、showcase、
back、reset、counter-button、disabled-button、card-button 等标签；实现常量保持 internal。 动态标签为
`system-{id}`、`mode-{enum}`、`font-{float}`、`size-{enum}`、`variant-{enum}`。

## 平台与资料

commonMain/commonTest 存放跨平台契约，androidMain/desktopMain/iosMain 保留平台能力适配， desktopTest
负责离线 Compose 渲染。Fluent adapter 的生产实现已共用 commonMain，sample 可在启用目标选择它。
平台开关沿用项目配置，不为展示页打开 JS/Wasm；Desktop 截图不能替代 Android 触摸或 iOS native 验收。
本轮与前轮结果、Release 依赖兼容缺口见设计说明。

- [Screen 创建规范](../../.agents/skills/create-kmp-screen/SKILL.md)
- [UI foundation](../../.agents/skills/core-ui-foundation/SKILL.md)
- [设计系统模块化方案](../../.docs/design/design-system-模块化方案.md)
