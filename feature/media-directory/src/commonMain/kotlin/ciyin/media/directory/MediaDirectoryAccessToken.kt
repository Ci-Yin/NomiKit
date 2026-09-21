package ciyin.media.directory

import kotlin.jvm.JvmInline

/**
 * 用户授权某个目录后得到的持久化不透明令牌。
 *
 * 内部内容由平台解释：
 * - Android：SAF Tree URI（`content://com.android.externalstorage.documents/tree/...`）
 * - iOS：security-scoped bookmark 字节的 base64 字符串
 * - Desktop：归一化的绝对路径
 *
 * Domain 与应用层不得解析或拼接此值。
 */
@JvmInline
value class MediaDirectoryAccessToken(val value: String)
