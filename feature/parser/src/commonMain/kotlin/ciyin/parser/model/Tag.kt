package ciyin.parser.model

/**
 * 标签信息。
 */
data class Tag(
    val tag: String = "",
    val count: Int = 0,
    val category: TagCategory = TagCategory.General,
    val updatedAt: Long = 0L,
    val createdAt: Long = 0L,
)