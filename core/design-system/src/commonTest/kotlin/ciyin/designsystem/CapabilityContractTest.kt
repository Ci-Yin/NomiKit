package ciyin.designsystem

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.NomiBorder
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.card.NomiCard
import ciyin.designsystem.component.NomiComponentCapabilities
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.component.surface.NomiSurfaceColors
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * 核心能力边界必须明确拒绝不支持的覆盖，不能静默忽略或改用重绘组件。
 */
@OptIn(ExperimentalTestApi::class)
class CapabilityContractTest {
    /**
     * 不支持按钮尺寸覆盖时，明确 size 必须立即失败。
     */
    @Test
    fun unsupportedButtonSizeThrows() = assertRejected(
        capabilities = NomiComponentCapabilities(buttonSize = false),
        message = "recording NomiButton 不支持 size 覆盖",
    ) {
        NomiButton(onClick = {}, size = NomiControlSize.Small) {}
    }

    /**
     * 不支持按钮形状覆盖时，明确 shape 必须立即失败。
     */
    @Test
    fun unsupportedButtonShapeThrows() = assertRejected(
        capabilities = NomiComponentCapabilities(buttonShape = false),
        message = "recording NomiButton 不支持 shape 覆盖",
    ) {
        NomiButton(onClick = {}, shape = RectangleShape) {}
    }

    /**
     * 不支持按钮 padding 时，即使显式零边距也不能被视作未指定。
     */
    @Test
    fun unsupportedButtonPaddingThrowsEvenForZero() = assertRejected(
        capabilities = NomiComponentCapabilities(buttonContentPadding = false),
        message = "recording NomiButton 不支持 contentPadding 覆盖",
    ) {
        NomiButton(onClick = {}, contentPadding = PaddingValues(0.dp)) {}
    }

    /**
     * 透明色仍是明确覆盖，卡片不支持颜色时必须拒绝。
     */
    @Test
    fun unsupportedCardColorThrowsEvenForTransparent() = assertRejected(
        capabilities = NomiComponentCapabilities(cardColors = false),
        message = "recording NomiCard 不支持 colors 覆盖；请使用 NomiSurface",
    ) {
        NomiCard(colors = NomiSurfaceColors(containerColor = Color.Transparent)) {}
    }

    /**
     * 无边框不等于原生默认，不支持卡片边框时也必须拒绝 None。
     */
    @Test
    fun unsupportedCardBorderThrowsEvenForNone() = assertRejected(
        capabilities = NomiComponentCapabilities(cardBorder = false),
        message = "recording NomiCard 不支持 border 覆盖；请使用 NomiSurface",
    ) {
        NomiCard(border = NomiBorder.None) {}
    }

    /**
     * 能力全部关闭不影响省略覆盖的原生默认调用。
     *
     * @return 当前平台的 Compose 测试执行结果，供测试框架等待组合断言与环境清理完成。
     */
    @Test
    fun unsupportedOverridesStillAllowNativeDefaults() = runComposeUiTest {
        val recorder = RecordingDesignSystem()
        val restricted = RestrictedDesignSystem(
            delegate = recorder,
            capabilities = NomiComponentCapabilities(
                buttonSize = false,
                buttonShape = false,
                buttonContentPadding = false,
                cardColors = false,
                cardBorder = false,
            ),
        )
        setContent {
            NomiTheme(designSystem = restricted) {
                NomiButton(onClick = {}) {}
                NomiCard {}
            }
        }
        runOnIdle {
            assertEquals(1, recorder.buttons.size)
            assertEquals(1, recorder.cards.size)
            assertEquals(NomiBorder.Default, recorder.cards.single().border)
            assertEquals(NomiSurfaceColors(), recorder.cards.single().colors)
        }
    }
}

/**
 * 保留真实录制 renderer，仅收紧设计系统声明的能力。
 *
 * @property delegate 提供标识、主题和 renderer 的被代理设计系统。
 * @property capabilities 替换被代理系统声明、供当前用例校验的能力集合。
 */
private class RestrictedDesignSystem(
    private val delegate: NomiDesignSystem,
    override val capabilities: NomiComponentCapabilities,
) : NomiDesignSystem by delegate

/**
 * 在实际组合边界精确断言异常类型和信息，并确认没有发生按钮或卡片派发。
 *
 * @param capabilities 本次录制设计系统应声明的受限能力集合。
 * @param message 预期 IllegalArgumentException 的完整信息。
 * @param content 在受限 NomiTheme 内组合、预期因显式覆盖而被拒绝的内容。
 */
@OptIn(ExperimentalTestApi::class)
private fun assertRejected(
    capabilities: NomiComponentCapabilities,
    message: String,
    content: @Composable () -> Unit,
) {
    val recorder = RecordingDesignSystem()
    val restricted = RestrictedDesignSystem(delegate = recorder, capabilities = capabilities)
    val failure = assertFailsWith<IllegalArgumentException> {
        runComposeUiTest {
            setContent { NomiTheme(designSystem = restricted, content = content) }
            waitForIdle()
        }
    }
    assertEquals(message, failure.message)
    assertEquals(emptyList(), recorder.buttons)
    assertEquals(emptyList(), recorder.cards)
}
