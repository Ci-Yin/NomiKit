package ciyin.designsystem.fluent

import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * 比较原生参照的尺寸与逐像素预乘颜色，不容忍布局漂移。
 *
 * Skia 8-bit 截图在低 alpha 边缘反预乘会放大 RGB 量化误差；比较预乘通道和 alpha，
 * 仅允许两次 8-bit 量化的误差，不跳过边缘像素或忽略透明度。
 *
 * @param expected 独立原生参照节点，提供预期布局尺寸和像素。
 * @param actual 待验证的适配器节点，尺寸必须完全一致，预乘通道和 alpha 误差不超过 PixelQuantizationTolerance。
 */
@OptIn(ExperimentalTestApi::class)
internal fun assertFluentNativePixels(
    expected: SemanticsNodeInteraction,
    actual: SemanticsNodeInteraction
) {
    assertEquals(expected.fetchSemanticsNode().size, actual.fetchSemanticsNode().size)
    val left = expected.captureToImage().toPixelMap()
    val right = actual.captureToImage().toPixelMap()
    assertEquals(left.width, right.width)
    assertEquals(left.height, right.height)
    for (y in 0 until left.height) {
        for (x in 0 until left.width) {
            val a = left[x, y]
            val b = right[x, y]
            assertTrue(
                abs(a.red * a.alpha - b.red * b.alpha) <= PixelQuantizationTolerance &&
                        abs(a.green * a.alpha - b.green * b.alpha) <= PixelQuantizationTolerance &&
                        abs(a.blue * a.alpha - b.blue * b.alpha) <= PixelQuantizationTolerance &&
                        abs(a.alpha - b.alpha) <= PixelQuantizationTolerance,
                "原生像素不一致：($x, $y)，$a / $b",
            )
        }
    }
}

/**
 * 读取真正参与文字布局的结果。
 *
 * @receiver 必须提供恰好一个文本布局结果的语义节点。
 * @return 节点实际产生的唯一 TextLayoutResult，供完整排版比较。
 */
internal fun SemanticsNodeInteraction.fluentTextLayout(): TextLayoutResult {
    val results = mutableListOf<TextLayoutResult>()
    performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action -> action(results) }
    return results.single()
}

/**
 * 比较完整输入排版与布局约束、换行、基线及输出大小，不能只检查字号。
 *
 * @param expected 原生文本布局结果，提供预期文本、样式、约束、密度及输出几何。
 * @param actual 适配器文本布局结果，所有参与比较的字段必须精确相等。
 */
internal fun assertFluentNativeText(expected: TextLayoutResult, actual: TextLayoutResult) {
    assertEquals(expected.layoutInput.text, actual.layoutInput.text)
    assertEquals(expected.layoutInput.style, actual.layoutInput.style)
    assertEquals(expected.layoutInput.maxLines, actual.layoutInput.maxLines)
    assertEquals(expected.layoutInput.softWrap, actual.layoutInput.softWrap)
    assertEquals(expected.layoutInput.overflow, actual.layoutInput.overflow)
    assertEquals(expected.layoutInput.constraints, actual.layoutInput.constraints)
    assertEquals(expected.layoutInput.density.density, actual.layoutInput.density.density)
    assertEquals(expected.layoutInput.density.fontScale, actual.layoutInput.density.fontScale)
    assertEquals(expected.layoutInput.layoutDirection, actual.layoutInput.layoutDirection)
    assertEquals(expected.layoutInput.placeholders, actual.layoutInput.placeholders)
    assertEquals(expected.size, actual.size)
    assertEquals(expected.lineCount, actual.lineCount)
    assertEquals(expected.firstBaseline, actual.firstBaseline)
    assertEquals(expected.lastBaseline, actual.lastBaseline)
    assertEquals(expected.didOverflowHeight, actual.didOverflowHeight)
    assertEquals(expected.didOverflowWidth, actual.didOverflowWidth)
}

/**
 * 两次 8-bit 存储量化的最大通道误差。
 */
private const val PixelQuantizationTolerance = 2f / 255f
