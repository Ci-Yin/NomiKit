package ciyin.designsystem.nomi.feedback

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.component.feedback.NomiFeedbackRenderer
import ciyin.designsystem.component.feedback.NomiProgressIndicatorSpec
import ciyin.designsystem.component.feedback.NomiProgressIndicatorType
import ciyin.designsystem.component.feedback.NomiTooltipSpec
import ciyin.designsystem.theme.NomiTheme

/**
 * Nomi 自身的反馈实现：提示浮层与进度指示器都以原生组件为交互载体，配色读取 Nomi 主题令牌。
 *
 * 进度指示器的绘制位于 [NomiProgressIndicatorContent]，确定进度发布进度语义，
 * 不确定进度只发布进度条角色。
 */
internal object DefaultNomiFeedbackRenderer : NomiFeedbackRenderer {
    /**
     * 为内容包一层提示浮层。
     *
     * @param tooltip 提示内容槽，放进原生普通提示容器。
     * @param modifier 应用于浮层锚点容器的修饰符。
     * @param nativeSpec 提示专属参数；Nomi 外观完全由 [NomiTheme] 令牌决定，本实现忽略它。
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
        val scheme = NomiTheme.colorScheme
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                TooltipAnchorPosition.Above,
            ),
            tooltip = {
                PlainTooltip(
                    shape = NomiTheme.shapes.small,
                    containerColor = scheme.surfaceHigher,
                    contentColor = scheme.textPrimary,
                ) {
                    tooltip()
                }
            },
            state = rememberTooltipState(),
            modifier = modifier,
            content = content,
        )
    }

    /**
     * 绘制 Nomi 进度指示器。
     *
     * @param progress 确定进度值，范围 0f..1f；null 表示不确定进度。
     * @param modifier 应用于进度指示器根节点的修饰符。
     * @param type 进度形态；线性与环形都由 Nomi 自身绘制。
     * @param nativeSpec 进度专属参数；Nomi 外观完全由 [NomiTheme] 令牌决定，本实现忽略它。
     */
    @Composable
    override fun ProgressIndicator(
        progress: Float?,
        modifier: Modifier,
        type: NomiProgressIndicatorType,
        nativeSpec: NomiProgressIndicatorSpec?,
    ) {
        NomiProgressIndicatorContent(
            progress = progress,
            modifier = modifier,
            type = type,
        )
    }
}
