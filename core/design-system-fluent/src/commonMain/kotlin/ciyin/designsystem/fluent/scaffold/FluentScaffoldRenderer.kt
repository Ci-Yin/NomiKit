package ciyin.designsystem.fluent.scaffold

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.scaffold.NomiScaffoldColors
import ciyin.designsystem.component.scaffold.NomiScaffoldRenderer
import ciyin.designsystem.component.scaffold.NomiScaffoldSpec
import ciyin.designsystem.component.scaffold.NomiTopAppBarColors
import ciyin.designsystem.component.scaffold.NomiTopAppBarSpec
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

    /**
     * Fluent 上游没有原生顶栏，用原生 Layer 色板与内容环境组装同结构外壳。
     *
     * 栏高取与原生命令栏相同的 48dp，标题排版取 bodyStrong；导航图标、标题与操作分别发布原生内容色
     * 与内容 alpha。导航图标为 null 时不保留占位宽度。
     *
     * @param title 标题槽，位于导航图标与操作槽之间并占据剩余宽度。
     * @param modifier 顶栏根节点的布局修饰符，其后追加行高、容器底色与水平内边距。
     * @param navigationIcon 导航图标槽；null 时不占位。
     * @param actions 操作槽，按行排列。
     * @param colors 逐字段颜色覆盖；Unspecified 回退 layer.default/text.primary/text.secondary，透明色是覆盖。
     * @param nativeSpec 顶栏专属参数；上游没有原生顶栏，忽略。
     */
    @Composable
    override fun TopAppBar(
        title: @Composable () -> Unit,
        modifier: Modifier,
        navigationIcon: (@Composable () -> Unit)?,
        actions: @Composable RowScope.() -> Unit,
        colors: NomiTopAppBarColors,
        @Suppress("UNUSED_PARAMETER") nativeSpec: NomiTopAppBarSpec?,
    ) {
        val container =
            colors.containerColor.takeOrElse { FluentTheme.colors.background.layer.default }
        val titleColor =
            colors.titleContentColor.takeOrElse { FluentTheme.colors.text.text.primary }
        val navigationColor = colors.navigationIconContentColor.takeOrElse {
            FluentTheme.colors.text.text.primary
        }
        val actionColor = colors.actionIconContentColor.takeOrElse {
            FluentTheme.colors.text.text.secondary
        }
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(FluentTopAppBarHeight)
                .background(container)
                .padding(horizontal = FluentTopAppBarHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FluentTopAppBarSpacing),
        ) {
            navigationIcon?.let {
                CompositionLocalProvider(
                    LocalContentColor provides navigationColor,
                    LocalContentAlpha provides navigationColor.alpha,
                ) {
                    it()
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = FluentTopAppBarHorizontalPadding),
            ) {
                ProvideTextStyle(FluentTheme.typography.bodyStrong.copy(color = titleColor)) {
                    CompositionLocalProvider(LocalContentColor provides titleColor) {
                        title()
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(FluentTopAppBarSpacing)) {
                CompositionLocalProvider(
                    LocalContentColor provides actionColor,
                    LocalContentAlpha provides actionColor.alpha,
                ) {
                    actions()
                }
            }
        }
    }
}

/**
 * Fluent 顶栏高度，与原生命令栏标准高度一致。
 */
private val FluentTopAppBarHeight = 48.dp

/**
 * Fluent 顶栏水平内边距。
 */
private val FluentTopAppBarHorizontalPadding = 8.dp

/**
 * Fluent 顶栏槽间距。
 */
private val FluentTopAppBarSpacing = 4.dp
