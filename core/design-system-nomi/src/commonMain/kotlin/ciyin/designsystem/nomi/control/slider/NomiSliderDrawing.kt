/*
 * Copyright 2022 Smart Tool Factory.
 * Migrated from SmartToolFactory/Compose-Colorful-Sliders (Apache-2.0).
 * Source: https://github.com/SmartToolFactory/Compose-Colorful-Sliders
 * 修改：改为 Nomi 令牌驱动的外观，并接入设计系统渲染契约。
 */

package ciyin.designsystem.nomi.control.slider

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/**
 * 绘制默认拇指：圆形填充加一圈描边，与应用滑块的拇指一致。
 *
 * @param style 当前启用状态对应的尺寸与画刷。
 */
@Composable
internal fun NomiSliderThumb(style: NomiSliderStyle) {
    Box(
        modifier = Modifier
            .size(style.thumbSize)
            .background(style.thumbBrush, CircleShape)
            .border(style.thumbBorderWidth, style.thumbBorderBrush, CircleShape),
    )
}

/**
 * 绘制轨道、刻度与可选描边。
 *
 * 绘制顺序与原实现一致：未选轨道、已选轨道、描边、刻度；RTL 布局时整条轨道镜像，
 * 已选段始终从布局起点延伸到当前值。
 *
 * @param modifier 应用于画布的修饰符。
 * @param fraction 当前值在取值区间中的比例。
 * @param tickFractions 刻度比例；空列表表示连续滑块。
 * @param style 当前启用状态对应的尺寸与画刷。
 * @param trackStart 轨道测量起点像素。
 * @param thumbHalfWidth 拇指半宽像素，用于刻度大小与 coerce 绘制起点。
 * @param coerceThumbInTrack 为 true 时轨道绘制覆盖到拇指边缘之外。
 * @param drawInactiveTrack 为 true 时绘制未选轨道；否则已选轨道画到终点。
 * @param borderStroke 轨道描边；null 表示不描边。
 */
@Composable
internal fun NomiSliderTrack(
    modifier: Modifier,
    fraction: Float,
    tickFractions: List<Float>,
    style: NomiSliderStyle,
    trackStart: Float,
    thumbHalfWidth: Float,
    coerceThumbInTrack: Boolean,
    drawInactiveTrack: Boolean,
    borderStroke: BorderStroke?,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val density = LocalDensity.current
    val trackPx = with(density) { style.trackHeight.toPx() }
    val strokeRadius = trackPx / 2f
    val borderWidth = borderStroke?.let { with(density) { it.width.toPx() } } ?: 0f
    val borderBrush = borderStroke?.brush
    val drawStart = if (coerceThumbInTrack) trackStart - thumbHalfWidth + strokeRadius else trackStart

    Canvas(modifier = modifier) {
        val left = Offset(drawStart, center.y)
        val right = Offset((size.width - drawStart).coerceAtLeast(drawStart), center.y)
        val start = if (isRtl) right else left
        val end = if (isRtl) left else right
        val valuePoint = Offset(start.x + (end.x - start.x) * fraction, center.y)

        if (drawInactiveTrack) {
            drawLine(
                brush = style.inactiveTrackBrush,
                start = start,
                end = end,
                strokeWidth = trackPx,
                cap = StrokeCap.Round,
            )
        }
        drawLine(
            brush = style.activeTrackBrush,
            start = start,
            end = if (drawInactiveTrack) valuePoint else end,
            strokeWidth = trackPx,
            cap = StrokeCap.Round,
        )
        borderBrush?.let { brush ->
            drawRoundRect(
                brush = brush,
                topLeft = Offset(start.x - strokeRadius, (size.height - trackPx) / 2f),
                size = Size(width = end.x - start.x + trackPx, height = trackPx),
                cornerRadius = CornerRadius(strokeRadius, strokeRadius),
                style = Stroke(width = borderWidth),
            )
        }
        if (drawInactiveTrack && tickFractions.isNotEmpty()) {
            tickFractions.groupBy { it > fraction }.forEach { (inactive, group) ->
                drawPoints(
                    points = group.map { Offset(lerp(start, end, it).x, center.y) },
                    pointMode = PointMode.Points,
                    brush = if (inactive) style.inactiveTickBrush else style.activeTickBrush,
                    strokeWidth = strokeRadius.coerceAtMost(thumbHalfWidth / 2f),
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
