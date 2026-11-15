package ciyin.ui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize

/**
 * 读取当前桌面窗口的客户区尺寸。
 *
 * 尺寸来自 [LocalWindowInfo] 的容器尺寸；窗口缩放时 Compose 会更新该状态并触发读取方重组，
 * 调用方可以据此实时切换窗口布局分级。
 *
 * @return 当前窗口客户区尺寸，单位 dp。
 */
@Composable
actual fun currentWindowDpSize(): DpSize {
    val containerSize = LocalWindowInfo.current.containerSize
    return with(LocalDensity.current) {
        DpSize(containerSize.width.toDp(), containerSize.height.toDp())
    }
}
