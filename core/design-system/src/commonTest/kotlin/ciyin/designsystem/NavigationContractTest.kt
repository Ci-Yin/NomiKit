package ciyin.designsystem

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.navigation.NomiNavigationItemColors
import ciyin.designsystem.component.navigation.NomiNavigationSuite
import ciyin.designsystem.component.navigation.NomiNavigationSuiteType
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 验证导航套件入口原样透传布局、容器色与两个导航项分组；核心不按能力拦截。
 */
@OptIn(ExperimentalTestApi::class)
class NavigationContractTest {
    /**
     * 验证 Bar 布局把主导航项与底部导航项按声明顺序交给 renderer，并保留槽位与稀疏颜色。
     */
    @Test
    fun suiteDispatchesGroupedItemsToRenderer() = runComposeUiTest {
        val recorder = RecordingDesignSystem()
        var contentComposed = false
        setContent {
            NomiTheme(designSystem = recorder) {
                NomiNavigationSuite(
                    layoutType = NomiNavigationSuiteType.Bar,
                    items = {
                        NavigationItem(
                            selected = true,
                            onClick = {},
                            icon = { },
                            label = { },
                        )
                        NavigationItem(
                            selected = false,
                            onClick = {},
                            modifier = Modifier.testTag("second"),
                            badge = { },
                            colors = NomiNavigationItemColors(
                                selectedContentColor = Color.Red,
                                indicatorColor = Color.Blue,
                            ),
                            interactionSource = remember { MutableInteractionSource() },
                        )
                    },
                    footerItems = {
                        NavigationItem(selected = false, onClick = {}, label = { })
                    },
                    content = { SideEffect { contentComposed = true } },
                )
            }
        }
        runOnIdle {
            val suite = recorder.navigations.single()
            assertEquals(NomiNavigationSuiteType.Bar, suite.layoutType)
            assertEquals(Color.Unspecified, suite.containerColor)
            assertEquals(2, suite.items.size)
            val selectedItem = suite.items[0]
            assertTrue(selectedItem.selected)
            assertFalse(selectedItem.hasModifier)
            assertTrue(selectedItem.hasIcon)
            assertTrue(selectedItem.hasLabel)
            assertFalse(selectedItem.hasBadge)
            assertFalse(selectedItem.hasInteractionSource)
            val configuredItem = suite.items[1]
            assertFalse(configuredItem.selected)
            assertTrue(configuredItem.hasModifier)
            assertTrue(configuredItem.hasBadge)
            assertFalse(configuredItem.hasIcon)
            assertTrue(configuredItem.hasInteractionSource)
            assertEquals(Color.Red, configuredItem.colors.selectedContentColor)
            assertEquals(Color.Blue, configuredItem.colors.indicatorColor)
            assertEquals(1, suite.footerItems.size)
            assertTrue(suite.footerItems.single().hasLabel)
            assertTrue(contentComposed)
        }
    }

    /**
     * 验证显式容器色原样交给 renderer，由实现映射到原生导航外壳。
     */
    @Test
    fun containerColorIsForwardedToRenderer() = runComposeUiTest {
        val recorder = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = recorder) {
                NomiNavigationSuite(
                    layoutType = NomiNavigationSuiteType.Bar,
                    containerColor = Color.Red,
                    items = { NavigationItem(selected = false, onClick = {}) },
                    content = {},
                )
            }
        }
        runOnIdle {
            assertEquals(Color.Red, recorder.navigations.single().containerColor)
        }
    }

    /**
     * 验证 Rail 布局与空槽位原样派发，底部组保持为空列表。
     */
    @Test
    fun railDispatchesSparseItemAndEmptyFooter() = runComposeUiTest {
        val recorder = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = recorder) {
                NomiNavigationSuite(
                    layoutType = NomiNavigationSuiteType.Rail,
                    items = { NavigationItem(selected = false, onClick = {}) },
                    content = {},
                )
            }
        }
        runOnIdle {
            val suite = recorder.navigations.single()
            assertEquals(NomiNavigationSuiteType.Rail, suite.layoutType)
            val item = suite.items.single()
            assertFalse(item.hasIcon)
            assertFalse(item.hasBadge)
            assertFalse(item.hasLabel)
            assertTrue(suite.footerItems.isEmpty())
        }
    }

    /**
     * 验证 Drawer 布局原样交给 renderer，不因能力差异在入口失败。
     */
    @Test
    fun drawerDispatchesToRenderer() = runComposeUiTest {
        val recorder = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = recorder) {
                NomiNavigationSuite(
                    layoutType = NomiNavigationSuiteType.Drawer,
                    items = { NavigationItem(selected = true, onClick = { }, label = { }) },
                    content = {},
                )
            }
        }
        runOnIdle {
            assertEquals(NomiNavigationSuiteType.Drawer, recorder.navigations.single().layoutType)
        }
    }
}
