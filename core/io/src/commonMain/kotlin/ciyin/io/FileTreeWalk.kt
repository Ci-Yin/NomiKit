/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package ciyin.io

import ciyin.lang.peek
import ciyin.lang.pop
import ciyin.lang.push
import okio.IOException


/**
 * 用于描述可能的遍历方向的枚举。
 * 有两种方向：从父目录开始，到子目录结束；
 * 以及从子目录开始，到父目录结束。两者都使用深度优先搜索。
 */
enum class FileWalkDirection {
    /** 深度优先搜索，目录在其文件之前被访问 */
    TOP_DOWN,

    /** 深度优先搜索，目录在其文件之后被访问 */
    BOTTOM_UP
    // 我们是否也需要广度优先搜索？
}

/**
 * 此类旨在实现不同的文件遍历方法。
 * 它允许迭代给定目录内的所有文件。
 *
 * 使用 [File.walk]、[File.walkTopDown] 或 [File.walkBottomUp] 扩展函数来实例化一个 `FileTreeWalk` 实例。
 *
 * 如果给定的文件路径只是一个文件，遍历器只迭代该文件。
 * 如果给定的文件路径不存在，遍历器不迭代任何内容，即它等同于一个空序列。
 */
class FileTreeWalk private constructor(
    private val start: File,
    private val direction: FileWalkDirection = FileWalkDirection.TOP_DOWN,
    private val onEnter: ((File) -> Boolean)?,
    private val onLeave: ((File) -> Unit)?,
    private val onFail: ((f: File, e: IOException) -> Unit)?,
    private val maxDepth: Int = Int.MAX_VALUE
) : Sequence<File> {

    internal constructor(
        start: File,
        direction: FileWalkDirection = FileWalkDirection.TOP_DOWN
    ) : this(
        start,
        direction,
        null,
        null,
        null
    )


    /** 返回一个遍历文件的迭代器。 */
    override fun iterator(): Iterator<File> = FileTreeWalkIterator()

    /** 封装了从给定 [root] 开始按某种顺序访问文件的抽象类 */
    private abstract class WalkState(val root: File) {
        /** 调用此函数会前进到下一个要访问的文件并返回它 */
        abstract fun step(): File?
    }

    /** 封装了从给定 [rootDir] 开始按某种顺序访问目录的抽象类 */
    private abstract class DirectoryState(rootDir: File) : WalkState(rootDir)

    private inner class FileTreeWalkIterator : AbstractIterator<File>() {

        // 目录状态的堆栈，从起始目录开始
        private val state = ArrayDeque<WalkState>()

        init {
            when {
                start.isDirectory -> state.push(directoryState(start))
                start.isFile -> state.push(SingleFileState(start))
                else -> done()
            }
        }

        override fun computeNext() {
            val nextFile = gotoNext()
            if (nextFile != null)
                setNext(nextFile)
            else
                done()
        }


        private fun directoryState(root: File): DirectoryState {
            return when (direction) {
                FileWalkDirection.TOP_DOWN -> TopDownDirectoryState(root)
                FileWalkDirection.BOTTOM_UP -> BottomUpDirectoryState(root)
            }
        }

        private tailrec fun gotoNext(): File? {
            // 从堆栈顶部获取下一个文件，如果堆栈为空则返回
            val topState = state.peek() ?: return null
            val file = topState.step()
            if (file == null) {
                // 堆栈顶部没有更多内容，返回上一级
                state.pop()
                return gotoNext()
            } else {
                // 检查文件/目录是否匹配过滤器
                if (file == topState.root || !file.isDirectory || state.size >= maxDepth) {
                    // 处理根目录或简单文件
                    return file
                } else {
                    // 处理子目录
                    state.push(directoryState(file))
                    return gotoNext()
                }
            }
        }

        /** 按自下而上的顺序访问 */
        private inner class BottomUpDirectoryState(rootDir: File) : DirectoryState(rootDir) {

            private var rootVisited = false

            private var fileList: Array<File>? = null

            private var fileIndex = 0

            private var failed = false

            /** 首先是所有子项，然后是根目录 */
            override fun step(): File? {
                if (!failed && fileList == null) {
                    if (onEnter?.invoke(root) == false) {
                        return null
                    }

                    fileList = root.listFiles()
                    if (fileList == null) {
                        onFail?.invoke(
                            root,
                            AccessDeniedException(file = root, reason = "无法列出目录中的文件")
                        )
                        failed = true
                    }
                }
                if (fileList != null && fileIndex < fileList!!.size) {
                    // 首先访问所有文件
                    return fileList!![fileIndex++]
                } else if (!rootVisited) {
                    // 然后访问根
                    rootVisited = true
                    return root
                } else {
                    // 结束
                    onLeave?.invoke(root)
                    return null
                }
            }
        }

        /** 按自上而下的顺序访问 */
        private inner class TopDownDirectoryState(rootDir: File) : DirectoryState(rootDir) {

            private var rootVisited = false

            private var fileList: Array<File>? = null

            private var fileIndex = 0

            /** 首先是根目录，然后是所有子项 */
            override fun step(): File? {
                if (!rootVisited) {
                    // 首先访问根
                    if (onEnter?.invoke(root) == false) {
                        return null
                    }

                    rootVisited = true
                    return root
                } else if (fileList == null || fileIndex < fileList!!.size) {
                    if (fileList == null) {
                        // 然后读取文件数组（如果有）
                        fileList = root.listFiles()
                        if (fileList == null) {
                            onFail?.invoke(
                                root,
                                AccessDeniedException(file = root, reason = "无法列出目录中的文件")
                            )
                        }
                        if (fileList == null || fileList!!.isEmpty()) {
                            onLeave?.invoke(root)
                            return null
                        }
                    }
                    // 然后访问所有文件
                    return fileList!![fileIndex++]
                } else {
                    // 结束
                    onLeave?.invoke(root)
                    return null
                }
            }
        }

        private inner class SingleFileState(rootFile: File) : WalkState(rootFile) {
            private var visited: Boolean = false

            override fun step(): File? {
                if (visited) return null
                visited = true
                return root
            }
        }

    }

    /**
     * 设置一个谓词 [function]，在访问任何目录的文件之前以及访问目录本身之前调用该函数。
     *
     * 如果 [function] 返回 `false`，则不进入该目录，也不访问该目录及其文件。
     */
    fun onEnter(function: (File) -> Boolean): FileTreeWalk {
        return FileTreeWalk(
            start,
            direction,
            onEnter = function,
            onLeave = onLeave,
            onFail = onFail,
            maxDepth = maxDepth
        )
    }

    /**
     * 设置一个回调 [function]，在访问任何目录的文件之后以及访问目录本身之后调用该函数。
     */
    fun onLeave(function: (File) -> Unit): FileTreeWalk {
        return FileTreeWalk(
            start,
            direction,
            onEnter = onEnter,
            onLeave = function,
            onFail = onFail,
            maxDepth = maxDepth
        )
    }

    /**
     * 设置一个回调 [function]，当无法获取目录的文件列表时调用该函数。
     *
     * 在这种情况下，[onEnter] 和 [onLeave] 回调函数仍会被调用。
     */
    fun onFail(function: (File, IOException) -> Unit): FileTreeWalk {
        return FileTreeWalk(
            start,
            direction,
            onEnter = onEnter,
            onLeave = onLeave,
            onFail = function,
            maxDepth = maxDepth
        )
    }

    /**
     * 设置要遍历的目录树的最大 [depth]。默认没有限制。
     *
     * 该值必须为正数，[Int.MAX_VALUE] 用于指定无限深度。
     *
     * 值为 1 时，遍历器仅访问源目录及其所有直接子项；
     * 值为 2 时，还会访问孙子项，依此类推。
     */
    fun maxDepth(depth: Int): FileTreeWalk {
        if (depth <= 0)
            throw IllegalArgumentException("depth 必须为正数，但当前为 $depth。")
        return FileTreeWalk(start, direction, onEnter, onLeave, onFail, depth)
    }
}

/**
 * 获取用于访问此目录及其所有内容的序列。
 *
 * @param direction 遍历方向，自上而下（默认）或自下而上。
 */
fun File.walk(direction: FileWalkDirection = FileWalkDirection.TOP_DOWN): FileTreeWalk =
    FileTreeWalk(this, direction)

/**
 * 获取用于按自上而下顺序访问此目录及其所有内容的序列。
 * 使用深度优先搜索，目录在其所有文件之前被访问。
 */
fun File.walkTopDown(): FileTreeWalk = walk(FileWalkDirection.TOP_DOWN)

/**
 * 获取用于按自下而上顺序访问此目录及其所有内容的序列。
 * 使用深度优先搜索，目录在其所有文件之后被访问。
 */
fun File.walkBottomUp(): FileTreeWalk = walk(FileWalkDirection.BOTTOM_UP)
