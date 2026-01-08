package ciyin.ui.foundation.dialog

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/9/4 下午9:32
 */

@Composable
fun AlertDialog2(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    confirm: String = "",
    dismiss: String = "",
    onConfirmClick: () -> Unit = {},
    onDismissClick: () -> Unit = {},
    icon: ImageVector? = null,
    title: String = "",
    text: String = "",
    shape: Shape = RoundedCornerShape(15.dp),
    containerColor: Color = MaterialTheme.colorScheme.background,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties(),
    content: @Composable (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        confirmButton = {
            if (confirm.isEmpty()) {
                return@AlertDialog
            }
            TextButton(onClick = onConfirmClick) {
                Text(text = confirm)
            }
        },
        dismissButton = {
            if (dismiss.isEmpty()) {
                return@AlertDialog
            }
            TextButton(onClick = onDismissClick) {
                Text(text = dismiss)
            }
        },
        icon = {
            if (icon == null) {
                return@AlertDialog
            }
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = icon,
                contentDescription = null
            )
        },
        title = {
            if (title.isEmpty()) {
                return@AlertDialog
            }
            Text(text = title)
        },
        text = {
            if (content != null) {
                content()
                return@AlertDialog
            }
            if (text.isNotEmpty()) {
                Text(text = text)
            }
        },
        shape = shape,
        containerColor = containerColor,
        iconContentColor = iconContentColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        tonalElevation = tonalElevation,
        properties = properties
    )
}
