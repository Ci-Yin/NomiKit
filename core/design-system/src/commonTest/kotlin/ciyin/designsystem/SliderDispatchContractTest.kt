package ciyin.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.control.NomiSlider
import ciyin.designsystem.component.control.NomiSliderSpec
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 滑块入口只派发显式参数，不预填取值范围之外的值。
 */
@OptIn(ExperimentalTestApi::class)
class SliderDispatchContractTest {
    /**
     * 只传值与回调时，其余参数保持默认且不提供结束回调与专属参数。
     */
    @Test
    fun sliderDefaultsRemainSparse() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiSlider(value = 0.5f, onValueChange = {})
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    RecordedSlider(
                        value = 0.5f,
                        enabled = true,
                        valueRange = 0f..1f,
                        steps = 0,
                        hasOnValueChangeFinished = false,
                        hasModifier = false,
                    )
                ),
                designSystem.sliders,
            )
        }
    }

    /**
     * 显式区间、刻度、禁用、结束回调、修饰符与专属参数原样透传。
     */
    @Test
    fun sliderExplicitParametersAreDispatchedUnchanged() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val spec = RecordingSliderSpec(marker = "slider")
        val range = -10f..10f
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiSlider(
                    value = 2.5f,
                    onValueChange = {},
                    modifier = Modifier.testTag("slider"),
                    enabled = false,
                    valueRange = range,
                    steps = 4,
                    onValueChangeFinished = {},
                    nativeSpec = spec,
                )
            }
        }
        runOnIdle {
            val recorded = designSystem.sliders.single()
            assertEquals(2.5f, recorded.value)
            assertEquals(false, recorded.enabled)
            assertEquals(range, recorded.valueRange)
            assertEquals(4, recorded.steps)
            assertEquals(true, recorded.hasOnValueChangeFinished)
            assertEquals(true, recorded.hasModifier)
            assertEquals(spec, recorded.nativeSpec)
        }
    }

    /**
     * 调用方不传专属参数时保持 null，不补空的 spec。
     */
    @Test
    fun sliderNativeSpecStaysNullWhenOmitted() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiSlider(value = 0f, onValueChange = {})
            }
        }
        runOnIdle {
            assertNull(designSystem.sliders.single().nativeSpec)
        }
    }
}

/**
 * 测试用滑块专属参数，模拟 adapter 模块定义的具体 spec。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class RecordingSliderSpec(val marker: String) : NomiSliderSpec
