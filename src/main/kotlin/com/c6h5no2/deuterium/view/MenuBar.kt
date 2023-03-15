package com.c6h5no2.deuterium.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import com.c6h5no2.deuterium.model.MainModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FrameWindowScope.menuBar(mainModel: MainModel) = MenuBar {
    val scope = rememberCoroutineScope()

    fun new() = scope.launch { println("todo: new") }
    fun open() = scope.launch { mainModel.openFile() }
    fun save() = scope.launch { println("todo: save") }
    fun exit() = scope.launch { println("todo: exit") }

    Menu("File", mnemonic = 'F') {
        Item("New", shortcut = KeyShortcut(Key.N, ctrl = true)) { new() }
        Item("Open", shortcut = KeyShortcut(Key.O, ctrl = true)) { open() }
        Separator()
        Item("Save", enabled = true, shortcut = KeyShortcut(Key.S, ctrl = true)) { save() }
        Separator()
        Item("Settings", shortcut = KeyShortcut(Key.Comma, ctrl = true)) { }
        Separator()
        Item("Exit") { exit() }
    }

    Menu("Run", mnemonic = 'R') {
        Item("Run Once", enabled = true) {}
        // Item("Run Multiple", enabled = true) {}
        // Separator()
        // Item("Stop", enabled = true) {}
        // Item("Skip", enabled = true) {}
        Separator()
        Item("Edit Configuration") {}
    }

    Menu("Help", mnemonic = 'H') {
        Item("About") {}
    }
}
