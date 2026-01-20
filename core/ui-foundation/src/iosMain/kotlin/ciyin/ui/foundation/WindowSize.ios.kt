package ciyin.ui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize

@Composable
actual fun currentWindowDpSize(): DpSize {
    // 获取当前窗口的容器大小信息
    val containerSize = LocalWindowInfo.current.containerSize

    // 使用 LocalDensity 将像素转换为 Dp
    return with(LocalDensity.current) {
        DpSize(containerSize.width.toDp(), containerSize.height.toDp())
    }
}