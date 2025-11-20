package ciyin.io

/**
 * 返回使用缓冲流时的默认缓冲区大小。
 */
const val DEFAULT_BUFFER_SIZE: Long = 8 * 1024

/**
 * 返回 forEachBlock() 的默认块大小。
 */
internal const val DEFAULT_BLOCK_SIZE: Long = 4096

/**
 * 返回 forEachBlock() 的最小块大小。
 */
internal const val MINIMUM_BLOCK_SIZE: Long = 512
