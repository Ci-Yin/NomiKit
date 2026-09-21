package com.ciyin.app.ui.screen.mediadirectory

import ciyin.io.File
import ciyin.io.deleteRecursively
import ciyin.io.resolve
import ciyin.io.writeText
import ciyin.media.directory.MediaDirectoryAccess
import ciyin.media.directory.MediaDirectoryAccessToken
import ciyin.media.directory.MediaDirectoryEntry
import ciyin.media.directory.MediaDirectoryError
import ciyin.media.directory.MediaDirectoryException
import ciyin.media.directory.MediaDirectorySession
import ciyin.platform.DesktopContext
import ciyin.platform.context.CommonContextFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

/** 用户媒体目录示例 ViewModel 测试。 */
@OptIn(ExperimentalCoroutinesApi::class)
class MediaDirectoryDemoViewModelTest {
    /** 选择目录、提交、检查、删除应形成完整闭环。 */
    @Test
    fun pickCommitExistsDeleteRoundTrip() = runTest(timeout = 5.minutes) {
        val recorded = RecordingMediaDirectorySession(initialDirectory = "/tmp/nomikit-demo")
        withDemoViewModel(session = recorded) { viewModel, context ->
            viewModel.dispatchAction(MediaDirectoryDemoAction.PickDirectoryClick)
            val granted = viewModel.awaitAccessState(MediaDirectoryDemoAccessState.Granted)

            assertEquals("/tmp/nomikit-demo", granted.access?.canonicalPath)
            assertEquals("demo", granted.access?.displayName)

            viewModel.dispatchAction(MediaDirectoryDemoAction.ValidateClick)
            viewModel.awaitAccessState(MediaDirectoryDemoAccessState.Granted)

            viewModel.dispatchAction(MediaDirectoryDemoAction.CommitSampleClick)
            val committed = viewModel.awaitEntryReady()
            assertEquals(1, recorded.commitCalls.size)
            assertEquals(recorded.tokenValue(), committed.lastEntry?.location)
            assertTrue(committed.lastEntry?.exists == true)
            runCurrent()

            viewModel.dispatchAction(MediaDirectoryDemoAction.ExistsClick)
            viewModel.awaitExistsCheckRecorded(recorded)
            assertEquals(1, recorded.existsCalls.size)
            runCurrent()

            viewModel.dispatchAction(MediaDirectoryDemoAction.DeleteClick)
            viewModel.awaitEntryCleared()
            assertEquals(1, recorded.deleteCalls.size)
            assertNull(viewModel.state.value.lastEntry)
        }
    }

    /** 用户取消目录选择不应记为错误。 */
    @Test
    fun pickerCancellationKeepsIdleState() = runTest {
        val recorded = CancellingMediaDirectorySession()
        withDemoViewModel(session = recorded) { viewModel, context ->
            viewModel.dispatchAction(MediaDirectoryDemoAction.PickDirectoryClick)
            runCurrent()
            val state = viewModel.state.value
            assertEquals(MediaDirectoryDemoAccessState.NotGranted, state.accessState)
            assertEquals(MediaDirectoryDemoPhase.Idle, state.phase)
            assertNull(state.error)
        }
    }

    /** 结构化错误应当被记录到页面 error。 */
    @Test
    fun commitFailureRecordsError() = runTest {
        val recorded = RecordingMediaDirectorySession(
            initialDirectory = "/tmp/nomikit-demo",
            commitError = MediaDirectoryException(MediaDirectoryError.AlreadyExists("demo.txt")),
        )
        withDemoViewModel(session = recorded) { viewModel, context ->
            viewModel.dispatchAction(MediaDirectoryDemoAction.PickDirectoryClick)
            viewModel.awaitAccessState(MediaDirectoryDemoAccessState.Granted)

            viewModel.dispatchAction(MediaDirectoryDemoAction.CommitSampleClick)
            runCurrent()
            val state = viewModel.state.value
            assertEquals(MediaDirectoryDemoErrorType.AlreadyExists, state.error?.type)
        }
    }

    /** validate 抛出 AccessRevoked 应切换到 Revoked 状态。 */
    @Test
    fun validateFailureMarksRevoked() = runTest {
        val recorded = RecordingMediaDirectorySession(
            initialDirectory = "/tmp/nomikit-demo",
            validateError = MediaDirectoryException(MediaDirectoryError.AccessRevoked("失效")),
        )
        withDemoViewModel(session = recorded) { viewModel, context ->
            viewModel.dispatchAction(MediaDirectoryDemoAction.PickDirectoryClick)
            viewModel.awaitAccessState(MediaDirectoryDemoAccessState.Granted)

            viewModel.dispatchAction(MediaDirectoryDemoAction.ValidateClick)
            viewModel.awaitAccessState(MediaDirectoryDemoAccessState.Revoked)
            assertEquals(
                MediaDirectoryDemoAccessState.Revoked,
                viewModel.state.value.accessState,
            )
        }
    }

    /** 在隔离临时目录和测试 Main dispatcher 上执行测试台。 */
    private suspend fun kotlinx.coroutines.test.TestScope.withDemoViewModel(
        session: MediaDirectorySession,
        block: suspend (
            viewModel: MediaDirectoryDemoViewModel,
            context: DesktopContext,
        ) -> Unit,
    ) {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val root = File(Files.createTempDirectory("nomikit-media-directory-demo-").toString())
        val cache = root.resolve("cache").apply { mkdirs() }
        val context = DesktopContext(
            CommonContextFiles(
                cacheDir = cache,
                dataDir = root.resolve("data"),
                defaultBaseMediaCacheDir = root.resolve("media-cache"),
            ),
        )
        val viewModel = MediaDirectoryDemoViewModel(
            sessionOverride = session,
            contextProvider = { context },
            sampleFileName = { "demo.txt" },
        )
        val stateCollection = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect { }
        }
        try {
            runCurrent()
            block(viewModel, context)
        } finally {
            stateCollection.cancelAndJoin()
            Dispatchers.resetMain()
            assertTrue(root.deleteRecursively())
        }
    }

    /** 等待页面授权状态匹配。 */
    private suspend fun MediaDirectoryDemoViewModel.awaitAccessState(
        expected: MediaDirectoryDemoAccessState,
    ): MediaDirectoryDemoUiState = state.first { current -> current.accessState == expected }

    /** 等待页面进入存在条目状态并返回最新状态。 */
    private suspend fun MediaDirectoryDemoViewModel.awaitEntryReady(): MediaDirectoryDemoUiState =
        state.first { current -> current.lastEntry != null }

    /** 等待最近条目被清空。 */
    private suspend fun MediaDirectoryDemoViewModel.awaitEntryCleared(): MediaDirectoryDemoUiState =
        state.first { current -> current.lastEntry == null }

    /**
     * 等待【exists】调用至少发生一次，避免对异步调度做时序假设。
     */
    private suspend fun MediaDirectoryDemoViewModel.awaitExistsCheckRecorded(
        session: RecordingMediaDirectorySession,
    ) {
        while (session.existsCalls.isEmpty()) {
            kotlinx.coroutines.yield()
        }
    }
}

/** 简单 session stub，按固定路径返回 token 并模拟可选异常。 */
private class RecordingMediaDirectorySession(
    private val initialDirectory: String,
    private val commitError: MediaDirectoryException? = null,
    private val validateError: MediaDirectoryException? = null,
) : MediaDirectorySession {
    /** commit 调用记录：(token, fileName, mimeType)。 */
    val commitCalls = mutableListOf<Triple<String, String, String>>()

    /** exists 调用记录：(token, location)。 */
    val existsCalls = mutableListOf<Pair<String, String>>()

    /** delete 调用记录：(token, location)。 */
    val deleteCalls = mutableListOf<Pair<String, String>>()

    private var counter = 1

    /** 下一个 commit 的预测 location。 */
    fun tokenValue(): String = "fake-location-${counter - 1}"

    override suspend fun requestAccess(): MediaDirectoryAccess {
        val token = MediaDirectoryAccessToken("token-${counter++}")
        return MediaDirectoryAccess(
            accessToken = token,
            displayName = "demo",
            canonicalPath = initialDirectory,
        )
    }

    override suspend fun validate(accessToken: MediaDirectoryAccessToken) {
        validateError?.let { throw it }
    }

    override suspend fun commit(
        accessToken: MediaDirectoryAccessToken,
        source: ciyin.io.File,
        fileName: String,
        mimeType: String,
    ): MediaDirectoryEntry {
        commitCalls += Triple(accessToken.value, fileName, mimeType)
        commitError?.let { throw it }
        val location = "fake-location-${counter++}"
        source.writeText("")
        return MediaDirectoryEntry(
            accessToken = accessToken,
            fileName = fileName,
            location = location,
            mimeType = mimeType,
            size = 0L,
        )
    }

    override suspend fun delete(accessToken: MediaDirectoryAccessToken, location: String) {
        deleteCalls += accessToken.value to location
    }

    override suspend fun exists(accessToken: MediaDirectoryAccessToken, location: String): Boolean {
        existsCalls += accessToken.value to location
        return true
    }
}

/** 总是抛 Cancelled 的 session。 */
private class CancellingMediaDirectorySession : MediaDirectorySession {
    override suspend fun requestAccess(): MediaDirectoryAccess =
        throw MediaDirectoryException(MediaDirectoryError.Cancelled)

    override suspend fun validate(accessToken: MediaDirectoryAccessToken) = Unit

    override suspend fun commit(
        accessToken: MediaDirectoryAccessToken,
        source: ciyin.io.File,
        fileName: String,
        mimeType: String,
    ): MediaDirectoryEntry = error("not used")

    override suspend fun delete(accessToken: MediaDirectoryAccessToken, location: String) = Unit

    override suspend fun exists(accessToken: MediaDirectoryAccessToken, location: String): Boolean =
        false
}
