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
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

/**
 * 直接主题参数、整组显式继承和冲突拒绝的完整契约。
 */
@OptIn(ExperimentalTestApi::class)
class DirectThemeContractTest {
    /**
     * 子整组颜色清除继承 accent，而子 accent 仍允许叠加父整组颜色。
     */
    @Test
    fun mergePreservesWholeGroupsAndTracksColorReplacement() {
        val tokens = customResolvedTheme()
        val parent = NomiThemeOverrides(
            accentColor = Color.Red,
            typography = tokens.typography,
            shapes = tokens.shapes,
            spacings = tokens.spacings,
            sizes = tokens.sizes,
        )
        val colorChild = NomiThemeOverrides(colorScheme = tokens.colorScheme)
        val merged = parent.merge(colorChild)
        assertEquals(Color.Unspecified, merged.accentColor)
        assertSame(tokens.colorScheme, merged.colorScheme)
        assertSame(tokens.typography, merged.typography)
        assertSame(tokens.shapes, merged.shapes)
        assertSame(tokens.spacings, merged.spacings)
        assertSame(tokens.sizes, merged.sizes)
        val accented = colorChild.merge(NomiThemeOverrides(accentColor = Color.Blue))
        assertSame(tokens.colorScheme, accented.colorScheme)
        assertEquals(Color.Blue, accented.accentColor)
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
    }

    /**
     * 新 root 继承父模式与 renderer，仅继承父明确非颜色组；父完整色板和 accent 都不能残留。
     */
    @Test
    fun nestedRootInheritsModeSystemAndExplicitNonColorGroupsOnly() {
        val tokens = customResolvedTheme()
        val colorInputs = listOf(
            NomiThemeOverrides(colorScheme = tokens.colorScheme),
            NomiThemeOverrides(accentColor = Color.Magenta),
        )
        colorInputs.forEach { colorInput ->
            runComposeUiTest {
                val recorder = RecordingDesignSystem()
                val snapshots = mutableListOf<ResolvedNomiTheme>()
                val systems = mutableListOf<String>()
                setContent {
                    NomiTheme(
                        darkMode = DarkMode.Dark,
                        typography = tokens.typography,
                        shapes = tokens.shapes,
                        spacings = tokens.spacings,
                        sizes = tokens.sizes,
                        designSystem = recorder,
                        overrides = colorInput,
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
     * 局部 Override 保留全部明确组；子色板替换清除父 accent，并在返回后恢复。
     *
     * @return 当前平台的 Compose 测试执行结果，供测试框架等待组合断言与环境清理完成。
     */
    @Test
    fun overrideInheritsAllExplicitGroupsAndChildColorsClearAccent() = runComposeUiTest {
        val tokens = customResolvedTheme()
        val replacement = tokens.colorScheme.copy(primary = Color.Blue, warning = Color.Yellow)
        val recorder = RecordingDesignSystem()
        val snapshots = mutableListOf<ResolvedNomiTheme>()
        setContent {
            NomiTheme(
                darkMode = DarkMode.Light,
                colorScheme = tokens.colorScheme,
                typography = tokens.typography,
                shapes = tokens.shapes,
                spacings = tokens.spacings,
                sizes = tokens.sizes,
                designSystem = recorder,
            ) {
                NomiThemeOverride(overrides = NomiThemeOverrides(accentColor = Color.Red)) {
                    Snapshot(snapshots)
                    NomiThemeOverride(colorScheme = replacement) { Snapshot(snapshots) }
                    Snapshot(snapshots)
                }
            }
        }
        runOnIdle {
            assertEquals(
                listOf(Color.Red, Color.Blue, Color.Red),
                snapshots.map { it.colorScheme.primary })
            assertEquals(
                listOf(Color.Red, Color.Unspecified, Color.Red),
                snapshots.map { it.overrides.accentColor })
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
     * 直接五组参数完整保留同一对象，不经原生往返而遗漏额外字段。
     *
     * @return 当前平台的 Compose 测试执行结果，供测试框架等待组合断言与环境清理完成。
     */
    @Test
    fun directGroupsRemainExactObjects() = runComposeUiTest {
        val tokens = customResolvedTheme()
        val snapshots = mutableListOf<ResolvedNomiTheme>()
        setContent {
            NomiTheme(
                darkMode = DarkMode.Dark,
                colorScheme = tokens.colorScheme,
                typography = tokens.typography,
                shapes = tokens.shapes,
                spacings = tokens.spacings,
                sizes = tokens.sizes,
                designSystem = RecorderWithoutNativeTheme,
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

    /**
     * root 与局部 Override 的五个同层同组重复入口都必须精确抛错，即使两份值相同。
     */
    @Test
    fun duplicateDirectAndOverridesGroupsFailAtBothBoundaries() {
        val tokens = customResolvedTheme()
        listOf(false, true).forEach { nested ->
            listOf("colorScheme", "typography", "shapes", "spacings", "sizes").forEach { group ->
                val failure = assertFailsWith<IllegalArgumentException> {
                    runComposeUiTest {
                        setContent {
                            if (nested) {
                                NomiTheme(designSystem = RecorderWithoutNativeTheme) {
                                    ConflictingGroup(group = group, tokens = tokens, nested = true)
                                }
                            } else {
                                ConflictingGroup(group = group, tokens = tokens, nested = false)
                            }
                        }
                        waitForIdle()
                    }
                }
                assertEquals(
                    "NomiTheme " + group + " 与 overrides." + group + " 不能同时指定",
                    failure.message
                )
            }
        }
    }

    /**
     * 完整颜色与 accent 同层不合法，两者都在 overrides 内也不得绕过检查。
     */
    @Test
    fun sameLayerFullColorAndAccentFailAtBothBoundaries() {
        val colors = customResolvedTheme().colorScheme
        listOf(false, true).forEach { nested ->
            listOf(false, true).forEach { bundled ->
                val overrides = NomiThemeOverrides(
                    accentColor = Color.Red,
                    colorScheme = if (bundled) colors else null,
                )
                val failure = assertFailsWith<IllegalArgumentException> {
                    runComposeUiTest {
                        setContent {
                            if (nested) {
                                NomiTheme(designSystem = RecorderWithoutNativeTheme) {
                                    NomiThemeOverride(
                                        colorScheme = if (bundled) null else colors,
                                        overrides = overrides
                                    ) {}
                                }
                            } else {
                                NomiTheme(
                                    designSystem = RecorderWithoutNativeTheme,
                                    colorScheme = if (bundled) null else colors,
                                    overrides = overrides,
                                ) {}
                            }
                        }
                        waitForIdle()
                    }
                }
                assertEquals(
                    "NomiTheme colorScheme 与 accentColor 不能在同一层同时指定",
                    failure.message
                )
            }
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

/**
 * 构造同组重复来源；两个边界使用完全相同的数据与断言标准。
 *
 * @param group 要重复提供的组名，只接受 colorScheme、typography、shapes、spacings、sizes。
 * @param tokens 为直接参数与 overrides 提供同一组对象的主题夹具。
 * @param nested 为 true 时使用局部 NomiThemeOverride，否则使用根 NomiTheme。
 * @throws IllegalStateException group 不属于支持的五组时抛出。
 * @throws IllegalArgumentException 支持组在主题边界被识别为同层重复输入时抛出，此异常是用例的预期结果。
 */
@Composable
private fun ConflictingGroup(group: String, tokens: ResolvedNomiTheme, nested: Boolean) {
    val overrides = when (group) {
        "colorScheme" -> NomiThemeOverrides(colorScheme = tokens.colorScheme)
        "typography" -> NomiThemeOverrides(typography = tokens.typography)
        "shapes" -> NomiThemeOverrides(shapes = tokens.shapes)
        "spacings" -> NomiThemeOverrides(spacings = tokens.spacings)
        "sizes" -> NomiThemeOverrides(sizes = tokens.sizes)
        else -> error("Unknown fixture group: " + group)
    }
    if (nested) {
        NomiThemeOverride(
            overrides = overrides,
            colorScheme = if (group == "colorScheme") tokens.colorScheme else null,
            typography = if (group == "typography") tokens.typography else null,
            shapes = if (group == "shapes") tokens.shapes else null,
            spacings = if (group == "spacings") tokens.spacings else null,
            sizes = if (group == "sizes") tokens.sizes else null,
        ) {}
    } else {
        NomiTheme(
            designSystem = RecorderWithoutNativeTheme,
            colorScheme = if (group == "colorScheme") tokens.colorScheme else null,
            typography = if (group == "typography") tokens.typography else null,
            shapes = if (group == "shapes") tokens.shapes else null,
            spacings = if (group == "spacings") tokens.spacings else null,
            sizes = if (group == "sizes") tokens.sizes else null,
            overrides = overrides,
        ) {}
    }
}
