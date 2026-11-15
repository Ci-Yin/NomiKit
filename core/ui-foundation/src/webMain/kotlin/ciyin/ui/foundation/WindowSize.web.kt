package ciyin.ui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpSize

/**
 * 读取当前 Web 页面视口尺寸。
 *
 * 尺寸来自 [LocalWindowInfo] 的容器尺寸：Compose Web 监听浏览器窗口的 resize 事件并更新该状态，
 * 浏览器窗口缩放会触发读取方重组。
 *
 * @return 当前视口尺寸，单位 dp。
 */
@Composable
actual fun currentWindowDpSize(): DpSize {
    val containerSize = LocalWindowInfo.current.containerSize
    return with(LocalDensity.current) {
        DpSize(containerSize.width.toDp(), containerSize.height.toDp())
    }
}
