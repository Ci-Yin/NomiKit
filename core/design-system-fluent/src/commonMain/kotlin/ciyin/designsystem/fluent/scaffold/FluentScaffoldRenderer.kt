package ciyin.designsystem.fluent.scaffold

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
import ciyin.designsystem.runtime.ProvideNomiContentStyle
import io.github.composefluent.FluentTheme
import io.github.composefluent.LocalContentAlpha
import io.github.composefluent.LocalContentColor
import io.github.composefluent.ProvideTextStyle

/**
 * Fluent 页面外壳适配；上游没有 Scaffold，用原生 Layer 色板与内容环境建立等价结构。
 *
 * 顶栏、主体与底栏在同一个 Column 中顺序排布，主体占据剩余高度，因此空槽位自然没有高度。
 */
internal object FluentScaffoldRenderer : NomiScaffoldRenderer {
    /**
     * 未指定容器色时使用原生 layer.default，未指定内容色时使用原生 text.primary。
     *
     * 内容环境与原生 Layer 保持一致：正文排版、内容色与内容 alpha 一并提供，主体只把显式内容色写入中立 slot。
     *
     * @param modifier 页面外壳根节点的布局修饰符，其后追加全尺寸与容器背景。
     * @param colors 逐通道颜色覆盖；Unspecified 分别回退原生 layer.default 与 text.primary，透明色仍是覆盖。
     * @param topBar 顶部栏槽；空 lambda 时高度为零。
     * @param bottomBar 底部栏槽；空 lambda 时高度为零。
     * @param nativeSpec 脚手架专属参数；上游没有 Scaffold，Fluent 侧没有实现，一律忽略。
     * @param content 顶栏与底栏之间的页面主体，占据剩余高度并继承原生内容环境。
     */
    @Composable
    override fun Scaffold(
        modifier: Modifier,
        colors: NomiScaffoldColors,
        topBar: @Composable () -> Unit,
        bottomBar: @Composable () -> Unit,
        @Suppress("UNUSED_PARAMETER") nativeSpec: NomiScaffoldSpec?,
        content: @Composable () -> Unit,
    ) {
        val container =
            colors.containerColor.takeOrElse { FluentTheme.colors.background.layer.default }
        val contentColor = colors.contentColor.takeOrElse { FluentTheme.colors.text.text.primary }
        ProvideTextStyle(FluentTheme.typography.body.copy(color = contentColor)) {
            CompositionLocalProvider(
                LocalContentColor provides contentColor,
                LocalContentAlpha provides contentColor.alpha,
            ) {
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .background(container)
                ) {
                    topBar()
                    Box(Modifier.weight(1f)) {
                        ProvideNomiContentStyle(
                            color = colors.contentColor,
                            textStyle = TextStyle.Default,
                            content = content,
                        )
                    }
                    bottomBar()
                }
            }
        }
    }
}
