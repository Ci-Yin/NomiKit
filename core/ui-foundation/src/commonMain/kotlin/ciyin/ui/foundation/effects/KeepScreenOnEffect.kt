package ciyin.ui.foundation.effects

import androidx.compose.runtime.Composable

/**
 * 保持屏幕处于常亮状态的效果工具方法。
 *
 * 调用该方法后，屏幕将保持常亮，避免因系统默认设置而导致的屏幕自动熄灭。
 * 不同平台的具体实现可能有所不同。
 */
@Composable
expect fun KeepScreenOnEffect()