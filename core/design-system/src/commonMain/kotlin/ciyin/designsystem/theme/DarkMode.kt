package ciyin.designsystem.theme

/**
 * 应用主题的明暗模式请求。
 */
enum class DarkMode {
    /**
     * 强制使用浅色令牌。
     */
    Light,

    /**
     * 强制使用深色令牌。
     */
    Dark,

    /**
     * 请求跟随系统；核心层在组合边界解析一次。
     */
    System,
}
