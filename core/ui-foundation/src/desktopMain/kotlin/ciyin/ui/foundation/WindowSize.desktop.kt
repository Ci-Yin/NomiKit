package ciyin.ui.foundation

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp


var currentWindowDpSize = DpSize(0.dp, 0.dp)

@Composable
actual fun currentWindowDpSize(): DpSize {
    return currentWindowDpSize
}
