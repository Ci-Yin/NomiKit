package ciyin.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import ciyin.designsystem.component.feedback.NomiProgressIndicator
import ciyin.designsystem.component.feedback.NomiProgressIndicatorSpec
import ciyin.designsystem.component.feedback.NomiProgressIndicatorType
import ciyin.designsystem.theme.NomiTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * 进度入口只派发显式进度、形态与专属参数，不补颜色或尺寸。
 */
@OptIn(ExperimentalTestApi::class)
class ProgressDispatchContractTest {
    /**
     * 只传进度值时默认使用线性形态，且不提供专属参数。
     */
    @Test
    fun progressDefaultsUseLinearShape() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiProgressIndicator(progress = 0.25f)
            }
        }
        runOnIdle {
            assertEquals(
                listOf(
                    RecordedProgress(
                        progress = 0.25f,
                        type = NomiProgressIndicatorType.Linear,
                        hasModifier = false,
                    )
                ),
                designSystem.progressIndicators,
            )
        }
    }

    /**
     * 不确定进度与环形形态原样透传，null 不会被本地填成 0f。
     */
    @Test
    fun indeterminateCircularProgressIsDispatchedAsNull() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiProgressIndicator(
                    progress = null,
                    modifier = Modifier.testTag("progress"),
                    type = NomiProgressIndicatorType.Circular,
                )
            }
        }
        runOnIdle {
            val recorded = designSystem.progressIndicators.single()
            assertNull(recorded.progress)
            assertEquals(NomiProgressIndicatorType.Circular, recorded.type)
            assertEquals(true, recorded.hasModifier)
            assertNull(recorded.nativeSpec)
        }
    }

    /**
     * 显式专属参数原样到达 renderer。
     */
    @Test
    fun progressNativeSpecIsDispatchedUnchanged() = runComposeUiTest {
        val designSystem = RecordingDesignSystem()
        val spec = RecordingProgressSpec(marker = "progress")
        setContent {
            NomiTheme(designSystem = designSystem) {
                NomiProgressIndicator(progress = 0.8f, nativeSpec = spec)
            }
        }
        runOnIdle {
            assertEquals(spec, designSystem.progressIndicators.single().nativeSpec)
        }
    }
}

/**
 * 测试用进度专属参数，模拟 adapter 模块定义的具体 spec。
 *
 * @property marker 用于区分实例来源的标记，不参与渲染。
 */
private data class RecordingProgressSpec(val marker: String) : NomiProgressIndicatorSpec
