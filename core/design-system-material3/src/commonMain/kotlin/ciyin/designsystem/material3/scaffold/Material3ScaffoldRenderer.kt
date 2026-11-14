package ciyin.designsystem.material3.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.Scaffold as NativeScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import ciyin.designsystem.component.scaffold.NomiScaffoldColors
import ciyin.designsystem.component.scaffold.NomiScaffoldRenderer
import ciyin.designsystem.runtime.ProvideNomiContentStyle

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
     * @param content 让出原生内边距后的页面主体；只有显式内容色发布为中立 slot 增量。
     */
    @Composable
    override fun Scaffold(
        modifier: Modifier,
        colors: NomiScaffoldColors,
        topBar: @Composable () -> Unit,
        bottomBar: @Composable () -> Unit,
        content: @Composable () -> Unit,
    ) {
        val container = colors.containerColor.takeOrElse { MaterialTheme.colorScheme.background }
        NativeScaffold(
            modifier = modifier,
            containerColor = container,
            contentColor = colors.contentColor.takeOrElse { contentColorFor(container) },
            topBar = topBar,
            bottomBar = bottomBar,
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
}
