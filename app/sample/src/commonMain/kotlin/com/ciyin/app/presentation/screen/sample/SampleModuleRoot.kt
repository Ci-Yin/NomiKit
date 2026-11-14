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
import ciyin.designsystem.theme.NomiTheme
import com.ciyin.app.presentation.screen.designsystem.DesignSystemDemoContent
import com.ciyin.app.presentation.screen.designsystem.component.SamplePreviewTheme
import com.ciyin.app.presentation.screen.designsystem.DesignSystemDemoUiState
import com.ciyin.app.presentation.screen.designsystem.DesignSystemDemoScreen
import org.jetbrains.compose.ui.tooling.preview.AppPreview
import com.ciyin.app.presentation.screen.filedownloader.FileDownloaderDemoScreen
import com.ciyin.app.presentation.screen.filepicker.FilePickerDemoScreen
import com.ciyin.app.presentation.screen.mediadirectory.MediaDirectoryDemoScreen
import com.ciyin.app.presentation.screen.medialibrary.MediaLibraryDemoScreen
import com.ciyin.app.presentation.screen.permissions.PermissionsScreen
import com.ciyin.app.presentation.screen.platformshare.PlatformShareDemoScreen
import com.ciyin.app.presentation.screen.runtimeinfo.RuntimeInfoScreen
import com.ciyin.app.presentation.screen.videoplayer.VideoPlayerDemoScreen

/**
 * 样例模块根入口，创建以首页为起点、支持保存恢复的独立子导航栈。
 *
 * 页面事件和系统返回都交给样例栈处理；设计系统候选从外层 provider 读取。
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
            entry<DesignSystemDemoRouter> {
                DesignSystemDemoScreen(
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

/**
 * 导航宿主同文件预览纯展示内容，不创建真实 back stack 或状态机。
 */
@AppPreview
@Composable
private fun SampleDesignSystemEntryPreview() {
    SamplePreviewTheme {
        DesignSystemDemoContent(state = DesignSystemDemoUiState(), onAction = {})
    }
}
