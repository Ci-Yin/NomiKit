package ciyin.designsystem.fluent.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.scaffold.NomiScaffold
import ciyin.designsystem.component.scaffold.NomiScaffoldColors
import ciyin.designsystem.fluent.Fluent
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import io.github.composefluent.FluentTheme
import io.github.composefluent.LocalContentColor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Fluent 脚手架适配：槽位顺序、容器像素与内容色环境。
 */
@OptIn(ExperimentalTestApi::class)
class FluentScaffoldTest {
    /**
     * 主体内容位于顶栏与底栏之间，占据剩余高度。
     */
    @Test
    fun contentSitsBetweenTopAndBottomBars() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Fluent) {
                NomiScaffold(
                    topBar = {
                        Box(Modifier.fillMaxWidth().height(48.dp).testTag("top"))
                    },
                    bottomBar = {
                        Box(Modifier.fillMaxWidth().height(48.dp).testTag("bottom"))
                    },
                ) {
                    Box(Modifier.fillMaxSize().testTag("body"))
                }
            }
        }
        val top = onNodeWithTag("top").getUnclippedBoundsInRoot()
        val body = onNodeWithTag("body").getUnclippedBoundsInRoot()
        val bottom = onNodeWithTag("bottom").getUnclippedBoundsInRoot()
        assertTrue(top.bottom <= body.top, "顶栏必须位于主体内容之上")
        assertTrue(body.bottom <= bottom.top, "底栏必须位于主体内容之下")
    }

    /**
     * 显式容器色绘制到页面外壳，显式内容色发布为原生内容环境。
     */
    @Test
    fun explicitColorsReachContainerAndContentEnvironment() = runComposeUiTest {
        var seenContentColor = Color.Unspecified
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Fluent) {
                NomiScaffold(
                    modifier = Modifier.testTag("scaffold"),
                    colors = NomiScaffoldColors(
                        containerColor = Color(0xFF102030),
                        contentColor = Color(0xFF405060),
                    ),
                ) {
                    Box(Modifier.fillMaxSize()) {
                        val current = LocalContentColor.current
                        SideEffect { seenContentColor = current }
                    }
                }
            }
        }
        runOnIdle { assertEquals(Color(0xFF405060), seenContentColor) }
        val pixels = onNodeWithTag("scaffold").captureToImage().toPixelMap()
        assertEquals(Color(0xFF102030), pixels[2, 2])
    }

    /**
     * 未指定内容色时沿用 Fluent 原生正文内容色，不提前填入中立默认值。
     */
    @Test
    fun defaultContentColorIsNativePrimaryText() = runComposeUiTest {
        var expected = Color.Unspecified
        var seenContentColor = Color.Unspecified
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Fluent) {
                expected = FluentTheme.colors.text.text.primary
                NomiScaffold {
                    Box(Modifier.fillMaxSize()) {
                        val current = LocalContentColor.current
                        SideEffect { seenContentColor = current }
                    }
                }
            }
        }
        runOnIdle { assertEquals(expected, seenContentColor) }
    }
}
