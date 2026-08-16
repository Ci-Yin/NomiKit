/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package ciyin.video.player.ui.progress

import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ciyin.video.player.ui.internal.PlatformPopupProperties
import ciyin.ui.foundation.viewmodel.AbstractViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.openani.mediamp.metadata.AudioTrack
import org.openani.mediamp.metadata.TrackGroup
import ciyin.video.player.generated.resources.Res
import ciyin.video.player.generated.resources.video_player_audio_track
import ciyin.video.player.generated.resources.video_player_auto_audio
import org.jetbrains.compose.resources.stringResource

/** 管理可选音轨列表与当前选中项。 */
@Stable
class AudioTrackState(
    current: StateFlow<AudioTrack?>,
    candidates: Flow<List<AudioTrack>>,
) : AbstractViewModel() {
    /** 可供选择的音轨展示项。 */
    val options = candidates.map { tracks ->
        tracks.map { track ->
            AudioPresentation(track, track.audioName)
        }
    }.flowOn(Dispatchers.Default).shareInBackground()

    /** 与当前选中音轨匹配的展示项。 */
    val value = combine(options, current) { options, current ->
        options.firstOrNull { it.audioTrack.id == current?.id }
    }.flowOn(Dispatchers.Default)
}


/** 使用 Mediamp 元数据能力展示音轨切换器。 */
@Composable
fun PlayerControllerDefaults.AudioSwitcher(
    playerState: TrackGroup<AudioTrack>,
    modifier: Modifier = Modifier,
    onSelect: (AudioTrack?) -> Unit = { playerState.select(it) },
) {
    val state = remember(playerState) {
        AudioTrackState(playerState.selected, playerState.candidates)
    }
    AudioSwitcher(state, onSelect, modifier)
}

/** 使用媒体元数据构造并展示音轨切换器。 */
@Composable
fun PlayerControllerDefaults.AudioSwitcher(
    state: AudioTrackState,
    onSelect: (AudioTrack?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options by state.options.collectAsStateWithLifecycle(emptyList())
    AudioSwitcher(
        value = state.value.collectAsStateWithLifecycle(null).value,
        onValueChange = { onSelect(it?.audioTrack) },
        optionsProvider = { options },
        modifier,
    )
}

/**
 * 选音轨.
 */
@Composable
fun PlayerControllerDefaults.AudioSwitcher(
    value: AudioPresentation?,
    onValueChange: (AudioPresentation?) -> Unit,
    optionsProvider: () -> List<AudioPresentation>,
    modifier: Modifier = Modifier,
) {
    val automaticLabel = stringResource(Res.string.video_player_auto_audio)
    val audioTrackLabel = stringResource(Res.string.video_player_audio_track)
    val optionsProviderUpdated by rememberUpdatedState(optionsProvider)
    val options by remember {
        derivedStateOf {
            optionsProviderUpdated() + null
        }
    }
    if (options.size <= 2) return // 1 for `null`, 只有一个的时候也不要显示
    return OptionsSwitcher(
        value = value,
        onValueChange = onValueChange,
        optionsProvider = { options },
        renderValue = {
            if (it == null) {
                Text(automaticLabel)
            } else {
                Text(it.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
        renderValueExposed = {
            Text(
                remember(it, audioTrackLabel) { it?.displayName ?: audioTrackLabel },
                Modifier.widthIn(max = 64.dp),
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        },
        modifier,
        properties = PlatformPopupProperties(
            clippingEnabled = false,
        ),
    )
}
