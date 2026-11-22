package ciyin.designsystem.nomi.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import ciyin.designsystem.component.icon.NomiIconRenderer
import ciyin.designsystem.component.icon.NomiIconSpec
import ciyin.designsystem.nomi.LocalNomiNativeContentColor
import ciyin.designsystem.theme.NomiTheme
import androidx.compose.material3.Icon as NativeIcon

/**
 * Nomi 自身图标实现；以原生图标为绘制载体，着色读取 Nomi 主题令牌。
 */
internal object DefaultNomiIconRenderer : NomiIconRenderer {
    /**
     * 绘制图标。
     *
     * @param imageVector 要绘制的图标矢量。
     * @param contentDescription 无障碍名称；null 表示装饰性图标。
     * @param modifier 应用于图标根节点的修饰符。
     * @param tint 图标着色；Unspecified 时按 Nomi 原生内容色、再按主题 textPrimary 依次回落。
     * @param nativeSpec 图标专属参数；Nomi 外观完全由 [NomiTheme] 令牌决定，本实现忽略它。
     */
    @Composable
    override fun Icon(
        imageVector: ImageVector,
        contentDescription: String?,
        modifier: Modifier,
        tint: Color,
        nativeSpec: NomiIconSpec?,
    ) {
        val contentColor = LocalNomiNativeContentColor.current
        val resolvedTint = when {
            tint != Color.Unspecified -> tint
            contentColor != Color.Unspecified -> contentColor
            else -> NomiTheme.colorScheme.textPrimary
        }
        NativeIcon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            modifier = modifier,
            tint = resolvedTint,
        )
    }
}
