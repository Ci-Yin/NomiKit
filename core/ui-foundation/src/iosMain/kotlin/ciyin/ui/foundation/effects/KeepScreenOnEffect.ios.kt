package ciyin.ui.foundation.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIApplication

@Composable
actual fun KeepScreenOnEffect() {
    DisposableEffect(Unit) {
        val app = UIApplication.sharedApplication()
        val previous = app.isIdleTimerDisabled()
        app.idleTimerDisabled = true

        onDispose {
            app.idleTimerDisabled = previous
        }
    }
}