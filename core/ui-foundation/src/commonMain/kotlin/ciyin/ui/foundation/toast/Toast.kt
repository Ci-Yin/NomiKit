package ciyin.ui.foundation.toast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ciyin.ui.foundation.extension.thenIf
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun Toast(
    message: String,
    showToast: Boolean,
    afterToastShown: (Boolean) -> Unit,
    toastDelay: ToastDelay = ToastDelay.ShortDelay,
    shape: Shape = RoundedCornerShape(15.dp),
    background: Color = MaterialTheme.colorScheme.background,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    alignment: Alignment = Alignment.BottomCenter,
    fillMaxWidth: Boolean = false,
    leadingIconSpace: Dp = 0.dp,
    trailingIconSpace: Dp = 0.dp,
    disableAutoHide: Boolean = false,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    enter: EnterTransition = fadeIn(animationSpec = tween(500, easing = LinearEasing)),
    exit: ExitTransition = fadeOut(animationSpec = tween(500, easing = LinearEasing)),
) {

    LaunchedEffect(key1 = showToast) {
        launch {
            delay(toastDelay.duration)
            if (disableAutoHide.not()) {
                afterToastShown(false)
            }
        }
    }

    AnimatedVisibility(visible = showToast, enter = enter, exit = exit) {


        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(vertical = 100.dp, horizontal = 50.dp),
            contentAlignment = alignment
        ) {
            Surface(
                shape = shape,
                //tonalElevation = 5.dp,
                shadowElevation = 15.dp
            ) {
                Row(
                    modifier = Modifier
                        .thenIf(fillMaxWidth) {
                            fillMaxWidth()
                        }
                        .clip(shape)
                        .background(background)
                        .padding(13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (disableAutoHide) Arrangement.SpaceBetween else Arrangement.Center
                ) {

                    val textModifier = if (fillMaxWidth) {
                        Modifier.weight(1f)
                    } else {
                        Modifier.wrapContentWidth()
                    }

                    if (leadingContent != null) {
                        leadingContent.invoke()
                        Spacer(modifier = Modifier.padding(leadingIconSpace))
                    }

                    Text(
                        text = message,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Start,
                        color = textColor,
                        modifier = textModifier,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (trailingContent != null) {
                        Spacer(modifier = Modifier.padding(trailingIconSpace))
                        trailingContent.invoke()
                    }
                }
            }
        }

    }
}

@Preview
@Composable
fun ToastPreview() {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Toast(message = "Toast", showToast = true, {})
    }
    Toast(message = "Toast", showToast = true, {})
}

sealed class ToastDelay(val duration: Long) {
    data object ShortDelay : ToastDelay(2000L)
    data object LongDelay : ToastDelay(3500L)
}