package com.ciyin.app.ui.component

import androidx.compose.runtime.Composable
import ciyin.foundation.WindowSize
import ciyin.foundation.currentWindowWidth


val windowAdaptive: WindowType
    @Composable
    get() {

        val windowSize = currentWindowWidth()
        val windowType = when (windowSize) {
            WindowSize.Compat -> WindowType.PHONE
            WindowSize.Expanded -> WindowType.DESKTOP
            else -> WindowType.TABLET
        }

        /*if (windowType == WindowType.PHONE && windowSize.width.value / windowSize.height.value >= 1.5f) {
            windowType = WindowType.PHONE_HORIZONTAL
        }*/

        return windowType
    }
val isPhoneWindow: Boolean
    @Composable
    get() = windowAdaptive == WindowType.PHONE

val isPhoneHorizontalWindow: Boolean
    @Composable
    get() = windowAdaptive == WindowType.PHONE_HORIZONTAL

val isTabletopWindow: Boolean
    @Composable
    get() = windowAdaptive == WindowType.TABLET

val isDesktopWindow: Boolean
    @Composable
    get() = windowAdaptive == WindowType.DESKTOP

enum class WindowType {
    PHONE,
    PHONE_HORIZONTAL,
    TABLET,
    DESKTOP,
    TV,
    CAR,
    WATCH,
    GAME_CONSOLE,
    HEADPHONES,
    HEARING_AID,
    BROWSER,
    OTHER;
}