package ciyin.designsystem.material3

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
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.DarkMode
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test

/**
 * Material 3 图标与分割线适配：原生入口承载，未指定取值回落原生默认。
 */
@OptIn(ExperimentalTestApi::class)
class Material3AtomsTest {
    /**
     * 图标把无障碍名称交给原生 Icon，并接受透明着色。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun iconRendersWithNativeSemantics() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                NomiIcon(
                    imageVector = IconPack.Home,
                    contentDescription = "首页",
                    modifier = Modifier.testTag("icon"),
                    tint = Color.Transparent,
                )
            }
        }
        onNodeWithTag("icon").assertExists()
        onNodeWithContentDescription("首页").assertExists()
    }

    /**
     * 水平与垂直分割线分别使用原生水平/垂直入口，显式粗细与颜色被接受。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun bothDirectionsRenderWithExplicitValues() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Material3, darkMode = DarkMode.Light) {
                Column(Modifier.fillMaxWidth()) {
                    NomiDivider(
                        modifier = Modifier.fillMaxWidth().height(2.dp).testTag("horizontal"),
                        thickness = 2.dp,
                        color = Color.Red,
                    )
                    Row(Modifier.fillMaxWidth().height(60.dp)) {
                        NomiDivider(
                            orientation = NomiDividerOrientation.Vertical,
                            modifier = Modifier.width(2.dp).fillMaxHeight().testTag("vertical"),
                            thickness = 2.dp,
                            color = Color.Blue,
                        )
                    }
                }
            }
        }
        onNodeWithTag("horizontal").assertExists()
        onNodeWithTag("vertical").assertExists()
    }
}
