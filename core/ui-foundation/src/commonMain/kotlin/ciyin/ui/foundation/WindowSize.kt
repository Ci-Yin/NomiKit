package ciyin.ui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowSize {
    Compat,
    Medium,
    Expanded
}

/** 返回当前应用窗口的宽度尺寸级别。 */
@Composable
fun currentWindowWidth(): WindowSize {
    val size = currentWindowDpSize()
    return classifyWindowWidth(size.width)
}

/**
 * 按可用宽度划分窗口尺寸级别。
 *
 * @param width 当前布局可用宽度
 * @return 对应的窗口宽度级别
 */
fun classifyWindowWidth(width: Dp): WindowSize = when (width) {
        in 0.dp..600.dp -> WindowSize.Compat
        in 600.dp..840.dp -> WindowSize.Medium
        else -> WindowSize.Expanded
}

@Composable
fun currentWindowHeight(): WindowSize {
    val size = currentWindowDpSize()
    return when (size.height) {
        in 0.dp..600.dp -> WindowSize.Compat
        in 600.dp..840.dp -> WindowSize.Medium
        else -> WindowSize.Expanded
    }
}

@Composable
fun currentWindowSize(): WindowSize {
    val width = currentWindowWidth()
    val height = currentWindowHeight()
    return if (width == WindowSize.Medium && height == WindowSize.Medium) {
        WindowSize.Medium
    } else if (width == WindowSize.Expanded && height == WindowSize.Expanded) {
        WindowSize.Expanded
    } else {
        WindowSize.Compat
    }
}

@Composable
expect fun currentWindowDpSize(): DpSize
