package ciyin.designsystem.component.navigation

import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily

/**
 * 导航套件专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；与导航项族的标记互不兼容。
 */
sealed interface NomiNavigationSpecFamily : NomiSpecFamily

/**
 * 导航套件专属参数。
 *
 * 只承载中立参数没有入口的原生外壳能力（色调阴影、窗口内边距、侧栏头等），
 * 具体实现由 adapter 模块定义；与具体布局无关的字段由实现自行决定是否生效。
 */
typealias NomiNavigationSpec = NomiNativeSpec<NomiNavigationSpecFamily>
