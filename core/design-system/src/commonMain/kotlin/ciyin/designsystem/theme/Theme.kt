package ciyin.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import ciyin.designsystem.runtime.NomiDesignSystem
import ciyin.designsystem.runtime.NomiThemeRequest
import ciyin.designsystem.runtime.LocalNomiContentColor
import ciyin.designsystem.runtime.LocalNomiDesignSystem
import ciyin.designsystem.runtime.LocalNomiTextStyle
import ciyin.designsystem.runtime.LocalResolvedNomiTheme
import ciyin.designsystem.runtime.requireNomiDesignSystem

/**
 * 设计系统主题根入口。未指定的颜色按当前设计系统与模式重新解析；其它组只继承父层明确设置。
 *
 * 本层显式输入只来自 [overrides]；可空字段的 null 表示未指定，不表示清除父级。
 * 根边界重置内容 slot；需要继承显式颜色与 slot 的局部调整使用 [NomiThemeOverride]。
 * 非空对象表示完整主题组，不推断 copy 修改的字段；退出分支后恢复父级环境。
 * 适配器无法承载的完整组由实现自行降级并记录警告，主题入口不按能力拦截。
 *
 * @param darkMode 明暗请求，默认继承当前请求模式；System 在本组合边界读取系统明暗。
 * @param designSystem 建立原生主题的实现；默认读取祖先显式提供的设计系统，无提供者时抛出带指引的 [IllegalStateException]。
 * @param overrides 本层显式增量；未指定的颜色不继承父层色板，其它组继承父层明确设置。
 * @param content 在新原生主题与中立投影下组合的内容，进入时清空父级显式内容颜色和文本样式。
 * @throws IllegalStateException [designSystem] 未显式提供且祖先也没有提供设计系统时抛出。
 */
@Composable
fun NomiTheme(
    darkMode: DarkMode = NomiTheme.darkMode,
    designSystem: NomiDesignSystem = requireNomiDesignSystem(),
    overrides: NomiThemeOverrides = NomiThemeOverrides(),
    content: @Composable () -> Unit,
) {
    val parent = LocalResolvedNomiTheme.current.overrides
    val inherited = NomiThemeOverrides(
        typography = parent.typography,
        shapes = parent.shapes,
        spacings = parent.spacings,
        sizes = parent.sizes,
    )
    val request = NomiThemeRequest(
        requestedMode = darkMode,
        isDark = resolveDarkMode(darkMode),
        overrides = inherited.merge(overrides),
    )
    CompositionLocalProvider(
        LocalNomiContentColor provides Color.Unspecified,
        LocalNomiTextStyle provides TextStyle.Default,
    ) {
        ProvideResolvedTheme(designSystem = designSystem, request = request, content = content)
    }
}

/**
 * 局部主题继承全部显式组及内容 slot，只叠加本层 [overrides]，不携带父级原生默认投影。
 *
 * null 字段表示本层未指定而非清除父值；非空对象作为完整组替换，不推断其中哪些字段发生变化。
 * 子层完整色板替换继承的父层显式色板。
 * 切换设计系统仍只继承明确输入；离开此组合分支后恢复父主题与 slot。
 * 父级也没有显式值的组由目标设计系统解析默认值；适配器无法表达时降级并记录警告。
 *
 * @param designSystem 用于建立本分支原生主题的实现；默认读取祖先显式提供的设计系统，无提供者时抛出带指引的 [IllegalStateException]。
 * @param darkMode 本层明暗请求；null 继承父请求和已解析明暗，非 null 在本边界重新解析。
 * @param overrides 本层显式增量；未指定字段继续继承父显式组。
 * @param content 在目标原生主题和中立投影下组合的内容，保留父级明确的内容颜色与文本样式。
 * @throws IllegalStateException [designSystem] 未显式提供且祖先也没有提供设计系统时抛出。
 */
@Composable
fun NomiThemeOverride(
    designSystem: NomiDesignSystem = requireNomiDesignSystem(),
    darkMode: DarkMode? = null,
    overrides: NomiThemeOverrides = NomiThemeOverrides(),
    content: @Composable () -> Unit,
) {
    val parent = LocalResolvedNomiTheme.current
    val request = NomiThemeRequest(
        requestedMode = darkMode ?: parent.requestedMode,
        isDark = if (darkMode == null) parent.isDark else resolveDarkMode(darkMode),
        overrides = parent.overrides.merge(overrides),
    )
    ProvideResolvedTheme(designSystem = designSystem, request = request, content = content)
}

/**
 * 先建立原生环境，再发布五组结果；模式与显式覆盖来源以本次请求为准，避免默认值循环回填。
 *
 * @param designSystem 提供原生主题环境及中立投影的实现。
 * @param request 已完成继承合并和明暗解析的显式主题请求。
 * @param content 在适配器回调的原生环境中组合的内容，可读取对应中立投影和显式来源。
 */
@Composable
private fun ProvideResolvedTheme(
    designSystem: NomiDesignSystem,
    request: NomiThemeRequest,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalNomiDesignSystem provides designSystem) {
        designSystem.ProvideTheme(request = request) { resolved ->
            val current = resolved.copy(
                requestedMode = request.requestedMode,
                isDark = request.isDark,
                overrides = request.overrides,
            )
            CompositionLocalProvider(LocalResolvedNomiTheme provides current, content = content)
        }
    }
}

/**
 * 在组合边界解析系统模式，adapter 不再读取系统状态。
 *
 * @param darkMode 请求模式；Light 和 Dark 使用固定明暗，System 读取当前系统主题。
 * @return 本次组合是否应使用深色主题。
 */
@Composable
private fun resolveDarkMode(darkMode: DarkMode): Boolean = when (darkMode) {
    DarkMode.Light -> false
    DarkMode.Dark -> true
    DarkMode.System -> isSystemInDarkTheme()
}

/**
 * 当前原生主题的中立只读投影，不作为下一个设计系统的默认输入。
 *
 * 各属性读取当前组合分支的解析上下文；没有主题包装时使用固定浅色 Nomi 默认投影，
 * 请求模式仍为 [DarkMode.System]，不会由这些 getter 自行查询系统明暗。
 */
object NomiTheme {
    /**
     * 当前请求模式；System 保留跟随系统的请求含义，不等于当前实际深浅色结果。
     */
    val darkMode: DarkMode
        @Composable @ReadOnlyComposable get() = LocalResolvedNomiTheme.current.requestedMode

    /**
     * 当前颜色投影。
     */
    val colorScheme: NomiColorScheme
        @Composable @ReadOnlyComposable get() = LocalResolvedNomiTheme.current.colorScheme

    /**
     * 当前排版投影。
     */
    val typography: NomiTypography
        @Composable @ReadOnlyComposable get() = LocalResolvedNomiTheme.current.typography

    /**
     * 当前形状投影。
     */
    val shapes: NomiShapes
        @Composable @ReadOnlyComposable get() = LocalResolvedNomiTheme.current.shapes

    /**
     * 页面间距令牌。
     */
    val spacings: Spacings
        @Composable @ReadOnlyComposable get() = LocalResolvedNomiTheme.current.spacings

    /**
     * 页面尺寸令牌。
     */
    val sizes: NomiSizes
        @Composable @ReadOnlyComposable get() = LocalResolvedNomiTheme.current.sizes
}
