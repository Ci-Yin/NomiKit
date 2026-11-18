package ciyin.designsystem.material3.scaffold

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import ciyin.designsystem.component.scaffold.NomiScaffoldSpec

/**
 * Material 3 页面脚手架未进入公共 API 的专属参数，实现脚手架族的契约 [NomiScaffoldSpec]。
 *
 * @property snackbarHost 原生提示条宿主槽；null 表示不占位。
 * @property floatingActionButton 原生悬浮按钮槽；null 表示不占位。
 * @property contentWindowInsets 主体内容的窗口内边距；null 保留 `ScaffoldDefaults.contentWindowInsets`。
 */
@Immutable
data class Material3ScaffoldSpec(
    val snackbarHost: (@Composable () -> Unit)? = null,
    val floatingActionButton: (@Composable () -> Unit)? = null,
    val contentWindowInsets: WindowInsets? = null,
) : NomiScaffoldSpec
