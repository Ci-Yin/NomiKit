@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package ciyin.video.player.ui.progress

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults.Container
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ciyin.video.player.ui.internal.PlatformPopupProperties
import ciyin.ui.foundation.effects.onKey
import ciyin.video.player.ui.internal.ifThen
import ciyin.video.player.ui.internal.slightlyWeaken
import ciyin.video.player.ui.internal.stronglyWeaken
import ciyin.video.player.data.MediaCacheProgressInfo
import ciyin.video.player.ui.PlaybackSpeedControllerState
import ciyin.video.player.ui.PlayerControllerState
import ciyin.video.player.ui.top.needWorkaroundForFocusManager
import ciyin.video.player.generated.resources.Res
import ciyin.video.player.generated.resources.video_player_cancel
import ciyin.video.player.generated.resources.video_player_danmaku_placeholder
import ciyin.video.player.generated.resources.video_player_disable_danmaku
import ciyin.video.player.generated.resources.video_player_enable_danmaku
import ciyin.video.player.generated.resources.video_player_enter_fullscreen
import ciyin.video.player.generated.resources.video_player_exit_fullscreen
import ciyin.video.player.generated.resources.video_player_mute
import ciyin.video.player.generated.resources.video_player_next_episode
import ciyin.video.player.generated.resources.video_player_pause
import ciyin.video.player.generated.resources.video_player_play
import ciyin.video.player.generated.resources.video_player_select_episode
import ciyin.video.player.generated.resources.video_player_send
import ciyin.video.player.generated.resources.video_player_skip_opening_or_ending
import ciyin.video.player.generated.resources.video_player_speed
import ciyin.video.player.generated.resources.video_player_volume
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

/** 选集按钮的测试标签。 */
const val TAG_SELECT_EPISODE_ICON_BUTTON = "SelectEpisodeIconButton"

/** 倍速按钮的测试标签。 */
const val TAG_SPEED_SWITCHER_TEXT_BUTTON = "SpeedSwitcherTextButton"

/** 倍速菜单的测试标签。 */
const val TAG_SPEED_SWITCHER_DROPDOWN_MENU = "SpeedSwitcherDropdownMenu"

/** 弹幕按钮的测试标签。 */
const val TAG_DANMAKU_ICON_BUTTON = "DanmakuIconButton"

/** 提供播放器控制栏组件与默认样式。 */
@Stable
object PlayerControllerDefaults {
    /**
     * To pause/play
     */
    @Composable
    fun PlaybackIcon(
        isPlaying: () -> Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        IconButton(
            onClick = onClick,
            modifier,
        ) {
            if (isPlaying()) {
                Icon(
                    Icons.Rounded.Pause,
                    contentDescription = stringResource(Res.string.video_player_pause),
                    Modifier.size(36.dp),
                )
            } else {
                Icon(
                    Icons.Rounded.PlayArrow,
                    contentDescription = stringResource(Res.string.video_player_play),
                    Modifier.size(36.dp),
                )
            }
        }
    }

    /**
     * To turn danmaku on/off
     */
    @Composable
    fun DanmakuIcon(
        danmakuEnabled: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        IconButton(
            onClick = onClick,
            modifier.testTag(TAG_DANMAKU_ICON_BUTTON),
        ) {
            if (danmakuEnabled) {
                Icon(
                    Icons.Rounded.Subtitles,
                    contentDescription = stringResource(Res.string.video_player_disable_danmaku),
                )
            } else {
                Icon(
                    Icons.Rounded.SubtitlesOff,
                    contentDescription = stringResource(Res.string.video_player_enable_danmaku),
                )
            }
        }
    }

    /** 显示音量或静音图标。 */
    @Composable
    fun AudioIcon(
        volume: Float,
        isMute: Boolean,
        maxValue: Float,
        onClick: () -> Unit,
        onchange: (Float) -> Unit,
        controllerState: PlayerControllerState,
        modifier: Modifier = Modifier,
    ) {
        val hoverInteraction = remember { MutableInteractionSource() }
        val isHovered by hoverInteraction.collectIsHoveredAsState()
        val audioIconRequester = remember { Any() }

        LaunchedEffect(true) {
            snapshotFlow { isHovered }.collect {
                controllerState.setRequestAlwaysOn(audioIconRequester, isHovered)
            }
        }
        Box(
            modifier = modifier.hoverable(hoverInteraction),
            contentAlignment = Alignment.BottomCenter,
        ) {
            val iconButton = @Composable {
                IconButton(
                    onClick = onClick,
                ) {
                    when {
                        isMute -> {
                            Icon(
                                Icons.AutoMirrored.Rounded.VolumeOff,
                                contentDescription = stringResource(Res.string.video_player_mute),
                            )
                        }

                        volume < 0.33f -> {
                            Icon(
                                Icons.AutoMirrored.Rounded.VolumeMute,
                                contentDescription = stringResource(Res.string.video_player_volume),
                            )
                        }

                        volume < 0.66f -> {
                            Icon(
                                Icons.AutoMirrored.Rounded.VolumeDown,
                                contentDescription = stringResource(Res.string.video_player_volume),
                            )
                        }

                        else -> {
                            Icon(
                                Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = stringResource(Res.string.video_player_volume),
                            )
                        }
                    }
                }
            }

            iconButton()

            Popup(
                alignment = Alignment.BottomCenter,
            ) {
                Surface(
                    modifier = Modifier
                        .hoverable(hoverInteraction)
                        .clip(shape = CircleShape),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        AnimatedVisibility(
                            visible = isHovered && !isMute,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = volume.times(100).roundToInt().toString(),
                                    modifier = Modifier.padding(8.dp),
                                )
                                val colors = SliderDefaults.colors(
                                    inactiveTrackColor = MaterialTheme.colorScheme.onSurface,
                                )
                                VerticalSlider(
                                    value = volume,
                                    onValueChange = onchange,
                                    modifier = Modifier.width(96.dp),
                                    thumb = {},
                                    colors = colors,
                                    track = { sliderState ->
                                        SliderDefaults.Track(
                                            colors = colors,
                                            enabled = true,
                                            sliderState = sliderState,
                                            thumbTrackGapSize = 0.dp,
                                        )
                                    },
                                    valueRange = 0f..maxValue,
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = isHovered && !isMute,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            iconButton()
                        }
                    }
                }
            }
        }
    }

    /** 显示下一集图标。 */
    @Composable
    fun NextEpisodeIcon(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        IconButton(
            onClick,
            modifier,
        ) {
            Icon(
                Icons.Rounded.SkipNext,
                stringResource(Res.string.video_player_next_episode),
                Modifier.size(36.dp),
            )
        }
    }

    /** 显示选集图标。 */
    @Composable
    fun SelectEpisodeIcon(
        onClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        TextButton(
            onClick,
            modifier.testTag(TAG_SELECT_EPISODE_ICON_BUTTON),
            colors = ButtonDefaults.textButtonColors(
                contentColor = LocalContentColor.current,
            ),
        ) {
            Text(stringResource(Res.string.video_player_select_episode))
        }
    }

    /**
     * To send danmaku
     */
    @Composable
    fun DanmakuSendButton(
        onClick: () -> Unit,
        enabled: Boolean = true,
        modifier: Modifier = Modifier,
    ) {
        IconButton(onClick = onClick, enabled = enabled, modifier = modifier) {
            Icon(
                Icons.AutoMirrored.Rounded.Send,
                contentDescription = stringResource(Res.string.video_player_send),
            )
        }
    }

    /** 提供视频内弹幕输入框颜色。 */
    @Composable
    fun inVideoDanmakuTextFieldColors(): TextFieldColors {
        return OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.stronglyWeaken(),
            focusedContainerColor = MaterialTheme.colorScheme.surface.stronglyWeaken(),
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface.slightlyWeaken(),
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
        )
    }

    /** 提供独立弹幕页输入框颜色。 */
    @Composable
    fun inTabDanmakuTextFieldColors(): TextFieldColors {
        return OutlinedTextFieldDefaults.colors(
        )
    }

    /**
     * To edit danmaku and send it by [trailingIcon]
     */
    @Composable
    fun DanmakuTextField(
        value: String,
        onValueChange: (String) -> Unit,
        modifier: Modifier = Modifier,
        onSend: () -> Unit = {},
        isSending: () -> Boolean = { false },
        interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
        placeholder: @Composable () -> Unit = {
            Text(
                stringResource(Res.string.video_player_danmaku_placeholder),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        leadingIcon: @Composable (() -> Unit)? = null,
        trailingIcon: @Composable (() -> Unit)? = {
            if (isSending()) {
                CircularProgressIndicator(
                    Modifier.size(20.dp),
//                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            } else {
                DanmakuSendButton(
                    onClick = { onSend() },
                    enabled = value.isNotBlank(),
                )
            }
        },
        enabled: Boolean = true,
        singleLine: Boolean = true,
        isError: Boolean = false,
        shape: Shape = MaterialTheme.shapes.medium,
        style: TextStyle = MaterialTheme.typography.bodyMedium,
        colors: TextFieldColors = inVideoDanmakuTextFieldColors()
    ) {
        BasicTextField(
            value,
            onValueChange,
            modifier.onKey(Key.Enter) {
                onSend()
            }.height(38.dp),
            textStyle = style.copy(color = colors.unfocusedTextColor),
            cursorBrush = SolidColor(rememberUpdatedState(if (isError) colors.errorCursorColor else colors.cursorColor).value),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value,
                    innerTextField,
                    enabled = enabled,
                    singleLine = singleLine,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    contentPadding = PaddingValues(vertical = 7.dp, horizontal = 16.dp),
                    colors = colors,
                    placeholder = {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(Modifier.weight(1f)) {
                                placeholder()
                            }
                        }
                    },
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    container = {
                        Container(
                            enabled = enabled,
                            isError = isError,
                            interactionSource = interactionSource,
                            colors = colors,
                            shape = shape,
                        )
                    },
                )
            },
        )
    }

    /**
     * To enter/exit fullscreen
     */
    @Composable
    fun FullscreenIcon(
        isFullscreen: Boolean,
        onClickFullscreen: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val focusManager by rememberUpdatedState(LocalFocusManager.current) // workaround for #288
        IconButton(
            onClick = onClickFullscreen,
            modifier.ifThen(needWorkaroundForFocusManager) {
                onFocusEvent {
                    if (it.hasFocus) {
                        focusManager.clearFocus()
                    }
                }
            },
        ) {
            if (isFullscreen) {
                Icon(
                    Icons.Rounded.FullscreenExit,
                    contentDescription = stringResource(Res.string.video_player_exit_fullscreen),
                    Modifier.size(32.dp),
                )
            } else {
                Icon(
                    Icons.Rounded.Fullscreen,
                    contentDescription = stringResource(Res.string.video_player_enter_fullscreen),
                    Modifier.size(32.dp),
                )
            }
        }
    }

    /**
     * Set 1x, 2x playback speed.
     * @param optionsProvider The options to choose from. Note that when the value changes, it will not reflect in the UI.
     */
    @Composable
    fun SpeedSwitcher(
        playbackSpeedControllerState: PlaybackSpeedControllerState,
        modifier: Modifier = Modifier,
        onExpandedChanged: (expanded: Boolean) -> Unit = {},
    ) {
        return OptionsSwitcher(
            value = playbackSpeedControllerState.currentIndex,
            onValueChange = { playbackSpeedControllerState.setSpeed(it) },
            optionsProvider = { playbackSpeedControllerState.speedList.indices.toList() },
            renderValue = { Text(remember(it) { "${playbackSpeedControllerState.speedList[it]}x" }) },
            renderValueExposed = {
                val speedValue = playbackSpeedControllerState.speedList[it]
                val speedLabel = stringResource(Res.string.video_player_speed)
                Text(
                    remember(
                        speedValue,
                        speedLabel
                    ) { if (speedValue == 1.0f) speedLabel else "${speedValue}x" })
            },
            modifier,
            properties = PlatformPopupProperties(
                clippingEnabled = false,
            ),
            textButtonTestTag = TAG_SPEED_SWITCHER_TEXT_BUTTON,
            dropdownMenuTestTag = TAG_SPEED_SWITCHER_DROPDOWN_MENU,
            onExpandedChanged = onExpandedChanged,
        )
    }

    /**
     * @param optionsProvider The options to choose from. Note that when the value changes, it will not reflect in the UI.
     */
    @Composable
    fun <T> OptionsSwitcher(
        value: T,
        onValueChange: (T) -> Unit,
        optionsProvider: () -> List<T>,
        renderValue: @Composable (T) -> Unit,
        renderValueExposed: @Composable (T) -> Unit = renderValue,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        properties: PopupProperties = PopupProperties(),
        textButtonTestTag: String = "textButton",
        dropdownMenuTestTag: String = "dropDownMenu",
        onExpandedChanged: (expanded: Boolean) -> Unit = {},
    ) {
        Box(modifier, contentAlignment = Alignment.Center) {
            var expanded by rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(true) {
                snapshotFlow { expanded }.collect {
                    onExpandedChanged(expanded)
                }
            }
            TextButton(
                { expanded = true },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = LocalContentColor.current,
                ),
                enabled = enabled,
                modifier = Modifier.testTag(textButtonTestTag),
            ) {
                renderValueExposed(value)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                properties = properties,
                modifier = Modifier.testTag(dropdownMenuTestTag),
            ) {
                val options = remember(optionsProvider) { optionsProvider() }
                for (option in options) {
                    DropdownMenuItem(
                        text = {
                            val color = if (value == option) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                LocalContentColor.current
                            }
                            CompositionLocalProvider(LocalContentColor provides color) {
                                renderValue(option)
                            }
                        },
                        onClick = {
                            expanded = false
                            onValueChange(option)
                        },
                    )
                }
            }
        }
    }

    /** 显示控制栏使用的媒体进度滑块。 */
    @Composable
    fun MediaProgressSlider(
        progressSliderState: PlayerProgressSliderState,
        cacheProgressInfoFlow: Flow<MediaCacheProgressInfo>,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        showPreviewTimeTextOnThumb: Boolean = true,
    ) {
        val cacheProgressInfo by cacheProgressInfoFlow.collectAsStateWithLifecycle(null)
        MediaProgressSlider(
            progressSliderState, { cacheProgressInfo },
            enabled = enabled,
            showPreviewTimeTextOnThumb = showPreviewTimeTextOnThumb,
            modifier = modifier,
        )
    }

    /** 显示控制栏左下角提示内容。 */
    @Composable
    fun LeftBottomTips(
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier.clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                    Text(
                        text = stringResource(Res.string.video_player_skip_opening_or_ending),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                    TextButton(onClick = onClick) {
                        Text(stringResource(Res.string.video_player_cancel))
                    }
                }
            }
        }
    }
}

/**
 * The controller bar of a video player. Usually at the bottom of the screen (the video player).
 *
 * See [PlayerControllerDefaults] for components.
 *
 * @param startActions [PlayerControllerDefaults.PlaybackIcon], [PlayerControllerDefaults.DanmakuIcon]
 * @param progressIndicator [MediaProgressIndicatorText]
 * @param progressSlider [MediaProgressSlider]
 * @param danmakuEditor [PlayerControllerDefaults.DanmakuTextField]
 * @param endActions [PlayerControllerDefaults.FullscreenIcon]
 * @param expanded Whether the controller bar is expanded.
 * If `true`, the [progressIndicator] and [progressSlider] will be shown on a separate row above. The bottom row will contain a [danmakuEditor].
 * If `false`, the entire bar will be only one row. [danmakuEditor] will be ignored.
 */
@Composable
fun PlayerControllerBar(
    startActions: @Composable RowScope.() -> Unit,
    progressIndicator: @Composable RowScope.() -> Unit,
    progressSlider: @Composable RowScope.() -> Unit,
    danmakuEditor: @Composable RowScope.() -> Unit,
    endActions: @Composable RowScope.() -> Unit,
    expanded: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clickable(
                remember { MutableInteractionSource() },
                null,
                onClick = {}) // Consume touch event
            .padding(
                horizontal = if (expanded) 8.dp else 4.dp,
                vertical = if (expanded) 4.dp else 2.dp,
            ),
    ) {
        Column {
            ProvideTextStyle(MaterialTheme.typography.labelMedium) {
                Row(
                    Modifier
                        .padding(start = if (expanded) 8.dp else 4.dp)
                        .padding(vertical = if (expanded) 4.dp else 2.dp),
                ) {
                    progressIndicator()
                }
                if (expanded) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        progressSlider()
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (expanded) 8.dp else 4.dp),
        ) {
            // 播放 / 暂停按钮
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                startActions()
            }

            Row(
                Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (expanded) {
                    ProvideTextStyle(MaterialTheme.typography.labelSmall) {
                        danmakuEditor()
                    }
                } else {
                    progressSlider()
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                endActions()
            }
        }
    }
}
