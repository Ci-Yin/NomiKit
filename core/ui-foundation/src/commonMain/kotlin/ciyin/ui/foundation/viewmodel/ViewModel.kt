package ciyin.ui.foundation.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2025/11/13 21:23
 */

/**
 * 一个 Composable 函数，它使用一个简单的工厂来提供一个 [ViewModel] 实例。
 *
 * 这是对标准的 [androidx.lifecycle.viewmodel.compose.viewModel] 的一个便捷包装，
 * 用于在不需要 `SavedStateHandle` 的情况下简化 ViewModel 的创建。
 *
 * @param VM 要创建的 [ViewModel] 的类型。
 * @param factory 一个创建 [ViewModel] 实例的 lambda 函数。
 * @param key 用于标识 [ViewModel] 实例的可选键。
 * @return 一个指定 [ViewModel] 的实例。
 */
@Composable
inline fun <reified VM : ViewModel> viewModel(
    crossinline factory: () -> VM,
    key: String? = null,
): VM {
    return viewModel(key = key) { factory() }
}

/**
 * 一个 Composable 函数，它使用一个接收 [SavedStateHandle] 的工厂来提供一个 [ViewModel] 实例。
 *
 * 这个函数是为跨平台环境设计的。它会尝试创建一个适合当前平台的 `SavedStateHandle`。
 * 在 Android 上，它使用 `createSavedStateHandle()`。在其他平台上，
 * 它会优雅地回退到一个默认的 `SavedStateHandle()`，从而允许 ViewModel 被创建而不会导致崩溃。
 *
 * @param VM 要创建的 [ViewModel] 的类型。
 * @param key 用于标识 [ViewModel] 实例的可选键。
 * @param factory 一个接收 [SavedStateHandle] 并返回 [ViewModel] 实例的 lambda 函数。
 * @return 一个指定 [ViewModel] 的实例。
 */
@Composable
inline fun <reified VM : ViewModel> viewModel(
    crossinline factory: (SavedStateHandle) -> VM,
    key: String? = null
): VM {
    return viewModel(key = key) {
        val handle = try {
            // ✅ Android 正常创建
            createSavedStateHandle()
        } catch (_: Throwable) {
            // ✅ 非 Android 环境优雅降级
            SavedStateHandle()
        }
        factory(handle)
    }
}