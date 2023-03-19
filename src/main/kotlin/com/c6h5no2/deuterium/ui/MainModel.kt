package com.c6h5no2.deuterium.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.c6h5no2.deuterium.platform.JbFile
import com.c6h5no2.deuterium.platform.toProjectFile
import com.c6h5no2.deuterium.ui.runner.RunnerModel
import com.c6h5no2.deuterium.util.dialog.AlertDialogResult
import com.c6h5no2.deuterium.util.dialog.DialogModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.*


private val logger = mu.KotlinLogging.logger {}

class MainModel {
    val currentFile get() = codeViewer.editors.active?.file?.jvmFile


    // ------- dialogs -------

    lateinit var dialogs: DialogModel


    // ------- editor -------

    lateinit var codeViewer: CodeViewer

    var editorState by mutableStateOf(EditorState.EMPTY)
        private set


    private fun openFile(file: JbFile) {
        logger.info { "Open file ${file.jvmFile.absolutePath}" }
        codeViewer.editors.open(file)
        codeViewer.editors.active?.onModified = { editorState = EditorState.MODIFIED }
        editorState = EditorState.LOADED
    }


    @OptIn(DelicateCoroutinesApi::class)
    fun requestOpenFile(file: JbFile) {
        if (!file.jvmFile.isFile)
            return
        if (editorState == EditorState.MODIFIED) {
            GlobalScope.launch {
                if (askToSaveFile())
                    openFile(file)
            }
        } else
            openFile(file)
    }


    suspend fun requestOpenFile() {
        if (!askToSaveFile())
            return
        val path = dialogs.openResultDeferred.awaitResult()
        val file = path?.toFile()
        if (file == null || !file.isFile) return
        editorState = EditorState.EMPTY
        openFile(file.toProjectFile())
    }


    private fun saveFile(): Boolean {
        val editor = codeViewer.editors.active ?: return false
        if (!editor.file.jvmFile.isFile) return false
        val text = editor.lines?.content?.text ?: return false
        logger.info { "Saving file to ${editor.file.jvmFile.absolutePath}" }
        try {
            val tmp = File.createTempFile("deuterium-${UUID.randomUUID()}", null)
            tmp.writeText(text, Charsets.UTF_8)
            Files.move(tmp.toPath(), editor.file.jvmFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            logger.info { "File saved to ${editor.file.jvmFile.absolutePath}" }
            return true
        } catch (e: IOException) {
            logger.error { "File failed to save because $e" }
        }
        return false
    }


    /** @return `true` if the file has been successfully saved. `false` otherwise. */
    suspend fun requestSaveFile(): Boolean {
        if (editorState != EditorState.MODIFIED)
            return true
        val isNew = false  // todo: check new state
        val file = if (isNew) {
            val path = dialogs.saveResultDeferred.awaitResult()
            path?.toFile()
        } else {
            currentFile
        }
        if (file == null || !file.isFile)
            return false
        if (saveFile()) {
            // todo: update editor if new
            editorState = EditorState.LOADED
            return true
        }
        return false
    }


    /** @return `true` if the file saving request has been properly handled. `false` otherwise. */
    suspend fun askToSaveFile(): Boolean {
        if (editorState != EditorState.MODIFIED)
            return true
        return when (dialogs.askToSaveResultDeferred.awaitResult()) {
            AlertDialogResult.Yes -> requestSaveFile()
            AlertDialogResult.No -> true
            AlertDialogResult.Cancel -> false
        }
    }


    enum class EditorState {
        EMPTY, LOADED, MODIFIED
    }


    // ------- runner -------

    lateinit var runner: RunnerModel

    val runnerState get() = if (runner.isRunning) RunnerState.RUNNING else RunnerState.STOPPED

    suspend fun runOnce() {
        if (runnerState != RunnerState.STOPPED)
            return
        if (editorState != EditorState.LOADED)
            if (!askToSaveFile())
                return
        // else: run either the saved script or the original one as user's intent  
        val file = currentFile ?: return
        runner.runOnce(file)
    }

    enum class RunnerState {
        STOPPED, RUNNING
    }
}
