package ciyin.designsystem.fluent.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import ciyin.designsystem.component.navigation.NomiNavigationSpec

/**
 * Fluent 导航套件未进入公共 API 的专属参数，实现导航套件族的契约 [NomiNavigationSpec]。
 *
 * @property title 原生侧栏标题槽；null 表示不占位，仅在展开态可见。
 * @property backButton 原生侧栏顶部返回按钮槽；null 表示不占位。
 */
@Immutable
data class FluentNavigationSpec(
    val title: (@Composable () -> Unit)? = null,
    val backButton: (@Composable () -> Unit)? = null,
) : NomiNavigationSpec
