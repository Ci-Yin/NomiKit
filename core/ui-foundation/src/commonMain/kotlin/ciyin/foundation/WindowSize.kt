package ciyin.foundation

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

enum class WindowSize {
    Compat,
    Medium,
    Expanded
}

@Composable
fun currentWindowWidth(): WindowSize {
    val size = currentWindowDpSize()
    return when (size.width) {
        in 0.dp..600.dp -> WindowSize.Compat
        in 600.dp..840.dp -> WindowSize.Medium
        else -> WindowSize.Expanded
    }
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