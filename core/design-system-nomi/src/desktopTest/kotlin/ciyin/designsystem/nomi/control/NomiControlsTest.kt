package ciyin.designsystem.nomi.control

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
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Nomi 选择与输入控件实现：交互沿用原生控件，配色读取 Nomi 令牌。
 */
@OptIn(ExperimentalTestApi::class)
class NomiControlsTest {
    /**
     * 开关点击回到调用方并更新原生状态。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun switchTogglesThroughCallerState() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
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
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
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
     * 输入框接受输入。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun textFieldAcceptsInput() = runComposeUiTest {
        var received = ""
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                NomiTextField(
                    value = received,
                    onValueChange = { received = it },
                    modifier = Modifier.testTag("text-field"),
                )
            }
        }
        onNodeWithTag("text-field").performTextInput("abc")
        runOnIdle {
            assertTrue(received == "abc", "输入应回到调用方，实际：" + received)
        }
    }
}
