import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.c6h5no2.deuterium.platform.HomeFolder
import com.c6h5no2.deuterium.ui.CodeViewer
import com.c6h5no2.deuterium.ui.MainModel
import com.c6h5no2.deuterium.ui.MainView
import com.c6h5no2.deuterium.ui.common.Settings
import com.c6h5no2.deuterium.ui.editor.Editors
import com.c6h5no2.deuterium.ui.filetree.FileTree
import com.c6h5no2.deuterium.ui.menuBar
import com.c6h5no2.deuterium.ui.runner.RunnerModel
import com.c6h5no2.deuterium.util.dialog.DialogModel
import com.c6h5no2.deuterium.util.dialog.FileDialog
import com.c6h5no2.deuterium.util.dialog.YesNoCancelDialog


fun main() = application {
    val mainModel = remember {
        MainModel().also { mainModel ->
            val editors = Editors()
            val viewer = CodeViewer(
                editors = editors,
                fileTree = FileTree(HomeFolder, opener = { mainModel.requestOpenFile(it) }),
                settings = Settings()
            )
            mainModel.codeViewer = viewer
            val runner = RunnerModel()
            runner.onErrorClick = { row, col -> editors.active?.moveCursorTo(row, col) }
            mainModel.runner = runner
            val dialogs = DialogModel()
            mainModel.dialogs = dialogs
        }
    }

    val windowState = remember { WindowState(WindowPlacement.Maximized) }

    Window(
        onCloseRequest = this::exitApplication,
        title = mainModel.getTitle(),
        state = windowState,
        icon = BitmapPainter(useResource("ic_launcher.png", ::loadImageBitmap)),
    ) {
        MainView(mainModel)
        menuBar(mainModel)
        dialogWindows(mainModel.dialogs)
    }
}


@Composable
fun FrameWindowScope.dialogWindows(model: DialogModel) {
    if (model.openResultDeferred.isAwaiting)
        FileDialog("Open file", isLoad = true, onResult = { model.openResultDeferred.onResult(it) })

    if (model.saveResultDeferred.isAwaiting)
        FileDialog("Save file", isLoad = false, onResult = { model.saveResultDeferred.onResult(it) })

    if (model.askToSaveResultDeferred.isAwaiting)
        YesNoCancelDialog(
            "Save changes?",
            message = "File modified, save changes?",
            onResult = { model.askToSaveResultDeferred.onResult(it) }
        )
}


private fun MainModel.getTitle(): String {
    val appName = "Deuterium"
    if (editorState == MainModel.EditorState.EMPTY)
        return appName
    val fileName = if (currentFile == null) "untitled" else currentFile!!.name
    val modified = if (editorState == MainModel.EditorState.MODIFIED) "* " else ""
    val running = if (runnerState == MainModel.RunnerState.RUNNING) "> " else ""
    return "$modified$running$fileName - $appName"
}
