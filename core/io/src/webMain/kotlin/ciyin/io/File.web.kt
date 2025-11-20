package ciyin.io

import okio.Buffer
import okio.FileHandle
import okio.FileMetadata
import okio.FileNotFoundException
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.Sink
import okio.Source
import okio.Timeout


// 抽象 Web Storage 操作接口
internal interface WebStorageAdapter {
    fun getItem(key: String): String?
    fun setItem(key: String, value: String)
    fun removeItem(key: String)
}

// 公共实现，只依赖 WebStorageAdapter
class BaseWebFileSystem internal constructor(private val storage: WebStorageAdapter) :
    FileSystem() {

    private val CONTENT_PREFIX = "fs_content:"
    private val META_PREFIX = "fs_meta:"
    private val DIR_PREFIX = "fs_dir:"

    private fun contentKey(path: Path) = "$CONTENT_PREFIX$path"
    private fun metaKey(path: Path) = "$META_PREFIX$path"
    private fun dirKey(path: Path) = "$DIR_PREFIX$path"

    override fun canonicalize(path: Path): Path {
        val parts = path.toString().split("/").filter { it.isNotEmpty() }
        val canonical = mutableListOf<String>()

        for (part in parts) {
            when (part) {
                "." -> continue
                ".." -> if (canonical.isNotEmpty()) canonical.removeLast()
                else -> canonical.add(part)
            }
        }

        return ("/" + canonical.joinToString("/")).toPath()
    }

    override fun metadataOrNull(path: Path): FileMetadata? {
        val metaJson = storage.getItem(metaKey(path)) ?: return null
        val parts = metaJson.split(":")
        if (parts.size < 3) return null

        return FileMetadata(
            isRegularFile = parts[2].toBoolean(),
            isDirectory = parts[0] == "directory",
            size = parts[1].toLongOrNull() ?: 0L
        )
    }

    override fun list(dir: Path): List<Path> {
        return listOrNull(dir) ?: throw FileNotFoundException("Directory not found: $dir")
    }

    override fun listOrNull(dir: Path): List<Path>? {
        val dirList = storage.getItem(dirKey(dir)) ?: return null
        if (dirList.isEmpty()) return emptyList()
        return dirList.split(",").map { (dir / it) }
    }

    override fun openReadOnly(file: Path): FileHandle {
        throw UnsupportedOperationException("FileHandle not supported in Web")
    }

    override fun openReadWrite(file: Path, mustCreate: Boolean, mustExist: Boolean): FileHandle {
        throw UnsupportedOperationException("FileHandle not supported in Web")
    }

    override fun source(file: Path): Source {
        val content = storage.getItem(contentKey(file))
            ?: throw FileNotFoundException("File not found: $file")

        return object : Source {
            private val buffer = Buffer().apply { writeUtf8(content) }
            private var closed = false

            override fun read(sink: Buffer, byteCount: Long): Long {
                if (closed) throw IllegalStateException("Source is closed")
                return buffer.read(sink, byteCount)
            }

            override fun timeout() = Timeout.NONE
            override fun close() {
                closed = true
            }
        }
    }

    override fun sink(file: Path, mustCreate: Boolean): Sink {
        if (mustCreate && storage.getItem(contentKey(file)) != null) {
            throw IOException("File already exists: $file")
        }
        return createSink(file, append = false)
    }

    override fun appendingSink(file: Path, mustExist: Boolean): Sink {
        if (mustExist && storage.getItem(contentKey(file)) == null) {
            throw FileNotFoundException("File does not exist: $file")
        }
        return createSink(file, append = true)
    }

    private fun createSink(file: Path, append: Boolean): Sink {
        val existingContent = if (append) storage.getItem(contentKey(file)) ?: "" else ""

        return object : Sink {
            private val buffer = Buffer().apply {
                if (existingContent.isNotEmpty()) writeUtf8(existingContent)
            }
            private var closed = false

            override fun write(source: Buffer, byteCount: Long) {
                if (closed) throw IllegalStateException("Sink is closed")
                buffer.write(source, byteCount)
            }

            override fun flush() {
                if (closed) throw IllegalStateException("Sink is closed")
                val content = buffer.readUtf8()
                buffer.writeUtf8(content)
                storage.setItem(contentKey(file), content)
                val size = content.length.toLong()
                storage.setItem(metaKey(file), "file:$size:true")
                updateParentDirectory(file)
            }

            override fun timeout() = Timeout.NONE

            override fun close() {
                if (!closed) {
                    flush()
                    closed = true
                }
            }
        }
    }

    override fun createDirectory(dir: Path, mustCreate: Boolean) {
        val existing = storage.getItem(dirKey(dir))

        if (mustCreate && existing != null) {
            throw IOException("Directory already exists: $dir")
        }

        if (existing == null) {
            storage.setItem(dirKey(dir), "")
            storage.setItem(metaKey(dir), "directory:0:false")
            updateParentDirectory(dir)
        }
    }

    override fun atomicMove(source: Path, target: Path) {
        val content = storage.getItem(contentKey(source))
            ?: throw FileNotFoundException("Source not found: $source")

        storage.setItem(contentKey(target), content)

        val meta = storage.getItem(metaKey(source))
        if (meta != null) {
            storage.setItem(metaKey(target), meta)
        }

        storage.removeItem(contentKey(source))
        storage.removeItem(metaKey(source))

        updateParentDirectory(source)
        updateParentDirectory(target)
    }

    override fun delete(path: Path, mustExist: Boolean) {
        val exists = storage.getItem(contentKey(path)) != null ||
                storage.getItem(dirKey(path)) != null

        if (mustExist && !exists) {
            throw FileNotFoundException("Path does not exist: $path")
        }

        if (exists) {
            storage.removeItem(contentKey(path))
            storage.removeItem(metaKey(path))
            storage.removeItem(dirKey(path))
            updateParentDirectory(path)
        }
    }

    override fun createSymlink(source: Path, target: Path) {
        throw UnsupportedOperationException("Symlinks not supported in Web")
    }

    private fun updateParentDirectory(path: Path) {
        val parent = path.parent ?: return
        val name = path.name

        val currentList = storage.getItem(dirKey(parent))?.split(",")?.toMutableSet()
            ?: mutableSetOf()

        val exists = storage.getItem(contentKey(path)) != null ||
                storage.getItem(dirKey(path)) != null

        if (exists) {
            currentList.add(name)
        } else {
            currentList.remove(name)
        }

        if (storage.getItem(dirKey(parent)) == null) {
            createDirectory(parent, mustCreate = false)
        }

        storage.setItem(dirKey(parent), currentList.filter { it.isNotEmpty() }.joinToString(","))
    }
}