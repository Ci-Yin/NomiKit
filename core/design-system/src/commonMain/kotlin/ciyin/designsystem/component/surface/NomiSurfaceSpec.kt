package ciyin.designsystem.component.surface

import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily

/**
 * 表面专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；与卡片族的标记互不兼容。
 */
sealed interface NomiSurfaceSpecFamily : NomiSpecFamily

/**
 * 表面专属参数。
 *
 * 只承载中立参数没有入口的原生表面能力（色调阴影、投影层级等），具体实现由 adapter 模块定义。
 */
typealias NomiSurfaceSpec = NomiNativeSpec<NomiSurfaceSpecFamily>
