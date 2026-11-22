package ciyin.designsystem.nomi.feedback

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.feedback.NomiProgressIndicator
import ciyin.designsystem.component.feedback.NomiProgressIndicatorType
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Nomi 进度实现：确定态发布进度值，不确定态只发布进度条角色。
 */
@OptIn(ExperimentalTestApi::class)
class NomiProgressIndicatorTest {
    /**
     * 确定的线性进度发布当前值。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun determinateLinearProgressPublishesValue() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                NomiProgressIndicator(
                    progress = 0.4f,
                    modifier = Modifier.testTag("linear"),
                )
            }
        }
        val info = onNodeWithTag("linear").fetchSemanticsNode()
            .config[SemanticsProperties.ProgressBarRangeInfo]
        assertEquals(0.4f, info.current)
    }

    /**
     * 不确定进度标记为 Indeterminate，且环形形态同样存在。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun indeterminateCircularProgressHasNoValue() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                NomiProgressIndicator(
                    progress = null,
                    modifier = Modifier.testTag("circular"),
                    type = NomiProgressIndicatorType.Circular,
                )
            }
        }
        val info = onNodeWithTag("circular").fetchSemanticsNode()
            .config[SemanticsProperties.ProgressBarRangeInfo]
        assertEquals(ProgressBarRangeInfo.Indeterminate, info)
    }

    /**
     * 线性进度使用与滑块轨道相同的 16dp 胶囊高度。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun linearProgressMatchesSliderTrackHeight() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                NomiProgressIndicator(
                    progress = 0.4f,
                    modifier = Modifier.testTag("linear"),
                )
            }
        }
        onNodeWithTag("linear").assertHeightIsEqualTo(16.dp)
    }

    /**
     * 环形进度保持 32dp 默认尺寸。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun circularProgressKeepsDefaultSize() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                NomiProgressIndicator(
                    progress = 0.4f,
                    modifier = Modifier.testTag("circular"),
                    type = NomiProgressIndicatorType.Circular,
                )
            }
        }
        onNodeWithTag("circular").assertWidthIsEqualTo(32.dp)
        onNodeWithTag("circular").assertHeightIsEqualTo(32.dp)
    }
}
