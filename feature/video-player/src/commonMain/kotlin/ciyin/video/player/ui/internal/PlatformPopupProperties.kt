package ciyin.video.player.ui.internal

import androidx.compose.ui.window.PopupProperties

/** 创建适配当前平台的弹窗属性。 */
@Suppress("FunctionName")
internal fun PlatformPopupProperties(
    focusable: Boolean = false,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    usePlatformDefaultWidth: Boolean = false,
    excludeFromSystemGesture: Boolean = true,
    clippingEnabled: Boolean = true,
    usePlatformInsets: Boolean = true,
): PopupProperties = platformPopupProperties(
    focusable = focusable,
    dismissOnBackPress = dismissOnBackPress,
    dismissOnClickOutside = dismissOnClickOutside,
    usePlatformDefaultWidth = usePlatformDefaultWidth,
    excludeFromSystemGesture = excludeFromSystemGesture,
    clippingEnabled = clippingEnabled,
    usePlatformInsets = usePlatformInsets,
)

/** 创建平台原生弹窗属性。 */
internal expect fun platformPopupProperties(
    focusable: Boolean,
    dismissOnBackPress: Boolean,
    dismissOnClickOutside: Boolean,
    usePlatformDefaultWidth: Boolean,
    excludeFromSystemGesture: Boolean,
    clippingEnabled: Boolean,
    usePlatformInsets: Boolean,
): PopupProperties
