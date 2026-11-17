package ciyin.designsystem.material3.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.navigation.NomiNavigationSuite
import ciyin.designsystem.component.navigation.NomiNavigationSuiteType
import ciyin.designsystem.icon.Home
import ciyin.designsystem.icon.IconPack
import ciyin.designsystem.icon.Settings
import ciyin.designsystem.material3.Material3
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Material 3 导航套件的槽位式导航项渲染、选中语义、分组顺序与抽屉能力回归。
 */
@OptIn(ExperimentalTestApi::class)
class Material3NavigationTest {
    /**
     * Bar 布局组合图标与文案槽，选中态来自调用方，点击回调只在原生导航项上派发。
     *
     * @return 本回归的跨平台测试完成结果；Web 由测试运行器等待其 Promise，JVM/Native 同步完成。
     */
    @Test
    fun barRendersSlotItemsAndDispatchesClicks() = runComposeUiTest {
        var mainClicks = 0
        var footerClicks = 0
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                NomiNavigationSuite(
                    layoutType = NomiNavigationSuiteType.Bar,
                    items = {
                        NavigationItem(
                            selected = true,
                            onClick = { mainClicks++ },
                            icon = {
                                Icon(
                                    imageVector = IconPack.Home,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            label = { Text(text = "主页") },
                        )
                        NavigationItem(
                            selected = false,
                            onClick = { mainClicks++ },
                            icon = {
                                Icon(
                                    imageVector = IconPack.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            label = { Text(text = "设置") },
                        )
                    },
                    footerItems = {
                        NavigationItem(
                            selected = false,
                            onClick = { footerClicks++ },
                            icon = {
                                Icon(
                                    imageVector = IconPack.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            label = { Text(text = "底部") },
                        )
                    },
                    content = {
                        Box(modifier = Modifier.testTag("bar-content")) {
                            Text(text = "页面内容")
                        }
                    },
                )
            }
        }
        onNodeWithText("主页").assertIsSelected()
        onNodeWithText("页面内容").assertExists()
        onNodeWithTag("bar-content").assertExists()
        onNodeWithText("设置").performClick()
        onNodeWithText("底部").performClick()
        assertEquals(1, mainClicks)
        assertEquals(1, footerClicks)
    }

    /**
     * Rail 布局按槽位显示原生文案，弹性占位把底部项推离主导航项。
     *
     * @return 本回归的跨平台测试完成结果；Web 由测试运行器等待其 Promise，JVM/Native 同步完成。
     */
    @Test
    fun railRendersSlotItemsAndSpacerSeparatesFooter() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                NomiNavigationSuite(
                    layoutType = NomiNavigationSuiteType.Rail,
                    items = {
                        NavigationItem(
                            selected = true,
                            onClick = {},
                            icon = {
                                Icon(
                                    imageVector = IconPack.Home,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            label = { Text(text = "主页") },
                        )
                        NavigationItem(
                            selected = false,
                            onClick = {},
                            icon = {
                                Icon(
                                    imageVector = IconPack.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            label = { Text(text = "设置") },
                        )
                    },
                    footerItems = {
                        NavigationItem(
                            selected = false,
                            onClick = {},
                            icon = {
                                Icon(
                                    imageVector = IconPack.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            label = { Text(text = "底部") },
                        )
                    },
                    content = { Box(modifier = Modifier.testTag("rail-content")) },
                )
            }
        }
        onNodeWithText("主页").assertIsSelected()
        onNodeWithText("设置").assertExists()
        onNodeWithText("底部").assertExists()
        onNodeWithTag("rail-content").assertExists()
        val lastMainBounds = onNodeWithText("设置").fetchSemanticsNode().boundsInRoot
        val footerBounds = onNodeWithText("底部").fetchSemanticsNode().boundsInRoot
        assertTrue(footerBounds.top - lastMainBounds.bottom > lastMainBounds.height)
    }

    /**
     * Drawer 布局由原生永久抽屉渲染槽位，选中、点击与徽标槽组合均保持在原生抽屉项上。
     *
     * @return 本回归的跨平台测试完成结果；Web 由测试运行器等待其 Promise，JVM/Native 同步完成。
     */
    @Test
    fun drawerRendersSlotItemsAndComposesBadgeSlot() = runComposeUiTest {
        var clicks = 0
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                NomiNavigationSuite(
                    layoutType = NomiNavigationSuiteType.Drawer,
                    containerColor = Color(0xFF112233),
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
                            badge = {
                                Box(
                                    modifier = Modifier
                                        .testTag("drawer-badge")
                                        .size(8.dp),
                                )
                            },
                            label = { Text(text = "主页") },
                        )
                    },
                    footerItems = {
                        NavigationItem(
                            selected = false,
                            onClick = {},
                            icon = {
                                Icon(
                                    imageVector = IconPack.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                )
                            },
                            label = { Text(text = "底部") },
                        )
                    },
                    content = { Box(modifier = Modifier.testTag("drawer-content")) },
                )
            }
        }
        onNodeWithText("主页").assertIsSelected()
        onNodeWithTag("drawer-badge", useUnmergedTree = true).assertExists()
        onNodeWithText("底部").assertExists()
        onNodeWithTag("drawer-content").assertExists()
        onNodeWithText("主页").performClick()
        assertEquals(1, clicks)
    }

}
