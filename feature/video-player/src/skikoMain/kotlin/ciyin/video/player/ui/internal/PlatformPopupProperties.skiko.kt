package ciyin.video.player.ui.internal

import androidx.compose.ui.window.PopupProperties

/** 创建 Skiko 平台弹窗属性。 */
internal actual fun platformPopupProperties(
    focusable: Boolean,
    dismissOnBackPress: Boolean,
    dismissOnClickOutside: Boolean,
    usePlatformDefaultWidth: Boolean,
    excludeFromSystemGesture: Boolean,
    clippingEnabled: Boolean,
    usePlatformInsets: Boolean,
): PopupProperties = PopupProperties(
    focusable = focusable,
    dismissOnBackPress = dismissOnBackPress,
    dismissOnClickOutside = dismissOnClickOutside,
    clippingEnabled = clippingEnabled,
    usePlatformDefaultWidth = usePlatformDefaultWidth,
    usePlatformInsets = usePlatformInsets,
)
