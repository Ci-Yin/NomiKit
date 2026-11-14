package ciyin.designsystem.nomi.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.navigation.NomiNavigationSuite
import ciyin.designsystem.component.navigation.NomiNavigationSuiteType
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.icon.Home
import ciyin.designsystem.icon.IconPack
import ciyin.designsystem.icon.Settings
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Nomi 原生导航套件的槽位渲染、分组顺序、选中语义与失败边界。
 *
 * 用例全部经 [NomiNavigationSuite] 统一入口或 Nomi 默认 renderer 进入，不替换设计系统；
 * 图标、徽标与文案由槽内容组合，用于锁定槽位映射、项级修饰符与 Drawer 拒绝行为。
 */
@OptIn(ExperimentalTestApi::class)
class DefaultNomiNavigationTest {
    /**
     * 底部导航布局同时渲染主导航项、底部导航项和页面内容，槽位按声明进入选中语义。
     */
    @Test
    fun barRendersGroupedSlotsAndSelectedSemantics() = runComposeUiTest {
        var clicks = 0
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                NomiNavigationSuite(
                    layoutType = NomiNavigationSuiteType.Bar,
                    items = {
                        NavigationItem(
                            selected = true,
                            onClick = { clicks++ },
                            icon = {
                                Icon(
                                    imageVector = IconPack.Home,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            label = { NomiText("home") },
                        )
                        NavigationItem(
                            selected = false,
                            onClick = { clicks++ },
                            icon = {
                                Icon(
                                    imageVector = IconPack.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            badge = {
                                NomiText(text = "9+", modifier = Modifier.testTag("badge"))
                            },
                            label = { NomiText("settings") },
                        )
                    },
                    footerItems = {
                        NavigationItem(
                            selected = false,
                            onClick = { clicks++ },
                            icon = {
                                Icon(
                                    imageVector = IconPack.Home,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            label = { NomiText("theme") },
                        )
                    },
                    content = { Box(modifier = Modifier.testTag("content")) },
                )
            }
        }
        onNodeWithTag("content").assertExists()
        onNodeWithText("home").assertIsSelected()
        onNodeWithText("settings").assertIsNotSelected()
        onNodeWithText("theme").assertIsDisplayed()
        onNodeWithTag("badge", useUnmergedTree = true).assertExists()
        onNodeWithText("settings").performClick()
        runOnIdle { assertEquals(1, clicks) }
    }

    /**
     * 侧边导航布局只渲染图标槽，文案槽作为 tooltip 提示不进入可见树；项级修饰符、点击与选中语义保留。
     */
    @Test
    fun railRendersIconOnlyItemsAndUsesLabelAsTooltip() = runComposeUiTest {
        var clicks = 0
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                NomiNavigationSuite(
                    layoutType = NomiNavigationSuiteType.Rail,
                    items = {
                        NavigationItem(
                            selected = true,
                            onClick = { clicks++ },
                            modifier = Modifier.testTag("rail-selected"),
                            icon = {
                                Icon(
                                    imageVector = IconPack.Home,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            label = { NomiText("home") },
                        )
                        NavigationItem(
                            selected = false,
                            onClick = { clicks++ },
                            modifier = Modifier.testTag("rail-icon-only"),
                            icon = {
                                Icon(
                                    imageVector = IconPack.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                        )
                    },
                    footerItems = {
                        NavigationItem(
                            selected = false,
                            onClick = { clicks++ },
                            icon = {
                                Icon(
                                    imageVector = IconPack.Home,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            label = { NomiText("footer") },
                        )
                    },
                    content = { Box(modifier = Modifier.testTag("content")) },
                )
            }
        }
        onNodeWithTag("content").assertExists()
        onNodeWithTag("rail-selected").assertIsSelected()
        onNodeWithTag("rail-icon-only").assertIsNotSelected()
        // 文案槽只在悬停 tooltip 时组合，未悬停不得进入可见语义树
        onNodeWithText("home").assertDoesNotExist()
        onNodeWithText("footer").assertDoesNotExist()
        onNodeWithTag("rail-icon-only").performClick()
        runOnIdle { assertEquals(1, clicks) }
    }

    /**
     * 槽位全部省略时仍渲染可点击的导航项，不要求图标、徽标或文案。
     */
    @Test
    fun itemWithoutAnySlotStaysSelectable() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                NomiNavigationSuite(
                    layoutType = NomiNavigationSuiteType.Bar,
                    items = {
                        NavigationItem(
                            selected = false,
                            onClick = {},
                            modifier = Modifier.testTag("empty-item"),
                        )
                    },
                    content = {},
                )
            }
        }
        onNodeWithTag("empty-item").assertIsNotSelected()
    }

    /**
     * 统一入口按能力声明拒绝 Drawer，并在进入原生渲染前失败。
     */
    @Test
    fun drawerLayoutFailsThroughCapabilityCheck() {
        val failure = assertFailsWith<IllegalArgumentException> {
            runComposeUiTest {
                setContent {
                    NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                        NomiNavigationSuite(
                            layoutType = NomiNavigationSuiteType.Drawer,
                            items = {
                                NavigationItem(selected = false, onClick = {})
                            },
                            content = {},
                        )
                    }
                }
                waitForIdle()
            }
        }
        assertTrue(failure.message?.contains("nomi") == true)
        assertTrue(failure.message?.contains("Drawer") == true)
    }

    /**
     * 直接调用 renderer 时 Drawer 分支给出明确的防御性失败，不静默渲染空导航。
     */
    @Test
    fun rendererRejectsDrawerLayoutDefensively() {
        val failure = assertFailsWith<IllegalStateException> {
            runComposeUiTest {
                setContent {
                    NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                        DefaultNomiNavigationRenderer.NavigationSuite(
                            layoutType = NomiNavigationSuiteType.Drawer,
                            modifier = Modifier,
                            items = {},
                            footerItems = {},
                            content = {},
                        )
                    }
                }
                waitForIdle()
            }
        }
        assertEquals("nomi 不支持 Drawer 导航布局", failure.message)
    }
}
