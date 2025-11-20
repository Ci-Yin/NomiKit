package com.ciyin.app.ui.screen.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ciyin.foundation.Window
import ciyin.foundation.viewmodel.viewModel
import ciyin.io.File
import ciyin.io.toFile
import ciyin.jar.getScriptProjectClassTry
import com.ciyin.app.data.project.datasource.defaultGameProjects
import com.ciyin.app.data.project.model.Game
import com.ciyin.app.ui.app.AppDialog
import com.ciyin.app.ui.app.dialogError
import com.ciyin.app.ui.component.AppPreview
import com.ciyin.app.ui.component.Card
import com.ciyin.app.ui.component.ContentBody
import com.ciyin.app.ui.component.FilledTextButtonData
import com.ciyin.app.ui.component.IconButton2
import com.ciyin.app.ui.component.MenuChip
import com.ciyin.app.ui.component.ProjectImageButton
import com.ciyin.app.ui.component.Screen
import com.ciyin.app.ui.component.StateTextButton
import com.ciyin.app.ui.component.Title
import com.ciyin.app.ui.component.common.GamePage
import com.ciyin.app.ui.component.common.PresetMenu
import com.ciyin.app.ui.component.common.Toolbar
import com.ciyin.app.ui.component.common.rememberPresetState
import com.ciyin.app.ui.theme.ScripRunning
import com.ciyin.app.ui.theme.iconpack.Copy
import com.ciyin.app.ui.theme.iconpack.Delete
import com.ciyin.app.ui.theme.iconpack.Edit
import com.ciyin.app.ui.theme.iconpack.IconPack
import com.ciyin.app.ui.theme.iconpack.Window
import com.ciyin.app.util.ImageExtensions
import com.ciyin.app.util.value
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.AppPreview
import rpa.app.shared.generated.resources.Res
import rpa.app.shared.generated.resources.main_config_file_label
import rpa.app.shared.generated.resources.main_create_new_project
import rpa.app.shared.generated.resources.main_edit_config_title
import rpa.app.shared.generated.resources.main_jar_path_label
import rpa.app.shared.generated.resources.main_package_name_label
import rpa.app.shared.generated.resources.main_preset_prefix
import rpa.app.shared.generated.resources.main_project_id_error
import rpa.app.shared.generated.resources.main_project_id_label
import rpa.app.shared.generated.resources.main_project_run_state_run
import rpa.app.shared.generated.resources.main_project_run_state_stop
import rpa.app.shared.generated.resources.main_screen_title
import rpa.app.shared.generated.resources.main_script_class_label
import rpa.app.shared.generated.resources.main_title_label

@Composable
fun MainScreen(vm: MainViewModel = viewModel(::MainViewModel)) {
    val state by vm.state.collectAsStateWithLifecycle()
    MainContent(
        projects = state.projects,
        onSaveData = vm::onSaveData,
        onScriptRunClick = vm::onScriptRunClick,
        onProjectIconSelected = vm::onProjectIconSelected,
        onPresetMenuSelectedChange = vm::onPresetMenuSelectedChange,
        onPresetItemRemoveClick = vm::onPresetItemRemoveClick,
        onProjectIdIsRepeat = vm::onProjectIdIsRepeat,
        onProjectEditChange = vm::onProjectEditChange,
        onProjectCopyClick = vm::onProjectCopyClick,
        onProjectRemoveClick = vm::onProjectRemoveClick,
        onTaskResetClick = vm::onTaskResetClick,
    )
}

@Composable
private fun MainContent(
    projects: List<ProjectUiModel>,
    onSaveData: () -> Unit,
    onScriptRunClick: (ProjectUiModel) -> Unit,
    onProjectIconSelected: (ProjectUiModel, String, IntSize) -> Unit,
    onPresetMenuSelectedChange: (ProjectUiModel, Int) -> Unit,
    onPresetItemRemoveClick: (ProjectUiModel, Game) -> Unit,
    onProjectIdIsRepeat: (ProjectUiModel, String) -> Boolean,
    onProjectEditChange: (ProjectUiModel, ProjectUiModel) -> Unit,
    onProjectCopyClick: (ProjectUiModel) -> Unit,
    onProjectRemoveClick: (ProjectUiModel) -> Unit,
    onTaskResetClick: (ProjectUiModel) -> Unit,
) = Screen(
    title = stringResource(Res.string.main_screen_title),
    toolbar = {
        Toolbar(stringResource(Res.string.main_create_new_project)) {

        }
    }
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(projects, { it.id }) { project ->
            ProjectItem(
                modifier = Modifier.animateItem(),
                project = project,
                onSaveData = onSaveData,
                onScriptRunClick = onScriptRunClick,

                onProjectIconSelected = onProjectIconSelected,

                onPresetSelectionChange = onPresetMenuSelectedChange,
                onPresetItemRemoveClick = onPresetItemRemoveClick,

                onProjectIdIsRepeat = onProjectIdIsRepeat,
                onProjectEditChange = onProjectEditChange,
                onProjectCopyClick = onProjectCopyClick,
                onProjectDelClick = onProjectRemoveClick,

                onTaskResetClick = onTaskResetClick,
            )
        }
    }
}


@Composable
private fun ProjectItem(
    project: ProjectUiModel,
    onSaveData: () -> Unit,

    onScriptRunClick: (ProjectUiModel) -> Unit,
    onProjectIconSelected: (ProjectUiModel, String, IntSize) -> Unit,

    onPresetSelectionChange: (ProjectUiModel, Int) -> Unit,
    onPresetItemRemoveClick: (ProjectUiModel, Game) -> Unit,

    onProjectIdIsRepeat: (ProjectUiModel, String) -> Boolean,
    onProjectEditChange: (ProjectUiModel, ProjectUiModel) -> Unit,
    onProjectCopyClick: (ProjectUiModel) -> Unit,
    onProjectDelClick: (ProjectUiModel) -> Unit,

    onTaskResetClick: (ProjectUiModel) -> Unit,

    modifier: Modifier = Modifier
) = Card(
    modifier = modifier,
    contentPaddings = PaddingValues(0.dp)
) {

    var expanded by rememberSaveable { mutableStateOf(false) }
    var isShowDialog by remember { mutableStateOf(false) }
    var showWindow by remember { mutableStateOf(false) }
    var iconSize = IntSize(0, 0)

    val singleLauncher = rememberFilePickerLauncher(
        mode = FileKitMode.Single,
        type = FileKitType.File(ImageExtensions + listOf("exe")),
    ) { platformFile ->
        platformFile?.let { onProjectIconSelected(project, it.toString(), iconSize) }
    }


    ProjectEditDialog(
        isShowDialog = isShowDialog,
        project = project,
        onShowDialog = { isShowDialog = it },
        onProjectIdIsRepeat = onProjectIdIsRepeat,
        onProjectEditChange = onProjectEditChange
    )

    Row(
        modifier = Modifier
            .clip(CardDefaults.outlinedShape)
            .clickable { expanded = !expanded }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        ProjectImageButton(
            modifier = Modifier.size(45.dp)
                .onSizeChanged {
                    iconSize = it
                },
            icon = project.icon
        ) {
            singleLauncher.launch()
        }


        Spacer(Modifier.width(15.dp))

        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Title(
                modifier = Modifier.padding(horizontal = 5.dp),
                text = project.name,
                fontSize = 16.sp,
            )

            ContentBody(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .padding(horizontal = 5.dp),
                text = "${project.jarPath.toFile().name} ${project.scriptProjectClass}",
                fontSize = 14.sp,
            )
        }

        Box(modifier = Modifier.padding(horizontal = 5.dp)) {
            val key1 = project.games.sumOf { it.preset.hashCode() }
            MenuChip(
                modifier = Modifier.sizeIn(minWidth = 110.dp, minHeight = 32.dp),
                menuItems = remember(key1) { project.games.map { it.preset } },
                selectedItemIndex = remember(key1, project.game) { project.selection },
                onSelectedChange = { onPresetSelectionChange(project, it) }
            )
        }

        StateTextButton(
            data = FilledTextButtonData.data(
                text = stringResource(Res.string.main_project_run_state_run),
                disabledText = stringResource(Res.string.main_project_run_state_stop),
                disabledContainerColor = ScripRunning
            ),
            state = project.isRunning.not(),
            onClick = {
                onScriptRunClick(project)
            }
        )

        IconButton2(IconPack.Edit) {
            isShowDialog = true
        }
        IconButton2(IconPack.Copy) {
            onProjectCopyClick(project)
        }
        IconButton2(IconPack.Delete) {
            onProjectDelClick(project)
        }
        IconButton2(IconPack.Window) {
            showWindow = true
        }
        Icon(
            modifier = Modifier.rotate(if (expanded) 180f else 0f),
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null
        )
    }

    AnimatedVisibility(expanded) {
        ProjectPage(
            project = project,
            onSaveData = onSaveData,
            onTaskResetClick = onTaskResetClick,
            onPresetSelectionChange = onPresetSelectionChange,
            onPresetItemRemoveClick = onPresetItemRemoveClick,
        )
    }

    ProjectWindow(
        project = project,
        showWindow = showWindow,
        onSaveData = onSaveData,
        onTaskResetClick = onTaskResetClick,
        onPresetSelectionChange = onPresetSelectionChange,
        onPresetItemRemoveClick = onPresetItemRemoveClick,
        onCloseRequest = {
            showWindow = false
        }
    )

}

@Composable
private fun ProjectPage(
    project: ProjectUiModel,
    onSaveData: () -> Unit,
    onTaskResetClick: (ProjectUiModel) -> Unit,
    onPresetSelectionChange: (ProjectUiModel, Int) -> Unit,
    onPresetItemRemoveClick: (ProjectUiModel, Game) -> Unit,
) {
    GamePage(
        modifier = Modifier
            .padding(bottom = 10.dp)
            .padding(horizontal = 10.dp),
        game = project.game,
        isLazyColumn = false,
        onSaveData = onSaveData,
        onTaskResetClick = { onTaskResetClick(project) },
        menuLayout = {
            val state = rememberPresetState(
                key = project,
                games = project.games.toMutableStateList(),
                selection = project.selection,
                onSelectionChange = {
                    onPresetSelectionChange(project, it)
                },
            )
            PresetMenu(
                title = "${stringResource(Res.string.main_preset_prefix)}${project.game.preset}",
                state = state,
                onItemRemoveClick = {
                    onPresetItemRemoveClick(project, it)
                    state.onItemRemoveClick(it)
                },
            )
        }
    )
}


@Composable
private fun ProjectEditDialog(
    isShowDialog: Boolean,
    project: ProjectUiModel,
    onShowDialog: (Boolean) -> Unit,
    onProjectIdIsRepeat: (ProjectUiModel, String) -> Boolean,
    onProjectEditChange: (ProjectUiModel, ProjectUiModel) -> Unit,
) {
    if (!isShowDialog) return

    var projectState by remember(project) { mutableStateOf(project) }

    var id by remember(project.id) { mutableStateOf(project.id.toString()) }

    var isIdError by remember { mutableStateOf(false) }

    val showJarFilePicker = rememberFilePickerLauncher(
        mode = FileKitMode.Single,
        type = FileKitType.File(listOf("jar", "dex")),
    ) { platformFile ->
        platformFile?.let {
            projectState = projectState.copy(
                jarPath = it.toString(),
                scriptProjectClass = getScriptProjectClassTry(projectState.jarPath)
            )
        }
    }

    val showConfigFilePicker = rememberFilePickerLauncher(
        mode = FileKitMode.Single,
        type = FileKitType.File(listOf("json")),
    ) { platformFile ->
        platformFile?.let {
            projectState = projectState.copy(gameConfig = it.toString())
        }
    }

    val showPackageNamePicker = rememberFilePickerLauncher(
        mode = FileKitMode.Single,
        type = FileKitType.File(listOf("exe")),
    ) { platformFile ->
        platformFile?.let {
            projectState = projectState.copy(packageName = it.toString())
        }
    }

    AppDialog(
        title = stringResource(Res.string.main_edit_config_title),
        onDismissRequest = { onShowDialog(false) },
        onDismissClick = { onShowDialog(false) },
        onConfirmClick = {
            if (isIdError) {
                dialogError(Res.string.main_project_id_error.value)
                return@AppDialog
            }
            onProjectEditChange(project, projectState.copy(id = id.toIntOrNull() ?: project.id))
            onShowDialog(false)
        }
    ) {
        Column(modifier = Modifier.width(300.dp)) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(Res.string.main_project_id_label)) },
                value = id,
                isError = isIdError,
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                onValueChange = {
                    id = it
                    if (it.isEmpty()) {
                        isIdError = true
                        return@OutlinedTextField
                    }
                    isIdError = onProjectIdIsRepeat(project, it)
                },
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(Res.string.main_title_label)) },
                value = projectState.name,
                onValueChange = {
                    projectState = projectState.copy(name = it)
                },
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(Res.string.main_package_name_label)) },
                value = projectState.packageName,
                trailingIcon = {
                    IconButton2(Icons.Default.AddCard, fraction = 0.7f) {
                        showPackageNamePicker.launch()
                    }
                },
                onValueChange = {
                    projectState = projectState.copy(packageName = it)
                },
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(Res.string.main_config_file_label)) },
                value = projectState.gameConfig,
                trailingIcon = {
                    IconButton2(Icons.Default.AddCard, fraction = 0.7f) {
                        showConfigFilePicker.launch()
                    }
                },
                onValueChange = {
                    projectState = projectState.copy(gameConfig = it)
                },
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(Res.string.main_jar_path_label)) },
                value = projectState.jarPath,
                trailingIcon = {
                    IconButton2(Icons.Default.AddCard, fraction = 0.7f) {
                        showJarFilePicker.launch()
                    }
                },
                onValueChange = {
                    projectState = projectState.copy(jarPath = it)
                    if (File(projectState.jarPath).exists()) {
                        projectState = projectState.copy(
                            scriptProjectClass = getScriptProjectClassTry(projectState.jarPath)
                        )
                    }
                },
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(Res.string.main_script_class_label)) },
                value = projectState.scriptProjectClass,
                trailingIcon = {
                    IconButton2(Icons.Default.Refresh, fraction = 0.7f) {
                        projectState = projectState.copy(
                            scriptProjectClass = getScriptProjectClassTry(projectState.jarPath)
                        )
                    }
                },
                onValueChange = {
                    projectState = projectState.copy(scriptProjectClass = it)
                },
            )
        }

    }

}


@Composable
private fun ProjectWindow(
    showWindow: Boolean,
    project: ProjectUiModel,
    onSaveData: () -> Unit,
    onCloseRequest: () -> Unit,
    onTaskResetClick: (ProjectUiModel) -> Unit,
    onPresetSelectionChange: (ProjectUiModel, Int) -> Unit,
    onPresetItemRemoveClick: (ProjectUiModel, Game) -> Unit,
) {
    Window(
        title = project.name,
        visible = showWindow,
        onCloseRequest = onCloseRequest,
    ) {
        ProjectPage(
            project = project,
            onSaveData = onSaveData,
            onTaskResetClick = onTaskResetClick,
            onPresetSelectionChange = onPresetSelectionChange,
            onPresetItemRemoveClick = onPresetItemRemoveClick,
        )
    }
}


@AppPreview
@Composable
private fun MainScreenPreview() = AppPreview {
    MainContent(
        projects = defaultGameProjects.map { it.toProjectUiModel() },
        onSaveData = {},
        onScriptRunClick = {},
        onProjectIconSelected = { _, _, _ -> },
        onPresetMenuSelectedChange = { _, _ -> },
        onPresetItemRemoveClick = { _, _ -> },
        onProjectIdIsRepeat = { _, _ -> false },
        onProjectEditChange = { _, _ -> },
        onProjectCopyClick = {},
        onProjectRemoveClick = {},
        onTaskResetClick = {}
    )
}
