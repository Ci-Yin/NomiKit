package ciyin.designsystem.material3.icon

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ciyin.designsystem.component.icon.NomiIconRenderer
import ciyin.designsystem.component.icon.NomiIconSpec
import androidx.compose.material3.Icon as NativeIcon

/**
 * 原生 Material 3 图标适配。
 *
 * 未指定着色时沿用原生内容色，不引入中立默认色；尺寸由调用方修饰符决定。
 */
internal object Material3IconRenderer : NomiIconRenderer {
    /**
     * 绘制原生图标。
     *
     * @param imageVector 要绘制的图标矢量。
     * @param contentDescription 无障碍名称；null 表示装饰性图标。
     * @param modifier 应用于图标根节点的修饰符。
     * @param tint 图标着色；Unspecified 时回落到原生内容色，Transparent 是有效覆盖。
     * @param nativeSpec 图标专属参数；当前没有实现，原样接受并忽略。
     */
    @Composable
    override fun Icon(
        imageVector: ImageVector,
        contentDescription: String?,
        modifier: Modifier,
        tint: Color,
        nativeSpec: NomiIconSpec?,
    ) {
        NativeIcon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = modifier,
            tint = if (tint == Color.Unspecified) LocalContentColor.current else tint,
        )
    }
}
