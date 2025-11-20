package ciyin.foundation

import androidx.compose.runtime.Composable


/**
 *
 * kotlin类作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2025/10/28 16:51
 * @version: 1.0
 */

/**
 * 窗口
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
    content: @Composable (() -> Unit)
)

@Composable
internal fun CommonWindow(
    visible: Boolean,
    title: String,
    onCloseRequest: () -> Unit,
    content: @Composable (() -> Unit)
) {
    if (visible) {
        AlertDialog2(
            onDismissRequest = onCloseRequest,
            title = title,
            content = content
        )
    }
}