package ciyin.ui.foundation

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.input.pointer.PointerKeyboardModifiers
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import kotlin.test.Test

/**
 * 验证窗口尺寸分级从 [LocalWindowInfo] 读取容器尺寸，并随容器尺寸变化重组。
 */
@OptIn(ExperimentalTestApi::class)
class WindowSizeUiTest {
    /**
     * 容器尺寸跨过窗口宽度断点时，读取窗口宽度的内容必须重组并输出新的分级。
     */
    @Test
    fun windowWidthRecomposesWhenContainerSizeChanges() = runComposeUiTest {
        val containerSize = mutableStateOf(IntSize(400, 800))
        setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(density = 1f),
                LocalWindowInfo provides FakeWindowInfo { containerSize.value },
            ) {
                BasicText(text = currentWindowWidth().name)
            }
        }
        onNodeWithText(WindowSize.Compat.name).assertExists()

        containerSize.value = IntSize(700, 800)
        waitForIdle()
        onNodeWithText(WindowSize.Medium.name).assertExists()

        containerSize.value = IntSize(1200, 800)
        waitForIdle()
        onNodeWithText(WindowSize.Expanded.name).assertExists()
    }
}

/**
 * 测试用窗口信息：容器尺寸来自外部状态，用来模拟桌面窗口缩放。
 *
 * @property containerSizeProvider 提供当前容器尺寸（像素）的回调。
 */
private class FakeWindowInfo(
    private val containerSizeProvider: () -> IntSize,
) : WindowInfo {
    override val isWindowFocused: Boolean = true

    override val keyboardModifiers: PointerKeyboardModifiers = PointerKeyboardModifiers()

    override val containerSize: IntSize
        get() = containerSizeProvider()
}
