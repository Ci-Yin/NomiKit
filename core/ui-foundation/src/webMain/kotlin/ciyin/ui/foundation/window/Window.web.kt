package ciyin.ui.foundation.window

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

/** Web 使用项目通用对话框承载窗口内容。 */
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

/** Web 使用项目通用对话框承载模态窗口内容。 */
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
