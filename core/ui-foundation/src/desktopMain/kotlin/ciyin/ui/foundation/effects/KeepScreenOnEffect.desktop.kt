package ciyin.ui.foundation.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect

@Composable
actual fun KeepScreenOnEffect() {
    DisposableEffect(true) {
//        WindowUtils.setPreventScreenSaver(true)
        onDispose {
//            WindowUtils.setPreventScreenSaver(false)
        }
    }
}