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
     * 尺寸控件与显式覆盖矩阵对所有系统常驻；不支持的实现由适配器降级，页面不再隐藏或改写。
     */
    @Test
    fun sizeMatrixStaysVisibleOnEveryDesignSystem() = runComposeUiTest {
        setContent {
            DesignSystemDemoContent(
                state = DesignSystemDemoUiState(designSystemId = NomiDesignSystem.Fluent.id),
                onAction = {},
            )
        }
        onNodeWithTag("design-system-demo-size-default").assertExists()
        NomiControlSize.entries.forEach { size ->
            onNodeWithTag("design-system-demo-size-$size").assertExists()
        }
        NomiButtonVariant.entries.forEach { variant ->
            NomiControlSize.entries.forEach { size ->
                onNodeWithTag("design-system-demo-explicit-button-$variant-$size").assertExists()
            }
        }
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
     * 控件分区暴露图标、分割线、三个选择控件、输入框与提示浮层，且开关可交互。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun controlsSectionExposesNativeWidgets() = runComposeUiTest {
        setContent { DesignSystemDemoContent(state = DesignSystemDemoUiState(), onAction = {}) }
        onNodeWithTag(DesignSystemDemoTags.ControlsSection).performScrollTo().assertExists()
        onNodeWithTag(DesignSystemDemoTags.IconSample).performScrollTo().assertExists()
        onNodeWithTag(DesignSystemDemoTags.DividerSample).assertExists()
        onNodeWithTag(DesignSystemDemoTags.Switch)
            .performScrollTo()
            .assertHasClickAction()
            .performClick()
        onNodeWithTag(DesignSystemDemoTags.Checkbox).assertHasClickAction()
        onNodeWithTag(DesignSystemDemoTags.Radio).assertHasClickAction()
        onNodeWithTag(DesignSystemDemoTags.TextField).performScrollTo().assertExists()
        onNodeWithTag(DesignSystemDemoTags.TextFieldError).assertExists()
        onNodeWithTag(DesignSystemDemoTags.Tooltip, useUnmergedTree = true)
            .performScrollTo()
            .assertExists()
    }

    /**
     * 控件分区暴露滑块、线性确定进度与环形不确定进度。
     */
    @Test
    fun controlsSectionExposesSliderAndProgress() = runComposeUiTest {
        setContent { DesignSystemDemoContent(state = DesignSystemDemoUiState(), onAction = {}) }
        onNodeWithTag(DesignSystemDemoTags.Slider).performScrollTo().assertExists()
        onNodeWithTag(DesignSystemDemoTags.ProgressLinear).performScrollTo().assertExists()
        onNodeWithTag(DesignSystemDemoTags.ProgressCircular).assertExists()
    }

    /**
     * 按钮分区暴露开关按钮、图标开关与下拉按钮；菜单只在展开后出现。
     */
    @Test
    fun buttonSectionExposesToggleAndDropDownEntries() = runComposeUiTest {
        setContent { DesignSystemDemoContent(state = DesignSystemDemoUiState(), onAction = {}) }
        onNodeWithTag(DesignSystemDemoTags.ToggleButton)
            .performScrollTo()
            .assertHasClickAction()
        onNodeWithTag(DesignSystemDemoTags.ToggleIconButton).assertHasClickAction()
        onNodeWithTag(DesignSystemDemoTags.DropDownButton)
            .performScrollTo()
            .assertHasClickAction()
        onNodeWithTag(DesignSystemDemoTags.MenuItem).assertDoesNotExist()
        onNodeWithTag(DesignSystemDemoTags.DropDownButton).performClick()
        onNodeWithTag(DesignSystemDemoTags.MenuItem).assertExists()
    }

    /**
     * 控件分区暴露组合框；收起时不渲染菜单项，展开后可看到选项。
     */
    @Test
    fun controlsSectionExposesComboBox() = runComposeUiTest {
        setContent { DesignSystemDemoContent(state = DesignSystemDemoUiState(), onAction = {}) }
        onNodeWithTag(DesignSystemDemoTags.ComboBox)
            .performScrollTo()
            .assertExists()
        onNodeWithTag(DesignSystemDemoTags.ComboBoxOption).assertDoesNotExist()
        onNodeWithTag(DesignSystemDemoTags.ComboBox).performClick()
        onNodeWithTag(DesignSystemDemoTags.ComboBoxOption).assertExists()
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
            DesignSystemDemoTags.Override to DesignSystemDemoAction.ToggleOverride,
            DesignSystemDemoTags.Reset to DesignSystemDemoAction.Reset,
        )
        choices.forEach { (tag, action) ->
            onNodeWithTag(tag).performScrollTo().performClick()
            runOnIdle { assertEquals(action, actions.last()) }
        }
        onNodeWithTag(DesignSystemDemoTags.Back).performClick()
        runOnIdle { assertEquals(DesignSystemDemoAction.BackClick, actions.last()) }
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