package ciyin.ui.foundation.window

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

/** Android 使用项目通用对话框承载普通窗口内容。 */
@Composable
actual fun Window(
    visible: Boolean,
    title: String,
    onCloseRequest: () -> Unit,
    content: @Composable (() -> Unit),
) = CommonWindow(
    visible = visible,
    title = title,
    onCloseRequest = onCloseRequest,
    content = content,
)

/** Android 使用项目通用对话框承载模态窗口内容。 */
@Composable
actual fun DialogWindow(
    visible: Boolean,
    title: String,
    icon: Painter?,
    config: DialogWindowConfig,
    onCloseRequest: () -> Unit,
    content: @Composable (() -> Unit),
) = CommonWindow(
    visible = visible,
    title = title,
    onCloseRequest = onCloseRequest,
    content = content,
)
