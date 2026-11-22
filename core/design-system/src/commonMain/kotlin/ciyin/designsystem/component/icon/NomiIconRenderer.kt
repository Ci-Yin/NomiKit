package ciyin.designsystem.component.icon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 图标原生渲染契约。
 *
 * 实现负责把矢量交给当前设计系统的原生图标组件绘制，不替调用方决定尺寸、布局或无障碍名称；
 * 尺寸默认取矢量固有尺寸，需要其它尺寸时由调用方通过 [Modifier.size] 约束。
 */
@Stable
interface NomiIconRenderer {
    /**
     * 绘制一个原生图标。
     *
     * @param imageVector 要绘制的图标矢量；固有尺寸与路径沿用矢量自身定义。
     * @param contentDescription 无障碍名称；null 表示装饰性图标，不朗读。
     * @param modifier 应用于图标根节点的修饰符；尺寸、内边距与对齐都由调用方决定。
     * @param tint 图标着色；Unspecified 表示保留原生内容色，Transparent 是有效覆盖。
     * @param nativeSpec 图标专属参数；null，或未携带本系统定义的类型时，全部沿用原生默认。
     */
    @Composable
    fun Icon(
        imageVector: ImageVector,
        contentDescription: String?,
        modifier: Modifier,
        tint: Color,
        nativeSpec: NomiIconSpec?,
    )
}
