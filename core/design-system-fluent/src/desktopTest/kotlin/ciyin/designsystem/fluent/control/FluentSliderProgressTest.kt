package ciyin.designsystem.fluent.control

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.control.NomiSlider
import ciyin.designsystem.component.feedback.NomiProgressIndicator
import ciyin.designsystem.component.feedback.NomiProgressIndicatorType
import ciyin.designsystem.fluent.Fluent
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test

/**
 * Fluent 滑块与进度适配：直接使用原生 Slider、ProgressBar 与 ProgressRing。
 */
@OptIn(ExperimentalTestApi::class)
class FluentSliderProgressTest {
    /**
     * 滑块、线性确定进度与环形不确定进度都能渲染并保留调用方修饰符。
     */
    @Test
    fun sliderAndProgressRenderWithNativeComponents() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Fluent, darkMode = DarkMode.Light) {
                Column {
                    NomiSlider(
                        value = 0.3f,
                        onValueChange = {},
                        modifier = Modifier.testTag("slider"),
                    )
                    NomiProgressIndicator(
                        progress = 0.4f,
                        modifier = Modifier.testTag("linear"),
                    )
                    NomiProgressIndicator(
                        progress = null,
                        modifier = Modifier.testTag("circular"),
                        type = NomiProgressIndicatorType.Circular,
                    )
                }
            }
        }
        onNodeWithTag("slider").assertExists()
        onNodeWithTag("linear").assertExists()
        onNodeWithTag("circular").assertExists()
    }
}
