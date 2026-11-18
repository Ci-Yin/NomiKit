package ciyin.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.button.NomiButtonSpec
import ciyin.designsystem.component.card.NomiCard
import ciyin.designsystem.component.card.NomiCardSpec
import ciyin.designsystem.component.list.NomiListItemSpec
import ciyin.designsystem.component.navigation.NomiNavigationItemSpec
import ciyin.designsystem.component.navigation.NomiNavigationSpec
import ciyin.designsystem.component.navigation.NomiNavigationSuite
import ciyin.designsystem.component.navigation.NomiNavigationSuiteType
import ciyin.designsystem.component.scaffold.NomiScaffold
import ciyin.designsystem.component.scaffold.NomiScaffoldSpec
import ciyin.designsystem.component.surface.NomiSurface
import ciyin.designsystem.component.surface.NomiSurfaceSpec
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.component.text.NomiTextSpec
import ciyin.designsystem.runtime.NomiNativeSpecs
import ciyin.designsystem.runtime.findSpec
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 各族入口把 nativeSpec 原样派发给 renderer，不解析、不改写、不补默认。
 */
@OptIn(ExperimentalTestApi::class)
class NativeSpecDispatchContractTest {
    /**
     * 文本入口原样派发文本专属参数。
     */
    @Test
    fun textDispatchesNativeSpec() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val spec = RecordingTextSpec(marker = "text")
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiText(text = "文本", nativeSpec = spec)
            }
        }
        runOnIdle { assertEquals(spec, designSystem.texts["文本"]?.nativeSpec) }
    }

    /**
     * 按钮入口原样派发按钮专属参数，并支持同族组合。
     */
    @Test
    fun buttonDispatchesNativeSpecAndComposes() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val first = RecordingButtonSpec(marker = "first")
        val second = OtherButtonSpec(marker = "second")
        val composed = first + second
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiButton(onClick = {}, nativeSpec = composed) { NomiText("按钮") }
            }
        }
        runOnIdle {
            val dispatched = designSystem.buttons.single().nativeSpec
            assertEquals(composed, dispatched)
            assertEquals(2, (dispatched as NomiNativeSpecs<*>).specs.size)
            assertEquals(first, dispatched.findSpec<RecordingButtonSpec>())
            assertEquals(second, dispatched.findSpec<OtherButtonSpec>())
            assertNull(dispatched.findSpec<RecordingScaffoldSpec>())
        }
    }

    /**
     * 表面入口原样派发表面专属参数。
     */
    @Test
    fun surfaceDispatchesNativeSpec() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val spec = RecordingSurfaceSpec(marker = "surface")
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiSurface(nativeSpec = spec) { NomiText("表面") }
            }
        }
        runOnIdle { assertEquals(spec, designSystem.surfaces.single().nativeSpec) }
    }

    /**
     * 卡片入口原样派发卡片专属参数。
     */
    @Test
    fun cardDispatchesNativeSpec() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val spec = RecordingCardSpec(marker = "card")
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiCard(nativeSpec = spec) { NomiText("卡片") }
            }
        }
        runOnIdle { assertEquals(spec, designSystem.cards.single().nativeSpec) }
    }

    /**
     * 导航套件与导航项分别原样派发各自的专属参数。
     */
    @Test
    fun navigationDispatchesSuiteAndItemSpecs() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val suiteSpec = RecordingNavigationSpec(marker = "suite")
        val itemSpec = RecordingNavigationItemSpec(marker = "item")
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiNavigationSuite(
                    layoutType = NomiNavigationSuiteType.Bar,
                    nativeSpec = suiteSpec,
                    items = {
                        NavigationItem(selected = true, onClick = {}, nativeSpec = itemSpec)
                    },
                    content = { NomiText("页面") },
                )
            }
        }
        runOnIdle {
            val suite = designSystem.navigations.single()
            assertEquals(suiteSpec, suite.nativeSpec)
            assertEquals(itemSpec, suite.items.single().nativeSpec)
        }
    }

    /**
     * 脚手架入口原样派发脚手架专属参数。
     */
    @Test
    fun scaffoldDispatchesNativeSpec() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val spec = RecordingScaffoldSpec(marker = "scaffold")
        setContent {
            NomiTheme(designSystem = designSystem, darkMode = DarkMode.Light) {
                NomiScaffold(nativeSpec = spec) { NomiText("页面") }
            }
        }
        runOnIdle { assertEquals(spec, designSystem.scaffolds.single().nativeSpec) }
    }
}

/**
 * 测试用文本专属参数。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class RecordingTextSpec(val marker: String) : NomiTextSpec

/**
 * 测试用按钮专属参数。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class RecordingButtonSpec(val marker: String) : NomiButtonSpec

/**
 * 同一族的第二个测试按钮专属参数，用于验证按类型隔离。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class OtherButtonSpec(val marker: String) : NomiButtonSpec

/**
 * 测试用表面专属参数。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class RecordingSurfaceSpec(val marker: String) : NomiSurfaceSpec

/**
 * 测试用卡片专属参数。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class RecordingCardSpec(val marker: String) : NomiCardSpec

/**
 * 测试用导航套件专属参数。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class RecordingNavigationSpec(val marker: String) : NomiNavigationSpec

/**
 * 测试用导航项专属参数。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class RecordingNavigationItemSpec(val marker: String) : NomiNavigationItemSpec

/**
 * 测试用脚手架专属参数。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class RecordingScaffoldSpec(val marker: String) : NomiScaffoldSpec
