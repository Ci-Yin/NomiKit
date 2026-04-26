package com.ciyin.app.ui.screen.aichat

/**
 * AI 聊天示例页面副作用。
 */
internal sealed interface AiChatEffect {
    /** 请求外层导航返回上一页。 */
    data object NavigateBack : AiChatEffect
}
