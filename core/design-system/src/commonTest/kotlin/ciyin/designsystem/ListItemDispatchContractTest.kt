package ciyin.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.list.NomiListItem
import ciyin.designsystem.component.list.NomiListItemSpec
import ciyin.designsystem.component.text.NomiText
import ciyin.designsystem.runtime.NomiNativeSpecs
import ciyin.designsystem.runtime.findSpec
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 列表项入口只派发明确参数，不将投影令牌或默认视觉反灌给 renderer。
 */
@OptIn(ExperimentalTestApi::class)
class ListItemDispatchContractTest {
    /**
     * 只提供必填标题时，点击、三个可选槽与专属参数保持 null，修饰符保持默认。
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

    /**
     * 单一列表项专属参数原样派发，且不依赖容器即可按类型取用。
     */
    @Test
    fun nativeSpecIsDispatchedUnchangedAndResolvesDirectly() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val spec = RecordingNativeSpec(marker = "first")
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiListItem(title = { NomiText("标题") }, nativeSpec = spec)
            }
        }
        runOnIdle {
            val dispatched = designSystem.listItems.single().nativeSpec
            assertEquals(spec, dispatched)
            assertEquals(spec, dispatched.findSpec<RecordingNativeSpec>())
        }
    }

    /**
     * 同一族的多个参数组合后一次派发，按类型取用互不影响；其它族或未携带的类型返回 null。
     */
    @Test
    fun composedNativeSpecsKeepEveryEntryAndResolveByType() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val first = RecordingNativeSpec(marker = "first")
        val second = OtherNativeSpec(marker = "second")
        val composed = first + second
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiListItem(title = { NomiText("标题") }, nativeSpec = composed)
            }
        }
        runOnIdle {
            val dispatched = designSystem.listItems.single().nativeSpec
            assertEquals(composed, dispatched)
            assertEquals(2, (dispatched as NomiNativeSpecs<*>).specs.size)
            assertEquals(first, dispatched.findSpec<RecordingNativeSpec>())
            assertEquals(second, dispatched.findSpec<OtherNativeSpec>())
            assertNull(dispatched.findSpec<UnusedNativeSpec>())
        }
    }
}

/**
 * 测试用列表项专属参数，模拟 adapter 模块定义的具体 spec。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class RecordingNativeSpec(
    val marker: String,
) : NomiListItemSpec

/**
 * 同一族的第二个测试专属参数，用于验证按类型隔离。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class OtherNativeSpec(
    val marker: String,
) : NomiListItemSpec

/**
 * 从未传入的列表项专属参数，用于验证未携带类型时返回 null。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class UnusedNativeSpec(
    val marker: String,
) : NomiListItemSpec
