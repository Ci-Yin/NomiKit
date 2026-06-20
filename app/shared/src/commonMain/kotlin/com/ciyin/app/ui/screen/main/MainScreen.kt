package com.ciyin.app.ui.screen.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ciyin.material.theme.AppTheme
import ciyin.ui.foundation.viewmodel.viewModel
import com.ciyin.app.shared.Res
import com.ciyin.app.shared.nav_home
import com.ciyin.app.ui.component.AppPreview
import com.ciyin.app.ui.component.ScreenScaffold
import com.ciyin.app.ui.screen.sample.SampleModuleRoot
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview


/**
 * 首页入口。
 *
 * @param vm 首页 ViewModel
 */
@Composable
fun MainScreen(vm: MainViewModel = viewModel(::MainViewModel)) {
    val state by vm.state.collectAsStateWithLifecycle()
//    MainContent(
//        state = state,
//        onAction = vm.dispatchAction
//    )
    SampleModuleRoot()
}

/**
 * 首页内容。
 *
 * @param state 首页状态
 * @param onAction 首页动作分发回调
 */
@Composable
private fun MainContent(
    state: MainUiState,
    onAction: (MainAction) -> Unit,
) = ScreenScaffold(
    title = stringResource(Res.string.nav_home),
    topBar = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacings.medium)
    ) {
        items(state.items, { it.id }) { item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onAction(MainAction.ItemAction(item))
                    }
                    .padding(AppTheme.spacings.small)
            ) {
                Text(item.name)
            }
        }
    }
}

/**
 * 首页预览。
 */
@AppPreview
@Composable
private fun MainScreenPreview() = AppPreview {
    MainContent(
        state = MainUiState(
            items = listOf(),
        ),
        onAction = {},
    )
}
