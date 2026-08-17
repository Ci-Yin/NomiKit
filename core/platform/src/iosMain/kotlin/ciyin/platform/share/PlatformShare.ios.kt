@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package ciyin.platform.share

import ciyin.platform.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UINavigationController
import platform.UIKit.UISceneActivationStateForegroundActive
import platform.UIKit.UITabBarController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController

/**
 * iOS 系统分享实现。
 *
 * @param context iOS 上下文
 * @param payload 系统分享内容
 */
@Suppress("UNUSED_PARAMETER")
actual suspend fun sharePlatformContent(
    context: Context,
    payload: PlatformSharePayload,
): PlatformShareResult = withContext(Dispatchers.Main) {
    payload.validatedPlatformShareTitleOrNull()
    val activityItems = payload.toActivityItems()
    val presenter = findActivePresenter()
        ?: throw PlatformShareException(
            reason = PlatformShareFailureReason.PresenterUnavailable,
            message = "没有找到可展示 iOS 分享面板的前台 scene、window 或 view controller",
        )
    val activityController = UIActivityViewController(
        activityItems = activityItems,
        applicationActivities = null,
    )
    activityController.popoverPresentationController?.apply {
        sourceView = presenter.view
        sourceRect = presenter.view.bounds
    }

    try {
        presenter.presentViewController(
            viewControllerToPresent = activityController,
            animated = true,
            completion = null,
        )
        PlatformShareResult.Opened
    } catch (exception: Exception) {
        throw PlatformShareException(
            reason = PlatformShareFailureReason.LaunchFailed,
            message = "iOS 系统分享面板展示失败",
            cause = exception,
        )
    }
}

/** 将跨平台分享载荷转换为 UIActivityViewController items。 */
private fun PlatformSharePayload.toActivityItems(): List<Any> = when (this) {
    is PlatformSharePayload.Text -> listOf(value.validatedPlatformShareText())
    is PlatformSharePayload.File -> listOf(value.toActivityItem())
    is PlatformSharePayload.Files -> {
        if (values.isEmpty()) {
            throw PlatformShareException(
                reason = PlatformShareFailureReason.InvalidPayload,
                message = "多文件分享列表不能为空",
            )
        }
        values.map(PlatformShareFile::toActivityItem)
    }
}

/** 将单个跨平台文件转换为 iOS 活动项。 */
private fun PlatformShareFile.toActivityItem(): NSURL {
    mimeType.validatedPlatformShareMimeType()

    return when (val currentSource = source) {
        is PlatformShareFileSource.Uri -> currentSource.value.toShareUrl()
        is PlatformShareFileSource.LocalFile -> {
            val file = currentSource.value
            if (!file.exists() || !file.isFile) {
                throw PlatformShareException(
                    reason = PlatformShareFailureReason.FileUnavailable,
                    message = "分享文件不存在或不是普通文件: ${file.path}",
                )
            }
            NSURL.fileURLWithPath(file.absolutePath)
        }
    }
}

/** 校验并创建 iOS 可分享 URL。 */
private fun String.toShareUrl(): NSURL {
    val uriValue = trim()
    val url = uriValue.takeIf(String::isNotEmpty)?.let(NSURL::URLWithString)
        ?: throw PlatformShareException(
            reason = PlatformShareFailureReason.InvalidUri,
            message = "分享 URI 为空或格式无效: $this",
        )
    if (url.fileURL) {
        val path = url.path
            ?: throw PlatformShareException(
                reason = PlatformShareFailureReason.InvalidUri,
                message = "file URI 不包含本地路径: $this",
            )
        val file = ciyin.io.File(path)
        if (!file.exists() || !file.isFile) {
            throw PlatformShareException(
                reason = PlatformShareFailureReason.FileUnavailable,
                message = "分享 URI 指向的文件不存在或不是普通文件: $this",
            )
        }
    }
    return url
}

/** 查找前台活动 scene 中最适合展示模态界面的控制器。 */
private fun findActivePresenter(): UIViewController? {
    val application = UIApplication.sharedApplication
    val activeScene = application.connectedScenes
        .filterIsInstance<UIWindowScene>()
        .firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
        ?: return null
    val activeWindow = activeScene.windows
        .filterIsInstance<UIWindow>()
        .firstOrNull(UIWindow::isKeyWindow)
        ?: activeScene.windows.filterIsInstance<UIWindow>().firstOrNull { !it.hidden }
        ?: return null
    return activeWindow.rootViewController?.topPresentedViewController()
}

/** 沿容器和模态展示链查找最上层控制器。 */
private fun UIViewController.topPresentedViewController(): UIViewController {
    presentedViewController?.let { return it.topPresentedViewController() }
    return when (this) {
        is UINavigationController -> visibleViewController?.topPresentedViewController() ?: this
        is UITabBarController -> selectedViewController?.topPresentedViewController() ?: this
        else -> this
    }
}
