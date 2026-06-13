---
name: core-coroutines
description: Use the core/coroutines Kotlin Multiplatform coroutine helper module. Covers childScope/childScopeContext, cancellableCoroutineScope, catching, shareTransparentlyIn, debounceWithInitial, throttle, resetStale, FlowRestarter, FlowRunning, and high-arity combine/combineTransform overloads. Use when 用户要在 NomiKit 中使用或维护 core/coroutines、编排协程作用域、处理 Flow 异常/节流/防抖/重启/运行状态，或排查该模块构建问题。
---

# core/coroutines 使用指南

`core/coroutines` 是 NomiKit 对 `kotlinx.coroutines` 的薄辅助层。优先使用标准协程 API；只有需要本模块的约定语义时，再引入 `ciyin.coroutines.*` 或 flow helper。

## 作用域 helper

```kotlin
val child = parentScope.childScope()
val childWithDispatcher = parentScope.childScope(Dispatchers.Default)
```

注意事项：

- `childScopeContext` 会从父 `CoroutineContext` 中取 `Job`，并创建 `SupervisorJob(parentJob)`。
- 传入的附加 `coroutineContext` 不能包含 `Job`，否则会 `check` 失败。
- 不要用这些 helper 绕过 ViewModel、状态机、UseCase 等既有生命周期；它们只负责派生结构化子作用域。

## 可取消 coroutineScope

```kotlin
val result = cancellableCoroutineScope(
    onCancel = { fallback },
) {
    launch { work() }
    cancelScope()
    value
}
```

注意事项：

- `cancelScope()` 使用内部 owner 标记的 `OwnedCancellationException`，只吞掉当前 scope 自己发起的取消。
- 无 `onCancel` 重载返回 `R?`，取消时返回 `null`。
- 外部取消或其它 `CancellationException` 不会被误吞。

## Flow 异常与共享

```kotlin
val resultFlow: Flow<Result<Item>> = source.catching()
val shared = source.shareTransparentlyIn(scope, SharingStarted.WhileSubscribed(), replay = 1)
```

注意事项：

- `catching()` 把非取消异常包装成 `Result.failure`，但会继续抛出 `CancellationException`。
- `shareTransparentlyIn()` 内部先 `catching()` 再 `shareIn()`，下游 `map { it.getOrThrow() }` 会重新抛出上游异常。
- 使用 `SharingStarted.WhileSubscribed` 且 `replay != 0` 时，历史异常可能被重新发送；需要重启语义时结合 `FlowRestarter`。

## Flow 节流、防抖、重启

```kotlin
val restarter = FlowRestarter()
val flow = queryFlow
    .debounceWithInitial(300)
    .throttle(1.seconds)
    .restartable(restarter)
```

注意事项：

- `debounceWithInitial` 会立即发送第一个值，之后按指定时间防抖，适合 `StateFlow` 初始值场景。
- `throttle` 在时间窗口内只发送第一个到达的值，会忽略窗口内后续值。
- `resetStale(durationMillis) { ... }` 会在一段时间没有新值时执行 `reset`，内部有定时循环，避免用于高频短生命周期流。
- `FlowRestarter.restart()` 通过递增内部 id 触发 `flatMapLatest` 重新订阅上游。
- 当前 `FlowRestarter.kt`、`FlowRunning.kt` 包名是 `coroutines.flows`，而不是 `ciyin.coroutines.flows`，使用前先确认 import。

## 高阶 combine

本模块补充了超过标准库常见数量的 `combine` / `combineTransform` 重载，适合多个状态流聚合为一个 UI 模型。

注意事项：

- 流数量过多时优先考虑拆分模型或先聚合局部状态，不要让单个 `combine` 承担不可读的大型编排。
- `combineTransform` 适合需要 `emit` 多次或挂起转换的场景；普通映射优先用 `combine`。

## 修改注意

- 新增 Kotlin API 遵守项目规则补中文 KDoc。
- 修改本模块后优先运行 `.\gradlew.bat :core:coroutines:compileCommonMainKotlinMetadata --console=plain`。
