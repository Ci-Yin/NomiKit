/*
 * Copyright lt 2023
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ciyin.ui.foundation.layout.refresh.content.top

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ciyin.ui.foundation.layout.refresh.RefreshContentStateEnum
import ciyin.ui.foundation.layout.refresh.RefreshLayoutState
import ciyin.ui.foundation.layout.refresh.Strings
import kotlin.math.abs

/**
 * creator: lt  2022/9/18  lt.dygzs@qq.com
 * effect : 下拉刷新的刷新组件
 *          Refresh component for pull down refresh
 * warning:
 */
@Composable
fun RefreshLayoutState.PullToRefreshContent() {
    val refreshContentState by remember {
        getRefreshContentState()
    }
    Row(
        Modifier
            .fillMaxWidth()
            .height(35.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
    ) {
        when (refreshContentState) {
            RefreshContentStateEnum.Stop -> {
                //no image
            }

            RefreshContentStateEnum.Refreshing -> {
                //循环旋转动画
                val infiniteTransition = rememberInfiniteTransition()
                val rotate by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
                Image(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "",
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotate)
                )
                Spacer(Modifier.width(10.dp))
            }

            RefreshContentStateEnum.Dragging -> {
                //旋转动画
                val isCannotRefresh =
                    abs(getRefreshContentOffset()) < getRefreshContentThreshold()
                val rotate by animateFloatAsState(targetValue = if (isCannotRefresh) 0f else 180f)
                Image(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "",
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotate)
                )
                Spacer(Modifier.width(10.dp))
            }
        }
        Text(
            text = when (refreshContentState) {
                RefreshContentStateEnum.Stop -> Strings.getRefreshCompleteString()
                RefreshContentStateEnum.Refreshing -> Strings.getRefreshingString()
                RefreshContentStateEnum.Dragging -> {
                    if (abs(getRefreshContentOffset()) < getRefreshContentThreshold()) {
                        Strings.getDropDownToRefreshString()
                    } else {
                        Strings.getReleaseRefreshNowString()
                    }
                }
            },
            fontSize = 14.sp,
            color = Color.Red,
        )
    }
}