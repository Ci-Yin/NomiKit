package com.ciyin.app.ui.component.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ciyin.app.data.project.datasource.DataStoreManager.gameDataStore2
import com.ciyin.app.data.project.model.project
import com.ciyin.app.domain.script.JarScriptManager
import com.ciyin.app.domain.script.usecase.RunJarScriptUseCase
import com.ciyin.app.ui.app.dialog
import com.ciyin.app.ui.component.FilledTextButton
import com.ciyin.app.ui.component.MenuChip
import com.ciyin.app.ui.theme.ScripRunning
import com.ciyin.app.ui.theme.ScripStop
import com.ciyin.app.ui.theme.iconpack.IconPack
import com.ciyin.app.ui.theme.iconpack.Paly
import com.ciyin.app.ui.theme.iconpack.Stop
import com.ciyin.app.util.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

val Shape = RoundedCornerShape(15)

@Composable
fun Toolbar(buttonText: String = "", onButtonClick: () -> Unit = {}) = Row(
    modifier = Modifier.fillMaxSize().padding(horizontal = 15.dp),
    verticalAlignment = Alignment.CenterVertically,
    content = {
        Spacer(Modifier.weight(1f))
        RunWidget()
        if (buttonText.isNotEmpty()) {
            FilledTextButton(
                modifier = Modifier.padding(start = 10.dp),
                text = buttonText,
                onClick = onButtonClick
            )
        }
    }
)

@Composable
fun RunWidget(
    state: RunState = rememberRunState(),
) = Row(
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.CenterVertically
) {
    Box {
        val key1 = state.projects.sumOf { it.name.hashCode() }
        MenuChip(
            modifier = Modifier.sizeIn(minHeight = 32.dp),
            menuItems = remember(key1) { state.projects.map { it.name } },
            selectedItemIndex = remember(key1) { state.selected },
            border = null,
            onSelectedChange = state::onProjectSelectedChange
        )
    }
    RunButton(
        imageVector = IconPack.Paly,
        color = if (state.isRunning) ScripStop else Color.Transparent,
        iconColor = if (state.isRunning) Color.White else ScripStop,
        onClick = {
            state.onRunClick()
        }
    )
    RunButton(
        imageVector = IconPack.Stop,
        color = if (state.isRunning) ScripRunning else Color.Transparent,
        iconColor = if (state.isRunning) Color.White else ScripStop,
        onClick = {
            state.onStopClick()
        }
    )
}

@Composable
private fun RunButton(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    color: Color,
    iconColor: Color,
    onClick: () -> Unit,
) = Surface(
    modifier = modifier.size(32.dp),
    shape = Shape,
    color = color,
    contentColor = iconColor,
    onClick = onClick,
) {
    Icon(
        modifier = Modifier.padding(8.dp),
        imageVector = imageVector,
        contentDescription = null
    )
}

@Composable
fun rememberRunState(vararg keys: Any?): RunState {
    val scope = rememberCoroutineScope()
    return remember(keys) {
        RunState(scope)
    }
}

class RunState(private val scope: CoroutineScope) : KoinComponent {

    val runJarScriptUseCase by inject<RunJarScriptUseCase>()

    var selected
        get() = gameDataStore2.data.projectSelected
        set(value) {
            gameDataStore2.data.projectSelected = value
        }

    val projects get() = gameDataStore2.data.projects

    val project get() = gameDataStore2.data.project

    var isRunning = false

    fun onProjectSelectedChange(selected: Int) {
        gameDataStore2.data.projectSelected = selected
        onSaveData()
    }

    fun onRunClick() = runCatching {
        // 运行jar文件
        scope.launch { runJarScriptUseCase(project) }
    }.onFailure {
        it.printStackTrace()
        dialog(message = it.message)
    }

    fun onStopClick() {
        JarScriptManager.destroyAll()
    }

    /**
     * 保存数据功能
     *
     * 此函数旨在将当前的游戏数据保存到一个持久化的存储中
     * 它调用了[DataStore.writeBacking]方法来执行实际的数据写入操作
     */
    fun onSaveData() {
        gameDataStore2.writeBacking()
    }

}