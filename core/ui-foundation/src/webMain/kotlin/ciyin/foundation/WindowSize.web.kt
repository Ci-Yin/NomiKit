package ciyin.foundation

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Composable
actual fun currentWindowDpSize(): DpSize {
    return DpSize(300.dp, 500.dp)
}