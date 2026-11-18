package ciyin.designsystem.component.button

import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily

/**
 * 按钮专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；其它组件族的标记与它互不兼容。
 */
sealed interface NomiButtonSpecFamily : NomiSpecFamily

/**
 * 按钮专属参数。
 *
 * 只承载中立参数没有入口的原生按钮能力（阴影、描边、五态色板等），具体实现由 adapter 模块定义，
 * 多个 spec 用 `+` 组合后一次传入。
 */
typealias NomiButtonSpec = NomiNativeSpec<NomiButtonSpecFamily>
