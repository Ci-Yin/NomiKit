package ciyin.ai.chat.openai.client

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * 读取 SSE 响应中的 `data:` 帧。
 *
 * 这里只保留对 OpenAI 兼容协议所需的最小语义：
 * - 累积同一事件内的多行 `data:`；
 * - 遇到空行时产出一帧；
 * - 注释行与其他字段一律忽略。
 */
internal fun ByteReadChannel.readSseDataFrames(): Flow<String> = flow {
    val lines = mutableListOf<String>()
    while (!isClosedForRead) {
        val line = readUTF8Line() ?: break
        when {
            line.isBlank() -> {
                if (lines.isNotEmpty()) {
                    emit(lines.joinToString("\n"))
                    lines.clear()
                }
            }

            line.startsWith("data:") -> lines += line.removePrefix("data:").trimStart()
            line.startsWith(":") -> Unit
            else -> Unit
        }
    }
    if (lines.isNotEmpty()) {
        emit(lines.joinToString("\n"))
    }
}
