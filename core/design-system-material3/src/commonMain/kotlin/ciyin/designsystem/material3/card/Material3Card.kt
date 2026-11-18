package ciyin.designsystem.material3.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import ciyin.designsystem.component.NomiBorder
import ciyin.designsystem.component.surface.NomiSurfaceColors
import ciyin.designsystem.material3.surface.toMaterialBorder
import ciyin.designsystem.runtime.ProvideNomiContentStyle

/**
 * 使用原生 Card 的颜色、圆角、阴影与 Column slot，只有显式 padding 才创建内层布局。
 *
 * @param modifier 原生卡片外层修饰符。
 * @param shape 显式卡片形状；null 保留 CardDefaults.shape。
 * @param colors 逐通道颜色覆盖；Unspecified 保留原生默认，Transparent 是有效覆盖。
 * @param border Default 与 None 均不加描边，Stroke 使用给定描边。
 * @param contentPadding null 直接使用原生 Column slot；非 null 新建带内边距的内层 Column。
 * @param spec 已取用的卡片专属参数；null 保留原生 elevation。
 * @param content ColumnScope 内容，继承原生内容色，仅显式内容色写入中立 slot。
 */
@Composable
internal fun Material3Card(
    modifier: Modifier,
    shape: Shape?,
    colors: NomiSurfaceColors,
    border: NomiBorder,
    contentPadding: PaddingValues?,
    spec: Material3CardSpec?,
    content: @Composable ColumnScope.() -> Unit,
) {
    val nativeColors = CardDefaults.cardColors().copy(
        containerColor = colors.containerColor,
        contentColor = colors.contentColor,
    )
    Card(
        modifier = modifier,
        shape = shape ?: CardDefaults.shape,
        colors = nativeColors,
        elevation = spec?.elevation ?: CardDefaults.cardElevation(),
        border = border.toMaterialBorder(),
    ) {
        val scope = this
        ProvideNomiContentStyle(
            color = colors.contentColor,
            textStyle = TextStyle.Default,
        ) {
            if (contentPadding == null) {
                content(scope)
            } else {
                Column(modifier = Modifier.padding(contentPadding), content = content)
            }
        }
    }
}
