package ciyin.ui.foundation.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ciyin.ui.foundation.extension.clickableNoRipple
import kotlin.math.roundToInt

/**
 * @Author: leavesCZY
 * @Desc:
 * @Github：https://github.com/leavesCZY
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BottomSheetDialog(
    modifier: Modifier = Modifier,
    visible: Boolean,
    shape: Shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    cancelable: Boolean = true,
    canceledOnTouchOutside: Boolean = true,
    onDismissRequest: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(durationMillis = 400, easing = LinearEasing)),
            exit = fadeOut(animationSpec = tween(durationMillis = 400, easing = LinearEasing))
        ) {
            BackHandler(enabled = visible) {
                if (cancelable) {
                    onDismissRequest()
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x4d373737))
                    .clickableNoRipple {
                        if (canceledOnTouchOutside) {
                            onDismissRequest()
                        }
                    }
            )
        }
        InnerDialog(
            visible = visible,
            cancelable = cancelable,
            shape = shape,
            onDismissRequest = onDismissRequest,
            content = content
        )
    }
}

@Composable
private fun BoxScope.InnerDialog(
    visible: Boolean,
    cancelable: Boolean,
    shape: Shape,
    onDismissRequest: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    val offsetYAnimate by animateFloatAsState(targetValue = offsetY, label = "")
    var bottomSheetHeight by remember { mutableFloatStateOf(0f) }
    AnimatedVisibility(
        modifier = Modifier
            .align(alignment = Alignment.BottomCenter)
            .clickableNoRipple {

            }
            .onGloballyPositioned {
                bottomSheetHeight = it.size.height.toFloat()
            }
            .offset(offset = {
                IntOffset(0, offsetYAnimate.roundToInt())
            })
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState(
                    onDelta = {
                        offsetY = (offsetY + it.toInt()).coerceAtLeast(0f)
                    }
                ),
                onDragStarted = {

                },
                onDragStopped = {
                    if (cancelable && offsetY > bottomSheetHeight / 2) {
                        onDismissRequest()
                    } else {
                        offsetY = 0f
                    }
                }
            ),
        visible = visible,
        enter = slideInVertically(
            animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
            initialOffsetY = {
                2 * it
            }
        ),
        exit = slideOutVertically(
            animationSpec = tween(durationMillis = 400, easing = LinearOutSlowInEasing),
            targetOffsetY = {
                it
            }
        )
    ) {
        DisposableEffect(key1 = null) {
            onDispose {
                offsetY = 0f
            }
        }

        Box(
            Modifier.clip(shape),
            content = content
        )
    }
}

@Composable
@ExperimentalMaterial3Api
fun ModalBottomSheet(
    visible: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = 0.dp,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.windowInsets },
    properties: ModalBottomSheetProperties = ModalBottomSheetDefaults.properties,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (visible.not()) return
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        sheetMaxWidth = sheetMaxWidth,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        scrimColor = scrimColor,
        dragHandle = dragHandle,
        contentWindowInsets = contentWindowInsets,
        properties = properties,
        content = content
    )
}
