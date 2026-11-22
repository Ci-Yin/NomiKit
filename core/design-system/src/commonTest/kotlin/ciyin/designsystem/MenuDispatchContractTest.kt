package ciyin.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.menu.NomiMenu
import ciyin.designsystem.component.menu.NomiMenuItem
import ciyin.designsystem.component.menu.NomiMenuItemSpec
import ciyin.designsystem.component.menu.NomiMenuSpec
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 菜单入口只派发显式参数，未展开时不组合菜单项。
 */
@OptIn(ExperimentalTestApi::class)
class MenuDispatchContractTest {
    /**
     * 收起状态不组合菜单项，也不留下浮层记录之外的内容。
     */
    @Test
    fun collapsedMenuDoesNotComposeItems() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiMenu(expanded = false, onDismissRequest = {}) {
                    NomiMenuItem(onClick = {}) { NomiText("收起项") }
                }
            }
        }
        runOnIdle {
            assertEquals(
                listOf(RecordedMenu(expanded = false, hasModifier = false)),
                designSystem.menus,
            )
            assertEquals(emptyList(), designSystem.menuItems)
            assertEquals(emptySet(), designSystem.texts.keys)
        }
    }

    /**
     * 展开状态按声明顺序组合菜单项，并原样记录浮层与菜单项的显式参数。
     */
    @Test
    fun expandedMenuDispatchesItemsAndSparseParameters() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val menuSpec = RecordingMenuSpec(marker = "menu")
        val itemSpec = RecordingMenuItemSpec(marker = "item")
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiMenu(
                    expanded = true,
                    onDismissRequest = {},
                    modifier = Modifier.testTag("menu"),
                    nativeSpec = menuSpec,
                ) {
                    NomiMenuItem(
                        onClick = {},
                        modifier = Modifier.testTag("item"),
                        enabled = false,
                        selected = true,
                        leading = { NomiText("前导") },
                        trailing = { NomiText("尾部") },
                        nativeSpec = itemSpec,
                    ) {
                        NomiText("菜单项")
                    }
                }
            }
        }
        runOnIdle {
            assertEquals(
                listOf(RecordedMenu(expanded = true, hasModifier = true, nativeSpec = menuSpec)),
                designSystem.menus,
            )
            assertEquals(
                listOf(
                    RecordedMenuItem(
                        enabled = false,
                        selected = true,
                        hasLeading = true,
                        hasTrailing = true,
                        hasModifier = true,
                        nativeSpec = itemSpec,
                    )
                ),
                designSystem.menuItems,
            )
            assertEquals(setOf("菜单项"), designSystem.texts.keys)
        }
    }

    /**
     * 菜单项默认参数保持稀疏：启用、未选中、无前导与尾部槽。
     */
    @Test
    fun menuItemDefaultsRemainSparse() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiMenu(expanded = true, onDismissRequest = {}) {
                    NomiMenuItem(onClick = {}) { NomiText("默认项") }
                }
            }
        }
        runOnIdle {
            val recorded = designSystem.menuItems.single()
            assertEquals(true, recorded.enabled)
            assertEquals(false, recorded.selected)
            assertEquals(false, recorded.hasLeading)
            assertEquals(false, recorded.hasTrailing)
            assertEquals(false, recorded.hasModifier)
            assertNull(recorded.nativeSpec)
        }
    }
}

/**
 * 测试用菜单浮层专属参数，模拟 adapter 模块定义的具体 spec。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class RecordingMenuSpec(val marker: String) : NomiMenuSpec

/**
 * 测试用菜单项专属参数。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class RecordingMenuItemSpec(val marker: String) : NomiMenuItemSpec
