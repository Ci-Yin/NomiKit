package ciyin.designsystem.nomi.scaffold

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import ciyin.designsystem.component.scaffold.NomiScaffoldColors
import ciyin.designsystem.component.scaffold.NomiScaffoldRenderer
import ciyin.designsystem.component.scaffold.NomiScaffoldSpec
import ciyin.designsystem.nomi.LocalNomiNativeContentColor
import ciyin.designsystem.runtime.ProvideNomiContentStyle
import ciyin.designsystem.theme.NomiTheme

/**
 * Nomi 自身页面外壳实现；顶栏、主体与底栏在同一个 Column 中顺序排布。
 */
internal object DefaultNomiScaffoldRenderer : NomiScaffoldRenderer {
    /**
     * 未指定容器色时使用 Nomi background，未指定内容色时使用 Nomi textPrimary。
     *
     * 主体占据剩余高度，空槽位没有高度；只有显式内容色发布到跨系统 slot。
     *
     * @param modifier 页面外壳根节点的布局修饰符，其后追加全尺寸与容器背景。
     * @param colors 逐通道颜色覆盖；Unspecified 回退 Nomi background/textPrimary，Transparent 不回退。
     * @param topBar 顶部栏槽；空 lambda 时高度为零。
     * @param bottomBar 底部栏槽；空 lambda 时高度为零。
     * @param content 顶栏与底栏之间的页面主体，占据剩余高度并继承 Nomi 内容色。
     * @param nativeSpec 脚手架专属参数；Nomi 外观由令牌决定，本实现忽略它。
     */
    @Composable
    override fun Scaffold(
        modifier: Modifier,
        colors: NomiScaffoldColors,
        topBar: @Composable () -> Unit,
        bottomBar: @Composable () -> Unit,
        nativeSpec: NomiScaffoldSpec?,
        content: @Composable () -> Unit,
    ) {
        val container = colors.containerColor.takeOrElse { NomiTheme.colorScheme.background }
        val contentColor = colors.contentColor.takeOrElse { NomiTheme.colorScheme.textPrimary }
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(container)
        ) {
            topBar()
            Box(Modifier.weight(1f)) {
                CompositionLocalProvider(LocalNomiNativeContentColor provides contentColor) {
                    ProvideNomiContentStyle(
                        color = colors.contentColor,
                        textStyle = TextStyle.Default,
                        content = content,
                    )
                }
            }
            bottomBar()
        }
    }
}
