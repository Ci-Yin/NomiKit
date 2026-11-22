package ciyin.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.scaffold.NomiScaffold
import ciyin.designsystem.component.scaffold.NomiScaffoldColors
import ciyin.designsystem.component.scaffold.NomiTopAppBar
import ciyin.designsystem.component.scaffold.NomiTopAppBarColors
import ciyin.designsystem.component.scaffold.NomiTopAppBarSpec
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 页面脚手架只派发明确参数，不将投影令牌反灌为原生默认。
 */
@OptIn(ExperimentalTestApi::class)
class ScaffoldDispatchContractTest {
    /**
     * 默认调用保留 Unspecified 颜色，并按顶栏、主体、底栏组合三个槽位。
     */
    @Test
    fun defaultsRemainSparseAndSlotsAreComposed() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiScaffold(
                    topBar = { NomiText("top") },
                    bottomBar = { NomiText("bottom") },
                ) {
                    NomiText("body")
                }
            }
        }
        runOnIdle {
            assertEquals(
                listOf(RecordedScaffold(colors = NomiScaffoldColors())),
                designSystem.scaffolds,
            )
            assertEquals(setOf("top", "body", "bottom"), designSystem.texts.keys)
        }
    }

    /**
     * 显式颜色逐字段原样透传，Transparent 仍是有效覆盖而不是未指定。
     */
    @Test
    fun explicitColorsArePassedThroughUnchanged() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val colors = NomiScaffoldColors(
            containerColor = Color(0xFF102030),
            contentColor = Color.Transparent,
        )
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiScaffold(colors = colors) {
                    NomiText("body")
                }
            }
        }
        runOnIdle {
            assertEquals(listOf(RecordedScaffold(colors = colors)), designSystem.scaffolds)
        }
    }

    /**
     * 顶栏默认调用保留 Unspecified 颜色、不提供导航图标，只组合标题槽。
     */
    @Test
    fun topAppBarDefaultsRemainSparse() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiTopAppBar(title = { NomiText("title") })
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    RecordedTopAppBar(
                        colors = NomiTopAppBarColors(),
                        hasNavigationIcon = false
                    )
                ),
                designSystem.topBars,
            )
            assertEquals(setOf("title"), designSystem.texts.keys)
        }
    }

    /**
     * 顶栏显式颜色、导航图标、操作槽与专属参数原样透传。
     */
    @Test
    fun topAppBarExplicitParametersAreDispatchedUnchanged() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val colors = NomiTopAppBarColors(
            containerColor = Color(0xFF102030),
            titleContentColor = Color.Transparent,
            navigationIconContentColor = Color(0xFF203040),
            actionIconContentColor = Color(0xFF304050),
        )
        val spec = RecordingTopAppBarSpec(marker = "top")
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiTopAppBar(
                    title = { NomiText("title") },
                    navigationIcon = { NomiText("nav") },
                    actions = { NomiText("action") },
                    colors = colors,
                    nativeSpec = spec,
                )
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    RecordedTopAppBar(
                        colors = colors,
                        hasNavigationIcon = true,
                        nativeSpec = spec,
                    )
                ),
                designSystem.topBars,
            )
            assertEquals(setOf("title", "nav", "action"), designSystem.texts.keys)
        }
    }
}

/**
 * 测试用顶栏专属参数，模拟 adapter 模块定义的具体 spec。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class RecordingTopAppBarSpec(val marker: String) : NomiTopAppBarSpec