package ciyin.ui.foundation.widget.progress

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Displays a full circular progress bar, where the progress animation starts from the top
 * <p>
 * @see [ProgressData]
 * @see [ArcProgressBar]
 * @param progress A list of progress to be displayed on the arc, see [ProgressData]
 * @param radius The radius of the arc, defining the size of the composable
 * @param stroke The thickness of the bars
 * @param insideCap The aspect of caps not in the edges of the arc
 */
@Composable
fun CircleProgressBar(
    modifier: Modifier = Modifier,
    progress: List<ProgressData>,
    color: Color = Color.Gray,
    radius: Dp = 50.dp,
    stroke: Dp = 2.dp,
    insideCap: StrokeCap = StrokeCap.Round
) {
    ArcProgressBar(
        progress = progress,
        modifier = modifier,
        color = color,
        radius = radius,
        stroke = stroke,
        baseAngle = 270f,
        endAngle = 360f,
        insideCap = insideCap
    )
}

/**
 * 显示动画弧形进度条，其中包含由 [ProgressData] 列表给出的不同进度
 * <p>
 * @see [ProgressData]
 * @param progress 要在弧线上显示的进度列表，见 [ProgressData]
 * @param radius 圆弧的半径，用于定义可组合项的大小
 * @param stroke 钢筋的粗细
 * @param baseAngle 弧线开始绘制的角度，0f 是正确的，增加该值将逆时针旋转
 * @param endAngle 圆弧停止的角度，根据 [baseAngle] 计算得出
 * @param insideCap 帽头不在圆弧边缘的方面
 */
@Composable
fun ArcProgressBar(
    modifier: Modifier = Modifier,
    progress: List<ProgressData>,
    color: Color = Color.Gray,
    radius: Dp = 50.dp,
    stroke: Dp = 2.dp,
    baseAngle: Float = 150f,
    endAngle: Float = 240f,
    insideCap: StrokeCap = StrokeCap.Round
) {
    val sorted = progress.toMutableList().sortedByDescending { it.progress }

    val calculatedProgresses = List(sorted.size) {
        sorted[it].progress * endAngle
    }

    val animatedBase = remember {
        Animatable(0f)
    }

    val animatedProgresses = sorted.map {
        remember {
            Animatable(0f)
        }
    }

    LaunchedEffect(key1 = true) {
        animatedBase.animateTo(endAngle)
    }

    LaunchedEffect(key1 = sorted) {
        animatedProgresses.forEachIndexed { index, it ->
            it.animateTo(calculatedProgresses[index])
        }
    }

    Canvas(
        modifier = Modifier
            .size(radius * 2f)
            .padding(stroke / 2)
            .then(modifier)
    ) {

        // Drawing base
        drawArc(
            color,
            baseAngle,
            animatedBase.value,
            useCenter = false,
            style = Stroke(width = stroke.toPx(), cap = insideCap)
        )

        animatedProgresses.forEachIndexed { index, it ->
            when (index) {
                0 -> {
                    drawArc(
                        color = sorted[index].color,
                        startAngle = baseAngle,
                        sweepAngle = it.value,
                        useCenter = false,
                        style = Stroke(width = stroke.toPx(), cap = insideCap)
                    )
                }

                else -> {
                    drawArc(
                        color = sorted[index].color,
                        startAngle = baseAngle,
                        sweepAngle = it.value / 2,
                        useCenter = false,
                        style = Stroke(width = stroke.toPx(), cap = insideCap)
                    )
                    drawArc(
                        color = sorted[index].color,
                        startAngle = baseAngle + it.value / 2,
                        sweepAngle = it.value / 2,
                        useCenter = false,
                        style = Stroke(width = stroke.toPx(), cap = insideCap)
                    )
                }
            }
        }
    }
}
