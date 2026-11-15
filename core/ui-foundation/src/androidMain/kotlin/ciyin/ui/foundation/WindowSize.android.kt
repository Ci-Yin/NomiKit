package ciyin.ui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.window.layout.WindowMetricsCalculator

/**
 * 读取当前 Android 窗口的客户区尺寸。
 *
 * 读取 [LocalConfiguration] 以便在旋转、分屏或折叠等配置变化时触发重组，再按窗口度量换算为 dp。
 *
 * @return 当前窗口客户区尺寸，单位 dp。
 */
@Composable
actual fun currentWindowDpSize(): DpSize {
    // 观察视图配置更改，并在每次更改时重新计算大小类。我们不能
    // 使用 Activity#onConfigurationChanged，因为有时无法在不同的
    // API 级别，因此需要@Composable此函数以便我们可以观察到
    // ComposeView 的配置更改。
    LocalConfiguration.current
    val windowBounds = WindowMetricsCalculator.getOrCreate()
        .computeCurrentWindowMetrics(LocalContext.current)
        .bounds
    val density = LocalDensity.current
    return DpSize(
        (windowBounds.width() / density.density).dp,
        (windowBounds.height() / density.density).dp
    )
}