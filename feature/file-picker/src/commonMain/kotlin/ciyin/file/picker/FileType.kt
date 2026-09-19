package ciyin.file.picker

sealed class FileType {
    data object Image : FileType()
    data object Video : FileType()
    data object ImageAndVideo : FileType()
    data class File(val extensions: Set<String>? = null) : FileType() {
        constructor(vararg extensions: String) : this(extensions.toSet())
        constructor(extensions: List<String>) : this(extensions.toSet())
        constructor(extension: String) : this(setOf(extension))
    }
}
