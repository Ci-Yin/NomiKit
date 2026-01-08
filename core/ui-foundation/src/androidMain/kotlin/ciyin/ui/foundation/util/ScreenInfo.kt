package ciyin.ui.foundation.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private fun getStatusBarHeight(context: Context): Int {
    var result = 0
    val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = context.resources.getDimensionPixelSize(resourceId)
    }
    return result
}

private fun getNavigationBarHeight(context: Context): Int {
    var result = 0
    val resourceId = context.resources.getIdentifier("navigation_bar_height", "dimen", "android")
    if (resourceId > 0) {
        result = context.resources.getDimensionPixelSize(resourceId)
    }
    return result
}

/**
 * 获取当前设备的屏幕信息。
 *
 * @return ScreenInfo对象，包含屏幕的宽度、高度、状态栏高度和导航栏高度等信息。
 */
fun Context.getScreenInfo(): ScreenInfo {

    val density = resources.displayMetrics.density
    var width = 0
    var height = 0
    var windowWidth = 0
    var windowHeight = 0
    var statusBarHeight = 0
    var navigationBarHeight = 0

    // 尝试从当前上下文中获取Window对象
    val window = (this as? Activity)?.window

    // 对于Android 11及以上版本，使用新的WindowMetrics接口获取屏幕信息
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // 如果Window对象不为空，则获取当前窗口的尺寸和 insets
        if (window != null) {
            val windowMetrics = window.windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars()
            )
            // 设置屏幕的宽度和高度，以及状态栏和导航栏的高度
            width = bounds.width()
            height = bounds.height()
            windowWidth = bounds.width() - insets.left - insets.right
            windowHeight = bounds.height() - insets.top - insets.bottom
            statusBarHeight = insets.top
            navigationBarHeight = insets.bottom
        }
    } else {
        // 对于Android R以下版本，使用旧的方法获取屏幕信息
        if (window != null) {
            val insets = window.decorView.rootWindowInsets
            val metrics = resources.displayMetrics
            // 设置状态栏和导航栏的高度，以及屏幕的宽度和高度
            statusBarHeight = insets.systemWindowInsetTop
            navigationBarHeight = insets.systemWindowInsetBottom
            width = metrics.widthPixels
            height = metrics.heightPixels + navigationBarHeight
            windowWidth = metrics.widthPixels
            windowHeight = metrics.heightPixels
        }
    }

    // 如果通过上述方法未能获取到状态栏或导航栏的高度，则尝试使用备用方法获取
    if (statusBarHeight == 0) {
        statusBarHeight = getStatusBarHeight(this)
    }

    if (navigationBarHeight == 0) {
        navigationBarHeight = getNavigationBarHeight(this)
    }

    // 如果通过上述方法未能获取到屏幕的宽度或高度，则使用备用方法获取
    if (width == 0 || height == 0) {
        val metrics = resources.displayMetrics
        if (width == 0) {
            width = metrics.widthPixels
            windowWidth = metrics.widthPixels
        }
        if (height == 0) {
            height = metrics.heightPixels + navigationBarHeight
            windowHeight = metrics.heightPixels
            // 对于Android 11及以上版本，屏幕高度需要再加上状态栏的高度
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                height += statusBarHeight
            }
        }
    }

    // 返回填充好的ScreenInfo对象
    return ScreenInfo(
        width = width,
        height = height,
        windowWidth = windowWidth,
        windowHeight = windowHeight,
        statusBarHeight = statusBarHeight,
        navigationBarHeight = navigationBarHeight,
        density = density
    )
}

/**
 * 屏幕信息数据类，用于存储设备屏幕的相关参数。
 *
 * @property width 屏幕宽度（像素），默认为0。
 * @property height 屏幕高度（像素），默认为0。
 * @property windowWidth 窗口宽度（像素），默认为0。在有虚拟按键的设备上，该值可能与屏幕宽度不同。
 * @property windowHeight 窗口高度（像素），默认为0。在有虚拟按键的设备上，该值可能与屏幕高度不同。
 * @property statusBarHeight 状态栏高度（像素），默认为0。
 * @property navigationBarHeight 导航栏高度（像素），默认为0。
 * @property density 屏幕密度（dpi），默认为0f。
 */
data class ScreenInfo(
    /**
     * 屏幕宽度（像素）。
     */
    val width: Int = 0,

    /**
     * 屏幕高度（像素）。
     */
    val height: Int = 0,

    /**
     * 窗口宽度（像素）。在有虚拟按键的设备上，该值可能与屏幕宽度不同。
     */
    val windowWidth: Int = 0,

    /**
     * 窗口高度（像素）。在有虚拟按键的设备上，该值可能与屏幕高度不同。
     */
    val windowHeight: Int = 0,

    /**
     * 状态栏高度（像素）。
     */
    val statusBarHeight: Int = 0,

    /**
     * 导航栏高度（像素）。
     */
    val navigationBarHeight: Int = 0,

    /**
     * 屏幕密度（dpi）。
     */
    val density: Float = 0f,
)


/**
 * 获取当前设备的屏幕信息。
 *
 * @return ScreenInfo对象，包含屏幕的宽度、高度、状态栏高度和导航栏高度等信息。
 */
val ScreenProperties: ScreenInfo
    @Composable
    get() {
        return LocalContext.current.getScreenInfo()
    }