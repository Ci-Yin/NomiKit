package ciyin.designsystem.component.feedback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

/**
 * 反馈与提示组件的原生渲染契约。
 *
 * 实现负责使用当前设计系统的原生提示浮层与进度指示器，并保留其触发方式、出现时长、位置与视觉；
 * 核心不暴露浮层状态、位置提供者、富文本提示或进度颜色与尺寸。
 */
@Stable
interface NomiFeedbackRenderer {
    /**
     * 为内容包一层原生提示浮层。
     *
     * @param tooltip 提示内容槽；只在原生浮层显示期间组合。
     * @param modifier 应用于浮层锚点容器的修饰符。
     * @param nativeSpec 提示专属参数；null，或未携带本系统定义的类型时，全部沿用原生默认。
     * @param content 触发提示的内容，由原生浮层包裹。
     */
    @Composable
    fun Tooltip(
        tooltip: @Composable () -> Unit,
        modifier: Modifier,
        nativeSpec: NomiTooltipSpec?,
        content: @Composable () -> Unit,
    )

}
