package ciyin.designsystem.component.feedback

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily
import ciyin.designsystem.runtime.requireNomiDesignSystem

/**
 * 统一进度指示器入口；轨道、进度段与不确定态动画沿用当前设计系统的原生默认。
 *
 * progress 为 null 时显示不确定进度，只表达"正在进行"；非 null 时由实现夹紧到 0f..1f。
 * 入口不提供颜色、尺寸或自定义轨道；这些能力在两侧原生实现中形态差异较大，留待后续轮次。
 * 未提供设计系统时由 [requireNomiDesignSystem] 抛出带指引的异常。
 *
 * @param progress 确定进度值，范围 0f..1f；null 表示不确定进度。
 * @param modifier 应用于进度指示器根节点的修饰符。
 * @param type 进度形态，默认线性。
 * @param nativeSpec 进度专属参数；null 表示不覆盖任何原生属性。
 */
@Composable
fun NomiProgressIndicator(
    progress: Float?,
    modifier: Modifier = Modifier,
    type: NomiProgressIndicatorType = NomiProgressIndicatorType.Linear,
    nativeSpec: NomiProgressIndicatorSpec? = null,
) = requireNomiDesignSystem().components.feedback.ProgressIndicator(
    progress = progress,
    modifier = modifier,
    type = type,
    nativeSpec = nativeSpec,
)

/**
 * 进度指示器的形态；未指定时使用线性。
 */
enum class NomiProgressIndicatorType {
    /**
     * 水平线性进度。
     */
    Linear,

    /**
     * 环形进度。
     */
    Circular,
}

/**
 * 进度指示器专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；与提示浮层的标记互不兼容，
 * 因此提示浮层的 spec 无法传给 [NomiProgressIndicator]。
 */
sealed interface NomiProgressIndicatorSpecFamily : NomiSpecFamily

/**
 * 进度指示器专属参数。
 *
 * 只接受 [NomiProgressIndicator]；当前没有实现，属于预留插孔。
 */
typealias NomiProgressIndicatorSpec = NomiNativeSpec<NomiProgressIndicatorSpecFamily>
