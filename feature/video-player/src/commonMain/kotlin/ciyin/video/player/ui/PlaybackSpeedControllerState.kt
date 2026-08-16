package ciyin.video.player.ui

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.openani.mediamp.InternalForInheritanceMediampApi
import org.openani.mediamp.features.PlaybackSpeed

/**
 * Side-effect: creation of this state will immediately set the playback speed to the initial speed.
 *
 * @param scope coroutine scope for playback speed collector, usually [rememberCoroutineScope].
 */
@Stable
class PlaybackSpeedControllerState(
    /** Mediamp 倍速能力。 */
    private val playbackSpeed: PlaybackSpeed,
    speedProvider: () -> List<Float> = { listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f, 3f) },
    scope: CoroutineScope
) {
    /** 可供用户选择的倍速列表。 */
    val speedList: List<Float> by derivedStateOf(speedProvider)

    /** 当前播放器倍速。 */
    var currentSpeed by mutableStateOf(playbackSpeed.value)
        private set

    /**
     * `-1` represents a invalid index, which means the current speed is not in the list.
     */
    val currentIndex: Int by derivedStateOf {
        val index = speedList.indexOf(currentSpeed)
        if (index == -1) {
            speedList.indexOf(1f).also {
                check(it != -1) {
                    "Playback speed list must contain 1.0f, but was $speedList"
                }
            }
        } else {
            index
        }
    }

    init {
        require(speedList.isNotEmpty()) { "Playback speed list must not be empty" }

        var hasOriginalSpeed = false
        val isMonotonicIncreasing = speedList
            .asSequence()
            .zipWithNext()
            .all { (a, b) ->
                if (a == 1f || b == 1f) hasOriginalSpeed = true
                a <= b
            }

        require(isMonotonicIncreasing) { "Playback speed list should be monotonic increasing" }
        require(hasOriginalSpeed) { "Playback speed list should contain 1.0f" }

        scope.launch {
            playbackSpeed.valueFlow
                .distinctUntilChanged()
                .collect { value -> currentSpeed = value }
        }
    }

    /**
     * Set playback speed to the next index presented in list based on [currentIndex].
     *
     * If current speed is not at the provider list, set to the nearest up speed in the list.
     */
    fun speedUp() {
        if (currentIndex == -1) {
            val nearestUp = speedList.firstOrNull { it > currentSpeed } ?: speedList.last()
            playbackSpeed.set(nearestUp)
        } else if (currentIndex < speedList.size - 1) {
            playbackSpeed.set(speedList[currentIndex + 1])
        }
    }

    /** 切换到下一档更低倍速。 */
    fun speedDown() {
        if (currentIndex == -1) {
            val nearestDown = speedList.lastOrNull { it < currentSpeed } ?: speedList.first()
            playbackSpeed.set(nearestDown)
        } else if (currentIndex > 0) {
            playbackSpeed.set(speedList[currentIndex - 1])
        }
    }

    /** 按列表索引设置倍速。 */
    fun setSpeed(index: Int) {
        require(index in speedList.indices) {
            "Speed index is out of range, index: $index, size: ${speedList.size}"
        }
        setSpeed(speedList[index])
    }

    /** 直接设置指定倍速。 */
    fun setSpeed(value: Float) {
        playbackSpeed.set(value)
    }

    /** 恢复正常倍速。 */
    fun reset() {
        setSpeed(1f)
    }
}

/** 不支持倍速能力时使用的空实现。 */
@OptIn(InternalForInheritanceMediampApi::class)
object NoOpPlaybackSpeedController : PlaybackSpeed {
    /** 固定为正常倍速。 */
    override val value: Float = 1f

    /** 固定发布正常倍速。 */
    override val valueFlow: Flow<Float> = flowOf(1f)

    /** 空实现不会修改倍速。 */
    override fun set(speed: Float) {

    }
}
