package ciyin.designsystem.component.button

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ciyin.designsystem.runtime.requireNomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.component.NomiControlSize

/**
 * 按钮原生渲染契约；可空参数表示调用方未指定，不能提前套用统一外观。
 */
@Stable
interface NomiActionRenderer {
    /**
     * 使用原生按钮处理交互及默认样式，只应用显式覆盖。
     *
     * 实现须与设计系统的能力声明一致，不支持的覆盖不能静默忽略。
     *
     * @param onClick 启用状态下用户激活按钮时调用；业务状态由调用方更新。
     * @param modifier 应用于按钮根节点的修饰符。
     * @param enabled 是否接受用户操作，并据此选择启用或禁用外观。
     * @param variant 按钮语义类型；null 使用当前设计系统的原生默认类型。
     * @param size 显式中立尺寸预设；null 保留原生尺寸。
     * @param colors 按启用/禁用状态逐字段覆盖颜色；Unspecified 保留对应原生值，Transparent 是有效覆盖。
     * @param shape 按钮轮廓；null 保留原生形状。
     * @param contentPadding 内容内边距；null 保留原生间距。
     * @param textStyle 内容文本样式增量；null 保留原生按钮排版。
     * @param content 在按钮 RowScope 中组合的内容，继承按钮内容样式，不负责维护点击状态。
     */
    @Composable
    fun Button(
        onClick: () -> Unit,
        modifier: Modifier,
        enabled: Boolean,
        variant: NomiButtonVariant?,
        size: NomiControlSize?,
        colors: NomiButtonColors,
        shape: Shape?,
        contentPadding: PaddingValues?,
        textStyle: TextStyle?,
        content: @Composable RowScope.() -> Unit,
    )
}

/**
 * 按钮视觉类型。
 */
enum class NomiButtonVariant {
    /**
     * 主要操作按钮。
     */
    Primary,

    /**
     * 默认表面按钮。
     */
    Default,

    /**
     * 低强调透明按钮。
     */
    Subtle,

    /**
     * 文本链接按钮。
     */
    Text,

    /**
     * 危险操作按钮。
     */
    Destructive,
}

/**
 * 按钮四种基础颜色状态的稀疏覆盖；Unspecified 表示保留原生值，Transparent 表示明确透明。
 *
 * @property containerColor 启用状态的容器颜色。
 * @property contentColor 启用状态的内容颜色。
 * @property disabledContainerColor 禁用状态的容器颜色。
 * @property disabledContentColor 禁用状态的内容颜色。
 */
@Immutable
data class NomiButtonColors(
    val containerColor: Color = Color.Unspecified,
    val contentColor: Color = Color.Unspecified,
    val disabledContainerColor: Color = Color.Unspecified,
    val disabledContentColor: Color = Color.Unspecified,
)

/**
 * 按钮尺寸对应的布局和排版度量，由显式中立预设或 Nomi 默认实现使用。
 *
 * @property contentPadding 按钮内容与轮廓之间的内边距。
 * @property textStyle 按钮内容的文本样式。
 * @property minTouchTarget 交互目标的最小边长，以 Dp 表示。
 */
@Immutable
data class NomiButtonMetrics(
    val contentPadding: PaddingValues,
    val textStyle: TextStyle,
    val minTouchTarget: Dp,
)

/**
 * 中立按钮预设，供调用方主动选用或 Nomi 自身默认渲染器解析样式。
 * 公共派发入口不会提前应用这些预设覆盖其他设计系统的原生默认值。
 */
object NomiButtonDefaults {
    /**
     * 根据按钮类型读取当前主题颜色，生成完整的启用与禁用颜色预设。
     *
     * @param variant 选择语义颜色的按钮类型；默认使用主要操作配色。
     * @return 新的按钮颜色配置；文本和低强调类型的容器在启用与禁用时均为透明。
     */
    @Composable
    fun colors(variant: NomiButtonVariant = NomiButtonVariant.Primary): NomiButtonColors {
        val scheme = NomiTheme.colorScheme
        val (container, content) = when (variant) {
            NomiButtonVariant.Primary -> scheme.primary to scheme.onPrimary
            NomiButtonVariant.Default -> scheme.surface to scheme.onSurface
            NomiButtonVariant.Subtle -> Color.Transparent to scheme.textPrimary
            NomiButtonVariant.Text -> Color.Transparent to scheme.primary
            NomiButtonVariant.Destructive -> scheme.error to scheme.onError
        }
        return NomiButtonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = if (variant == NomiButtonVariant.Text || variant == NomiButtonVariant.Subtle) {
                Color.Transparent
            } else {
                scheme.surfaceLow
            },
            disabledContentColor = scheme.textDisabled,
        )
    }

    /**
     * 根据控件尺寸读取当前主题的间距和排版，所有预设均使用 48.dp 最小交互目标。
     *
     * @param size 要解析的中立尺寸预设，默认使用中等尺寸。
     * @return 当前主题对应的新按钮布局与排版配置。
     */
    @Composable
    fun metrics(size: NomiControlSize = NomiControlSize.Medium): NomiButtonMetrics {
        val typography = NomiTheme.typography
        val spacings = NomiTheme.spacings
        return when (size) {
            NomiControlSize.Small -> NomiButtonMetrics(
                contentPadding = PaddingValues(
                    horizontal = spacings.medium,
                    vertical = spacings.extraSmall
                ),
                textStyle = typography.labelMedium,
                minTouchTarget = 48.dp,
            )

            NomiControlSize.Medium -> NomiButtonMetrics(
                contentPadding = PaddingValues(
                    horizontal = spacings.large,
                    vertical = spacings.small
                ),
                textStyle = typography.labelLarge,
                minTouchTarget = 48.dp,
            )

            NomiControlSize.Large -> NomiButtonMetrics(
                contentPadding = PaddingValues(
                    horizontal = spacings.huge,
                    vertical = spacings.medium
                ),
                textStyle = typography.titleSmall,
                minTouchTarget = 48.dp,
            )
        }
    }
}


/**
 * 默认调用对应原生按钮；明确不支持的视觉覆盖在边界拒绝，不重绘或静默忽略。
 *
 * @param onClick 启用状态下用户激活按钮时调用；业务状态由调用方更新。
 * @param modifier 应用于按钮根节点的修饰符。
 * @param enabled 是否接受用户操作，并据此选择启用或禁用外观。
 * @param variant 按钮语义类型；null 使用当前设计系统的原生默认类型。
 * @param size 显式中立尺寸预设；null 保留原生尺寸。
 * @param colors 按启用/禁用状态逐字段覆盖颜色；Unspecified 保留对应原生值，Transparent 是有效覆盖。
 * @param shape 按钮轮廓；null 保留原生形状。
 * @param contentPadding 内容内边距；null 保留原生间距。
 * @param textStyle 内容文本样式增量；null 保留原生按钮排版。
 * @param content 在按钮 RowScope 中组合的内容，继承按钮内容样式，不负责维护点击状态。
 * @throws IllegalArgumentException 当前设计系统不支持传入的 size、shape 或 contentPadding 覆盖时抛出。
 */
@Composable
fun NomiButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: NomiButtonVariant? = null,
    size: NomiControlSize? = null,
    colors: NomiButtonColors = NomiButtonColors(),
    shape: Shape? = null,
    contentPadding: PaddingValues? = null,
    textStyle: TextStyle? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val designSystem = requireNomiDesignSystem()
    val capabilities = designSystem.capabilities
    require(size == null || capabilities.buttonSize) { designSystem.id.value + " NomiButton 不支持 size 覆盖" }
    require(shape == null || capabilities.buttonShape) { designSystem.id.value + " NomiButton 不支持 shape 覆盖" }
    require(contentPadding == null || capabilities.buttonContentPadding) { designSystem.id.value + " NomiButton 不支持 contentPadding 覆盖" }
    designSystem.components.actions.Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        variant = variant,
        size = size,
        colors = colors,
        shape = shape,
        contentPadding = contentPadding,
        textStyle = textStyle,
        content = content,
    )
}
