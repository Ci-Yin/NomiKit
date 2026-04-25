package ciyin.sdwebui.extension

import ciyin.sdwebui.payload.script.ControlNetScriptArgs
import ciyin.sdwebui.payload.script.ScriptPayload
import ciyin.sdwebui.process.Process

/**
 * ControlNet 多单元 DSL：生成 [ScriptPayload.Multiple] 并可通过 [Companion.controlNet] 挂到 [Process.Builder]。
 */
class ControlNet private constructor(
    internal val units: List<Unit>,
) : Extension {

    /**
     * 单个 ControlNet 控制单元（对应 `alwayson_scripts` 中的一项参数对象）。
     */
    class Unit private constructor(
        internal val args: ControlNetScriptArgs,
    ) {

        /**
         * 配置模块、模型、权重与引导区间等 ControlNet 字段。
         */
        class Builder {

            private var inputImage: String? = null
            private var module: String = "none"
            private var model: String = "None"
            private var weight: Float = 1.0f
            private var resizeMode: String = "Resize and Fill"
            private var lowVRam: Boolean = false
            private var mask: String? = null
            private var processorRes: Int = 512
            private var thresholdA: Float = 64f
            private var thresholdB: Float = 64f
            private var guidance: Float = 1f
            private var guidanceStart: Float = 0.0f
            private var guidanceEnd: Float = 1.0f
            private var controlMode: Int = 0
            private var pixelPerfect: Boolean = false

            fun inputImage(inputImage: String?) = apply {
                this.inputImage = inputImage
            }

            fun module(module: String) = apply {
                this.module = module
            }

            fun model(model: String) = apply {
                this.model = model
            }

            fun weight(weight: Float) = apply {
                this.weight = weight
            }

            fun resizeMode(resizeMode: String) = apply {
                this.resizeMode = resizeMode
            }

            fun lowVRam(lowVRam: Boolean) = apply {
                this.lowVRam = lowVRam
            }

            fun mask(mask: String?) = apply {
                this.mask = mask
            }

            fun processorRes(processorRes: Int) = apply {
                this.processorRes = processorRes
            }

            fun thresholdA(thresholdA: Float) = apply {
                this.thresholdA = thresholdA
            }

            fun thresholdB(thresholdB: Float) = apply {
                this.thresholdB = thresholdB
            }

            fun guidance(guidance: Float) = apply {
                this.guidance = guidance
            }

            fun guidanceStart(guidanceStart: Float) = apply {
                this.guidanceStart = guidanceStart
            }

            fun guidanceEnd(guidanceEnd: Float) = apply {
                this.guidanceEnd = guidanceEnd
            }

            fun controlMode(controlMode: Int) = apply {
                this.controlMode = controlMode
            }

            fun pixelPerfect(pixelPerfect: Boolean) = apply {
                this.pixelPerfect = pixelPerfect
            }

            /**
             * 生成不可变的 [Unit]。
             */
            fun build() = Unit(
                args = ControlNetScriptArgs(
                    inputImage = inputImage,
                    module = module,
                    model = model,
                    weight = weight,
                    resizeMode = resizeMode,
                    lowVRam = lowVRam,
                    mask = mask,
                    processorRes = processorRes,
                    thresholdA = thresholdA,
                    thresholdB = thresholdB,
                    guidance = guidance,
                    guidanceStart = guidanceStart,
                    guidanceEnd = guidanceEnd,
                    controlMode = controlMode,
                    pixelPerfect = pixelPerfect,
                )
            )
        }
    }

    /**
     * 聚合多个 [Unit] 为一次 ControlNet 脚本负载。
     */
    class Builder {

        private val units = mutableListOf<Unit>()

        /**
         * 追加若干控制单元。
         */
        fun addUnit(vararg unit: Unit) = apply { units.addAll(unit) }

        /**
         * 组装 [ControlNet] 实例。
         */
        fun build(): ControlNet = ControlNet(units)
    }

    companion object {

        /**
         * 使用 DSL 创建单个 [Unit]。
         */
        fun controlNetUnit(init: Unit.Builder.() -> kotlin.Unit): Unit {
            val builder = Unit.Builder()
            builder.init()
            return builder.build()
        }

        /**
         * 使用 DSL 创建包含多单元的 [ControlNet]。
         */
        fun controlNet(init: Builder.() -> kotlin.Unit): ControlNet {
            val builder = Builder()
            builder.init()
            return builder.build()
        }

        /**
         * 将 [ControlNet] 注册为键名为 `ControlNet` 的常驻脚本。
         */
        fun <T : Process.Builder> T.controlNet(controlNet: ControlNet) = apply {
            addAlwaysonScript("ControlNet", ScriptPayload.Multiple(controlNet.units.map { unit -> unit.args }))
        }
    }
}
