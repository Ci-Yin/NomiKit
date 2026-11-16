package ciyin.designsystem.fluent.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.navigation.NomiNavigationItemColors
import ciyin.designsystem.component.navigation.NomiNavigationSuite
import ciyin.designsystem.component.navigation.NomiNavigationSuiteType
import ciyin.designsystem.fluent.Fluent
import ciyin.designsystem.icon.IconPack
import ciyin.designsystem.icon.Null
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import io.github.composefluent.component.NavigationDisplayMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import io.github.composefluent.component.Icon as NativeIcon
import io.github.composefluent.component.Text as NativeText

/**
 * Fluent 导航套件回归：显示模式映射、槽位组合、点击分发与分组。
 *
 * 颜色与像素断言留给原生主题测试，这里只验证布局形态、槽位内容与交互是否按中立契约落地。
 */
@OptIn(ExperimentalTestApi::class)
class FluentNavigationTest {
    /**
     * 三种中立布局映射到约定的原生显示模式。
     */
    @Test
    fun layoutTypesMapToFluentDisplayModes() {
        assertEquals(
            NavigationDisplayMode.LeftCollapsed,
            NomiNavigationSuiteType.Bar.toFluentDisplayMode(),
        )
        assertEquals(
            NavigationDisplayMode.LeftCompact,
            NomiNavigationSuiteType.Rail.toFluentDisplayMode(),
        )
        assertEquals(
            NavigationDisplayMode.Left,
            NomiNavigationSuiteType.Drawer.toFluentDisplayMode()
        )
    }

    /**
     * Bar 布局映射折叠侧边：默认收起且页面内容全幅，点击展开按钮后导航项进入可见语义树并可分发点击。
     */
    @Test
    fun barLayoutExpandsCollapsedShellAndDispatchesClick() = runComposeUiTest {
        var clicks = 0
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Fluent) {
                NomiNavigationSuite(
                    layoutType = NomiNavigationSuiteType.Bar,
                    items = {
                        NavigationItem(
                            selected = true,
                            onClick = { clicks++ },
                            icon = { TestIconSlot(tag = "icon-首页") },
                            badge = { TestBadgeSlot(tag = "badge-首页") },
                            label = { NativeText("首页") },
                            colors = NomiNavigationItemColors(
                                selectedContentColor = Color(
                                    0xFF32745A
                                )
                            ),
                        )
                        NavigationItem(
                            selected = false,
                            onClick = {},
                            label = { NativeText("设置") },
                        )
                    },
                    footerItems = {
                        NavigationItem(
                            selected = false,
                            onClick = {},
                            icon = { TestIconSlot(tag = "icon-账户") },
                            label = { NativeText("账户") },
                        )
                    },
                    content = {
                        Box(modifier = Modifier.fillMaxSize().testTag("pane"))
                    },
                )
            }
        }
        val paneBounds = onNodeWithTag("pane").fetchSemanticsNode().boundsInRoot
        assertEquals(0f, paneBounds.left)
        assertEquals(0f, paneBounds.top)
        // 折叠侧边默认收起：导航项不进入可见语义树，只保留展开按钮。
        onNodeWithText("首页").assertDoesNotExist()
        onNodeWithTag("icon-首页", useUnmergedTree = true).assertDoesNotExist()

        onAllNodes(hasClickAction()).onFirst().performClick()
        waitForIdle()
        onNodeWithText("首页").assertExists()
        onNodeWithText("设置").assertExists()
        onNodeWithTag("icon-首页", useUnmergedTree = true).assertExists()
        onNodeWithTag("badge-首页", useUnmergedTree = true).assertExists()
        onNodeWithText("账户").assertExists()
        onNodeWithText("首页").performClick()
        runOnIdle { assertEquals(1, clicks) }
    }

    /**
     * Rail 布局映射紧凑侧边：默认折叠为常驻图标栏，文案收在浮出菜单里。
     *
     * 折叠状态下内容按图标栏宽度起始内缩，图标与徽标槽仍组合，点击图标即可分发导航。
     */
    @Test
    fun railLayoutRendersCompactIconsAndDispatchesClick() = runComposeUiTest {
        var clicks = 0
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Fluent) {
                NomiNavigationSuite(
                    layoutType = NomiNavigationSuiteType.Rail,
                    items = {
                        NavigationItem(
                            selected = true,
                            onClick = {},
                            icon = { TestIconSlot(tag = "icon-首页") },
                            badge = { TestBadgeSlot(tag = "badge-首页") },
                            label = { NativeText("首页") },
                        )
                        NavigationItem(
                            selected = false,
                            onClick = { clicks++ },
                            icon = { TestIconSlot(tag = "icon-设置") },
                            label = { NativeText("设置") },
                        )
                    },
                    footerItems = {
                        NavigationItem(
                            selected = false,
                            onClick = {},
                            icon = { TestIconSlot(tag = "icon-账户") },
                            label = { NativeText("账户") },
                        )
                    },
                    content = {
                        Box(modifier = Modifier.fillMaxSize().testTag("pane"))
                    },
                )
            }
        }
        assertTrue(onNodeWithTag("pane").fetchSemanticsNode().boundsInRoot.left > 0f)
        onNodeWithTag("icon-首页", useUnmergedTree = true).assertExists()
        onNodeWithTag("badge-首页", useUnmergedTree = true).assertExists()
        onNodeWithText("首页").assertDoesNotExist()
        onNodeWithTag("icon-设置", useUnmergedTree = true).performClick()
        runOnIdle { assertEquals(1, clicks) }
    }

    /**
     * Drawer 布局映射完整侧边：图标、徽标、文案与底部项都进入左侧栏，页面内容右移。
     */
    @Test
    fun drawerLayoutRendersExpandedSlotsWithLabels() = runComposeUiTest {
        var clicks = 0
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Fluent) {
                NomiNavigationSuite(
                    layoutType = NomiNavigationSuiteType.Drawer,
                    items = {
                        NavigationItem(
                            selected = true,
                            onClick = {},
                            icon = { TestIconSlot(tag = "icon-首页") },
                            badge = { TestBadgeSlot(tag = "badge-首页") },
                            label = { NativeText("首页") },
                        )
                        NavigationItem(
                            selected = false,
                            onClick = { clicks++ },
                            label = { NativeText("设置") },
                        )
                    },
                    footerItems = {
                        NavigationItem(
                            selected = false,
                            onClick = {},
                            icon = { TestIconSlot(tag = "icon-账户") },
                            label = { NativeText("账户") },
                        )
                    },
                    content = {
                        Box(modifier = Modifier.fillMaxSize().testTag("pane"))
                    },
                )
            }
        }
        assertTrue(onNodeWithTag("pane").fetchSemanticsNode().boundsInRoot.left > 0f)
        onNodeWithText("首页").assertExists()
        onNodeWithText("账户").assertExists()
        onNodeWithTag("icon-首页", useUnmergedTree = true).assertExists()
        onNodeWithTag("badge-首页", useUnmergedTree = true).assertExists()
        assertTrue(
            onAllNodes(hasClickAction()).fetchSemanticsNodes().size >= 3,
            "完整侧边应为主导航项与底部项保留点击区域",
        )
        onNodeWithText("设置").performClick()
        runOnIdle { assertEquals(1, clicks) }
    }

    /**
     * Fluent 原生导航项没有项级修饰符入口：忽略 modifier 并正常渲染。
     */
    @Test
    fun itemModifierIsIgnoredAndSuiteRenders() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Fluent) {
                NomiNavigationSuite(
                    layoutType = NomiNavigationSuiteType.Rail,
                    items = {
                        NavigationItem(
                            selected = false,
                            onClick = {},
                            modifier = Modifier.testTag("item"),
                            label = { NativeText("首页") },
                        )
                    },
                    content = { Box(modifier = Modifier.fillMaxSize().testTag("pane")) },
                )
            }
        }
        onNodeWithTag("pane").assertExists()
        onNodeWithTag("item", useUnmergedTree = true).assertDoesNotExist()
    }
}

/**
 * 测试用图标槽：给中立空图标显式约束尺寸，避免按 256dp 固有尺寸绘制。
 *
 * @param tag 用于验证图标槽已被原生导航项组合的测试标签。
 */
@Composable
private fun TestIconSlot(tag: String) {
    NativeIcon(
        imageVector = IconPack.Null,
        contentDescription = null,
        modifier = Modifier.size(24.dp).testTag(tag),
    )
}

/**
 * 测试用徽标槽：小尺寸占位块，仅用于验证徽标槽已被原生导航项组合。
 *
 * @param tag 用于验证徽标槽已被原生导航项组合的测试标签。
 */
@Composable
private fun TestBadgeSlot(tag: String) {
    Box(modifier = Modifier.size(8.dp).testTag(tag))
}
