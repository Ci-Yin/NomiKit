package ciyin.designsystem.component.text

import ciyin.designsystem.runtime.NomiNativeSpec
import ciyin.designsystem.runtime.NomiSpecFamily

/**
 * 文本专属参数的族标记。
 *
 * 只作为 [NomiNativeSpec] 的类型参数使用；当前没有任何实现，属于预留插孔，
 * 用于将来透传中立排版参数未覆盖的原生文本能力。
 */
sealed interface NomiTextSpecFamily : NomiSpecFamily

/**
 * 文本专属参数。
 *
 * 中立 NomiText 已逐字段暴露原生 Text 的全部参数，因此本轮没有实现；预留该契约是为了让未来的
 * 原生专有能力无需修改公共签名即可接入。
 */
typealias NomiTextSpec = NomiNativeSpec<NomiTextSpecFamily>
