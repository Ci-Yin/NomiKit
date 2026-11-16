package ciyin.designsystem.component.surface

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import ciyin.designsystem.component.NomiBorder
import ciyin.designsystem.runtime.requireNomiDesignSystem
import ciyin.designsystem.theme.NomiTheme

/**
 * 容器与非点击卡片分别派发，避免用统一表面配方代替各系统原生 Card。
 */
@Stable
interface NomiSurfaceRenderer {
    /**
     * 使用原生表面默认值，只应用调用方显式指定的参数。
     *
     * @param modifier 应用于表面根节点的修饰符。
     * @param shape 表面轮廓；null 使用当前设计系统的默认形状。
     * @param colors 容器色与内容色的逐字段覆盖；Unspecified 保留原生值，Transparent 是有效覆盖。
     * @param border Default 保留原生描边，None 移除描边，Stroke 指定描边。
     * @param content 表面内组合的内容，继承其内容颜色，不提供额外布局 Scope。
     */
    @Composable
    fun Surface(
        modifier: Modifier,
        shape: Shape?,
        colors: NomiSurfaceColors,
        border: NomiBorder,
        content: @Composable () -> Unit,
    )

    /**
     * 使用原生非点击卡片，只应用显式覆盖；实现须遵守声明的卡片颜色与描边能力。
     *
     * @param modifier 应用于非点击卡片根节点的修饰符。
     * @param shape 卡片轮廓；null 保留当前设计系统的原生形状。
     * @param colors 容器色与内容色的逐字段覆盖；Unspecified 保留原生值，Transparent 是有效覆盖。
     * @param border Default 保留原生描边，None 移除描边，Stroke 指定描边。
     * @param contentPadding 卡片内容内边距；null 保留原生间距。
     * @param content 在卡片 ColumnScope 中组合的内容，继承卡片内容样式。
     */
    @Composable
    fun Card(
        modifier: Modifier,
        shape: Shape?,
        colors: NomiSurfaceColors,
        border: NomiBorder,
        contentPadding: PaddingValues?,
        content: @Composable ColumnScope.() -> Unit,
    )
}

/**
 * 表面颜色的稀疏覆盖；Transparent 是有效覆盖，Unspecified 才表示未指定。
 *
 * @property containerColor 表面背景颜色，未指定时由原生组件决定。
 * @property contentColor 传递给表面内容的颜色，未指定时由原生组件决定。
 */
@Immutable
data class NomiSurfaceColors(
    val containerColor: Color = Color.Unspecified,
    val contentColor: Color = Color.Unspecified,
)

/**
 * 显式中立表面预设；普通 NomiSurface 默认不调用此工厂。
 */
object NomiSurfaceDefaults {
    /**
     * 创建调用方主动选用的语义颜色预设，默认读取当前中立主题的表面与表面内容颜色。
     *
     * @param containerColor 容器颜色；显式传入 Unspecified 时仍保留未指定状态。
     * @param contentColor 内容颜色；显式传入 Unspecified 时仍保留未指定状态。
     * @return 保存这两个颜色的新表面配置。
     */
    @Composable
    fun colors(
        containerColor: Color = NomiTheme.colorScheme.surface,
        contentColor: Color = NomiTheme.colorScheme.onSurface,
    ): NomiSurfaceColors =
        NomiSurfaceColors(containerColor = containerColor, contentColor = contentColor)
}

/**
 * 原生表面入口；未指定形状、颜色和描边均保留当前库默认。
 *
 * @param modifier 应用于表面根节点的修饰符。
 * @param shape 表面轮廓；null 使用当前设计系统的默认形状。
 * @param colors 容器色与内容色的逐字段覆盖；Unspecified 保留原生值，Transparent 是有效覆盖。
 * @param border Default 保留原生描边，None 移除描边，Stroke 指定描边。
 * @param content 表面内组合的内容，继承其内容颜色，不提供额外布局 Scope。
 */
@Composable
fun NomiSurface(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    colors: NomiSurfaceColors = NomiSurfaceColors(),
    border: NomiBorder = NomiBorder.Default,
    content: @Composable () -> Unit,
) = requireNomiDesignSystem().components.surfaces.Surface(
    modifier = modifier,
    shape = shape,
    colors = colors,
    border = border,
    content = content,
)
