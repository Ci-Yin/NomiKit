package ciyin.ai.core.image

/**
 * 结构化条件控制。
 *
 * 描述"我想用一张姿势图 / 边缘图 / 参考图来约束本次生成"的通用意图；
 * 具体如何映射到底层 `alwayson script`（如 SD WebUI 的 `ControlNet`、ComfyUI 的 graph 节点）
 * 由各 `ImageEngine` 的 mapper 负责。
 */
sealed interface ImageControl {

    /**
     * ControlNet 条件控制。
     *
     * @property module 预处理模块名（如 `"openpose"` / `"canny"` / `"depth_midas"`）。
     * @property model ControlNet 模型名，需与目标引擎已加载的模型匹配。
     * @property image 控制图字节。
     * @property weight 控制权重，常见取值 `0.0..2.0`，默认 `1.0`。
     * @property guidanceStart 控制开始步占比，范围 `0.0..1.0`。
     * @property guidanceEnd 控制结束步占比，范围 `0.0..1.0`，需大于 [guidanceStart]。
     */
    data class ControlNet(
        val module: String,
        val model: String,
        val image: ByteArray,
        val weight: Float = 1.0f,
        val guidanceStart: Float = 0f,
        val guidanceEnd: Float = 1f,
    ) : ImageControl {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as ControlNet

            if (weight != other.weight) return false
            if (guidanceStart != other.guidanceStart) return false
            if (guidanceEnd != other.guidanceEnd) return false
            if (module != other.module) return false
            if (model != other.model) return false
            if (!image.contentEquals(other.image)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = weight.hashCode()
            result = 31 * result + guidanceStart.hashCode()
            result = 31 * result + guidanceEnd.hashCode()
            result = 31 * result + module.hashCode()
            result = 31 * result + model.hashCode()
            result = 31 * result + image.contentHashCode()
            return result
        }
    }

    /**
     * IP-Adapter 风格 / 主体迁移。
     *
     * @property image 参考图字节。
     * @property weight 控制权重，常见取值 `0.0..1.0`，默认 `1.0`。
     */
    data class IPAdapter(
        val image: ByteArray,
        val weight: Float = 1.0f,
    ) : ImageControl {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as IPAdapter

            if (weight != other.weight) return false
            if (!image.contentEquals(other.image)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = weight.hashCode()
            result = 31 * result + image.contentHashCode()
            return result
        }
    }
}
