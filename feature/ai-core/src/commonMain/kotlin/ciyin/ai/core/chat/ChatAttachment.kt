package ciyin.ai.core.chat

/**
 * 聊天消息附件。
 *
 * 既可在 [ChatMessage.User.attachments] 上挂载用户输入的多模态资源，
 * 也可在 [ChatRequest.attachments] 上做请求级附件（如 RAG 检索到的文档片段）。
 *
 * 二进制内容用 [bytes] 直接承载；引擎适配层会根据自身协议决定是 base64 上传、
 * 还是先上传到对象存储再传 URL。
 */
sealed interface ChatAttachment {

    /**
     * 图像附件（视觉理解）。
     *
     * @property bytes 原始字节，由调用方保证格式与 [mimeType] 匹配。
     * @property mimeType 标准 MIME 类型，如 `"image/png"` / `"image/jpeg"` / `"image/webp"`。
     */
    data class Image(
        val bytes: ByteArray,
        val mimeType: String,
    ) : ChatAttachment

    /**
     * 文档附件（如 PDF / Markdown / 纯文本片段）。
     *
     * @property bytes 原始字节。
     * @property mimeType 标准 MIME 类型，如 `"application/pdf"` / `"text/markdown"`。
     * @property fileName 可选的文件名，仅用于展示与日志。
     */
    data class Document(
        val bytes: ByteArray,
        val mimeType: String,
        val fileName: String? = null,
    ) : ChatAttachment

    /**
     * 音频附件（如 ASR 输入、语音对话）。
     *
     * @property bytes 原始字节。
     * @property mimeType 标准 MIME 类型，如 `"audio/wav"` / `"audio/mpeg"`。
     */
    data class Audio(
        val bytes: ByteArray,
        val mimeType: String,
    ) : ChatAttachment
}
