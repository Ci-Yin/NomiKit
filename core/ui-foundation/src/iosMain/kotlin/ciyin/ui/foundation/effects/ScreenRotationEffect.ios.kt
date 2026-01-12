package ciyin.ui.foundation.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientationDidChangeNotification
import platform.UIKit.UIScreen

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun ScreenRotationEffect(onChange: (isLandscape: Boolean) -> Unit) {
    val currentOnChange = rememberUpdatedState(onChange)

    fun currentIsLandscape(): Boolean =
        UIScreen.mainScreen.bounds.useContents { size.width > size.height }

    var isLandscape by remember { mutableStateOf(currentIsLandscape()) }

    LaunchedEffect(isLandscape) {
        currentOnChange.value(isLandscape)
    }

    DisposableEffect(Unit) {
        val center = NSNotificationCenter.defaultCenter

        // 开始生成设备方向变更通知
        UIDevice.currentDevice.beginGeneratingDeviceOrientationNotifications()

        // 监听设备方向变更
        val observer = center.addObserverForName(
            name = UIDeviceOrientationDidChangeNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue,
            usingBlock = { _: NSNotification? ->
                val now = currentIsLandscape()
                if (now != isLandscape) {
                    isLandscape = now
                }
            }
        )

        onDispose {
            // 清理观察者与通知
            center.removeObserver(observer)
            UIDevice.currentDevice.endGeneratingDeviceOrientationNotifications()
        }
    }
}