package ciyin.designsystem.fluent.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import ciyin.designsystem.component.list.NomiListItemRenderer
import ciyin.designsystem.component.list.NomiListItemSpec
import ciyin.designsystem.fluent.rememberFluentWarning
import ciyin.designsystem.runtime.findSpec
import io.github.composefluent.FluentTheme
import io.github.composefluent.LocalContentAlpha
import io.github.composefluent.LocalContentColor
import io.github.composefluent.LocalTextStyle
import io.github.composefluent.ProvideTextStyle
import io.github.composefluent.component.CardExpanderItem
import io.github.composefluent.component.ExpanderDefaults

/**
 * Fluent 原生列表项适配：可点击行使用 CardExpanderItem 的点击重载，静态行使用无点击重载。
 *
 * 两个重载共享同一卡片外观与最小行高；未携带 [FluentListItemSpec] 时不传 dropdown，因此不会出现
 * 展开箭头。上游只为 caption 提供原生文本样式，标题与尾部槽由本适配器建立原生文本环境。
 */
internal object FluentListItemRenderer : NomiListItemRenderer {
    /**
     * 渲染 Fluent 卡片式列表项。
     *
     * 未携带 [FluentListItemSpec] 时完全保留原先的两条原生路径；携带时只应用 spec 中非 null 的
     * 字段，其它设计系统的参数静默忽略；静态行无法承载 enabled、colors 与 interactionSource 时
     * 记录一次警告。
     *
     * @param modifier 应用于卡片行的修饰符。
     * @param onClick 点击回调；null 时使用无点击重载，卡片不响应交互。
     * @param leading 前导槽，进入原生 48dp 图标区域；null 时上游保留 16dp 占位。
     * @param title 标题槽，注入原生 bodyStrong 文本样式。
     * @param subtitle 副标题槽，进入原生 caption 槽；null 时传入空槽。
     * @param trailing 尾部槽，注入原生 caption 样式与 secondary 内容色；null 时传入空槽。
     * @param nativeSpec 列表项专属参数；只识别 [FluentListItemSpec]。
     */
    @Composable
    override fun ListItem(
        modifier: Modifier,
        onClick: (() -> Unit)?,
        leading: (@Composable () -> Unit)?,
        title: @Composable () -> Unit,
        subtitle: (@Composable () -> Unit)?,
        trailing: (@Composable () -> Unit)?,
        nativeSpec: NomiListItemSpec?,
    ) {
        val spec = nativeSpec.findSpec<FluentListItemSpec>()
        val heading: @Composable () -> Unit = {
            ProvideTextStyle(FluentTheme.typography.bodyStrong) {
                title()
            }
        }
        val trailingContent: @Composable () -> Unit = {
            val secondary = FluentTheme.colors.text.text.secondary
            CompositionLocalProvider(
                LocalContentColor provides secondary,
                LocalContentAlpha provides secondary.alpha,
                LocalTextStyle provides FluentTheme.typography.caption,
            ) {
                trailing?.invoke()
            }
        }
        if (onClick == null) {
            rememberFluentWarning(
                overridden = spec != null &&
                        (!spec.enabled || spec.colors != null || spec.interactionSource != null),
                key = spec,
                message = { "Fluent 静态列表行没有 enabled、colors 与 interactionSource 入口，已忽略对应专属参数。" },
            )
            CardExpanderItem(
                heading = heading,
                modifier = modifier,
                shape = spec?.shape ?: FluentTheme.shapes.control,
                icon = leading,
                caption = subtitle ?: {},
                trailing = trailingContent,
                dropdown = spec?.dropdown,
            )
        } else {
            CardExpanderItem(
                onClick = onClick,
                heading = heading,
                modifier = modifier,
                shape = spec?.shape ?: FluentTheme.shapes.control,
                enabled = spec?.enabled ?: true,
                colors = spec?.colors ?: ExpanderDefaults.cardExpanderItemColors(),
                interactionSource = spec?.interactionSource,
                icon = leading,
                caption = subtitle ?: {},
                trailing = trailingContent,
                dropdown = spec?.dropdown,
            )
        }
    }
}
