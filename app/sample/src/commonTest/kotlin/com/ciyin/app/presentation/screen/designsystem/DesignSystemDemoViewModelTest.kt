package com.ciyin.app.presentation.screen.designsystem

import androidx.lifecycle.viewModelScope
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystemId
import ciyin.designsystem.theme.DarkMode
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 覆盖展示页同步配置、计数与导航，测试不启动任何真实宿主依赖。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DesignSystemDemoViewModelTest {
    /**
     * 各配置独立更新，切换设计系统不清空其它选择或计数。
     *
     * @return 协程测试框架的跨平台执行结果，供测试运行器等待断言与清理完成。
     */
    @Test
    fun choicesUpdateWithoutResettingOtherConfiguration() = runTest {
        withViewModel { viewModel ->
            viewModel.dispatchAction(DesignSystemDemoAction.SelectDarkMode(DarkMode.Dark))
            viewModel.dispatchAction(DesignSystemDemoAction.SelectSize(NomiControlSize.Large))
            viewModel.dispatchAction(DesignSystemDemoAction.SelectVariant(NomiButtonVariant.Destructive))
            viewModel.dispatchAction(DesignSystemDemoAction.SelectFontScale(2f))
            viewModel.dispatchAction(DesignSystemDemoAction.ToggleOverride)
            viewModel.dispatchAction(DesignSystemDemoAction.ButtonClick)
            viewModel.dispatchAction(DesignSystemDemoAction.SelectDesignSystem(SecondSystem))
            val state = viewModel.state.first { it.designSystemId == SecondSystem }
            assertEquals(DarkMode.Dark, state.darkMode)
            assertEquals(NomiControlSize.Large, state.size)
            assertEquals(NomiButtonVariant.Destructive, state.variant)
            assertEquals(2f, state.fontScale)
            assertEquals(1, state.clickCount)
            assertTrue(state.override)
        }
    }

    /**
     * 默认参数保持 null，显式选择之后也可以分别恢复原生默认。
     *
     * @return 协程测试框架的跨平台执行结果，供测试运行器等待断言与清理完成。
     */
    @Test
    fun nativeDefaultsRemainNullAndCanBeRestored() = runTest {
        withViewModel { viewModel ->
            assertEquals(null, viewModel.state.value.size)
            assertEquals(null, viewModel.state.value.variant)
            viewModel.dispatchAction(DesignSystemDemoAction.SelectSize(NomiControlSize.Large))
            viewModel.dispatchAction(DesignSystemDemoAction.SelectVariant(NomiButtonVariant.Primary))
            viewModel.state.first { it.variant == NomiButtonVariant.Primary }
            viewModel.dispatchAction(DesignSystemDemoAction.SelectSize(null))
            viewModel.dispatchAction(DesignSystemDemoAction.SelectVariant(null))
            val restored = viewModel.state.first { it.size == null && it.variant == null }
            assertEquals(DesignSystemDemoUiState(designSystemId = FirstSystem), restored)
        }
    }

    /**
     * 切换设计系统保留尺寸覆盖；不支持的系统由适配器降级，状态层不再清理。
     *
     * @return 协程测试框架的跨平台执行结果，供测试运行器等待断言与清理完成。
     */
    @Test
    fun sizeOverrideSurvivesDesignSystemSwitch() = runTest {
        withViewModel { viewModel ->
            viewModel.dispatchAction(DesignSystemDemoAction.SelectSize(NomiControlSize.Large))
            viewModel.state.first { it.size == NomiControlSize.Large }
            viewModel.dispatchAction(DesignSystemDemoAction.SelectDesignSystem(LimitedSystem))
            val switched = viewModel.state.first { it.designSystemId == LimitedSystem }
            assertEquals(NomiControlSize.Large, switched.size)
            viewModel.dispatchAction(DesignSystemDemoAction.SelectSize(NomiControlSize.Small))
            viewModel.dispatchAction(DesignSystemDemoAction.SelectDesignSystem(FirstSystem))
            val restored = viewModel.state.first { it.designSystemId == FirstSystem }
            assertEquals(NomiControlSize.Small, restored.size)
        }
    }

    /**
     * 禁用时即使误分发点击也不计数，重新启用后恢复。
     *
     * @return 协程测试框架的跨平台执行结果，供测试运行器等待断言与清理完成。
     */
    @Test
    fun disabledButtonsDoNotIncrementCount() = runTest {
        withViewModel { viewModel ->
            viewModel.dispatchAction(DesignSystemDemoAction.ToggleEnabled)
            viewModel.state.first { !it.enabled }
            viewModel.dispatchAction(DesignSystemDemoAction.ButtonClick)
            runCurrent()
            assertEquals(0, viewModel.state.value.clickCount)
            assertFalse(viewModel.state.value.enabled)
            viewModel.dispatchAction(DesignSystemDemoAction.ToggleEnabled)
            viewModel.state.first { it.enabled }
            viewModel.dispatchAction(DesignSystemDemoAction.ButtonClick)
            assertEquals(1, viewModel.state.first { it.clickCount == 1 }.clickCount)
        }
    }

    /**
     * 重置必须恢复宿主首项和全部配置，而非只恢复配色。
     *
     * @return 协程测试框架的跨平台执行结果，供测试运行器等待断言与清理完成。
     */
    @Test
    fun resetRestoresEveryFieldAndHostDefault() = runTest {
        withViewModel { viewModel ->
            viewModel.dispatchAction(DesignSystemDemoAction.SelectDesignSystem(SecondSystem))
            viewModel.dispatchAction(DesignSystemDemoAction.SelectDarkMode(DarkMode.Light))
            viewModel.dispatchAction(DesignSystemDemoAction.SelectSize(NomiControlSize.Small))
            viewModel.dispatchAction(DesignSystemDemoAction.SelectVariant(NomiButtonVariant.Text))
            viewModel.dispatchAction(DesignSystemDemoAction.SelectFontScale(1.3f))
            viewModel.dispatchAction(DesignSystemDemoAction.ToggleOverride)
            viewModel.dispatchAction(DesignSystemDemoAction.ButtonClick)
            viewModel.dispatchAction(DesignSystemDemoAction.ToggleEnabled)
            viewModel.state.first { !it.enabled }
            viewModel.dispatchAction(DesignSystemDemoAction.Reset)
            val defaults = DesignSystemDemoUiState(designSystemId = FirstSystem)
            assertEquals(defaults, viewModel.state.first { it == defaults })
        }
    }

    /**
     * 无效候选与缩放参数不会破坏合法状态。
     *
     * @return 协程测试框架的跨平台执行结果，供测试运行器等待断言与清理完成。
     */
    @Test
    fun unsupportedInputsKeepStateUnchanged() = runTest {
        withViewModel { viewModel ->
            viewModel.dispatchAction(DesignSystemDemoAction.SelectDesignSystem(NomiDesignSystemId("missing")))
            viewModel.dispatchAction(DesignSystemDemoAction.SelectFontScale(Float.NaN))
            viewModel.dispatchAction(DesignSystemDemoAction.SelectFontScale(-1f))
            runCurrent()
            assertEquals(
                DesignSystemDemoUiState(designSystemId = FirstSystem),
                viewModel.state.value
            )
        }
    }

    /**
     * 返回是一次性导航事件，不改变配置。
     *
     * @return 协程测试框架的跨平台执行结果，供测试运行器等待断言与清理完成。
     */
    @Test
    fun backEmitsNavigationEffectWithoutChangingState() = runTest {
        withViewModel { viewModel ->
            val initial = viewModel.state.value
            val effect =
                async(start = CoroutineStart.UNDISPATCHED) { viewModel.sideEffects.first() }
            viewModel.dispatchAction(DesignSystemDemoAction.BackClick)
            assertEquals(DesignSystemDemoEffect.NavigateBack, effect.await())
            assertEquals(initial, viewModel.state.value)
        }
    }

    /**
     * 空目录与重复身份在组装边界即失败，避免界面伪造候选。
     */
    @Test
    fun invalidCatalogFailsAtConstruction() {
        assertFailsWith<IllegalArgumentException> { DesignSystemDemoViewModel(emptyList()) }
        assertFailsWith<IllegalArgumentException> {
            DesignSystemDemoViewModel(listOf(fakeSystem(FirstSystem), fakeSystem(FirstSystem)))
        }
    }

    /**
     * 在受控主调度器中运行并取消 ViewModel 生命周期，防止状态机泄漏。
     *
     * 退出测试块时取消 ViewModel 作用域并恢复主调度器，即使测试块抛出异常也会清理。
     *
     * @receiver 提供虚拟时间调度器的协程测试作用域。
     * @param block 初始状态就绪后执行的挂起断言块，接收包含三个测试候选的 ViewModel；本函数负责其生命周期。
     */
    private suspend fun TestScope.withViewModel(block: suspend (DesignSystemDemoViewModel) -> Unit) {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = DesignSystemDemoViewModel(
            listOf(
                fakeSystem(FirstSystem),
                fakeSystem(SecondSystem),
                fakeSystem(LimitedSystem),
            ),
        )
        try {
            viewModel.state.first()
            runCurrent()
            block(viewModel)
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    /**
     * 测试用宿主候选身份，不构造 renderer。
     */
    private companion object {
        /**
         * 宿主默认系统。
         */
        val FirstSystem = NomiDesignSystemId("host-default")

        /**
         * 宿主第二候选。
         */
        val SecondSystem = NomiDesignSystemId("host-alternative")

        /**
         * 第三个宿主候选身份。
         */
        val LimitedSystem = NomiDesignSystemId("limited")

        /**
         * 用稳定身份包装 Nomi renderer，避免测试构造第三方主题。
         *
         * @param id 测试候选的稳定标识。
         * @return 仅替换身份、其余行为委托给 [NomiDesignSystem.Default] 的新实现。
         */
        fun fakeSystem(id: NomiDesignSystemId): NomiDesignSystem =
            object : NomiDesignSystem by NomiDesignSystem.Default {
                /**
                 * 测试候选身份。
                 */
                override val id: NomiDesignSystemId = id
            }
    }
}
