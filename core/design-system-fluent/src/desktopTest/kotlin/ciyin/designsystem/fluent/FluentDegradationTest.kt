package ciyin.designsystem.fluent

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.NomiBorder
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.card.NomiCard
import ciyin.designsystem.component.navigation.NomiNavigationSuite
import ciyin.designsystem.component.navigation.NomiNavigationSuiteType
import ciyin.designsystem.component.surface.NomiSurfaceColors
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Fluent 无法承载的覆盖降级为原生默认：不抛异常，切换设计系统不中断。
 */
@OptIn(ExperimentalTestApi::class)
class FluentDegradationTest {
    /**
     * 按钮 size/shape/contentPadding 被忽略，原生点击仍然分发。
     */
    @Test
    fun unsupportedButtonOverridesAreIgnored() = runComposeUiTest {
        var clicks = 0
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Fluent) {
                NomiButton(
                    onClick = { clicks++ },
                    size = NomiControlSize.Small,
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(2.dp),
                ) {
                    NomiText("按钮")
                }
            }
        }
        onNodeWithText("按钮").assertExists()
        onNodeWithText("按钮").performClick()
        runOnIdle { assertEquals(1, clicks) }
    }

    /**
     * 卡片 colors/border 与文本 autoSize 被忽略，内容正常渲染。
     */
    @Test
    fun unsupportedCardAndTextOverridesAreIgnored() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Fluent) {
                NomiCard(
                    colors = NomiSurfaceColors(containerColor = Color.Red),
                    border = NomiBorder.None,
                ) {
                    NomiText(text = "卡片内容", autoSize = TextAutoSize.StepBased())
                }
            }
        }
        onNodeWithText("卡片内容").assertExists()
    }

    /**
     * 导航容器色被忽略，外壳与内容正常渲染。
     */
    @Test
    fun unsupportedNavigationContainerColorIsIgnored() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Fluent) {
                NomiNavigationSuite(
                    layoutType = NomiNavigationSuiteType.Bar,
                    containerColor = Color.Red,
                    items = {},
                    content = { NomiText("内容") },
                )
            }
        }
        onNodeWithText("内容").assertExists()
    }
}
