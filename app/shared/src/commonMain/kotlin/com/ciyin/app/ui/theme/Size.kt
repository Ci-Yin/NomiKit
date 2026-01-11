package com.ciyin.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ========== 图标尺寸 ==========
private val Icon14 = 14.dp
private val Icon16 = 16.dp
private val Icon18 = 18.dp
private val Icon20 = 20.dp
private val Icon24 = 24.dp
private val Icon32 = 32.dp
private val Icon40 = 40.dp
private val Icon48 = 48.dp
private val Icon56 = 56.dp
private val Icon64 = 64.dp

// ========== 头像尺寸 ==========
private val Avatar18 = 18.dp
private val Avatar24 = 24.dp
private val Avatar32 = 32.dp
private val Avatar40 = 40.dp
private val Avatar48 = 48.dp
private val Avatar56 = 56.dp
private val Avatar64 = 64.dp
private val Avatar72 = 72.dp
private val Avatar96 = 96.dp

// ========== Strokes 默认值 ==========
private val StrokeThin = 1.dp
private val StrokeMedium = 2.dp
private val StrokeThick = 3.dp

// ========== ComponentHeights 默认值 ==========
private val ComponentHeightAppBar = 56.dp
private val ComponentHeightBottomBar = 56.dp
private val ComponentHeightTab = 48.dp
private val ComponentHeightChip = 32.dp
private val ComponentHeightBadge = 16.dp

// ========== LayoutConstraints 默认值 ==========
private val LayoutConstraintCardMinWidth = 280.dp
private val LayoutConstraintDialogMinWidth = 280.dp
private val LayoutConstraintDialogMaxWidth = 560.dp
private val LayoutConstraintSheetMinWidth = 360.dp

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
    val icon: SizeScale = SizeScale(
        tiny = Icon14,
        extraSmall = Icon16,
        small = Icon18,
        medium = Icon20,
        large = Icon24,
        extraLarge = Icon32,
        huge = Icon40,
        massive = Icon48,
        colossal = Icon56,
    ),

    val avatar: SizeScale = SizeScale(
        tiny = Avatar18,
        extraSmall = Avatar24,
        small = Avatar32,
        medium = Avatar40,
        large = Avatar48,
        extraLarge = Avatar56,
        huge = Avatar64,
        massive = Avatar72,
        colossal = Avatar96,
    ),

    val strokes: Strokes = Strokes(
        thin = StrokeThin,       // 1dp
        medium = StrokeMedium,   // 2dp
        thick = StrokeThick,     // 3dp
    ),

    val componentHeights: ComponentHeights = ComponentHeights(
        appBar = ComponentHeightAppBar,           // 56dp
        bottomBar = ComponentHeightBottomBar,     // 56dp
        tab = ComponentHeightTab,                 // 48dp
        chip = ComponentHeightChip,               // 32dp
        badge = ComponentHeightBadge,             // 16dp
    ),

    val layoutConstraints: LayoutConstraints = LayoutConstraints(
        cardMinWidth = LayoutConstraintCardMinWidth,       // 280dp
        dialogMinWidth = LayoutConstraintDialogMinWidth,   // 280dp
        dialogMaxWidth = LayoutConstraintDialogMaxWidth,   // 560dp
        sheetMinWidth = LayoutConstraintSheetMinWidth,     // 360dp
    ),
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
    val thin: Dp,       // 1dp
    val medium: Dp,     // 2dp
    val thick: Dp,      // 3dp
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
