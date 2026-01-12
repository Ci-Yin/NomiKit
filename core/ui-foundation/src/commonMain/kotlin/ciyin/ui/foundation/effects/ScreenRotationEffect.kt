package ciyin.ui.foundation.effects

import androidx.compose.runtime.Composable

/**
 * 用于检测屏幕旋转（横竖屏切换）并调用提供的回调函数。
 *
 * @param onChange 当屏幕方向变化时触发的回调函数。参数 [isLandscape] 表示当前是否为横屏模式。
 */
@Composable
expect fun ScreenRotationEffect(onChange: (isLandscape: Boolean) -> Unit)