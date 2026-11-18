package ciyin.designsystem.component.scaffold

import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily

/**
 * 页面脚手架专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；其它组件族的标记与它互不兼容。
 */
sealed interface NomiScaffoldSpecFamily : NomiSpecFamily

/**
 * 页面脚手架专属参数。
 *
 * 只承载中立参数没有入口的原生外壳能力（提示条宿主、悬浮按钮、内容窗口内边距），
 * 具体实现由 adapter 模块定义。
 */
typealias NomiScaffoldSpec = NomiNativeSpec<NomiScaffoldSpecFamily>
