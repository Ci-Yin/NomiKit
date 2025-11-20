package com.ciyin.app.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import ciyin.foundation.thenIf


/**
 *
 * kotlin文件作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2024/10/21 下午6:05
 * @version: 1.0
 */

/**
 * 自定义卡片
 *
 * @param modifier
 * @param contentPaddings
 * @param content
 */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    shape: Shape = CardDefaults.outlinedShape,
    colors: CardColors = CardDefaults.outlinedCardColors().copy(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ),
    contentPaddings: PaddingValues = PaddingValues(15.dp),
    elevation: CardElevation = CardDefaults.outlinedCardElevation(),
    border: BorderStroke = CardDefaults.outlinedCardBorder(),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) = OutlinedCard(
    modifier = modifier.thenIf(onClick != null) {
        clickable { onClick!!.invoke() }
    },
    colors = colors,
    shape = shape,
    elevation = elevation,
    border = border,
    content = {
        Column(
            modifier = Modifier.padding(contentPaddings),
            content = content,
        )
    },
)