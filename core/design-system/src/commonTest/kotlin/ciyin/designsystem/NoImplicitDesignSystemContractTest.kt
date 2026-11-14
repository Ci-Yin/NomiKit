package ciyin.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.card.NomiCard
import ciyin.designsystem.component.surface.NomiSurface
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * 验证未显式提供 [ciyin.designsystem.runtime.NomiDesignSystem] 时主题入口与四组件明确失败。
 *
 * 覆盖无隐式默认契约：应用根、测试与 Preview 必须显式提供实现，缺省装配问题不能被静默回退掩盖。
 */
@OptIn(ExperimentalTestApi::class)
class NoImplicitDesignSystemContractTest {
    /**
     * 验证裸 NomiTheme 在未提供设计系统时以带指引的 IllegalStateException 失败。
     */
    @Test
    fun bareThemeFailsWithGuidance() {
        assertMissingDesignSystemFails {
            NomiTheme(content = {})
        }
    }

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
