import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch


fun main() = application {
    val scope = rememberCoroutineScope()

    val mainModel = remember {
        MainModel().also { mainModel ->
            mainModel.appExitFunc = ::exitApplication
            mainModel.codeViewer = CodeViewer(
                editors = Editors(),
                fileTree = FileTree(HomeFolder, opener = { scope.launch { mainModel.requestOpenFile(it) } }),
                settings = Settings()
            )
            mainModel.runner = RunnerModel().also { runner ->
                runner.onErrorClick = { row, col -> mainModel.codeViewer.editors.active?.moveCursorTo(row, col) }
            }
            mainModel.dialogs = DialogModel()
        }
    }

    val windowState = remember { WindowState(WindowPlacement.Maximized) }

    Window(
        onCloseRequest = { scope.launch { mainModel.requestExitApp() } },
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
