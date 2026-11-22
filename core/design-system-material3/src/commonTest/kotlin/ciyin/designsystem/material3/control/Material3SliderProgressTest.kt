package ciyin.designsystem.material3

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.control.NomiSlider
import ciyin.designsystem.component.feedback.NomiProgressIndicator
import ciyin.designsystem.component.feedback.NomiProgressIndicatorType
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test

/**
 * Material 3 滑块与进度适配：直接使用原生组件，不重绘轨道或拇指。
 */
@OptIn(ExperimentalTestApi::class)
class Material3SliderProgressTest {
    /**
     * 滑块、线性确定进度与环形不确定进度都能渲染并保留调用方修饰符。
     */
    @Test
    fun sliderAndProgressRenderWithNativeComponents() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
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
