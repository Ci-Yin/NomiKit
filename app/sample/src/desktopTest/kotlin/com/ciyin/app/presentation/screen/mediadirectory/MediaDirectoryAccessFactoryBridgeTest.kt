package com.ciyin.app.presentation.screen.mediadirectory

import ciyin.io.File
import ciyin.io.deleteRecursively
import ciyin.media.directory.MediaDirectoryAccess
import ciyin.media.directory.MediaDirectoryAccessToken
import ciyin.media.directory.MediaDirectoryError
import ciyin.media.directory.MediaDirectoryException
import ciyin.media.directory.MediaDirectorySession
import ciyin.platform.DesktopContext
import ciyin.platform.context.CommonContextFiles
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 验证 [MediaDirectoryDemoViewModel] 与 `accessFactoryOverride` 注入点的协作行为：
 * - 注入非空 factory 时，ViewModel 优先调用它而不是会话内置 [MediaDirectorySession.requestAccess]。
 * - factory 返回 null 时，应进入 `DirectoryCancelled` 状态而不报错。
 * - factory 抛 [MediaDirectoryException] 时，按错误类型分发到 OperationFailed 或 DirectoryCancelled。
 * - 未注入 factory 时，仍走 [MediaDirectorySession.requestAccess]。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MediaDirectoryAccessFactoryBridgeTest {

    /** 注入的 factory 返回 Access，应直接进入 Granted，不调会话内置 launcher。 */
    @Test
    fun injectedFactoryWinsOverSessionRequestAccess() = runTest {
        val session = RecordingSession(tokenValue = "session-token")
        val access = MediaDirectoryAccess(
            accessToken = MediaDirectoryAccessToken("injected-token"),
            displayName = "injected",
            canonicalPath = "/injected/path",
        )
        var factoryCalls = 0
        val factory: suspend () -> MediaDirectoryAccess? = {
            factoryCalls += 1
            access
        }
        withDemoViewModel(session = session, accessFactoryOverride = factory) { viewModel, _ ->
            viewModel.dispatchAction(MediaDirectoryDemoAction.PickDirectoryClick)
            viewModel.awaitAccessState(MediaDirectoryDemoAccessState.Granted)
            val state = viewModel.state.value
            assertEquals("injected-token", state.access?.token?.value)
            assertEquals("injected", state.access?.displayName)
            // 会话内置的 requestAccess 不应被调用。
            assertEquals(0, session.requestAccessCalls)
            assertEquals(1, factoryCalls)
        }
    }

    /** factory 返回 null 应走 DirectoryCancelled，且不调会话 requestAccess。 */
    @Test
    fun factoryReturningNullMarksCancelled() = runTest {
        val session = RecordingSession(tokenValue = "session-token")
        val factory: suspend () -> MediaDirectoryAccess? = suspend { null }
        withDemoViewModel(session = session, accessFactoryOverride = factory) { viewModel, _ ->
            viewModel.dispatchAction(MediaDirectoryDemoAction.PickDirectoryClick)
            runCurrent()
            val state = viewModel.state.value
            assertEquals(MediaDirectoryDemoAccessState.NotGranted, state.accessState)
            assertEquals(MediaDirectoryDemoPhase.Idle, state.phase)
            assertNull(state.error)
            assertEquals(0, session.requestAccessCalls)
        }
    }

    /** factory 抛 [MediaDirectoryException] 携带 `Cancelled` 应走 DirectoryCancelled。 */
    @Test
    fun factoryCancelledMarksCancelled() = runTest {
        val session = RecordingSession(tokenValue = "session-token")
        val factory: suspend () -> MediaDirectoryAccess? = suspend {
            throw MediaDirectoryException(MediaDirectoryError.Cancelled)
        }
        withDemoViewModel(session = session, accessFactoryOverride = factory) { viewModel, _ ->
            viewModel.dispatchAction(MediaDirectoryDemoAction.PickDirectoryClick)
            runCurrent()
            val state = viewModel.state.value
            assertEquals(MediaDirectoryDemoAccessState.NotGranted, state.accessState)
            assertEquals(MediaDirectoryDemoPhase.Idle, state.phase)
            assertNull(state.error)
        }
    }

    /** factory 抛 [MediaDirectoryException] 携带非 Cancelled 应走 OperationFailed。 */
    @Test
    fun factoryFailureMarksOperationFailed() = runTest {
        val session = RecordingSession(tokenValue = "session-token")
        val factory: suspend () -> MediaDirectoryAccess? = suspend {
            throw MediaDirectoryException(MediaDirectoryError.PermissionDenied("拒绝"))
        }
        withDemoViewModel(session = session, accessFactoryOverride = factory) { viewModel, _ ->
            viewModel.dispatchAction(MediaDirectoryDemoAction.PickDirectoryClick)
            runCurrent()
            val state = viewModel.state.value
            assertEquals(MediaDirectoryDemoAccessState.NotGranted, state.accessState)
            assertEquals(MediaDirectoryDemoPhase.Idle, state.phase)
            assertNotNull(state.error)
            assertEquals(MediaDirectoryDemoErrorType.PermissionDenied, state.error?.type)
        }
    }

    /** 未注入 factory 时应回退到会话内置 [MediaDirectorySession.requestAccess]。 */
    @Test
    fun withoutInjectedFactoryFallsBackToSession() = runTest {
        val session = RecordingSession(tokenValue = "session-token")
        withDemoViewModel(session = session, accessFactoryOverride = null) { viewModel, _ ->
            viewModel.dispatchAction(MediaDirectoryDemoAction.PickDirectoryClick)
            viewModel.awaitAccessState(MediaDirectoryDemoAccessState.Granted)
            val state = viewModel.state.value
            assertEquals("session-token-1", state.access?.token?.value)
            assertEquals(1, session.requestAccessCalls)
        }
    }

    /** 提供 [MediaDirectoryAccessFactoryBridgeTest] 自身的 withDemoViewModel，避免依赖外部 helper。 */
    private suspend fun kotlinx.coroutines.test.TestScope.withDemoViewModel(
        session: MediaDirectorySession,
        accessFactoryOverride: (suspend () -> MediaDirectoryAccess?)?,
        block: suspend (MediaDirectoryDemoViewModel, DesktopContext) -> Unit,
    ) {
        kotlinx.coroutines.Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val root = File(Files.createTempDirectory("nomikit-media-directory-bridge-").toString())
        try {
            val cache = File(root, "cache").apply { mkdirs() }
            val data = File(root, "data")
            val media = File(root, "media-cache")
            val context = DesktopContext(
                CommonContextFiles(
                    cacheDir = cache,
                    dataDir = data,
                    defaultBaseMediaCacheDir = media,
                ),
            )
            val viewModel = MediaDirectoryDemoViewModel(
                sessionOverride = session,
                accessFactoryOverride = accessFactoryOverride,
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
            }
        } finally {
            assertTrue(root.deleteRecursively())
            kotlinx.coroutines.Dispatchers.resetMain()
        }
    }

    /** 等待页面授权状态匹配。 */
    private suspend fun MediaDirectoryDemoViewModel.awaitAccessState(
        expected: MediaDirectoryDemoAccessState,
    ): MediaDirectoryDemoUiState = state.first { current -> current.accessState == expected }

    /** 测试用 Session：记录 requestAccess 次数并按调用顺序生成 token。 */
    private class RecordingSession(private val tokenValue: String) : MediaDirectorySession {
        var requestAccessCalls: Int = 0

        override suspend fun requestAccess(): MediaDirectoryAccess {
            requestAccessCalls += 1
            return MediaDirectoryAccess(
                accessToken = MediaDirectoryAccessToken("$tokenValue-$requestAccessCalls"),
                displayName = "demo",
                canonicalPath = "/tmp/demo",
            )
        }

        override suspend fun validate(accessToken: MediaDirectoryAccessToken) = Unit

        override suspend fun commit(
            accessToken: MediaDirectoryAccessToken,
            source: File,
            fileName: String,
            mimeType: String,
        ): ciyin.media.directory.MediaDirectoryEntry =
            ciyin.media.directory.MediaDirectoryEntry(
                accessToken = accessToken,
                fileName = fileName,
                location = "$fileName-location",
                mimeType = mimeType,
                size = 0L,
            )

        override suspend fun delete(accessToken: MediaDirectoryAccessToken, location: String) = Unit

        override suspend fun exists(
            accessToken: MediaDirectoryAccessToken,
            location: String
        ): Boolean = true
    }
}
