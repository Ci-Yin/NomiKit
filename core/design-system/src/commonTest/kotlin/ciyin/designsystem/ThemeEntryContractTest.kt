package ciyin.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ciyin.designsystem.runtime.LocalResolvedNomiTheme
import ciyin.designsystem.runtime.ResolvedNomiTheme
import ciyin.designsystem.runtime.requireNomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import ciyin.designsystem.theme.NomiThemeOverride
import ciyin.designsystem.theme.NomiThemeOverrides
import ciyin.designsystem.theme.defaultDarkNomiColorScheme
import ciyin.designsystem.theme.defaultLightNomiColorScheme
import ciyin.designsystem.theme.defaultNomiShapes
import ciyin.designsystem.theme.defaultNomiSizes
import ciyin.designsystem.theme.defaultNomiTypography
import ciyin.designsystem.theme.defaultSpacings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * 主题根/局部入口基于 [NomiThemeOverrides] 的整组显式继承与颜色替换契约。
 */
@OptIn(ExperimentalTestApi::class)
class ThemeEntryContractTest {
    /**
     * 子整组颜色替换父色板，其它显式组原样保留并继续参与后续合并。
     */
    @Test
    fun mergePreservesWholeGroupsAndTracksColorReplacement() {
        val tokens = customResolvedTheme()
        val parent = NomiThemeOverrides(
            typography = tokens.typography,
            shapes = tokens.shapes,
            spacings = tokens.spacings,
            sizes = tokens.sizes,
        )
        val colorChild = NomiThemeOverrides(colorScheme = tokens.colorScheme)
        val merged = parent.merge(colorChild)
        assertSame(tokens.colorScheme, merged.colorScheme)
        assertSame(tokens.typography, merged.typography)
        assertSame(tokens.shapes, merged.shapes)
        assertSame(tokens.spacings, merged.spacings)
        assertSame(tokens.sizes, merged.sizes)
        assertEquals(merged, merged.merge(NomiThemeOverrides()))
        val replacement = ResolvedNomiTheme(requestedMode = DarkMode.Dark, isDark = true)
        val child = NomiThemeOverrides(
            typography = replacement.typography,
            shapes = replacement.shapes,
            spacings = replacement.spacings,
            sizes = replacement.sizes,
        )
        val result = merged.merge(child)
        assertSame(replacement.typography, result.typography)
        assertSame(replacement.shapes, result.shapes)
        assertSame(replacement.spacings, result.spacings)
        assertSame(replacement.sizes, result.sizes)
        assertSame(tokens.colorScheme, result.colorScheme)
        val nextColors = tokens.colorScheme.copy(primary = Color.Transparent)
        assertSame(
            nextColors,
            merged.merge(NomiThemeOverrides(colorScheme = nextColors)).colorScheme,
        )
    }

    /**
     * 新 root 继承父模式与系统，仅继承父明确非颜色组；父完整色板不能残留。
     */
    @Test
    fun nestedRootInheritsModeSystemAndExplicitNonColorGroupsOnly() {
        val tokens = customResolvedTheme()
        val groups = NomiThemeOverrides(
            typography = tokens.typography,
            shapes = tokens.shapes,
            spacings = tokens.spacings,
            sizes = tokens.sizes,
        )
        val colorInputs = listOf(
            NomiThemeOverrides(colorScheme = tokens.colorScheme),
            NomiThemeOverrides(colorScheme = tokens.colorScheme.copy(primary = Color.Magenta)),
        )
        colorInputs.forEach { colorInput ->
            runComposeUiTest {
                val recorder = RecordingDesignSystem()
                val snapshots = mutableListOf<ResolvedNomiTheme>()
                val systems = mutableListOf<String>()
                setContent {
                    NomiTheme(
                        darkMode = DarkMode.Dark,
                        designSystem = recorder,
                        overrides = colorInput.merge(groups),
                    ) {
                        Snapshot(snapshots, systems)
                        NomiTheme { Snapshot(snapshots, systems) }
                        Snapshot(snapshots, systems)
                    }
                }
                runOnIdle {
                    assertEquals(listOf("recording", "recording", "recording"), systems)
                    assertEquals(
                        listOf(DarkMode.Dark, DarkMode.Dark, DarkMode.Dark),
                        snapshots.map { it.requestedMode })
                    assertEquals(listOf(true, true, true), snapshots.map { it.isDark })
                    val expectedChild = NomiThemeOverrides(
                        typography = tokens.typography,
                        shapes = tokens.shapes,
                        spacings = tokens.spacings,
                        sizes = tokens.sizes,
                    )
                    assertEquals(expectedChild, snapshots[1].overrides)
                    assertEquals(defaultDarkNomiColorScheme(), snapshots[1].colorScheme)
                    assertSame(tokens.typography, snapshots[1].typography)
                    assertSame(tokens.shapes, snapshots[1].shapes)
                    assertSame(tokens.spacings, snapshots[1].spacings)
                    assertSame(tokens.sizes, snapshots[1].sizes)
                    assertEquals(snapshots.first(), snapshots.last())
                    assertEquals(expectedChild, recorder.requests[1].overrides)
                }
            }
        }
    }

    /**
     * 父 renderer 默认投影中的排版形状尺寸不能变成子 root 的显式输入。
     *
     * @return 当前平台的 Compose 测试执行结果，供测试框架等待组合断言与环境清理完成。
     */
    @Test
    fun nestedRootNeverPromotesDefaultProjectionToExplicitInput() = runComposeUiTest {
        val parent = RecordingDesignSystem(name = "parent", nativeTheme = customResolvedTheme())
        val childDefaults = ResolvedNomiTheme(requestedMode = DarkMode.Dark, isDark = true)
        val child = RecordingDesignSystem(name = "child", nativeTheme = childDefaults)
        val snapshots = mutableListOf<ResolvedNomiTheme>()
        setContent {
            NomiTheme(darkMode = DarkMode.Dark, designSystem = parent) {
                NomiTheme(designSystem = child) { Snapshot(snapshots) }
            }
        }
        runOnIdle {
            assertEquals(NomiThemeOverrides(), child.requests.single().overrides)
            assertEquals(DarkMode.Dark, child.requests.single().requestedMode)
            assertEquals(childDefaults, snapshots.single())
        }
    }

    /**
     * 局部 Override 保留全部明确组；子色板替换父色板，并在返回后恢复。
     *
     * @return 当前平台的 Compose 测试执行结果，供测试框架等待组合断言与环境清理完成。
     */
    @Test
    fun overrideInheritsAllExplicitGroupsAndChildColorsReplacePalette() = runComposeUiTest {
        val tokens = customResolvedTheme()
        val replacement = tokens.colorScheme.copy(primary = Color.Blue, warning = Color.Yellow)
        val recorder = RecordingDesignSystem()
        val snapshots = mutableListOf<ResolvedNomiTheme>()
        setContent {
            NomiTheme(
                darkMode = DarkMode.Light,
                designSystem = recorder,
                overrides = NomiThemeOverrides(
                    colorScheme = tokens.colorScheme,
                    typography = tokens.typography,
                    shapes = tokens.shapes,
                    spacings = tokens.spacings,
                    sizes = tokens.sizes,
                ),
            ) {
                NomiThemeOverride {
                    Snapshot(snapshots)
                    NomiThemeOverride(overrides = NomiThemeOverrides(colorScheme = replacement)) {
                        Snapshot(snapshots)
                    }
                    Snapshot(snapshots)
                }
            }
        }
        runOnIdle {
            assertEquals(
                listOf(tokens.colorScheme.primary, Color.Blue, tokens.colorScheme.primary),
                snapshots.map { it.colorScheme.primary })
            assertEquals(replacement, snapshots[1].colorScheme)
            assertSame(tokens.colorScheme, snapshots[0].overrides.colorScheme)
            snapshots.forEach { snapshot ->
                assertSame(tokens.typography, snapshot.typography)
                assertSame(tokens.shapes, snapshot.shapes)
                assertSame(tokens.spacings, snapshot.spacings)
                assertSame(tokens.sizes, snapshot.sizes)
            }
            assertEquals(snapshots.first(), snapshots.last())
        }
    }

    /**
     * 显式五组参数完整保留同一对象，不经原生往返而遗漏额外字段。
     *
     * @return 当前平台的 Compose 测试执行结果，供测试框架等待组合断言与环境清理完成。
     */
    @Test
    fun explicitGroupsRemainExactObjects() = runComposeUiTest {
        val tokens = customResolvedTheme()
        val snapshots = mutableListOf<ResolvedNomiTheme>()
        setContent {
            NomiTheme(
                darkMode = DarkMode.Dark,
                designSystem = RecorderWithoutNativeTheme,
                overrides = NomiThemeOverrides(
                    colorScheme = tokens.colorScheme,
                    typography = tokens.typography,
                    shapes = tokens.shapes,
                    spacings = tokens.spacings,
                    sizes = tokens.sizes,
                ),
            ) { Snapshot(snapshots) }
        }
        runOnIdle {
            val actual = snapshots.single()
            assertSame(tokens.colorScheme, actual.colorScheme)
            assertSame(tokens.typography, actual.typography)
            assertSame(tokens.shapes, actual.shapes)
            assertSame(tokens.spacings, actual.spacings)
            assertSame(tokens.sizes, actual.sizes)
            assertEquals(DarkMode.Dark, actual.requestedMode)
            assertEquals(true, actual.isDark)
            assertEquals(
                NomiThemeOverrides(
                    colorScheme = tokens.colorScheme,
                    typography = tokens.typography,
                    shapes = tokens.shapes,
                    spacings = tokens.spacings,
                    sizes = tokens.sizes,
                ),
                actual.overrides,
            )
        }
    }
}

/**
 * 使用 Nomi 默认基线、不做原生往返的录制型设计系统，供核心主题契约用例显式提供。
 */
private val RecorderWithoutNativeTheme = RecordingDesignSystem()

/**
 * 提供各组都明显偏离默认值的完整主题，用于验证整组输入保真。
 *
 * @return 基于五组独立默认工厂、定制颜色、排版、形状、间距和尺寸的新主题值。
 */
private fun customResolvedTheme(): ResolvedNomiTheme {
    val defaults = ResolvedNomiTheme(
        requestedMode = DarkMode.Light,
        isDark = false,
        colorScheme = defaultLightNomiColorScheme(),
        typography = defaultNomiTypography(),
        shapes = defaultNomiShapes(),
        spacings = defaultSpacings(),
        sizes = defaultNomiSizes(),
    )
    val customText = TextStyle(
        color = Color.Cyan,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 29.sp,
        lineHeight = 39.sp,
        letterSpacing = 1.7.sp,
    )
    return defaults.copy(
        colorScheme = defaults.colorScheme.copy(
            primary = Color.Green,
            warning = Color.Cyan,
            surfaceLower = Color.Red
        ),
        typography = defaults.typography.copy(
            displaySmall = customText,
            bodyMedium = customText,
            labelSmall = customText
        ),
        shapes = defaults.shapes.copy(
            tiny = RoundedCornerShape(3.dp),
            colossal = RoundedCornerShape(53.dp)
        ),
        spacings = defaults.spacings.copy(tiny = 3.dp, colossal = 59.dp),
        sizes = defaults.sizes.copy(
            icon = defaults.sizes.icon.copy(tiny = 17.dp),
            strokes = defaults.sizes.strokes.copy(thick = 7.dp)
        ),
    )
}

/**
 * 组合期捕获主题与系统身份，组合提交后追加快照，不在副作用中读取 Local。
 *
 * @param themes 接收当前已解析主题的可变列表，每次提交追加一项。
 * @param systems 可选的系统标识记录列表；null 表示不记录标识。
 */
@Composable
private fun Snapshot(themes: MutableList<ResolvedNomiTheme>, systems: MutableList<String>? = null) {
    val theme = LocalResolvedNomiTheme.current
    val system = requireNomiDesignSystem().id.value
    SideEffect {
        themes += theme
        systems?.add(system)
    }
}
