package ciyin.material.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 默认间距令牌。
 */
private object DefaultSpacingTokens {
    /** 2dp 超小间距，用于极紧凑元素间隔。 */
    val spacing2: Dp = 2.dp

    /** 4dp 极小间距，用于紧凑元素间隔。 */
    val spacing4: Dp = 4.dp

    /** 8dp 小间距，用于一般控件内边距。 */
    val spacing8: Dp = 8.dp

    /** 12dp 中等间距，用于列表项、卡片内容间隔。 */
    val spacing12: Dp = 12.dp

    /** 16dp 中大间距，常用的基础间距。 */
    val spacing16: Dp = 16.dp

    /** 20dp 大间距，用于区块之间的分隔。 */
    val spacing20: Dp = 20.dp

    /** 24dp 特大间距，用于页面主要分区。 */
    val spacing24: Dp = 24.dp

    /** 32dp 超大间距，用于页面级别的留白。 */
    val spacing32: Dp = 32.dp

    /** 48dp 极大间距，用于全屏段落或横向间隔。 */
    val spacing48: Dp = 48.dp
}

/**
 * 定义应用全局统一使用的间距体系（Spacing Scale）。
 *
 * 本间距体系采用**语义化等级命名**，而非直接暴露 dp 数值，
 * 用于表达“空间层级与结构关系”，而不是具体尺寸。
 *
 * ---
 *
 * ## 间距等级顺序（由小到大）
 *
 * `tiny → extraSmall → small → medium → large → extraLarge → huge → massive → colossal`
 *
 * ---
 *
 * ## 使用规范（非常重要）
 *
 * ### 🟢 组件级间距（Component Level）
 * - `tiny`：极度紧凑的元素间距（icon / text 微调）
 * - `extraSmall`：紧凑组件内边距
 * - `small`：普通控件内边距（常用）
 * - `medium`：列表项、卡片内容间距
 * - `large`：组件之间的标准间距（常用）
 *
 * ### 🟡 区块级间距（Section Level）
 * - `extraLarge`：模块 / 区块之间的分隔
 * - `huge`：页面主要分区（常用）
 *
 * ### 🔴 页面级间距（Layout Level）
 * - `massive`：页面级留白
 * - `colossal`：全屏布局、顶部/底部安全留白
 *
 * ---
 *
 * ❗ 禁止在业务代码中直接使用 `dp` 常量，
 * 所有布局间距必须来自 `Spacings`，以保证整体视觉一致性。
 * @property tiny 极小间距，默认：`2.dp`，用于紧凑元素间隔
 * @property extraSmall 小间距，默认：`4.dp`，用于一般控件内边距
 * @property small 中等小间距，默认：`8.dp`，用于列表项、卡片内容间隔（常用）
 * @property medium 中等间距，默认：`12.dp`，用于中等内容的间隔
 * @property large 中大间距，默认：`16.dp`，常用的基础间距（常用）
 * @property extraLarge 大间距，默认：`20.dp`，用于区块之间的分隔
 * @property huge 特大间距，默认：`24.dp`，用于页面主要分区（常用）
 * @property massive 超大间距，默认：`32.dp`，用于页面级别的留白
 * @property colossal 极大间距，默认：`48.dp`，用于全屏段落或横向间隔
 */
@Immutable
@ConsistentCopyVisibility
data class Spacings internal constructor(
    val tiny: Dp = DefaultSpacingTokens.spacing2,
    val extraSmall: Dp = DefaultSpacingTokens.spacing4,
    val small: Dp = DefaultSpacingTokens.spacing8,
    val medium: Dp = DefaultSpacingTokens.spacing12,
    val large: Dp = DefaultSpacingTokens.spacing16,
    val extraLarge: Dp = DefaultSpacingTokens.spacing20,
    val huge: Dp = DefaultSpacingTokens.spacing24,
    val massive: Dp = DefaultSpacingTokens.spacing32,
    val colossal: Dp = DefaultSpacingTokens.spacing48,
)

/** CompositionLocal：用于在树中提供/覆盖 Spacings。 */
internal val LocalSpacings = staticCompositionLocalOf { Spacings() }
