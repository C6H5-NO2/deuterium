package com.c6h5no2.deuterium.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.c6h5no2.deuterium.platform.JbFile
import com.c6h5no2.deuterium.platform.toProjectFile
import com.c6h5no2.deuterium.ui.runner.RunnerModel
import com.c6h5no2.deuterium.util.DialogState
import java.nio.file.Path


private val logger = mu.KotlinLogging.logger {}

class MainModel {
    val currentFile get() = codeViewer.editors.active?.file?.jvmFile


    // ------- editor -------

    lateinit var codeViewer: CodeViewer

    var editorState by mutableStateOf(EditorState.EMPTY)
        private set


    fun openFile(file: JbFile) {
        // todo: check save state
        if (!file.jvmFile.isFile) return
        logger.info { "Open file ${file.jvmFile.absolutePath}" }
        codeViewer.editors.open(file)
        editorState = EditorState.LOADED
    }


    val openDialog = DialogState<Path?>()
    suspend fun openFile() {
        // todo: check save state
        val path = openDialog.awaitResult()
        val file = path?.toFile()
        if (file == null || !file.isFile) return
        openFile(file.toProjectFile())
    }

    enum class EditorState {
        EMPTY, LOADED, MODIFIED
    }


    // ------- runner -------

    lateinit var runner: RunnerModel

    val runnerState get() = if (runner.isRunning) RunnerState.RUNNING else RunnerState.STOPPED

    suspend fun runOnce() {
        // todo: check save state
        val file = currentFile
        file ?: return
        runner.runOnce(file)
    }

    enum class RunnerState {
        STOPPED, RUNNING
    }
}
