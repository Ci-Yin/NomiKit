package com.ciyin.app.domain.project

/**
 * 代表应用程序支持的不同平台的枚举类。
 */
enum class Platform {
    /**
     * 表示Windows平台的枚举值。
     *
     * 该枚举值用于标识应用程序运行在Windows操作系统上。
     */
    Windows,

    /**
     * 代表Android平台的枚举成员。此枚举值用于标识应用程序运行在Android操作系统上。
     */
    Android,

    /**
     * 表示应用程序支持的平台中的Web平台。
     *
     * 该枚举值用于标识运行环境为Web浏览器或基于Web的技术栈。
     */
    Web,
}