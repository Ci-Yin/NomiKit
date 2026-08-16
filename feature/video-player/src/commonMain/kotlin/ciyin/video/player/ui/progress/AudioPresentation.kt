/*
 * Copyright (C) 2024 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package ciyin.video.player.ui.progress

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import org.openani.mediamp.metadata.AudioTrack

/** 音轨选择菜单使用的展示模型。 */
@Immutable
class AudioPresentation(
    /** 原始音轨。 */
    val audioTrack: AudioTrack,
    /** 菜单显示名称。 */
    val displayName: String,
)

/** 获取音轨优先用于展示的名称。 */
@Stable
val AudioTrack.audioName: String
    get() = name ?: labels.firstOrNull()?.value ?: internalId
