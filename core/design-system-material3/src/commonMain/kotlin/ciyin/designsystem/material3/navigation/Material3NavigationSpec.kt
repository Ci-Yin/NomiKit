package ciyin.designsystem.material3.navigation

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import ciyin.designsystem.component.navigation.NomiNavigationSpec

/**
 * Material 3 导航套件未进入公共 API 的专属参数，实现导航套件族的契约 [NomiNavigationSpec]。
 *
 * 字段按布局生效：Bar 消费 contentColor/tonalElevation/windowInsets，Rail 消费
 * contentColor/windowInsets/railHeader，Drawer 消费 contentColor/tonalElevation/windowInsets；
 * 不适用的字段在该布局被忽略。
 *
 * @property contentColor 导航外壳内容色；null 由原生按容器色推导。
 * @property tonalElevation 导航外壳色调阴影；null 保留该布局的原生默认高度。
 * @property windowInsets 导航外壳窗口内边距；null 保留该布局的原生默认。
 * @property railHeader 仅 Rail 可见的顶部头槽；null 表示不占位。
 */
@Immutable
data class Material3NavigationSpec(
    val contentColor: Color? = null,
    val tonalElevation: Dp? = null,
    val windowInsets: WindowInsets? = null,
    val railHeader: (@Composable ColumnScope.() -> Unit)? = null,
) : NomiNavigationSpec
