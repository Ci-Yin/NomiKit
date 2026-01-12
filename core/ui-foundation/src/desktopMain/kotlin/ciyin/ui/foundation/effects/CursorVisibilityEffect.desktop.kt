package ciyin.ui.foundation.effects

import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.testTag
import com.yy.myuko.core.system.window.AwtWindowUtils.Companion.blankCursor

actual fun androidx.compose.ui.Modifier.cursorVisibility(visible: Boolean): androidx.compose.ui.Modifier {
    return if (visible) {
        testTag(TAG_CURSOR_VISIBILITY_EFFECT_VISIBLE)
    } else {
        (blankCursor?.let {
            pointerHoverIcon(PointerIcon(it))
        } ?: this)
            .testTag(TAG_CURSOR_VISIBILITY_EFFECT_INVISIBLE)
    }
}