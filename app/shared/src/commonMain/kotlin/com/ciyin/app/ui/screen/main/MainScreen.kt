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
import ciyin.ui.foundation.viewmodel.viewModel
import com.ciyin.app.ui.component.AppPreview
import com.ciyin.app.ui.component.Screen
import com.ciyin.app.ui.theme.AppTheme
import nomikit.app.shared.generated.resources.Res
import nomikit.app.shared.generated.resources.nav_home
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview


@Composable
fun MainScreen(vm: MainViewModel = viewModel(::MainViewModel)) {
    val state by vm.state.collectAsStateWithLifecycle()
    MainContent(
        state = state,
        onAction = vm.dispatchAction
    )
}

@Composable
private fun MainContent(
    state: MainUiState,
    onAction: (MainAction) -> Unit,
) = Screen(
    title = stringResource(Res.string.nav_home),
    toolbar = {}
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
