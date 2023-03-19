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
    fun openFile() = scope.launch { mainModel.requestOpenFile() }
    fun saveFile() = scope.launch { mainModel.requestSaveFile() }
    fun exitApp() = scope.launch { println("todo: exit") }

    Menu("File", mnemonic = 'F') {
        Item("New", shortcut = KeyShortcut(Key.N, ctrl = true)) { newFile() }
        Item("Open", shortcut = KeyShortcut(Key.O, ctrl = true)) { openFile() }
        Separator()
        Item(
            "Save",
            enabled = mainModel.editorState == MainModel.EditorState.MODIFIED,
            shortcut = KeyShortcut(Key.S, ctrl = true)
        ) { saveFile() }
        Separator()
        Item("Exit") { exitApp() }
    }

    fun runOnce() = scope.launch { mainModel.runOnce() }

    Menu("Run", mnemonic = 'R') {
        Item(
            "Run Once",
            enabled = mainModel.editorState != MainModel.EditorState.EMPTY
                    && mainModel.runnerState == MainModel.RunnerState.STOPPED
        ) { runOnce() }
        // Item("Run Multiple", enabled = true) {}
        Separator()
        Item("Edit Configuration") {}
    }

    // Menu("Help", mnemonic = 'H') {
    //     Item("About") {}
    // }
}
