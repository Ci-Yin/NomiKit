package ciyin.ui.foundation.effects

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

//import androidx.compose.ui.Modifier
//import androidx.compose.ui.input.pointer.PointerIcon
//import androidx.compose.ui.input.pointer.pointerHoverIcon
//import androidx.compose.ui.platform.testTag
//import ciyin.system.window.AwtWindowUtils.Companion.blankCursor
//
//actual fun Modifier.cursorVisibility(visible: Boolean): Modifier {
//    return if (visible) {
//        testTag(TAG_CURSOR_VISIBILITY_EFFECT_VISIBLE)
//    } else {
//        (blankCursor?.let {
//            pointerHoverIcon(PointerIcon(it))
//        } ?: this)
//            .testTag(TAG_CURSOR_VISIBILITY_EFFECT_INVISIBLE)
//    }
//}

actual fun Modifier.cursorVisibility(visible: Boolean): Modifier {
    return if (visible) {
        testTag(TAG_CURSOR_VISIBILITY_EFFECT_VISIBLE)
    } else {
        testTag(TAG_CURSOR_VISIBILITY_EFFECT_INVISIBLE)
    }
}