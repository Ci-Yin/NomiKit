---
name: create-kmp-screen
description: Use when creating, migrating, splitting, or substantially refactoring a NomiKit UI screen. Generates or reshapes an MVI + FlowRedux2 + StateMachineMviViewModel screen, decides screen/component ownership, keeps same-file Compose previews, and optionally creates a scoped <Name>Ext.kt for non-private non-Compose extensions.
---

# 创建与重构 NomiKit Screen

本 skill 适用于 `com.ciyin.app` 的 Compose Multiplatform 页面。它覆盖新建页面，也覆盖把旧页面迁移到 shared、拆分页面包和大型 UI 重构。实现必须保持 **MVI + FlowRedux2 + `StateMachineMviViewModel`**，并遵守 Data-Domain-UI 边界、主题令牌和 KMP 平台边界。

## 一、触发范围

必须使用本 skill 的场景：

- 新建 `XxxScreen`、新建页面或页面 ViewModel。
- 从旧模块/旧包迁移页面，保留旧实现但切断新调用链。
- 将一个页面拆分为多个 screen、content 或 component。
- 大规模重构页面结构、导航、MVI 状态或页面级交互。

以下 **Local Fix** 可以不使用本 skill：单个按钮、单条文案、局部颜色/间距或局部 bug，且不改变页面边界、导航、MVI 契约或 Compose 文件组织。只要改动扩大到这些边界，重新进入本 skill 流程。

## 二、实现前页面契约

在写 Kotlin 之前，先在任务计划或变更说明中记录以下内容；迁移/拆分必须先完成这一步：

1. Screen 入口、Content 纯 UI、ViewModel、Effect 宿主回调的边界。
2. 每个视觉单元是否属于 `screen/<name>/component/`。记录组件接收的 UI model 和 callbacks；组件不得直接依赖页面 Action/Effect。
3. 每个包含用户界面 `@Composable` 的生产 Kotlin 文件的 Preview 状态：Loading、Failure、Content，以及一个关键交互态是否覆盖。
4. 是否需要 `<Name>Ext.kt`，以及哪些非私有扩展函数/扩展属性会放入其中。
5. 旧实现的保留、禁用和引用策略。旧实现保留时，`rg` 必须证明新 shared/bridge 调用链没有旧包引用。
6. 导航路由、`entryProvider`、宿主回调和紧凑/宽屏策略。

如果需求文档、原型、旧代码存在冲突，先列出冲突和明确的产品调整，再实现；不要用模板默认值替代产品决策。

## 三、目录与文件清单

标准页面目录：

```text
app/shared/src/commonMain/kotlin/com/ciyin/app/ui/screen/<name>/
├── <Name>Action.kt        必选
├── <Name>Effect.kt        必选
├── <Name>UiState.kt       必选
├── <Name>Model.kt         按需
├── <Name>Mapper.kt        按需
├── <Name>ViewModel.kt     必选
├── <Name>Screen.kt        必选，包含入口、Content 和同文件 Preview
├── <Name>Ext.kt           可选，仅放非私有、非 Compose 扩展
└── component/             按职责判定，不能只按行数决定
```

`<Name>Ext.kt` 不是 Preview 文件，也不是“杂项文件”。没有合格扩展时不要生成它。

包名规则：顶级页面使用 `com.ciyin.app.ui.screen.<name>`；子页面追加父级包名；组件使用对应包名的 `.component`。

## 四、Screen 与 component 判定

先判定再实现：

- 直接绑定页面 `UiState`、`Action`、`Effect`，或负责页面级导航/状态机的 Composable，留在 `screen/<name>/`。
- 只接收 UI model 和 callbacks、可独立复用的视觉单元，放入 `screen/<name>/component/`。
- 媒体、操作栏、元数据、标签、画册、分页内容等独立视觉单元优先拆成 component。
- component 不直接 import 页面 Action、Effect 或 ViewModel；页面把回调转换为局部 lambda 后传入。
- 从旧页面迁移到 component 时，先把参数改成稳定 UI model + callbacks，再移动文件并补同文件 Preview。

## 五、同文件 Preview 硬规则

- 含有用户界面 `@Composable` 的生产 Kotlin 文件，必须在**同一个文件**内提供至少一个 `@AppPreview`。
- 禁止创建 `XxxPreview.kt`、`Preview.kt` 或任何独立 Preview 文件。
- Screen 入口依赖 Koin、ViewModel、导航、网络或平台对象时，Preview 同文件中的纯 Content；不得启动真实副作用。
- 复杂页面至少提供 Loading、Failure、Content 和一个关键交互态。可在同文件放多个私有 Preview。
- Preview 夹具必须无网络、无真实导航、无持久化写入；优先使用稳定的本地 UI model。
- `expect/actual` 或完全不可独立渲染的平台包装器可例外；必须在页面契约中说明原因，静态脚本会跳过明确的 `expect/actual` 包装文件，并为可渲染的实际 Content 提供 Preview。
- Preview 使用：

```kotlin
import com.ciyin.app.ui.component.AppPreview
import org.jetbrains.compose.ui.tooling.preview.AppPreview

@AppPreview
@Composable
private fun XxxContentPreview() = AppPreview {
    XxxContent(state = previewState, onAction = {})
}
```

第一个 `AppPreview` 是项目主题容器，第二个是注解；不要从 `com.ciyin.app.ui.util` 导入过时符号。

## 六、`<Name>Ext.kt` 规则

脚手架询问：

```text
是否存在该 Screen 目录内需要集中管理的非私有、非 Compose 扩展函数或扩展属性？
```

选择“是”时生成 `screen/<name>/<Name>Ext.kt`，并遵守：

- 只放置该 Screen 目录内的非私有顶层扩展函数和扩展属性。
- `internal` 是默认可见性；只有明确的跨模块 API 需求才使用 `public`。
- `private` 扩展函数和扩展属性留在原文件，不迁移到 Ext。
- `@Composable` 扩展函数留在对应 Compose 文件；Ext.kt 不得包含 `@Composable`、Preview、ViewModel 状态机或副作用逻辑。
- 每个非私有扩展函数和扩展属性补中文 KDoc。
- Data/Domain mapper 和通用扩展留在各自层，不因为页面使用就迁入 Screen Ext。

示例：

```kotlin
package com.ciyin.app.ui.screen.example

/** 将页面路由转换为详情初始身份。 */
internal fun ExampleRouter.toIdentity(): PictureIdentity = initial.toIdentity()

/** 当前页面是否存在可展示的画册内容。 */
internal val ExampleUiState.hasAlbumContent: Boolean
    get() = albumItems.isNotEmpty()
```

## 七、MVI、FlowRedux 与平台边界

- `Screen` 只创建/取得 ViewModel、收集 state/effect，并把 Action 路由给 ViewModel；不在 Screen 中实现业务状态机。
- `UiState` 只放可序列化/可比较的业务 UI 状态；`LazyListState`、`PagerState`、缩放状态、文本编辑状态和动画进度留在 Compose `remember` / `rememberSaveable`。
- 一次性导航、Toast、弹窗和滚动命令使用 Effect，不塞进 UiState。
- ViewModel 使用项目已有 `StateMachineMviViewModel`、FlowRedux2 DSL 和 `condition`/`untilIdentityChanges` 等模式；异步请求必须以页面身份/请求身份校验迟到结果。
- Data 层只输出 `DataError`；Domain 定义场景错误和 UseCase；UI 将场景错误映射到 Compose Resources，不展示 parser/network 原始错误。
- KMP commonMain 不直接依赖 Android Context、Activity、AndroidX 平台实现；通过已有平台接口、feature 模块和 Koin 装配。
- 用户可见文案必须登记在 `app/shared/src/commonMain/composeResources/values/strings.xml`，UI 使用 Compose Resources 引用。
- 尺寸、颜色、形状、间距、字体和图标优先使用 `ciyin.material.theme.AppTheme`、`AppTheme.spacings/typography/colorScheme/shapes`、项目 `IconPack` 和既有组件；不得使用 Material `Icons` 示例或业务硬编码颜色。

## 八、导航与宿主接入

新增或迁移 Screen 时同时盘点：路由序列化、`NavSavedStateConfig`、`entryProvider`、宿主 `onBack`/导航回调、Compact/Medium/Expanded 策略和宽屏 TwoPane 条件。详情页与独立阅读器等页面不得只改 UI 而遗漏宿主入口。

## 九、脚手架流程

1. 读取本项目规则、目标模块、需求/原型（若有）和旧实现（若有）。
2. 完成页面契约，判定 Screen/Content/component/Ext/Preview/旧引用策略。
3. 生成 Action、Effect、UiState、Model/Mapper、ViewModel、Screen；需要时生成 component 和 `<Name>Ext.kt`。
4. 在每个含 UI `@Composable` 的生产文件中就地补 Preview；先完成纯 Content，再接 ViewModel 和导航。
5. 接入 Koin、路由和宿主，检查旧实现没有进入新调用链。
6. 执行静态检查、最窄编译/测试和 `git diff --check`；修复后再扩大验证范围。

目录已存在时，先说明是合并、迁移还是拆分；不得把迁移需求当成覆盖整个目录的普通新建。

## 十、模板与验证

模板位于 `templates/`：`Action.kt.template`、`Effect.kt.template`、`UiState.kt.template`、`Model.kt.template`、`Mapper.kt.template`、`ViewModel.kt.template`、`Screen.kt.template` 和可选的 `Ext.kt.template`。模板中的 `{{...}}` 是生成器占位符，生成后必须替换或删除 TODO。

至少运行：

```powershell
rg -n "@Composable" app/shared/src/commonMain/kotlin/com/ciyin/app/ui/screen
rg -n "@AppPreview" app/shared/src/commonMain/kotlin/com/ciyin/app/ui/screen
rg -n "Preview\.kt$" app/shared/src/commonMain/kotlin/com/ciyin/app/ui/screen
rg -n "androidx\.compose\.material\.icons|com\.ciyin\.app\.ui\.util\.AppPreview" .agents/skills/create-kmp-screen
rg -n "private\s+(fun|val)\s+\w+\.|(internal|public)\s+(fun|val)\s+\w+\." app/shared/src/commonMain/kotlin/com/ciyin/app/ui/screen/<name>
```

其中 `Preview.kt$` 的结果必须为空；每个含 `@Composable` 的文件都要人工确认同文件存在 `@AppPreview`。`Ext.kt` 必须没有 `@Composable`、没有 Preview 注解、没有 private 扩展，并且每个扩展有中文 KDoc。

按改动风险运行最窄的 `compileKotlinDesktop`、对应 `desktopTest`、`bridge:compileAndroidMain` 或 `android:assembleDebug`。skill 文件本身改动时，重复执行新建、迁移、拆分、重构和 Ext/Preview 分类压力场景。

可对单个页面目录运行可重复检查：

```powershell
powershell -ExecutionPolicy Bypass -File .agents/skills/create-kmp-screen/scripts/validate_create_kmp_screen.ps1 -ScreenPath app/shared/src/commonMain/kotlin/com/ciyin/app/ui/screen/<name>
```

## 十一、常见错误

- 只在 `XxxScreen.kt` 写 Preview，却让 `component/`、Content 或媒体文件没有同文件 Preview。
- 新建 `XxxPreview.kt` 逃避同文件规则。
- 把 `private` 或 `@Composable` 扩展塞进 Ext.kt。
- component 直接拿页面 Action/Effect，导致 UI 与状态机耦合。
- 用 Material `Icons`、错误的 `AppPreview` import、错误的 `AppTheme` 包名或硬编码文案/颜色。
- 迁移页面时删除旧实现，或忘记检查导航宿主和旧引用。

参考实例见 [references.md](references.md)，MVI 与 FlowRedux 说明见项目 `.docs/contributing/mvi.md` 和 `.docs/guides/flow-redux.md`。
