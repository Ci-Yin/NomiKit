package ciyin.designsystem.material3

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.control.NomiCheckbox
import ciyin.designsystem.component.control.NomiRadioButton
import ciyin.designsystem.component.control.NomiSwitch
import ciyin.designsystem.component.text.NomiTextField
import ciyin.designsystem.component.text.NomiTextFieldVariant
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Material 3 选择与输入控件适配：状态由调用方持有，回调与原生控件直接交互。
 */
@OptIn(ExperimentalTestApi::class)
class Material3ControlsTest {
    /**
     * 开关点击回到调用方并更新原生状态。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun switchTogglesThroughCallerState() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                var checked by remember { mutableStateOf(false) }
                NomiSwitch(
                    checked = checked,
                    onCheckedChange = { checked = it },
                    modifier = Modifier.testTag("switch"),
                )
            }
        }
        onNodeWithTag("switch").assertIsOff().performClick().assertIsOn()
    }

    /**
     * 复选框与单选按钮各自把点击交给调用方。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun checkboxAndRadioDispatchClicks() = runComposeUiTest {
        var checkboxClicks = 0
        var radioClicks = 0
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                Column {
                    NomiCheckbox(
                        checked = false,
                        onCheckedChange = { checkboxClicks += 1 },
                        modifier = Modifier.testTag("checkbox"),
                    )
                    NomiRadioButton(
                        selected = false,
                        onClick = { radioClicks += 1 },
                        modifier = Modifier.testTag("radio"),
                    )
                }
            }
        }
        onNodeWithTag("checkbox").performClick()
        onNodeWithTag("radio").performClick()
        runOnIdle {
            assertEquals(1, checkboxClicks)
            assertEquals(1, radioClicks)
        }
    }

    /**
     * 文本输入框接受输入，描边变体与错误态都落到原生入口。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun textFieldAcceptsInputInOutlinedErrorState() = runComposeUiTest {
        var received = ""
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                NomiTextField(
                    value = received,
                    onValueChange = { received = it },
                    modifier = Modifier.testTag("text-field"),
                    variant = NomiTextFieldVariant.Outlined,
                    isError = true,
                )
            }
        }
        onNodeWithTag("text-field").performTextInput("abc")
        runOnIdle {
            assertTrue(received == "abc", "输入应回到调用方，实际：" + received)
        }
    }
}
