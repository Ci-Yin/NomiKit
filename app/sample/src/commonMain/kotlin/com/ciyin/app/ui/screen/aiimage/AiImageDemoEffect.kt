package com.ciyin.app.ui.screen.aiimage

/**
 * 文生图演示页面的一次性副作用。
 *
 * 由 [AiImageDemoViewModel] 通过 `poseEffect` 触发，由 [AiImageDemoScreen] 中 `collectSideEffects` 消费。
 */
internal sealed interface AiImageDemoEffect {

    /** 请求宿主退栈（由 [AiImageDemoScreen] 的 `onBack` 回调执行）。 */
    data object NavigateBack : AiImageDemoEffect
}
