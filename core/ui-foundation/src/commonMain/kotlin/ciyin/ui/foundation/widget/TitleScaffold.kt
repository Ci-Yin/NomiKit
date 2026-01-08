package ciyin.ui.foundation.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


/**
 *
 * kotlin文件作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2025/9/2 0:20
 * @version: 1.0
 */

/**
 * 一个用于展示卡片布局的通用模板组件。卡片顶部展示一个标题，主体部分可以包含自定义内容。
 *
 * @param title 一个可组合项，用于设置卡片顶部的标题部分。标题部分通常包含文字或图标。
 * @param modifier [Modifier]，用于配置组件外层的修饰，例如大小、背景等。默认为[Modifier]，也可通过`Modifier`链式调用对其进行扩展。
 * @param content 一个可组合项，用于设置卡片主体部分的内容。主要展示与卡片功能相关的详细信息或子组件。
 */
@Composable
fun TitleScaffold(
    title: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(top = 12.dp, bottom = 4.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleMedium) {
                title()
            }
        }
        content()
    }
}