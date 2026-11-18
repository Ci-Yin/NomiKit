package ciyin.designsystem.runtime

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

/**
 * 设计系统专属参数的族标记。
 *
 * 每个组件族一个，只作为 [NomiNativeSpec] 的类型参数使用，不参与运行时判断；不同族的标记互不兼容，
 * 因此把某族的 spec 传给别的组件、或跨族组合都会在编译期失败。
 */
@Stable
interface NomiSpecFamily

/**
 * 某个组件族的设计系统专属参数。
 *
 * 具体子类只能由 adapter 模块定义；核心既不引用原生类型，也不解析参数内容。当前设计系统按类型
 * 取用自己认识的那一个，其余一律静默忽略，因此携带其它设计系统参数的共享代码不需要按设计系统
 * 分支，也不会在运行期报错或刷日志。
 *
 * 组合由成员运算符 [plus] 完成，结果仍是本族契约，可以直接传给对应组件的 nativeSpec 参数。
 * 实现必须是 [Immutable]、只持有原生参数值，并且每个字段的默认值等于原生默认值：未指定的字段
 * 必须回落，不能把空参数变成一次改写原生外观的覆盖。
 *
 * @param F 族标记；决定该参数可以传给哪个组件族。
 */
@Stable
interface NomiNativeSpec<F : NomiSpecFamily> {
    /**
     * 与同族参数组合成新的集合，不修改任何已有实例。
     *
     * 两侧都已是 [NomiNativeSpecs] 时展平为单层；不去重、不排序，同类型以先声明者为准。
     *
     * @param other 要追加的同族参数，可以是另一个 [NomiNativeSpecs]。
     * @return 包含两侧全部成员的新集合。
     */
    operator fun plus(other: NomiNativeSpec<F>): NomiNativeSpecs<F> {
        @Suppress("UNCHECKED_CAST")
        val left = (this as? NomiNativeSpecs<F>)?.specs ?: listOf(this)

        @Suppress("UNCHECKED_CAST")
        val right = (other as? NomiNativeSpecs<F>)?.specs ?: listOf(other)
        return NomiNativeSpecs(left + right)
    }
}

/**
 * 同一族多个专属参数的不可变集合。
 *
 * 自身仍满足该族契约，因此可以直接作为组件的 nativeSpec 参数；由 [NomiNativeSpec.plus] 组合产生。
 *
 * @property specs 按声明顺序保存的专属参数。
 */
@Immutable
data class NomiNativeSpecs<F : NomiSpecFamily>(
    val specs: List<NomiNativeSpec<F>>,
) : NomiNativeSpec<F>

/**
 * 按具体类型取用专属参数。
 *
 * @receiver 调用方传给组件的族契约参数；null 表示未指定。
 * @return 第一个类型匹配的 spec；未携带该类型时返回 null，调用方应回落到原生默认。
 */
inline fun <reified T : NomiNativeSpec<*>> NomiNativeSpec<*>?.findSpec(): T? = when (this) {
    is T -> this
    is NomiNativeSpecs<*> -> specs.firstOrNull { it is T } as? T
    else -> null
}
