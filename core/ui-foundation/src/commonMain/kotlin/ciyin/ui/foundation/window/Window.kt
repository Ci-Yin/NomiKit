package ciyin.ui.foundation.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.DpSize
import ciyin.ui.foundation.dialog.AlertDialog2

/**
 * 原生模态窗口配置。
 *
 * @property size 调用方指定的窗口规格尺寸，Desktop 包含系统装饰区域
 * @property resizable 是否允许用户缩放窗口
 * @property dismissOnEscape 是否在释放 Esc 时请求关闭窗口
 * @property darkTitleBar 是否使用深色系统标题栏
 */
@Immutable
data class DialogWindowConfig(
    val size: DpSize,
    val resizable: Boolean = false,
    val dismissOnEscape: Boolean = true,
    val darkTitleBar: Boolean = false,
)

/**
 * 跨平台窗口。
 *
 * @param visible 窗口是否可见
 * @param title 窗口标题
 * @param onCloseRequest 窗口关闭请求
 * @param content 窗口内容
 */
@Composable
expect fun Window(
    visible: Boolean = true,
    title: String = "Untitled",
    onCloseRequest: () -> Unit,
    content: @Composable (() -> Unit),
)

/**
 * 跨平台原生模态窗口。
 *
 * Desktop 使用系统装饰的 document-modal 对话框；其它平台回退到 [CommonWindow]。
 *
 * @param visible 窗口是否可见
 * @param title 窗口标题
 * @param icon 系统标题栏图标
 * @param config 窗口尺寸与系统标题栏配置
 * @param onCloseRequest 窗口关闭请求
 * @param content 窗口内容
 */
@Composable
expect fun DialogWindow(
    visible: Boolean = true,
    title: String = "Untitled",
    icon: Painter? = null,
    config: DialogWindowConfig,
    onCloseRequest: () -> Unit,
    content: @Composable (() -> Unit),
)

/**
 * 使用项目通用对话框承载不支持原生窗口的平台内容。
 *
 * @param visible 窗口是否可见
 * @param title 窗口标题
 * @param onCloseRequest 窗口关闭请求
 * @param content 窗口内容
 */
@Composable
internal fun CommonWindow(
    visible: Boolean,
    title: String,
    onCloseRequest: () -> Unit,
    content: @Composable (() -> Unit),
) {
    if (visible) {
        AlertDialog2(
            onDismissRequest = onCloseRequest,
            title = title,
            content = content,
        )
    }
}
