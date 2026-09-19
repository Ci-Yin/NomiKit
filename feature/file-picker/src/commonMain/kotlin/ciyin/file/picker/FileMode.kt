package ciyin.file.picker

import ciyin.io.File

/**
 * 文件选择模式。
 *
 * 定义文件选择器的选择行为，包括单选和多选两种模式。
 *
 * @param Result 选择器返回的结果类型
 *   - [Single] 模式返回 `File?`（单个文件，可能为 null）
 *   - [Multiple] 模式返回 `List<File?>`（文件列表）
 */
sealed class FileMode<out Result> {
    /**
     * 单选模式，返回单个文件。
     *
     * 用户只能选择一个文件，结果类型为 `File?`。
     * 如果用户取消选择，返回 `null`。
     */
    data object Single : FileMode<File?>()

    /**
     * 多选模式，返回文件列表。
     *
     * 用户可以选择多个文件，结果类型为 `List<File?>`。
     * 如果用户取消选择，返回空列表。
     *
     * @param maxItems 最大选择数量限制
     *   - `null`：无限制
     *   - 正整数：最多可选择指定数量的文件
     */
    data class Multiple(val maxItems: Int? = null) : FileMode<List<File?>>()
}
