package ciyin.designsystem.fluent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.NomiBorder
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.card.NomiCard
import ciyin.designsystem.component.surface.NomiSurfaceColors
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

/**
 * 不支持参数通过独立组合明确失败，不在一个测试环境内多次 setContent。
 */
@OptIn(ExperimentalTestApi::class)
class FluentCapabilitiesTest {
    /**
     * 能力声明精确反映上游不支持的五项。
     */
    @Test
    fun capabilitiesRejectUnsupportedOverrides() {
        val capabilities = NomiDesignSystem.Fluent.capabilities
        assertFalse(capabilities.buttonSize)
        assertFalse(capabilities.buttonShape)
        assertFalse(capabilities.buttonContentPadding)
        assertFalse(capabilities.cardColors)
        assertFalse(capabilities.cardBorder)
        assertFalse(capabilities.textAutoSize)
    }

    /**
     * 按钮尺寸不能被静默忽略。
     */
    @Test
    fun buttonSizeFails() = expectUnsupported {
        NomiButton(
            onClick = {},
            size = NomiControlSize.Small
        ) { NomiText("尺寸") }
    }

    /**
     * 按钮形状不能被重绘模拟。
     */
    @Test
    fun buttonShapeFails() = expectUnsupported {
        NomiButton(
            onClick = {},
            shape = CutCornerShape(8.dp)
        ) { NomiText("形状") }
    }

    /**
     * 按钮内边距不能被外边距伪造。
     */
    @Test
    fun buttonPaddingFails() = expectUnsupported {
        NomiButton(
            onClick = {},
            contentPadding = PaddingValues(8.dp)
        ) { NomiText("内边距") }
    }

    /**
     * 卡片容器色不能被静默忽略。
     */
    @Test
    fun cardContainerColorFails() = expectUnsupported {
        NomiCard(colors = NomiSurfaceColors(containerColor = Color.Red)) {
            NomiText("颜色")
        }
    }

    /**
     * 卡片内容色同样不受原生 Card 支持。
     */
    @Test
    fun cardContentColorFails() = expectUnsupported {
        NomiCard(colors = NomiSurfaceColors(contentColor = Color.Red)) {
            NomiText("文字色")
        }
    }

    /**
     * None 与原生默认不同，必须明确拒绝。
     */
    @Test
    fun cardNoBorderFails() =
        expectUnsupported { NomiCard(border = NomiBorder.None) { NomiText("无描边") } }

    /**
     * 显式 Stroke 必须明确拒绝。
     */
    @Test
    fun cardExplicitBorderFails() = expectUnsupported {
        NomiCard(
            border = NomiBorder.Stroke(
                BorderStroke(
                    width = 2.dp,
                    color = Color.Red
                )
            )
        ) { NomiText("描边") }
    }

    /**
     * 文本自动字号不能被静默忽略或重绘模拟。
     */
    @Test
    fun textAutoSizeFails() =
        expectUnsupported { NomiText(text = "自动字号", autoSize = TextAutoSize.StepBased()) }
}

/**
 * 每次验证创建自己的 Compose 环境，断言本次不支持调用抛出 IllegalArgumentException。
 *
 * @param content 在 Fluent 主题中执行的非法组件调用，每次使用独立组合环境。
 */
@OptIn(ExperimentalTestApi::class)
private fun expectUnsupported(content: @Composable () -> Unit) {
    assertFailsWith<IllegalArgumentException> {
        runComposeUiTest {
            setContent { NomiTheme(designSystem = NomiDesignSystem.Fluent, content = content) }
        }
    }
}
