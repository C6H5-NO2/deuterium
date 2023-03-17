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
    fun openFile() = scope.launch { mainModel.openFile() }
    fun saveFile() = scope.launch { println("todo: save") }
    fun exitApp() = scope.launch { println("todo: exit") }

    Menu("File", mnemonic = 'F') {
        Item("New", shortcut = KeyShortcut(Key.N, ctrl = true)) { newFile() }
        Item("Open", shortcut = KeyShortcut(Key.O, ctrl = true)) { openFile() }
        Separator()
        Item("Save", enabled = true, shortcut = KeyShortcut(Key.S, ctrl = true)) { saveFile() }
        Separator()
        Item("Settings", shortcut = KeyShortcut(Key.Comma, ctrl = true)) { }
        Separator()
        Item("Exit") { exitApp() }
    }

    fun runOnce() = scope.launch { mainModel.runOnce() }

    Menu("Run", mnemonic = 'R') {
        Item(
            "Run Once",
            enabled = mainModel.editorState == MainModel.EditorState.LOADED && !mainModel.runner.isRunning
        ) { runOnce() }
        // Item("Run Multiple", enabled = true) {}
        // Separator()
        // Item("Stop", enabled = true) {}
        // Item("Skip", enabled = true) {}
        Separator()
        Item("Edit Configuration") {}
    }

    // Menu("Help", mnemonic = 'H') {
    //     Item("About") {}
    // }
}
