package ciyin.designsystem.nomi.icon

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.divider.NomiDivider
import ciyin.designsystem.component.divider.NomiDividerOrientation
import ciyin.designsystem.component.icon.NomiIcon
import ciyin.designsystem.icon.Home
import ciyin.designsystem.icon.IconPack
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test

/**
 * Nomi 图标与分割线实现：着色与线条读取 Nomi 令牌。
 */
@OptIn(ExperimentalTestApi::class)
class NomiAtomsTest {
    /**
     * 图标渲染并保留无障碍名称。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun iconRendersWithNativeSemantics() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                NomiIcon(
                    imageVector = IconPack.Home,
                    contentDescription = "首页",
                    modifier = Modifier.testTag("icon"),
                )
            }
        }
        onNodeWithTag("icon").assertExists()
        onNodeWithContentDescription("首页").assertExists()
    }

    /**
     * 水平与垂直分割线都使用 Nomi 令牌绘制。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun bothDirectionsRender() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default, darkMode = DarkMode.Light) {
                Column(Modifier.fillMaxWidth()) {
                    NomiDivider(
                        modifier = Modifier.fillMaxWidth().height(2.dp).testTag("horizontal"),
                        color = Color.Red,
                    )
                    Row(Modifier.fillMaxWidth().height(60.dp)) {
                        NomiDivider(
                            orientation = NomiDividerOrientation.Vertical,
                            modifier = Modifier.width(2.dp).fillMaxHeight().testTag("vertical"),
                        )
                    }
                }
            }
        }
        onNodeWithTag("horizontal").assertExists()
        onNodeWithTag("vertical").assertExists()
    }
}
