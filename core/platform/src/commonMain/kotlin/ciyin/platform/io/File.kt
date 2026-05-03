package ciyin.platform.io

import ciyin.io.File
import ciyin.platform.Context


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2026/5/3 05:09
 */

/**
 * 从任意 Uri（content:// 或 file://）复制内容到临时缓存文件，并返回该文件路径。
 *
 * 支持 Android Q+ 的分区存储，不依赖真实文件路径。
 *
 * @param context 上下文（建议传入 ApplicationContext）
 * @return 新创建的缓存文件
 */
expect suspend fun String.copyUriToTempFile(context: Context): File