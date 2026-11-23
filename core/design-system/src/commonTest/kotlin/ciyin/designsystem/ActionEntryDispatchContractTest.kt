package ciyin.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.component.button.NomiButtonColors
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.component.button.NomiDropDownButton
import ciyin.designsystem.component.button.NomiDropDownButtonSpec
import ciyin.designsystem.component.button.NomiIconButton
import ciyin.designsystem.component.button.NomiIconButtonSpec
import ciyin.designsystem.component.button.NomiToggleButton
import ciyin.designsystem.component.button.NomiToggleButtonColors
import ciyin.designsystem.component.button.NomiToggleButtonSpec
import ciyin.designsystem.component.control.NomiComboBox
import ciyin.designsystem.component.control.NomiComboBoxSpec
import ciyin.designsystem.component.menu.NomiMenuItem
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 图标按钮、开关按钮、下拉按钮与组合框入口只派发明确参数，不预填统一尺寸或颜色。
 */
@OptIn(ExperimentalTestApi::class)
class ActionEntryDispatchContractTest {
    /**
     * 图标按钮默认参数保持稀疏：启用、无颜色覆盖、无专属参数。
     */
    @Test
    fun iconButtonDefaultsRemainSparse() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiIconButton(onClick = {}) { NomiText("图标") }
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    RecordedIconButton(
                        enabled = true,
                        colors = NomiButtonColors(),
                        hasModifier = false,
                    )
                ),
                designSystem.iconButtons,
            )
            assertEquals(setOf("图标"), designSystem.texts.keys)
        }
    }

    /**
     * 图标按钮的禁用状态、修饰符、颜色与专属参数原样透传。
     */
    @Test
    fun iconButtonExplicitParametersAreDispatchedUnchanged() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val spec = RecordingIconButtonSpec(marker = "icon")
        val colors = NomiButtonColors(
            containerColor = Color.Red,
            disabledContentColor = Color.Blue,
        )
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiIconButton(
                    onClick = {},
                    modifier = Modifier.testTag("icon"),
                    enabled = false,
                    colors = colors,
                    nativeSpec = spec,
                ) {}
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    RecordedIconButton(
                        enabled = false,
                        colors = colors,
                        hasModifier = true,
                        nativeSpec = spec,
                    )
                ),
                designSystem.iconButtons,
            )
        }
    }

    /**
     * 开关按钮默认参数保持稀疏：启用、未选中、文字形态、无尺寸与颜色覆盖。
     */
    @Test
    fun toggleButtonDefaultsRemainSparse() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiToggleButton(checked = false, onCheckedChange = {}) { NomiText("开关") }
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    RecordedToggleButton(
                        checked = false,
                        enabled = true,
                        iconOnly = false,
                        size = null,
                        colors = NomiToggleButtonColors(),
                        hasModifier = false,
                    )
                ),
                designSystem.toggleButtons,
            )
            assertEquals(setOf("开关"), designSystem.texts.keys)
        }
    }

    /**
     * 开关按钮的选中、禁用、图标形态、尺寸、两态颜色与专属参数原样透传。
     */
    @Test
    fun toggleButtonExplicitParametersAreDispatchedUnchanged() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val spec = RecordingToggleButtonSpec(marker = "toggle")
        val colors = NomiToggleButtonColors(
            checked = NomiButtonColors(containerColor = Color.Red),
            unchecked = NomiButtonColors(contentColor = Color.Blue),
        )
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiToggleButton(
                    checked = true,
                    onCheckedChange = {},
                    modifier = Modifier.testTag("toggle"),
                    enabled = false,
                    iconOnly = true,
                    size = NomiControlSize.Small,
                    colors = colors,
                    nativeSpec = spec,
                ) {}
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    RecordedToggleButton(
                        checked = true,
                        enabled = false,
                        iconOnly = true,
                        size = NomiControlSize.Small,
                        colors = colors,
                        hasModifier = true,
                        nativeSpec = spec,
                    )
                ),
                designSystem.toggleButtons,
            )
        }
    }

    /**
     * 下拉按钮默认参数保持稀疏，并且不自行追加任何内容。
     */
    @Test
    fun dropDownButtonDefaultsRemainSparse() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiDropDownButton(onClick = {}) { NomiText("导出") }
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    RecordedDropDownButton(
                        enabled = true,
                        variant = null,
                        size = null,
                        colors = NomiButtonColors(),
                        hasModifier = false,
                    )
                ),
                designSystem.dropDownButtons,
            )
        }
    }

    /**
     * 下拉按钮的变体、尺寸、颜色与专属参数原样透传。
     */
    @Test
    fun dropDownButtonExplicitParametersAreDispatchedUnchanged() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val spec = RecordingDropDownButtonSpec(marker = "drop-down")
        val colors = NomiButtonColors(contentColor = Color.Green)
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiDropDownButton(
                    onClick = {},
                    modifier = Modifier.testTag("drop-down"),
                    enabled = false,
                    variant = NomiButtonVariant.Subtle,
                    size = NomiControlSize.Large,
                    colors = colors,
                    nativeSpec = spec,
                ) {}
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    RecordedDropDownButton(
                        enabled = false,
                        variant = NomiButtonVariant.Subtle,
                        size = NomiControlSize.Large,
                        colors = colors,
                        hasModifier = true,
                        nativeSpec = spec,
                    )
                ),
                designSystem.dropDownButtons,
            )
        }
    }

    /**
     * 组合框收起时不组合菜单项，展开时才组合，且收起态内容始终参与组合。
     */
    @Test
    fun comboBoxComposesMenuItemsOnlyWhenExpanded() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiComboBox(
                    expanded = false,
                    onExpandedChange = {},
                    menuItems = { NomiMenuItem(onClick = {}) { NomiText("选项") } },
                ) {
                    NomiText("当前值")
                }
            }
        }
        runOnIdle {
            assertEquals(
                listOf(RecordedComboBox(expanded = false, enabled = true, hasModifier = false)),
                designSystem.comboBoxes,
            )
            assertEquals(setOf("当前值"), designSystem.texts.keys)
            assertEquals(emptyList(), designSystem.menuItems)
        }
    }

    /**
     * 组合框展开时组合菜单项，并原样透传禁用状态与专属参数。
     */
    @Test
    fun comboBoxExplicitParametersAreDispatchedUnchanged() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val spec = RecordingComboBoxSpec(marker = "combo")
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiComboBox(
                    expanded = true,
                    onExpandedChange = {},
                    menuItems = { NomiMenuItem(onClick = {}) { NomiText("选项") } },
                    modifier = Modifier.testTag("combo"),
                    enabled = false,
                    nativeSpec = spec,
                ) {
                    NomiText("当前值")
                }
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    RecordedComboBox(
                        expanded = true,
                        enabled = false,
                        hasModifier = true,
                        nativeSpec = spec,
                    )
                ),
                designSystem.comboBoxes,
            )
            assertEquals(setOf("当前值", "选项"), designSystem.texts.keys)
            assertEquals(1, designSystem.menuItems.size)
            assertNull(designSystem.menuItems.single().nativeSpec)
        }
    }
}

/**
 * 测试用图标按钮专属参数，模拟 adapter 模块定义的具体 spec。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class RecordingIconButtonSpec(val marker: String) : NomiIconButtonSpec

/**
 * 测试用开关按钮专属参数，模拟 adapter 模块定义的具体 spec。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class RecordingToggleButtonSpec(val marker: String) : NomiToggleButtonSpec

/**
 * 测试用下拉按钮专属参数。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class RecordingDropDownButtonSpec(val marker: String) : NomiDropDownButtonSpec

/**
 * 测试用组合框专属参数。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class RecordingComboBoxSpec(val marker: String) : NomiComboBoxSpec
