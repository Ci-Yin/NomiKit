package ciyin.ui.foundation.systemuicontroller

import androidx.compose.runtime.Composable

@Composable
actual fun rememberSystemUiController(): SystemUiController {
    return CommonSystemUiController()
}