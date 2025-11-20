package ciyin.io

/**
 * 文件列表和遍历相关扩展函数
 */

/**
 * 返回当前目录下的文件和目录的字符串列表。
 *
 * @param addDir 如果为 true，则返回的列表包含目录路径；如果为 false，则仅包含文件或目录名称。
 * @return 包含当前目录下所有文件和目录的字符串列表。
 */
fun File.list2(addDir: Boolean = true): MutableList<String> {
    return if (addDir) {
        list()?.map { concat(it) } as MutableList<String>? ?: mutableListOf()
    } else {
        list()?.toMutableList() ?: mutableListOf()
    }
}

/**
 * 返回当前目录下的文件列表。
 *
 * @return 包含当前目录下所有文件的文件对象列表。
 */
fun File.listFiles2(): MutableList<File> {
    return listFiles()?.toMutableList() ?: mutableListOf()
}

/**
 * 遍历文件夹及其子文件夹
 *
 * @param direction 遍历方向，默认为从上到下
 * @param onProgress 进度回调，参数为当前遍历到的文件对象，返回 `false` 可取消遍历
 */
fun File.walkFileTree(
    direction: FileWalkDirection = FileWalkDirection.TOP_DOWN,
    onProgress: (File) -> Boolean = { true }
) {
    walk(direction).forEach {
        if (!onProgress(it)) return@forEach
    }
}
