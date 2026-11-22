/*
 * Copyright 2022 Smart Tool Factory.
 * Migrated from SmartToolFactory/Compose-Colorful-Sliders (Apache-2.0).
 * Source: https://github.com/SmartToolFactory/Compose-Colorful-Sliders
 * 修改：改为 Nomi 令牌驱动的外观，并接入设计系统渲染契约。
 */

package ciyin.designsystem.nomi.control.slider

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * Nomi 滑块内容：测量、轨道、拇指与手势。
 *
 * 外观对齐应用滑块的既有形态：16dp 胶囊轨道、15dp 描边拇指、轨道两端对齐拇指边缘、刻度不显示；
 * 值与区间由调用方持有，本组件只把拖拽与点按换算成新值，点按时吸附到最近刻度（拖拽松手沿用原实现，
 * 只回调结束事件）。绘制全部读取 Nomi 令牌，不读取宿主原生主题。
 *
 * @param value 当前值。
 * @param onValueChange 值变化回调。
 * @param modifier 应用于滑块根节点的修饰符。
 * @param enabled 是否接受用户操作。
 * @param valueRange 取值区间。
 * @param steps 离散刻度数量；负值按 0 处理。
 * @param onValueChangeFinished 用户结束交互后的回调；null 表示不关心。
 */
@Composable
internal fun NomiSliderContent(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChangeFinished: (() -> Unit)?,
) {
    val effectiveSteps = steps.coerceAtLeast(0)
    val style = rememberNomiSliderStyle(enabled)
    val coercedValue = value.coerceIn(valueRange.start, valueRange.endInclusive)
    val tickFractions = remember(effectiveSteps) { sliderStepsToTickFractions(effectiveSteps) }
    val onValueChangeState = rememberUpdatedState(onValueChange)

    SubcomposeLayout(
        modifier = modifier
            .requiredSizeIn(minWidth = style.thumbSize, minHeight = style.thumbSize)
            .progressSemantics(
                value = coercedValue,
                valueRange = valueRange,
                steps = effectiveSteps,
            )
            .semantics {
                if (!enabled) disabled()
                setProgress { target ->
                    if (enabled) {
                        onValueChangeState.value(
                            target.coerceIn(valueRange.start, valueRange.endInclusive),
                        )
                    }
                    true
                }
            },
    ) { constraints ->
        val thumbPlaceable = subcompose(NomiSliderSlot.Thumb) {
            NomiSliderThumb(style)
        }.map { it.measure(constraints) }.first()
        val thumbHalfWidth = thumbPlaceable.width / 2f
        val thumbHeight = thumbPlaceable.height.toFloat()
        val widthPx = constraints.maxWidth.toFloat()
        val strokeRadius = style.trackHeight.toPx() / 2f
        val trackStart = thumbHalfWidth.coerceAtLeast(strokeRadius)
        val trackEnd = (widthPx - trackStart).coerceAtLeast(trackStart)
        val sliderPlaceable = subcompose(NomiSliderSlot.Slider) {
            NomiSliderBody(
                value = coercedValue,
                valueRange = valueRange,
                enabled = enabled,
                style = style,
                tickFractions = tickFractions,
                trackStart = trackStart,
                trackEnd = trackEnd,
                widthPx = widthPx,
                thumbHalfWidth = thumbHalfWidth,
                thumbHeight = thumbHeight,
                onValueChange = { onValueChangeState.value(it) },
                onValueChangeFinished = onValueChangeFinished,
            )
        }.map { it.measure(constraints) }.first()
        layout(sliderPlaceable.width, sliderPlaceable.height) {
            sliderPlaceable.placeRelative(0, 0)
        }
    }
}

/**
 * 滑块的可交互主体：轨道、拇指、拖拽与点按。
 *
 * @param value 当前值，已夹紧到取值区间。
 * @param valueRange 取值区间。
 * @param enabled 是否接受用户操作。
 * @param style 当前启用状态对应的尺寸与画刷。
 * @param tickFractions 刻度比例；空列表表示连续滑块。
 * @param trackStart 轨道测量起点像素。
 * @param trackEnd 轨道测量终点像素。
 * @param widthPx 滑块宽度像素，用于点按换算。
 * @param thumbHalfWidth 拇指半宽像素。
 * @param thumbHeight 拇指高度像素，用于约束滑块高度。
 * @param onValueChange 值变化回调。
 * @param onValueChangeFinished 用户结束交互后的回调。
 */
@Composable
private fun NomiSliderBody(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    style: NomiSliderStyle,
    tickFractions: List<Float>,
    trackStart: Float,
    trackEnd: Float,
    widthPx: Float,
    thumbHalfWidth: Float,
    thumbHeight: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)?,
) {
    val density = LocalDensity.current
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val onValueChangeState = rememberUpdatedState(onValueChange)
    val onValueChangeFinishedState = rememberUpdatedState(onValueChangeFinished)
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }
    val fraction = sliderCalculateFraction(valueRange.start, valueRange.endInclusive, value)

    fun offsetOf(userValue: Float): Float =
        sliderScale(valueRange.start, valueRange.endInclusive, userValue, trackStart, trackEnd)

    fun valueOf(offset: Float): Float =
        sliderScale(trackStart, trackEnd, offset, valueRange.start, valueRange.endInclusive)

    val rawOffset = remember { mutableStateOf(offsetOf(value)) }
    val pressOffset = remember { mutableStateOf(0f) }
    val dragState = remember(trackStart, trackEnd, valueRange) {
        NomiSliderDragState { delta ->
            rawOffset.value += delta + pressOffset.value
            pressOffset.value = 0f
            val offsetInTrack = rawOffset.value.coerceIn(trackStart, trackEnd)
            onValueChangeState.value(valueOf(offsetInTrack))
        }
    }
    syncSliderOffset(::offsetOf, valueRange, trackStart..trackEnd, rawOffset, value)

    val gestureEnd: (Float) -> Unit = { velocity ->
        val current = rawOffset.value
        val target = snapSliderValueToTick(current, tickFractions, trackStart, trackEnd)
        if (current != target) {
            scope.launch {
                animateSliderToTarget(dragState, current, target, velocity)
                onValueChangeFinishedState.value?.invoke()
            }
        } else if (!dragState.isDragging) {
            onValueChangeFinishedState.value?.invoke()
        }
    }
    val gestureEndState = rememberUpdatedState(gestureEnd)

    val tapModifier = Modifier.nomiSliderTapModifier(
        draggableState = dragState,
        interactionSource = interactionSource,
        maxPx = widthPx,
        isRtl = isRtl,
        rawOffset = rawOffset,
        gestureEndAction = gestureEndState,
        pressOffset = pressOffset,
        enabled = enabled,
    )
    val dragModifier = Modifier.draggable(
        orientation = Orientation.Horizontal,
        reverseDirection = isRtl,
        enabled = enabled,
        interactionSource = interactionSource,
        onDragStopped = {
            if (enabled) {
                onValueChangeFinishedState.value?.invoke()
            }
        },
        startDragImmediately = dragState.isDragging,
        state = dragState,
    )
    val thumbCenter = if (isRtl) {
        trackEnd - (trackEnd - trackStart) * fraction
    } else {
        trackStart + (trackEnd - trackStart) * fraction
    }
    val thumbHeightDp = with(density) {
        (2f * thumbHalfWidth.coerceAtLeast(thumbHeight)).toDp()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = maxOf(style.trackHeight, thumbHeightDp, NomiSliderMinHeight))
            .then(tapModifier)
            .then(dragModifier),
        contentAlignment = Alignment.CenterStart,
    ) {
        NomiSliderTrack(
            modifier = Modifier.fillMaxSize(),
            fraction = fraction,
            tickFractions = tickFractions,
            style = style,
            trackStart = trackStart,
            thumbHalfWidth = thumbHalfWidth,
            coerceThumbInTrack = NomiSliderCoerceThumbInTrack,
            drawInactiveTrack = NomiSliderDrawInactiveTrack,
            borderStroke = null,
        )
        Box(
            modifier = Modifier.offset {
                IntOffset((thumbCenter - thumbHalfWidth).toInt(), 0)
            },
        ) {
            NomiSliderThumb(style)
        }
    }
}

/**
 * 滑块 `SubcomposeLayout` 的槽位划分：先测量拇指，再编排轨道与手势主体。
 */
private enum class NomiSliderSlot {
    /**
     * 用于测量尺寸的拇指槽。
     */
    Thumb,

    /**
     * 轨道与手势主体槽。
     */
    Slider,
}

/**
 * 轨道最矮高度，稳定视觉语义：与原实现的最小轨道量级一致。
 */
private val NomiSliderMinHeight = 4.dp

/**
 * 轨道两端是否对齐拇指边缘；沿用应用滑块的设置。
 */
private const val NomiSliderCoerceThumbInTrack = true

/**
 * 是否绘制未选轨道；沿用应用滑块的设置。
 */
private const val NomiSliderDrawInactiveTrack = true
