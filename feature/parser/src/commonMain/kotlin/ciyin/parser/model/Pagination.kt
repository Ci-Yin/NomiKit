package ciyin.parser.model

/**
 * 解析结果的页码分页状态。
 *
 * 页码从 1 开始。无法从站点响应可靠判断总页数或下一页状态时使用 `null`，
 * 不使用虚构的固定页数代替未知状态。
 *
 * @property page 当前请求页码
 * @property totalPages 服务端明确返回的总页数，未知时为 `null`
 * @property hasNext 是否存在下一页，无法判断时为 `null`
 */
data class Pagination(
    val page: Int = 1,
    val totalPages: Int? = null,
    val hasNext: Boolean? = null,
) {
    init {
        require(page >= 1) {
            "分页页码必须大于等于 1：$page"
        }
        require(totalPages == null || totalPages >= 1) {
            "总页数必须大于 0：$totalPages"
        }
    }
}
