package ciyin.ui.foundation.window

import androidx.compose.runtime.Composable

@Composable
actual fun Window(
    visible: Boolean,
    title: String,
    onCloseRequest: () -> Unit,
    content: @Composable (() -> Unit)
) = CommonWindow(
    visible = visible,
    title = title,
    onCloseRequest = onCloseRequest,
    content = content
)