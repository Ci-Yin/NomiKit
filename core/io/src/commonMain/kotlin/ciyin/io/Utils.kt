package ciyin.io

import okio.Buffer
import okio.IOException
import okio.buffer
import okio.use

/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

/**
 * 返回此文件的扩展名（不包括点），如果没有，则返回空字符串。
 */
val String.extension: String get() = substringAfterLast('.', "")

/**
 * 返回此文件的扩展名（不包括点），如果没有，则返回空字符串。
 */
val File.extension: String get() = name.extension

/**
 * 使用不变分隔符 '/' 返回此文件的 [path][File.path]
 * 在名称序列中分隔名称。
 */
val File.invariantSeparatorsPath: String
    get() = if (File.separatorChar != '/') path.replace(File.separatorChar, '/') else path

/**
 * 返回不带扩展名的文件名。
 */
val File.nameWithoutExtension: String get() = name.substringBeforeLast(".")

/**
 * 计算此文件相对于 [base] 文件的相对路径。
 * 注意，[base] 文件被视为一个目录。
 * 如果此文件与 [base] 文件匹配，则返回一个空字符串。
 *
 * @return 从 [base] 到此文件的相对路径。
 *
 * @throws IllegalArgumentException 如果此文件和 base 文件的路径具有不同的根。
 */
fun File.toRelativeString(base: File): String =
    toRelativeStringOrNull(base)
        ?: throw IllegalArgumentException("此文件和 base 文件具有不同的根: $this 和 $base.")

/**
 * 计算此文件相对于 [base] 文件的相对路径。
 * 注意，[base] 文件被视为一个目录。
 * 如果此文件与 [base] 文件匹配，则将返回一个路径为空的 [File]。
 *
 * @return 带有从 [base] 到此文件的相对路径的 File 对象。
 *
 * @throws IllegalArgumentException 如果此文件和 base 文件的路径具有不同的根。
 */
fun File.relativeTo(base: File): File = File(this.toRelativeString(base))

/**
 * 计算此文件相对于 [base] 文件的相对路径。
 * 注意，[base] 文件被视为一个目录。
 * 如果此文件与 [base] 文件匹配，则将返回一个路径为空的 [File]。
 *
 * @return 带有从 [base] 到此文件的相对路径的 File 对象，如果此文件和 base 文件的路径具有不同的根，则返回 `this`。
 */
fun File.relativeToOrSelf(base: File): File =
    toRelativeStringOrNull(base)?.let(::File) ?: this

/**
 * 计算此文件相对于 [base] 文件的相对路径。
 * 注意，[base] 文件被视为一个目录。
 * 如果此文件与 [base] 文件匹配，则将返回一个路径为空的 [File]。
 *
 * @return 带有从 [base] 到此文件的相对路径的 File 对象，如果此文件和 base 文件的路径具有不同的根，则返回 `null`。
 */
fun File.relativeToOrNull(base: File): File? =
    toRelativeStringOrNull(base)?.let(::File)


private fun File.toRelativeStringOrNull(base: File): String? {
    // 检查根
    val thisComponents = this.toComponents().normalize()
    val baseComponents = base.toComponents().normalize()
    if (thisComponents.root != baseComponents.root) {
        return null
    }

    val baseCount = baseComponents.size
    val thisCount = thisComponents.size

    val sameCount = run countSame@{
        var i = 0
        val maxSameCount = minOf(thisCount, baseCount)
        while (i < maxSameCount && thisComponents.segments[i] == baseComponents.segments[i])
            i++
        return@countSame i
    }

    // 通过添加所需数量的 .. 部分来消除不同的基本组件
    val res = StringBuilder()
    for (i in baseCount - 1 downTo sameCount) {
        if (baseComponents.segments[i].name == "..") {
            return null
        }

        res.append("..")

        if (i != sameCount) {
            res.append(File.separatorChar)
        }
    }

    // 添加剩余的 this 组件
    if (sameCount < thisCount) {
        // 如果附加了一些 ..
        if (sameCount < baseCount)
            res.append(File.separatorChar)

        thisComponents.segments.drop(sameCount).joinTo(res, File.separator)
    }

    return res.toString()
}


/**
 * 将此文件复制到给定的 [target] 文件。
 *
 * 如果通往 [target] 的路径中缺少某些目录，则会创建它们。
 * 如果 [target] 文件已存在，除非将 [overwrite] 参数设置为 `true`，否则此函数将失败。
 *
 * 当 [overwrite] 为 `true` 且 [target] 是一个目录时，只有在目录为空时才会被替换。
 *
 * 如果此文件是一个目录，它将被复制，但不包含其内容，即会创建一个空的 [target] 目录。
 * 如果要复制包含其内容的目录，请使用 [copyRecursively]。
 *
 * 该操作不保留复制文件的属性，例如创建/修改日期、权限等。
 *
 * @param overwrite 如果允许覆盖目标，则为 `true`。
 * @param bufferSize 复制时使用的缓冲区大小。
 * @return [target] 文件。
 * @throws NoSuchFileException 如果源文件不存在。
 * @throws FileAlreadyExistsException 如果目标文件已存在且 [overwrite] 参数设置为 `false`。
 * @throws IOException 如果在复制过程中发生任何错误。
 */
fun File.copyTo(
    target: File,
    overwrite: Boolean = false,
    bufferSize: Long = DEFAULT_BUFFER_SIZE
): File {
    if (!this.exists()) {
        throw NoSuchFileException(file = this, reason = "源文件不存在。")
    }

    if (target.exists()) {
        if (!overwrite)
            throw FileAlreadyExistsException(
                file = this,
                other = target,
                reason = "目标文件已存在。"
            )
        else if (!target.delete())
            throw FileAlreadyExistsException(
                file = this,
                other = target,
                reason = "试图覆盖目标，但删除失败。"
            )
    }

    if (this.isDirectory) {
        if (!target.mkdirs())
            throw FileSystemException(file = this, other = target, reason = "创建目标目录失败。")
    } else {
        target.parentFile?.mkdirs()

        // 使用 Buffer 进行流式复制
        SystemFileSystem.source(toPath()).buffer().use { source ->
            SystemFileSystem.sink(target.toPath()).buffer().use { sink ->
                val buffer = Buffer()
                while (source.read(buffer, bufferSize) != -1L) {
                    sink.writeAll(buffer)
                }
            }
        }

    }

    return target
}

/**
 * 可用于指定 `copyRecursively()` 函数在异常情况下的行为的枚举。
 */
enum class OnErrorAction {
    /** 跳过此文件并继续下一个。 */
    SKIP,

    /** 终止函数的执行。 */
    TERMINATE
}

/** 私有异常类，用于终止递归复制。 */
private class TerminateException(file: File) : FileSystemException(file) {}

/**
 * 将此文件及其所有子文件复制到指定的目标 [target] 路径。
 * 如果通往目标的路径中缺少某些目录，则会创建它们。
 *
 * 如果此文件路径指向单个文件，则它将被复制到路径为 [target] 的文件。
 * 如果此文件路径指向一个目录，则其子文件将被复制到路径为 [target] 的目录。
 *
 * 如果 [target] 已存在，并且 [overwrite] 参数允许，则在复制之前会将其删除。
 *
 * 该操作不保留复制文件的属性，例如创建/修改日期、权限等。
 *
 * 如果在复制过程中发生任何错误，则后续操作将取决于对 `onError(File, IOException)` 函数的调用结果，
 * 该函数将被调用并带有参数，指定导致错误的文件和异常本身。
 * 默认情况下，此函数会重新抛出异常。
 *
 * 可以传递给 `onError` 函数的异常：
 *
 * - [NoSuchFileException] - 如果尝试复制不存在的文件
 * - [FileAlreadyExistsException] - 如果存在冲突
 * - [AccessDeniedException] - 如果尝试打开目录失败
 * - [IOException] - 如果在复制时出现一些问题
 *
 * 请注意，如果此函数失败，则可能已发生部分复制。
 *
 * @param overwrite 如果允许覆盖现有目标文件和目录，则为 `true`。
 * @return 如果复制被终止，则为 `false`，否则为 `true`。
 */
fun File.copyRecursively(
    target: File,
    overwrite: Boolean = false,
    onError: (File, IOException) -> OnErrorAction = { _, exception -> throw exception }
): Boolean {
    if (!exists()) {
        return onError(this, NoSuchFileException(file = this, reason = "源文件不存在。")) !=
                OnErrorAction.TERMINATE
    }
    try {
        // 我们无法从 lambda 内部中断 for 循环，所以我们必须在这里使用异常
        for (src in walkTopDown().onFail { f, e ->
            if (onError(f, e) == OnErrorAction.TERMINATE) throw TerminateException(f)
        }
        ) {
            if (!src.exists()) {
                if (onError(
                        src,
                        NoSuchFileException(file = src, reason = "源文件不存在。")
                    ) == OnErrorAction.TERMINATE
                )
                    return false
            } else {
                val relPath = src.toRelativeString(this)
                val dstFile = File(target, relPath)
                if (dstFile.exists() && !(src.isDirectory && dstFile.isDirectory)) {
                    val stillExists = if (!overwrite) true else {
                        if (dstFile.isDirectory)
                            !dstFile.deleteRecursively()
                        else
                            !dstFile.delete()
                    }

                    if (stillExists) {
                        if (onError(
                                dstFile, FileAlreadyExistsException(
                                    file = src,
                                    other = dstFile,
                                    reason = "目标文件已存在。"
                                )
                            ) == OnErrorAction.TERMINATE
                        )
                            return false

                        continue
                    }
                }

                if (src.isDirectory) {
                    dstFile.mkdirs()
                } else {
                    if (src.copyTo(dstFile, overwrite).length() != src.length()) {
                        if (onError(
                                src,
                                IOException("源文件未完全复制，目标文件长度不同。")
                            ) == OnErrorAction.TERMINATE
                        )
                            return false
                    }
                }
            }
        }
        return true
    } catch (e: TerminateException) {
        return false
    }
}

/**
 * 删除此文件及其所有子文件。
 * 请注意，如果此操作失败，则可能已发生部分删除。
 *
 * @return 如果文件或目录成功删除，则为 `true`，否则为 `false`。
 */
fun File.deleteRecursively(): Boolean =
    walkBottomUp().fold(true, { res, it -> (it.delete() || !it.exists()) && res })

/**
 * 确定此文件是否与 [other] 属于同一个根，
 * 并且以与 [other] 相同顺序的所有组件开头。
 * 因此，如果 [other] 有 N 个组件，则 `this` 的前 N 个组件必须与 [other] 中的相同。
 *
 * @return 如果此路径以 [other] 路径开头，则为 `true`，否则为 `false`。
 */
fun File.startsWith(other: File): Boolean {
    val components = toComponents()
    val otherComponents = other.toComponents()
    if (components.root != otherComponents.root)
        return false
    return if (components.size < otherComponents.size) false
    else components.segments.subList(0, otherComponents.size).equals(otherComponents.segments)
}

/**
 * 确定此文件是否与 [other] 属于同一个根，
 * 并且以与 [other] 相同顺序的所有组件开头。
 * 因此，如果 [other] 有 N 个组件，则 `this` 的前 N 个组件必须与 [other] 中的相同。
 *
 * @return 如果此路径以 [other] 路径开头，则为 `true`，否则为 `false`。
 */
fun File.startsWith(other: String): Boolean = startsWith(File(other))

/**
 * 确定此文件路径是否以 [other] 文件的路径结尾。
 *
 * 如果 [other] 是根路径，它必须等于此文件路径。
 * 如果 [other] 是相对路径，则 `this` 的最后 N 个组件必须与 [other] 中的所有组件相同，
 * 其中 N 是 [other] 中的组件数。
 *
 * @return 如果此路径以 [other] 路径结尾，则为 `true`，否则为 `false`。
 */
fun File.endsWith(other: File): Boolean {
    val components = toComponents()
    val otherComponents = other.toComponents()
    if (otherComponents.isRooted)
        return this == other
    val shift = components.size - otherComponents.size
    return if (shift < 0) false
    else components.segments.subList(shift, components.size).equals(otherComponents.segments)
}

/**
 * 确定此文件是否与 [other] 属于同一个根，
 * 并且以与 [other] 相同顺序的所有组件结尾。
 * 因此，如果 [other] 有 N 个组件，则 `this` 的最后 N 个组件必须与 [other] 中的相同。
 * 对于相对的 [other]，`this` 可以属于任何根。
 *
 * @return 如果此路径以 [other] 路径结尾，则为 `true`，否则为 `false`。
 */
fun File.endsWith(other: String): Boolean = endsWith(File(other))

/**
 * 删除此文件名中的所有 . 并解析所有可能的 .. 。
 * 例如，`File("/foo/./bar/gav/../baaz").normalize()` 是 `File("/foo/bar/baaz")`。
 *
 * @return 规范化的路径名，其中 . 和可能的 .. 已被删除。
 */
fun File.normalize(): File =
    with(toComponents()) { root.resolve(segments.normalize().joinToString(File.separator)) }

private fun FilePathComponents.normalize(): FilePathComponents =
    FilePathComponents(root, segments.normalize())

private fun List<File>.normalize(): List<File> {
    val list: MutableList<File> = ArrayList(this.size)
    for (file in this) {
        when (file.name) {
            "." -> {}
            ".." -> if (!list.isEmpty() && list.last().name != "..") list.removeAt(list.size - 1) else list.add(
                file
            )

            else -> list.add(file)
        }
    }
    return list
}

/**
 * 将 [relative] 文件添加到此文件，将此文件视为目录。
 * 如果 [relative] 有根，则返回 [relative]。
 * 例如，`File("/foo/bar").resolve(File("gav"))` 是 `File("/foo/bar/gav")`。
 * 此函数与 [relativeTo] 互补，
 * 因此 `f.resolve(g.relativeTo(f)) == g` 应该始终为 `true`，除非根不同。
 *
 * @return 连接的 this 和 [relative] 路径，如果 [relative] 是绝对路径，则仅返回 [relative]。
 */
fun File.resolve(relative: File): File {
    if (relative.isRooted)
        return relative
    val baseName = this.toString()
    return if (baseName.isEmpty() || baseName.endsWith(File.separatorChar)) File(baseName + relative) else File(
        baseName + File.separatorChar + relative
    )
}

/**
 * 将 [relative] 名称添加到此文件，将此文件视为目录。
 * 如果 [relative] 有根，则返回 [relative]。
 * 例如，`File("/foo/bar").resolve("gav")` 是 `File("/foo/bar/gav")`。
 *
 * @return 连接的 this 和 [relative] 路径，如果 [relative] 是绝对路径，则仅返回 [relative]。
 */
fun File.resolve(relative: String): File = resolve(File(relative))

/**
 * 将 [relative] 文件添加到此父目录。
 * 如果 [relative] 有根或此文件没有父目录，则返回 [relative]。
 * 例如，`File("/foo/bar").resolveSibling(File("gav"))` 是 `File("/foo/gav")`。
 *
 * @return 连接的 this.parent 和 [relative] 路径，如果 [relative] 是绝对路径或此文件没有父目录，则仅返回 [relative]。
 */
fun File.resolveSibling(relative: File): File {
    val components = this.toComponents()
    val parentSubPath =
        if (components.size == 0) File("..") else components.subPath(0, components.size - 1)
    return components.root.resolve(parentSubPath).resolve(relative)
}

/**
 * 将 [relative] 名称添加到此父目录。
 * 如果 [relative] 有根或此文件没有父目录，则返回 [relative]。
 * 例如，`File("/foo/bar").resolveSibling("gav")` 是 `File("/foo/gav")`。
 *
 * @return 连接的 this.parent 和 [relative] 路径，如果 [relative] 是绝对路径或此文件没有父目录，则仅返回 [relative]。
 */
fun File.resolveSibling(relative: String): File = resolveSibling(File(relative))
