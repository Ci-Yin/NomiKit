package ciyin.ai.chat.openai.mapper

import ciyin.ai.chat.openai.client.OpenAiChatStreamHttpException
import ciyin.ai.core.error.AiEngineError
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ErrorMapperTest {

    @Test
    fun `parseOpenAiStyleErrorMessage 读取 error message`() {
        val body = """{"error":{"message":"Model Not Exist","type":"invalid_request_error"}}"""
        assertEquals("Model Not Exist", parseOpenAiStyleErrorMessage(body))
    }

    @Test
    fun `OpenAiChatStreamHttpException 映射为含上游文案的 Protocol`() {
        val ex = OpenAiChatStreamHttpException(
            HttpStatusCode.BadRequest,
            """{"error":{"message":"Model Not Exist","type":"invalid_request_error"}}""",
        )
        val err = ex.toAiEngineError()
        val protocol = assertIs<AiEngineError.Protocol>(err)
        assertEquals("Model Not Exist", protocol.message)
    }
}
