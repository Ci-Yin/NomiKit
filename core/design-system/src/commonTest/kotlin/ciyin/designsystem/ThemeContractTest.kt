package ciyin.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ciyin.designsystem.runtime.LocalNomiContentColor
import ciyin.designsystem.runtime.LocalNomiTextStyle
import ciyin.designsystem.runtime.LocalResolvedNomiTheme
import ciyin.designsystem.runtime.NomiThemeRequest
import ciyin.designsystem.runtime.ProvideNomiContentStyle
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
import ciyin.designsystem.theme.defaultStrokes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * 原生优先主题请求、稀疏覆盖、投影及中立 slot 边界契约。
 */
@OptIn(ExperimentalTestApi::class)
class ThemeContractTest {
    /**
     * Nomi 自有默认令牌保留稳定基线，但不作为其它 renderer 的默认输入。
     */
    @Test
    fun defaultLightTokensKeepStableBaseline() {
        val colors = defaultLightNomiColorScheme()
        val spacings = defaultSpacings()
        assertEquals(Color(0xFF0284C7), colors.primary)
        assertEquals(Color(0xFFF0FBFF), colors.onPrimary)
        assertEquals(Color(0xFFFFFBFE), colors.background)
        assertEquals(Color(0xFFF3EDF7), colors.surface)
        assertEquals(Color(0xFF1C1B1F), colors.textPrimary)
        assertEquals(12.dp, spacings.medium)
    }

    /**
     * 未包装的 Local 使用固定浅色五组，同时公开 getter 与同一对象保持身份一致。
     *
     * @return 当前平台的 Compose 测试执行结果，供测试框架等待组合读取与身份断言完成。
     */
    @Test
    fun unwrappedLocalUsesFixedLightWithSystemRequest() = runComposeUiTest {
        var observed: ResolvedNomiTheme? = null
        setContent {
            val local = LocalResolvedNomiTheme.current
            val darkMode = NomiTheme.darkMode
            val colors = NomiTheme.colorScheme
            val typography = NomiTheme.typography
            val shapes = NomiTheme.shapes
            val spacings = NomiTheme.spacings
            val sizes = NomiTheme.sizes
            SideEffect {
                observed = local
                assertEquals(DarkMode.System, local.requestedMode)
                assertEquals(false, local.isDark)
                assertEquals(NomiThemeOverrides(), local.overrides)
                assertEquals(defaultLightNomiColorScheme(), local.colorScheme)
                assertEquals(defaultNomiTypography(), local.typography)
                assertEquals(defaultNomiShapes(), local.shapes)
                assertEquals(defaultSpacings(), local.spacings)
                assertEquals(defaultNomiSizes(), local.sizes)
                assertSame(local.colorScheme, colors)
                assertSame(local.typography, typography)
                assertSame(local.shapes, shapes)
                assertSame(local.spacings, spacings)
                assertSame(local.sizes, sizes)
                assertEquals(DarkMode.System, darkMode)
            }
        }
        runOnIdle { assertEquals(defaultLightNomiColorScheme(), observed?.colorScheme) }
    }

    /**
     * 适配器返回错误的模式、明暗值和覆盖来源时，核心仍以本次 request 规范化 metadata。
     *
     * @return 当前平台的 Compose 测试执行结果，供测试框架等待适配器回调与核心断言完成。
     */
    @Test
    fun flattenNormalizesAdapterMetadataToRequest() = runComposeUiTest {
        val wrongTheme = ResolvedNomiTheme(
            requestedMode = DarkMode.Light,
            isDark = false,
            overrides = NomiThemeOverrides(accentColor = Color.Green),
        )
        val designSystem = RecordingDesignSystem(nativeTheme = wrongTheme)
        val requestOverrides = NomiThemeOverrides(accentColor = Color.Red)
        val snapshots = mutableListOf<ResolvedNomiTheme>()
        setContent {
            NomiTheme(
                designSystem = designSystem,
                darkMode = DarkMode.Dark,
                overrides = requestOverrides,
            ) {
                RecordTheme(snapshots)
            }
        }
        runOnIdle {
            val callback = designSystem.callbackThemes.single()
            assertEquals(DarkMode.Light, callback.requestedMode)
            assertEquals(false, callback.isDark)
            assertSame(wrongTheme.overrides, callback.overrides)
            assertEquals(Color.Green, callback.overrides.accentColor)
            val resolved = snapshots.single()
            assertSame(callback.colorScheme, resolved.colorScheme)
            assertSame(callback.typography, resolved.typography)
            assertSame(callback.shapes, resolved.shapes)
            assertSame(callback.spacings, resolved.spacings)
            assertSame(callback.sizes, resolved.sizes)
            assertSame(designSystem.requests.single().overrides, resolved.overrides)
            assertEquals(DarkMode.Dark, resolved.requestedMode)
            assertEquals(true, resolved.isDark)
            assertEquals(requestOverrides, resolved.overrides)
            assertEquals(
                listOf(
                    NomiThemeRequest(
                        requestedMode = DarkMode.Dark,
                        isDark = true,
                        overrides = requestOverrides,
                    ),
                ),
                designSystem.requests,
            )
            assertEquals(Color.Red, resolved.colorScheme.primary)
        }
    }

    /**
     * 空覆盖继承父值；只有子级明确提供的字段参与覆盖，包括透明强调色。
     */
    @Test
    fun sparseOverridesMergeOnlyExplicitChildFields() {
        val spacing = defaultSpacings().copy(medium = 19.dp)
        val sizes = defaultNomiSizes().copy(
            strokes = defaultStrokes().copy(
                thin = 2.dp,
                medium = 3.dp,
                thick = 5.dp
            )
        )
        val parent = NomiThemeOverrides(accentColor = Color.Red, spacings = spacing, sizes = sizes)
        assertEquals(parent, parent.merge(NomiThemeOverrides()))
        assertEquals(parent, NomiThemeOverrides().merge(parent))
        assertEquals(
            parent.copy(accentColor = Color.Transparent),
            parent.merge(NomiThemeOverrides(accentColor = Color.Transparent)),
        )
        val childSpacing = defaultSpacings().copy(medium = 23.dp)
        assertEquals(
            parent.copy(spacings = childSpacing),
            parent.merge(NomiThemeOverrides(spacings = childSpacing)),
        )
        val childSizes = defaultNomiSizes()
        assertEquals(
            parent.copy(sizes = childSizes),
            parent.merge(NomiThemeOverrides(sizes = childSizes))
        )
    }

    /**
     * 根主题必须清除外部 slot，而 adapter callback 的投影必须成为 NomiTheme 的真实值。
     *
     * @return 当前平台的 Compose 测试执行结果，供测试框架等待组合断言与环境清理完成。
     */
    @Test
    fun rootResetsSlotsAndPublishesAdapterProjection() = runComposeUiTest {
        val baseline = ResolvedNomiTheme(
            requestedMode = DarkMode.Light,
            isDark = false,
            colorScheme = defaultLightNomiColorScheme(),
            typography = defaultNomiTypography(),
            shapes = defaultNomiShapes(),
            spacings = defaultSpacings(),
            sizes = defaultNomiSizes(),
        )
        val projected = baseline.copy(colorScheme = baseline.colorScheme.copy(primary = Color.Cyan))
        val designSystem = RecordingDesignSystem(nativeTheme = projected)
        val themes = mutableListOf<ResolvedNomiTheme>()
        val slots = mutableListOf<Pair<Color, TextStyle>>()
        setContent {
            CompositionLocalProvider(
                LocalNomiContentColor provides Color.Red,
                LocalNomiTextStyle provides TextStyle(color = Color.Blue, fontSize = 29.sp),
            ) {
                NomiTheme(designSystem = designSystem, darkMode = DarkMode.Light) {
                    RecordTheme(themes)
                    RecordSlots(slots)
                }
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    NomiThemeRequest(
                        requestedMode = DarkMode.Light,
                        isDark = false,
                        overrides = NomiThemeOverrides(),
                    ),
                ),
                designSystem.requests,
            )
            assertEquals(listOf(Color.Unspecified to TextStyle.Default), slots)
            val resolved = themes.single()
            assertEquals(DarkMode.Light, resolved.requestedMode)
            assertEquals(false, resolved.isDark)
            assertEquals(NomiThemeOverrides(), resolved.overrides)
            assertSame(projected.colorScheme, resolved.colorScheme)
            assertSame(projected.typography, resolved.typography)
            assertSame(projected.shapes, resolved.shapes)
            assertSame(projected.spacings, resolved.spacings)
            assertSame(projected.sizes, resolved.sizes)
        }
    }

    /**
     * renderer 切换只继承显式覆盖，不把父 adapter 投影后的全部令牌当成子默认。
     *
     * @return 当前平台的 Compose 测试执行结果，供测试框架等待组合断言与环境清理完成。
     */
    @Test
    fun rendererOverrideInheritsExplicitOverridesButNotParentProjection() = runComposeUiTest {
        val defaults = ResolvedNomiTheme(
            requestedMode = DarkMode.Light,
            isDark = false,
            colorScheme = defaultLightNomiColorScheme(),
            typography = defaultNomiTypography(),
            shapes = defaultNomiShapes(),
            spacings = defaultSpacings(),
            sizes = defaultNomiSizes(),
        )
        val parentTheme = defaults.copy(
            colorScheme = defaults.colorScheme.copy(primary = Color.Red, surface = Color.Yellow),
            spacings = defaultSpacings().copy(large = 29.dp),
        )
        val childTheme = defaults.copy(
            colorScheme = defaults.colorScheme.copy(primary = Color.Blue, surface = Color.Green),
            spacings = defaultSpacings().copy(large = 7.dp),
        )
        val parent = RecordingDesignSystem(name = "parent", nativeTheme = parentTheme)
        val child = RecordingDesignSystem(name = "child", nativeTheme = childTheme)
        val explicit = NomiThemeOverrides(accentColor = Color.Magenta)
        val snapshots = mutableListOf<ResolvedNomiTheme>()
        val systems = mutableListOf<String>()
        setContent {
            NomiTheme(designSystem = parent, darkMode = DarkMode.Dark, overrides = explicit) {
                RecordTheme(snapshots)
                RecordSystem(systems)
                NomiThemeOverride(designSystem = child) {
                    RecordTheme(snapshots)
                    RecordSystem(systems)
                }
                RecordTheme(snapshots)
                RecordSystem(systems)
            }
        }
        runOnIdle {
            assertEquals(listOf("parent", "child", "parent"), systems)
            assertEquals(
                listOf(
                    NomiThemeRequest(
                        requestedMode = DarkMode.Dark,
                        isDark = true,
                        overrides = explicit
                    )
                ),
                child.requests,
            )
            assertEquals(listOf(explicit, explicit, explicit), snapshots.map { it.overrides })
            assertEquals(
                listOf(Color.Yellow, Color.Green, Color.Yellow),
                snapshots.map { it.colorScheme.surface })
            assertEquals(listOf(29.dp, 7.dp, 29.dp), snapshots.map { it.spacings.large })
            assertEquals(
                listOf(Color.Magenta, Color.Magenta, Color.Magenta),
                snapshots.map { it.colorScheme.primary })
            assertEquals(snapshots.first(), snapshots.last())
        }
    }

    /**
     * 显式模式切换重新建立对应的 adapter 默认，同时合并而非清空父级显式覆盖。
     *
     * @return 当前平台的 Compose 测试执行结果，供测试框架等待组合断言与环境清理完成。
     */
    @Test
    fun explicitModeOverrideMergesSparseValuesAndRestoresParent() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val spacing = defaultSpacings().copy(large = 21.dp)
        val parent = NomiThemeOverrides(accentColor = Color.Red, spacings = spacing)
        val child = NomiThemeOverrides(accentColor = Color.Blue)
        val snapshots = mutableListOf<ResolvedNomiTheme>()
        setContent {
            NomiTheme(designSystem = designSystem, darkMode = DarkMode.Light, overrides = parent) {
                RecordTheme(snapshots)
                NomiThemeOverride(darkMode = DarkMode.Dark, overrides = child) {
                    RecordTheme(snapshots)
                }
                RecordTheme(snapshots)
            }
        }
        runOnIdle {
            assertEquals(
                listOf(DarkMode.Light, DarkMode.Dark, DarkMode.Light),
                snapshots.map { it.requestedMode })
            assertEquals(listOf(false, true, false), snapshots.map { it.isDark })
            assertEquals(
                listOf(parent, parent.merge(child), parent),
                snapshots.map { it.overrides })
            assertEquals(listOf(spacing, spacing, spacing), snapshots.map { it.spacings })
            assertEquals(defaultDarkNomiColorScheme().surface, snapshots[1].colorScheme.surface)
            assertEquals(snapshots.first(), snapshots.last())
        }
    }

    /**
     * 局部主题保留明确 slot，嵌套根主题重置它，离开根和覆盖后恢复父级。
     *
     * @return 当前平台的 Compose 测试执行结果，供测试框架等待组合断言与环境清理完成。
     */
    @Test
    fun overridePreservesExplicitSlotsWhileNestedRootResetsThem() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val snapshots = mutableListOf<Pair<Color, TextStyle>>()
        val inherited = TextStyle(color = Color.Green, fontSize = 27.sp)
        setContent {
            NomiTheme(designSystem = designSystem) {
                ProvideNomiContentStyle(color = Color.Magenta, textStyle = inherited) {
                    RecordSlots(snapshots)
                    NomiThemeOverride(overrides = NomiThemeOverrides(accentColor = Color.Blue)) {
                        RecordSlots(snapshots)
                        NomiTheme(designSystem = designSystem) { RecordSlots(snapshots) }
                        RecordSlots(snapshots)
                    }
                    RecordSlots(snapshots)
                }
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    Color.Magenta to inherited,
                    Color.Magenta to inherited,
                    Color.Unspecified to TextStyle.Default,
                    Color.Magenta to inherited,
                    Color.Magenta to inherited,
                ),
                snapshots,
            )
        }
    }
}

/**
 * 先在组合函数中读取主题，在提交后的副作用中校验公开读模型一致性并记录快照。
 *
 * @param snapshots 接收当前已解析主题的可变列表；每次成功提交且断言通过后追加一项。
 */
@Composable
private fun RecordTheme(snapshots: MutableList<ResolvedNomiTheme>) {
    val resolved = LocalResolvedNomiTheme.current
    val mode = NomiTheme.darkMode
    val colors = NomiTheme.colorScheme
    val typography = NomiTheme.typography
    val shapes = NomiTheme.shapes
    val spacings = NomiTheme.spacings
    val sizes = NomiTheme.sizes
    SideEffect {
        assertEquals(resolved.requestedMode, mode)
        assertSame(resolved.colorScheme, colors)
        assertSame(resolved.typography, typography)
        assertSame(resolved.shapes, shapes)
        assertSame(resolved.spacings, spacings)
        assertSame(resolved.sizes, sizes)
        snapshots += resolved
    }
}

/**
 * 记录当前 renderer 身份，用提交顺序证明覆盖恢复。
 *
 * @param snapshots 接收当前设计系统标识的可变列表，每次组合提交追加一项。
 */
@Composable
private fun RecordSystem(snapshots: MutableList<String>) {
    val id = requireNomiDesignSystem().id.value
    SideEffect { snapshots += id }
}

/**
 * 记录实际中立 slot 的颜色与样式，不在非组合 callback 中读取 Local。
 *
 * @param snapshots 接收内容色与文本样式二元组的可变列表，每次组合提交追加一项。
 */
@Composable
private fun RecordSlots(snapshots: MutableList<Pair<Color, TextStyle>>) {
    val color = LocalNomiContentColor.current
    val style = LocalNomiTextStyle.current
    SideEffect { snapshots += color to style }
}
