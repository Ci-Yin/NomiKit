package ciyin.io

import okio.IOException

/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */


private fun constructMessage(file: File, other: File?, reason: String?): String {
    val sb = StringBuilder(file.toString())
    if (other != null) {
        sb.append(" -> $other")
    }
    if (reason != null) {
        sb.append(": $reason")
    }
    return sb.toString()
}

/**
 * 文件系统异常的基类。
 * @property file 执行失败操作的文件。
 * @property other 操作中涉及的第二个文件（如果有），例如复制或移动的目标文件。
 * @property reason 错误的描述信息。
 */
open class FileSystemException(
    val file: File,
    val other: File? = null,
    val reason: String? = null
) : IOException(constructMessage(file, other, reason))

/**
 * 当尝试创建或复制到的文件已经存在时，抛出此异常。
 */
class FileAlreadyExistsException(
    file: File,
    other: File? = null,
    reason: String? = null
) : FileSystemException(file, other, reason)

/**
 * 当没有足够权限执行某个操作时，抛出此异常。
 */
class AccessDeniedException(
    file: File,
    other: File? = null,
    reason: String? = null
) : FileSystemException(file, other, reason)

/**
 * 当要复制的文件不存在时，抛出此异常。
 */
class NoSuchFileException(
    file: File,
    other: File? = null,
    reason: String? = null
) : FileSystemException(file, other, reason)
