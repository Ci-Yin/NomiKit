package ciyin.designsystem

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.NomiComponentCapabilities
import ciyin.designsystem.component.navigation.NomiNavigationItemColors
import ciyin.designsystem.component.navigation.NomiNavigationSuite
import ciyin.designsystem.component.navigation.NomiNavigationSuiteType
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * 验证导航套件入口按能力分派、保留两个导航项分组，并在不支持时提前失败。
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
     * 验证默认能力支持 Rail，槽位全空时仍派出空导航项且底部组保持为空列表。
     */
    @Test
    fun railIsSupportedByDefaultAndEmptyFooterStaysEmpty() = runComposeUiTest {
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
     * 验证默认能力拒绝 Drawer，且在进入 renderer 前失败。
     */
    @Test
    fun unsupportedDrawerFailsBeforeRenderer() {
        val recorder = RecordingDesignSystem()
        val failure = assertFailsWith<IllegalArgumentException> {
            runComposeUiTest {
                setContent {
                    NomiTheme(designSystem = recorder) {
                        NomiNavigationSuite(
                            layoutType = NomiNavigationSuiteType.Drawer,
                            items = { NavigationItem(selected = false, onClick = {}) },
                            content = {},
                        )
                    }
                }
                waitForIdle()
            }
        }
        assertTrue(failure.message?.contains("Drawer") == true)
        assertTrue(recorder.navigations.isEmpty())
    }

    /**
     * 验证实现显式声明 Drawer 能力后，入口把布局交给 renderer。
     */
    @Test
    fun drawerDispatchesWhenCapabilityDeclared() = runComposeUiTest {
        val recorder = RecordingDesignSystem(
            capabilities = NomiComponentCapabilities(navigationDrawer = true),
        )
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
