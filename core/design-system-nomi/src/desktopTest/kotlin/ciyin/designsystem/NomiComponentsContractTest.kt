package ciyin.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.NomiControlSize
import ciyin.designsystem.component.button.NomiButton
import ciyin.designsystem.component.button.NomiButtonColors
import ciyin.designsystem.component.button.NomiButtonVariant
import ciyin.designsystem.component.card.NomiCard
import ciyin.designsystem.component.surface.NomiSurface
import ciyin.designsystem.component.surface.NomiSurfaceColors
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.nomi.LocalNomiNativeContentColor
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Nomi 中立组件的尺寸、语义、键盘和状态视觉契约。
 */
@OptIn(ExperimentalTestApi::class)
class NomiComponentsContractTest {
    /**
     * 五种变体与三种尺寸都保持可访问按钮角色和唯一 clickable。
     */
    @Test
    fun allVariantsAndSizesExposeSingleButtonNode() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default) {
                Column {
                    NomiButtonVariant.entries.forEach { variant ->
                        NomiControlSize.entries.forEach { size ->
                            NomiButton(
                                onClick = {},
                                modifier = Modifier.testTag(variant.name + "-" + size.name),
                                variant = variant,
                                size = size,
                            ) {
                                NomiText(variant.name + "/" + size.name)
                            }
                        }
                    }
                }
            }
        }
        assertEquals(
            NomiButtonVariant.entries.size * NomiControlSize.entries.size,
            onAllNodes(hasClickAction(), useUnmergedTree = true).fetchSemanticsNodes().size,
        )
        NomiButtonVariant.entries.forEach { variant ->
            NomiControlSize.entries.forEach { size ->
                onNodeWithTag(variant.name + "-" + size.name)
                    .assert(hasClickAction())
                    .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            }
        }
    }

    /**
     * 按钮向键盘派发点击，禁用后不可触发。
     */
    @Test
    fun buttonSupportsKeyboardAndDisabledSemantics() = runComposeUiTest {
        var enabled by mutableStateOf(true)
        var clicks = 0
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default) {
                NomiButton(
                    onClick = { clicks++ },
                    modifier = Modifier.testTag("button"),
                    enabled = enabled,
                ) { NomiText("Action") }
            }
        }
        val button = onNodeWithTag("button")
        button.assertIsEnabled()
        button.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        button.assertIsFocused()
        button.performKeyInput {
            keyDown(Key.Enter)
            keyUp(Key.Enter)
            keyDown(Key.Spacebar)
            keyUp(Key.Spacebar)
        }
        runOnIdle { assertEquals(2, clicks) }
        runOnIdle { enabled = false }
        button.assertIsNotEnabled()
        button.performKeyInput {
            keyDown(Key.Enter)
            keyUp(Key.Enter)
            keyDown(Key.Spacebar)
            keyUp(Key.Spacebar)
        }
        runOnIdle { assertEquals(2, clicks) }
    }

    /**
     * 启用与禁用按钮均向 slot 提供对应的精确内容色。
     */
    @Test
    fun buttonProvidesEnabledAndDisabledContentColors() = runComposeUiTest {
        var enabled by mutableStateOf(true)
        var observed = Color.Unspecified
        val colors = NomiButtonColors(
            containerColor = Color(0xFF555555),
            contentColor = Color.Yellow,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.Magenta,
        )
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default) {
                NomiButton(
                    onClick = {},
                    modifier = Modifier.testTag("color-button"),
                    enabled = enabled,
                    colors = colors,
                ) {
                    observed = LocalNomiNativeContentColor.current
                    NomiText("Color")
                }
            }
        }
        runOnIdle { assertEquals(Color.Yellow, observed) }
        runOnIdle { enabled = false }
        runOnIdle { assertEquals(Color.Magenta, observed) }
    }

    /**
     * 获得键盘焦点后按钮边缘显示主题焦点描边。
     */
    @Test
    fun focusedButtonShowsFocusOutline() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default) {
                NomiButton(
                    onClick = {},
                    modifier = Modifier.testTag("focus-button")
                ) { NomiText("Focus") }
            }
        }
        val button = onNodeWithTag("focus-button")
        val before = button.captureToImage().toPixelMap()
        val beforeEdge = before[before.width / 2, 0]
        button.performSemanticsAction(SemanticsActions.RequestFocus) { it() }
        val after = button.captureToImage().toPixelMap()
        val afterEdge = after[after.width / 2, 0]
        assertNotEquals(beforeEdge, afterEdge)
    }

    /**
     * 每种尺寸的实际布局宽高都不小于双向 48 dp 触摸目标。
     */
    @Test
    fun everyControlSizeMeetsBidirectionalMinimumTouchTarget() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default) {
                Column {
                    NomiControlSize.entries.forEach { size ->
                        NomiButton(
                            modifier = Modifier.testTag(size.name),
                            onClick = {},
                            size = size,
                        ) { NomiText("x") }
                    }
                }
            }
        }
        NomiControlSize.entries.forEach { size ->
            val bounds = onNodeWithTag(size.name).getUnclippedBoundsInRoot()
            assertTrue((bounds.right - bounds.left).value >= 48f)
            assertTrue((bounds.bottom - bounds.top).value >= 48f)
        }
    }

    /**
     * 透明强调按钮在悬停和按下状态提供可观测 alpha overlay。
     */
    @Test
    fun transparentVariantShowsHoverAndPressedOverlay() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default) {
                NomiButton(
                    onClick = {},
                    modifier = Modifier.testTag("transparent"),
                    variant = NomiButtonVariant.Text,
                ) { NomiText("Text") }
            }
        }
        val button = onNodeWithTag("transparent")
        val defaultImage = button.captureToImage().toPixelMap()
        val defaultPixel = defaultImage[defaultImage.width / 2, 3]
        button.performMouseInput { moveTo(center) }
        mainClock.advanceTimeBy(100)
        val hoverImage = button.captureToImage().toPixelMap()
        val hoverPixel = hoverImage[hoverImage.width / 2, 3]
        button.performMouseInput { press() }
        mainClock.advanceTimeBy(100)
        val pressedImage = button.captureToImage().toPixelMap()
        val pressedPixel = pressedImage[pressedImage.width / 2, 3]
        assertNotEquals(defaultPixel, hoverPixel)
        assertNotEquals(hoverPixel, pressedPixel)
        button.performMouseInput { release(); exit() }
    }

    /**
     * Surface 与 Card 传递精确内容色。
     */
    @Test
    fun surfaceAndCardPropagateContentColor() = runComposeUiTest {
        val color = Color(0xFF216E4E)
        var surfaceContent = Color.Unspecified
        var cardContent = Color.Unspecified
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default) {
                Column {
                    NomiSurface(
                        modifier = Modifier.testTag("surface"),
                        colors = NomiSurfaceColors(
                            containerColor = Color.White,
                            contentColor = color
                        ),
                    ) { surfaceContent = LocalNomiNativeContentColor.current }
                    NomiCard(
                        modifier = Modifier.testTag("card"),
                        colors = NomiSurfaceColors(
                            containerColor = Color.White,
                            contentColor = color
                        ),
                    ) { cardContent = LocalNomiNativeContentColor.current }
                }
            }
        }
        runOnIdle {
            assertEquals(color, surfaceContent)
            assertEquals(color, cardContent)
        }
    }
}
