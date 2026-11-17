package ciyin.designsystem.material3

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import androidx.compose.material3.Button as NativeButton
import androidx.compose.material3.Card as NativeCard
import androidx.compose.material3.Surface as NativeSurface
import androidx.compose.material3.Text as NativeText

/**
 * 独立原生 MaterialTheme 与中立入口的几何、像素和排版等价测试。
 */
@OptIn(ExperimentalTestApi::class)
class Material3NativeParityTest {
    /**
     * 默认 Button/Card/Surface/Text 在浅深色中保留原生外观，不注入统一高度或内边距。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun omittedParametersMatchNativeComponents() = runComposeUiTest {
        var dark by mutableStateOf(false)
        var enabled by mutableStateOf(true)
        setContent {
            Column {
                MaterialTheme(
                    colorScheme = if (dark) darkColorScheme() else lightColorScheme(),
                    typography = Typography(),
                    shapes = Shapes(),
                ) {
                    Column {
                        NativeButton(
                            onClick = {},
                            modifier = Modifier.testTag("native-button"),
                            enabled = enabled,
                        ) {
                            NativeText(
                                "原生对照",
                                modifier = Modifier.testTag("native-button-text")
                            )
                        }
                        NativeCard(modifier = Modifier.testTag("native-card")) { NativeText("卡片对照") }
                        NativeSurface(modifier = Modifier.testTag("native-surface")) { NativeText("表面对照") }
                        NativeText("正文对照", modifier = Modifier.testTag("native-text"))
                    }
                }
                NomiTheme(
                    darkMode = if (dark) DarkMode.Dark else DarkMode.Light,
                    designSystem = NomiDesignSystem.Material3,
                ) {
                    Column {
                        NomiButton(
                            onClick = {},
                            modifier = Modifier.testTag("app-button"),
                            enabled = enabled,
                        ) {
                            NomiText("原生对照", modifier = Modifier.testTag("app-button-text"))
                        }
                        NomiCard(modifier = Modifier.testTag("app-card")) { NomiText("卡片对照") }
                        NomiSurface(modifier = Modifier.testTag("app-surface")) { NomiText("表面对照") }
                        NomiText("正文对照", modifier = Modifier.testTag("app-text"))
                    }
                }
            }
        }
        for (isDark in listOf(false, true)) {
            for (isEnabled in listOf(true, false)) {
                runOnIdle { dark = isDark; enabled = isEnabled }
                waitForIdle()
                for (component in listOf("button", "card", "surface", "text")) {
                    assertNativeImageEquals(
                        expected = onNodeWithTag("native-$component"),
                        actual = onNodeWithTag("app-$component"),
                    )
                }
                assertEquals(
                    onNodeWithTag("native-button-text", useUnmergedTree = true).actualTextStyle(),
                    onNodeWithTag("app-button-text", useUnmergedTree = true).actualTextStyle(),
                )
            }
        }
    }

    /**
     * 所有变体的原生默认及只覆盖内容色的禁用态与独立原生按钮一致。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun variantsAndSparseColorsMatchTheirNativeCounterparts() = runComposeUiTest {
        var variant by mutableStateOf<NomiButtonVariant?>(null)
        var enabled by mutableStateOf(true)
        var custom by mutableStateOf(false)
        setContent {
            Column {
                MaterialTheme(
                    colorScheme = lightColorScheme(),
                    typography = Typography(),
                    shapes = Shapes(),
                ) {
                    NativeReferenceButton(
                        variant = variant,
                        enabled = enabled,
                        contentColor = if (custom) Color.Magenta else Color.Unspecified,
                    ) { NativeText("按钮对照") }
                }
                NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                    NomiButton(
                        onClick = {},
                        modifier = Modifier.testTag("app-variant"),
                        enabled = enabled,
                        variant = variant,
                        colors = NomiButtonColors(contentColor = if (custom) Color.Magenta else Color.Unspecified),
                    ) { NomiText("按钮对照") }
                }
            }
        }
        for (current in listOf(null) + NomiButtonVariant.entries) {
            for (isEnabled in listOf(true, false)) {
                for (isCustom in listOf(false, true)) {
                    runOnIdle { variant = current; enabled = isEnabled; custom = isCustom }
                    waitForIdle()
                    assertNativeImageEquals(
                        expected = onNodeWithTag("native-variant"),
                        actual = onNodeWithTag("app-variant"),
                    )
                }
            }
        }
    }

    /**
     * 组件级任意形状、padding、单个颜色覆盖精确传入，显式文字样式优先尺寸预设。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun explicitOverridesMatchNativeAndTextStyleOverridesSizePreset() = runComposeUiTest {
        val shape = CutCornerShape(7.dp)
        val padding = PaddingValues(horizontal = 19.dp, vertical = 17.dp)
        val style = TextStyle(fontSize = 25.sp, lineHeight = 31.sp)
        val border = BorderStroke(width = 2.dp, color = Color.Red)
        setContent {
            Column {
                MaterialTheme(
                    colorScheme = lightColorScheme(),
                    typography = Typography(),
                    shapes = Shapes(),
                ) {
                    NativeButton(
                        onClick = {},
                        modifier = Modifier.testTag("native-explicit"),
                        shape = shape,
                        contentPadding = padding,
                        colors = ButtonDefaults.buttonColors(contentColor = Color.Yellow),
                    ) {
                        NativeText(
                            "显式按钮",
                            style = MaterialTheme.typography.labelMedium.merge(style)
                        )
                    }
                    NativeCard(
                        modifier = Modifier.testTag("native-explicit-card"),
                        shape = shape,
                        colors = CardDefaults.cardColors().copy(contentColor = Color.Blue),
                        border = border,
                    ) { Column(modifier = Modifier.padding(padding)) { NativeText("显式卡片") } }
                }
                NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                    NomiButton(
                        onClick = {},
                        modifier = Modifier.testTag("app-explicit"),
                        size = NomiControlSize.Small,
                        shape = shape,
                        contentPadding = padding,
                        textStyle = style,
                        colors = NomiButtonColors(contentColor = Color.Yellow),
                    ) { NomiText("显式按钮") }
                    NomiCard(
                        modifier = Modifier.testTag("app-explicit-card"),
                        shape = shape,
                        colors = NomiSurfaceColors(contentColor = Color.Blue),
                        border = NomiBorder.Stroke(border),
                        contentPadding = padding,
                    ) { NomiText("显式卡片") }
                }
            }
        }
        assertNativeImageEquals(onNodeWithTag("native-explicit"), onNodeWithTag("app-explicit"))
        assertNativeImageEquals(
            onNodeWithTag("native-explicit-card"),
            onNodeWithTag("app-explicit-card")
        )
    }

    /**
     * 原生按钮内的中立文字部分样式不能重置原生 labelLarge 的字号、行高与内容色。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun partialTextStyleMergesWithNativeSlot() = runComposeUiTest {
        setContent {
            Column {
                MaterialTheme(
                    colorScheme = darkColorScheme(),
                    typography = Typography(),
                    shapes = Shapes(),
                ) {
                    NativeButton(onClick = {}, enabled = false) {
                        NativeText(
                            text = "部分样式",
                            modifier = Modifier.testTag("native-partial"),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        )
                    }
                }
                NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Dark) {
                    NativeButton(onClick = {}, enabled = false) {
                        NomiText(
                            text = "部分样式",
                            modifier = Modifier.testTag("app-partial"),
                            style = TextStyle(fontWeight = FontWeight.Bold),
                        )
                    }
                }
            }
        }
        assertEquals(
            onNodeWithTag("native-partial", useUnmergedTree = true).actualTextStyle(),
            onNodeWithTag("app-partial", useUnmergedTree = true).actualTextStyle(),
        )
    }

    /**
     * 逐字段文本覆盖与自动字号在适配器与原生 Text 间产生相同布局样式。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun fieldOverridesAndAutoSizeMatchNativeText() = runComposeUiTest {
        setContent {
            MaterialTheme {
                Column {
                    NativeText(
                        text = "逐字段",
                        modifier = Modifier.testTag("native-fields"),
                        fontSize = 19.sp,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        textDecoration = TextDecoration.Underline,
                        textAlign = TextAlign.Center,
                        lineHeight = 27.sp,
                        autoSize = TextAutoSize.StepBased(),
                    )
                    NativeText(
                        text = AnnotatedString("富文本"),
                        modifier = Modifier.testTag("native-rich"),
                        fontWeight = FontWeight.Medium,
                    )
                }
                NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                    Column {
                        NomiText(
                            text = "逐字段",
                            modifier = Modifier.testTag("app-fields"),
                            fontSize = 19.sp,
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            textDecoration = TextDecoration.Underline,
                            textAlign = TextAlign.Center,
                            lineHeight = 27.sp,
                            autoSize = TextAutoSize.StepBased(),
                        )
                        NomiText(
                            text = AnnotatedString("富文本"),
                            modifier = Modifier.testTag("app-rich"),
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
        assertEquals(
            onNodeWithTag("native-fields").actualTextStyle(),
            onNodeWithTag("app-fields").actualTextStyle(),
        )
        assertEquals(
            onNodeWithTag("native-rich").actualTextStyle(),
            onNodeWithTag("app-rich").actualTextStyle(),
        )
        assertNativeImageEquals(onNodeWithTag("native-fields"), onNodeWithTag("app-fields"))
    }
}

/**
 * 不经过 NomiTheme 的独立原生变体参照。
 *
 * @param variant null/Primary 使用 Button，Default 使用 FilledTonalButton，Subtle/Text 使用 TextButton，Destructive 使用错误色 Button。
 * @param enabled 原生按钮的启用状态。
 * @param contentColor 仅覆盖启用态内容色；Unspecified 保留该变体的原生值。
 * @param content 原生 RowScope 内容，使用按钮提供的真实排版与颜色。
 */
@Composable
private fun NativeReferenceButton(
    variant: NomiButtonVariant?,
    enabled: Boolean,
    contentColor: Color,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = when (variant) {
        NomiButtonVariant.Default -> ButtonDefaults.filledTonalButtonColors()
        NomiButtonVariant.Subtle, NomiButtonVariant.Text -> ButtonDefaults.textButtonColors()
        NomiButtonVariant.Destructive -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
        )

        null, NomiButtonVariant.Primary -> ButtonDefaults.buttonColors()
    }.copy(contentColor = contentColor)
    when (variant) {
        NomiButtonVariant.Default -> FilledTonalButton(
            onClick = {},
            modifier = Modifier.testTag("native-variant"),
            enabled = enabled,
            colors = colors,
            content = content,
        )

        NomiButtonVariant.Subtle, NomiButtonVariant.Text -> TextButton(
            onClick = {},
            modifier = Modifier.testTag("native-variant"),
            enabled = enabled,
            colors = colors,
            content = content,
        )

        null, NomiButtonVariant.Primary, NomiButtonVariant.Destructive -> NativeButton(
            onClick = {},
            modifier = Modifier.testTag("native-variant"),
            enabled = enabled,
            colors = colors,
            content = content,
        )
    }
}

/**
 * 同时比较测量尺寸与逐像素颜色；允许浮点颜色编码的微小舍入，不放宽布局偏差。
 *
 * @param expected 独立原生参照节点，提供预期尺寸和截图。
 * @param actual 待验证的适配器节点；尺寸须相等，每个 RGBA 通道误差须小于 0.01。
 */
@OptIn(ExperimentalTestApi::class)
private fun assertNativeImageEquals(
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
                abs(a.red - b.red) < 0.01f && abs(a.green - b.green) < 0.01f && abs(a.blue - b.blue) < 0.01f && abs(
                    a.alpha - b.alpha
                ) < 0.01f, "像素不匹配：($x, $y)，$a / $b"
            )
        }
    }
}

/**
 * 读取实际排版输出，避免仅验证外层局部值。
 *
 * @receiver 提供恰好一项文本布局结果的语义节点。
 * @return 实际布局输入使用的 TextStyle。
 */
private fun SemanticsNodeInteraction.actualTextStyle(): TextStyle {
    val results = mutableListOf<TextLayoutResult>()
    performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action -> action(results) }
    return results.single().layoutInput.style
}
