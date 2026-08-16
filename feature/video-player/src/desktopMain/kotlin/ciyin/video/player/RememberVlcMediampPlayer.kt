package ciyin.video.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import org.openani.mediamp.MediampPlayer
import uk.co.caprica.vlcj.factory.NativeLibraryMappingException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/** Desktop VLC 播放器的初始化结果。 */
sealed interface VlcPlayerInitializationResult {
    /** VLC 运行时可用时创建出的播放器。 */
    class Ready(
        /** 已创建并由组合生命周期持有的播放器。 */
        val player: MediampPlayer,
    ) : VlcPlayerInitializationResult

    /** VLC 原生库缺失或无法链接时的失败结果。 */
    class Unavailable(
        /** 原生库链接失败的具体原因。 */
        val cause: Throwable,
    ) : VlcPlayerInitializationResult
}

/**
 * 记住 Desktop VLC 播放器，并把原生库链接错误转换为可显示的初始化结果。
 *
 * 成功创建的播放器会在离开组合时自动关闭。
 */
@Composable
fun rememberVlcMediampPlayer(
    parentCoroutineContext: () -> CoroutineContext = { EmptyCoroutineContext },
): VlcPlayerInitializationResult {
    val coroutineContext = remember { parentCoroutineContext() }
    val result = remember(coroutineContext) {
        initializeVlcPlayer {
            MediampPlayer(
                context = Unit,
                parentCoroutineContext = coroutineContext,
            )
        }
    }
    val player = (result as? VlcPlayerInitializationResult.Ready)?.player
    DisposableEffect(player) {
        onDispose {
            player?.close()
        }
    }
    return result
}

/** 创建 VLC 播放器，并把原生链接及 VLCJ 原生映射错误转换为不可用结果。 */
internal fun initializeVlcPlayer(
    createPlayer: () -> MediampPlayer,
): VlcPlayerInitializationResult = try {
    VlcPlayerInitializationResult.Ready(
        createPlayer(),
    )
} catch (error: LinkageError) {
    VlcPlayerInitializationResult.Unavailable(error)
} catch (error: NativeLibraryMappingException) {
    VlcPlayerInitializationResult.Unavailable(error)
}
