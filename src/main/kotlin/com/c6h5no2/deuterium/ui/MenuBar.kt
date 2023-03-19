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

    fun newFile() = scope.launch { println("todo: new") }

    Menu("File", mnemonic = 'F') {
        Item("New", shortcut = KeyShortcut(Key.N, ctrl = true)) { newFile() }
        Item(
            "Open",
            shortcut = KeyShortcut(Key.O, ctrl = true),
            onClick = { scope.launch { mainModel.requestOpenFile() } }
        )
        Separator()
        Item(
            "Save",
            enabled = mainModel.editorState == MainModel.EditorState.MODIFIED,
            shortcut = KeyShortcut(Key.S, ctrl = true),
            onClick = { scope.launch { mainModel.requestSaveFile() } }
        )
        Separator()
        Item("Exit") { scope.launch { mainModel.requestExitApp() } }
    }

    Menu("Run", mnemonic = 'R') {
        Item(
            "Run Once",
            enabled = mainModel.editorState != MainModel.EditorState.EMPTY
                    && mainModel.runnerState == MainModel.RunnerState.STOPPED
        ) { scope.launch { mainModel.runOnce() } }
        // Item("Run Multiple", enabled = true) {}
        Separator()
        Item("Edit Configuration") {}
    }

    // Menu("Help", mnemonic = 'H') {
    //     Item("About") {}
    // }
}
