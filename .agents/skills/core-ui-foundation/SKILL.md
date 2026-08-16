---
name: core-ui-foundation
description: Use the core/ui-foundation Compose Multiplatform UI foundation module (package ciyin.ui.foundation.*). Covers ViewModel/MVVM/MVI bases, StateMachineMviViewModel/SingleStateMachine, HasBackgroundScope, saved state helpers, Window/FloatWindow, SystemUiController, window size, effects, dialogs, refresh/grid layouts, scrollbars, Text/Button/FlexButton/SettingItem/TwoPane/progress/menu widgets, and color/unit/modifier helpers. Use when 用户要在 NomiKit 中使用或维护 UI 基建、ViewModel 基类、Compose 通用组件、窗口/系统栏/刷新布局，或排查 core/ui-foundation。
---

# core/ui-foundation 使用指南

`core/ui-foundation` 是 NomiKit 的 Compose Multiplatform UI 基建层，包名为 `ciyin.ui.foundation.*`。优先复用这里的 ViewModel 基类、效果、布局和组件，再考虑新增项目内重复封装。

## ViewModel 与状态

常用入口：

- `AbsMvvmViewModel<S, E>`：简单 MVVM，内部 `StateFlow<S>` + 一次性 `SharedFlow<E>`。
- `AbsMviViewModel<S, A, E>`：基于 `SingleStateMachine` 的轻量 MVI。
- `StateMachineMviViewModel<S, A, E>`：基于 FlowRedux2 的复杂状态机 MVI。
- `StateViewModel` / `ActionViewModel` / `EffectViewModel` / `MviViewModel`：UI 层接口边界。
- `HasBackgroundScope` / `BackgroundScope` / `rememberBackgroundScope`：受控后台作用域与 Flow 到 Compose State 的 helper。

注意事项：

- 新建页面/MVI 三件套优先使用 `create-kmp-screen` skill。
- 写复杂 FlowRedux2 状态机时必须使用 `flowredux-statemachine` skill。
- Compose `UiState` / `Model` / `Action` / `Effect` 数据模型按项目规则保持不可变，并给 Compose 状态模型加 `@Immutable`。
- 不要直接执行 `BackgroundScope().backgroundScope.launch { ... }`；要把 `HasBackgroundScope` 交给可管理对象或 `rememberBackgroundScope`。
- `AbstractViewModel` 同时实现 Compose `RememberObserver` 与 AndroidX `ViewModel`，生命周期要与 composition/ViewModelStore 对齐。

## 窗口、系统 UI 与效果

常用入口：

- `Window(...)`：跨平台普通窗口 expect。
- `DialogWindow(icon = ..., config = DialogWindowConfig(...), ...)`：跨平台模态窗口；Desktop 创建系统装饰的 document-modal 原生对话框，Android/iOS/Web 回退 `CommonWindow`。
- `ComposeWindow.prepareWindowsSystemBackdrop()`：在 `SwingWindow.init` 中预安装可回滚的透明客户区宿主。
- `FrameWindowScope.WindowsSystemBackdropEffect(type, darkTitleBar, onApplied)`：Desktop 主窗口系统背景材质效果；`WindowsSystemBackdrop` 提供 `Mica`、`DesktopAcrylic` 与 `MicaAlt`。
- `FloatWindow` / `FloatWindow2`：Popup / DropdownMenu 浮窗。
- `rememberSystemUiController()` 与 `SystemUiControllerEffect(...)`：系统栏颜色、可见性与深色图标。
- `currentWindowWidth()` / `currentWindowHeight()` / `currentWindowSize()` / `currentWindowDpSize()`：窗口尺寸分级。
- `classifyWindowWidth(width)`：按布局实时可用宽度复用项目的 Compact / Medium / Expanded 断点，适合窗口组件基于 `BoxWithConstraints.maxWidth` 自适应。
- `KeepScreenOnEffect`、`ScreenRotationEffect`、`DarkStatusBarAppearance`、`cursorVisibility`、`blurEffect`、`OnLifecycleEvent`。

注意事项：

- `SystemUiController` 在非 Android 平台可能是简化或空操作实现，不要把业务正确性建立在系统栏必然可控上。
- `DialogWindowConfig.size` 必须由业务调用方明确传入，不要在基建内写死业务尺寸；`resizable`、`dismissOnEscape` 分别控制用户缩放与 Esc 关闭行为；`darkTitleBar` 只影响 Windows DWM 系统标题栏。
- Desktop `DialogWindow` 按 `dismissOnEscape` 决定是否消费 Esc 关闭请求，并使用 DWM 深色标题栏属性 20、失败时回退属性 19；DWM 调用失败只记录警告，不要在业务层重复操作原生窗口句柄。
- Desktop 系统背景材质使用 DWM 属性 38；Windows 11 24H2 会同时尝试属性 39 的重定向位图 Alpha。`onApplied` 只有在 DWM 与 Compose/Skia 客户区透明化均成功时才返回 `true`，调用方必须据此决定是否移除实色根背景。
- Desktop 系统背景材质生效期间会同时拦截顶层窗口与 Skia 硬件子窗口的 `WM_ERASEBKGND`，避免交互式缩放先用默认实色擦除透明客户区；释放材质时必须恢复各自的原始窗口过程，不要改用影响所有窗口的 AWT 全局背景擦除开关。
- `WindowsSystemBackdropEffect` 保留系统装饰窗口，不应与 Compose Desktop 仅支持无装饰窗口的 `transparent = true` 混用；主窗口必须使用 `SwingWindow`，并在 `init` 中先调用 `prepareWindowsSystemBackdrop()`，确保在窗口可显示前安全安装客户区宿主；离开组合时会恢复 DWM 与渲染层原状态。
- `SystemTray(...)` 通过 `DisposableEffect` 注册并释放原生托盘图标；注册失败只记录错误，不得阻断主窗口创建。业务调用方应传入资源化的 `openWindowLabel` 与 `exitLabel`。
- 平台差异能力通过 expect/actual 维护，避免在 common UI 中写平台判断。

## 布局与滚动

常用入口：

- `RefreshLayout`、`PullToRefresh`、`RefreshLayoutState`、`rememberRefreshLayoutState`。
- `HorizontalGrid` / `VerticalGrid` 与 `SimpleGridCells`。
- `JKLazyColumn`、`JKLazyGrid`、`JKLazyVerticalStaggeredGrid` 以及 scrollbar state/helper。
- `Modifier.onScrollStateChanged(...)`：统一监听触摸拖拽与桌面鼠标滚轮，`Top` 表示向内容末端、`Bottom` 表示向内容起点；普通按下/点击不会产生滚动方向。
- `TwoPane` / `TwoPaneMode`。

注意事项：

- `RefreshLayoutState.setRefreshState(...)` 需要在 `RefreshLayout` 至少组合初始化后调用；过早调用会抛未初始化异常。
- `VerticalGrid` 需要有限宽度，`HorizontalGrid` 需要有限高度；不要放在无约束方向的无限布局里。
- `JKLazyGrid` / `JKLazyVerticalStaggeredGrid` 位于 `ciyin.ui.foundation.widget.scrollbar.usage`，不要再引入历史遗留的 `lcpp.github.scrollbar.usage` 包名。
- `Scrollbar(isSupperSmall = ...)` 的紧凑外观与交互能力相互独立；默认仍可拖动，需要只读装饰时显式设置 `isInteractive = false`。运行时修改 `isInteractive` 或 `orientation` 会重建手势监听，拖动取消与正常结束都会调用 `onDragEnd`，调用方应在此清理拖动态。
- 横向 `Scrollbar` 按指针 `x` 坐标推进，竖向按 `y` 坐标推进；`JKLazyGrid` 与 `JKLazyVerticalStaggeredGrid` 的 full-height 滚动条固定使用竖直轴。

## 基础组件

常用入口：

- 文本：`Title`、`ContentBody`、`SingleText`。
- 按钮：`Button`、`SmallButton`、`OutLineButton`、`OutLineTextButton`、`FlexButton`、`ButtonStyle`、`FlexButtonStyles.Default`。
- 设置项：`SettingItem`、`SettingSwitch`、`SettingRadio`、`SettingCheckbox`、`SettingScrollChoose`、`SettingIconButton`、`SettingDefaults.settingStyle()`。
- 容器/其它：`BoxSurface`、`ColumnSurface`、`RowSurface`、`MenuChip`、`ExposedMenu`、`MenuText`、`Toast`、`ProgressData`、`LinearProgressBar`、`ArcProgressBar`。

注意事项：

- `SingleText` 是文本封装，没有 content slot；需要动画或富内容时在外层组合。
- 面向用户文案优先走项目资源/多语言机制，不要在可复用组件里硬编码业务文案。
- 新增通用组件时保持参数稳定、状态外置、modifier 优先，并遵守项目 UI 设计规则。

## 修改注意

- UI 基建改动影响面大，优先做最小相关验证。
- 修改本模块后优先运行 `.\gradlew.bat :core:ui-foundation:compileCommonMainKotlinMetadata --console=plain`。
