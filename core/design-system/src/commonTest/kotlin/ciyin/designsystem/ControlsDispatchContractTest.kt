package ciyin.designsystem

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import ciyin.designsystem.component.control.NomiCheckbox
import ciyin.designsystem.component.control.NomiCheckboxSpec
import ciyin.designsystem.component.control.NomiRadioButton
import ciyin.designsystem.component.control.NomiRadioButtonSpec
import ciyin.designsystem.component.control.NomiSwitch
import ciyin.designsystem.component.control.NomiSwitchSpec
import ciyin.designsystem.component.text.NomiTextField
import ciyin.designsystem.component.text.NomiTextFieldSpec
import ciyin.designsystem.component.text.NomiTextFieldVariant
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 选择与输入控件入口只派发明确参数，不预填统一尺寸、颜色或内边距。
 */
@OptIn(ExperimentalTestApi::class)
class ControlsDispatchContractTest {
    /**
     * 三个选择控件在默认参数下派发关闭状态与启用态，且各自记录到对应种类。
     */
    @Test
    fun toggleDefaultsAreDispatchedAsEnabledAndUnchecked() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiSwitch(checked = false, onCheckedChange = {})
                NomiCheckbox(checked = false, onCheckedChange = {})
                NomiRadioButton(selected = false, onClick = {})
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    RecordedToggle(RecordedToggleKind.Switch, checked = false, enabled = true, hasModifier = false),
                    RecordedToggle(RecordedToggleKind.Checkbox, checked = false, enabled = true, hasModifier = false),
                    RecordedToggle(RecordedToggleKind.RadioButton, checked = false, enabled = true, hasModifier = false),
                ),
                designSystem.toggles,
            )
        }
    }

    /**
     * 选中状态、禁用态、修饰符与各自的专属参数原样透传。
     */
    @Test
    fun toggleExplicitParametersAreDispatchedUnchanged() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val switchSpec = RecordingSwitchSpec(marker = "switch")
        val checkboxSpec = RecordingCheckboxSpec(marker = "checkbox")
        val radioSpec = RecordingRadioButtonSpec(marker = "radio")
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiSwitch(
                    checked = true,
                    onCheckedChange = {},
                    modifier = Modifier.testTag("switch"),
                    enabled = false,
                    nativeSpec = switchSpec,
                )
                NomiCheckbox(
                    checked = true,
                    onCheckedChange = {},
                    modifier = Modifier.testTag("checkbox"),
                    enabled = false,
                    nativeSpec = checkboxSpec,
                )
                NomiRadioButton(
                    selected = true,
                    onClick = {},
                    modifier = Modifier.testTag("radio"),
                    enabled = false,
                    nativeSpec = radioSpec,
                )
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    RecordedToggle(RecordedToggleKind.Switch, checked = true, enabled = false, hasModifier = true, nativeSpec = switchSpec),
                    RecordedToggle(RecordedToggleKind.Checkbox, checked = true, enabled = false, hasModifier = true, nativeSpec = checkboxSpec),
                    RecordedToggle(RecordedToggleKind.RadioButton, checked = true, enabled = false, hasModifier = true, nativeSpec = radioSpec),
                ),
                designSystem.toggles,
            )
        }
    }

    /**
     * 输入框只传必填值时，槽位、变体、错误态与键盘动作保持默认。
     */
    @Test
    fun textFieldDefaultsRemainSparse() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiTextField(value = "值", onValueChange = {})
            }
        }
        runOnIdle {
            val recorded = designSystem.textFields.single()
            assertEquals("值", recorded.value)
            assertEquals(true, recorded.enabled)
            assertEquals(false, recorded.readOnly)
            assertEquals(false, recorded.singleLine)
            assertNull(recorded.variant)
            assertEquals(false, recorded.hasLabel)
            assertEquals(false, recorded.hasPlaceholder)
            assertEquals(false, recorded.hasLeadingIcon)
            assertEquals(false, recorded.hasTrailingIcon)
            assertEquals(false, recorded.isError)
            assertEquals(false, recorded.hasModifier)
            assertNull(recorded.nativeSpec)
        }
    }

    /**
     * 输入框的全部显式槽位、变体、错误态与键盘配置原样透传。
     */
    @Test
    fun textFieldExplicitParametersAreDispatchedUnchanged() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val spec = RecordingTextFieldSpec(marker = "text-field")
        val keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiTextField(
                    value = "查询",
                    onValueChange = {},
                    modifier = Modifier.testTag("text-field"),
                    enabled = false,
                    readOnly = true,
                    singleLine = true,
                    maxLines = 3,
                    visualTransformation = PasswordVisualTransformation(),
                    variant = NomiTextFieldVariant.Outlined,
                    label = { NomiText("标签") },
                    placeholder = { NomiText("占位") },
                    leadingIcon = { NomiText("前导") },
                    trailingIcon = { NomiText("尾部") },
                    isError = true,
                    keyboardOptions = keyboardOptions,
                    nativeSpec = spec,
                )
            }
        }
        runOnIdle {
            val recorded = designSystem.textFields.single()
            assertEquals("查询", recorded.value)
            assertEquals(false, recorded.enabled)
            assertEquals(true, recorded.readOnly)
            assertEquals(true, recorded.singleLine)
            assertEquals(3, recorded.maxLines)
            assertEquals(PasswordVisualTransformation(), recorded.visualTransformation)
            assertEquals(NomiTextFieldVariant.Outlined, recorded.variant)
            assertEquals(true, recorded.hasLabel)
            assertEquals(true, recorded.hasPlaceholder)
            assertEquals(true, recorded.hasLeadingIcon)
            assertEquals(true, recorded.hasTrailingIcon)
            assertEquals(true, recorded.isError)
            assertEquals(true, recorded.hasModifier)
            assertEquals(keyboardOptions, recorded.keyboardOptions)
            assertEquals(spec, recorded.nativeSpec)
        }
    }
}

/**
 * 测试用开关专属参数，模拟 adapter 模块定义的具体 spec。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class RecordingSwitchSpec(val marker: String) : NomiSwitchSpec

/**
 * 测试用复选框专属参数。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class RecordingCheckboxSpec(val marker: String) : NomiCheckboxSpec

/**
 * 测试用单选按钮专属参数。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class RecordingRadioButtonSpec(val marker: String) : NomiRadioButtonSpec

/**
 * 测试用输入框专属参数。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class RecordingTextFieldSpec(val marker: String) : NomiTextFieldSpec
