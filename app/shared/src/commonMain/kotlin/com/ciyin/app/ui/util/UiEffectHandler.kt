package com.ciyin.app.ui.util

import org.jetbrains.compose.resources.StringResource

/**
 * UI 副作用处理器接口。
 *
 * 用于在非 UI 组合环境（如 ViewModel 或普通类）中处理 UI 相关的副作用，
 * 例如显示对话框、Toast 提示等。
 */
interface UiEffectHandler {


//    /**
//     * 显示通用对话框。
//     *
//     * @param title 对话框标题
//     * @param message 对话框内容消息
//     * @param cancelText 取消按钮文案，默认为 "取消"
//     * @param confirmText 确认按钮文案，默认为 "确认"
//     * @param dismissOnClickOutside 点击对话框外部是否消失，默认为 true
//     * @param dismissOnBackPress 点击返回键是否消失，默认为 true
//     * @param dismissOnCancelPress 点击取消按钮是否消失，默认为 true
//     * @param dismissOnConfirmPress 点击确认按钮是否消失，默认为 true
//     * @param onDismiss 对话框消失时的回调
//     * @param onCancel 点击取消按钮时的回调，可以调用 onDismiss 来关闭对话框，一般用于嵌套对话框
//     * @param onConfirm 点击确认按钮时的回调，可以调用 onDismiss 来关闭对话框，一般用于嵌套对话框
//     */
//    fun dialog(
//        title: String,
//        message: String,
//        cancelText: String? = "取消",
//        confirmText: String? = "确认",
//        dismissOnClickOutside: Boolean = true,
//        dismissOnBackPress: Boolean = true,
//        dismissOnCancelPress: Boolean = true,
//        dismissOnConfirmPress: Boolean = true,
//        onDismiss: () -> Unit = {},
//        onCancel: (onDismiss: () -> Unit) -> Unit = {},
//        onConfirm: (onDismiss: () -> Unit) -> Unit = {},
//    ) = Dialoger.show(
//        DialogState.Simple(
//            visible = true,
//            title = title,
//            message = message,
//            cancelText = cancelText,
//            confirmText = confirmText,
//            dismissOnClickOutside = dismissOnClickOutside,
//            dismissOnBackPress = dismissOnBackPress,
//            dismissOnCancelPress = dismissOnCancelPress,
//            dismissOnConfirmPress = dismissOnConfirmPress,
//            onDismiss = onDismiss,
//            onCancel = onCancel,
//            onConfirm = onConfirm
//        )
//    )
//
//    /**
//     * 显示登录对话框。
//     */
//    suspend fun dialogLogin(
//        onDismiss: () -> Unit = {},
//        onCancel: (onDismiss: () -> Unit) -> Unit = {},
//        onConfirm: (onDismiss: () -> Unit) -> Unit = {},
//    ) = Dialoger.show(
//        DialogState.Login(
//            visible = true,
//            title = "登录提示",
//            message = "登录后可享受更个性化的应用管理体验～",
//            cancelText = getString(Res.string.dialog_cancel),
//            confirmText = getString(Res.string.dialog_go_to),
//            dismissOnClickOutside = true,
//            dismissOnBackPress = true,
//            dismissOnCancelPress = true,
//            dismissOnConfirmPress = true,
//            onDismiss = onDismiss,
//            onCancel = onCancel,
//            onConfirm = onConfirm
//        )
//    )

    /**
     * 显示 Toast 提示信息。
     *
     * @param text 提示文本内容
     */
    fun toast(text: String) {
//        Toaster.show(text)
    }

    /**
     * 显示格式化的 Toast 提示信息。
     *
     * @param resource 字符串资源 ID
     * @param args 格式化参数
     */
    suspend fun toast(resource: StringResource, vararg args: Any) {
//        Toaster.show(getString(resource).format(args))
    }

//    /**
//     * 显示 Loading Toast。
//     *
//     * @param text 提示文案，默认为 "加载中..."
//     * @param config Toast 显示配置
//     */
//    fun toastLoading(
//        text: String = "加载中...",
//        config: ToastConfig = ToastConfig.Loading.copy(layout = ToastLayout.Vertical)
//    ) {
//        Toaster.loading(text, config = config)
//    }
//
//    /**
//     * 显示 Loading Toast（资源文件）。
//     *
//     * @param resource 提示文案资源 ID
//     * @param config Toast 显示配置
//     */
//    suspend fun toastLoading(
//        resource: StringResource,
//        config: ToastConfig = ToastConfig.Loading.copy(layout = ToastLayout.Vertical)
//    ) {
//        Toaster.loading(getString(resource), config = config)
//    }
//
//    /**
//     * 显示成功 Toast。
//     *
//     * @param text 提示文案
//     * @param config Toast 显示配置
//     */
//    fun toastSuccess(text: String, config: ToastConfig = ToastConfig.Default) {
//        Toaster.succeed(text, config = config)
//    }
//
//    /**
//     * 显示成功 Toast（资源文件）。
//     *
//     * @param resource 提示文案资源 ID
//     * @param config Toast 显示配置
//     */
//    suspend fun toastSuccess(resource: StringResource, config: ToastConfig = ToastConfig.Default) {
//        Toaster.succeed(getString(resource), config = config)
//    }
//
//    /**
//     * 显示错误/失败 Toast。
//     *
//     * @param text 提示文案
//     * @param config Toast 显示配置
//     */
//    fun toastError(text: String, config: ToastConfig = ToastConfig.Default) {
//        Toaster.failed(text, config = config)
//    }
//
//    /**
//     * 显示错误/失败 Toast（资源文件）。
//     *
//     * @param resource 提示文案资源 ID
//     * @param config Toast 显示配置
//     */
//    suspend fun toastError(resource: StringResource, config: ToastConfig = ToastConfig.Default) {
//        Toaster.failed(getString(resource), config = config)
//    }
//
//    /**
//     * 显示警告 Toast。
//     *
//     * @param text 提示文案
//     * @param config Toast 显示配置
//     */
//    fun toastWarning(text: String, config: ToastConfig = ToastConfig.Default) {
//        Toaster.warning(text, config = config)
//    }
//
//    /**
//     * 显示警告 Toast（资源文件）。
//     *
//     * @param resource 提示文案资源 ID
//     * @param config Toast 显示配置
//     */
//    suspend fun toastWarning(resource: StringResource, config: ToastConfig = ToastConfig.Default) {
//        Toaster.warning(getString(resource), config = config)
//    }

}
