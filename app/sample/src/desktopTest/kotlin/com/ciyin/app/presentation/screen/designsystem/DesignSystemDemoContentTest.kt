package com.ciyin.app.presentation.screen.designsystem

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.fluent.Fluent
import ciyin.designsystem.material3.Material3
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.runtime.NomiDesignSystemId
import ciyin.designsystem.theme.DarkMode
import com.ciyin.app.presentation.screen.sample.ProvideSampleDesignSystems
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 对真实纯 Content 的语义与动作边界进行验证，不启动导航或 ViewModel。
 */
@OptIn(ExperimentalTestApi::class)
class DesignSystemDemoContentTest {
    /**
     * 默认目录在共享层提供 Fluent，页面无需 Desktop host 注入即可渲染。
     */
    @Test
    fun sharedCatalogOffersFluentWithoutHostInjection() = runComposeUiTest {
        val actions = mutableListOf<DesignSystemDemoAction>()
        setContent {
            DesignSystemDemoContent(
                state = DesignSystemDemoUiState(designSystemId = NomiDesignSystem.Fluent.id),
                onAction = actions::add,
            )
        }
        onNodeWithTag("design-system-demo-system-nomi").assertExists()
        onNodeWithTag("design-system-demo-system-material3").assertExists()
        onNodeWithTag("design-system-demo-system-fluent").assertExists()
        onNodeWithTag(DesignSystemDemoTags.CounterButton).performScrollTo().performClick()
        runOnIdle {
            assertEquals(
                listOf<DesignSystemDemoAction>(DesignSystemDemoAction.ButtonClick),
                actions
            )
        }
    }

    /**
     * 原生默认区域四组件独立存在，默认选择为 nullable 原生参数。
     */
    @Test
    fun nativeAreaContainsFourComponentsAndNullSelections() = runComposeUiTest {
        setContent { DesignSystemDemoContent(state = DesignSystemDemoUiState(), onAction = {}) }
        onNodeWithTag("design-system-demo-native-text").assertExists()
        onNodeWithTag("design-system-demo-native-button").assertHasClickAction()
        onNodeWithTag("design-system-demo-native-surface").assertExists()
        onNodeWithTag("design-system-demo-native-card").assertHasNoClickAction()
        onNodeWithTag("design-system-demo-size-default").assertIsSelected()
        onNodeWithTag("design-system-demo-variant-default").assertIsSelected()
    }

    /**
     * Fluent 不暴露尺寸选择，每种变体仅渲染原生尺寸样例；不会以小中大标签伪造支持。
     */
    @Test
    fun fluentHidesUnsupportedSizeAndRendersOneNativeSizePerVariant() = runComposeUiTest {
        setContent {
            DesignSystemDemoContent(
                state = DesignSystemDemoUiState(designSystemId = NomiDesignSystem.Fluent.id),
                onAction = {},
            )
        }
        onNodeWithTag("design-system-demo-size-default").assertDoesNotExist()
        NomiControlSize.entries.forEach { size ->
            onNodeWithTag("design-system-demo-size-$size").assertDoesNotExist()
        }
        NomiButtonVariant.entries.forEach { variant ->
            onNodeWithTag("design-system-demo-explicit-button-$variant-default").assertExists()
            NomiControlSize.entries.forEach { size ->
                onNodeWithTag("design-system-demo-explicit-button-$variant-$size").assertDoesNotExist()
            }
        }
    }

    /**
     * 验证原生能力说明默认收起，点击切换按钮可展开并再次收起详情。
     */
    @Test
    fun fluentCapabilityDetailsCanBeExpandedAndCollapsed() = runComposeUiTest {
        setContent {
            DesignSystemDemoContent(
                state = DesignSystemDemoUiState(designSystemId = NomiDesignSystem.Fluent.id),
                onAction = {},
            )
        }
        onNodeWithTag("design-system-demo-restrictions-details").assertDoesNotExist()
        onNodeWithTag("design-system-demo-restrictions-toggle").performScrollTo().performClick()
        onNodeWithTag("design-system-demo-restrictions-details").assertExists()
        onNodeWithTag("design-system-demo-restrictions-toggle").performScrollTo().performClick()
        onNodeWithTag("design-system-demo-restrictions-details").assertDoesNotExist()
    }

    /**
     * 宿主可注入新身份，sample 不需要知道实现所在的平台或模块。
     */
    @Test
    fun hostCatalogSuppliesUnknownDesignSystem() = runComposeUiTest {
        val hostSystem = object : NomiDesignSystem by NomiDesignSystem.Default {
            /**
             * 测试宿主的独立身份。
             */
            override val id = NomiDesignSystemId("host-test")
        }
        setContent {
            ProvideSampleDesignSystems(systems = listOf(hostSystem)) {
                DesignSystemDemoContent(
                    state = DesignSystemDemoUiState(designSystemId = hostSystem.id),
                    onAction = {},
                )
            }
        }
        onNodeWithTag("design-system-demo-system-host-test").assertExists()
        onNodeWithTag("design-system-demo-system-material3").assertDoesNotExist()
    }

    /**
     * 列表项分区同时提供可点击行与静态行；尾部动作按钮保持独立点击语义。
     */
    @Test
    fun listItemSectionExposesClickableAndStaticRows() = runComposeUiTest {
        setContent { DesignSystemDemoContent(state = DesignSystemDemoUiState(), onAction = {}) }
        onNodeWithTag(DesignSystemDemoTags.ListItemClickable)
            .performScrollTo()
            .assertHasClickAction()
        onNodeWithTag(DesignSystemDemoTags.ListItemValue, useUnmergedTree = true).assertExists()
        onNodeWithTag(DesignSystemDemoTags.ListItemStatic).assertExists()
        onNodeWithTag(DesignSystemDemoTags.ListItemAction)
            .performScrollTo()
            .assertHasClickAction()
    }

    /**
     * 十五种角色均可选择；默认仅预览正文，点击后原样展示所选角色。
     */
    @Test
    fun typographyExamplesAreComplete() = runComposeUiTest {
        setContent { DesignSystemDemoContent(state = DesignSystemDemoUiState(), onAction = {}) }
        repeat(15) { index -> onNodeWithTag("text-role-choice-$index").assertHasClickAction() }
        onNodeWithTag("text-role-choice-10").assertIsSelected()
        onNodeWithTag("text-role-preview").assertTextEquals("BodyMedium · 排版示例")
        onNodeWithTag("text-role-choice-0").performScrollTo().performClick()
        onNodeWithTag("text-role-choice-0").assertIsSelected()
        onNodeWithTag("text-role-preview").assertTextEquals("DisplayLarge · 排版示例")
        onNodeWithTag("text-role-choice-10").performScrollTo().performClick()
        onNodeWithTag("text-role-preview").assertTextEquals("BodyMedium · 排版示例")
        onNodeWithTag(DesignSystemDemoTags.LongText).assertExists()
        onNodeWithTag(DesignSystemDemoTags.MultilineText).assertExists()
    }

    /**
     * 控制面板的设计系统、模式、字体、尺寸、样式及开关均转发对应动作。
     */
    @Test
    fun controlsDispatchTypedActions() = runComposeUiTest {
        val actions = mutableListOf<DesignSystemDemoAction>()
        setContent {
            DesignSystemDemoContent(
                state = DesignSystemDemoUiState(),
                onAction = actions::add
            )
        }
        val choices = listOf(
            "design-system-demo-system-material3" to DesignSystemDemoAction.SelectDesignSystem(
                NomiDesignSystem.Material3.id
            ),
            "design-system-demo-system-fluent" to DesignSystemDemoAction.SelectDesignSystem(
                NomiDesignSystem.Fluent.id
            ),
            "design-system-demo-mode-Dark" to DesignSystemDemoAction.SelectDarkMode(DarkMode.Dark),
            "design-system-demo-font-1.3" to DesignSystemDemoAction.SelectFontScale(1.3f),
            "design-system-demo-size-default" to DesignSystemDemoAction.SelectSize(null),
            "design-system-demo-size-Large" to DesignSystemDemoAction.SelectSize(NomiControlSize.Large),
            "design-system-demo-variant-default" to DesignSystemDemoAction.SelectVariant(null),
            "design-system-demo-variant-Destructive" to DesignSystemDemoAction.SelectVariant(
                NomiButtonVariant.Destructive
            ),
            DesignSystemDemoTags.Enabled to DesignSystemDemoAction.ToggleEnabled,
            DesignSystemDemoTags.CustomColors to DesignSystemDemoAction.ToggleCustomTokens,
            DesignSystemDemoTags.Override to DesignSystemDemoAction.ToggleOverride,
            DesignSystemDemoTags.Reset to DesignSystemDemoAction.Reset,
            DesignSystemDemoTags.Back to DesignSystemDemoAction.BackClick,
        )
        choices.forEach { (tag, action) ->
            onNodeWithTag(tag).performScrollTo().performClick()
            runOnIdle { assertEquals(action, actions.last()) }
        }
    }

    /**
     * 卡片自身没有点击语义，其内部按钮与计数按钮独立发出点击动作。
     */
    @Test
    fun cardContainerIsNotClickableAndInnerButtonWorks() = runComposeUiTest {
        val actions = mutableListOf<DesignSystemDemoAction>()
        setContent {
            DesignSystemDemoContent(
                state = DesignSystemDemoUiState(),
                onAction = actions::add
            )
        }
        onNodeWithTag(DesignSystemDemoTags.Card).assertHasNoClickAction()
        onNodeWithTag(DesignSystemDemoTags.CardButton).performScrollTo().assertHasClickAction()
            .performClick()
        onNodeWithTag(DesignSystemDemoTags.CounterButton).performScrollTo().performClick()
        runOnIdle {
            assertEquals(
                listOf<DesignSystemDemoAction>(
                    DesignSystemDemoAction.ButtonClick,
                    DesignSystemDemoAction.ButtonClick
                ), actions
            )
        }
    }

    /**
     * 禁用示例始终不可用；关闭全局展示开关不影响控制区的重置按钮。
     */
    @Test
    fun disabledShowcaseKeepsControlsOperable() = runComposeUiTest {
        val actions = mutableListOf<DesignSystemDemoAction>()
        setContent {
            DesignSystemDemoContent(
                state = DesignSystemDemoUiState(enabled = false),
                onAction = actions::add,
            )
        }
        onNodeWithTag("design-system-demo-native-button").assertIsNotEnabled()
        onNodeWithTag(DesignSystemDemoTags.DisabledButton).assertIsNotEnabled()
        onNodeWithTag(DesignSystemDemoTags.CounterButton).assertIsNotEnabled()
        onNodeWithTag(DesignSystemDemoTags.CardButton).assertIsNotEnabled()
        onNodeWithTag(DesignSystemDemoTags.Reset).performScrollTo().performClick()
        runOnIdle {
            assertEquals(
                listOf<DesignSystemDemoAction>(DesignSystemDemoAction.Reset),
                actions
            )
        }
    }
}