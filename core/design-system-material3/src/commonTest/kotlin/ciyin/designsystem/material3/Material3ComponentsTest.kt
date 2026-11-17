package ciyin.designsystem.material3

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.NomiBorder
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.button.NomiButtonColors
import ciyin.designsystem.component.button.NomiButtonDefaults
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.component.card.NomiCard
import ciyin.designsystem.component.surface.NomiSurface
import ciyin.designsystem.component.surface.NomiSurfaceColors
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.runtime.LocalNomiContentColor
import ciyin.designsystem.runtime.LocalNomiTextStyle
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import androidx.compose.material3.Button as NativeButton
import androidx.compose.material3.Text as NativeText

/**
 * Material 适配的原生状态、slot 样式及中立文字路径回归。
 */
@OptIn(ExperimentalTestApi::class)
class Material3ComponentsTest {
    /**
     * 每种变体、尺寸与启用态均使用实际 Material 内容色和中立度量。
     *
     * @return 本回归的跨平台测试完成结果；Web 由测试运行器等待其 Promise，JVM/Native 同步完成。
     */
    @Test
    fun buttonVariantsKeepStateAndSizeInBothSlotProtocols() = runComposeUiTest {
        var variant by mutableStateOf(NomiButtonVariant.Primary)
        var size by mutableStateOf(NomiControlSize.Medium)
        var enabled by mutableStateOf(true)
        var nativeColor = Color.Unspecified
        var neutralColor = Color.Unspecified
        var nativeStyle = TextStyle.Default
        var neutralStyle = TextStyle.Default
        var expectedStyle = TextStyle.Default
        var clicks = 0
        val colors = NomiButtonColors(
            containerColor = Color.Blue,
            contentColor = Color.Yellow,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.Cyan,
        )
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                expectedStyle = NomiButtonDefaults.metrics(size).textStyle
                NomiButton(
                    onClick = { clicks++ },
                    modifier = Modifier.testTag("button"),
                    enabled = enabled,
                    variant = variant,
                    size = size,
                    colors = colors,
                ) {
                    nativeColor = LocalContentColor.current
                    neutralColor = LocalNomiContentColor.current
                    nativeStyle = LocalTextStyle.current
                    neutralStyle = LocalNomiTextStyle.current
                    NomiText(text = "内容", modifier = Modifier.testTag("content"))
                }
            }
        }
        for (currentVariant in NomiButtonVariant.entries) {
            for (currentSize in NomiControlSize.entries) {
                for (currentEnabled in listOf(true, false)) {
                    runOnIdle {
                        variant = currentVariant
                        size = currentSize
                        enabled = currentEnabled
                    }
                    waitForIdle()
                    val expectedColor =
                        if (currentEnabled) colors.contentColor else colors.disabledContentColor
                    assertEquals(expectedColor, nativeColor)
                    assertEquals(nativeColor, neutralColor)
                    assertEquals(expectedStyle.fontSize, nativeStyle.fontSize)
                    assertEquals(expectedStyle.fontSize, neutralStyle.fontSize)
                    assertEquals(
                        expectedColor,
                        onNodeWithTag("content", useUnmergedTree = true).renderedTextStyle().color
                    )
                    val previousClicks = clicks
                    if (!currentEnabled) onNodeWithTag("button").assertIsNotEnabled()
                    onNodeWithTag("button").performClick()
                    assertEquals(previousClicks + if (currentEnabled) 1 else 0, clicks)
                }
            }
        }
    }

    /**
     * 原生 Material 按钮内部的中立文字必须读取真实禁用内容色，而不是根主题正文色。
     *
     * @return 本回归的跨平台测试完成结果；Web 由测试运行器等待其 Promise，JVM/Native 同步完成。
     */
    @Test
    fun textReadsActualNativeContentColorAndExplicitOverrides() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                NativeButton(
                    onClick = {},
                    enabled = false,
                    colors = ButtonDefaults.buttonColors(
                        disabledContentColor = Color.Magenta,
                    ),
                ) {
                    NomiText(text = "继承", modifier = Modifier.testTag("inherited"))
                    NomiText(
                        text = "样式",
                        modifier = Modifier.testTag("styled"),
                        style = TextStyle(color = Color.Green)
                    )
                    NomiText(
                        text = "显式",
                        modifier = Modifier.testTag("explicit"),
                        style = TextStyle(color = Color.Green),
                        color = Color.Red,
                    )
                }
            }
        }
        assertEquals(
            Color.Magenta,
            onNodeWithTag("inherited", useUnmergedTree = true).renderedTextStyle().color
        )
        assertEquals(
            Color.Green,
            onNodeWithTag("styled", useUnmergedTree = true).renderedTextStyle().color
        )
        assertEquals(
            Color.Red,
            onNodeWithTag("explicit", useUnmergedTree = true).renderedTextStyle().color
        )
    }

    /**
     * 显式部分样式仅改指定字段，不能丢失显式 size 所选的字号、行高和字距。
     *
     * @return 本回归的跨平台测试完成结果；Web 由测试运行器等待其 Promise，JVM/Native 同步完成。
     */
    @Test
    fun partialButtonTextStyleKeepsSizeMetrics() = runComposeUiTest {
        var expectedStyle = TextStyle.Default
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                expectedStyle = NomiButtonDefaults.metrics(NomiControlSize.Small).textStyle
                NomiButton(
                    onClick = {},
                    size = NomiControlSize.Small,
                    textStyle = TextStyle(fontWeight = FontWeight.Bold),
                ) {
                    NomiText(text = "部分样式", modifier = Modifier.testTag("partial-size"))
                }
            }
        }
        val actual = onNodeWithTag("partial-size", useUnmergedTree = true).renderedTextStyle()
        assertEquals(expectedStyle.fontSize, actual.fontSize)
        assertEquals(expectedStyle.lineHeight, actual.lineHeight)
        assertEquals(expectedStyle.letterSpacing, actual.letterSpacing)
        assertEquals(FontWeight.Bold, actual.fontWeight)
    }

    /**
     * 当前状态的显式颜色优先于文字样式色，样式色再优先原生色，原生和中立文字必须完全一致。
     *
     * @return 本回归的跨平台测试完成结果；Web 由测试运行器等待其 Promise，JVM/Native 同步完成。
     */
    @Test
    fun stateColorsAndTextStyleColorHaveExplicitPriorityInBothStates() = runComposeUiTest {
        var enabled by mutableStateOf(true)
        var colors by mutableStateOf(NomiButtonColors())
        var style by mutableStateOf<TextStyle?>(null)
        var slotColor = Color.Unspecified
        var defaultEnabled = Color.Unspecified
        var defaultDisabled = Color.Unspecified
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                val defaults = ButtonDefaults.buttonColors()
                defaultEnabled = defaults.contentColor
                defaultDisabled = defaults.disabledContentColor
                NomiButton(
                    onClick = {},
                    enabled = enabled,
                    colors = colors,
                    textStyle = style,
                ) {
                    slotColor = LocalContentColor.current
                    NativeText(text = "原生文字", modifier = Modifier.testTag("native-priority"))
                    NomiText(text = "中立文字", modifier = Modifier.testTag("app-priority"))
                }
            }
        }
        for (currentEnabled in listOf(true, false)) {
            for (stateOverride in listOf(Color.Unspecified, Color.Yellow, Color.Transparent)) {
                for (styleOverride in listOf(Color.Unspecified, Color.Red)) {
                    runOnIdle {
                        enabled = currentEnabled
                        colors = if (currentEnabled) {
                            NomiButtonColors(
                                contentColor = stateOverride,
                                disabledContentColor = Color.Cyan
                            )
                        } else {
                            NomiButtonColors(
                                contentColor = Color.Cyan,
                                disabledContentColor = stateOverride
                            )
                        }
                        style =
                            if (styleOverride == Color.Unspecified) null else TextStyle(color = styleOverride)
                    }
                    waitForIdle()
                    val expected = when {
                        stateOverride != Color.Unspecified -> stateOverride
                        styleOverride != Color.Unspecified -> styleOverride
                        currentEnabled -> defaultEnabled
                        else -> defaultDisabled
                    }
                    assertEquals(expected, slotColor)
                    assertEquals(
                        expected,
                        onNodeWithTag(
                            "native-priority",
                            useUnmergedTree = true
                        ).renderedTextStyle().color
                    )
                    assertEquals(
                        expected,
                        onNodeWithTag(
                            "app-priority",
                            useUnmergedTree = true
                        ).renderedTextStyle().color
                    )
                }
            }
        }
    }

    /**
     * Surface 和 Card 均通过 Material 表面传递精确内容色并接收任意圆角与描边。
     *
     * @return 本回归的跨平台测试完成结果；Web 由测试运行器等待其 Promise，JVM/Native 同步完成。
     */
    @Test
    fun surfaceAndCardKeepCustomContentColor() = runComposeUiTest {
        var surfaceNativeColor = Color.Unspecified
        var cardNativeColor = Color.Unspecified
        val shape = RoundedCornerShape(
            topStart = 3.dp,
            topEnd = 7.dp,
            bottomEnd = 11.dp,
            bottomStart = 17.dp
        )
        val colors = NomiSurfaceColors(containerColor = Color.Blue, contentColor = Color.Yellow)
        val border = NomiBorder.Stroke(BorderStroke(width = 2.dp, color = Color.Red))
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                Column {
                    NomiSurface(shape = shape, colors = colors, border = border) {
                        surfaceNativeColor = LocalContentColor.current
                        NomiText(text = "表面", modifier = Modifier.testTag("surface"))
                    }
                    NomiCard(shape = shape, colors = colors, border = border) {
                        cardNativeColor = LocalContentColor.current
                        NomiText(text = "卡片", modifier = Modifier.testTag("card"))
                    }
                }
            }
        }
        assertEquals(colors.contentColor, surfaceNativeColor)
        assertEquals(colors.contentColor, cardNativeColor)
        assertEquals(colors.contentColor, onNodeWithTag("surface").renderedTextStyle().color)
        assertEquals(colors.contentColor, onNodeWithTag("card").renderedTextStyle().color)
    }
}

/**
 * 读取文字实际布局使用的样式，而不是仅检查外层 CompositionLocal。
 *
 * @receiver 必须能提供恰好一项文本布局结果的语义节点。
 * @return 实际布局输入中的 TextStyle，包含最终颜色与排版。
 */
private fun SemanticsNodeInteraction.renderedTextStyle(): TextStyle {
    val results = mutableListOf<TextLayoutResult>()
    performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action -> action(results) }
    return results.single().layoutInput.style
}
