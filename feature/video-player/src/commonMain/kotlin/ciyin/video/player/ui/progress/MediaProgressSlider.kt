@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    kotlin.experimental.ExperimentalTypeInference::class,
)

package ciyin.video.player.ui.progress

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ciyin.video.player.ui.internal.PlatformPopupProperties
import ciyin.ui.foundation.effects.onPointerEventMultiplatform
import ciyin.video.player.ui.internal.slightlyWeaken
import ciyin.video.player.ui.internal.weaken
import ciyin.video.player.data.ChunkState
import ciyin.video.player.data.MediaCacheProgressInfo
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.features.chapters
import org.openani.mediamp.metadata.Chapter
import org.openani.mediamp.metadata.MediaProperties
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/** 进度预览弹窗的测试标签。 */
const val TAG_PROGRESS_SLIDER_PREVIEW_POPUP = "ProgressSliderPreviewPopup"

/** 进度滑块的测试标签。 */
const val TAG_PROGRESS_SLIDER = "ProgressSlider"

/**
 * 播放器进度滑块的状态.
 *
 * - 支持从 [currentPositionMillis] 同步当前播放位置, 从 [totalDurationMillis] 同步总时长.
 * - 使用 [onPreview] 和 [onPreviewFinished] 来处理用户拖动进度条的事件.
 *
 * @see MediaProgressSlider
 */
@Stable
class PlayerProgressSliderState(
    currentPositionMillis: () -> Long,
    totalDurationMillis: () -> Long,
    chapters: () -> List<Chapter>,
    /**
     * 当用户正在拖动进度条时触发. 每有一个 change 都会调用.
     */
    private val onPreview: (positionMillis: Long) -> Unit,
    /**
     * 当用户松开进度条时触发. 此时播放器应当要跳转到该位置.
     */
    private val onPreviewFinished: (positionMillis: Long) -> Unit,
) {
    /** 当前播放位置。 */
    val currentPositionMillis: Long by derivedStateOf(currentPositionMillis)

    /** 媒体总时长。 */
    val totalDurationMillis: Long by derivedStateOf(totalDurationMillis)

    /** 媒体章节列表。 */
    val chapters by derivedStateOf(chapters)

    /** 当前拖动预览位置比例。 */
    private var previewPositionRatio: Float by mutableFloatStateOf(Float.NaN)

    /** 用户当前是否正在拖动预览。 */
    val isPreviewing: Boolean by derivedStateOf {
        !previewPositionRatio.isNaN()
    }

    /**
     * Sets the slider to move to the given position.
     * [onPreview] will be triggered.
     */
    fun previewPositionRatio(ratio: Float) {
        previewPositionRatio = ratio
        onPreview((totalDurationMillis * ratio).roundToLong())
    }

    /**
     * The ratio of the current display position to the total duration. Range is `0..1`
     */
    val displayPositionRatio by derivedStateOf {
        val previewPositionRatio = this.previewPositionRatio
        if (!previewPositionRatio.isNaN()) {
            return@derivedStateOf previewPositionRatio
        }

        val total = this.totalDurationMillis
        if (total == 0L) {
            return@derivedStateOf 0f
        }
        this.currentPositionMillis.toFloat() / total
    }

    /** 结束预览并提交当前目标位置。 */
    fun finishPreview() {
        // 防抖
        val ratio = this.previewPositionRatio
        if (ratio.isNaN()) return
        onPreviewFinished((ratio * totalDurationMillis).roundToLong())
        previewPositionRatio = Float.NaN
    }
}

/** 进度滑块组合阶段使用的聚合数据。 */
private class Data(
    /** 当前播放位置。 */
    val currentPosition: Long,
    /** 当前媒体属性。 */
    val mediaProperties: MediaProperties?,
    /** 当前媒体章节。 */
    val chapters: List<Chapter>,
) {
    @Stable
    companion object {
        @Stable
        val EMPTY = Data(0, null, emptyList())
    }
}

/**
 * 便捷方法, 从 [MediampPlayer.currentPositionMillis] 创建  [PlayerProgressSliderState]
 */
@Composable
fun rememberMediaProgressSliderState(
    player: MediampPlayer,
    onPreview: (positionMillis: Long) -> Unit,
    onPreviewFinished: (positionMillis: Long) -> Unit,
): PlayerProgressSliderState { // TODO:  refactor rememberMediaProgressSliderState

    val flow = remember(player) {
        combine(
            player.currentPositionMillis,
            player.mediaProperties,
            player.chapters ?: flowOf(emptyList()),
            ::Data,
        ) // TODO: this should be in domain layer
    }

    val data by flow.collectAsStateWithLifecycle(Data.EMPTY)

    val totalDuration by remember {
        derivedStateOf {
            data.mediaProperties?.durationMillis ?: 0L
        }
    }

    val onPreviewUpdated by rememberUpdatedState(onPreview)
    val onPreviewFinishedUpdated by rememberUpdatedState(onPreviewFinished)
    return remember {
        PlayerProgressSliderState(
            { data.currentPosition },
            { totalDuration },
            { data.chapters },
            onPreviewUpdated,
            onPreviewFinishedUpdated,
        )
    }
}

/** 提供进度滑块的默认颜色。 */
object MediaProgressSliderDefaults {
    /** 构造进度滑块默认颜色。 */
    @Composable
    fun colors(
        trackBackgroundColor: Color = MaterialTheme.colorScheme.surface,
        trackProgressColor: Color = MaterialTheme.colorScheme.primary,
        thumbColor: Color = MaterialTheme.colorScheme.primary,
        cachedProgressColor: Color = MaterialTheme.colorScheme.onSurface.weaken(),
        downloadingColor: Color = Color.Yellow,
        notAvailableColor: Color = MaterialTheme.colorScheme.error.slightlyWeaken(),
        chapterColor: Color = MaterialTheme.colorScheme.onSurface,
        previewTimeBackgroundColor: Color = MaterialTheme.colorScheme.surface,
        previewTimeTextColor: Color = MaterialTheme.colorScheme.onSurface,
    ): MediaProgressSliderColors {
        return MediaProgressSliderColors(
            trackBackgroundColor,
            trackProgressColor,
            thumbColor,
            cachedProgressColor,
            downloadingColor,
            notAvailableColor,
            chapterColor,
            previewTimeBackgroundColor,
            previewTimeTextColor,
        )
    }
}

/** 描述进度滑块各状态使用的颜色。 */
@Immutable
class MediaProgressSliderColors(
    /** 轨道背景色。 */
    val trackBackgroundColor: Color,
    /** 已播放进度色。 */
    val trackProgressColor: Color,
    /** 滑块颜色。 */
    val thumbColor: Color,
    /** 已缓存进度色。 */
    val cachedProgressColor: Color,
    /** 下载中分块颜色。 */
    val downloadingColor: Color,
    /** 不可用分块颜色。 */
    val notAvailableColor: Color,
    /** 章节标记颜色。 */
    val chapterColor: Color,
    /** 预览时间背景色。 */
    val previewTimeBackgroundColor: Color,
    /** 预览时间文字色。 */
    val previewTimeTextColor: Color,
)

/**
 * 视频播放器的进度条, 支持拖动调整播放位置, 支持显示缓冲进度.
 */
@Composable
fun MediaProgressSlider(
    state: PlayerProgressSliderState,
    cacheProgressInfoFlow: () -> MediaCacheProgressInfo?,
    colors: MediaProgressSliderColors = MediaProgressSliderDefaults.colors(),
    enabled: Boolean = true,
    showPreviewTimeTextOnThumb: Boolean = true,
//    drawThumb: @Composable DrawScope.() -> Unit = {
//        drawCircle(
//            MaterialTheme.colorScheme.primary,
//            radius = 12f,
//        )
//    },
    modifier: Modifier = Modifier,
) {
    Box(
        modifier.fillMaxWidth()
            .height(24.dp)
            .testTag(TAG_PROGRESS_SLIDER),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            Modifier.fillMaxWidth().height(6.dp)
                .padding(horizontal = 2.dp) // half thumb width
                .clip(CircleShape),
        ) {
            Canvas(Modifier.matchParentSize()) {
                // draw track
                drawRect(
                    colors.trackBackgroundColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, size.height),
                )
            }

            Canvas(Modifier.matchParentSize()) {
                // draw cached progress
                val snapshotCacheProgress =
                    cacheProgressInfoFlow() ?: return@Canvas // ignore initial state

                var currentX = 0f

                // 连续的缓存区块连着画, 否则会因精度缺失导致不连续
                forEachConsecutiveChunk(snapshotCacheProgress) { state, weight ->
                    val color = when (state) {
                        ChunkState.NONE -> Color.Unspecified
                        ChunkState.DOWNLOADING -> colors.downloadingColor
                        ChunkState.DONE -> colors.cachedProgressColor
                        ChunkState.NOT_AVAILABLE -> colors.notAvailableColor
                    }
                    if (color != Color.Unspecified) {
                        val size = Size(
                            weight * size.width,
                            size.height,
                        )// TODO: draw more cache states (colors)
                        drawRect(
                            color,
                            topLeft = Offset(currentX, 0f),
                            size = size,
                        )
                    }
                    currentX += weight * size.width
                }
            }

            Canvas(Modifier.matchParentSize()) {
                // draw play progress
                val xPlay = size.width * state.displayPositionRatio

                drawRect(
                    colors.trackProgressColor,
                    topLeft = Offset(0f, 0f),
                    size = Size(xPlay, size.height),
                )

                // 下面的是有 gap 的视线, 但是会抖动, 不知道为什么
//                val thumbWidth = 4.dp.toPx()
//                val gapWidthEach = 3.dp.toPx() // thumb width + gap
//                val actualXPlay = (xPlay - (gapWidthEach + thumbWidth / 2)).fastCoerceAtLeast(0f)
//                drawRect(
//                    trackProgressColor,
//                    topLeft = Offset(0f, 0f),
//                    size = Size(actualXPlay, size.height),
//                )
//                val drawBackgroundWidth = xPlay - actualXPlay
//                if (drawBackgroundWidth != 0f) {
//                    // 画上背景, 覆盖掉加载中颜色
//                    drawRect(
//                        trackBackgroundColor,
//                        topLeft = Offset(actualXPlay, 0f),
//                        size = Size(drawBackgroundWidth, size.height),
//                        blendMode = BlendMode.Src, // override
//                    )
//                }
//                drawRect(
//                    trackBackgroundColor,
//                    topLeft = Offset(xPlay, 0f),
//                    size = Size(gapWidthEach + thumbWidth / 2, size.height),
//                    blendMode = BlendMode.Src, // override
//                )
            }

            Canvas(Modifier.matchParentSize()) {
                if (state.totalDurationMillis == 0L) return@Canvas
                state.chapters.forEach {
                    val percent = it.offsetMillis.toFloat().div(state.totalDurationMillis)
                    drawCircle(
                        color = colors.chapterColor,
                        radius = 2.dp.toPx(),
                        center = Offset(size.width * percent, this.center.y),
                        blendMode = BlendMode.Src, // override background
                    )
                }
            }
        }

        var mousePosX by rememberSaveable { mutableStateOf(0f) }
        var thumbWidth by rememberSaveable { mutableIntStateOf(0) }
        var sliderWidth by rememberSaveable { mutableIntStateOf(0) }

        fun renderPreviewTime(previewTimeMillis: Long): String {
            state.chapters.find {
                previewTimeMillis in it.offsetMillis..<it.offsetMillis + it.durationMillis
            }?.let {
                val chapterName = if (it.name.isBlank()) "" else it.name + "\n"
                return chapterName + renderSeconds(
                    previewTimeMillis / 1000,
                    state.totalDurationMillis / 1000,
                ).substringBefore(" ")
            }

            return renderSeconds(
                previewTimeMillis / 1000,
                state.totalDurationMillis / 1000
            ).substringBefore(" ")
        }

        val previewTimeText by remember {
            derivedStateOf {
                val containerWidth = sliderWidth - thumbWidth
                if (containerWidth == 0) { // avoid division by zero during preview or in a extremely small container
                    ""
                } else {
                    val percent = mousePosX.minus(thumbWidth / 2).div(containerWidth)
                        .coerceIn(0f, 1f)
                    val previewTimeMillis = state.totalDurationMillis.times(percent).toLong()

                    renderPreviewTime(previewTimeMillis)
                }
            }
        }
        val previewTimeOnThumb by remember(state) {
            derivedStateOf {
                val previewTimeMillis =
                    state.totalDurationMillis.times(state.displayPositionRatio).toLong()

                renderPreviewTime(previewTimeMillis)
            }
        }
        val hoverInteraction = remember { MutableInteractionSource() }
        val isHovered by hoverInteraction.collectIsHoveredAsState() // works only for desktop
        var isPressed by remember { mutableStateOf(false) }
        val showPreviewTime by remember {
            derivedStateOf {
                isHovered || isPressed
            }
        }
        if (showPreviewTime) {
            ProgressSliderPreviewPopup(
                offsetX = { mousePosX.roundToInt() },
                previewTimeBackgroundColor = colors.previewTimeBackgroundColor,
            ) {
                PreviewTimeText(previewTimeText, colors.previewTimeTextColor)
            }
        }
        // draw thumb
        val interactionSource = remember { MutableInteractionSource() }
        Slider(
            value = state.displayPositionRatio,
            valueRange = 0f..1f,
            onValueChange = { state.previewPositionRatio(it) },
            interactionSource = interactionSource,
            thumb = {
                Canvas(Modifier.width(12.dp).height(24.dp)) {
                    drawCircle(
                        colors.thumbColor,
                        radius = 8.dp.toPx(),
                    )
                }
//                SliderDefaults.Thumb(
//                    interactionSource = interactionSource,
//                    colors = SliderDefaults.colors(
//                        thumbColor = MaterialTheme.colorScheme.primary,
//                    ),
//                    enabled = true,
//                    modifier = Modifier.onSizeChanged {
//                        thumbWidth = it.width
//                    },
//                    thumbSize = DpSize(6.dp, 32.dp)
//                )

                // 仅在 detached slider 上显示
                if (state.isPreviewing && showPreviewTimeTextOnThumb) {
                    ProgressSliderPreviewPopup(
                        offsetX = { thumbWidth / 2 },
                        previewTimeBackgroundColor = colors.previewTimeBackgroundColor,
                    ) {
                        PreviewTimeText(previewTimeOnThumb, colors.previewTimeTextColor)
                    }
                }
            },
            track = {
                SliderDefaults.Track(
                    it,
                    colors = SliderDefaults.colors(
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent,
                        disabledActiveTrackColor = Color.Transparent,
                        disabledInactiveTrackColor = Color.Transparent,
                    ),
                )
            },
            onValueChangeFinished = {
                state.finishPreview()
            },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().height(24.dp)
                .onSizeChanged {
                    sliderWidth = it.width
                }
                .hoverable(interactionSource = hoverInteraction)
                .onPointerEventMultiplatform(PointerEventType.Move) {
                    mousePosX =
                        it.changes.firstOrNull()?.position?.x ?: return@onPointerEventMultiplatform
                },
        )
    }
}

/** 在拖动进度时显示时间与缩略预览弹窗。 */
@Composable
fun ProgressSliderPreviewPopup(
    offsetX: () -> Int,
    previewTimeBackgroundColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val popupPositionProviderState = remember {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val anchor = IntRect(
                    offset = IntOffset(
                        offsetX(),
                        with(density) { -8.dp.toPx().toInt() },
                    ) + anchorBounds.topLeft,
                    size = IntSize.Zero,
                )
                val tooltipArea = IntRect(
                    IntOffset(
                        anchor.left - popupContentSize.width,
                        anchor.top - popupContentSize.height,
                    ),
                    IntSize(
                        popupContentSize.width * 2,
                        popupContentSize.height * 2,
                    ),
                )
                val position =
                    Alignment.TopCenter.align(popupContentSize, tooltipArea.size, layoutDirection)

                return IntOffset(
                    x = (tooltipArea.left + position.x).coerceIn(
                        0,
                        windowSize.width - popupContentSize.width
                    ),
                    y = (tooltipArea.top + position.y).coerceIn(
                        0,
                        windowSize.height - popupContentSize.height
                    ),
                )
            }
        }
    }
    Popup(
        properties = PlatformPopupProperties(usePlatformInsets = false),
        popupPositionProvider = popupPositionProviderState,
    ) {
        Box(
            modifier = modifier
                .testTag(TAG_PROGRESS_SLIDER_PREVIEW_POPUP)
                .clip(shape = CircleShape)
                .background(previewTimeBackgroundColor)
                .animateContentSize(),
        ) {
            Box(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                content()
            }
        }
    }
}

/** 显示进度预览对应的时间文本。 */
@Composable
fun PreviewTimeText(
    text: String,
    previewTimeTextColor: Color,
) {
    ProvideTextStyle(MaterialTheme.typography.labelLarge) {
        Text(
            // 占位置
            text = text,
            Modifier.alpha(0f),
            fontFamily = FontFamily.Monospace,
        )
        Text(
            text = text,
            color = previewTimeTextColor,
            textAlign = TextAlign.Center,
        )
    }
}

/** 按连续缓存状态分组遍历分块。 */
private inline fun forEachConsecutiveChunk(
    chunks: MediaCacheProgressInfo,
    action: (state: ChunkState, weight: Float) -> Unit
) {
    if (chunks.isEmpty()) return

    var currentState: ChunkState = chunks.chunkStates[0]
    var start = 0
    var end = 0

    for (index in 1..chunks.lastIndex) {
        val chunk = chunks.chunkStates[index]
        if (chunk != currentState) {
            action(currentState, chunks.sumWeightOfRange(start, end + 1))
            currentState = chunk
            start = index
        }
        end = index
    }
    // Handle the final chunk
    action(currentState, chunks.sumWeightOfRange(start, end + 1))
}

/** 计算指定分块区间的总权重。 */
private fun MediaCacheProgressInfo.sumWeightOfRange(start: Int, endExclusive: Int): Float {
    var sum: Float = 0.toFloat()
    for (i in start until endExclusive) {
        sum += chunkWeights[i]
    }
    return sum
}

/** 使用 Float 选择器求和，补充标准库缺少的重载。 */
@OverloadResolutionByLambdaReturnType
inline fun <T> Iterable<T>.sumOf(selector: (T) -> Float): Float {
    var sum: Float = 0.toFloat()
    for (element in this) {
        sum += selector(element)
    }
    return sum
}
