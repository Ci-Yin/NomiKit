# FlowRedux2 状态机参考

本文件配套 `SKILL.md` 使用，只在任务命中对应进阶主题时按需读取。API 与示例基于 NomiKit 当前
`StateMachineMviViewModel` 契约和 FlowRedux2 `2.0.1`。

## 目录

- [DSL 完整参考](#dsl-完整参考)
  - [核心类型体系](#核心类型体系)
  - [Builder 嵌套与 DSL](#builder-嵌套与-dsl)
  - [ExecutionPolicy](#executionpolicy)
  - [状态与副作用语义](#状态与副作用语义)
- [子状态机](#子状态机)
  - [拆分条件与两种形态](#拆分条件与两种形态)
  - [完整子状态机与启动扩展](#完整子状态机与启动扩展)
  - [启动 API 签名](#启动-api-签名)
  - [生命周期与同步](#生命周期与同步)
- [错误处理与状态流转模式](#错误处理与状态流转模式)
  - [Either 与复用 handler](#either-与复用-handler)
  - [常见状态流转](#常见状态流转)
- [Compose、文件组织与测试](#compose文件组织与测试)
  - [Compose 收集](#compose-收集)
  - [文件组织](#文件组织)
  - [测试](#测试)

## DSL 完整参考

### 核心类型体系

```text
FlowReduxStateMachineFactory<S, A>
    + initializeWith { }
    + spec { }
    + installLogger(logger, name)
    + launchIn(scope) -> FlowReduxStateMachine<StateFlow<S>, A>
    + shareIn(scope)  -> FlowReduxStateMachine<SharedFlow<S>, A>

FlowReduxStateMachine<S, A>
    + state: S
    + dispatchAction: (A) -> Unit
    + dispatch(action: A)

StateMachineMviViewModel<S, A, E>
    + state: StateFlow<S>
    + dispatchAction: (A) -> Unit
    + sideEffects: SharedFlow<E>
    + poseEffect(effect)
    + tryPoseEffect(effect)
```

状态变更类型：

```text
State<InputState>
    + snapshot: InputState
    + ChangeableState<InputState>
        + mutate { }   -> ChangedState<InputState>
        + override { } -> ChangedState<S>
        + noChange()   -> ChangedState<Nothing>
```

- `onEnter`、`on<Action>`、`collectWhileInState` 的 handler 接收者是 `ChangeableState`，必须返回
  `ChangedState`。
- `onEnterEffect`、`onActionEffect`、`collectWhileInStateEffect` 的 handler 接收者是只读 `State`，
  返回 `Unit`。
- `snapshot` 是触发时快照；`mutate` 的 reducer 最终应用到队列中的最新状态。

### Builder 嵌套与 DSL

```text
FlowReduxBuilder<S, A>
    + inState<SubState> { } -> InStateBuilder<SubState, S, A>
        + condition { } -> ConditionBuilder<SubState, S, A>
            + untilIdentityChanges { } -> IdentityBuilder<SubState, S, A>
        + untilIdentityChanges { } -> IdentityBuilder<SubState, S, A>

BaseBuilder<InputState, S, A>
    + InStateBuilder
    + ConditionBuilder
    + IdentityBuilder
```

所有 Builder 都继承 `BaseBuilder`，因此能使用 `on`、`onEnter`、Flow 订阅和子状态机启动 API。
`IdentityBuilder` 不再嵌套新的 `condition` 或 identity 块。

完整 DSL 形状：

```kotlin
spec {
    inState<PageUiState> {
        // 进入页面后加载首屏数据。
        onEnter {
            loadFirstPage()
        }

        // 进入页面后上报曝光事件。
        onEnterEffect {
            reportExposure(snapshot.pageId)
        }

        // 用户提交表单时发起保存流程。
        on<PageAction.Submit>(
            executionPolicy = ExecutionPolicy.CancelPrevious,
        ) { action ->
            submit(action.input)
        }

        // 用户点击返回时发送导航副作用。
        onActionEffect<PageAction.BackClick> {
            poseEffect(PageEffect.NavigateBack)
        }

        // 监听外部网络状态并同步页面状态。
        collectWhileInState(
            flow = networkStatusFlow,
            executionPolicy = ExecutionPolicy.Ordered,
        ) { isOnline ->
            mutate { copy(isOnline = isOnline) }
        }

        // 根据进入状态时的参数构建倒计时流。
        collectWhileInState(
            flowBuilder = { state -> countdownFlow(state.remainingSeconds) },
            executionPolicy = ExecutionPolicy.Ordered,
        ) { remaining ->
            mutate { copy(remainingSeconds = remaining) }
        }

        // 转发只产生副作用的一次性事件。
        collectWhileInStateEffect(messageEvents) { message ->
            poseEffect(PageEffect.Message(message))
        }

        // 仅在提交状态下处理提交结果。
        condition({ state -> state.isSubmitting }) {
            // 提交状态建立后执行请求。
            onEnter {
                submitCurrentForm()
            }
        }

        // 选中条目变化时重启详情加载流程。
        untilIdentityChanges({ state -> state.selectedId }) {
            // 当前选中条目建立后加载详情。
            onEnter {
                loadSelectedDetail(snapshot.selectedId)
            }
        }
    }
}
```

常用签名与用途：

| DSL | handler 接收者 | 用途 |
|---|---|---|
| `onEnter` | `ChangeableState<InputState>` | 进入范围时执行并改变状态 |
| `onEnterEffect` | `State<InputState>` | 进入范围时执行纯副作用 |
| `on<Action>` | `ChangeableState<InputState>` | 处理 Action 并改变状态 |
| `onActionEffect<Action>` | `State<InputState>` | 处理只产生副作用的 Action |
| `collectWhileInState` | `ChangeableState<InputState>` | 范围存续期间收集 Flow 并改变状态 |
| `collectWhileInStateEffect` | `State<InputState>` | 范围存续期间收集纯副作用 Flow |
| `condition` | Builder | 用轻量谓词限制生命周期范围 |
| `untilIdentityChanges` | Builder | selector 变化时取消并重启内部操作 |

### ExecutionPolicy

| 策略 | 内部行为 | 适用场景 |
|---|---|---|
| `CancelPrevious` | 取消上一个同类执行，只保留最新任务 | 搜索、重复提交、选择变化 |
| `Ordered` | 按到达顺序串行执行 | 状态事件、必须保序的写入 |
| `Unordered` | 并行执行 | 互不依赖的预加载或上报 |
| `Throttled(duration)` | 首值立即通过；窗口内或 handler 仍运行时丢弃后续值 | 高频且允许限频的进度事件 |

不要仅凭“更快”选择 `Unordered`。如果结果会写入同一字段，必须先证明乱序完成不会产生陈旧覆盖。

### 状态与副作用语义

一个 handler 只返回一个 `ChangedState`：

```kotlin
// 错误：第一次 mutate 的返回值被丢弃。
on<PageAction.Submit> {
    mutate { copy(isLoading = true) }
    mutate { copy(error = null) }
}

// 正确：一次合并全部状态变化。
on<PageAction.Submit> {
    mutate { copy(isLoading = true, error = null) }
}
```

`mutate` 与 `override`：

```kotlin
inState<PageState.Content> {
    // 加载更多时只更新当前 Content 状态。
    on<PageAction.LoadMore> {
        mutate { copy(isLoadingMore = true) }
    }
}

inState<PageState.Loading> {
    // 首屏加载完成后切换到 Content 子类型。
    on<PageAction.Loaded> { action ->
        override { PageState.Content(items = action.items) }
    }
}
```

副作用：

- `poseEffect(effect)` 是 suspend 调用并最终调用 `emit`；项目 SharedFlow 使用 `DROP_OLDEST`，因此慢订阅者
  场景可能淘汰旧 Effect，不提供“必达”保证。
- `tryPoseEffect(effect)` 使用 `tryEmit`。在当前 `extraBufferCapacity = 1` 与 `DROP_OLDEST` 配置下，
  缓冲溢出通常以丢弃旧值处理，返回 `true` 也不表示下游已经消费。
- 两者是 `StateMachineMviViewModel` 能力，不是普通 `FlowReduxStateMachineFactory` 的 API。
- 状态范围退出会取消其内部 handler。不要捕获并吞掉 `CancellationException`。

## 子状态机

### 拆分条件与两种形态

只有同时满足以下条件才创建完整子状态机：

- 流程有独立生命周期，进入父状态或条件范围时启动，离开时取消；
- 流程有独立的 `ChildState` 与 `ChildAction`；
- 流程能与父状态解耦，父状态只负责初始值、Action 映射、状态同步与副作用出口。

简单的一次性加载先写 `BaseBuilder` 扩展：

```kotlin
/** 在 Active 状态下加载详情。 */
private fun <Action : Any> BaseBuilder<
    ParentState.Active,
    ParentState,
    Action,
>.loadDetailForActive(
    loadDetailUseCase: LoadDetailUseCase,
) {
    // 进入 Active 后加载对应详情。
    onEnter {
        loadDetailUseCase(snapshot.id).fold(
            ifLeft = { error ->
                mutate { copy(detail = DetailState.Error(error)) }
            },
            ifRight = { detail ->
                mutate { copy(detail = DetailState.Content(detail)) }
            },
        )
    }
}
```

逻辑需要自己的生命周期、Action 和状态时，再使用完整类。

### 完整子状态机与启动扩展

```kotlin
/** Active 父状态下运行的子状态机。 */
internal class ChildStateMachine(
    private val parentId: Long,
    private val initialState: ChildState,
    private val loadChildUseCase: LoadChildUseCase,
    private val onError: suspend (ChildError) -> Unit,
) : FlowReduxStateMachineFactory<ChildState, ChildAction>() {

    init {
        initializeWith { initialState }

        spec {
            inState<ChildState> {
                // 用户刷新子流程时进入加载状态。
                on<ChildAction.Refresh> {
                    mutate { copy(isLoading = true) }
                }

                // 加载状态建立后请求子流程数据。
                condition({ state -> state.isLoading }) {
                    // 请求完成后回填数据或上送场景错误。
                    onEnter {
                        loadChildUseCase(parentId).fold(
                            ifLeft = { error ->
                                onError(error)
                                mutate { copy(isLoading = false) }
                            },
                            ifRight = { items ->
                                mutate { copy(isLoading = false, items = items) }
                            },
                        )
                    }
                }
            }
        }
    }
}

/** 在 Active 状态下启动并同步子状态机。 */
private fun BaseBuilder<
    ParentState.Active,
    ParentState,
    ParentAction,
>.startChildStateMachineForActive(
    loadChildUseCase: LoadChildUseCase,
    onError: suspend (ChildError) -> Unit,
) {
    // Active 存续期间运行子流程并同步其状态。
    onEnterStartStateMachine(
        stateMachineFactoryBuilder = {
            ChildStateMachine(
                parentId = snapshot.id,
                initialState = snapshot.child,
                loadChildUseCase = loadChildUseCase,
                onError = onError,
            )
        },
        actionMapper = { action ->
            when (action) {
                ParentAction.RefreshChild -> ChildAction.Refresh
                else -> null
            }
        },
        handler = { childState ->
            mutate { copy(child = childState) }
        },
    )
}
```

在父 ViewModel 中调用：

```kotlin
override fun FlowReduxBuilder<ParentState, ParentAction>.spec() {
    inState<ParentState.Active> {
        // Active 生命周期内运行子状态机。
        startChildStateMachineForActive(
            loadChildUseCase = loadChildUseCase,
            onError = { error -> poseEffect(ParentEffect.ChildFailed(error)) },
        )
    }
}
```

启动扩展函数与子状态机类放在同一个 `.kt` 文件。简单子流程可以只保留一个 `XxxBuilder.kt`；完整子流程按
`XxxStateMachine.kt`、`XxxState.kt`、`XxxAction.kt` 拆分。

### 启动 API 签名

进入状态时启动并映射 Action：

```kotlin
public fun <ChildState : Any, ChildAction : Any> onEnterStartStateMachine(
    stateMachineFactoryBuilder: State<InputState>.() ->
        FlowReduxStateMachineFactory<ChildState, ChildAction>,
    actionMapper: (A) -> ChildAction?,
    cancelOnState: (ChildState) -> Boolean = { false },
    name: String? = null,
    handler: suspend ChangeableState<InputState>.(ChildState) -> ChangedState<S>,
)
```

不需要独立 Action 映射时使用与父 Action 同类型的重载：

```kotlin
public fun <ChildState : Any> onEnterStartStateMachine(
    stateMachineFactoryBuilder: State<InputState>.() ->
        FlowReduxStateMachineFactory<ChildState, A>,
    cancelOnState: (ChildState) -> Boolean = { false },
    name: String? = null,
    handler: suspend ChangeableState<InputState>.(ChildState) -> ChangedState<S>,
)
```

由特定 Action 启动：

```kotlin
public inline fun <
    reified TriggerAction : A,
    ChildState : Any,
> onActionStartStateMachine(
    noinline stateMachineFactoryBuilder: State<InputState>.(TriggerAction) ->
        FlowReduxStateMachineFactory<ChildState, A>,
    noinline cancelOnState: (ChildState) -> Boolean = { false },
    name: String? = null,
    noinline handler: suspend ChangeableState<InputState>.(ChildState) -> ChangedState<S>,
)
```

子状态机使用独立 Action 类型时增加 `actionMapper`：

```kotlin
public inline fun <
    reified TriggerAction : A,
    ChildState : Any,
    ChildAction : Any,
> onActionStartStateMachine(
    noinline stateMachineFactoryBuilder: State<InputState>.(TriggerAction) ->
        FlowReduxStateMachineFactory<ChildState, ChildAction>,
    noinline actionMapper: (A) -> ChildAction?,
    noinline cancelOnState: (ChildState) -> Boolean = { false },
    name: String? = null,
    noinline handler: suspend ChangeableState<InputState>.(ChildState) -> ChangedState<S>,
)
```

| 扩展接收者 | 使用位置 |
|---|---|
| `BaseBuilder<Parent, Root, Action>` | `inState` 中直接启动 |
| `ConditionBuilder<Parent, Root, Action>` | `condition` 内启动 |
| `IdentityBuilder<Parent, Root, Action>` | `untilIdentityChanges` 内启动 |
| `BaseBuilder<Parent.Active, Root, Action>` | 限定父状态子类型 |

### 生命周期与同步

- 子状态机随所属 `inState` / `condition` / identity 范围启动和取消。
- `actionMapper` 对无关父 Action 返回 `null`；不要把所有父 Action 转发给子机。
- `handler` 每次收到子状态时只返回一次父状态变更；无需更新时返回 `noChange()`。
- `cancelOnState` 只描述子状态达到何种条件时结束，不承担业务错误处理。
- 子状态机没有父 ViewModel 的副作用 API，通过构造回调上送明确事件或场景错误。

外部资源与状态生命周期绑定：

```kotlin
inState<ChildState.Active> {
    // Active 存续期间持有 controller，退出时保证释放。
    onEnterEffect {
        try {
            awaitCancellation()
        } finally {
            controller.release()
        }
    }
}
```

handler 可联动父状态，但应基于当前 `mutate` 接收者合并：

```kotlin
handler = { childState ->
    val entering = childState.isActive && !snapshot.child.isActive
    mutate {
        copy(
            child = childState,
            sibling = if (entering) sibling.pause() else sibling,
        )
    }
}
```

子状态机检查：

- [ ] 初始状态由父状态或构造参数明确传入。
- [ ] `initializeWith` 与 `spec` 在子状态机 `init` 中各调用一次。
- [ ] `actionMapper` 只映射相关 Action。
- [ ] handler 将子状态同步回父状态并返回 `ChangedState`。
- [ ] 副作用通过回调交给父 ViewModel。
- [ ] 启动扩展与子状态机类位于同一文件。

## 错误处理与状态流转模式

### Either 与复用 handler

状态机只消费 Domain 层场景错误：

```kotlin
inState<PageUiState> {
    // 用户刷新页面时重新加载数据。
    on<PageAction.Refresh> {
        loadPageUseCase(snapshot.query).fold(
            ifLeft = { error ->
                mutate { copy(isLoading = false, error = error) }
            },
            ifRight = { data ->
                mutate { copy(isLoading = false, data = data, error = null) }
            },
        )
    }
}
```

多个入口复用时提取返回类型明确的 suspend 扩展：

```kotlin
/** 加载当前查询并返回一次状态变更。 */
private suspend fun ChangeableState<PageUiState>.loadPage(): ChangedState<PageUiState> =
    loadPageUseCase(snapshot.query).fold(
        ifLeft = { error ->
            mutate { copy(isLoading = false, error = error) }
        },
        ifRight = { data ->
            mutate { copy(isLoading = false, data = data, error = null) }
        },
    )

override fun FlowReduxBuilder<PageUiState, PageAction>.spec() {
    inState<PageUiState> {
        // 首次进入页面时加载当前查询。
        onEnter { loadPage() }

        // 用户刷新时复用同一加载流程。
        on<PageAction.Refresh> { loadPage() }
    }
}
```

同一 handler 同时创建状态变更并启动受控任务时，必须保留并返回 `ChangedState`：

```kotlin
// 创建忙碌状态变更，同时启动不依赖该状态已提交的平台请求。
on<PageAction.RequestPermission> { action ->
    val changedState = mutate { copy(activePermission = action.permission) }
    requestPermission(action.permission)
    changedState
}
```

这里的 `mutate` 只创建待 reduce 的 `ChangedState`，任务会在 handler 返回、状态提交之前启动。若任务必须
观察到新状态或依赖新状态范围，应先返回状态变更，再由对应 `condition` / `inState` 的 `onEnter` 启动。

子状态机通过回调上送错误，不直接引用父 ViewModel：

```kotlin
ChildStateMachine(
    initialState = snapshot.child,
    onError = { error -> poseEffect(ParentEffect.ChildFailed(error)) },
)
```

### 常见状态流转

加载、内容与错误使用 sealed 状态：

```kotlin
sealed interface PageState {
    data object Loading : PageState
    data class Content(val items: List<Item>) : PageState
    data class Error(val error: PageError) : PageState
}

spec {
    inState<PageState.Loading> {
        // 进入加载态后请求首屏数据。
        onEnter {
            loadPageUseCase().fold(
                ifLeft = { error -> override { PageState.Error(error) } },
                ifRight = { items -> override { PageState.Content(items) } },
            )
        }
    }

    inState<PageState.Error> {
        // 用户重试时重新进入加载态。
        on<PageAction.Retry> {
            override { PageState.Loading }
        }
    }
}
```

单一 data class 用条件块承载异步生命周期：

```kotlin
inState<FormUiState> {
    // 用户提交时建立提交状态。
    on<FormAction.Submit> {
        mutate { copy(isSubmitting = true, submitError = null) }
    }

    // 提交状态存续期间执行请求。
    condition({ state -> state.isSubmitting }) {
        // 请求完成后退出提交状态。
        onEnter {
            submitUseCase(snapshot.toInput()).fold(
                ifLeft = { error -> mutate { copy(isSubmitting = false, submitError = error) } },
                ifRight = {
                    poseEffect(FormEffect.SubmitSucceeded)
                    mutate { copy(isSubmitting = false) }
                },
            )
        }
    }
}
```

identity 变化时取消旧加载并重启：

```kotlin
inState<PageUiState> {
    // 选中标签变化时重启标签内容加载。
    untilIdentityChanges({ state -> state.selectedTabId }) {
        // 当前标签建立后加载对应内容。
        onEnter {
            val data = loadTabUseCase(snapshot.selectedTabId)
            mutate { copy(currentTabData = data) }
        }
    }
}
```

监听外部状态：

```kotlin
inState<PageUiState> {
    // 页面存续期间同步网络状态。
    collectWhileInState(networkStatusFlow) { isOnline ->
        if (isOnline == snapshot.isOnline) {
            noChange()
        } else {
            mutate { copy(isOnline = isOnline) }
        }
    }
}
```

倒计时由状态范围管理，不要用它等待另一个业务状态：

```kotlin
inState<PageState.Cooldown> {
    // Cooldown 存续期间逐秒更新剩余时间。
    collectWhileInState(
        flowBuilder = { state -> countdownFlow(state.remainingSeconds) },
    ) { remaining ->
        if (remaining <= 0) {
            override { PageState.Ready }
        } else {
            mutate { copy(remainingSeconds = remaining) }
        }
    }
}
```

同一状态内多个 `onEnter` 并行执行。只有各 handler 更新互不覆盖的字段，且失败策略彼此独立时才这样写：

```kotlin
inState<HomeUiState> {
    // 进入首页后加载分类。
    onEnter { loadCategories() }

    // 进入首页后并行加载公告。
    onEnter { loadNotices() }
}
```

## Compose、文件组织与测试

### Compose 收集

完整 Screen 脚手架使用 `create-kmp-screen`。与状态机有关的收集方式：

```kotlin
@Composable
fun PageScreen(
    viewModel: PageViewModel = viewModel(::PageViewModel),
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    viewModel.collectSideEffects { effect ->
        when (effect) {
            PageEffect.NavigateBack -> onBack()
        }
    }

    PageContent(
        state = state,
        onAction = viewModel.dispatchAction,
    )
}
```

使用：

```kotlin
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ciyin.ui.foundation.viewmodel.collectSideEffects
import ciyin.ui.foundation.viewmodel.viewModel
```

- 不使用项目中不存在的 `Toaster`、`LocalNavigator` 或 `koinViewModel()`。
- 导航、消息等一次性事件通过 Effect 交给 Screen，或使用项目当前可用的 `UiEffectHandler`。
- `LazyListState`、`PagerState` 等 Compose runtime 状态留在 Compose 层，不放入 `UiState`。

### 文件组织

复杂页面在 screen 目录下增加 `statemachine/`：

```text
app/shared/src/commonMain/kotlin/com/ciyin/app/ui/screen/page/
    PageScreen.kt
    PageViewModel.kt
    PageUiState.kt
    PageAction.kt
    PageEffect.kt
    statemachine/
        ChildBuilder.kt
        child/
            ChildStateMachine.kt
            ChildState.kt
            ChildAction.kt
            ChildEffect.kt
```

命名：

| 类型 | 命名 |
|---|---|
| 子状态机 | `ChildStateMachine` |
| 启动扩展 | `startChildStateMachineForActive` |
| 子状态 | `ChildState` |
| 子 Action | `ChildAction` |
| 子 Effect | `ChildEffect` |

简单子流程只有 Builder 扩展时使用 `ChildBuilder.kt`。完整子状态机的启动扩展与类必须同文件，便于同步
生命周期和 Action 映射契约。

### 测试

使用 `shareIn` 与 Turbine 观察不被 `StateFlow` 合并的状态序列：

```kotlin
@Test
fun `loading action moves state to content`() = runTest {
    val factory = PageStateMachineFactory(loadPageUseCase)
    factory.initializeWith { PageState.Loading }
    val stateMachine = factory.shareIn(backgroundScope)

    stateMachine.state.test {
        assertEquals(PageState.Loading, awaitItem())
        stateMachine.dispatch(PageAction.Load)
        assertEquals(PageState.Content(items), awaitItem())
    }
}
```

直接测试 handler reduce：

```kotlin
@Test
fun `increment handler increases count`() = runTest {
    val state = ChangeableState(CounterState(count = 0))
    val changedState: ChangedState<CounterState> = incrementHandler(state)
    val actual = changedState.reduce(state.snapshot)

    assertEquals(CounterState(count = 1), actual)
}
```

至少覆盖：

- 初始状态；
- 每个 Action 的成功、场景错误与忽略分支；
- `mutate` / `override` 的目标状态；
- `condition` 与 identity 进入、退出、取消和重启；
- 子状态机 Action 映射、状态同步、错误回调与取消；
- 需要保序或并行的 `ExecutionPolicy` 行为；
- Effect 缓冲溢出与 `DROP_OLDEST` 语义，不把 `emit` / `tryEmit` 返回视为下游送达确认。
