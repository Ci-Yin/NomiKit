package ciyin.material.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 默认尺寸令牌。
 */
private object DefaultSizeTokens {
    /** 图标尺寸等级。 */
    val icon: SizeScale = SizeScale(
        tiny = 14.dp,
        extraSmall = 16.dp,
        small = 18.dp,
        medium = 20.dp,
        large = 24.dp,
        extraLarge = 32.dp,
        huge = 40.dp,
        massive = 48.dp,
        colossal = 56.dp,
    )

    /** 头像尺寸等级。 */
    val avatar: SizeScale = SizeScale(
        tiny = 18.dp,
        extraSmall = 24.dp,
        small = 32.dp,
        medium = 40.dp,
        large = 48.dp,
        extraLarge = 56.dp,
        huge = 64.dp,
        massive = 72.dp,
        colossal = 96.dp,
    )

    /** 线条宽度等级。 */
    val strokes: Strokes = Strokes(
        thin = 1.dp,
        medium = 2.dp,
        thick = 3.dp,
    )

    /** 常用组件高度。 */
    val componentHeights: ComponentHeights = ComponentHeights(
        appBar = 56.dp,
        bottomBar = 56.dp,
        tab = 48.dp,
        chip = 32.dp,
        badge = 16.dp,
    )

    /** 常用布局宽度约束。 */
    val layoutConstraints: LayoutConstraints = LayoutConstraints(
        cardMinWidth = 280.dp,
        dialogMinWidth = 280.dp,
        dialogMaxWidth = 560.dp,
        sheetMinWidth = 360.dp,
    )
}

/**
 * 定义应用全局使用的尺寸（Dp）。
 *
 * 包含图标、头像、线条、组件高度、布局约束等各种 UI 元素的统一尺寸标准。
 * 通过 `AppSizes` 可以方便地在整个应用中复用标准尺寸，保持视觉一致性。
 *
 * @property icon 图标尺寸等级
 * @property avatar 头像尺寸等级
 * @property strokes 线条宽度（边框、分隔线等）
 * @property componentHeights 组件高度
 * @property layoutConstraints 布局约束（最小/最大宽度等）
 */
@Immutable
@ConsistentCopyVisibility
data class AppSizes internal constructor(
    val icon: SizeScale = DefaultSizeTokens.icon,
    val avatar: SizeScale = DefaultSizeTokens.avatar,
    val strokes: Strokes = DefaultSizeTokens.strokes,
    val componentHeights: ComponentHeights = DefaultSizeTokens.componentHeights,
    val layoutConstraints: LayoutConstraints = DefaultSizeTokens.layoutConstraints,
)

/**
 * 尺寸等级体系。
 *
 * 用于统一管理应用中各种 UI 元素的尺寸等级，采用语义化命名而非直接暴露 dp 数值。
 * 该体系共 9 个等级，由小到大依次为：
 * `tiny → extraSmall → small → medium → large → extraLarge → huge → massive → colossal`
 *
 * 适用于图标、头像等需要多级尺寸的 UI 元素。
 *
 * @property tiny 极小尺寸
 * @property extraSmall 超小尺寸
 * @property small 小尺寸
 * @property medium 中等尺寸
 * @property large 大尺寸
 * @property extraLarge 超大尺寸
 * @property huge 巨大尺寸
 * @property massive 庞大尺寸
 * @property colossal 宏伟尺寸
 */
@Immutable
@ConsistentCopyVisibility
data class SizeScale internal constructor(
    val tiny: Dp,
    val extraSmall: Dp,
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp,
    val huge: Dp,
    val massive: Dp,
    val colossal: Dp,
)

/**
 * 线条宽度（边框、分隔线等）。
 *
 * 用于统一管理应用中各种线条的宽度标准，包括边框和分隔线的粗细等级。
 *
 * @property thin 细线条宽度，默认：`1.dp`
 * @property medium 中等线条宽度，默认：`2.dp`
 * @property thick 粗线条宽度，默认：`3.dp`
 */
@Immutable
@ConsistentCopyVisibility
data class Strokes internal constructor(
    val thin: Dp,
    val medium: Dp,
    val thick: Dp,
)

/**
 * 组件高度。
 *
 * 用于统一管理应用中各种组件的标准高度值。
 *
 * @property appBar 应用栏高度，默认：`56.dp`
 * @property bottomBar 底部导航栏高度，默认：`56.dp`
 * @property tab 标签栏高度，默认：`48.dp`
 * @property chip 芯片高度，默认：`32.dp`
 * @property badge 徽章尺寸，默认：`16.dp`
 */
@Immutable
@ConsistentCopyVisibility
data class ComponentHeights internal constructor(
    val appBar: Dp,
    val bottomBar: Dp,
    val tab: Dp,
    val chip: Dp,
    val badge: Dp,
)

/**
 * 布局约束（最小/最大宽度等）。
 *
 * 用于统一管理应用中各种布局容器的最小和最大宽度约束。
 *
 * @property cardMinWidth 卡片最小宽度，默认：`280.dp`
 * @property dialogMinWidth 对话框最小宽度，默认：`280.dp`
 * @property dialogMaxWidth 对话框最大宽度，默认：`560.dp`
 * @property sheetMinWidth 底部表单最小宽度，默认：`360.dp`
 */
@Immutable
@ConsistentCopyVisibility
data class LayoutConstraints internal constructor(
    val cardMinWidth: Dp,
    val dialogMinWidth: Dp,
    val dialogMaxWidth: Dp,
    val sheetMinWidth: Dp,
)

/** CompositionLocal：用于在树中提供/覆盖 AppSizes。 */
internal val LocalSizes = staticCompositionLocalOf { AppSizes() }
