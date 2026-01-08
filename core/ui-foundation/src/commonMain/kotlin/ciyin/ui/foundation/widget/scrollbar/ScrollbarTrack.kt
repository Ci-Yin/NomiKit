package ciyin.ui.foundation.widget.scrollbar

import androidx.compose.runtime.Immutable
import androidx.compose.ui.util.packFloats
import kotlin.jvm.JvmInline

@Immutable
@JvmInline
value class ScrollbarTrack(
// // 轨道核心属性的类定义
    val packedValue: Long,
) {
    constructor(
        max: Float,
        min: Float,
    ) : this(packFloats(max, min))
}