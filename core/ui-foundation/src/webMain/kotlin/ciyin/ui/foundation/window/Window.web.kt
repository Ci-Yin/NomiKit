package ciyin.ui.window.foundation

@androidx.compose.runtime.Composable
actual fun Window(
    visible: Boolean,
    title: String,
    onCloseRequest: () -> Unit,
    content: @androidx.compose.runtime.Composable (() -> Unit)
) = CommonWindow(
    visible = visible,
    title = title,
    onCloseRequest = onCloseRequest,
    content = content
)