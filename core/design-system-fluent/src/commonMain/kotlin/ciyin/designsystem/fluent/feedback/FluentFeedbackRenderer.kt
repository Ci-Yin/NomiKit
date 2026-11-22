package ciyin.designsystem.fluent.feedback

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.component.feedback.NomiFeedbackRenderer
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

}
