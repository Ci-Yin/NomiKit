package com.ciyin.app.ui.screen.main

import ciyin.ui.foundation.viewmodel.AbsMviViewModel
import ciyin.ui.foundation.viewmodel.SingleStateMachine
import com.ciyin.app.ui.screen.main.MainAction.ItemAction
import org.koin.core.component.KoinComponent


/**
 *
 * kotlin类作用描述
 *
 * @author <a href="https://github.com/Ci-Yin">次音(CiYin)</a>
 * @since 2024/10/1 下午6:44
 */
class MainViewModel() : AbsMviViewModel<MainUiState, MainAction, MainEffect>(null), KoinComponent {

    override val initState: MainUiState = MainUiState(
        items = List(100) {
            MainUiModel(
                id = it,
                name = "Item $it"
            )
        }
    )

    override fun SingleStateMachine<MainUiState, MainAction>.spec() {

        // 创建项目
        on<ItemAction> { action ->
            update {
                copy(
                    items = items.map {
                        if (it.id == action.item.id) {
                            action.item.copy(name = "点击")
                        } else {
                            it
                        }
                    }
                )
            }
        }

    }

}
