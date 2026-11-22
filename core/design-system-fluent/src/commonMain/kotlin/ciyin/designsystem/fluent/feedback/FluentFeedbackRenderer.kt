package ciyin.designsystem.fluent.feedback

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.component.feedback.NomiFeedbackRenderer
import ciyin.designsystem.component.feedback.NomiProgressIndicatorSpec
import ciyin.designsystem.component.feedback.NomiProgressIndicatorType
import ciyin.designsystem.component.feedback.NomiTooltipSpec
import io.github.composefluent.ExperimentalFluentApi
import io.github.composefluent.component.ProgressBar
import io.github.composefluent.component.ProgressRing
import io.github.composefluent.component.TooltipBox

/**
 * 原生 Fluent 反馈适配：提示浮层使用原生 TooltipBox，进度使用原生 ProgressBar 与 ProgressRing。
 *
 * 悬停触发、出现时长、位置与进度外观都由原生默认值决定；调用方不感知浮层状态。
 */
internal object FluentFeedbackRenderer : NomiFeedbackRenderer {
    /**
     * 为内容包一层原生提示浮层。
     *
     * @param tooltip 提示内容槽，放进原生浮层容器。
     * @param modifier 应用于浮层锚点容器的修饰符。
     * @param nativeSpec 提示专属参数；当前没有实现，原样接受并忽略。
     * @param content 触发提示的内容，由原生浮层包裹。
     */
    @OptIn(ExperimentalFluentApi::class, ExperimentalFoundationApi::class)
    @Composable
    override fun Tooltip(
        tooltip: @Composable () -> Unit,
        modifier: Modifier,
        nativeSpec: NomiTooltipSpec?,
        content: @Composable () -> Unit,
    ) {
        TooltipBox(
            tooltip = tooltip,
            modifier = modifier,
            content = content,
        )
    }

    /**
     * 使用原生进度指示器；确定态传入进度值，不确定态使用原生循环动画。
     *
     * @param progress 确定进度值，范围 0f..1f；null 表示不确定进度。
     * @param modifier 应用于原生进度指示器的修饰符。
     * @param type 进度形态；线性使用 ProgressBar，环形使用 ProgressRing。
     * @param nativeSpec 进度专属参数；当前没有实现，原样接受并忽略。
     */
    @Composable
    override fun ProgressIndicator(
        progress: Float?,
        modifier: Modifier,
        type: NomiProgressIndicatorType,
        nativeSpec: NomiProgressIndicatorSpec?,
    ) {
        val coerced = progress?.coerceIn(0f, 1f)
        when (type) {
            NomiProgressIndicatorType.Linear -> if (coerced == null) {
                ProgressBar(modifier = modifier)
            } else {
                ProgressBar(progress = coerced, modifier = modifier)
            }

            NomiProgressIndicatorType.Circular -> if (coerced == null) {
                ProgressRing(modifier = modifier)
            } else {
                ProgressRing(progress = coerced, modifier = modifier)
            }
        }
    }
}
