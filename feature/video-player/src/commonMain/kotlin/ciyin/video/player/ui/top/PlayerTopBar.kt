package ciyin.video.player.ui.top

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import ciyin.platform.currentPlatform
import ciyin.platform.isDesktop
import ciyin.video.player.ui.internal.ifThen
import ciyin.video.player.generated.resources.Res
import ciyin.video.player.generated.resources.video_player_back
import ciyin.video.player.generated.resources.video_player_settings
import ciyin.video.player.generated.resources.video_player_title_placeholder
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * 播放器顶部导航栏
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PlayerTopBar(
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    onBackPressed: () -> Unit = {},
    actions: @Composable (RowScope.() -> Unit) = {},
    color: Color = MaterialTheme.colorScheme.onBackground,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) {
    TopAppBar(
        title = {
            CompositionLocalProvider(LocalContentColor provides color) {
                if (title != null) {
                    title()
                }
            }
        },
        modifier
            .fillMaxWidth(),
        navigationIcon = {
            CompositionLocalProvider(LocalContentColor provides color) {
                val focusManager by rememberUpdatedState(LocalFocusManager.current) // workaround for #288
                IconButton(
                    onClick = onBackPressed,
                    Modifier.ifThen(needWorkaroundForFocusManager) {
                        onFocusEvent {
                            if (it.hasFocus) {
                                focusManager.clearFocus()
                            }
                        }
                    },
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        stringResource(Res.string.video_player_back),
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
        ),
        actions = {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) {
                actions()
            }
        },
        windowInsets = windowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
    )
}

/** 当前平台是否需要主动清理 Compose 焦点。 */
internal val needWorkaroundForFocusManager: Boolean
    @Composable
    get() = currentPlatform().isDesktop()

/** 预览播放器顶栏布局。 */
@Preview
@Composable
fun TopAppBarPreview(

) {
    PlayerTopBar(
        title = { Text(stringResource(Res.string.video_player_title_placeholder)) },
        actions = {
            // Add top bar actions here
            IconButton(onClick = { /* Settings action */ }) {
                Icon(
                    Icons.Rounded.Settings,
                    contentDescription = stringResource(Res.string.video_player_settings),
                )
            }
        }
    )
}
