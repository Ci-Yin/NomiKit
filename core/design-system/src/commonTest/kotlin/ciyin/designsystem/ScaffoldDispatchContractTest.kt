package ciyin.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.scaffold.NomiScaffold
import ciyin.designsystem.component.scaffold.NomiScaffoldColors
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 页面脚手架只派发明确参数，不将投影令牌反灌为原生默认。
 */
@OptIn(ExperimentalTestApi::class)
class ScaffoldDispatchContractTest {
    /**
     * 默认调用保留 Unspecified 颜色，并按顶栏、主体、底栏组合三个槽位。
     */
    @Test
    fun defaultsRemainSparseAndSlotsAreComposed() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem, darkMode = DarkMode.Light) {
                NomiScaffold(
                    topBar = { NomiText("top") },
                    bottomBar = { NomiText("bottom") },
                ) {
                    NomiText("body")
                }
            }
        }
        runOnIdle {
            assertEquals(
                listOf(RecordedScaffold(colors = NomiScaffoldColors())),
                designSystem.scaffolds,
            )
            assertEquals(setOf("top", "body", "bottom"), designSystem.texts.keys)
        }
    }

    /**
     * 显式颜色逐字段原样透传，Transparent 仍是有效覆盖而不是未指定。
     */
    @Test
    fun explicitColorsArePassedThroughUnchanged() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val colors = NomiScaffoldColors(
            containerColor = Color(0xFF102030),
            contentColor = Color.Transparent,
        )
        setContent {
            NomiTheme(designSystem = designSystem, darkMode = DarkMode.Light) {
                NomiScaffold(colors = colors) {
                    NomiText("body")
                }
            }
        }
        runOnIdle {
            assertEquals(listOf(RecordedScaffold(colors = colors)), designSystem.scaffolds)
        }
    }
}
