package ciyin.ui.foundation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ciyin.ui.foundation.savedstate.createSavedMutableStateFlow
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 一个通用的 MVVM ViewModel 基类。
 *
 * S: UI 状态类型（State）
 * E: 一次性事件类型（Effect），例如导航、Toast、SnackBar 等不应写入状态的副作用
 *
 * 特性：
 * - 使用 StateFlow 持有并暴露 UI 状态
 * - 使用 SharedFlow 发送一次性事件
 * - 提供 setState/updateState 安全更新状态
 */
abstract class AbsMvvmViewModel<S : Any, E : Any>(
    savedStateHandle: SavedStateHandle?,
) : AbstractViewModel(),
    StateViewModel<S>,
    EffectViewModel<E> {

    // 内部可变状态
    private val _state by lazy {
        val initialValue = initState()
        savedStateHandle?.createSavedMutableStateFlow(
            scope = viewModelScope,
            key = "${this::class.qualifiedName}.state",
            initialValue = initialValue
        ) ?: MutableStateFlow(initialValue)
    }

    // 对外只读状态
    override val state: StateFlow<S> by lazy { _state.asStateFlow() }

    protected val stateValue: S get() = _state.value

    // 一次性事件：replay=0、不保留历史；额外缓冲 1；溢出丢弃最旧
    private val _sideEffects = MutableSharedFlow<E>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val sideEffects: SharedFlow<E> = _sideEffects

    protected abstract fun initState(): S

    /**
     * 基于当前状态做不可变更新。
     * 注意：transform 应该是快速且纯函数，避免重活。
     */
    protected fun updateState(transform: S.() -> S) {
        _state.value = transform(_state.value)
    }


    override suspend fun poseEffect(effect: E) {
        _sideEffects.emit(effect)
    }

    override fun tryPoseEffect(effect: E): Boolean = _sideEffects.tryEmit(effect)

}