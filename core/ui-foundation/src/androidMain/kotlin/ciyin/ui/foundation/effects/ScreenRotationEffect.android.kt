package ciyin.ui.foundation.effects

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalConfiguration

@Composable
actual fun ScreenRotationEffect(onChange: (isLandscape: Boolean) -> Unit) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    LaunchedEffect(isLandscape) {
        onChange(isLandscape)
    }
}