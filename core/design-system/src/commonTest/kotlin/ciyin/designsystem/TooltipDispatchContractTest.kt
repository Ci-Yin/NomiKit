package ciyin.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.feedback.NomiTooltip
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 提示浮层入口只组合调用方内容，不为浮层补状态、延时或位置。
 */
@OptIn(ExperimentalTestApi::class)
class TooltipDispatchContractTest {
    /**
     * 提示槽与触发内容都记录为已提供，内容在组合阶段执行。
     */
    @Test
    fun tooltipAndContentSlotsAreDispatched() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiTooltip(tooltip = { NomiText("提示") }) {
                    NomiText("触发")
                }
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    RecordedTooltip(hasTooltip = true, hasContent = true, hasModifier = false)
                ),
                designSystem.tooltips,
            )
            assertEquals(setOf("触发"), designSystem.texts.keys)
        }
    }

    /**
     * 显式修饰符原样透传。
     */
    @Test
    fun tooltipModifierIsDispatchedUnchanged() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiTooltip(
                    tooltip = { NomiText("提示") },
                    modifier = Modifier.testTag("tooltip"),
                ) {
                    NomiText("触发")
                }
            }
        }
        runOnIdle {
            assertEquals(true, designSystem.tooltips.single().hasModifier)
        }
    }
}
