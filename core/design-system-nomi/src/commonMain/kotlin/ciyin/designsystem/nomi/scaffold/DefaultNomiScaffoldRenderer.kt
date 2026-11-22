package ciyin.designsystem.nomi.scaffold

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
import ciyin.designsystem.component.scaffold.NomiScaffoldColors
import ciyin.designsystem.component.scaffold.NomiScaffoldRenderer
import ciyin.designsystem.component.scaffold.NomiScaffoldSpec
import ciyin.designsystem.component.scaffold.NomiTopAppBarColors
import ciyin.designsystem.component.scaffold.NomiTopAppBarSpec
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

    /**
     * 用 Nomi 令牌组装顶栏：容器 surface、标题 textPrimary + titleLarge、操作 textSecondary。
     *
     * 导航图标、标题与操作槽分别发布 Nomi 原生内容色；栏高取主题的 appBar 令牌，不消费窗口内边距。
     * 导航图标为 null 时不保留占位宽度。
     *
     * @param title 标题槽，位于导航图标与操作槽之间并占据剩余宽度。
     * @param modifier 顶栏根节点的布局修饰符，其后追加行高、容器底色与水平内边距。
     * @param navigationIcon 导航图标槽；null 时不占位。
     * @param actions 操作槽，按行排列。
     * @param colors 逐字段颜色覆盖；Unspecified 回退 Nomi surface/textPrimary/textSecondary，透明色是覆盖。
     * @param nativeSpec 顶栏专属参数；Nomi 外观由令牌决定，本实现忽略它。
     */
    @Composable
    override fun TopAppBar(
        title: @Composable () -> Unit,
        modifier: Modifier,
        navigationIcon: (@Composable () -> Unit)?,
        actions: @Composable RowScope.() -> Unit,
        colors: NomiTopAppBarColors,
        nativeSpec: NomiTopAppBarSpec?,
    ) {
        val scheme = NomiTheme.colorScheme
        val container = colors.containerColor.takeOrElse { scheme.surface }
        val titleColor = colors.titleContentColor.takeOrElse { scheme.textPrimary }
        val navigationColor = colors.navigationIconContentColor.takeOrElse { scheme.textPrimary }
        val actionColor = colors.actionIconContentColor.takeOrElse { scheme.textSecondary }
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(NomiTheme.sizes.componentHeights.appBar)
                .background(container)
                .padding(horizontal = NomiTheme.spacings.extraSmall),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            navigationIcon?.let {
                CompositionLocalProvider(LocalNomiNativeContentColor provides navigationColor) {
                    ProvideNomiContentStyle(
                        color = colors.navigationIconContentColor,
                        textStyle = TextStyle.Default,
                        content = it,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = NomiTheme.spacings.small),
            ) {
                CompositionLocalProvider(LocalNomiNativeContentColor provides titleColor) {
                    ProvideNomiContentStyle(
                        color = colors.titleContentColor,
                        textStyle = NomiTheme.typography.titleLarge,
                        content = title,
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(NomiTheme.spacings.extraSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CompositionLocalProvider(LocalNomiNativeContentColor provides actionColor) {
                    ProvideNomiContentStyle(
                        color = colors.actionIconContentColor,
                        textStyle = TextStyle.Default,
                        content = { actions() },
                    )
                }
            }
        }
    }
}
