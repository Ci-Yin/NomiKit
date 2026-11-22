package ciyin.designsystem.material3.feedback

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.component.feedback.NomiFeedbackRenderer
import ciyin.designsystem.component.feedback.NomiTooltipSpec

/**
 * 原生 Material 3 反馈适配：提示浮层使用原生 TooltipBox，进度使用原生线形与环形进度。
 *
 * 状态、位置与配色都由原生默认值决定；调用方不感知浮层状态，也不传进度颜色或尺寸。
 */
internal object Material3FeedbackRenderer : NomiFeedbackRenderer {
    /**
     * 为内容包一层原生提示浮层。
     *
     * @param tooltip 提示内容槽，放进原生普通提示容器。
     * @param modifier 应用于浮层锚点容器的修饰符。
     * @param nativeSpec 提示专属参数；当前没有实现，原样接受并忽略。
     * @param content 触发提示的内容，由原生浮层包裹。
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Tooltip(
        tooltip: @Composable () -> Unit,
        modifier: Modifier,
        nativeSpec: NomiTooltipSpec?,
        content: @Composable () -> Unit,
    ) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                TooltipAnchorPosition.Above,
            ),
            tooltip = {
                PlainTooltip {
                    tooltip()
                }
            },
            state = rememberTooltipState(),
            modifier = modifier,
            content = content,
        )
    }

}
