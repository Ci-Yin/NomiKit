package com.ciyin.app.ui.screen.medialibrary

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ciyin.io.File
import ciyin.material.theme.AppTheme
import ciyin.platform.DesktopContext
import ciyin.platform.LocalContext
import ciyin.platform.context.CommonContextFiles
import java.nio.file.Files
import java.nio.file.Path
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue
import org.jetbrains.skia.Image

/** 媒体库测试台宽窄响应式布局测试。 */
@OptIn(ExperimentalTestApi::class)
class MediaLibraryDemoLayoutTest {
    /** 宽视口应呈现左右双栏且保存完整像素快照。 */
    @Test
    fun wideViewportUsesTwoColumns() = runComposeUiTest {
        setMediaLibraryContent(width = WideViewportWidth)
        val matrixBounds = onNodeWithTag(MediaLibraryDemoMatrixTag).fetchSemanticsNode().boundsInRoot
        val detailBounds = onNodeWithTag(MediaLibraryDemoDetailTag).fetchSemanticsNode().boundsInRoot

        assertTrue(abs(matrixBounds.top - detailBounds.top) < LayoutTolerancePx)
        assertTrue(matrixBounds.right <= detailBounds.left)
        onNodeWithTag(MediaLibraryDemoRootTag).writePng(WideScreenshotPath)
    }

    /** 窄视口应上下排列且保留下一区域的可见提示。 */
    @Test
    fun narrowViewportUsesStackedPanels() = runComposeUiTest {
        setMediaLibraryContent(width = NarrowViewportWidth)
        val matrixBounds = onNodeWithTag(MediaLibraryDemoMatrixTag).fetchSemanticsNode().boundsInRoot
        val detailNode = onNodeWithTag(MediaLibraryDemoDetailTag)
        val detailBounds = detailNode.fetchSemanticsNode().boundsInRoot

        assertTrue(matrixBounds.bottom <= detailBounds.top)
        onNodeWithTag(MediaLibraryDemoRootTag).writePng(NarrowTopScreenshotPath)
    }

    /** 窄视口滚动到底部后应完整显示单项命令区。 */
    @Test
    fun narrowViewportShowsItemActionsAtScrollEnd() = runComposeUiTest {
        setMediaLibraryContent(
            width = NarrowViewportWidth,
            initialScroll = Int.MAX_VALUE,
        )
        val rootBounds = onNodeWithTag(MediaLibraryDemoRootTag).fetchSemanticsNode().boundsInRoot
        val actionsBounds = onNodeWithTag(MediaLibraryDemoActionsTag).fetchSemanticsNode().boundsInRoot

        assertTrue(actionsBounds.top >= rootBounds.top)
        assertTrue(actionsBounds.bottom <= rootBounds.bottom)
        onNodeWithTag(MediaLibraryDemoRootTag).writePng(NarrowActionsScreenshotPath)
    }

    /** 在指定宽度的稳定测试视口中渲染混合状态页面。 */
    private fun androidx.compose.ui.test.ComposeUiTest.setMediaLibraryContent(
        width: Dp,
        initialScroll: Int = 0,
    ) {
        val context = DesktopContext(
            CommonContextFiles(
                cacheDir = File("build/media-library-demo-layout/cache"),
                dataDir = File("build/media-library-demo-layout/data"),
                defaultBaseMediaCacheDir = File("build/media-library-demo-layout/media-cache"),
            ),
        )
        setContent {
            CompositionLocalProvider(LocalContext provides context) {
                AppTheme {
                    Box(
                        modifier = Modifier
                            .testTag(ViewportTag)
                            .size(width = width, height = ViewportHeight),
                    ) {
                        MediaLibraryDemoContent(
                            state = visualTestState(),
                            onAction = {},
                            contentScrollState = ScrollState(initialScroll),
                        )
                    }
                }
            }
        }
        waitForIdle()
    }

    /** 将节点实际渲染保存为 PNG 并确认像素数据非空。 */
    private fun SemanticsNodeInteraction.writePng(path: Path) {
        val bitmap = captureToImage().asSkiaBitmap()
        val bytes = Image.makeFromBitmap(bitmap).encodeToData()?.bytes
            ?: error("无法编码媒体库布局截图")
        Files.createDirectories(path.parent)
        Files.write(path, bytes)
        assertTrue(bytes.isNotEmpty())
    }

    /** 创建覆盖成功、不支持、失败和已删除状态的视觉测试数据。 */
    private fun visualTestState(): MediaLibraryDemoUiState {
        val image = MediaLibraryDemoPublishedModel(
            platformId = "content://media/external/images/media/42",
            uri = "content://media/external/images/media/42",
            displayName = "nomikit-media-library-image-42.png",
            mimeType = "image/png",
            size = 2636L,
        )
        return MediaLibraryDemoUiState(
            selectedSampleId = MediaLibraryDemoSampleId.Image,
            items = defaultMediaLibraryDemoItems.map { item ->
                when (item.sampleId) {
                    MediaLibraryDemoSampleId.Image -> item.copy(
                        phase = MediaLibraryDemoPhase.Published,
                        sourceSize = 2636L,
                        published = image,
                        exists = true,
                    )
                    MediaLibraryDemoSampleId.Video -> item.copy(
                        phase = MediaLibraryDemoPhase.Deleted,
                        sourceSize = 134_178L,
                        exists = false,
                    )
                    MediaLibraryDemoSampleId.Audio -> item.copy(
                        phase = MediaLibraryDemoPhase.Unsupported,
                        sourceSize = 176_478L,
                        error = MediaLibraryDemoErrorModel(MediaLibraryDemoErrorType.Unsupported),
                    )
                    MediaLibraryDemoSampleId.Download -> item.copy(
                        phase = MediaLibraryDemoPhase.Failed,
                        sourceSize = 149L,
                        error = MediaLibraryDemoErrorModel(MediaLibraryDemoErrorType.PermissionDenied),
                    )
                }
            },
            batch = MediaLibraryDemoBatchState.Completed(
                operation = MediaLibraryDemoBatchOperation.Publish,
                summary = MediaLibraryDemoBatchSummary(
                    processed = 4,
                    succeeded = 2,
                    unsupported = 1,
                    failed = 1,
                ),
            ),
        )
    }

    private companion object {
        /** 宽屏测试视口宽度。 */
        val WideViewportWidth = 1024.dp

        /** 窄屏测试视口宽度。 */
        val NarrowViewportWidth = 480.dp

        /** 两种布局共用的可见视口高度。 */
        val ViewportHeight = 720.dp

        /** 浮点布局边界比较容差。 */
        const val LayoutTolerancePx = 1f

        /** 测试视口语义标签。 */
        const val ViewportTag = "media-library-demo-viewport"

        /** 宽屏渲染输出路径。 */
        val WideScreenshotPath = Path.of("build/reports/media-library-demo/wide.png")

        /** 窄屏顶部渲染输出路径。 */
        val NarrowTopScreenshotPath = Path.of("build/reports/media-library-demo/narrow-top.png")

        /** 窄屏命令区渲染输出路径。 */
        val NarrowActionsScreenshotPath = Path.of("build/reports/media-library-demo/narrow-actions.png")
    }
}
