package ciyin.designsystem.nomi.control

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.click
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.control.NomiSlider
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.nomi.control.slider.NomiSliderStyle
import ciyin.designsystem.nomi.control.slider.rememberNomiSliderStyle
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiColorScheme
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Nomi 滑块实现：应用滑块外观、拖拽与禁用行为、RTL、刻度吸附与进度语义。
 */
@OptIn(ExperimentalTestApi::class)
class NomiSliderTest {
    /**
     * 向右拖拽后值增大，并夹紧在取值区间内。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun dragRightIncreasesValue() = runComposeUiTest {
        val value = mutableStateOf(0.2f)
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                NomiSlider(
                    value = value.value,
                    onValueChange = { value.value = it },
                    modifier = Modifier.width(240.dp).testTag("slider"),
                )
            }
        }
        onNodeWithTag("slider").performTouchInput { swipeRight() }
        runOnIdle {
            assertTrue(value.value > 0.2f, "期望拖拽后值增大，实际为 " + value.value)
            assertTrue(value.value <= 1f)
        }
    }

    /**
     * 禁用滑块不响应拖拽。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun disabledSliderIgnoresDrag() = runComposeUiTest {
        val value = mutableStateOf(0.2f)
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                NomiSlider(
                    value = value.value,
                    onValueChange = { value.value = it },
                    enabled = false,
                    modifier = Modifier.width(240.dp).testTag("slider"),
                )
            }
        }
        onNodeWithTag("slider").performTouchInput { swipeRight() }
        runOnIdle {
            assertEquals(0.2f, value.value)
        }
    }

    /**
     * 滑块发布当前值与取值区间，供读屏使用。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun sliderPublishesProgressRangeInfo() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                NomiSlider(
                    value = 0.5f,
                    onValueChange = {},
                    modifier = Modifier.width(240.dp).testTag("slider"),
                )
            }
        }
        val info = onNodeWithTag("slider").fetchSemanticsNode()
            .config[SemanticsProperties.ProgressBarRangeInfo]
        assertEquals(0.5f, info.current)
        assertEquals(0f, info.range.start)
        assertEquals(1f, info.range.endInclusive)
    }

    /**
     * 默认外观对齐应用滑块：16dp 轨道、30dp 整体高度。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun defaultSliderHeightMatchesAppSlider() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                NomiSlider(
                    value = 0.5f,
                    onValueChange = {},
                    modifier = Modifier.width(240.dp).testTag("slider"),
                )
            }
        }
        onNodeWithTag("slider").assertHeightIsEqualTo(30.dp)
    }

    /**
     * 尺寸与色板取自应用滑块的基线：16dp 轨道、15dp 拇指、38% 禁用透明度、透明刻度。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun styleMatchesAppSliderLook() = runComposeUiTest {
        var enabledStyle: NomiSliderStyle? = null
        var disabledStyle: NomiSliderStyle? = null
        var scheme: NomiColorScheme? = null
        var strokeWidth: Dp = 0.dp
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                scheme = NomiTheme.colorScheme
                strokeWidth = NomiTheme.sizes.strokes.medium
                enabledStyle = rememberNomiSliderStyle(enabled = true)
                disabledStyle = rememberNomiSliderStyle(enabled = false)
            }
        }
        runOnIdle {
            val colors = requireNotNull(scheme)
            val enabled = requireNotNull(enabledStyle)
            assertEquals(16.dp, enabled.trackHeight)
            assertEquals(15.dp, enabled.thumbSize)
            assertEquals(strokeWidth, enabled.thumbBorderWidth)
            assertEquals(colors.primary, (enabled.activeTrackBrush as SolidColor).value)
            assertEquals(colors.surfaceHigher, (enabled.inactiveTrackBrush as SolidColor).value)
            assertEquals(colors.onPrimary, (enabled.thumbBrush as SolidColor).value)
            assertEquals(colors.primary, (enabled.thumbBorderBrush as SolidColor).value)
            assertEquals(0f, (enabled.activeTickBrush as SolidColor).value.alpha)
            assertEquals(0f, (enabled.inactiveTickBrush as SolidColor).value.alpha)

            val disabled = requireNotNull(disabledStyle)
            assertEquals(colors.primary.copy(alpha = 0.38f), (disabled.activeTrackBrush as SolidColor).value)
            assertEquals(colors.surfaceHigher.copy(alpha = 0.38f), (disabled.inactiveTrackBrush as SolidColor).value)
            assertEquals(colors.onPrimary.copy(alpha = 0.38f), (disabled.thumbBrush as SolidColor).value)
            assertEquals(colors.primary.copy(alpha = 0.38f), (disabled.thumbBorderBrush as SolidColor).value)
        }
    }

    /**
     * RTL 布局下向右拖拽使值减小。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun rtlDragDecreasesValue() = runComposeUiTest {
        val value = mutableStateOf(0.5f)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                    NomiSlider(
                        value = value.value,
                        onValueChange = { value.value = it },
                        modifier = Modifier.width(240.dp).testTag("slider"),
                    )
                }
            }
        }
        onNodeWithTag("slider").performTouchInput { swipeRight() }
        runOnIdle {
            assertTrue(value.value < 0.5f, "期望 RTL 下右滑使值减小，实际为 " + value.value)
        }
    }

    /**
     * 点按滑块吸附到最近刻度：steps = 2 时四个刻度为 0、1/3、2/3、1，点 30% 处落在 1/3。
     *
     * 拖拽松手沿用原实现，只回调结束事件、不做吸附。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun tapSnapsToNearestStep() = runComposeUiTest {
        val value = mutableStateOf(0f)
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                NomiSlider(
                    value = value.value,
                    onValueChange = { value.value = it },
                    steps = 2,
                    modifier = Modifier.width(240.dp).testTag("slider"),
                )
            }
        }
        onNodeWithTag("slider").performTouchInput {
            click(Offset(width * 0.3f, centerY))
        }
        waitForIdle()
        runOnIdle {
            assertEquals(1f / 3f, value.value, 0.01f)
        }
    }
}
