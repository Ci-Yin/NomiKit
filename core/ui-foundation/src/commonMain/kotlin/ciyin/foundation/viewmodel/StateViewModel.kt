package ciyin.foundation.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow

interface StateViewModel<S : Any> {

    /**
     * 表示当前 ViewModel 的状态流。这是一个不可变的状态流（StateFlow），用于向 UI 提供状态更新。
     *
     * - 此状态流包含与业务逻辑相关的状态数据。
     * - 该流对象始终具有最新状态，并向订阅者实时推送更新。
     * - 适用于界面通过状态绑定的 MVI/MVVM 架构下的单一数据源模式。
     */
    val state: StateFlow<S>

}

@Composable
fun <S : Any> StateViewModel<S>.stateCollectAsStateWithLifecycle() =
    state.collectAsStateWithLifecycle()

