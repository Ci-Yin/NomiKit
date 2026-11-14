package ciyin.material.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import ciyin.designsystem.theme.NomiShapes as DesignSystemShapes
import ciyin.designsystem.theme.enable as enableDesignSystemCorners
import ciyin.designsystem.theme.disable as disableDesignSystemCorners
import ciyin.designsystem.theme.top as designSystemTop
import ciyin.designsystem.theme.bottom as designSystemBottom
import ciyin.designsystem.theme.start as designSystemStart
import ciyin.designsystem.theme.end as designSystemEnd

/**
 * 旧九级圆角类型的源码兼容别名。
 *
 * 通过中立核心 [ciyin.designsystem.theme.defaultNomiShapes] 工厂与生成的成员 copy 定制；圆角数据由中立核心维护。
 */
typealias AppShapes = DesignSystemShapes

/**
 * 仅保留指定角的原有圆角，其余角设为直角。
 *
 * 起始侧和结束侧随布局方向解释，并不固定为物理左侧和右侧；不修改接收对象。
 *
 * @receiver 提供各角原始 [RoundedCornerShape] 圆角尺寸的形状。
 * @param topStart 是否保留顶部起始角；默认 false，将该角置为 0dp。
 * @param topEnd 是否保留顶部结束角；默认 false，将该角置为 0dp。
 * @param bottomStart 是否保留底部起始角；默认 false，将该角置为 0dp。
 * @param bottomEnd 是否保留底部结束角；默认 false，将该角置为 0dp。
 * @return 仅选中角保持原尺寸的新形状；全部省略时四角均为直角。
 */
fun RoundedCornerShape.enable(
    topStart: Boolean = false,
    topEnd: Boolean = false,
    bottomStart: Boolean = false,
    bottomEnd: Boolean = false,
): RoundedCornerShape = enableDesignSystemCorners(
    topStart = topStart,
    topEnd = topEnd,
    bottomStart = bottomStart,
    bottomEnd = bottomEnd,
)

/**
 * 将指定角变为直角，其他角保持原有圆角。
 *
 * 起始侧和结束侧随布局方向解释，并不固定为物理左侧和右侧；不修改接收对象。
 *
 * @receiver 提供各角原始圆角尺寸的形状。
 * @param topStart 是否将顶部起始角置为 0dp；默认 false，保留原尺寸。
 * @param topEnd 是否将顶部结束角置为 0dp；默认 false，保留原尺寸。
 * @param bottomStart 是否将底部起始角置为 0dp；默认 false，保留原尺寸。
 * @param bottomEnd 是否将底部结束角置为 0dp；默认 false，保留原尺寸。
 * @return 选中角为直角的新形状；全部省略时各角尺寸与原形状相同。
 */
fun RoundedCornerShape.disable(
    topStart: Boolean = false,
    topEnd: Boolean = false,
    bottomStart: Boolean = false,
    bottomEnd: Boolean = false,
): RoundedCornerShape = disableDesignSystemCorners(
    topStart = topStart,
    topEnd = topEnd,
    bottomStart = bottomStart,
    bottomEnd = bottomEnd,
)

/**
 * 仅保留顶部两个角的原有圆角。
 *
 * @receiver 提供待保留角尺寸的原形状；调用不会修改该对象。
 * @return 顶部两角保持原尺寸、底部两角为直角的新形状。
 */
fun RoundedCornerShape.top(): RoundedCornerShape = designSystemTop()

/**
 * 仅保留底部两个角的原有圆角。
 *
 * @receiver 提供待保留角尺寸的原形状；调用不会修改该对象。
 * @return 底部两角保持原尺寸、顶部两角为直角的新形状。
 */
fun RoundedCornerShape.bottom(): RoundedCornerShape = designSystemBottom()

/**
 * 仅保留起始侧两个角的原有圆角。
 *
 * 起始侧随布局方向解释，不固定为物理左侧。
 *
 * @receiver 提供待保留角尺寸的原形状；调用不会修改该对象。
 * @return 起始侧两角保持原尺寸、结束侧两角为直角的新形状。
 */
fun RoundedCornerShape.start(): RoundedCornerShape = designSystemStart()

/**
 * 仅保留结束侧两个角的原有圆角。
 *
 * 结束侧随布局方向解释，不固定为物理右侧。
 *
 * @receiver 提供待保留角尺寸的原形状；调用不会修改该对象。
 * @return 结束侧两角保持原尺寸、起始侧两角为直角的新形状。
 */
fun RoundedCornerShape.end(): RoundedCornerShape = designSystemEnd()
