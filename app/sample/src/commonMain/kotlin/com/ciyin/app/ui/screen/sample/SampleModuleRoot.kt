package com.ciyin.app.ui.screen.sample

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.ciyin.app.ui.screen.aichat.AiChatScreen
import com.ciyin.app.ui.screen.aiimage.AiImageDemoScreen

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
            entry<SampleExamplePlaceholderARouter> {
                SamplePlaceholderScreen(
                    title = "占位示例 A",
                    onBack = { sampleBackStack.back() },
                )
            }
        },
    )
}
