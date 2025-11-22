package com.ciyin.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ciyin.app.ui.component.TabRowDefaults2.tabIndicatorOffset


/**
 *
 * kotlin文件作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/10/18 下午10:30
 */


@Composable
fun <V> RpaTabRow2(
    dataList: List<V>,
    selectedTabIndex: Int,
    onClick: (Int) -> Unit,
    title: (V) -> String,
    modifier: Modifier = Modifier,
) = RpaTabRow(
    modifier = modifier,
    selectedTabIndex = selectedTabIndex,
) {
    for ((index, data) in dataList.withIndex()) {
        RpaTab(
            title = title(data),
            selected = selectedTabIndex == index,
            onClick = { onClick(index) }
        )
    }
}

@Composable
fun RpaTabRow(
    selectedTabIndex: Int,
    modifier: Modifier = Modifier,
    tabs: @Composable () -> Unit,
) = ScrollableTabRow(
    modifier = modifier,
    selectedTabIndex = selectedTabIndex,
    contentColor = MaterialTheme.colorScheme.onBackground,
    containerColor = Color.Transparent,
    minTabWidth = 55.dp,
    edgePadding = 0.dp,
    indicator = { tabPositions ->
        Box(
            modifier = Modifier
                .tabIndicatorOffset(tabPositions[selectedTabIndex])
                .clip(CircleShape)
                .background(Color(0xFFFBBA51))
        )
    },
    divider = {},
    tabs = tabs,
)

@Composable
fun RpaTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10))
            .clickable(onClick = onClick)
            .padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier.alpha(0f),
            text = title,
            maxLines = 1,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            ),
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = title,
            maxLines = 1,
            textAlign = TextAlign.Center,
            style = if (selected) {
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            } else {
                MaterialTheme.typography.bodyLarge
            },
            overflow = TextOverflow.Ellipsis,
        )
    }

}