package com.c6h5no2.deuterium.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.c6h5no2.deuterium.platform.JbFile
import com.c6h5no2.deuterium.platform.toProjectFile
import com.c6h5no2.deuterium.ui.runner.RunnerModel
import com.c6h5no2.deuterium.util.dialog.AlertDialogResult
import com.c6h5no2.deuterium.util.dialog.DialogModel
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*


private val logger = mu.KotlinLogging.logger {}

class MainModel {
    fun getTitle(): String {
        val appName = "Deuterium"
        if (editorState == EditorState.EMPTY)
            return appName
        val fileName = if (isNewFile || currentFile == null) "untitled" else currentFile!!.name
        val modified = if (editorState == EditorState.MODIFIED) "* " else ""
        val running = if (runnerState == RunnerState.RUNNING) "> " else ""
        return "$modified$running$fileName - $appName"
    }


    lateinit var appExitFunc: () -> Unit

    suspend fun requestExitApp() {
        if (askToSaveFile())
            appExitFunc()
    }


    // ------- dialogs -------

    lateinit var dialogs: DialogModel


    // ------- editor -------

    lateinit var codeViewer: CodeViewer

    private val currentFile get() = codeViewer.editors.active?.file?.jvmFile

    var editorState by mutableStateOf(EditorState.EMPTY)
        private set


    private fun openFileToEditor(file: JbFile) {
        logger.info { "Open file ${file.jvmFile.absolutePath}" }
        codeViewer.editors.open(file)
        codeViewer.editors.active?.onModified = { editorState = EditorState.MODIFIED }
    }


    suspend fun requestOpenFile(file: JbFile) {
        if (!file.jvmFile.isFile)
            return
        if (runnerState != RunnerState.STOPPED)
            return
        if (editorState == EditorState.MODIFIED)
            if (!askToSaveFile())
                return
        openFileToEditor(file)
        isNewFile = false
        editorState = EditorState.LOADED
    }


    suspend fun requestOpenFile() {
        if (runnerState != RunnerState.STOPPED)
            return
        if (!askToSaveFile())
            return
        val path = dialogs.openResultDeferred.awaitResult()
        val file = path?.toFile()
        if (file == null || !file.isFile)
            return
        openFileToEditor(file.toProjectFile())
        isNewFile = false
        editorState = EditorState.LOADED
    }


    private fun saveEditorToFile(file: File): Boolean {
        val editor = codeViewer.editors.active ?: return false
        if (!editor.file.jvmFile.isFile) return false
        val text = editor.lines?.content?.text ?: return false
        logger.info { "Saving file to ${file.absolutePath}" }
        try {
            val tmp = newTempFile()
            tmp.writeText(text, Charsets.UTF_8)
            Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
            logger.info { "File saved to ${file.absolutePath}" }
            return true
        } catch (e: IOException) {
            logger.error { "File failed to save because $e" }
        }
        return false
    }


    /** @return `true` if the file has been successfully saved. `false` otherwise. */
    suspend fun requestSaveFile(): Boolean {
        if (runnerState != RunnerState.STOPPED)
            return false
        if (editorState != EditorState.MODIFIED)
            return true
        val file = if (isNewFile) {
            val path = dialogs.saveResultDeferred.awaitResult()
            path?.toFile()
        } else {
            currentFile
        }
        if (file == null || (file.exists() && !file.isFile))
            return false
        if (saveEditorToFile(file)) {
            if (isNewFile)
                openFileToEditor(file.toProjectFile())
            isNewFile = false
            editorState = EditorState.LOADED
            return true
        }
        return false
    }


    /** @return `true` if the file saving request has been properly handled. `false` otherwise. */
    private suspend fun askToSaveFile(): Boolean {
        if (runnerState != RunnerState.STOPPED)
            return false
        if (editorState != EditorState.MODIFIED)
            return true
        return when (dialogs.askToSaveResultDeferred.awaitResult()) {
            AlertDialogResult.Yes -> requestSaveFile()
            AlertDialogResult.No -> true
            AlertDialogResult.Cancel -> false
        }
    }


    private var isNewFile by mutableStateOf(false)

    suspend fun requestNewFile() {
        if (runnerState != RunnerState.STOPPED)
            return
        if (!askToSaveFile())
            return
        val tmp = newTempFile()
        openFileToEditor(tmp.toProjectFile())
        isNewFile = true
        editorState = EditorState.MODIFIED
    }


    private fun newTempFile() = File.createTempFile("deuterium-${UUID.randomUUID()}", ".kts")


    enum class EditorState {
        EMPTY, LOADED, MODIFIED
    }


    // ------- runner -------

    lateinit var runner: RunnerModel

    val runnerState get() = if (runner.isRunning) RunnerState.RUNNING else RunnerState.STOPPED

    suspend fun runScript() {
        if (runnerState != RunnerState.STOPPED)
            return
        if (editorState != EditorState.LOADED)
            if (!askToSaveFile() || isNewFile)
                return
        // else: run either the saved script or the original one as user's intent  
        val file = currentFile ?: return
        runner.runOnce(file)
    }

    enum class RunnerState {
        STOPPED, RUNNING
    }
}
