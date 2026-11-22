/*
 * Copyright 2022 Smart Tool Factory.
 * Migrated from SmartToolFactory/Compose-Colorful-Sliders (Apache-2.0).
 * Source: https://github.com/SmartToolFactory/Compose-Colorful-Sliders
 * 修改：改为 Nomi 令牌驱动的外观，并接入设计系统渲染契约。
 */

package ciyin.designsystem.nomi.control.slider

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ciyin.designsystem.theme.NomiTheme

/**
 * 滑块一次绘制所需的尺寸与画刷。
 *
 * 取值对齐应用滑块的既有外观：胶囊轨道、带描边的圆形拇指、透明刻度；禁用态由调用方按状态换一份
 * 样式，不在绘制阶段判断。
 *
 * @property trackHeight 轨道高度。
 * @property thumbSize 拇指边长。
 * @property thumbBorderWidth 拇指描边宽度。
 * @property activeTrackBrush 已选轨道画刷。
 * @property inactiveTrackBrush 未选轨道画刷。
 * @property thumbBrush 拇指填充画刷。
 * @property thumbBorderBrush 拇指描边画刷。
 * @property activeTickBrush 已选段刻度画刷。
 * @property inactiveTickBrush 未选段刻度画刷。
 */
@Immutable
internal data class NomiSliderStyle(
    val trackHeight: Dp,
    val thumbSize: Dp,
    val thumbBorderWidth: Dp,
    val activeTrackBrush: Brush,
    val inactiveTrackBrush: Brush,
    val thumbBrush: Brush,
    val thumbBorderBrush: Brush,
    val activeTickBrush: Brush,
    val inactiveTickBrush: Brush,
)

/**
 * 按当前启用状态解析滑块样式。
 *
 * 启用态读取 Nomi 主题的主色与表面色；禁用态把轨道、拇指与拇指描边的透明度统一降到 38%，
 * 刻度始终透明。只读取 Nomi 令牌，不读取宿主原生主题。
 *
 * @param enabled 当前是否启用。
 * @return 一份新的不可变样式；调用方不持有跨组合状态。
 */
@Composable
internal fun rememberNomiSliderStyle(enabled: Boolean): NomiSliderStyle {
    val scheme = NomiTheme.colorScheme
    val active = if (enabled) scheme.primary else scheme.primary.copy(alpha = NomiSliderDisabledAlpha)
    val inactive = if (enabled) {
        scheme.surfaceHigher
    } else {
        scheme.surfaceHigher.copy(alpha = NomiSliderDisabledAlpha)
    }
    val thumb = if (enabled) {
        scheme.onPrimary
    } else {
        scheme.onPrimary.copy(alpha = NomiSliderDisabledAlpha)
    }
    return NomiSliderStyle(
        trackHeight = NomiSliderTrackHeight,
        thumbSize = NomiSliderThumbSize,
        thumbBorderWidth = NomiTheme.sizes.strokes.medium,
        activeTrackBrush = SolidColor(active),
        inactiveTrackBrush = SolidColor(inactive),
        thumbBrush = SolidColor(thumb),
        thumbBorderBrush = SolidColor(active),
        activeTickBrush = SolidColor(NomiSliderTransparent),
        inactiveTickBrush = SolidColor(NomiSliderTransparent),
    )
}

/**
 * 轨道高度，稳定视觉语义：与应用滑块的胶囊轨道保持一致。
 */
private val NomiSliderTrackHeight = 16.dp

/**
 * 拇指边长，稳定视觉语义：与应用滑块的圆形拇指保持一致。
 */
private val NomiSliderThumbSize = 15.dp

/**
 * 禁用态透明度，与应用滑块的禁用外观保持一致。
 */
private const val NomiSliderDisabledAlpha = 0.38f

/**
 * 刻度使用的透明色；保持显式常量，避免刻度被主题着色。
 */
private val NomiSliderTransparent = Color.Transparent
