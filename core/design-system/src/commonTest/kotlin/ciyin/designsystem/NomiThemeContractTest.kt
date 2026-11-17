package ciyin.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import ciyin.designsystem.runtime.LocalNomiContentColor
import ciyin.designsystem.runtime.LocalNomiDesignSystem
import ciyin.designsystem.runtime.LocalNomiTextStyle
import ciyin.designsystem.runtime.requireNomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.LocalNomiTheme
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.defaultDarkNomiColorScheme
import ciyin.designsystem.theme.defaultLightNomiColorScheme
import ciyin.designsystem.theme.defaultNomiShapes
import ciyin.designsystem.theme.defaultNomiSizes
import ciyin.designsystem.theme.defaultNomiTypography
import ciyin.designsystem.theme.defaultSpacings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame

/**
 * 中立主题值与唯一主题入口的契约。
 */
@OptIn(ExperimentalTestApi::class)
class NomiThemeContractTest {
    /**
     * 浅色与深色工厂只替换颜色和明暗，其它组复用默认值。
     */
    @Test
    fun lightAndDarkFactoriesUseResolvedMode() {
        assertEquals(defaultLightNomiColorScheme(), NomiTheme.light().colorScheme)
        assertEquals(false, NomiTheme.light().isDark)
        assertEquals(defaultDarkNomiColorScheme(), NomiTheme.dark().colorScheme)
        assertEquals(true, NomiTheme.dark().isDark)
        assertSame(defaultNomiTypography(), NomiTheme.dark().typography)
        assertEquals(defaultNomiShapes(), NomiTheme.dark().shapes)
        assertEquals(defaultSpacings(), NomiTheme.dark().spacings)
        assertEquals(defaultNomiSizes(), NomiTheme.dark().sizes)
    }

    /**
     * 未进入主题时 LocalNomiTheme 与伴生 getter 使用浅色默认值。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun unwrappedLocalUsesLightDefault() = runComposeUiTest {
        setContent {
            val local = LocalNomiTheme.current
            val colorScheme = NomiTheme.colorScheme
            val typography = NomiTheme.typography
            val shapes = NomiTheme.shapes
            val spacings = NomiTheme.spacings
            val sizes = NomiTheme.sizes
            val isDark = NomiTheme.isDark
            SideEffect {
                assertEquals(NomiTheme(), local)
                assertSame(local.colorScheme, colorScheme)
                assertSame(local.typography, typography)
                assertSame(local.shapes, shapes)
                assertSame(local.spacings, spacings)
                assertSame(local.sizes, sizes)
                assertEquals(local.isDark, isDark)
            }
        }
    }

    /**
     * 入口发布显式主题并提供设计系统，同时清空外部显式内容 slot。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun entryPublishesExplicitThemeAndResetsSlots() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val theme = NomiTheme.dark().copy(
            typography = defaultNomiTypography().copy(bodyMedium = TextStyle(fontSize = 29.sp)),
        )
        var observedTheme: NomiTheme? = null
        var observedSystem: String? = null
        var observedColor: Color? = null
        var observedStyle: TextStyle? = null
        setContent {
            CompositionLocalProvider(
                LocalNomiContentColor provides Color.Red,
                LocalNomiTextStyle provides TextStyle(color = Color.Blue),
            ) {
                NomiTheme(theme = theme, designSystem = designSystem) {
                    observedTheme = LocalNomiTheme.current
                    observedSystem = requireNomiDesignSystem().id.value
                    observedColor = LocalNomiContentColor.current
                    observedStyle = LocalNomiTextStyle.current
                }
            }
        }
        runOnIdle {
            assertEquals(theme, observedTheme)
            assertEquals(designSystem.id.value, observedSystem)
            assertEquals(listOf(theme), designSystem.received)
            assertEquals(Color.Unspecified, observedColor)
            assertEquals(TextStyle.Default, observedStyle)
        }
    }

    /**
     * 省略 theme 时按 darkMode 安装浅色或深色默认主题。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun darkModeSelectsDefaultTheme() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        var light: NomiTheme? = null
        var dark: NomiTheme? = null
        setContent {
            NomiTheme(designSystem = designSystem, darkMode = DarkMode.Light) {
                light = LocalNomiTheme.current
            }
            NomiTheme(designSystem = designSystem, darkMode = DarkMode.Dark) {
                dark = LocalNomiTheme.current
            }
        }
        runOnIdle {
            assertEquals(NomiTheme.light(), light)
            assertEquals(NomiTheme.dark(), dark)
        }
    }

    /**
     * 显式 theme 优先于 darkMode 请求。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun explicitThemeWinsOverDarkMode() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val explicit = NomiTheme.dark()
        var observed: NomiTheme? = null
        setContent {
            NomiTheme(
                designSystem = designSystem,
                darkMode = DarkMode.Light,
                theme = explicit,
            ) {
                observed = LocalNomiTheme.current
            }
        }
        runOnIdle { assertEquals(explicit, observed) }
    }

    /**
     * 嵌套入口显式传入当前主题时继承父级，显式主题离开后恢复父级。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun nestedEntryInheritsWhenCurrentThemePassed() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val parent = NomiTheme.dark()
        val child = NomiTheme.light()
        val observed = mutableListOf<NomiTheme>()
        setContent {
            NomiTheme(theme = parent, designSystem = designSystem) {
                RecordTheme(observed)
                NomiTheme(theme = LocalNomiTheme.current, designSystem = designSystem) {
                    RecordTheme(observed)
                }
                NomiTheme(theme = child, designSystem = designSystem) { RecordTheme(observed) }
                RecordTheme(observed)
            }
        }
        runOnIdle {
            assertEquals(listOf(parent, parent, child, parent), observed)
            assertNotSame(parent, child)
        }
    }
}

/**
 * 在组合期读取当前主题并记录，供嵌套恢复断言使用。
 *
 * @param themes 接收当前主题的可变列表。
 */
@Composable
private fun RecordTheme(themes: MutableList<NomiTheme>) {
    val theme = LocalNomiTheme.current
    SideEffect { themes += theme }
}