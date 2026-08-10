package ciyin.ui.foundation.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.window.DialogModalityType
import androidx.compose.ui.window.DialogWindow as ComposeDialogWindow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowDecoration
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import androidx.compose.ui.window.rememberWindowState

/** Desktop 使用原生系统窗口承载普通窗口内容。 */
@Composable
actual fun Window(
    visible: Boolean,
    title: String,
    onCloseRequest: () -> Unit,
    content: @Composable (() -> Unit),
) {
    Window(
        title = title,
        visible = visible,
        state = rememberWindowState(
            position = WindowPosition.Aligned(Alignment.Center),
            size = DpSize(900.dp, 720.dp),
        ),
        onCloseRequest = onCloseRequest,
        content = { content() },
    )
}

/** Desktop 使用系统装饰的 document-modal 原生对话框。 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun DialogWindow(
    visible: Boolean,
    title: String,
    icon: Painter?,
    config: DialogWindowConfig,
    onCloseRequest: () -> Unit,
    content: @Composable (() -> Unit),
) {
    ComposeDialogWindow(
        visible = visible,
        title = title,
        icon = icon,
        state = rememberDialogState(
            position = WindowPosition.Aligned(Alignment.Center),
            size = config.size,
        ),
        decoration = WindowDecoration.SystemDefault,
        resizable = config.resizable,
        modalityType = DialogModalityType.DocumentModal,
        onCloseRequest = onCloseRequest,
        onPreviewKeyEvent = { event ->
            if (
                config.dismissOnEscape &&
                event.key == Key.Escape &&
                event.type == KeyEventType.KeyUp
            ) {
                onCloseRequest()
                true
            } else {
                false
            }
        },
    ) {
        DisposableEffect(window, config.darkTitleBar) {
            applyWindowsDarkTitleBar(
                window = window,
                dark = config.darkTitleBar,
            )
            onDispose { }
        }
        content()
    }
}
