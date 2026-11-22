package ciyin.designsystem.material3.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.Scaffold as NativeScaffold
import androidx.compose.material3.TopAppBar as NativeTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.ScaffoldDefaults
import ciyin.designsystem.component.scaffold.NomiScaffoldColors
import ciyin.designsystem.component.scaffold.NomiScaffoldRenderer
import ciyin.designsystem.component.scaffold.NomiScaffoldSpec
import ciyin.designsystem.component.scaffold.NomiTopAppBarColors
import ciyin.designsystem.component.scaffold.NomiTopAppBarSpec
import ciyin.designsystem.runtime.ProvideNomiContentStyle
import ciyin.designsystem.runtime.findSpec

/**
 * Material 3 原生 Scaffold 适配；页面外壳、内容让位与系统栏内边距全部交给原生组件。
 */
object Material3ScaffoldRenderer : NomiScaffoldRenderer {
    /**
     * 未指定容器色时使用原生 background，未指定内容色时按原生规则由容器色推导。
     *
     * 原生 Scaffold 的顶栏与底栏悬浮在内容之上，因此主体内容按 innerPadding 让位并消费对应窗口内边距。
     *
     * @param modifier 原生 Scaffold 的布局修饰符。
     * @param colors 逐通道颜色覆盖；容器 Unspecified 使用原生 background，内容 Unspecified 由 contentColorFor 推导，透明色为覆盖。
     * @param topBar 原生顶部栏槽。
     * @param bottomBar 原生底部栏槽。
     * @param nativeSpec 脚手架专属参数；只识别 Material 3 定义的类型，其它系统的参数静默忽略。
     * @param content 让出原生内边距后的页面主体；只有显式内容色发布为中立 slot 增量。
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
        val spec = nativeSpec.findSpec<Material3ScaffoldSpec>()
        val container = colors.containerColor.takeOrElse { MaterialTheme.colorScheme.background }
        NativeScaffold(
            modifier = modifier,
            containerColor = container,
            contentColor = colors.contentColor.takeOrElse { contentColorFor(container) },
            topBar = topBar,
            bottomBar = bottomBar,
            snackbarHost = spec?.snackbarHost ?: {},
            floatingActionButton = spec?.floatingActionButton ?: {},
            contentWindowInsets = spec?.contentWindowInsets ?: ScaffoldDefaults.contentWindowInsets,
        ) { innerPadding ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) {
                ProvideNomiContentStyle(
                    color = colors.contentColor,
                    textStyle = TextStyle.Default,
                    content = content,
                )
            }
        }
    }

    /**
     * 直接使用原生顶栏，颜色经原生色板逐字段映射。
     *
     * 未指定的颜色由原生 `takeOrElse` 回退到默认顶栏色板；系统栏内边距沿用原生 `windowInsets` 默认值。
     *
     * @param title 标题槽。
     * @param modifier 原生顶栏的布局修饰符。
     * @param navigationIcon 导航图标槽；null 时交给原生空槽，标题保留原生缩进。
     * @param actions 操作槽。
     * @param colors 逐字段颜色覆盖；Unspecified 保留原生值，透明色为覆盖。
     * @param nativeSpec 顶栏专属参数；上游没有具体类型，忽略。
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun TopAppBar(
        title: @Composable () -> Unit,
        modifier: Modifier,
        navigationIcon: (@Composable () -> Unit)?,
        actions: @Composable RowScope.() -> Unit,
        colors: NomiTopAppBarColors,
        @Suppress("UNUSED_PARAMETER") nativeSpec: NomiTopAppBarSpec?,
    ) {
        NativeTopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = { navigationIcon?.invoke() },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colors.containerColor,
                titleContentColor = colors.titleContentColor,
                navigationIconContentColor = colors.navigationIconContentColor,
                actionIconContentColor = colors.actionIconContentColor,
            ),
        )
    }
}
