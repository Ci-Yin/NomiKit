package com.ciyin.app.ui.theme

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

// 定义不同圆角大小
private val Shape2 = RoundedCornerShape(2.dp)
private val Shape4 = RoundedCornerShape(4.dp)
private val Shape8 = RoundedCornerShape(8.dp)
private val Shape12 = RoundedCornerShape(12.dp)
private val Shape16 = RoundedCornerShape(16.dp)
private val Shape20 = RoundedCornerShape(20.dp)
private val Shape24 = RoundedCornerShape(24.dp)
private val Shape32 = RoundedCornerShape(32.dp)
private val Shape48 = RoundedCornerShape(48.dp)

/**
 * 定义应用全局统一使用的圆角形状（Shapes Scale）。
 *
 * 本设计采用**语义化尺寸等级**而非数值命名，用于描述“视觉体量感”，
 * 而不是直接暴露 dp 数值，从而：
 *
 * - 保持设计语言的稳定性
 * - 避免业务代码依赖具体数值
 * - 便于整体设计风格的调整与演进
 *
 * ---
 *
 * ## 圆角等级语义说明
 *
 * 该圆角体系共 9 个等级，由小到大依次为：
 *
 * `tiny → extraSmall → small → medium → large → extraLarge → huge → massive → colossal`
 *
 * ---
 *
 * ## 使用规范（非常重要）
 *
 * **不同等级用于不同层级的 UI 结构，请勿随意混用：**
 *
 * ### 🟢 常规组件级（按钮 / 输入框 / Chip / ListItem）
 * - `tiny`
 * - `extraSmall`
 * - `small`
 * - `medium`
 * - `large`
 *
 * ### 🟡 容器 / 区块级（卡片 / Sheet / Panel）
 * - `extraLarge`
 * - `huge`
 *
 * ### 🔴 页面 / 覆盖层级（Dialog / Modal / 全屏浮层）
 * - `massive`
 * - `colossal`
 *
 * `massive` 与 `colossal` **不应**用于普通控件，
 * 它们仅用于具有“页面结构意义”的 UI 容器。
 *
 * ---
 *
 * 如需调整整体圆角风格，应统一修改 Shape 定义，
 * 而不是在业务代码中使用硬编码的 `RoundedCornerShape(dp)`。
 *
 * @property tiny 极小圆角，默认：`2dp` 圆角
 * @property extraSmall 小圆角，默认：`4dp` 圆角
 * @property small 中等圆角，默认：`8dp` 圆角
 * @property medium 较大圆角，默认：`12dp` 圆角
 * @property large 大圆角，默认：`16dp` 圆角
 * @property extraLarge 更大圆角，默认：`20dp` 圆角
 * @property huge 很大圆角，默认：`24dp` 圆角
 * @property massive 超大圆角，默认：`32dp` 圆角
 * @property colossal 极大圆角，默认：`48dp` 圆角
 */
@Immutable
@ConsistentCopyVisibility
data class AppShapes internal constructor(
    val tiny: RoundedCornerShape = Shape2,
    val extraSmall: RoundedCornerShape = Shape4,
    val small: RoundedCornerShape = Shape8,
    val medium: RoundedCornerShape = Shape12,
    val large: RoundedCornerShape = Shape16,
    val extraLarge: RoundedCornerShape = Shape20,
    val huge: RoundedCornerShape = Shape24,
    val massive: RoundedCornerShape = Shape32,
    val colossal: RoundedCornerShape = Shape48,
)

/**
 * 启用指定角的圆角效果，其他角将被设置为直角(0dp)。
 *
 * @param topStart 是否启用左上角圆角
 * @param topEnd 是否启用右上角圆角
 * @param bottomStart 是否启用左下角圆角
 * @param bottomEnd 是否启用右下角圆角
 * @return 返回一个新的RoundedCornerShape，仅指定的角保持原有圆角，其他角为直角
 */
fun RoundedCornerShape.enable(
    topStart: Boolean = false,
    topEnd: Boolean = false,
    bottomStart: Boolean = false,
    bottomEnd: Boolean = false
) = copy(
    topStart = if (topStart) this.topStart else CornerSize(0.dp),
    topEnd = if (topEnd) this.topEnd else CornerSize(0.dp),
    bottomStart = if (bottomStart) this.bottomStart else CornerSize(0.dp),
    bottomEnd = if (bottomEnd) this.bottomEnd else CornerSize(0.dp)
)

/**
 * 禁用指定角的圆角效果，让这些角变为直角(0dp)，其他角保持原有圆角。
 *
 * @param topStart 是否禁用左上角圆角
 * @param topEnd 是否禁用右上角圆角
 * @param bottomStart 是否禁用左下角圆角
 * @param bottomEnd 是否禁用右下角圆角
 * @return 返回一个新的RoundedCornerShape，指定的角变为直角，其他角保持原有圆角
 */
fun RoundedCornerShape.disable(
    topStart: Boolean = false,
    topEnd: Boolean = false,
    bottomStart: Boolean = false,
    bottomEnd: Boolean = false
) = copy(
    topStart = if (topStart) CornerSize(0.dp) else this.topStart,
    topEnd = if (topEnd) CornerSize(0.dp) else this.topEnd,
    bottomStart = if (bottomStart) CornerSize(0.dp) else this.bottomStart,
    bottomEnd = if (bottomEnd) CornerSize(0.dp) else this.bottomEnd
)

/**
 * 启用顶部两个角的圆角效果。
 *
 * @return 返回一个新的RoundedCornerShape，仅顶部两个角保持原有圆角，底部两个角为直角
 */
fun RoundedCornerShape.top() = enable(
    topStart = true,
    topEnd = true
)

/**
 * 启用底部两个角的圆角效果。
 *
 * @return 返回一个新的RoundedCornerShape，仅底部两个角保持原有圆角，顶部两个角为直角
 */
fun RoundedCornerShape.bottom() = enable(
    bottomStart = true,
    bottomEnd = true
)

/**
 * 启用起始侧(左侧)两个角的圆角效果。
 *
 * @return 返回一个新的RoundedCornerShape，仅起始侧两个角保持原有圆角，另一侧两个角为直角
 */
fun RoundedCornerShape.start() = enable(
    topStart = true,
    bottomStart = true
)

/**
 * 启用结束侧(右侧)两个角的圆角效果。
 *
 * @return 返回一个新的RoundedCornerShape，仅结束侧两个角保持原有圆角，另一侧两个角为直角
 */
fun RoundedCornerShape.end() = enable(
    topEnd = true,
    bottomEnd = true
)


/** CompositionLocal：用于在树中提供/覆盖 AppShapes。 */
internal val LocalShapes = staticCompositionLocalOf { AppShapes() }

