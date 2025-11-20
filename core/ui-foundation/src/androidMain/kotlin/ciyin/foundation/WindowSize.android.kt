package ciyin.foundation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.window.layout.WindowMetricsCalculator

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
        (windowBounds.width() / density.density).dp
    )
}