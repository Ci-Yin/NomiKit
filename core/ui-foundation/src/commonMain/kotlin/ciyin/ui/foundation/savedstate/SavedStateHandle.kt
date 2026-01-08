package ciyin.ui.foundation.savedstate

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.InternalSerializationApi


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/11/13 23:22
 */


/**
 * 创建一个与 [SavedStateHandle] 关联的 [MutableStateFlow]
 *
 * - 此函数用于创建一个可观察的状态流，该状态流的值会自动保存到 [SavedStateHandle] 中，
 * 从而在组件重建时保持状态。当状态发生变化时，会自动更新 [SavedStateHandle] 中对应的值。
 * - 仅在 `Android` 平台上可用，在其他平台上只会返回 [MutableStateFlow] 的默认实现。
 *
 * @param T 状态值的类型，必须为非空类型
 * @param scope 用于启动协程的 [CoroutineScope]，用于订阅流监听状态值的变化并更新 [SavedStateHandle]
 * @param key 用于在 [SavedStateHandle] 中存储状态值的键名
 * @param initialValue 状态的初始值，当 [SavedStateHandle] 中不存在对应键时使用此值
 * @return 返回一个与 [SavedStateHandle] 绑定的 [MutableStateFlow]，对其值的修改会自动保存
 */
@Suppress("UNCHECKED_CAST")
@OptIn(InternalSerializationApi::class)
expect fun <T : Any> SavedStateHandle.createSavedMutableStateFlow(
    scope: CoroutineScope,
    key: String,
    initialValue: T
): MutableStateFlow<T>
