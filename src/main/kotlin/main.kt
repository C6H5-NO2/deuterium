import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
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
import com.c6h5no2.deuterium.util.FileDialog


fun main() = application {
    val mainModel = remember {
        MainModel().also { mainModel ->
            val editors = Editors()
            val viewer = CodeViewer(
                editors = editors,
                fileTree = FileTree(HomeFolder, opener = { mainModel.openFile(it) }),
                settings = Settings()
            )
            mainModel.codeViewer = viewer
            val runner = RunnerModel()
            mainModel.runner = runner
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

        if (mainModel.openDialog.isAwaiting)
            FileDialog("Open file", isLoad = true, onResult = { mainModel.openDialog.onResult(it) })
    }
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
