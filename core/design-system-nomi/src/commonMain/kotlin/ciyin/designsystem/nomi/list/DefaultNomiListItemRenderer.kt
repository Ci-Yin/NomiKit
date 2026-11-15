package ciyin.designsystem.nomi.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import ciyin.designsystem.component.list.NomiListItemRenderer
import ciyin.designsystem.runtime.ProvideNomiContentStyle
import ciyin.designsystem.theme.NomiTheme

/**
 * Nomi 原生列表项实现：透明行，标题、副标题与尾部内容分槽建立令牌文本环境。
 *
 * 行内边距、槽间距与点击圆角全部读取 [NomiTheme] 令牌，不固定行高；点击行使用 Nomi 圆角与
 * 环境指示器，静态行不附加点击修饰符。
 */
internal object DefaultNomiListItemRenderer : NomiListItemRenderer {
    /**
     * 渲染 Nomi 列表项。
     *
     * @param modifier 应用于列表行根节点的修饰符。
     * @param onClick 点击回调；null 时保持静态行。
     * @param leading 前导槽；null 表示不占位，槽内图标尺寸由调用方约束。
     * @param title 标题槽，使用 titleSmall 加 SemiBold 的 Nomi 原生标题样式与 textPrimary。
     * @param subtitle 副标题槽；null 表示不占位，使用 bodySmall 与 textSecondary。
     * @param trailing 尾部槽；null 表示不占位，使用 textSecondary 内容色。
     */
    @Composable
    override fun ListItem(
        modifier: Modifier,
        onClick: (() -> Unit)?,
        leading: (@Composable () -> Unit)?,
        title: @Composable () -> Unit,
        subtitle: (@Composable () -> Unit)?,
        trailing: (@Composable () -> Unit)?,
    ) {
        val colorScheme = NomiTheme.colorScheme
        val spacings = NomiTheme.spacings
        val titleTextStyle = NomiTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
        val subtitleTextStyle = NomiTheme.typography.bodySmall
        Row(
            modifier = modifier
                .fillMaxWidth()
                .then(
                    if (onClick == null) {
                        Modifier
                    } else {
                        Modifier.clip(NomiTheme.shapes.medium).clickable(onClick = onClick)
                    }
                )
                .padding(horizontal = spacings.large, vertical = spacings.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacings.medium),
        ) {
            if (leading != null) {
                ProvideNomiContentStyle(
                    color = colorScheme.textPrimary,
                    textStyle = TextStyle.Default,
                ) {
                    leading()
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(spacings.tiny),
            ) {
                ProvideNomiContentStyle(
                    color = colorScheme.textPrimary,
                    textStyle = titleTextStyle,
                ) {
                    title()
                }
                if (subtitle != null) {
                    ProvideNomiContentStyle(
                        color = colorScheme.textSecondary,
                        textStyle = subtitleTextStyle,
                    ) {
                        subtitle()
                    }
                }
            }
            if (trailing != null) {
                ProvideNomiContentStyle(
                    color = colorScheme.textSecondary,
                    textStyle = TextStyle.Default,
                ) {
                    trailing()
                }
            }
        }
    }
}
