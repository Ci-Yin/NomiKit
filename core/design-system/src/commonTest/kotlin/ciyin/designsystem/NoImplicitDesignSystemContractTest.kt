package ciyin.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.card.NomiCard
import ciyin.designsystem.component.control.NomiCheckbox
import ciyin.designsystem.component.control.NomiRadioButton
import ciyin.designsystem.component.control.NomiSwitch
import ciyin.designsystem.component.text.NomiTextField
import ciyin.designsystem.component.divider.NomiDivider
import ciyin.designsystem.component.feedback.NomiTooltip
import ciyin.designsystem.component.icon.NomiIcon
import ciyin.designsystem.component.list.NomiListItem
import ciyin.designsystem.component.surface.NomiSurface
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.icon.Home
import ciyin.designsystem.icon.IconPack
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * 验证未显式提供 [ciyin.designsystem.runtime.NomiDesignSystem] 时各组件明确失败。
 *
 * 覆盖无隐式默认契约：应用根、测试与 Preview 必须显式提供实现，缺省装配问题不能被静默回退掩盖。
 */
@OptIn(ExperimentalTestApi::class)
class NoImplicitDesignSystemContractTest {
    /**
     * 验证未提供设计系统时 NomiText 在组合阶段明确失败。
     */
    @Test
    fun bareTextFailsWithGuidance() {
        assertMissingDesignSystemFails {
            NomiText(text = "missing design system")
        }
    }

    /**
     * 验证未提供设计系统时 NomiButton 在组合阶段明确失败。
     */
    @Test
    fun bareButtonFailsWithGuidance() {
        assertMissingDesignSystemFails {
            NomiButton(onClick = {}) {
                NomiText(text = "missing design system")
            }
        }
    }

    /**
     * 验证未提供设计系统时 NomiSurface 在组合阶段明确失败。
     */
    @Test
    fun bareSurfaceFailsWithGuidance() {
        assertMissingDesignSystemFails {
            NomiSurface {
                NomiText(text = "missing design system")
            }
        }
    }

    /**
     * 验证未提供设计系统时 NomiCard 在组合阶段明确失败。
     */
    @Test
    fun bareCardFailsWithGuidance() {
        assertMissingDesignSystemFails {
            NomiCard {
                NomiText(text = "missing design system")
            }
        }
    }

    /**
     * 验证未提供设计系统时 NomiListItem 在组合阶段明确失败。
     */
    @Test
    fun bareListItemFailsWithGuidance() {
        assertMissingDesignSystemFails {
            NomiListItem(title = { NomiText(text = "missing design system") })
        }
    }

    /**
     * 验证未提供设计系统时 NomiIcon 在组合阶段明确失败。
     */
    @Test
    fun bareIconFailsWithGuidance() {
        assertMissingDesignSystemFails {
            NomiIcon(imageVector = IconPack.Home, contentDescription = null)
        }
    }

    /**
     * 验证未提供设计系统时 NomiDivider 在组合阶段明确失败。
     */
    @Test
    fun bareDividerFailsWithGuidance() {
        assertMissingDesignSystemFails {
            NomiDivider()
        }
    }

    /**
     * 验证未提供设计系统时 NomiSwitch 在组合阶段明确失败。
     */
    @Test
    fun bareSwitchFailsWithGuidance() {
        assertMissingDesignSystemFails {
            NomiSwitch(checked = false, onCheckedChange = {})
        }
    }

    /**
     * 验证未提供设计系统时 NomiCheckbox 在组合阶段明确失败。
     */
    @Test
    fun bareCheckboxFailsWithGuidance() {
        assertMissingDesignSystemFails {
            NomiCheckbox(checked = false, onCheckedChange = {})
        }
    }

    /**
     * 验证未提供设计系统时 NomiRadioButton 在组合阶段明确失败。
     */
    @Test
    fun bareRadioButtonFailsWithGuidance() {
        assertMissingDesignSystemFails {
            NomiRadioButton(selected = false, onClick = {})
        }
    }

    /**
     * 验证未提供设计系统时 NomiTextField 在组合阶段明确失败。
     */
    @Test
    fun bareTextFieldFailsWithGuidance() {
        assertMissingDesignSystemFails {
            NomiTextField(value = "", onValueChange = {})
        }
    }

    /**
     * 验证未提供设计系统时 NomiTooltip 在组合阶段明确失败。
     */
    @Test
    fun bareTooltipFailsWithGuidance() {
        assertMissingDesignSystemFails {
            NomiTooltip(tooltip = { NomiText(text = "missing design system") }) {
                NomiText(text = "missing design system")
            }
        }
    }
}

/**
 * 在没有任何设计系统提供者的组合环境中执行内容，断言其以缺少实现的异常失败。
 *
 * @param content 未显式提供设计系统时组合的内容。
 */
@OptIn(ExperimentalTestApi::class)
private fun assertMissingDesignSystemFails(content: @Composable () -> Unit) {
    val failure = assertFailsWith<IllegalStateException> {
        runComposeUiTest {
            setContent(content)
            waitForIdle()
        }
    }
    assertTrue(
        actual = failure.message?.contains("未提供 NomiDesignSystem") == true,
        message = "缺少设计系统时须给出显式提供指引，实际信息：" + failure.message,
    )
}