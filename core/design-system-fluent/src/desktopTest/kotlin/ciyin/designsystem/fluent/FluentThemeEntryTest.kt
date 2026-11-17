package ciyin.designsystem.fluent

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.LocalNomiTheme
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.defaultNomiShapes
import ciyin.designsystem.theme.defaultNomiSizes
import ciyin.designsystem.theme.defaultNomiTypography
import ciyin.designsystem.theme.defaultSpacings
import io.github.composefluent.Colors
import io.github.composefluent.CornerRadius
import io.github.composefluent.Typography
import io.github.composefluent.darkColors
import io.github.composefluent.lightColors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import io.github.composefluent.FluentTheme as NativeFluentTheme

/**
 * Fluent 主题入口的映射函数模型：预置映射、自定义映射与原生主题安装。
 */
@OptIn(ExperimentalTestApi::class)
class FluentThemeEntryTest {
    /**
     * 零参数工厂只跟随明暗模式，安装原生浅色板，中立主题原样保留入口传入值。
     */
    @Test
    fun nativeFactoryInstallsPaletteAndKeepsExplicitTheme() = runComposeUiTest {
        val spacings = defaultSpacings().copy(large = 27.dp)
        val sizes = defaultNomiSizes()
        val theme = NomiTheme.light().copy(spacings = spacings, sizes = sizes)
        var nativeColors: Colors? = null
        var published: NomiTheme? = null
        setContent {
            NomiTheme(theme = theme, designSystem = NomiDesignSystem.Fluent) {
                nativeColors = NativeFluentTheme.colors
                published = LocalNomiTheme.current
            }
        }
        runOnIdle {
            assertEquals(false, requireNotNull(nativeColors).darkMode)
            assertSame(theme, requireNotNull(published))
        }
    }

    /**
     * 深色中立主题安装深色原生色板，不读取系统状态。
     */
    @Test
    fun darkThemeInstallsDarkPalette() = runComposeUiTest {
        val theme = NomiTheme.dark()
        var nativeColors: Colors? = null
        var published: NomiTheme? = null
        setContent {
            NomiTheme(theme = theme, designSystem = NomiDesignSystem.Fluent) {
                nativeColors = NativeFluentTheme.colors
                published = LocalNomiTheme.current
            }
        }
        runOnIdle {
            assertTrue(requireNotNull(nativeColors).darkMode)
            assertSame(theme, requireNotNull(published))
        }
    }

    /**
     * Primary 映射只替换强调色阶，非强调色角色与原生浅色明暗保持不变。
     */
    @Test
    fun primaryMapOnlyReplacesAccentRamp() = runComposeUiTest {
        val accent = Color(0xFF123456)
        val theme = NomiTheme.light().copy(
            colorScheme = NomiTheme.light().colorScheme.copy(primary = accent),
        )
        var nativeColors: Colors? = null
        var published: NomiTheme? = null
        setContent {
            NomiTheme(
                theme = theme,
                designSystem = NomiDesignSystem.Fluent { toPrimaryFluentTheme() }) {
                nativeColors = NativeFluentTheme.colors
                published = LocalNomiTheme.current
            }
        }
        runOnIdle {
            val colors = requireNotNull(nativeColors)
            val defaults = lightColors()
            assertEquals(accent, colors.shades.base)
            assertEquals(false, colors.darkMode)
            assertEquals(defaults.background.solid.base, colors.background.solid.base)
            assertEquals(defaults.text.text.primary, colors.text.text.primary)
            assertEquals(defaults.stroke.control.default, colors.stroke.control.default)
            assertEquals(defaults.system.critical, colors.system.critical)
            assertSame(theme, requireNotNull(published))
        }
    }

    /**
     * Full 映射把八角色排版与三档圆角写入原生主题，中立主题仍保持入口传入值。
     */
    @Test
    fun fullMapTranslatesEightTypeRolesAndThreeCornerRadii() = runComposeUiTest {
        val typography = defaultNomiTypography().copy(
            displayLarge = TextStyle(fontSize = 61.sp),
            headlineLarge = TextStyle(fontSize = 39.sp),
            headlineMedium = TextStyle(fontSize = 29.sp),
            titleLarge = TextStyle(fontSize = 23.sp),
            bodyLarge = TextStyle(fontSize = 19.sp),
            bodyMedium = TextStyle(fontSize = 17.sp),
            bodySmall = TextStyle(fontSize = 13.sp),
            labelLarge = TextStyle(fontSize = 15.sp),
        )
        val shapes = defaultNomiShapes().copy(
            tiny = RoundedCornerShape(9.dp),
            small = RoundedCornerShape(11.dp),
            large = RoundedCornerShape(13.dp),
        )
        val theme = NomiTheme.light().copy(typography = typography, shapes = shapes)
        var nativeTypography: Typography? = null
        var nativeCorners: CornerRadius? = null
        var published: NomiTheme? = null
        setContent {
            NomiTheme(
                theme = theme,
                designSystem = NomiDesignSystem.Fluent { toFullFluentTheme() }) {
                nativeTypography = NativeFluentTheme.typography
                nativeCorners = NativeFluentTheme.cornerRadius
                published = LocalNomiTheme.current
            }
        }
        runOnIdle {
            val native = requireNotNull(nativeTypography)
            assertEquals(typography.bodySmall, native.caption)
            assertEquals(typography.bodyMedium, native.body)
            assertEquals(typography.labelLarge, native.bodyStrong)
            assertEquals(typography.bodyLarge, native.bodyLarge)
            assertEquals(typography.titleLarge, native.subtitle)
            assertEquals(typography.headlineMedium, native.title)
            assertEquals(typography.headlineLarge, native.titleLarge)
            assertEquals(typography.displayLarge, native.display)
            val corners = requireNotNull(nativeCorners)
            assertEquals(9.dp, corners.intersectionEdge)
            assertEquals(11.dp, corners.control)
            assertEquals(13.dp, corners.overlay)
            assertSame(theme, requireNotNull(published))
        }
    }

    /**
     * 自定义映射函数能读到本层中立主题，显式原生色板原样安装而不被工厂改写。
     */
    @Test
    fun customMapReceivesNeutralThemeAndExplicitValuesWin() = runComposeUiTest {
        val explicitColors = darkColors()
        var seenMode: Boolean? = null
        val map: NomiTheme.() -> FluentTheme = {
            seenMode = isDark
            FluentTheme(isDark = isDark, colors = explicitColors)
        }
        val theme = NomiTheme.dark()
        var nativeColors: Colors? = null
        var published: NomiTheme? = null
        setContent {
            NomiTheme(theme = theme, designSystem = NomiDesignSystem.Fluent(map)) {
                nativeColors = NativeFluentTheme.colors
                published = LocalNomiTheme.current
            }
        }
        runOnIdle {
            assertEquals(true, seenMode)
            assertSame(explicitColors, nativeColors)
            assertSame(theme, requireNotNull(published))
        }
    }
}
