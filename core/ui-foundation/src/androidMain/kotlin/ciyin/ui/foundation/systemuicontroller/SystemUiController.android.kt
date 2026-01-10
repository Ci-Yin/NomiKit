package ciyin.ui.foundation.systemuicontroller

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.View
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/**
 * 为给定的 `window` 记住一个 [SystemUiController]。
 *
 * 如果没有提供 `window`，则尝试找到正确的 [Window]。
 *
 * 首先，如果 [LocalView] 的父级是 [DialogWindowProvider]，则将使用该对话框的 [Window]。
 *
 * 其次，我们要尝试查找包含 [LocalView] 的 [Activity] 的 [Window]。
 *
 * 如果这些都找不到（例如在预览中可能发生的情况），则返回的 [SystemUiController] 的功能将降级，但不会抛出异常。
 */
@Composable
actual fun rememberSystemUiController(): SystemUiController {
    val window = findWindow()
    val view = LocalView.current
    return remember(view, window) { AndroidSystemUiController(view, window) }
}

@Composable
private fun findWindow(): Window? =
    (LocalView.current.parent as? DialogWindowProvider)?.window
        ?: LocalView.current.context.findWindow()

private tailrec fun Context.findWindow(): Window? =
    when (this) {
        is Activity -> window
        is ContextWrapper -> baseContext.findWindow()
        else -> null
    }

/**
 * A helper class for setting the navigation and status bar colors for a [View], gracefully
 * degrading behavior based upon API level.
 *
 * Typically you would use [rememberSystemUiController] to remember an instance of this.
 */
internal class AndroidSystemUiController(
    private val view: View,
    private val window: Window?,
) : SystemUiController {
    private val windowInsetsController = window?.let {
        WindowCompat.getInsetsController(it, view)
    }

    override fun setStatusBarColor(
        color: Color,
        darkIcons: Boolean,
        transformColorForLightContent: (Color) -> Color,
    ) {
        statusBarDarkContentEnabled = darkIcons

        window?.statusBarColor = when {
            darkIcons && windowInsetsController?.isAppearanceLightStatusBars != true -> {
                // If we're set to use dark icons, but our windowInsetsController call didn't
                // succeed (usually due to API level), we instead transform the color to maintain
                // contrast
                transformColorForLightContent(color)
            }

            else -> color
        }.toArgb()
    }

    override fun setNavigationBarColor(
        color: Color,
        darkIcons: Boolean,
        navigationBarContrastEnforced: Boolean,
        transformColorForLightContent: (Color) -> Color,
    ) {
        navigationBarDarkContentEnabled = darkIcons
        isNavigationBarContrastEnforced = navigationBarContrastEnforced
        window?.navigationBarColor = when {
            darkIcons && windowInsetsController?.isAppearanceLightNavigationBars != true -> {
                // If we're set to use dark icons, but our windowInsetsController call didn't
                // succeed (usually due to API level), we instead transform the color to maintain
                // contrast
                transformColorForLightContent(color)
            }
            else -> color
        }.toArgb()
    }

    override var isStatusBarVisible: Boolean
        get() {
            return ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.statusBars()) == true
        }
        set(value) {
            if (value) {
                windowInsetsController?.show(WindowInsetsCompat.Type.statusBars())
            } else {
                windowInsetsController?.hide(WindowInsetsCompat.Type.statusBars())
            }
        }

    override var isNavigationBarVisible: Boolean
        get() {
            return ViewCompat.getRootWindowInsets(view)
                ?.isVisible(WindowInsetsCompat.Type.navigationBars()) == true
        }
        set(value) {
            if (value) {
                windowInsetsController?.show(WindowInsetsCompat.Type.navigationBars())
            } else {
                windowInsetsController?.hide(WindowInsetsCompat.Type.navigationBars())
            }
        }

    override var statusBarDarkContentEnabled: Boolean
        get() = windowInsetsController?.isAppearanceLightStatusBars == true
        set(value) {
            windowInsetsController?.isAppearanceLightStatusBars = value
        }

    override var navigationBarDarkContentEnabled: Boolean
        get() = windowInsetsController?.isAppearanceLightNavigationBars == true
        set(value) {
            windowInsetsController?.isAppearanceLightNavigationBars = value
        }

    override var isNavigationBarContrastEnforced: Boolean
        get() = Build.VERSION.SDK_INT >= 29 && window?.isNavigationBarContrastEnforced == true
        set(value) {
            if (Build.VERSION.SDK_INT >= 29) {
                window?.isNavigationBarContrastEnforced = value
            }
        }
}