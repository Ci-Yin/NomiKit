package ciyin.designsystem.fluent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ciyin.designsystem.component.NomiBorder
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.card.NomiCard
import ciyin.designsystem.component.surface.NomiSurface
import ciyin.designsystem.component.surface.NomiSurfaceColors
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import io.github.composefluent.FluentTheme
import io.github.composefluent.LocalTextStyle
import io.github.composefluent.background.Layer
import io.github.composefluent.darkColors
import io.github.composefluent.lightColors
import kotlin.test.Test
import kotlin.test.assertEquals
import io.github.composefluent.component.Button as NativeButton
import io.github.composefluent.component.Text as NativeText
import io.github.composefluent.surface.Card as NativeCard

/**
 * 四组件使用独立原生 FluentTheme 参照，检查测量、像素、完整排版与实际交互。
 */
@OptIn(ExperimentalTestApi::class)
class FluentComponentsTest {
    /**
     * 未指定任何视觉参数时，四组件在浅深色及按钮启禁用下与原生一致。
     */
    @Test
    fun defaultComponentsMatchIndependentNativeTheme() = runComposeUiTest {
        var dark by mutableStateOf(false)
        var enabled by mutableStateOf(true)
        var nativeClicks = 0
        var adapterClicks = 0
        setContent {
            Row {
                FluentTheme(colors = if (dark) darkColors() else lightColors()) {
                    Column(modifier = Modifier.width(280.dp)) {
                        NativeButton(
                            onClick = { nativeClicks++ },
                            modifier = Modifier.testTag("native-button"),
                            disabled = !enabled,
                        ) {
                            NativeText(
                                "按钮对照",
                                modifier = Modifier.testTag("native-button-text")
                            )
                        }
                        Layer(modifier = Modifier.testTag("native-surface")) {
                            NativeText(
                                "表面对照",
                                modifier = Modifier.testTag("native-surface-text")
                            )
                        }
                        NativeCard(modifier = Modifier.testTag("native-card")) {
                            Column {
                                NativeText(
                                    "卡片对照",
                                    modifier = Modifier.testTag("native-card-text")
                                )
                            }
                        }
                        NativeText("正文对照", modifier = Modifier.testTag("native-text"))
                    }
                }
                NomiTheme(
                    designSystem = NomiDesignSystem.Fluent,
                    darkMode = if (dark) DarkMode.Dark else DarkMode.Light
                ) {
                    Column(modifier = Modifier.width(280.dp)) {
                        NomiButton(
                            onClick = { adapterClicks++ },
                            modifier = Modifier.testTag("app-button"),
                            enabled = enabled,
                        ) { NomiText("按钮对照", modifier = Modifier.testTag("app-button-text")) }
                        NomiSurface(modifier = Modifier.testTag("app-surface")) {
                            NomiText(
                                "表面对照",
                                modifier = Modifier.testTag("app-surface-text")
                            )
                        }
                        NomiCard(modifier = Modifier.testTag("app-card")) {
                            NomiText(
                                "卡片对照",
                                modifier = Modifier.testTag("app-card-text")
                            )
                        }
                        NomiText("正文对照", modifier = Modifier.testTag("app-text"))
                    }
                }
            }
        }
        for (isDark in listOf(false, true)) {
            for (isEnabled in listOf(true, false)) {
                runOnIdle { dark = isDark; enabled = isEnabled }
                waitForIdle()
                for (component in listOf("button", "surface", "card", "text")) {
                    assertFluentNativePixels(
                        onNodeWithTag("native-$component"),
                        onNodeWithTag("app-$component")
                    )
                    val suffix = if (component == "text") "text" else "$component-text"
                    assertFluentNativeText(
                        expected = onNodeWithTag(
                            "native-$suffix",
                            useUnmergedTree = true
                        ).fluentTextLayout(),
                        actual = onNodeWithTag(
                            "app-$suffix",
                            useUnmergedTree = true
                        ).fluentTextLayout(),
                    )
                }
                val raw = onNodeWithTag("native-button").fetchSemanticsNode().config
                val adapter = onNodeWithTag("app-button").fetchSemanticsNode().config
                assertEquals(
                    raw.contains(SemanticsProperties.Role),
                    adapter.contains(SemanticsProperties.Role)
                )
                assertEquals(
                    raw.contains(SemanticsProperties.Disabled),
                    adapter.contains(SemanticsProperties.Disabled)
                )
                onAllNodes(hasClickAction()).assertCountEquals(2)
                val before = nativeClicks
                onNodeWithTag("native-button").performTouchInput { click() }
                onNodeWithTag("app-button").performTouchInput { click() }
                waitForIdle()
                assertEquals(before + if (isEnabled) 1 else 0, nativeClicks)
                assertEquals(nativeClicks, adapterClicks)
            }
        }
    }

    /**
     * 部分文字样式与换行参数必须产生和原生相同的完整 TextLayout 输入与输出。
     */
    @Test
    fun partialTextStyleAndOverflowMatchNativeLayout() = runComposeUiTest {
        val style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 30.sp)
        setContent {
            Row {
                FluentTheme(colors = darkColors()) {
                    Layer {
                        NativeText(
                            text = "长中文与 English overflow 对照",
                            modifier = Modifier.width(130.dp).testTag("native-text"),
                            style = LocalTextStyle.current.merge(style),
                            color = Color.Magenta,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false,
                            maxLines = 1,
                        )
                    }
                }
                NomiTheme(designSystem = NomiDesignSystem.Fluent, darkMode = DarkMode.Dark) {
                    NomiSurface {
                        NomiText(
                            text = "长中文与 English overflow 对照",
                            modifier = Modifier.width(130.dp).testTag("app-text"),
                            style = style,
                            color = Color.Magenta,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = false,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
        assertFluentNativeText(
            onNodeWithTag("native-text").fluentTextLayout(),
            onNodeWithTag("app-text").fluentTextLayout()
        )
        assertFluentNativePixels(onNodeWithTag("native-text"), onNodeWithTag("app-text"))
    }

    /**
     * Surface 的颜色、任意形状、描边与 Card 的显式形状和内部 padding 精确映射。
     */
    @Test
    fun explicitSurfaceAndCardOverridesMatchNative() = runComposeUiTest {
        val shape = CutCornerShape(7.dp)
        val border = BorderStroke(width = 2.dp, color = Color.Red)
        val padding = PaddingValues(horizontal = 17.dp, vertical = 11.dp)
        setContent {
            Row {
                FluentTheme(colors = lightColors()) {
                    Column {
                        Layer(
                            modifier = Modifier.testTag("native-surface"),
                            shape = shape,
                            color = Color.White,
                            contentColor = Color.Green,
                            border = border,
                        ) { NativeText("表面显式对照") }
                        NativeCard(modifier = Modifier.testTag("native-card"), shape = shape) {
                            Column(modifier = Modifier.padding(padding)) { NativeText("卡片显式对照") }
                        }
                    }
                }
                NomiTheme(designSystem = NomiDesignSystem.Fluent, darkMode = DarkMode.Light) {
                    Column {
                        NomiSurface(
                            modifier = Modifier.testTag("app-surface"),
                            shape = shape,
                            colors = NomiSurfaceColors(
                                containerColor = Color.White,
                                contentColor = Color.Green
                            ),
                            border = NomiBorder.Stroke(border),
                        ) { NomiText("表面显式对照") }
                        NomiCard(
                            modifier = Modifier.testTag("app-card"),
                            shape = shape,
                            contentPadding = padding
                        ) { NomiText("卡片显式对照") }
                    }
                }
            }
        }
        assertFluentNativePixels(onNodeWithTag("native-surface"), onNodeWithTag("app-surface"))
        assertFluentNativePixels(onNodeWithTag("native-card"), onNodeWithTag("app-card"))
    }
}
