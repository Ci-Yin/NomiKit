package com.ciyin.app.ui.app

import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.DialogProperties
import ciyin.foundation.AlertDialog2
import com.ciyin.app.ui.theme.iconpack.IconPack
import com.ciyin.app.ui.theme.iconpack.Null
import com.ciyin.app.util.value
import org.jetbrains.compose.resources.stringResource
import rpa.app.shared.generated.resources.Res
import rpa.app.shared.generated.resources.dialog_cancel
import rpa.app.shared.generated.resources.dialog_confirm
import rpa.app.shared.generated.resources.dialog_title_error
import rpa.app.shared.generated.resources.dialog_title_hint

/**
 *
 * kotlin文件作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2024/9/5 上午1:59
 * @version: 1.0
 */

private var appDialogUiState = AppDialogUiState()

@Composable
fun AppDialog(
    uiState: AppDialogUiState = appDialogUiState,
    modifier: Modifier = Modifier
) {
    if (!uiState.showDialog) return
    AlertDialog2(
        modifier = modifier,
        title = uiState.title,
        text = uiState.message,
        icon = if (uiState.icon == IconPack.Null) null else uiState.icon,
        confirm = uiState.confirm,
        dismiss = uiState.cancel,
        onConfirmClick = uiState.onConfirmClick,
        onDismissClick = uiState.onDismissClick,
        onDismissRequest = uiState.onDismissRequest
    )
}

@Composable
fun AppDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    confirm: String = stringResource(Res.string.dialog_confirm),
    dismiss: String = stringResource(Res.string.dialog_cancel),
    onConfirmClick: () -> Unit = {},
    onDismissClick: () -> Unit = {},
    icon: ImageVector? = null,
    title: String = "",
    text: String = "",
    shape: Shape = AlertDialogDefaults.shape,
    containerColor: Color = MaterialTheme.colorScheme.background,
    iconContentColor: Color = AlertDialogDefaults.iconContentColor,
    titleContentColor: Color = AlertDialogDefaults.titleContentColor,
    textContentColor: Color = AlertDialogDefaults.textContentColor,
    tonalElevation: Dp = AlertDialogDefaults.TonalElevation,
    properties: DialogProperties = DialogProperties(),
    content: @Composable (() -> Unit)? = null,
) = AlertDialog2(
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    confirm = confirm,
    dismiss = dismiss,
    onConfirmClick = onConfirmClick,
    onDismissClick = onDismissClick,
    icon = icon,
    title = title,
    text = text,
    shape = shape,
    containerColor = containerColor,
    iconContentColor = iconContentColor,
    titleContentColor = titleContentColor,
    textContentColor = textContentColor,
    tonalElevation = tonalElevation,
    properties = properties,
    content = content
)

class AppDialogUiState {
    var showDialog by mutableStateOf(false)
    var icon by mutableStateOf(IconPack.Null)
    var title by mutableStateOf("")
    var message by mutableStateOf("")
    var confirm by mutableStateOf("")
    var cancel by mutableStateOf("")
    var onConfirmClick: () -> Unit = {}
    var onDismissClick: () -> Unit = {}
    var onDismissRequest: () -> Unit = {}
}

fun dialog(
    message: String? = null,
    icon: ImageVector? = null,
    title: String? = Res.string.dialog_title_hint.value,
    confirm: String? = Res.string.dialog_confirm.value,
    dismiss: String? = Res.string.dialog_cancel.value,
    onDismissClick: () -> Boolean = { true },
    onDismissRequest: () -> Boolean = { true },
    onConfirmClick: () -> Boolean = { true },
) = appDialogUiState.let {
    it.showDialog = true
    it.icon = icon ?: it.icon
    it.title = title ?: it.title
    it.message = message ?: it.message
    it.confirm = confirm ?: it.confirm
    it.cancel = dismiss ?: it.cancel
    it.onConfirmClick = {
        if (onConfirmClick()) {
            it.showDialog = false
        }
    }
    it.onDismissClick = {
        if (onDismissClick()) {
            it.showDialog = false
        }
    }
    it.onDismissRequest = {
        if (onDismissRequest()) {
            it.showDialog = false
        }
    }
}

fun dialogError(
    message: String? = null,
    icon: ImageVector? = null,
    title: String? = Res.string.dialog_title_error.value,
    confirm: String? = Res.string.dialog_confirm.value,
    dismiss: String? = Res.string.dialog_cancel.value,
    onDismissClick: () -> Boolean = { true },
    onDismissRequest: () -> Boolean = { true },
    onConfirmClick: () -> Boolean = { true },
) = dialog(
    icon = icon,
    title = title,
    message = message,
    confirm = confirm,
    dismiss = dismiss,
    onDismissClick = onDismissClick,
    onDismissRequest = onDismissRequest,
    onConfirmClick = onConfirmClick
)