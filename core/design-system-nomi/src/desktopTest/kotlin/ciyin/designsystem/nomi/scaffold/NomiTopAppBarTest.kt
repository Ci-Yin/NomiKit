package ciyin.designsystem.nomi.scaffold

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.scaffold.NomiTopAppBar
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.nomi.Default
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Nomi 顶栏适配：appBar 令牌高度与三个槽位的顺序。
 */
@OptIn(ExperimentalTestApi::class)
class NomiTopAppBarTest {
    /**
     * 默认高度取主题的 appBar 令牌。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun defaultHeightUsesAppBarToken() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default) {
                NomiTopAppBar(
                    title = { NomiText("标题") },
                    modifier = Modifier.testTag("topBar"),
                )
            }
        }
        onNodeWithTag("topBar").assertHeightIsEqualTo(56.dp)
    }

    /**
     * 导航图标、标题与操作按从左到右的顺序排布。
     *
     * @return 当前平台的 Compose 测试执行结果。
     */
    @Test
    fun slotsAreComposedInOrder() = runComposeUiTest {
        setContent {
            NomiTheme(designSystem = NomiDesignSystem.Default) {
                NomiTopAppBar(
                    title = { NomiText("标题", modifier = Modifier.testTag("title")) },
                    navigationIcon = {
                        NomiText(
                            "返回",
                            modifier = Modifier.testTag("navigation")
                        )
                    },
                    actions = { NomiText("操作", modifier = Modifier.testTag("action")) },
                )
            }
        }
        val navigation = onNodeWithTag("navigation").getUnclippedBoundsInRoot()
        val title = onNodeWithTag("title").getUnclippedBoundsInRoot()
        val action = onNodeWithTag("action").getUnclippedBoundsInRoot()
        assertTrue(navigation.right <= title.left, "导航图标必须位于标题左侧")
        assertTrue(title.right <= action.left, "操作槽必须位于标题右侧")
    }
}
