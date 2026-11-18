package ciyin.designsystem.component.card

import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily

/**
 * 卡片专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；与表面族的标记互不兼容。
 */
sealed interface NomiCardSpecFamily : NomiSpecFamily

/**
 * 卡片专属参数。
 *
 * 只承载中立参数没有入口的原生卡片能力（例如 elevation 层级），具体实现由 adapter 模块定义。
 */
typealias NomiCardSpec = NomiNativeSpec<NomiCardSpecFamily>
