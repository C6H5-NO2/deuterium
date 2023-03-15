package com.c6h5no2.deuterium.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.WindowState


enum class EditorState {
    EMPTY, LOADED, MODIFIED
}


class MainModel {
    // val windowState = WindowState()


    var editorState by mutableStateOf(EditorState.EMPTY)
        private set

    suspend fun openFile() {
        // todo: check save state
        val path = "../kt-scripts/hello.kt"
        // todo: check path
        println("try open")
        // try {
        //
        // }
    }
}
