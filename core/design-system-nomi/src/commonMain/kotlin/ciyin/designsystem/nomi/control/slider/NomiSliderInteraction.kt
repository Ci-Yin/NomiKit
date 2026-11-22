/*
 * Copyright 2022 Smart Tool Factory.
 * Migrated from SmartToolFactory/Compose-Colorful-Sliders (Apache-2.0).
 * Source: https://github.com/SmartToolFactory/Compose-Colorful-Sliders
 * 修改：改为 Nomi 令牌驱动的外观，并接入设计系统渲染契约。
 */

package ciyin.designsystem.nomi.control.slider

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.gestures.DragScope
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.GestureCancellationException
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * 在区间之间做线性插值。
 *
 * @param start 起点。
 * @param end 终点。
 * @param amount 插值比例，0 返回起点、1 返回终点。
 * @return 插值结果。
 */
internal fun sliderLerp(start: Float, end: Float, amount: Float): Float =
    (1 - amount) * start + amount * end

/**
 * 把一个区间内的位置映射到另一个区间。
 *
 * @param start1 来源区间起点。
 * @param end1 来源区间终点。
 * @param position 来源区间中的位置。
 * @param start2 目标区间起点。
 * @param end2 目标区间终点。
 * @return 目标区间中对应的位置。
 */
internal fun sliderScale(
    start1: Float,
    end1: Float,
    position: Float,
    start2: Float,
    end2: Float,
): Float = sliderLerp(start2, end2, sliderCalculateFraction(start1, end1, position))

/**
 * 计算位置在区间中的比例，并夹紧到 0f..1f。
 *
 * 区间起止相等时按 0 处理，避免除零。
 *
 * @param start 区间起点。
 * @param end 区间终点。
 * @param position 待计算位置。
 * @return 0f..1f 之间的比例。
 */
internal fun sliderCalculateFraction(start: Float, end: Float, position: Float): Float =
    (if (end - start == 0f) 0f else (position - start) / (end - start)).coerceIn(0f, 1f)

/**
 * 把离散刻度数量展开为比例列表，包含两端点。
 *
 * @param steps 离散刻度数量；小于等于 0 时返回空列表，表示连续滑块。
 * @return 按顺序排列的刻度比例。
 */
internal fun sliderStepsToTickFractions(steps: Int): List<Float> =
    if (steps <= 0) emptyList() else List(steps + 2) { index -> index.toFloat() / (steps + 1) }

/**
 * 松手吸附到刻度的动画时长。
 */
private val SliderToTickAnimation = TweenSpec<Float>(durationMillis = 100)

/**
 * 滑块的拖拽状态：把像素增量转交给调用方，并串行化并发拖拽。
 *
 * @property onDelta 像素增量回调，由调用方换算成数值。
 */
internal class NomiSliderDragState(private val onDelta: (Float) -> Unit) : DraggableState {
    /**
     * 当前是否处于拖拽中；用于判断松手时是否需要播放吸附动画。
     */
    var isDragging by mutableStateOf(false)
        private set

    /**
     * 把拖拽块内的增量转交给调用方。
     */
    private val dragScope: DragScope = object : DragScope {
        override fun dragBy(pixels: Float) {
            onDelta(pixels)
        }
    }

    /**
     * 串行化拖拽与吸附动画，避免两者同时修改偏移。
     */
    private val mutationMutex = MutatorMutex()

    /**
     * 在给定优先级下执行拖拽块。
     *
     * @param dragPriority 拖拽优先级。
     * @param block 只允许调用 dragBy 的拖拽块。
     */
    override suspend fun drag(
        dragPriority: MutatePriority,
        block: suspend DragScope.() -> Unit,
    ) {
        coroutineScope {
            isDragging = true
            try {
                mutationMutex.mutateWith(dragScope, dragPriority, block)
            } finally {
                isDragging = false
            }
        }
    }

    /**
     * 把系统分发的原始增量直接转交调用方。
     *
     * @param delta 像素增量。
     */
    override fun dispatchRawDelta(delta: Float) {
        onDelta(delta)
    }
}

/**
 * 点按滑块的修饰符：按下时记录拇指偏移，松开后由调用方触发吸附。
 *
 * 禁用时不安装任何手势，保持节点可组合但不响应输入。
 *
 * @receiver 滑块交互容器的修饰符。
 * @param draggableState 拖拽状态，用于把点按转换成一次数值更新。
 * @param interactionSource 交互源，参与手势 key。
 * @param maxPx 滑块可用宽度，用于反向布局换算。
 * @param isRtl 是否从右到左布局。
 * @param rawOffset 拇指当前偏移。
 * @param gestureEndAction 手势结束后执行的吸附动作。
 * @param pressOffset 按下位置与拇指当前位置的差值。
 * @param enabled 是否启用。
 * @return 安装了点按手势的修饰符。
 */
internal fun Modifier.nomiSliderTapModifier(
    draggableState: DraggableState,
    interactionSource: MutableInteractionSource,
    maxPx: Float,
    isRtl: Boolean,
    rawOffset: State<Float>,
    gestureEndAction: State<(Float) -> Unit>,
    pressOffset: MutableState<Float>,
    enabled: Boolean,
): Modifier = composed {
    if (!enabled) return@composed this
    val scope = rememberCoroutineScope()
    pointerInput(draggableState, interactionSource, maxPx, isRtl) {
        detectTapGestures(
            onPress = { position ->
                val target = if (isRtl) maxPx - position.x else position.x
                pressOffset.value = target - rawOffset.value
                try {
                    awaitRelease()
                } catch (_: GestureCancellationException) {
                    pressOffset.value = 0f
                }
            },
            onTap = {
                scope.launch {
                    draggableState.drag(MutatePriority.UserInput) {
                        dragBy(0f)
                    }
                    gestureEndAction.value.invoke(0f)
                }
            },
        )
    }
}

/**
 * 把拇指动画移动到目标像素位置，并把每一帧的增量交回拖拽状态。
 *
 * @param draggableState 拖拽状态。
 * @param current 当前像素位置。
 * @param target 目标像素位置。
 * @param velocity 手势速度，用于选择动画初速度。
 */
internal suspend fun animateSliderToTarget(
    draggableState: DraggableState,
    current: Float,
    target: Float,
    velocity: Float,
) {
    draggableState.drag {
        var latest = current
        Animatable(initialValue = current).animateTo(target, SliderToTickAnimation, velocity) {
            dragBy(value - latest)
            latest = value
        }
    }
}

/**
 * 找到距离当前位置最近的刻度像素位置。
 *
 * @param current 当前像素位置。
 * @param tickFractions 刻度比例列表；为空时返回当前位置。
 * @param minPx 轨道起点像素。
 * @param maxPx 轨道终点像素。
 * @return 最近的刻度像素位置，没有刻度时返回当前值。
 */
internal fun snapSliderValueToTick(
    current: Float,
    tickFractions: List<Float>,
    minPx: Float,
    maxPx: Float,
): Float = tickFractions
    .minByOrNull { fraction -> abs(sliderLerp(minPx, maxPx, fraction) - current) }
    ?.let { fraction -> sliderLerp(minPx, maxPx, fraction) }
    ?: current

/**
 * 外部值变化时把拇指偏移同步到对应像素位置。
 *
 * 只在偏差超过千分之一区间且当前偏移仍落在轨道内时更新，避免拖动过程中被外部值反复拉回。
 *
 * @param scaleToOffset 数值到像素的换算。
 * @param valueRange 取值区间。
 * @param trackRange 轨道像素区间。
 * @param valueState 拇指偏移状态。
 * @param value 当前数值。
 */
@Composable
internal fun syncSliderOffset(
    scaleToOffset: (Float) -> Float,
    valueRange: ClosedFloatingPointRange<Float>,
    trackRange: ClosedFloatingPointRange<Float>,
    valueState: MutableState<Float>,
    value: Float,
) {
    SideEffect {
        val tolerance = (valueRange.endInclusive - valueRange.start) / 1000f
        val newOffset = scaleToOffset(value)
        if (abs(newOffset - valueState.value) > tolerance && valueState.value in trackRange) {
            valueState.value = newOffset
        }
    }
}
