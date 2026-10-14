package com.ciyin.app.presentation.screen.sample

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.ciyin.app.presentation.screen.aichat.AiChatScreen
import com.ciyin.app.presentation.screen.aiimage.AiImageDemoScreen
import com.ciyin.app.presentation.screen.filedownloader.FileDownloaderDemoScreen
import com.ciyin.app.presentation.screen.filepicker.FilePickerDemoScreen
import com.ciyin.app.presentation.screen.mediadirectory.MediaDirectoryDemoScreen
import com.ciyin.app.presentation.screen.medialibrary.MediaLibraryDemoScreen
import com.ciyin.app.presentation.screen.permissions.PermissionsScreen
import com.ciyin.app.presentation.screen.platformshare.PlatformShareDemoScreen
import com.ciyin.app.presentation.screen.runtimeinfo.RuntimeInfoScreen
import com.ciyin.app.presentation.screen.videoplayer.VideoPlayerDemoScreen

/**
 * 样例模块根 Composable：独立子导航栈
 */
@Composable
fun SampleModuleRoot() {
    val sampleBackStack = rememberNavBackStack(NavSavedStateConfig, SampleHubRouter)

    NavigationBackHandler(rememberNavigationEventState(NavigationEventInfo.None)) {
        sampleBackStack.back()
    }

    NavDisplay(
        backStack = sampleBackStack,
        onBack = {
            sampleBackStack.back()
        },
        entryProvider = entryProvider {
            entry<SampleHubRouter> {
                SampleHubScreen(
                    toNavigate = { sampleBackStack.navigate(it) },
                    onExitSampleModule = { sampleBackStack.back() },
                )
            }
            entry<AiImageDemoRouter> {
                AiImageDemoScreen(
                    onBack = { sampleBackStack.back() },
                )
            }
            entry<AiChatRouter> {
                AiChatScreen(
                    onBack = { sampleBackStack.back() },
                )
            }
            entry<FileDownloaderDemoRouter> {
                FileDownloaderDemoScreen(
                    onBack = { sampleBackStack.back() },
                )
            }
            entry<RuntimeInfoRouter> {
                RuntimeInfoScreen(
                    onBack = { sampleBackStack.back() },
                )
            }
            entry<PermissionsRouter> {
                PermissionsScreen(
                    onBack = { sampleBackStack.back() },
                )
            }
            entry<PlatformShareDemoRouter> {
                PlatformShareDemoScreen(
                    onBackRequest = { sampleBackStack.back() },
                )
            }
            entry<VideoPlayerDemoRouter> {
                VideoPlayerDemoScreen(
                    onBack = { sampleBackStack.back() },
                )
            }
            entry<MediaLibraryDemoRouter> {
                MediaLibraryDemoScreen(
                    onBack = { sampleBackStack.back() },
                )
            }
            entry<FilePickerDemoRouter> {
                FilePickerDemoScreen(
                    onBack = { sampleBackStack.back() },
                )
            }
            entry<MediaDirectoryDemoRouter> {
                MediaDirectoryDemoScreen(
                    onBack = { sampleBackStack.back() },
                )
            }
            entry<SampleExamplePlaceholderARouter> {
                SamplePlaceholderScreen(
                    title = "占位示例 A",
                    onBack = { sampleBackStack.back() },
                )
            }
        },
    )
}
