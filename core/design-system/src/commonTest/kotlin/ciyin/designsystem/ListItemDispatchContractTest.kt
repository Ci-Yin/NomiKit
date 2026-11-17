package ciyin.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.list.NomiListItem
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 列表项入口只派发明确参数，不将投影令牌或默认视觉反灌给 renderer。
 */
@OptIn(ExperimentalTestApi::class)
class ListItemDispatchContractTest {
    /**
     * 只提供必填标题时，点击与三个可选槽保持 null，修饰符保持默认。
     */
    @Test
    fun defaultsRemainSparseAndOptionalSlotsStayNull() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiListItem(title = { NomiText("标题") })
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    RecordedListItem(
                        hasModifier = false,
                        hasOnClick = false,
                        hasLeading = false,
                        hasSubtitle = false,
                        hasTrailing = false,
                    )
                ),
                designSystem.listItems,
            )
            assertEquals(setOf("标题"), designSystem.texts.keys)
        }
    }

    /**
     * 显式修饰符、点击回调与三个可选槽原样透传，槽内容仍在组合阶段执行。
     */
    @Test
    fun explicitParametersAreDispatchedUnchanged() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiListItem(
                    title = { NomiText("标题") },
                    modifier = Modifier.testTag("list-item"),
                    onClick = {},
                    leading = { NomiText("前导") },
                    subtitle = { NomiText("副标题") },
                    trailing = { NomiText("尾部") },
                )
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    RecordedListItem(
                        hasModifier = true,
                        hasOnClick = true,
                        hasLeading = true,
                        hasSubtitle = true,
                        hasTrailing = true,
                    )
                ),
                designSystem.listItems,
            )
            assertEquals(setOf("标题", "前导", "副标题", "尾部"), designSystem.texts.keys)
        }
    }
}