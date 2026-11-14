package ciyin.designsystem.material3

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.card.NomiCard
import ciyin.designsystem.component.surface.NomiSurface
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.runtime.LocalNomiContentColor
import ciyin.designsystem.runtime.LocalNomiTextStyle
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.runtime.ProvideNomiContentStyle
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.NomiThemeOverride
import ciyin.designsystem.theme.defaultLightNomiColorScheme
import ciyin.designsystem.theme.defaultNomiTypography
import kotlin.test.Test
import kotlin.test.assertEquals
import androidx.compose.material3.Button as NativeButton

/**
 * 区分原生默认环境与中立显式 slot，避免嵌套原生控件被父容器的默认值覆盖。
 */
@OptIn(ExperimentalTestApi::class)
class Material3ExplicitSlotTest {
    /**
     * 默认 Card/Surface 不得向中立显式 locals 投影原生默认正文色及字号。
     *
     * @return 本回归的跨平台测试完成结果；Web 由测试运行器等待其 Promise，JVM/Native 同步完成。
     */
    @Test
    fun defaultContainersDoNotShadowNestedNativeButtonStateOrTypography() = runComposeUiTest {
        val typography = Typography(
            bodyLarge = TextStyle(fontSize = 33.sp),
            labelLarge = TextStyle(fontSize = 22.sp),
        )
        setContent {
            val designSystem = rememberMaterial3DesignSystem(typography = typography)
            NomiTheme(designSystem = designSystem, darkMode = DarkMode.Light) {
                Column {
                    NomiCard { NestedNativeButton("card-native") }
                    NomiSurface { NestedNativeButton("surface-native") }
                    NomiButton(onClick = {}) {
                        assertEquals(Color.Unspecified, LocalNomiContentColor.current)
                        assertEquals(TextStyle.Default, LocalNomiTextStyle.current)
                        NomiText("默认按钮")
                    }
                }
            }
        }
        for (tag in listOf("card-native", "surface-native")) {
            val actual = onNodeWithTag(tag, useUnmergedTree = true).layoutStyle()
            assertEquals(Color.Magenta, actual.color)
            assertEquals(typography.labelLarge.fontSize, actual.fontSize)
        }
    }

    /**
     * 默认 Material Card 内切到 Nomi 使用 Nomi 本身的默认正文，退出后恢复 Material 正文。
     *
     * @return 本回归的跨平台测试完成结果；Web 由测试运行器等待其 Promise，JVM/Native 同步完成。
     */
    @Test
    fun defaultCardDoesNotCarryNativeDefaultsIntoNomiOverride() = runComposeUiTest {
        val typography = Typography(bodyLarge = TextStyle(fontSize = 33.sp))
        val nomiTypography = defaultNomiTypography()
        val nomiColors = defaultLightNomiColorScheme()
        setContent {
            val designSystem = rememberMaterial3DesignSystem(typography = typography)
            NomiTheme(designSystem = designSystem, darkMode = DarkMode.Light) {
                NomiCard {
                    NomiThemeOverride(designSystem = NomiDesignSystem.Default) {
                        NomiText("Nomi正文", modifier = Modifier.testTag("nomi-body"))
                    }
                    NomiText("恢复正文", modifier = Modifier.testTag("restored-body"))
                }
            }
        }
        val child = onNodeWithTag("nomi-body").layoutStyle()
        assertEquals(nomiTypography.bodyMedium.fontSize, child.fontSize)
        assertEquals(nomiColors.textPrimary, child.color)
        assertEquals(33.sp, onNodeWithTag("restored-body").layoutStyle().fontSize)
    }

    /**
     * 默认容器不新增显式值，但必须保留调用方已经建立的显式 slot。
     *
     * @return 本回归的跨平台测试完成结果；Web 由测试运行器等待其 Promise，JVM/Native 同步完成。
     */
    @Test
    fun defaultContainersPreserveExplicitOuterSlot() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3) {
                ProvideNomiContentStyle(
                    color = Color.Green,
                    textStyle = TextStyle(fontSize = 27.sp)
                ) {
                    Column {
                        NomiCard {
                            NomiText(
                                "显式卡片",
                                modifier = Modifier.testTag("explicit-card")
                            )
                        }
                        NomiSurface {
                            NomiText(
                                "显式表面",
                                modifier = Modifier.testTag("explicit-surface")
                            )
                        }
                    }
                }
            }
        }
        for (tag in listOf("explicit-card", "explicit-surface")) {
            val style = onNodeWithTag(tag).layoutStyle()
            assertEquals(Color.Green, style.color)
            assertEquals(27.sp, style.fontSize)
        }
    }
}

/**
 * 构造真正的原生禁用按钮，不能用中立按钮绕过原生上下文回退。
 *
 * @param tag 内部中立文字的测试标识，用于读取其实际布局样式。
 */
@Composable
private fun NestedNativeButton(tag: String) {
    assertEquals(Color.Unspecified, LocalNomiContentColor.current)
    assertEquals(TextStyle.Default, LocalNomiTextStyle.current)
    NativeButton(
        onClick = {},
        enabled = false,
        colors = ButtonDefaults.buttonColors(disabledContentColor = Color.Magenta),
    ) {
        NomiText("原生嵌套", modifier = Modifier.testTag(tag))
    }
}

/**
 * 从实际文本布局读取颜色与排版，避免仅断言 provider 的中间值。
 *
 * @receiver 提供恰好一个文本布局结果的语义节点。
 * @return 布局实际使用的样式，包含最终内容色和字号。
 */
private fun SemanticsNodeInteraction.layoutStyle(): TextStyle {
    val results = mutableListOf<TextLayoutResult>()
    performSemanticsAction(SemanticsActions.GetTextLayoutResult) { action -> action(results) }
    return results.single().layoutInput.style
}
