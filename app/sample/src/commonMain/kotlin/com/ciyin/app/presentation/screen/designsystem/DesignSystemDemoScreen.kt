package com.ciyin.app.presentation.screen.designsystem

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.surface.NomiSurface
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.DarkMode
import ciyin.ui.foundation.WindowSize
import ciyin.ui.foundation.classifyWindowWidth
import ciyin.ui.foundation.viewmodel.collectSideEffects
import ciyin.ui.foundation.viewmodel.viewModel
import com.ciyin.app.presentation.screen.designsystem.component.DesignSystemShowcase
import com.ciyin.app.presentation.screen.designsystem.component.SamplePreviewTheme
import com.ciyin.app.presentation.screen.sample.LocalSampleDesignSystems
import com.ciyin.app.sample.Res
import com.ciyin.app.sample.design_system_demo_back
import com.ciyin.app.sample.design_system_demo_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview

/**
 * 展示页入口：只绑定状态机、事件分发与宿主返回，不在此持有控件状态。
 *
 * @param onBack 状态机发出 NavigateBack 副作用时调用，由宿主执行导航返回。
 */
@Composable
internal fun DesignSystemDemoScreen(onBack: () -> Unit) {
    val systems = LocalSampleDesignSystems.current
    val factory: () -> DesignSystemDemoViewModel = { DesignSystemDemoViewModel(systems) }
    val viewModel = viewModel(
        factory = factory,
        key = systems.joinToString(separator = ",") { it.id.value },
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    viewModel.collectSideEffects { effect ->
        when (effect) {
            DesignSystemDemoEffect.NavigateBack -> onBack()
        }
    }
    DesignSystemDemoContent(
        state = state,
        onAction = viewModel.dispatchAction,
        systems = systems,
    )
}

/**
 * 无真实导航与平台副作用的展示页内容，按配置选择设计系统与字体比例。
 *
 * @param state 外部持有的展示配置快照。
 * @param onAction 用户操作的分发入口，由调用方处理状态更新及返回意图。
 * @param systems 可选实现目录，默认读取宿主 provider；须恰好包含一个与状态标识匹配的实现。
 * @param contentScrollState 整页纵向滚动状态，默认在当前组合中记忆，可由测试或宿主注入。
 * @throws NoSuchElementException 候选目录中没有与状态标识匹配的实现时抛出。
 * @throws IllegalArgumentException 候选目录中有多个与状态标识匹配的实现时抛出。
 */
@Composable
internal fun DesignSystemDemoContent(
    state: DesignSystemDemoUiState,
    onAction: (DesignSystemDemoAction) -> Unit,
    systems: List<NomiDesignSystem> = LocalSampleDesignSystems.current,
    contentScrollState: ScrollState = rememberScrollState(),
) {
    val system = systems.single { it.id == state.designSystemId }
    val alternate = systems.firstOrNull { it.id != system.id }
    val density = LocalDensity.current
    NomiTheme(designSystem = system, darkMode = state.darkMode) {
        CompositionLocalProvider(
            LocalDensity provides Density(
                density = density.density,
                fontScale = state.fontScale
            ),
        ) {
            DesignSystemDemoLayout(
                state = state,
                systems = systems,
                alternateDesignSystem = alternate,
                onAction = onAction,
                contentScrollState = contentScrollState,
            )
        }
    }
}

/**
 * 按可用宽度事实选择上下或左右排布，两个模式都保持一条完整的纵向滚动链。
 *
 * @param state 用于配置控件和组件展示的页面快照。
 * @param system 当前选中的设计系统。
 * @param systems 控制面板列出的候选实现，包含当前选中系统。
 * @param alternateDesignSystem 局部覆盖示例的另一实现；null 时保留当前系统。
 * @param onAction 用户操作的分发入口，状态仍由调用方持有。
 * @param contentScrollState 控制整页纵向滚动的状态。
 */
@Composable
private fun DesignSystemDemoLayout(
    state: DesignSystemDemoUiState,
    systems: List<NomiDesignSystem>,
    alternateDesignSystem: NomiDesignSystem?,
    onAction: (DesignSystemDemoAction) -> Unit,
    contentScrollState: ScrollState,
) {
    AppThemeSurfaceRoot {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            val controlWidth =
                NomiTheme.sizes.layoutConstraints.cardMinWidth + NomiTheme.spacings.huge
            val fontScale = LocalDensity.current.fontScale
            val expanded = classifyWindowWidth(maxWidth) == WindowSize.Expanded &&
                    maxWidth >= controlWidth + NomiTheme.sizes.layoutConstraints.cardMinWidth * fontScale + NomiTheme.spacings.colossal
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(DesignSystemDemoTags.Scroll)
                    .verticalScroll(contentScrollState)
                    .padding(NomiTheme.spacings.large),
                verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.large),
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(NomiTheme.spacings.medium),
                    verticalArrangement = Arrangement.spacedBy(NomiTheme.spacings.small),
                ) {
                    NomiButton(
                        onClick = { onAction(DesignSystemDemoAction.BackClick) },
                        modifier = Modifier.testTag(DesignSystemDemoTags.Back),
                    ) { NomiText(stringResource(Res.string.design_system_demo_back)) }
                    NomiText(
                        text = stringResource(Res.string.design_system_demo_title),
                        style = NomiTheme.typography.headlineMedium,
                    )
                }
                if (expanded) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(NomiTheme.spacings.large),
                    ) {
                        DesignSystemDemoControls(
                            state = state,
                            systems = systems,
                            onAction = onAction,
                            modifier = Modifier.width(controlWidth),
                        )
                        DesignSystemDemoShowcase(
                            state = state,
                            alternateDesignSystem = alternateDesignSystem,
                            onAction = onAction,
                            modifier = Modifier.weight(1f),
                        )
                    }
                } else {
                    DesignSystemDemoControls(
                        state = state,
                        systems = systems,
                        onAction = onAction,
                    )
                    DesignSystemDemoShowcase(
                        state = state,
                        alternateDesignSystem = alternateDesignSystem,
                        onAction = onAction,
                    )
                }
            }
        }
    }
}

/**
 * 根表面只承载页面背景，不主动覆盖原生表面参数。
 *
 * @param content 在原生表面内容环境中组合的页面，无接收者作用域。
 */
@Composable
private fun AppThemeSurfaceRoot(content: @Composable () -> Unit) {
    NomiSurface(
        modifier = Modifier.fillMaxSize().testTag(DesignSystemDemoTags.Root),
    ) {
        content()
    }
}

/**
 * 将页面状态缩减为纯展示模型，组件不感知 Action 或 Effect。
 *
 * @param state 提供尺寸、样式、启用状态、局部覆盖开关和点击计数的页面快照。
 * @param alternateDesignSystem 局部覆盖示例的备用实现；null 时沿用当前系统。
 * @param onAction 展示按钮点击时接收 ButtonClick，由调用方更新计数。
 * @param modifier 传给展示区域根容器的修饰符。
 */
@Composable
private fun DesignSystemDemoShowcase(
    state: DesignSystemDemoUiState,
    alternateDesignSystem: NomiDesignSystem?,
    onAction: (DesignSystemDemoAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    DesignSystemShowcase(
        model = DesignSystemDemoModel(
            size = state.size,
            variant = state.variant,
            enabled = state.enabled,
            override = state.override,
            clickCount = state.clickCount,
            alternateDesignSystem = alternateDesignSystem,
        ),
        onButtonClick = { onAction(DesignSystemDemoAction.ButtonClick) },
        modifier = modifier,
    )
}

/**
 * 默认内容态；预览使用新主题与纯 Content，不启动真实状态机。
 */
@AppPreview
@Composable
private fun DesignSystemDemoContentPreview() {
    SamplePreviewTheme { DesignSystemDemoContent(state = DesignSystemDemoUiState(), onAction = {}) }
}

/**
 * 关键交互态：深色、大字、局部覆盖和禁用按钮。
 */
@AppPreview
@Composable
private fun DesignSystemDemoConfiguredPreview() {
    SamplePreviewTheme {
        DesignSystemDemoContent(
            state = DesignSystemDemoUiState(
                darkMode = DarkMode.Dark,
                enabled = false,
                override = true,
                fontScale = 2f,
                clickCount = 7,
            ),
            onAction = {},
        )
    }
}
