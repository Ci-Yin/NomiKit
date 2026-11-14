package com.ciyin.app.presentation.screen.designsystem

import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystemId
import ciyin.ui.foundation.viewmodel.StateMachineMviViewModel
import com.freeletics.flowredux2.FlowReduxBuilder
import com.freeletics.flowredux2.FlowReduxStateMachineFactory
import com.freeletics.flowredux2.initializeWith
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * 用 FlowRedux2 管理本地同步展示配置；没有网络、加载或失败状态。
 *
 * @param availableDesignSystems 非空且标识唯一的宿主候选实现；按标识保存引用，首项作为默认选择和重置目标。
 * @throws IllegalArgumentException 候选列表为空或存在重复标识时抛出。
 */
@OptIn(ExperimentalCoroutinesApi::class)
internal class DesignSystemDemoViewModel(
    availableDesignSystems: List<NomiDesignSystem>,
) : StateMachineMviViewModel<DesignSystemDemoUiState, DesignSystemDemoAction, DesignSystemDemoEffect>() {
    /**
     * 构造时按标识建立的候选实现映射，支持动作边界校验而不把实现放入 UiState。
     */
    private val designSystems = availableDesignSystems.associateBy { it.id }.also { systems ->
        require(systems.isNotEmpty()) { "展示页至少需要一个设计系统" }
        require(systems.size == availableDesignSystems.size) { "设计系统标识不可重复" }
    }

    /**
     * 宿主候选首项，作为页面默认选择与重置目标。
     */
    private val defaultDesignSystem: NomiDesignSystem = availableDesignSystems.first()

    /**
     * 创建宿主默认配置，所有视觉覆盖均保留为原生默认。
     *
     * @return 使用宿主首项标识、默认交互配置和零点击计数的新状态。
     */
    private fun initialState(): DesignSystemDemoUiState = DesignSystemDemoUiState(
        designSystemId = defaultDesignSystem.id,
    )

    /**
     * 将宿主默认配置注册为状态机的初始内容状态。
     *
     * @receiver 当前 ViewModel 的状态机工厂，负责在启动时创建初始状态。
     */
    override fun FlowReduxStateMachineFactory<DesignSystemDemoUiState, DesignSystemDemoAction>.initialize() {
        initializeWith { initialState() }
    }

    /**
     * 注册展示配置的同步动作处理；返回通过一次性副作用交给宿主。
     *
     * 无效候选、字体比例及不受支持的尺寸不修改状态；重置恢复首项候选及默认配置。
     *
     * @receiver 当前 ViewModel 的状态机规则构建器。
     */
    override fun FlowReduxBuilder<DesignSystemDemoUiState, DesignSystemDemoAction>.spec() {
        inState<DesignSystemDemoUiState> {
            // 切换设计系统并在边界清理不支持的尺寸覆盖。
            on<DesignSystemDemoAction.SelectDesignSystem> { action ->
                val selected = designSystems[action.id]
                if (selected == null) {
                    noChange()
                } else {
                    mutate {
                        copy(
                            designSystemId = selected.id,
                            size = size.takeIf { selected.capabilities.buttonSize },
                        )
                    }
                }
            }
            // 切换当前展示页的深浅色请求。
            on<DesignSystemDemoAction.SelectDarkMode> { action ->
                mutate { copy(darkMode = action.value) }
            }
            // 仅在当前设计系统支持尺寸时保存显式尺寸覆盖。
            on<DesignSystemDemoAction.SelectSize> { action ->
                if (action.value == null || designSystems[snapshot.designSystemId]?.capabilities?.buttonSize == true) {
                    mutate { copy(size = action.value) }
                } else {
                    noChange()
                }
            }
            // 按钮样式所有候选实现均支持，null 用于恢复原生默认。
            on<DesignSystemDemoAction.SelectVariant> { action ->
                mutate { copy(variant = action.value) }
            }
            // 只接受展示页公开的三档字体缩放。
            on<DesignSystemDemoAction.SelectFontScale> { action ->
                if (action.value in DesignSystemDemoFontScales) {
                    mutate { copy(fontScale = action.value) }
                } else {
                    noChange()
                }
            }
            // 切换展示按钮的可用状态，不影响控制面板。
            on<DesignSystemDemoAction.ToggleEnabled> { mutate { copy(enabled = !enabled) } }
            // 切换稀疏强调色覆盖，不生成完整主题令牌。
            on<DesignSystemDemoAction.ToggleCustomTokens> { mutate { copy(customTokens = !customTokens) } }
            // 切换局部设计系统与强调色覆盖示例。
            on<DesignSystemDemoAction.ToggleOverride> { mutate { copy(override = !override) } }
            // 只有启用状态下的展示按钮才增加有效点击次数。
            on<DesignSystemDemoAction.ButtonClick> {
                if (snapshot.enabled) mutate { copy(clickCount = clickCount + 1) } else noChange()
            }
            // 恢复宿主默认设计系统和原生默认视觉参数。
            on<DesignSystemDemoAction.Reset> { override { initialState() } }
            // 返回由宿主导航执行，不把一次性命令放入页面状态。
            onActionEffect<DesignSystemDemoAction.BackClick> { poseEffect(DesignSystemDemoEffect.NavigateBack) }
        }
    }
}
