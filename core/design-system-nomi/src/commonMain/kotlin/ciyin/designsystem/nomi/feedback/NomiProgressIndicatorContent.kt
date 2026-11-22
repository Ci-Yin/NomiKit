package ciyin.designsystem.nomi.feedback

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.feedback.NomiProgressIndicatorType
import ciyin.designsystem.theme.NomiTheme

/**
 * Nomi 进度指示器内容：线性与环形、确定与不确定两种状态。
 *
 * 轨道与配色参照滑块：线性为 16dp 胶囊轨道，进度色 `primary`、轨道色 `surfaceHigher`；环形保持
 * 原尺寸与描边，只统一配色。确定状态发布进度语义，不确定状态只发布进度条角色。
 *
 * @param progress 确定进度值，范围 0f..1f；null 表示不确定进度。
 * @param modifier 应用于根节点的修饰符。
 * @param type 进度形态。
 */
@Composable
internal fun NomiProgressIndicatorContent(
    progress: Float?,
    modifier: Modifier,
    type: NomiProgressIndicatorType,
) {
    val semanticsModifier = if (progress == null) {
        Modifier.progressSemantics()
    } else {
        Modifier.progressSemantics(progress.coerceIn(0f, 1f))
    }
    val contentModifier = modifier.then(semanticsModifier)
    when (type) {
        NomiProgressIndicatorType.Linear -> NomiLinearProgressIndicator(progress, contentModifier)
        NomiProgressIndicatorType.Circular -> NomiCircularProgressIndicator(progress, contentModifier)
    }
}

/**
 * 线性进度：16dp 胶囊轨道 + 进度段，不确定态用一段循环滑动的短段表达。
 *
 * @param progress 确定进度值；null 表示不确定。
 * @param modifier 应用于画布的修饰符。
 */
@Composable
private fun NomiLinearProgressIndicator(progress: Float?, modifier: Modifier) {
    val scheme = NomiTheme.colorScheme
    val trackColor = scheme.surfaceHigher
    val activeColor = scheme.primary
    val strokePx = with(LocalDensity.current) { NomiProgressTrackHeight.toPx() }
    val animatedProgress = if (progress == null) {
        0f
    } else {
        animateFloatAsState(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = NomiProgressAnimationMillis),
            label = "linear-progress",
        ).value
    }
    val sweep = if (progress == null) {
        rememberInfiniteTransition(label = "linear-progress").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = NomiIndeterminateAnimationMillis,
                    easing = LinearEasing,
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "linear-progress-sweep",
        ).value
    } else {
        0f
    }
    Canvas(modifier = modifier.fillMaxWidth().height(NomiProgressTrackHeight)) {
        val startX = strokePx / 2f
        val endX = size.width - strokePx / 2f
        val centerY = center.y
        drawLine(
            color = trackColor,
            start = Offset(startX, centerY),
            end = Offset(endX, centerY),
            strokeWidth = strokePx,
            cap = StrokeCap.Round,
        )
        if (progress == null) {
            val segmentWidth = (endX - startX) * NomiIndeterminateSegmentFraction
            val travel = (endX - startX) + segmentWidth
            val segmentStart = (startX + travel * sweep - segmentWidth).coerceIn(startX, endX)
            val segmentEnd = (segmentStart + segmentWidth).coerceIn(startX, endX)
            drawLine(
                color = activeColor,
                start = Offset(segmentStart, centerY),
                end = Offset(segmentEnd, centerY),
                strokeWidth = strokePx,
                cap = StrokeCap.Round,
            )
        } else if (animatedProgress > 0f) {
            // 0 进度不画零长度线段，避免圆头端点留下一个圆点
            drawLine(
                color = activeColor,
                start = Offset(startX, centerY),
                end = Offset(startX + (endX - startX) * animatedProgress, centerY),
                strokeWidth = strokePx,
                cap = StrokeCap.Round,
            )
        }
    }
}

/**
 * 环形进度：整圈轨道 + 从顶部开始的进度弧，不确定态用循环旋转的短弧表达。
 *
 * @param progress 确定进度值；null 表示不确定。
 * @param modifier 应用于画布的修饰符。
 */
@Composable
private fun NomiCircularProgressIndicator(progress: Float?, modifier: Modifier) {
    val scheme = NomiTheme.colorScheme
    val trackColor = scheme.surfaceHigher
    val activeColor = scheme.primary
    val strokePx = with(LocalDensity.current) { NomiCircularProgressStroke.toPx() }
    val animatedProgress = if (progress == null) {
        0f
    } else {
        animateFloatAsState(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = NomiProgressAnimationMillis),
            label = "circular-progress",
        ).value
    }
    val rotation = if (progress == null) {
        rememberInfiniteTransition(label = "circular-progress").animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = NomiIndeterminateAnimationMillis,
                    easing = LinearEasing,
                ),
                repeatMode = RepeatMode.Restart,
            ),
            label = "circular-progress-rotation",
        ).value
    } else {
        0f
    }
    Canvas(modifier = modifier.size(NomiCircularProgressSize)) {
        drawArc(
            color = trackColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokePx),
        )
        if (progress == null) {
            drawArc(
                color = activeColor,
                startAngle = rotation,
                sweepAngle = NomiIndeterminateArcSweep,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
        } else {
            drawArc(
                color = activeColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
        }
    }
}

/**
 * 线性进度轨道高度，稳定语义值：与滑块轨道同高。
 */
private val NomiProgressTrackHeight = 16.dp

/**
 * 环形进度默认尺寸，稳定语义值：与原生环形进度默认尺寸一致。
 */
private val NomiCircularProgressSize = 32.dp

/**
 * 环形进度描边宽度，稳定语义值：32dp 尺寸下取 3dp。
 */
private val NomiCircularProgressStroke = 3.dp

/**
 * 不确定线性进度中短段占轨道的比例。
 */
private const val NomiIndeterminateSegmentFraction = 0.3f

/**
 * 不确定环形进度的可见弧长。
 */
private const val NomiIndeterminateArcSweep = 90f

/**
 * 确定进度动画时长。
 */
private const val NomiProgressAnimationMillis = 200

/**
 * 不确定态循环动画时长。
 */
private const val NomiIndeterminateAnimationMillis = 1200
