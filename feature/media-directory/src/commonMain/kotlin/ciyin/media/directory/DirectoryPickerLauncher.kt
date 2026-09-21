package ciyin.media.directory

/**
 * 平台目录选择器抽象。
 *
 * - Android：宿主（通常是 Activity）注入 `ActivityResultLauncher<Uri?>`，
 *   内部启动 `ACTION_OPEN_DOCUMENT_TREE`。
 * - Desktop：宿主可不提供，actual 内部用 `JFileChooser` 启动模态选择；
 *   提供时优先使用宿主注入。
 * - iOS：宿主注入 `UIDocumentPickerViewController` 工厂。
 */
fun interface DirectoryPickerLauncher {
    /**
     * 启动目录选择。
     *
     * @return 用户取消时返回 `null`；授权成功时返回平台 URI / 路径 / bookmark 的字符串表示。
     */
    suspend fun pick(): String?
}
