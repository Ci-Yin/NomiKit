package com.ciyin.app

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 经真实 MainActivity → App → SampleHub 进入展示页的 Android 端到端验证。
 *
 * 不调用 setContent，不替换应用入口或 ViewModel，不依赖 sample 的 internal 类型。
 * 执行需要用户批准的 Android 真机或模拟器；离线编译不代表仪器测试已通过。
 */
@RunWith(AndroidJUnit4::class)
class DesignSystemDemoInstrumentedTest {
    /**
     * 启动产品现有 Activity，由应用自身创建导航与主题环境。
     */
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    /**
     * 默认 Nomi，可切换 M3、深色与两倍字体，计数仍保留且返回恢复样例入口。
     */
    @Test
    fun switchesDesignSystemModeAndFontThenReturnsToHub() {
        openGalleryFromHub()
        awaitSelected(NomiTag)
        awaitSelected("design-system-demo-size-default")
        awaitSelected("design-system-demo-variant-default")
        awaitCounter(expected = 0)

        clickGallery(CounterButtonTag)
        awaitCounter(expected = 1)
        selectGallery(Material3Tag)
        selectGallery(DarkTag)
        selectGallery(FontTwoTag)
        awaitCounter(expected = 1)

        composeRule.onNodeWithTag(LongButtonTag).performScrollTo().assertIsDisplayed()
        clickGallery(CounterButtonTag)
        awaitCounter(expected = 2)
        awaitSelected(Material3Tag)
        awaitSelected(DarkTag)
        awaitSelected(FontTwoTag)

        clickGallery(BackTag)
        awaitExists(HubEntryTag)
        composeRule.onNodeWithTag(HubEntryTag).assertIsDisplayed()
        composeRule.waitUntil(timeoutMillis = UiTimeoutMillis) {
            composeRule.onAllNodesWithTag(RootTag).fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithTag(RootTag).assertDoesNotExist()
    }

    /**
     * Nomi、M3 和 Fluent 的禁用按钮均拒绝真实触摸，重新启用后恢复计数。
     */
    @Test
    fun disabledButtonsNeverIncrementForAllDesignSystems() {
        openGalleryFromHub()
        listOf(NomiTag, Material3Tag, FluentTag).forEach { systemTag ->
            selectGallery(systemTag)
            clickGallery(ResetTag)
            awaitCounter(expected = 0)
            // 重置会恢复宿主默认 Nomi，因此随后再次选择当前待验证系统。
            selectGallery(systemTag)

            clickGallery(CounterButtonTag)
            awaitCounter(expected = 1)
            touchDisabledButton(DisabledButtonTag)
            assertCounter(expected = 1)

            clickGallery(EnabledToggleTag)
            composeRule.waitUntil(timeoutMillis = UiTimeoutMillis) {
                composeRule.onNodeWithTag(CounterButtonTag)
                    .fetchSemanticsNode().config.contains(SemanticsProperties.Disabled)
            }
            touchDisabledButton(CounterButtonTag)
            assertCounter(expected = 1)

            clickGallery(EnabledToggleTag)
            composeRule.waitUntil(timeoutMillis = UiTimeoutMillis) {
                !composeRule.onNodeWithTag(CounterButtonTag)
                    .fetchSemanticsNode().config.contains(SemanticsProperties.Disabled)
            }
            composeRule.onNodeWithTag(CounterButtonTag).assertIsEnabled()
            clickGallery(CounterButtonTag)
            awaitCounter(expected = 2)
        }
    }

    /**
     * Android 直接使用共享 Fluent，切换深色与两倍字体后状态仍保持。
     */
    @Test
    fun fluentIsAvailableWithoutDesktopHost() {
        openGalleryFromHub()
        selectGallery(FluentTag)
        composeRule.onNodeWithTag("design-system-demo-size-Large").assertDoesNotExist()
        awaitSelected("design-system-demo-variant-default")
        selectGallery(DarkTag)
        selectGallery(FontTwoTag)
        clickGallery(CounterButtonTag)
        awaitCounter(expected = 1)
        awaitSelected(FluentTag)
        awaitSelected(DarkTag)
        awaitSelected(FontTwoTag)
        clickGallery(BackTag)
        awaitExists(HubEntryTag)
    }

    /**
     * 点击首页第一个真实样例条目，不走测试专用导航捷径。
     */
    private fun openGalleryFromHub() {
        awaitExists(HubEntryTag)
        composeRule.onNodeWithTag(HubEntryTag).assertIsDisplayed().performClick()
        awaitExists(RootTag)
        composeRule.onNodeWithTag(RootTag).assertIsDisplayed()
    }

    /**
     * 点击选项并等待异步状态机提交选中状态，不使用固定睡眠拼接时序。
     *
     * @param tag 目标选择项的稳定语义标签；点击前先将其滚动到可见区域。
     */
    private fun selectGallery(tag: String) {
        clickGallery(tag)
        awaitSelected(tag)
    }

    /**
     * 页面是全页滚动布局，先滚动到对应稳定标签再分发点击。
     *
     * @param tag 待点击节点的唯一稳定语义标签。
     */
    private fun clickGallery(tag: String) {
        composeRule.onNodeWithTag(tag).performScrollTo().assertIsDisplayed().performClick()
    }

    /**
     * 用真实触摸而非强行调用 OnClick 语义验证 disabled 行为。
     *
     * @param tag 应处于禁用状态的按钮标签；滚动到可见后断言禁用，再发送触摸并等待空闲。
     */
    private fun touchDisabledButton(tag: String) {
        composeRule.onNodeWithTag(tag)
            .performScrollTo()
            .assertIsDisplayed()
            .assertIsNotEnabled()
            .performTouchInput { click() }
        composeRule.waitForIdle()
    }

    /**
     * 等待节点进入语义树，覆盖应用启动与真实导航过渡。
     *
     * @param tag 预期恰好出现一个节点的稳定标签，等待上限由 [UiTimeoutMillis] 指定。
     */
    private fun awaitExists(tag: String) {
        composeRule.waitUntil(timeoutMillis = UiTimeoutMillis) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().size == 1
        }
    }

    /**
     * 以控件真实 Selected 语义确认用户选择已反馈到 UI。
     *
     * @param tag 待确认的唯一选项标签；在等待上限内应发布 selected = true。
     */
    private fun awaitSelected(tag: String) {
        composeRule.waitUntil(timeoutMillis = UiTimeoutMillis) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().any { node ->
                node.config.contains(SemanticsProperties.Selected) &&
                        node.config[SemanticsProperties.Selected]
            }
        }
        composeRule.onNodeWithTag(tag).assertIsSelected()
    }

    /**
     * 等待有效点击数真实呈现在界面，不直接读取 ViewModel。
     *
     * @param expected 计数节点最终应显示的有效点击次数。
     */
    private fun awaitCounter(expected: Int) {
        composeRule.waitUntil(timeoutMillis = UiTimeoutMillis) { displayedCounter() == expected }
        assertCounter(expected)
    }

    /**
     * 断言界面当前有效点击数与预期一致，不额外等待状态变化。
     *
     * @param expected 当前计数节点应显示的有效点击次数。
     */
    private fun assertCounter(expected: Int) {
        assertEquals(expected, displayedCounter())
    }

    /**
     * 从计数专用节点读取唯一数字，不绑定当前语言的前后文。
     *
     * @return 计数文本中唯一的非负十进制整数。
     * @throws AssertionError 节点文本不是恰好包含一个整数时断言失败。
     */
    private fun displayedCounter(): Int {
        val text = composeRule.onNodeWithTag(CounterTag)
            .fetchSemanticsNode().config[SemanticsProperties.Text]
            .joinToString(separator = " ") { it.text }
        val numbers = Regex("[0-9]+").findAll(text).map { it.value }.toList()
        assertTrue("计数节点应且仅应含一个整数：$text", numbers.size == 1)
        return numbers.single().toInt()
    }

    /**
     * 跨模块测试使用公开语义约定，不向 sample 暴露内部测试 API。
     */
    private companion object {
        /**
         * 启动与状态机同步的最大等待时间，单位为毫秒，不是固定延迟。
         */
        const val UiTimeoutMillis = 10_000L

        /**
         * 样例首页第一个设计系统条目。
         */
        const val HubEntryTag = "design-system-hub-entry"

        /**
         * 展示页根节点。
         */
        const val RootTag = "design-system-demo-root"

        /**
         * 默认 Nomi 选择项。
         */
        const val NomiTag = "design-system-demo-system-nomi"

        /**
         * Material 3 选择项。
         */
        const val Material3Tag = "design-system-demo-system-material3"

        /**
         * 跨平台 Fluent 选择项。
         */
        const val FluentTag = "design-system-demo-system-fluent"

        /**
         * 深色模式选择项。
         */
        const val DarkTag = "design-system-demo-mode-Dark"

        /**
         * 两倍字体选择项。
         */
        const val FontTwoTag = "design-system-demo-font-2.0"

        /**
         * 计数值文本。
         */
        const val CounterTag = "design-system-demo-counter"

        /**
         * 有效点击计数按钮。
         */
        const val CounterButtonTag = "design-system-demo-counter-button"

        /**
         * 始终禁用的示例按钮。
         */
        const val DisabledButtonTag = "design-system-demo-disabled-button"

        /**
         * 启用状态控制按钮。
         */
        const val EnabledToggleTag = "design-system-demo-enabled"

        /**
         * 长中文自适应高度按钮。
         */
        const val LongButtonTag = "design-system-demo-long-button"

        /**
         * 重置全部展示配置。
         */
        const val ResetTag = "design-system-demo-reset"

        /**
         * 展示页返回操作。
         */
        const val BackTag = "design-system-demo-back"
    }
}
