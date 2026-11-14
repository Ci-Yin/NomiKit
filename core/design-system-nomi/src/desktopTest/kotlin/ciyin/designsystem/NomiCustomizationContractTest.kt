package ciyin.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ciyin.designsystem.component.NomiBorder
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.button.NomiButtonColors
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.component.card.NomiCard
import ciyin.designsystem.component.surface.NomiSurface
import ciyin.designsystem.component.surface.NomiSurfaceColors
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.defaultLightNomiColorScheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Nomi 自有默认和显式定制仍由真实 Foundation 绘制消费，不只验证派发参数。
 */
@OptIn(ExperimentalTestApi::class)
class NomiCustomizationContractTest {
    /**
     * 可空默认参数在 Nomi 内部仍等价于自身的 Primary/Medium，而不是改变旧风格。
     */
    @Test
    fun omittedVariantAndSizeRetainNomiPrimaryMediumDefaults() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                Column {
                    NomiButton(onClick = {}, modifier = Modifier.testTag("default")) {
                        NomiText(text = "Same", modifier = Modifier.testTag("default-text"))
                    }
                    NomiButton(
                        onClick = {},
                        modifier = Modifier.testTag("explicit"),
                        variant = NomiButtonVariant.Primary,
                        size = NomiControlSize.Medium,
                    ) {
                        NomiText(text = "Same", modifier = Modifier.testTag("explicit-text"))
                    }
                }
            }
        }
        val defaultBounds = onNodeWithTag("default").getUnclippedBoundsInRoot()
        val explicitBounds = onNodeWithTag("explicit").getUnclippedBoundsInRoot()
        assertEquals(
            explicitBounds.right - explicitBounds.left,
            defaultBounds.right - defaultBounds.left
        )
        assertEquals(
            explicitBounds.bottom - explicitBounds.top,
            defaultBounds.bottom - defaultBounds.top
        )
        val defaultPixels = onNodeWithTag("default").captureToImage().toPixelMap()
        val explicitPixels = onNodeWithTag("explicit").captureToImage().toPixelMap()
        assertEquals(
            explicitPixels[explicitPixels.width / 2, 3],
            defaultPixels[defaultPixels.width / 2, 3]
        )
        assertEquals(
            onNodeWithTag("explicit-text", useUnmergedTree = true).renderedStyle(),
            onNodeWithTag("default-text", useUnmergedTree = true).renderedStyle(),
        )
        assertEquals(
            defaultLightNomiColorScheme().primary,
            defaultPixels[defaultPixels.width / 2, 3]
        )
    }

    /**
     * 部分按钮颜色仅覆盖指定状态，其余仍使用 Nomi 该状态默认值；显式排版进入实际文本布局。
     */
    @Test
    fun partialColorsAndExplicitTextStylePreserveUnspecifiedStateDefaults() = runComposeUiTest {
        var enabled by mutableStateOf(true)
        val colors = defaultLightNomiColorScheme()
        val style = TextStyle(fontSize = 25.sp, lineHeight = 31.sp)
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                NomiButton(
                    onClick = {},
                    modifier = Modifier.testTag("button"),
                    enabled = enabled,
                    colors = NomiButtonColors(contentColor = Color.Cyan),
                    textStyle = style,
                ) {
                    NomiText(text = "Color", modifier = Modifier.testTag("text"))
                }
            }
        }
        val text = onNodeWithTag("text", useUnmergedTree = true)
        assertEquals(Color.Cyan, text.renderedStyle().color)
        assertEquals(style.fontSize, text.renderedStyle().fontSize)
        assertEquals(style.lineHeight, text.renderedStyle().lineHeight)
        val enabledPixels = onNodeWithTag("button").captureToImage().toPixelMap()
        assertEquals(colors.primary, enabledPixels[enabledPixels.width / 2, 3])
        runOnIdle { enabled = false }
        assertEquals(colors.textDisabled, text.renderedStyle().color)
        val disabledPixels = onNodeWithTag("button").captureToImage().toPixelMap()
        assertEquals(colors.surfaceLow, disabledPixels[disabledPixels.width / 2, 3])
    }

    /**
     * 颜色配置优先文字样式色；未指定当前状态颜色时显式文字色生效。
     */
    @Test
    fun explicitTextColorIsUsedUnlessStateColorOverridesIt() = runComposeUiTest {
        var enabled by mutableStateOf(true)
        var stateColor by mutableStateOf(Color.Unspecified)
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default) {
                NomiButton(
                    onClick = {},
                    enabled = enabled,
                    colors = NomiButtonColors(
                        contentColor = stateColor,
                        disabledContentColor = stateColor
                    ),
                    textStyle = TextStyle(color = Color.Red),
                ) { NomiText("Explicit", modifier = Modifier.testTag("text-style-color")) }
            }
        }
        for (state in listOf(true, false)) {
            for (value in listOf(Color.Unspecified, Color.Yellow, Color.Transparent)) {
                runOnIdle { enabled = state; stateColor = value }
                assertEquals(
                    if (value == Color.Unspecified) Color.Red else value,
                    onNodeWithTag("text-style-color", useUnmergedTree = true).renderedStyle().color,
                )
            }
        }
    }

    /**
     * 显式边距决定实际按钮尺寸，传入形状必须被绘制路径调用。
     */
    @Test
    fun explicitButtonPaddingAndShapeReachActualLayoutAndDrawing() = runComposeUiTest {
        val shape = ObservedRectangleShape()
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default) {
                NomiButton(
                    onClick = {},
                    modifier = Modifier.testTag("custom"),
                    shape = shape,
                    contentPadding = PaddingValues(horizontal = 30.dp, vertical = 17.dp),
                ) {
                    Box(Modifier.size(20.dp))
                }
            }
        }
        val bounds = onNodeWithTag("custom").getUnclippedBoundsInRoot()
        assertEquals(80.dp, bounds.right - bounds.left)
        assertEquals(54.dp, bounds.bottom - bounds.top)
        onNodeWithTag("custom").captureToImage()
        runOnIdle { assertTrue(shape.outlineCount > 0) }
    }

    /**
     * Nomi Card 单独消费内边距和边框；Surface 不额外增加 Card 的内容留白。
     */
    @Test
    fun cardPaddingAndBorderAreIndependentFromSurface() = runComposeUiTest {
        val colors = NomiSurfaceColors(containerColor = Color.White, contentColor = Color.Magenta)
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default) {
                Column {
                    NomiSurface(
                        modifier = Modifier.testTag("surface"),
                        shape = RectangleShape,
                        colors = colors,
                        border = NomiBorder.None,
                    ) { Box(Modifier.size(20.dp)) }
                    NomiCard(
                        modifier = Modifier.testTag("card"),
                        shape = RectangleShape,
                        colors = colors,
                        border = NomiBorder.Stroke(BorderStroke(2.dp, Color.Red)),
                        contentPadding = PaddingValues(11.dp),
                    ) { Box(Modifier.size(20.dp)) }
                }
            }
        }
        val surfaceBounds = onNodeWithTag("surface").getUnclippedBoundsInRoot()
        val cardBounds = onNodeWithTag("card").getUnclippedBoundsInRoot()
        assertEquals(20.dp, surfaceBounds.right - surfaceBounds.left)
        assertEquals(20.dp, surfaceBounds.bottom - surfaceBounds.top)
        assertEquals(42.dp, cardBounds.right - cardBounds.left)
        assertEquals(42.dp, cardBounds.bottom - cardBounds.top)
        val pixels = onNodeWithTag("card").captureToImage().toPixelMap()
        assertEquals(Color.Red, pixels[pixels.width / 2, 0])
        assertEquals(Color.White, pixels[pixels.width / 2, pixels.height / 2])
    }
}

/**
 * 从实际文本布局读取样式，避免仅断言中立 slot 而漏掉 renderer 错误。
 *
 * @receiver 提供 GetTextLayoutResult 语义操作且恰好返回一个布局结果的文本节点。
 * @return 唯一文本布局实际使用的输入样式。
 */
private fun SemanticsNodeInteraction.renderedStyle(): TextStyle {
    val results = mutableListOf<TextLayoutResult>()
    performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
    return results.single().layoutInput.style
}

/**
 * 记录自定义形状确实进入绘制流程；矩形让像素与布局验证保持精确。
 */
private class ObservedRectangleShape : Shape {
    /**
     * 已生成轮廓的次数。
     */
    var outlineCount: Int = 0
        private set

    /**
     * 递增本实例轮廓计数，并保留实际参数委托 RectangleShape 生成轮廓。
     *
     * @param size 要生成轮廓的实际像素尺寸。
     * @param layoutDirection 当前布局方向，原样传给矩形形状。
     * @param density 当前像素密度与字体缩放环境，原样传给矩形形状。
     * @return 覆盖给定尺寸的矩形轮廓。
     */
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        outlineCount++
        return RectangleShape.createOutline(
            size = size,
            layoutDirection = layoutDirection,
            density = density
        )
    }
}
