package ciyin.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import ciyin.designsystem.component.divider.NomiDivider
import ciyin.designsystem.component.divider.NomiDividerOrientation
import ciyin.designsystem.component.icon.NomiIcon
import ciyin.designsystem.icon.Home
import ciyin.designsystem.icon.IconPack
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 图标与分割线入口只派发明确参数，不将中立默认色或统一尺寸反灌给 renderer。
 */
@OptIn(ExperimentalTestApi::class)
class AtomsDispatchContractTest {
    /**
     * 只传必填矢量时，无障碍名称与着色保持未指定，修饰符保持默认。
     */
    @Test
    fun iconDefaultsRemainSparse() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiIcon(imageVector = IconPack.Home, contentDescription = null)
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    RecordedIcon(
                        imageVector = IconPack.Home,
                        contentDescription = null,
                        tint = Color.Unspecified,
                        hasModifier = false,
                    )
                ),
                designSystem.icons,
            )
        }
    }

    /**
     * 显式无障碍名称、着色与修饰符原样透传，透明色仍是有效覆盖。
     */
    @Test
    fun iconExplicitParametersAreDispatchedUnchanged() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiIcon(
                    imageVector = IconPack.Home,
                    contentDescription = "首页",
                    modifier = Modifier.testTag("icon"),
                    tint = Color.Transparent,
                )
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    RecordedIcon(
                        imageVector = IconPack.Home,
                        contentDescription = "首页",
                        tint = Color.Transparent,
                        hasModifier = true,
                    )
                ),
                designSystem.icons,
            )
        }
    }

    /**
     * 分割线默认横向，且粗细与颜色保持未指定。
     */
    @Test
    fun dividerDefaultsToHorizontalAndSparse() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiDivider()
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    RecordedDivider(
                        orientation = NomiDividerOrientation.Horizontal,
                        thickness = null,
                        color = Color.Unspecified,
                        hasModifier = false,
                    )
                ),
                designSystem.dividers,
            )
        }
    }

    /**
     * 显式纵向、粗细、颜色与修饰符原样透传。
     */
    @Test
    fun dividerExplicitParametersAreDispatchedUnchanged() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiDivider(
                    orientation = NomiDividerOrientation.Vertical,
                    modifier = Modifier.testTag("divider"),
                    thickness = 2.dp,
                    color = Color.Red,
                )
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    RecordedDivider(
                        orientation = NomiDividerOrientation.Vertical,
                        thickness = 2.dp,
                        color = Color.Red,
                        hasModifier = true,
                    )
                ),
                designSystem.dividers,
            )
        }
    }
}
