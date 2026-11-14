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
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import io.github.composefluent.component.Icon as NativeIcon
import io.github.composefluent.component.Text as NativeText

/**
 * Fluent 导航套件回归：显示模式映射、槽位组合、点击分发、分组与不支持能力。
 *
 * 颜色与像素断言留给原生主题测试，这里只验证布局形态、槽位内容与交互是否按中立契约落地。
 */
@OptIn(ExperimentalTestApi::class)
class FluentNavigationTest {
    /**
     * 三种中立布局映射到约定的原生显示模式，并声明 Fluent 能力边界。
     */
    @Test
    fun layoutTypeAndCapabilitiesMatchFluentLimits() {
        assertEquals(NavigationDisplayMode.Top, NomiNavigationSuiteType.Bar.toFluentDisplayMode())
        assertEquals(NavigationDisplayMode.Left, NomiNavigationSuiteType.Rail.toFluentDisplayMode())
        assertEquals(
            NavigationDisplayMode.LeftCompact,
            NomiNavigationSuiteType.Drawer.toFluentDisplayMode(),
        )
        assertTrue(NomiDesignSystem.Fluent.capabilities.navigationDrawer)
        assertFalse(NomiDesignSystem.Fluent.capabilities.navigationItemModifier)
    }

    /**
     * Bar 布局使用顶部导航栏：图标、徽标与文案槽进入原生项，页面内容位于导航栏下方。
     *
     * 顶部导航的原生底部项只显示图标与徽标、不显示可见文案，因此用点击区域确认分组存在。
     */
    @Test
    fun barLayoutRendersSlotsAndDispatchesClick() = runComposeUiTest {
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
        onNodeWithText("首页").assertExists()
        onNodeWithText("设置").assertExists()
        onNodeWithTag("icon-首页", useUnmergedTree = true).assertExists()
        onNodeWithTag("badge-首页", useUnmergedTree = true).assertExists()
        onNodeWithText("账户").assertDoesNotExist()
        assertTrue(onNodeWithTag("pane").fetchSemanticsNode().boundsInRoot.top > 0f)
        assertTrue(
            onAllNodes(hasClickAction()).fetchSemanticsNodes().size >= 3,
            "顶部导航应渲染两个主导航项与一个底部项的点击区域",
        )
        onNodeWithText("首页").performClick()
        runOnIdle { assertEquals(1, clicks) }
    }

    /**
     * Rail 布局使用完整侧边导航：槽位与可见文案都进入原生项，页面内容位于导航栏右侧。
     */
    @Test
    fun railLayoutRendersSlotsExpandedAndDispatchesClick() = runComposeUiTest {
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
        onNodeWithText("首页").assertExists()
        onNodeWithText("账户").assertExists()
        onNodeWithTag("icon-首页", useUnmergedTree = true).assertExists()
        onNodeWithTag("badge-首页", useUnmergedTree = true).assertExists()
        assertTrue(onNodeWithTag("pane").fetchSemanticsNode().boundsInRoot.left > 0f)
        onNodeWithText("设置").performClick()
        runOnIdle { assertEquals(1, clicks) }
    }

    /**
     * Drawer 布局使用紧凑侧边导航：折叠时只组合图标与徽标槽，页面内容右移出导航宽度。
     */
    @Test
    fun drawerLayoutRendersCompactSlotsWithoutLabels() = runComposeUiTest {
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
        assertTrue(onNodeWithTag("pane").fetchSemanticsNode().boundsInRoot.left > 0f)
        onNodeWithText("首页").assertDoesNotExist()
        onNodeWithTag("icon-首页", useUnmergedTree = true).assertExists()
        onNodeWithTag("badge-首页", useUnmergedTree = true).assertExists()
        assertTrue(
            onAllNodes(hasClickAction()).fetchSemanticsNodes().size >= 3,
            "折叠抽屉应为主导航项与底部项保留点击区域",
        )
    }

    /**
     * Fluent 原生导航项没有项级修饰符入口：能力声明为 false，非默认 modifier 明确失败。
     */
    @Test
    fun itemModifierIsRejected() {
        assertFalse(NomiDesignSystem.Fluent.capabilities.navigationItemModifier)
        assertFailsWith<IllegalArgumentException> {
            runComposeUiTest {
                setContent {
                    NomiTheme(designSystem = NomiDesignSystem.Fluent) {
                        NomiNavigationSuite(
                            layoutType = NomiNavigationSuiteType.Rail,
                            items = {
                                NavigationItem(
                                    selected = false,
                                    onClick = {},
                                    modifier = Modifier.testTag("item"),
                                )
                            },
                            content = {},
                        )
                    }
                }
            }
        }
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
