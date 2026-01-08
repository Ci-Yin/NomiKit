package ciyin.ui.foundation.unit

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/**
 *
 * kotlin文件作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2025/9/6 19:18
 * @version: 1.0
 */
@Composable
fun Int.toDp(): Dp {
    return toFloat().toDp()
}

@Composable
fun Float.toDp(): Dp {
    return (this / LocalDensity.current.density).dp
}

@Composable
fun Dp.toPx(): Float {
    return value * LocalDensity.current.density
}

@Composable
fun TextUnit.toDp(): Dp {
    return (value * LocalDensity.current.fontScale).dp
}

@Composable
fun Dp.toSp(): TextUnit {
    return (value / LocalDensity.current.fontScale).sp
}