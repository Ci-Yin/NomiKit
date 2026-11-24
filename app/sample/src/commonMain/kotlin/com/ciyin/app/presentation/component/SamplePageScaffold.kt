package com.ciyin.app.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import io.github.composefluent.icons.Icons
import io.github.composefluent.icons.regular.ArrowLeft
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ciyin.designsystem.component.button.NomiIconButton
import ciyin.designsystem.component.icon.NomiIcon
import ciyin.designsystem.component.scaffold.NomiScaffold
import ciyin.designsystem.component.scaffold.NomiScaffoldColors
import ciyin.designsystem.component.scaffold.NomiTopAppBar
import ciyin.designsystem.component.scaffold.NomiTopAppBarColors
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.fluent.navigation.LocalFluentSystemBackdropActive
import ciyin.designsystem.material3.toMaterial3ColorScheme
import ciyin.designsystem.material3.toMaterial3Shapes
import ciyin.designsystem.material3.toMaterial3Typography
import ciyin.designsystem.theme.LocalNomiTheme
import ciyin.designsystem.theme.NomiTheme

/**
 * 样例页统一外壳：页面与顶栏共用透明或实色背景，返回按钮与标题样式保持一致。
 *
 * 内容由脚手架实现完成顶栏让位，调用方不再接收内边距。仍读取原生主题的既有实现
 * 由 [SampleMaterialTheme] 提供兼容上下文，避免明暗切换时页面背景与组件配色脱节。
 *
 * @param title 顶栏标题。
 * @param onBack 返回上一页的回调。
 * @param modifier 页面根节点修饰符；测试标签等作用在整个脚手架上。
 * @param backContentDescription 返回按钮的无障碍名称。
 * @param backModifier 返回按钮的修饰符；需要测试标签时传入。
 * @param actions 顶栏尾部操作槽。
 * @param topBarColors 顶栏颜色覆盖；null 时使用当前 Nomi 主题颜色。
 * @param content 顶栏之下的页面主体。
 */
@Composable
internal fun SamplePageScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backContentDescription: String = "返回",
    backModifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    topBarColors: NomiTopAppBarColors? = null,
    content: @Composable () -> Unit,
) {
    SampleMaterialTheme {
        NomiScaffold(
            modifier = modifier.windowInsetsPadding(WindowInsets.safeDrawing),
            colors = NomiScaffoldColors(containerColor = samplePageBackgroundColor()),
            topBar = {
                NomiTopAppBar(
                    title = { NomiText(text = title) },
                    navigationIcon = {
                        NomiIconButton(onClick = onBack, modifier = backModifier) {
                            NomiIcon(
                                imageVector = Icons.Regular.ArrowLeft,
                                contentDescription = backContentDescription,
                            )
                        }
                    },
                    actions = actions,
                    colors = topBarColors ?: NomiTopAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = NomiTheme.colorScheme.textPrimary,
                        navigationIconContentColor = NomiTheme.colorScheme.textPrimary,
                        actionIconContentColor = NomiTheme.colorScheme.textSecondary,
                    ),
                )
            },
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }
}

/**
 * 样例页统一背景：系统窗口材质生效时完全透明，让 Mica/Acrylic 直接透出；否则使用当前主题实色背景。
 *
 * 透明模式下不再叠加主题遮罩，页面内容需自行提供可读的表面与文字颜色。
 * 顶栏使用透明容器，与页面共用同一层背景。
 *
 * @return 可用于脚手架背景的页面颜色。
 */
@Composable
@ReadOnlyComposable
internal fun samplePageBackgroundColor(): Color = if (LocalFluentSystemBackdropActive.current) {
    NomiTheme.colorScheme.background.copy(alpha = 0f)
} else {
    NomiTheme.colorScheme.background
}

/**
 * 为仍读取原生主题的既有实现提供与当前 Nomi 主题同步的 MaterialTheme。
 *
 * 页面新增组件应优先使用 Nomi 组件；本桥仅用于 feature/video-player 等尚未迁移的原生 UI、
 * Nomi 输入框的内部原生实现，以及保留的 AlertDialog 宿主。
 *
 * @param content 需要原生主题上下文的组合内容。
 */
@Composable
internal fun SampleMaterialTheme(content: @Composable () -> Unit) {
    val theme = LocalNomiTheme.current
    MaterialTheme(
        colorScheme = theme.colorScheme.toMaterial3ColorScheme(theme.isDark),
        typography = theme.typography.toMaterial3Typography(),
        shapes = theme.shapes.toMaterial3Shapes(),
        content = content,
    )
}
