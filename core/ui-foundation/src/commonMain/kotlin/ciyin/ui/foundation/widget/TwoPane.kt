package ciyin.ui.foundation.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ciyin.ui.foundation.extension.thenIf
import ciyin.ui.foundation.unit.toDp
import org.jetbrains.compose.ui.tooling.preview.Preview


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TwoPane(
    first: @Composable () -> Unit,
    second: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onSliderChange: (Float) -> Unit = {},
    onSliderEnd: (Float) -> Unit = {},
    mode: TwoPaneMode = TwoPaneMode.Default,
    orientation: Orientation = Orientation.Horizontal,
    firstFraction: Float = 0.5f,
    minFraction: Float = 0.2f,
    maxFraction: Float = 0.8f,
    gapWidth: Dp = 20.dp,
) {

    var intSize = IntSize.Zero
    var firstFractionState by remember { mutableFloatStateOf(firstFraction) }
    val secondAnim by animateFloatAsState(firstFractionState, tween(500))

    val slider = @Composable {
        Box(
            Modifier
                .fillMaxHeight()
                .width(gapWidth)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(8.dp, 60.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .combinedClickable(
                        onClick = {}, onDoubleClick = { firstFractionState = firstFraction }
                    )
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                firstFractionState += change.position.x / intSize.width.toFloat()
                                onSliderChange(firstFractionState)
                            },
                            onDragEnd = {
                                onSliderEnd(firstFractionState)
                            }
                        )
                    },
            )
        }
    }
    VerticalTwoPane(
        modifier = modifier.onSizeChanged {
            intSize = it
        },
        firstFraction = firstFractionState,
        minFraction = minFraction,
        maxFraction = maxFraction,
        gapWidth = gapWidth,
        first = first,
        mode = mode,
        slider = slider,
        second = second,
    )

    AnimatedVisibility(
        visible = mode == TwoPaneMode.SecondOnly,
        enter = fadeIn(),
        exit = fadeOut(),
        content = { second() }
    )

}

@Composable
private fun VerticalTwoPane(
    modifier: Modifier,
    firstFraction: Float,
    minFraction: Float,
    maxFraction: Float,
    gapWidth: Dp,
    mode: TwoPaneMode,
    first: @Composable () -> Unit,
    slider: @Composable () -> Unit,
    second: @Composable () -> Unit
) {
    var widthState by remember { mutableIntStateOf(100) }

    Row(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                widthState = it.width
            }
    ) {
        val widthDp = widthState.toDp()
        val firstWidth = (widthDp - gapWidth).coerceIn(1.dp, null) * firstFraction.coerceIn(
            minFraction, maxFraction
        )

        if (mode == TwoPaneMode.Default || mode == TwoPaneMode.FirstOnly) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .thenIf(mode == TwoPaneMode.FirstOnly, {
                        fillMaxWidth()
                    }, {
                        width(firstWidth)
                    }),
                content = { first() }
            )
        }
        if (mode == TwoPaneMode.Default && gapWidth != 0.dp) {
            slider()
        }
        if (mode == TwoPaneMode.Default) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(widthDp - firstWidth),
                content = { second() }
            )
        }
    }
}

@Composable
private fun Comp(
    orientation: Orientation,
    content: @Composable () -> Unit
) {
    when (orientation) {
        Orientation.Horizontal -> {
            Row {
                content()
            }
        }

        Orientation.Vertical -> {
            Column {
                content()
            }
        }
    }
}

/**
 *
 * kotlin文件作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2024/9/4 上午2:27
 * @version: 1.0
 */

@Preview
@Composable
private fun TwoPanePreview() = MaterialPreview {
    TwoPane(
        gapWidth = 15.dp,
        first = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Yellow)
            )
        },
        second = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Blue)
            )
        }
    )
}

enum class TwoPaneMode {
    Default,
    FirstOnly,
    SecondOnly,
}