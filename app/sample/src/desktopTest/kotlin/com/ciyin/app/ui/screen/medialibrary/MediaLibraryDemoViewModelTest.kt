package com.ciyin.app.ui.screen.medialibrary

import ciyin.io.File
import ciyin.io.deleteRecursively
import ciyin.io.resolve
import ciyin.io.source
import ciyin.media.library.MediaCollection
import ciyin.media.library.MediaLibrary
import ciyin.media.library.MediaLibraryError
import ciyin.media.library.MediaLibraryException
import ciyin.media.library.MediaPublishRequest
import ciyin.media.library.PublishedMedia
import ciyin.platform.DesktopContext
import ciyin.platform.context.CommonContextFiles
import java.nio.file.Files
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okio.buffer
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** 系统媒体库四类媒体测试台行为测试。 */
@OptIn(ExperimentalCoroutinesApi::class)
class MediaLibraryDemoViewModelTest {
    /** 批量发布应按固定顺序构造四类请求并写入各自源字节。 */
    @Test
    fun publishAllBuildsFourRequestsInFixedOrder() = runTest {
        val permissionCalls = mutableListOf<MediaCollection>()
        withDemoViewModel(
            permissionRequester = { _, collection ->
                permissionCalls += collection
                true
            },
        ) { viewModel, library, context ->
            viewModel.dispatchAction(MediaLibraryDemoAction.PublishAllClick(context))
            val batch = viewModel.awaitCompletedBatch()

            assertEquals(ExpectedCollections, library.requests.map { request -> request.collection })
            assertEquals(ExpectedMimeTypes, library.requests.map { request -> request.mimeType })
            assertEquals(ExpectedDisplayNames, library.requests.map { request -> request.displayName })
            assertEquals(ExpectedSourceNames, library.requests.map { request -> request.source.name })
            assertTrue(library.requests.all { request -> request.relativeDirectory == null })
            assertEquals(ExpectedCollections, permissionCalls)
            mediaLibraryDemoSamples.forEach { sample ->
                assertContentEquals(
                    TestBytes.getValue(sample.id),
                    library.publishedBytes.getValue(sample.collection),
                )
            }
            assertEquals(4, batch.summary.succeeded)
            assertEquals(4, batch.summary.processed)
        }
    }

    /** 活动引用应禁止重复发布，删除后应允许重新发布且不影响其他项目。 */
    @Test
    fun activeReferenceBlocksDuplicateAndDeleteAllowsRepublish() = runTest {
        withDemoViewModel { viewModel, library, context ->
            viewModel.dispatchAction(
                MediaLibraryDemoAction.PublishClick(context, MediaLibraryDemoSampleId.Image),
            )
            viewModel.awaitItemPhase(MediaLibraryDemoSampleId.Image, MediaLibraryDemoPhase.Published)

            viewModel.dispatchAction(
                MediaLibraryDemoAction.PublishClick(context, MediaLibraryDemoSampleId.Image),
            )
            runCurrent()
            assertEquals(1, library.publishCount(MediaCollection.Images))
            assertEquals(
                MediaLibraryDemoPhase.Ready,
                viewModel.state.value.item(MediaLibraryDemoSampleId.Video).phase,
            )

            viewModel.dispatchAction(
                MediaLibraryDemoAction.DeleteClick(context, MediaLibraryDemoSampleId.Image),
            )
            val deleted = viewModel.awaitItemPhase(
                MediaLibraryDemoSampleId.Image,
                MediaLibraryDemoPhase.Deleted,
            )
            assertFalse(deleted.item(MediaLibraryDemoSampleId.Image).exists ?: true)
            assertTrue(deleted.item(MediaLibraryDemoSampleId.Image).published != null)

            viewModel.dispatchAction(
                MediaLibraryDemoAction.PublishClick(context, MediaLibraryDemoSampleId.Image),
            )
            viewModel.awaitItemPhase(MediaLibraryDemoSampleId.Image, MediaLibraryDemoPhase.Published)
            assertEquals(2, library.publishCount(MediaCollection.Images))
        }
    }

    /** 检查到媒体不存在时应释放活动引用并允许再次发布。 */
    @Test
    fun missingReferenceCanBePublishedAgain() = runTest {
        withDemoViewModel { viewModel, library, context ->
            viewModel.dispatchAction(
                MediaLibraryDemoAction.PublishClick(context, MediaLibraryDemoSampleId.Video),
            )
            viewModel.awaitItemPhase(MediaLibraryDemoSampleId.Video, MediaLibraryDemoPhase.Published)
            library.existsOverrides[MediaCollection.Videos] = false

            viewModel.dispatchAction(
                MediaLibraryDemoAction.ExistsClick(context, MediaLibraryDemoSampleId.Video),
            )
            viewModel.awaitItemPhase(MediaLibraryDemoSampleId.Video, MediaLibraryDemoPhase.Missing)

            viewModel.dispatchAction(
                MediaLibraryDemoAction.PublishClick(context, MediaLibraryDemoSampleId.Video),
            )
            viewModel.awaitItemPhase(MediaLibraryDemoSampleId.Video, MediaLibraryDemoPhase.Published)
            assertEquals(2, library.publishCount(MediaCollection.Videos))
        }
    }

    /** 单项结构化错误应只标记目标项目。 */
    @Test
    fun individualErrorDoesNotPolluteOtherItems() = runTest {
        val library = RecordingMediaLibrary().apply {
            publishFailures[MediaCollection.Audio] = MediaLibraryException(MediaLibraryError.Io())
        }
        withDemoViewModel(library = library) { viewModel, _, context ->
            viewModel.dispatchAction(
                MediaLibraryDemoAction.PublishClick(context, MediaLibraryDemoSampleId.Audio),
            )
            val state = viewModel.awaitItemPhase(
                MediaLibraryDemoSampleId.Audio,
                MediaLibraryDemoPhase.Failed,
            )

            assertEquals(
                MediaLibraryDemoErrorType.Io,
                state.item(MediaLibraryDemoSampleId.Audio).error?.type,
            )
            assertEquals(
                MediaLibraryDemoPhase.Ready,
                state.item(MediaLibraryDemoSampleId.Image).phase,
            )
        }
    }

    /** Unsupported 应计入预期结果并继续发布剩余项目。 */
    @Test
    fun unsupportedContinuesPublishBatch() = runTest {
        val library = RecordingMediaLibrary().apply {
            publishFailures[MediaCollection.Videos] = MediaLibraryException(
                MediaLibraryError.Unsupported("测试平台不支持视频发布"),
            )
        }
        withDemoViewModel(library = library) { viewModel, _, context ->
            viewModel.dispatchAction(MediaLibraryDemoAction.PublishAllClick(context))
            val batch = viewModel.awaitCompletedBatch()

            assertEquals(ExpectedCollections, library.requests.map { request -> request.collection })
            assertEquals(3, batch.summary.succeeded)
            assertEquals(1, batch.summary.unsupported)
            assertEquals(0, batch.summary.failed)
            assertEquals(
                MediaLibraryDemoPhase.Unsupported,
                viewModel.state.value.item(MediaLibraryDemoSampleId.Video).phase,
            )
        }
    }

    /** 普通 I/O 错误应只标记当前项目并继续批量发布。 */
    @Test
    fun ioFailureContinuesPublishBatch() = runTest {
        val library = RecordingMediaLibrary().apply {
            publishFailures[MediaCollection.Images] = MediaLibraryException(MediaLibraryError.Io())
        }
        withDemoViewModel(library = library) { viewModel, _, context ->
            viewModel.dispatchAction(MediaLibraryDemoAction.PublishAllClick(context))
            val batch = viewModel.awaitCompletedBatch()

            assertEquals(ExpectedCollections, library.requests.map { request -> request.collection })
            assertEquals(3, batch.summary.succeeded)
            assertEquals(1, batch.summary.failed)
            assertEquals(4, batch.summary.processed)
        }
    }

    /** 权限拒绝应终止后续项目且输出停止状态。 */
    @Test
    fun permissionDeniedStopsPublishBatch() = runTest {
        val permissionCalls = mutableListOf<MediaCollection>()
        withDemoViewModel(
            permissionRequester = { _, collection ->
                permissionCalls += collection
                collection != MediaCollection.Videos
            },
        ) { viewModel, library, context ->
            viewModel.dispatchAction(MediaLibraryDemoAction.PublishAllClick(context))
            val batch = viewModel.awaitStoppedBatch()

            assertEquals(listOf(MediaCollection.Images), library.requests.map { it.collection })
            assertEquals(
                listOf(MediaCollection.Images, MediaCollection.Videos),
                permissionCalls,
            )
            assertEquals(1, batch.summary.succeeded)
            assertEquals(1, batch.summary.failed)
            assertEquals(2, batch.summary.processed)
            assertEquals(MediaLibraryDemoErrorType.PermissionDenied, batch.error.type)
        }
    }

    /** 空间不足应在当前项目失败后终止后续项目。 */
    @Test
    fun noSpaceStopsPublishBatch() = runTest {
        val library = RecordingMediaLibrary().apply {
            publishFailures[MediaCollection.Audio] = MediaLibraryException(MediaLibraryError.NoSpace())
        }
        withDemoViewModel(library = library) { viewModel, _, context ->
            viewModel.dispatchAction(MediaLibraryDemoAction.PublishAllClick(context))
            val batch = viewModel.awaitStoppedBatch()

            assertEquals(
                listOf(MediaCollection.Images, MediaCollection.Videos, MediaCollection.Audio),
                library.requests.map { request -> request.collection },
            )
            assertEquals(2, batch.summary.succeeded)
            assertEquals(1, batch.summary.failed)
            assertEquals(3, batch.summary.processed)
            assertEquals(MediaLibraryDemoErrorType.NoSpace, batch.error.type)
        }
    }

    /** 检查和清理批次应跳过没有活动引用的项目并保留最近发布结果。 */
    @Test
    fun checkAndDeleteBatchesSkipItemsWithoutReferences() = runTest {
        withDemoViewModel { viewModel, library, context ->
            listOf(MediaLibraryDemoSampleId.Image, MediaLibraryDemoSampleId.Audio).forEach { sampleId ->
                viewModel.dispatchAction(MediaLibraryDemoAction.PublishClick(context, sampleId))
                viewModel.awaitItemPhase(sampleId, MediaLibraryDemoPhase.Published)
            }

            viewModel.dispatchAction(MediaLibraryDemoAction.ExistsAllClick(context))
            val checked = viewModel.awaitCompletedBatch(MediaLibraryDemoBatchOperation.Check)
            assertEquals(2, checked.summary.succeeded)
            assertEquals(2, checked.summary.skipped)
            assertEquals(
                listOf("exists:Images", "exists:Audio"),
                library.operations.filter { operation -> operation.startsWith("exists:") },
            )

            viewModel.dispatchAction(MediaLibraryDemoAction.DeleteAllClick(context))
            val deleted = viewModel.awaitCompletedBatch(MediaLibraryDemoBatchOperation.Delete)
            assertEquals(2, deleted.summary.succeeded)
            assertEquals(2, deleted.summary.skipped)
            listOf(MediaLibraryDemoSampleId.Image, MediaLibraryDemoSampleId.Audio).forEach { sampleId ->
                val item = viewModel.state.value.item(sampleId)
                assertEquals(MediaLibraryDemoPhase.Deleted, item.phase)
                assertTrue(item.published != null)
                assertFalse(item.hasActiveReference)
            }
        }
    }

    /** 在隔离临时目录和测试 Main dispatcher 上执行测试台。 */
    private suspend fun kotlinx.coroutines.test.TestScope.withDemoViewModel(
        library: RecordingMediaLibrary = RecordingMediaLibrary(),
        permissionRequester: suspend (ciyin.platform.Context, MediaCollection) -> Boolean = { _, _ -> true },
        block: suspend (
            viewModel: MediaLibraryDemoViewModel,
            library: RecordingMediaLibrary,
            context: DesktopContext,
        ) -> Unit,
    ) {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val root = File(Files.createTempDirectory("nomikit-media-library-demo-").toString())
        val cache = root.resolve("cache").apply { mkdirs() }
        val context = DesktopContext(
            CommonContextFiles(
                cacheDir = cache,
                dataDir = root.resolve("data"),
                defaultBaseMediaCacheDir = root.resolve("media-cache"),
            ),
        )
        val viewModel = MediaLibraryDemoViewModel(
            mediaLibraryOverride = library,
            sourceBytesProvider = { sample -> TestBytes.getValue(sample.id) },
            displayNameProvider = { sample ->
                "demo-${sample.displayNameStem}.${sample.sourceFileName.substringAfterLast('.')}"
            },
            permissionRequester = permissionRequester,
        )
        val stateCollection = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect { }
        }
        try {
            runCurrent()
            block(viewModel, library, context)
        } finally {
            stateCollection.cancelAndJoin()
            Dispatchers.resetMain()
            assertTrue(root.deleteRecursively())
        }
    }

    /** 等待指定项目进入目标阶段并返回页面状态。 */
    private suspend fun MediaLibraryDemoViewModel.awaitItemPhase(
        sampleId: MediaLibraryDemoSampleId,
        phase: MediaLibraryDemoPhase,
    ): MediaLibraryDemoUiState = state.first { current ->
        current.item(sampleId).phase == phase
    }

    /** 等待指定类型批量操作正常完成。 */
    private suspend fun MediaLibraryDemoViewModel.awaitCompletedBatch(
        operation: MediaLibraryDemoBatchOperation = MediaLibraryDemoBatchOperation.Publish,
    ): MediaLibraryDemoBatchState.Completed = state.first { current ->
        current.batch is MediaLibraryDemoBatchState.Completed && current.batch.operation == operation
    }.batch.let { batch -> assertIs<MediaLibraryDemoBatchState.Completed>(batch) }

    /** 等待批量操作被阻塞错误提前终止。 */
    private suspend fun MediaLibraryDemoViewModel.awaitStoppedBatch(): MediaLibraryDemoBatchState.Stopped =
        state.first { current -> current.batch is MediaLibraryDemoBatchState.Stopped }
            .batch
            .let { batch -> assertIs<MediaLibraryDemoBatchState.Stopped>(batch) }

    /** 记录页面发起的所有媒体库操作。 */
    private class RecordingMediaLibrary : MediaLibrary {
        /** 按分类配置的发布异常。 */
        val publishFailures = mutableMapOf<MediaCollection, MediaLibraryException>()

        /** 按分类配置的存在性覆盖结果。 */
        val existsOverrides = mutableMapOf<MediaCollection, Boolean>()

        /** 收到的全部发布请求。 */
        val requests = mutableListOf<MediaPublishRequest>()

        /** 按分类保存的发布源字节。 */
        val publishedBytes = mutableMapOf<MediaCollection, ByteArray>()

        /** 按执行顺序保存的平台操作。 */
        val operations = mutableListOf<String>()

        /** 平台标识对应的媒体分类。 */
        private val collectionsByPlatformId = mutableMapOf<String, MediaCollection>()

        /** 当前仍存在的平台标识。 */
        private val existingPlatformIds = mutableSetOf<String>()

        /** 生成唯一平台标识的递增序号。 */
        private var nextPlatformId = 1

        /** 读取发布源文件并返回稳定 fake 引用。 */
        override suspend fun publish(request: MediaPublishRequest): PublishedMedia {
            requests += request
            operations += "publish:${request.collection.name}"
            publishFailures[request.collection]?.let { exception -> throw exception }
            val bytes = request.source.source().buffer().use { source -> source.readByteArray() }
            publishedBytes[request.collection] = bytes
            val platformId = "${request.collection.name}-${nextPlatformId++}"
            collectionsByPlatformId[platformId] = request.collection
            existingPlatformIds += platformId
            return PublishedMedia(
                platformId = platformId,
                uri = "file:///fake/$platformId",
                displayName = request.displayName,
                mimeType = request.mimeType,
                size = bytes.size.toLong(),
            )
        }

        /** 删除 fake 平台引用。 */
        override suspend fun delete(media: PublishedMedia) {
            val collection = collectionsByPlatformId.getValue(media.platformId)
            operations += "delete:${collection.name}"
            existingPlatformIds -= media.platformId
        }

        /** 返回 fake 平台引用的当前存在状态。 */
        override suspend fun exists(media: PublishedMedia): Boolean {
            val collection = collectionsByPlatformId.getValue(media.platformId)
            operations += "exists:${collection.name}"
            return existsOverrides[collection] ?: (media.platformId in existingPlatformIds)
        }

        /** 返回指定分类已经收到的发布次数。 */
        fun publishCount(collection: MediaCollection): Int =
            requests.count { request -> request.collection == collection }
    }

    private companion object {
        /** 每个内置测试使用的可区分二进制内容。 */
        val TestBytes = mapOf(
            MediaLibraryDemoSampleId.Image to byteArrayOf(1, 2),
            MediaLibraryDemoSampleId.Video to byteArrayOf(3, 4, 5),
            MediaLibraryDemoSampleId.Audio to byteArrayOf(6, 7, 8, 9),
            MediaLibraryDemoSampleId.Download to byteArrayOf(10, 11, 12, 13, 14),
        )

        /** 四类测试的固定媒体库分类顺序。 */
        val ExpectedCollections = listOf(
            MediaCollection.Images,
            MediaCollection.Videos,
            MediaCollection.Audio,
            MediaCollection.Downloads,
        )

        /** 四类测试的固定 MIME 顺序。 */
        val ExpectedMimeTypes = listOf("image/png", "video/mp4", "audio/wav", "text/plain")

        /** 四类测试的确定性发布名称。 */
        val ExpectedDisplayNames = listOf(
            "demo-image.png",
            "demo-video.mp4",
            "demo-audio.wav",
            "demo-download.txt",
        )

        /** 四类测试在 cache 中的源文件名。 */
        val ExpectedSourceNames = listOf(
            "media_library_demo.png",
            "media_library_demo.mp4",
            "media_library_demo.wav",
            "media_library_demo.txt",
        )
    }
}
