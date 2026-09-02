---
name: flowredux-statemachine
description: 在 NomiKit 项目（com.ciyin.app）中使用 FlowRedux2 状态机进行 ViewModel 编排时的 DSL 速查、关键约束与进阶模式（condition / untilIdentityChanges / collectWhileInState / 子状态机 onEnterStartStateMachine 与 onActionStartStateMachine / 复用 suspend 提取）。本 skill 不负责脚手架式新建 screen（请用 create-kmp-screen），专门用于"已有 StateMachineMviViewModel 想写更复杂的状态流转 / 子状态机 / 多状态 sealed interface"的场景。当用户提到 FlowRedux / FlowRedux2 / inState / onEnter / mutate / override / noChange / condition / untilIdentityChanges / collectWhileInState / 子状态机 / onEnterStartStateMachine / ChangeableState / ChangedState / actionMapper 时使用。
---

# FlowRedux2 状态机进阶 Skill

本 skill 用于在 NomiKit 中编排已有 `StateMachineMviViewModel` 的 FlowRedux2 状态流转。项目当前使用
FlowRedux2 `2.0.1`，库坐标为 `com.freeletics.flowredux2:flowredux`。

## 职责边界

- 从零创建 Screen、ViewModel、Action、Effect、UiState、Model 或 Mapper：使用 `create-kmp-screen`。
- 已有 `StateMachineMviViewModel`，需要编写复杂状态流转、条件块、Flow 订阅或子状态机：使用本 skill。
- Data/Domain 错误定义与映射：使用 `data-domain`；本 skill 只约束场景错误进入状态机后的处理。
- Screen 模板、导航、Preview 和通用 Compose 结构不在本 skill 重复。

## 权威来源与现有用例

写代码前先核对当前源码，不要凭旧项目写法推断 API：

- ViewModel 契约：
  `core/ui-foundation/src/commonMain/kotlin/ciyin/ui/foundation/viewmodel/StateMachineMviViewModel.kt`
- 依赖版本：`gradle/libs.versions.toml`
- `collectWhileInState` 与 `ExecutionPolicy.Ordered`：
  `app/sample/src/commonMain/kotlin/com/ciyin/app/ui/screen/filedownloader/FileDownloaderDemoViewModel.kt`
- 状态变更后启动异步任务：
  `app/sample/src/commonMain/kotlin/com/ciyin/app/ui/screen/permissions/PermissionsViewModel.kt`
- `onEnterEffect`、Action 与流式结果回灌：
  `app/sample/src/commonMain/kotlin/com/ciyin/app/ui/screen/aichat/AiChatViewModel.kt`

项目已经有多个真实 `StateMachineMviViewModel` sample；不要继续使用“项目尚无实际继承者”的旧假设。

## 快速决策

```text
页面是否有多步骤流程、条件触发的异步逻辑或状态生命周期？
  是 -> StateMachineMviViewModel（本 skill）
  否 -> 是否需要 Action 分发？
        是 -> AbsMviViewModel
        否 -> AbsMvvmViewModel
```

三种基类都在 `ciyin.ui.foundation.viewmodel` 包内。

## 执行流程

1. 阅读 `.agents/rules/AGENTS.md`、本 skill、目标 ViewModel 和一个最接近的真实 sample。
2. 从 `initialize()` 明确唯一初始状态，从 `spec()` 明确每个状态范围内允许的 Action、Flow 和副作用。
3. 先使用 `inState`、`on`、`onEnter` 与一次状态变更完成最小流转；只有存在独立生命周期和状态模型时才拆子状态机。
4. 所有异步错误都转换为明确场景错误或 Action；不吞异常，不用 `delay` 拼业务时序。
5. 为状态转换、取消、重启和错误路径补最窄测试，再按本文件末尾清单回扫。

## 按需读取

- 普通 `initialize()` / `spec()`、`mutate` / `override` / `noChange`、单层 `condition`、直接
  `Either.fold` 和关键约束：本文件已经覆盖，不读取 `references.md`。
- 需要完整类型/签名、嵌套 Builder、`ExecutionPolicy`、`untilIdentityChanges` 或
  `collectWhileInState` 时：[references.md - DSL 完整参考](references.md#dsl-完整参考)。
- 子状态机、`actionMapper`、`onEnterStartStateMachine`、
  `onActionStartStateMachine`：[references.md - 子状态机](references.md#子状态机)。使用子状态机时必须读取该节。
- 复用 suspend handler、比较多种状态建模方式或查更多错误模式：
  [references.md - 错误处理与状态流转模式](references.md#错误处理与状态流转模式)。
- Compose 收集、文件组织和 Turbine 测试：
  [references.md - Compose、文件组织与测试](references.md#compose文件组织与测试)。

`references.md` 是本 skill 的按需参考，不要为普通单状态 Action 处理一次性加载全文。

## 核心契约

`StateMachineMviViewModel<S, A, E>` 通过两个扩展函数暴露契约，不是 abstract 属性：

| 函数 | 接收者 | 作用 |
|---|---|---|
| `initialize()` | `FlowReduxStateMachineFactory<S, A>` | 通过 `initializeWith` 设置初始状态 |
| `spec()` | `FlowReduxBuilder<S, A>` | 声明状态转换、订阅与副作用 |

最小示例：

```kotlin
/** 初始化表单页面状态。 */
override fun FlowReduxStateMachineFactory<FormUiState, FormAction>.initialize() {
    initializeWith { FormUiState() }
}

/** 声明表单页面状态流转。 */
override fun FlowReduxBuilder<FormUiState, FormAction>.spec() {
    inState<FormUiState> {
        // 用户提交表单时进入提交状态并清除旧错误。
        on<FormAction.Submit> {
            if (snapshot.isSubmitting) {
                noChange()
            } else {
                mutate { copy(isSubmitting = true, submitError = null) }
            }
        }

        // 提交状态存续期间执行提交请求。
        condition({ state -> state.isSubmitting }) {
            // 请求完成后发送成功副作用或回填场景错误。
            onEnter {
                submitFormUseCase(email = snapshot.email).fold(
                    ifLeft = { error ->
                        mutate { copy(isSubmitting = false, submitError = error) }
                    },
                    ifRight = {
                        poseEffect(FormEffect.SubmitSucceeded)
                        mutate { copy(isSubmitting = false, submitError = null) }
                    },
                )
            }
        }
    }
}
```

`initializeWith` 的完整形式为：

```kotlin
initializeWith(
    reuseLastEmittedStateOnLaunch = true,
) {
    FormUiState()
}
```

## 状态操作速查

| API | 用途 | 约束 |
|---|---|---|
| `snapshot` | 读取 handler 触发时的状态快照 | 只读，不代替最新 reduce 状态 |
| `mutate { copy(...) }` | 修改同一状态类型 | lambda 的接收者是 reduce 时的最新状态 |
| `override { NewState(...) }` | 替换状态或切换 sealed 子类型 | 不要用 `mutate` 跨类型 |
| `noChange()` | 明确不改变状态 | handler 仍必须返回 `ChangedState` |
| `poseEffect(effect)` | suspend 调用，通过当前 SharedFlow `emit` | `DROP_OLDEST` 下不保证下游消费 |
| `tryPoseEffect(effect)` | 非挂起调用 `tryEmit` | 返回值不等于“下游已收到” |

## 关键约束

### 一个 handler 只返回一次状态变更

`on`、`onEnter` 和 `collectWhileInState` 的 handler 只返回一个 `ChangedState`。不要连续调用多个
`mutate`；把字段合并到一次 `copy`。如果同一 handler 既创建状态变更又启动受控任务，保存返回值并显式
返回。`mutate` 不会立即提交状态；业务必须在状态提交后启动时，改由后续 `condition` / `onEnter` 驱动：

```kotlin
// 用户确认后创建状态变更，同时启动不依赖该状态已提交的受控任务。
on<FormAction.Confirm> {
    val changedState = mutate { copy(isSubmitting = true) }
    startSubmit(snapshot.toInput())
    changedState
}
```

### 正确选择 mutate、override 与 noChange

- 同一 data class 或 sealed 子类型内更新字段：`mutate`。
- 从 `Loading` 切换到 `Content` / `Error` 等不同子类型：`override`。
- 只发送副作用、启动受控任务或忽略重复 Action：`noChange()`。

### 正确理解 snapshot 与取消

- `snapshot` 是 handler 触发时快照；发请求所需参数从这里读取。
- `mutate` reducer 在状态队列中作用于最新状态，不要在 reducer 外复制整份旧快照覆盖并发更新。
- 离开 `inState`、`condition` 或 identity 范围时，其内部运行中的 handler/订阅会被取消。
- 不在 `spec()` 中读取 `state.value` 绕过状态机。

### 保持条件和身份选择器轻量

`condition` 谓词与 `untilIdentityChanges` selector 只做稳定、快速的属性读取。复杂筛选、IO 和业务计算放到
handler、UseCase 或 Mapper。

### 明确并发与执行策略

- 同一 `inState` 中多个 `onEnter` 会并行运行。
- 默认 `ExecutionPolicy.CancelPrevious` 适合只保留最新任务。
- 必须按顺序处理时使用 `Ordered`，互不依赖且允许并行时才使用 `Unordered`。
- 完整策略和 Flow DSL 读取 [DSL 完整参考](references.md#dsl-完整参考)。

### 副作用与协程边界

- `poseEffect` 是 suspend API，但当前 SharedFlow 使用 `DROP_OLDEST`，慢订阅者场景可能淘汰旧 Effect。
- `tryPoseEffect` 返回 `tryEmit` 结果；在当前丢弃策略下通常仍为 `true`，不能把它当作送达确认。
- 不在 `spec()` 中使用 `viewModelScope.launch { poseEffect(...) }`。
- 子状态机不能直接调用父 ViewModel 的 `poseEffect`，通过构造参数回调上送。
- 不裸用 `GlobalScope`，不通过 `delay` 等待另一状态变化。

### 为每个 DSL 调用写业务意图注释

`inState<XXX> { ... }` 内每个 `onEnter`、`onEnterEffect`、`on<Action>`、`onActionEffect<Action>`、
`collectWhileInState`、`condition`、`untilIdentityChanges`、`onEnterStartStateMachine` 和
`onActionStartStateMachine` 调用前必须有中文单行注释。注释说明业务目的，不复述 API 名称。

```kotlin
inState<PageUiState> {
    // 切换当前查看的历史条目。
    on<PageAction.PageChange> { action ->
        mutate { copy(currentIndex = action.index) }
    }
}
```

## 错误处理摘要

- Data 层只产生 `DataError`，Domain 层映射为场景错误，UI 状态机只消费场景错误。
- 对 `Either<XxxError, T>` 使用 `fold` 覆盖左右分支；不要把技术异常直接写入 UI 状态。
- 多个 DSL handler 复用加载逻辑时，提取返回类型明确的
  `suspend fun ChangeableState<XxxUiState>.load(): ChangedState<XxxUiState>`。
- 子状态机错误通过回调交给父 ViewModel，再由父 ViewModel 决定副作用或父状态变化。

完整示例见 [错误处理与状态流转模式](references.md#错误处理与状态流转模式)。

## 代码审查检查清单

- [ ] ViewModel 覆盖扩展函数 `initialize()` / `spec()`，没有声明同名 abstract 属性。
- [ ] 初始状态与真实页面入口一致，状态模型保持不可变并按项目规则使用 `@Immutable`。
- [ ] 每个 `onEnter`、`on<Action>` 或 Flow handler 只返回一次状态变更。
- [ ] 同类型更新用 `mutate`，跨类型切换用 `override`，跳过更新用 `noChange()`。
- [ ] 请求参数来自 `snapshot`，没有在 `spec()` 中读取 `state.value`。
- [ ] `condition` 谓词和 identity selector 是简单属性读取。
- [ ] 已明确默认、顺序或并行执行策略，取消语义符合业务要求。
- [ ] 异步错误通过明确的场景错误、Action 或 `Either.fold` 处理，没有吞异常。
- [ ] ViewModel 副作用使用 `poseEffect` / `tryPoseEffect`；子状态机通过回调上送副作用。
- [ ] 子状态机的 `actionMapper` 对无关 Action 返回 `null`，handler 将子状态同步回父状态。
- [ ] `inState` 内每个 DSL 调用前都有描述业务意图的中文单行注释。
- [ ] 新增类型、函数和属性具有符合仓库规则的中文 KDoc。
- [ ] `LazyListState`、`PagerState` 等 Compose 运行时状态不放入 `UiState`。
- [ ] 没有裸用 `GlobalScope`、用 `delay` 拼业务时序，或额外 `launch` 发送普通副作用。
- [ ] 使用子状态机时已读取 [子状态机参考](references.md#子状态机)，并让启动扩展与子状态机类同文件。
- [ ] 状态转换、错误、取消与重启路径有最窄相关测试。
