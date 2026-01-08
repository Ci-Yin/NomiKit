package ciyin.ui.foundation.window

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties


/**
 *
 * kotlin文件作用描述
 *
 * @author 次音(CiYin) QQ:2964221430
 * @github <a href="https://github.com/Ci-Yin">CiYin</a>
 * @since 2024/8/28 上午2:59
 * @version: 1.0
 */

@Composable
fun FloatWindow(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopStart,
    offset: IntOffset = IntOffset(0, 0),
    buttonContent: @Composable (() -> Unit),
    content: @Composable () -> Unit,
) {
    buttonContent()
    if (expanded) {
        Popup(
            onDismissRequest = onDismissRequest,
            alignment = alignment,
            offset = offset,
            properties = PopupProperties(focusable = true),
            content = {
                AnimatedVisibility(visible = expanded) {
                    Surface(
                        modifier.sizeIn(maxWidth = 400.dp, maxHeight = 700.dp),
                        shadowElevation = 4.dp,
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Box {
                            content()
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun FloatWindow2(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    scrollState: ScrollState = rememberScrollState(),
    properties: PopupProperties = PopupProperties(focusable = true),
    shape: Shape = MenuDefaults.shape,
    containerColor: Color = MenuDefaults.containerColor,
    tonalElevation: Dp = MenuDefaults.TonalElevation,
    shadowElevation: Dp = MenuDefaults.ShadowElevation,
    border: BorderStroke? = null,
    buttonContent: @Composable (ColumnScope.() -> Unit),
    content: @Composable (ColumnScope.() -> Unit),
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        offset = offset,
        scrollState = scrollState,
        properties = properties,
        shape = shape,
        containerColor = containerColor,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        border = border
    ) {
        buttonContent()
        content()
    }
}

internal class AlignmentOffsetPositionProvider(
    val alignment: Alignment,
    val offset: IntOffset
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        // TODO: Decide which is the best way to round to result without reimplementing Alignment.align

        val anchorAlignmentPoint = alignment.align(
            IntSize.Zero,
            anchorBounds.size,
            layoutDirection
        )
        // Note the negative sign. Popup alignment point contributes negative offset.
        val popupAlignmentPoint = -alignment.align(
            IntSize.Zero,
            popupContentSize,
            layoutDirection
        )
        val resolvedUserOffset = IntOffset(
            offset.x * (if (layoutDirection == LayoutDirection.Ltr) 1 else -1),
            offset.y
        )

        return anchorBounds.topLeft +
                anchorAlignmentPoint +
                popupAlignmentPoint +
                resolvedUserOffset
    }
}
