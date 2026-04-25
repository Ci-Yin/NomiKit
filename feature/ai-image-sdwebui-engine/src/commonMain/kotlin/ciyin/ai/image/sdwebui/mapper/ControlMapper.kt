package ciyin.ai.image.sdwebui.mapper

import ciyin.ai.core.error.AiEngineError
import ciyin.ai.core.image.ImageControl
import ciyin.sdwebui.extension.ControlNet.Companion.controlNet
import ciyin.sdwebui.extension.ControlNet.Companion.controlNet as applyControlNet
import ciyin.sdwebui.extension.ControlNet.Companion.controlNetUnit
import ciyin.sdwebui.process.Process

/**
 * 把通用层 [ImageControl] 列表映射到 SD WebUI 的前置脚本 DSL。
 */
internal fun Process.Builder.applyControls(controls: List<ImageControl>) {
    if (controls.isEmpty()) return

    val controlNetUnits = controls.map { control ->
        when (control) {
            is ImageControl.ControlNet -> controlNetUnit {
                inputImage(control.image.toSdWebUiBase64())
                module(control.module)
                model(control.model)
                weight(control.weight)
                guidanceStart(control.guidanceStart)
                guidanceEnd(control.guidanceEnd)
            }

            is ImageControl.IPAdapter -> throw AiEngineErrorException(
                AiEngineError.Unsupported("SdWebUiImageEngine 暂不支持 IPAdapter"),
            )
        }
    }

    applyControlNet(
        controlNet {
            addUnit(*controlNetUnits.toTypedArray())
        },
    )
}
