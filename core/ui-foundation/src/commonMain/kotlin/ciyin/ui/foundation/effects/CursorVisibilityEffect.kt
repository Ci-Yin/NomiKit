package ciyin.ui.foundation.effects


import androidx.compose.ui.Modifier

/**
 * 仅在 PC 有效, 当 [visible] 为 false 时, 隐藏光标
 */
expect fun Modifier.cursorVisibility(visible: Boolean = true): Modifier

const val TAG_CURSOR_VISIBILITY_EFFECT_VISIBLE = "CursorVisibilityEffect-visible"
const val TAG_CURSOR_VISIBILITY_EFFECT_INVISIBLE = "CursorVisibilityEffect-invisible"
