@file:OptIn(kotlin.experimental.ExperimentalTypeInference::class)

package ciyin.video.player.ui.internal

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/** 仅保留最近一次协程任务的轻量执行器。 */
internal class LatestTask(
    /** 启动替换任务的协程作用域。 */
    private val scope: CoroutineScope,
) {
    /** 当前仍在运行的任务。 */
    private var job: Job? = null

    /** 取消前一个任务并启动新任务。 */
    fun launch(
        context: CoroutineContext = EmptyCoroutineContext,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit,
    ): Job {
        job?.cancel()
        return scope.launch(context, start, block).also { job = it }
    }

    /** 取消当前任务。 */
    fun cancel() {
        job?.cancel()
    }
}

/** 在组合生命周期内记住一个仅执行最新任务的执行器。 */
@Composable
internal fun rememberLatestTask(): LatestTask {
    val scope = rememberCoroutineScope()
    return remember(scope) { LatestTask(scope) }
}

/** 将整数左侧补齐到指定长度。 */
internal fun Int.fixToString(length: Int, prefix: Char = '0'): String =
    toString().padStart(length, prefix)

/** 将长整数左侧补齐到指定长度。 */
internal fun Long.fixToString(length: Int, prefix: Char = '0'): String =
    toString().padStart(length, prefix)

/** 按条件应用修饰符。 */
@OptIn(ExperimentalContracts::class)
@OverloadResolutionByLambdaReturnType
internal inline fun Modifier.ifThen(
    condition: Boolean,
    modifier: Modifier.Companion.() -> Modifier?,
): Modifier {
    contract {
        callsInPlace(modifier, InvocationKind.AT_MOST_ONCE)
    }
    return if (condition) then(modifier(Modifier) ?: Modifier) else this
}

/** 返回带轻度透明度的颜色。 */
internal fun Color.slightlyWeaken(): Color = copy(alpha = 0.618f)

/** 返回半透明颜色。 */
internal fun Color.weaken(): Color = copy(alpha = 0.5f)

/** 返回带较强透明度的颜色。 */
internal fun Color.stronglyWeaken(): Color = copy(alpha = 0.382f)

/** 在悬停状态变化时执行回调。 */
internal fun Modifier.hoverable(
    onHover: () -> Unit,
    onUnhover: () -> Unit,
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val currentOnHover by rememberUpdatedState(onHover)
    val currentOnUnhover by rememberUpdatedState(onUnhover)
    LaunchedEffect(interactionSource) {
        val entered = mutableListOf<HoverInteraction.Enter>()
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is HoverInteraction.Enter -> entered.add(interaction)
                is HoverInteraction.Exit -> entered.remove(interaction.enter)
            }
            if (entered.isEmpty()) currentOnUnhover() else currentOnHover()
        }
    }
    hoverable(interactionSource)
}
