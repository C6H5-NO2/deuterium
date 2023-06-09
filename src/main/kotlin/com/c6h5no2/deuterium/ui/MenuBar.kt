package com.c6h5no2.deuterium.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import kotlinx.coroutines.launch


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FrameWindowScope.menuBar(mainModel: MainModel) = MenuBar {
    val scope = rememberCoroutineScope()

    Menu("File", mnemonic = 'F') {
        Item(
            "New",
            enabled = mainModel.runnerState == MainModel.RunnerState.STOPPED,
            shortcut = KeyShortcut(Key.N, ctrl = true),
            onClick = { scope.launch { mainModel.requestNewFile() } }
        )
        Item(
            "Open",
            enabled = mainModel.runnerState == MainModel.RunnerState.STOPPED,
            shortcut = KeyShortcut(Key.O, ctrl = true),
            onClick = { scope.launch { mainModel.requestOpenFile() } }
        )
        Separator()
        Item(
            "Save",
            enabled = mainModel.editorState == MainModel.EditorState.MODIFIED
                    && mainModel.runnerState == MainModel.RunnerState.STOPPED,
            shortcut = KeyShortcut(Key.S, ctrl = true),
            onClick = { scope.launch { mainModel.requestSaveFile() } }
        )
        Separator()
        Item("Kill") { mainModel.appExitFunc() }
        Separator()
        Item("Exit") { scope.launch { mainModel.requestExitApp() } }
    }

    Menu("Run", mnemonic = 'R') {
        Item(
            "Run",
            enabled = mainModel.editorState != MainModel.EditorState.EMPTY
                    && mainModel.runnerState == MainModel.RunnerState.STOPPED
        ) { scope.launch { mainModel.runScript() } }
        Separator()
        Item(
            "Edit Configuration",
            enabled = mainModel.runnerState == MainModel.RunnerState.STOPPED
        ) { mainModel.runner.runConfig.showDialog = true }
    }

    // Menu("Help", mnemonic = 'H') {
    //     Item("About") {}
    // }
}
