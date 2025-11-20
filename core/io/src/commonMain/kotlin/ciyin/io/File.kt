package ciyin.io

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import kotlin.js.JsName
import kotlin.jvm.JvmName

/**
 * 平台文件系统的 expect 定义。
 * 每个平台分别 actual 提供 FileSystem.SYSTEM。
 */
expect val SystemFileSystem: FileSystem

/**
 * Kotlin Multiplatform 通用 File 实现（基于 okio）
 *
 * 模拟 `java.io.File` 的常用功能，提供跨平台的文件系统访问。
 * 支持 JVM、Native、JS 等平台。
 *
 * 注意：
 * - 本实现依赖 [okio.FileSystem]，部分平台的文件权限或符号链接等特性可能不完全支持。
 * - 权限相关方法（如 `setWritable`、`setReadable`）为兼容接口，实际不会修改系统权限。
 */
class File {

    /** 文件系统实例（跨平台） */
    private val fileSystem: FileSystem = SystemFileSystem

    /** 当前文件路径 */
    private val _path: Path

    /** 根据路径字符串创建文件对象 */
    constructor(pathname: String) {
        this._path = pathname.toPath()
    }

    /** 根据父路径与子路径创建文件对象 */
    constructor(parent: String, child: String) {
        this._path = parent.toPath() / child
    }

    /** 根据父 File 与子路径创建文件对象 */
    constructor(parent: File?, child: String) {
        this._path = if (parent != null) parent._path / child else child.toPath()
    }

    /** 使用现有 [Path] 创建文件对象 */
    constructor(path: Path) {
        this._path = path
    }

    /** 文件名（不含路径部分） */
    val name: String get() = _path.name

    /** 文件路径字符串 */
    val path: String get() = _path.toString()

    /** 文件绝对路径（规范化后的完整路径） */
    val absolutePath: String get() = _path.normalized().toString()

    /** 文件规范路径（去除多余的 `.`、`..`） */
    val canonicalPath: String get() = _path.normalized().toString()

    /** 父目录路径 */
    val parent: String? get() = _path.parent?.toString()

    /** 父目录文件对象 */
    val parentFile: File? get() = _path.parent?.let { File(it) }

    val isFile get() = isFile()
    val isDirectory get() = isDirectory()

    /** 判断文件或目录是否存在 */
    fun exists(): Boolean = fileSystem.exists(_path)

    /** 判断是否为目录 */
    @JsName("checkIsDirectory")
    @JvmName("checkIsDirectory") // 改 JVM 名字，防止冲突
    fun isDirectory(): Boolean = try {
        fileSystem.metadata(_path).isDirectory
    } catch (e: Exception) {
        false
    }

    /** 判断是否为普通文件 */
    @JsName("checkIsFile")
    @JvmName("checkIsFile") // 改 JVM 名字，防止冲突
    fun isFile(): Boolean = try {
        fileSystem.metadata(_path).isRegularFile
    } catch (e: Exception) {
        false
    }

    /** 判断是否为隐藏文件（以 `.` 开头） */
    fun isHidden(): Boolean = name.startsWith(".")

    /** 是否可读（基于存在性判断） */
    fun canRead(): Boolean = exists()

    /** 是否可写（基于存在性判断） */
    fun canWrite(): Boolean = exists()

    /** 是否可执行（基于存在性判断） */
    fun canExecute(): Boolean = exists()

    /** 获取文件长度（字节数） */
    fun length(): Long = try {
        fileSystem.metadata(_path).size ?: 0L
    } catch (e: Exception) {
        0L
    }

    /** 获取最后修改时间（毫秒） */
    fun lastModified(): Long = try {
        fileSystem.metadata(_path).lastModifiedAtMillis ?: 0L
    } catch (e: Exception) {
        0L
    }

    /**
     * 设置最后修改时间（未实现，始终返回 false）
     */
    fun setLastModified(time: Long): Boolean = false

    /**
     * 将文件设为只读（未实现，始终返回 false）
     */
    fun setReadOnly(): Boolean = false

    /**
     * 设置文件可写状态（未实现，返回 true 以保持 API 兼容）
     */
    fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean = true

    /**
     * 设置文件可读状态（未实现，返回 true 以保持 API 兼容）
     */
    fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean = true

    /**
     * 设置文件可执行状态（未实现，返回 true 以保持 API 兼容）
     */
    fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean = true

    /**
     * 获取当前目录下的所有文件对象。
     * @return 文件数组，若不是目录或访问失败则返回 null。
     */
    fun listFiles(): Array<File>? = try {
        if (!isDirectory()) null
        else fileSystem.list(_path).map { File(it) }.toTypedArray()
    } catch (e: Exception) {
        null
    }

    /**
     * 获取当前目录下的文件名数组。
     * @return 文件名数组，若不是目录或访问失败则返回 null。
     */
    fun list(): Array<String>? = try {
        if (!isDirectory()) null
        else fileSystem.list(_path).map { it.name }.toTypedArray()
    } catch (e: Exception) {
        null
    }

    /**
     * 创建当前目录（不递归）
     * @return 是否创建成功。
     */
    fun mkdir(): Boolean = try {
        fileSystem.createDirectory(_path, mustCreate = false)
        true
    } catch (e: Exception) {
        false
    }

    /**
     * 递归创建目录。
     * @return 是否创建成功。
     */
    fun mkdirs(): Boolean = try {
        fileSystem.createDirectories(_path, mustCreate = false)
        true
    } catch (e: Exception) {
        false
    }

    /**
     * 删除文件或目录。
     * @return 是否删除成功。
     */
    fun delete(): Boolean = try {
        fileSystem.delete(_path, mustExist = false)
        true
    } catch (e: Exception) {
        false
    }

    /**
     * 进程退出时删除文件（无法跨平台实现，留空）
     */
    fun deleteOnExit() { /* 无法跨平台实现 */
    }

    /**
     * 重命名（或移动）文件。
     * @param dest 目标文件对象
     * @return 是否成功。
     */
    fun renameTo(dest: File): Boolean = try {
        fileSystem.atomicMove(_path, dest._path)
        true
    } catch (e: Exception) {
        false
    }

    /**
     * 创建一个新文件（若文件已存在则返回 false）。
     * @return 是否创建成功。
     */
    fun createNewFile(): Boolean = try {
        if (exists()) false else {
            fileSystem.write(_path, mustCreate = true) { }
            true
        }
    } catch (e: Exception) {
        false
    }

    /**
     * 在系统临时目录中创建临时文件。
     * @param prefix 文件名前缀
     * @param suffix 文件名后缀（可选）
     * @return 新创建的临时文件对象。
     */
    fun createTempFile(prefix: String, suffix: String?): File {
        val tmpDir = FileSystem.SYSTEM_TEMPORARY_DIRECTORY
        val tmpFile = tmpDir / ("$prefix${suffix ?: ".tmp"}")
        fileSystem.write(tmpFile, mustCreate = true) { }
        return File(tmpFile)
    }

    /** 转换为 [okio.Path] */
    fun toPath(): Path = _path

    /** 转换为 URI 格式字符串 */
    fun toURI(): String = "file://${_path.normalized()}"

    override fun hashCode(): Int = _path.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is File) return false
        return _path == other._path
    }

    override fun toString(): String = _path.toString()

    companion object {

        /** 路径分隔符（"/"） */
        const val separator: String = "/"

        /** 路径分隔符字符 */
        const val separatorChar: Char = '/'

        /** 多路径分隔符（":"） */
        const val pathSeparator: String = ":"

        /** 多路径分隔符字符 */
        const val pathSeparatorChar: Char = ':'

        /**
         * 创建临时文件。
         * @param prefix 文件名前缀
         * @param suffix 文件名后缀（可选）
         * @param directory 临时文件所在目录（可选）
         * @return 新创建的临时文件对象。
         */
        fun createTempFile(prefix: String, suffix: String?, directory: File? = null): File {
            val baseDir = directory?.toPath() ?: FileSystem.SYSTEM_TEMPORARY_DIRECTORY
            val tmpFile = baseDir / ("$prefix${suffix ?: ".tmp"}")
            SystemFileSystem.write(tmpFile, mustCreate = true) { }
            return File(tmpFile)
        }

        /**
         * 获取根目录列表（仅返回 "/"）
         */
        fun listRoots(): Array<File> = arrayOf(File("/"))
    }
}
