package ciyin.designsystem.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import ciyin.designsystem.component.NomiDesignSystemComponents
import kotlin.jvm.JvmInline

/**
 * 设计系统稳定标识。
 *
 * @property value 标识文本，例如 nomi、material3 或 fluent；本类型不校验唯一性或格式。
 */
@Immutable
@JvmInline
value class NomiDesignSystemId(
    val value: String,
)

/**
 * 设计系统的原生主题与组件扩展契约，负责原生默认值与只读投影；无法承载的显式覆盖由实现自行降级。
 */
@Stable
interface NomiDesignSystem {
    /**
     * 设计系统实现的绑定命名空间。
     *
     * adapter 模块用扩展属性把自身实现挂到这里，核心因此不反向依赖任何 adapter。
     * 扩展属性必须导入对应模块的声明，IDE 会在 `NomiDesignSystem.` 后补全。
     */
    companion object

    /**
     * 稳定标识。
     */
    val id: NomiDesignSystemId

    /**
     * 首期组件族集合。
     */
    val components: NomiDesignSystemComponents

    /**
     * 先建立原生主题，再通过回调提供中立只读投影。
     *
     * 只应用请求中的显式增量，不能把另一设计系统的已解析默认值作为本系统默认。
     * 无法承载的显式覆盖由实现自行降级并保留原生默认，不重绘伪造支持；结构性输入冲突仍明确失败。
     *
     * @param request 外层主题边界解析的模式、实际深色状态与显式覆盖；不包含预先填充的默认令牌。
     * @param content 在原生主题环境内组合的子树，接收直接包含五组结果的运行时主题；
     * 核心发布时保留这些结果，但模式和显式覆盖来源仍以 request 为准，不能由投影默认值反推。
     */
    @Composable
    fun ProvideTheme(request: NomiThemeRequest, content: @Composable (ResolvedNomiTheme) -> Unit)
}

/**
 * 当前组合子树显式提供的设计系统；未提供时为 null，不做隐式回退。
 *
 * 应用根、测试与 Preview 必须通过 CompositionLocalProvider 或 NomiTheme(designSystem = ...) 指定实现；
 * 读取方使用 [requireNomiDesignSystem] 获得带指引的失败信息。
 */
val LocalNomiDesignSystem = staticCompositionLocalOf<NomiDesignSystem?> { null }

/**
 * 读取当前显式提供的设计系统；未提供时明确失败并提示提供方式。
 *
 * @return 当前组合子树提供的设计系统实例。
 * @throws IllegalStateException 任何祖先都未提供设计系统时抛出。
 */
@Composable
@ReadOnlyComposable
internal fun requireNomiDesignSystem(): NomiDesignSystem = LocalNomiDesignSystem.current
    ?: error("未提供 NomiDesignSystem：请在应用根、测试或 Preview 使用 CompositionLocalProvider(LocalNomiDesignSystem provides ...) 或 NomiTheme(designSystem = ...) 显式指定。")
