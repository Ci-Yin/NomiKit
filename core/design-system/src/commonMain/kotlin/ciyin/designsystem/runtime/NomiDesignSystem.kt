package ciyin.designsystem.runtime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import ciyin.designsystem.component.NomiDesignSystemComponents
import ciyin.designsystem.theme.NomiTheme
import kotlin.jvm.JvmInline

/**
 * 设计系统稳定标识。
 *
 * @property value 标识文本，内置实现使用 nomi，adapter 模块各自使用自身标识；本类型不校验唯一性或格式。
 */
@Immutable
@JvmInline
value class NomiDesignSystemId(
    val value: String,
)

/**
 * 设计系统契约：把中立 [NomiTheme] 映射为自身原生主题。
 * 中立主题由主题入口发布，本接口不再负责回写或投影。
 */
@Stable
interface NomiDesignSystem {
    /**
     * 设计系统实现的绑定命名空间。
     *
     * adapter 模块用扩展函数把自身工厂挂到这里，核心因此不反向依赖任何 adapter。
     */
    companion object

    /**
     * 稳定标识。
     */
    val id: NomiDesignSystemId

    /**
     * 组件族集合。
     */
    val components: NomiDesignSystemComponents

    /**
     * 安装原生主题；中立主题与内容 slot 由主题入口负责发布。
     *
     * @param theme 本层显式提供的中立主题，仅作为映射来源。
     * @param content 在原生主题环境中组合的子树。
     */
    @Composable
    fun ProvideTheme(theme: NomiTheme, content: @Composable () -> Unit)
}

/**
 * 当前组合子树显式提供的设计系统；未提供时为 null，不做隐式回退。
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
