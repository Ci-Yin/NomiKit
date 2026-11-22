package ciyin.designsystem.component.surface

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import ciyin.designsystem.component.NomiBorder
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily
import ciyin.designsystem.runtime.requireNomiDesignSystem
import ciyin.designsystem.theme.NomiTheme

/**
 * 原生表面入口；未指定形状、颜色和描边均保留当前库默认。
 *
 * @param modifier 应用于表面根节点的修饰符。
 * @param shape 表面轮廓；null 使用当前设计系统的默认形状。
 * @param colors 容器色与内容色的逐字段覆盖；Unspecified 保留原生值，Transparent 是有效覆盖。
 * @param border Default 保留原生描边，None 移除描边，Stroke 指定描边。
 * @param nativeSpec 表面专属参数；null 表示不覆盖任何原生属性。
 * @param content 表面内组合的内容，继承其内容颜色，不提供额外布局 Scope。
 */
@Composable
fun NomiSurface(
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    colors: NomiSurfaceColors = NomiSurfaceColors(),
    border: NomiBorder = NomiBorder.Default,
    nativeSpec: NomiSurfaceSpec? = null,
    content: @Composable () -> Unit,
) = requireNomiDesignSystem().components.surfaces.Surface(
    modifier = modifier,
    shape = shape,
    colors = colors,
    border = border,
    nativeSpec = nativeSpec,
    content = content,
)

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
 * 表面专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；与卡片族的标记互不兼容。
 */
sealed interface NomiSurfaceSpecFamily : NomiSpecFamily

/**
 * 表面专属参数。
 *
 * 只承载中立参数没有入口的原生表面能力（色调阴影、投影层级等），具体实现由 adapter 模块定义。
 */
typealias NomiSurfaceSpec = NomiNativeSpec<NomiSurfaceSpecFamily>
