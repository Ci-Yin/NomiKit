package com.ciyin.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ========== Spacing 默认取值，基于 4dp 倍数体系（Material Design 规范） ==========
private val Spacing2 = 2.dp      // 2dp 超小间距，用于极紧凑元素间隔
private val Spacing4 = 4.dp      // 4dp 极小间距，用于紧凑元素间隔
private val Spacing8 = 8.dp      // 8dp 小间距，用于一般控件内边距（常用）
private val Spacing12 = 12.dp    // 12dp 中等间距，用于列表项、卡片内容间隔
private val Spacing16 = 16.dp    // 16dp 中大间距，常用的基础间距（常用）
private val Spacing20 = 20.dp    // 20dp 大间距，用于区块之间的分隔
private val Spacing24 = 24.dp    // 24dp 特大间距，用于页面主要分区（常用）
private val Spacing32 = 32.dp    // 32dp 超大间距，用于页面级别的留白
private val Spacing48 = 48.dp    // 48dp 特大间距，用于全屏段落或横向间隔

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
    val tiny: Dp = Spacing2,
    val extraSmall: Dp = Spacing4,
    val small: Dp = Spacing8,
    val medium: Dp = Spacing12,
    val large: Dp = Spacing16,
    val extraLarge: Dp = Spacing20,
    val huge: Dp = Spacing24,
    val massive: Dp = Spacing32,
    val colossal: Dp = Spacing48,
)

/** CompositionLocal：用于在树中提供/覆盖 Spacings。 */
internal val LocalSpacings = staticCompositionLocalOf { Spacings() }

