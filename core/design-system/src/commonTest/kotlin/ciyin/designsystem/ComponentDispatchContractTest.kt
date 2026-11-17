package ciyin.designsystem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
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
import ciyin.designsystem.runtime.LocalNomiContentColor
import ciyin.designsystem.runtime.ProvideNomiContentStyle
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.defaultDarkNomiColorScheme
import ciyin.designsystem.theme.defaultNomiShapes
import ciyin.designsystem.theme.defaultNomiSizes
import ciyin.designsystem.theme.defaultNomiTypography
import ciyin.designsystem.theme.defaultSpacings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * 核心四组件只派发明确参数，不将投影令牌反灌为原生默认。
 */
@OptIn(ExperimentalTestApi::class)
class ComponentDispatchContractTest {
    /**
     * 即使主题投影不同，默认组件请求仍保留 null/Unspecified/Default。
     *
     * @return 当前平台的 Compose 测试执行结果，供测试框架等待组合断言与环境清理完成。
     */
    @Test
    fun defaultsRemainSparseAndCardHasIndependentDispatch() = runComposeUiTest {
        val defaults = NomiTheme(
            colorScheme = defaultDarkNomiColorScheme(),
            isDark = true,
            typography = defaultNomiTypography(),
            shapes = defaultNomiShapes(),
            spacings = defaultSpacings(),
            sizes = defaultNomiSizes(),
        )
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(theme = defaults, designSystem = designSystem) {
                NomiText("plain")
                NomiButton(onClick = {}) { NomiText("button-slot") }
                NomiSurface { NomiText("surface-slot") }
                NomiCard { NomiText("card-slot") }
            }
        }
        runOnIdle {
            assertEquals(
                setOf("plain", "button-slot", "surface-slot", "card-slot"),
                designSystem.texts.keys
            )
            designSystem.texts.values.forEach { call ->
                assertNull(call.style)
                assertEquals(Color.Unspecified, call.color)
            }
            assertEquals(
                RecordedButton(
                    enabled = true,
                    variant = null,
                    size = null,
                    colors = NomiButtonColors(),
                    shape = null,
                    contentPadding = null,
                    textStyle = null,
                ),
                designSystem.buttons.single(),
            )
            val surface = RecordedSurface(
                shape = null,
                colors = NomiSurfaceColors(),
                border = NomiBorder.Default
            )
            assertEquals(listOf(surface), designSystem.surfaces)
            assertEquals(listOf(surface), designSystem.cards)
        }
    }

    /**
     * 文本颜色优先级保持明确且无正文预填，部分样式只合并 slot 中指定的字段。
     *
     * @return 当前平台的 Compose 测试执行结果，供测试框架等待组合断言与环境清理完成。
     */
    @Test
    fun textPreservesNullableStyleAndExactColorPrecedence() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val inherited = TextStyle(color = Color.Green, fontSize = 23.sp)
        val explicitStyle = TextStyle(color = Color.Yellow, fontWeight = FontWeight.Bold)
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiText("native")
                NomiText(text = "partial", style = TextStyle(fontWeight = FontWeight.Bold))
                ProvideNomiContentStyle(color = Color.Blue, textStyle = inherited) {
                    NomiText(text = "explicit", style = explicitStyle, color = Color.Red)
                    NomiText(text = "style", style = explicitStyle)
                    NomiText("slot")
                    CompositionLocalProvider(LocalNomiContentColor provides Color.Unspecified) {
                        NomiText("inherited")
                    }
                    ProvideNomiContentStyle(
                        color = Color.Unspecified,
                        textStyle = TextStyle.Default
                    ) {
                        NomiText("unspecified-provider")
                    }
                }
                NomiText(
                    text = "layout",
                    style = TextStyle(fontSize = 19.sp),
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    maxLines = 2,
                    minLines = 1,
                )
            }
        }
        runOnIdle {
            assertNull(designSystem.texts.getValue("native").style)
            assertEquals(Color.Unspecified, designSystem.texts.getValue("native").color)
            assertEquals(
                TextStyle(fontWeight = FontWeight.Bold),
                designSystem.texts.getValue("partial").style
            )
            assertEquals(Color.Red, designSystem.texts.getValue("explicit").color)
            assertEquals(Color.Yellow, designSystem.texts.getValue("style").color)
            assertEquals(Color.Blue, designSystem.texts.getValue("slot").color)
            assertEquals(Color.Green, designSystem.texts.getValue("inherited").color)
            assertEquals(Color.Blue, designSystem.texts.getValue("unspecified-provider").color)
            assertEquals(
                inherited.merge(explicitStyle),
                designSystem.texts.getValue("explicit").style
            )
            assertEquals(inherited, designSystem.texts.getValue("slot").style)
            assertEquals(
                RecordedText(
                    style = TextStyle(fontSize = 19.sp),
                    color = Color.Unspecified,
                    overflow = TextOverflow.Ellipsis,
                    softWrap = false,
                    maxLines = 2,
                    minLines = 1,
                ),
                designSystem.texts.getValue("layout"),
            )
        }
    }

    /**
     * 显式按钮形状、边距、排版与部分颜色逐项原样交给 renderer。
     *
     * @return 当前平台的 Compose 测试执行结果，供测试框架等待组合断言与环境清理完成。
     */
    @Test
    fun explicitButtonParametersDoNotFillUnspecifiedColors() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val shape = RoundedCornerShape(topStart = 9.dp, bottomEnd = 17.dp)
        val padding = PaddingValues(horizontal = 31.dp, vertical = 13.dp)
        val style = TextStyle(fontSize = 24.sp)
        val colors = NomiButtonColors(contentColor = Color.Cyan)
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiButton(
                    onClick = {},
                    enabled = false,
                    variant = NomiButtonVariant.Default,
                    size = NomiControlSize.Large,
                    colors = colors,
                    shape = shape,
                    contentPadding = padding,
                    textStyle = style,
                ) {}
            }
        }
        runOnIdle {
            val call = designSystem.buttons.single()
            assertEquals(
                RecordedButton(
                    enabled = false,
                    variant = NomiButtonVariant.Default,
                    size = NomiControlSize.Large,
                    colors = colors,
                    shape = shape,
                    contentPadding = padding,
                    textStyle = style,
                ),
                call,
            )
            assertSame(shape, call.shape)
            assertEquals(Color.Unspecified, call.colors.containerColor)
            assertEquals(Color.Unspecified, call.colors.disabledContainerColor)
            assertEquals(Color.Unspecified, call.colors.disabledContentColor)
        }
    }

    /**
     * 容器三态边框不合并，Card 使用自身派发并保留明确内边距。
     *
     * @return 当前平台的 Compose 测试执行结果，供测试框架等待组合断言与环境清理完成。
     */
    @Test
    fun surfaceAndCardPreserveDefaultNoneAndStroke() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val shape = RoundedCornerShape(11.dp)
        val padding = PaddingValues(7.dp)
        val colors = NomiSurfaceColors(contentColor = Color.Magenta)
        val borders = listOf(
            NomiBorder.Default,
            NomiBorder.None,
            NomiBorder.Stroke(BorderStroke(2.dp, Color.Red))
        )
        setContent {
            NomiTheme(designSystem = designSystem) {
                borders.forEach { border ->
                    NomiSurface(shape = shape, colors = colors, border = border) {}
                    NomiCard(
                        shape = shape,
                        colors = colors,
                        border = border,
                        contentPadding = padding
                    ) {}
                }
            }
        }
        runOnIdle {
            assertEquals(
                borders.map { RecordedSurface(shape = shape, colors = colors, border = it) },
                designSystem.surfaces,
            )
            assertEquals(
                borders.map {
                    RecordedSurface(
                        shape = shape,
                        colors = colors,
                        border = it,
                        contentPadding = padding
                    )
                },
                designSystem.cards,
            )
            assertEquals(3, designSystem.surfaces.size)
            assertEquals(3, designSystem.cards.size)
        }
    }

    /**
     * 逐字段文本覆盖与富文本内联内容原样到达 renderer，未指定字段保持稀疏。
     *
     * @return 当前平台的 Compose 测试执行结果，供测试框架等待组合断言与环境清理完成。
     */
    @Test
    fun textFieldOverridesAndAnnotatedContentReachRenderer() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val inline = mapOf(
            "icon" to InlineTextContent(
                Placeholder(
                    width = 12.sp,
                    height = 12.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center,
                ),
            ) { },
        )
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiText(
                    text = "fields",
                    fontSize = 21.sp,
                    fontStyle = FontStyle.Italic,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp,
                    textDecoration = TextDecoration.Underline,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp,
                    onTextLayout = { },
                    autoSize = TextAutoSize.StepBased(),
                )
                NomiText(text = AnnotatedString("rich"), inlineContent = inline)
                NomiText("sparse")
            }
        }
        runOnIdle {
            val fields = requireNotNull(designSystem.texts["fields"])
            assertEquals(21.sp, fields.fontSize)
            assertEquals(FontStyle.Italic, fields.fontStyle)
            assertEquals(FontWeight.Bold, fields.fontWeight)
            assertEquals(FontFamily.Monospace, fields.fontFamily)
            assertEquals(2.sp, fields.letterSpacing)
            assertEquals(TextDecoration.Underline, fields.textDecoration)
            assertEquals(TextAlign.Center, fields.textAlign)
            assertEquals(30.sp, fields.lineHeight)
            assertTrue(fields.hasOnTextLayout)
            assertEquals(TextAutoSize.StepBased(), fields.autoSize)
            val rich = requireNotNull(designSystem.texts["rich"])
            assertEquals(setOf("icon"), rich.inlineContentKeys)
            val sparse = requireNotNull(designSystem.texts["sparse"])
            assertEquals(TextUnit.Unspecified, sparse.fontSize)
            assertNull(sparse.fontStyle)
            assertNull(sparse.fontWeight)
            assertNull(sparse.fontFamily)
            assertEquals(TextUnit.Unspecified, sparse.letterSpacing)
            assertNull(sparse.textDecoration)
            assertNull(sparse.textAlign)
            assertEquals(TextUnit.Unspecified, sparse.lineHeight)
            assertEquals(emptySet<String>(), sparse.inlineContentKeys)
            assertFalse(sparse.hasOnTextLayout)
            assertNull(sparse.autoSize)
        }
    }
}
