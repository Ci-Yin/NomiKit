package ciyin.designsystem.component.navigation

import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily

/**
 * 导航项专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；当前没有任何实现，属于预留插孔，
 * 用于将来透传各布局专属的原生导航项能力。
 */
sealed interface NomiNavigationItemSpecFamily : NomiSpecFamily

/**
 * 导航项专属参数。
 *
 * 三种布局的原生导航项颜色类型互不相同，单一 spec 只能写成联合体，因此本轮没有实现；
 * 预留该契约是为了让未来的布局专属能力无需修改公共签名即可接入。
 */
typealias NomiNavigationItemSpec = NomiNativeSpec<NomiNavigationItemSpecFamily>
