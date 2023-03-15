import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import com.c6h5no2.deuterium.model.MainModel
import com.c6h5no2.deuterium.view.menuBar
import org.jetbrains.codeviewer.ui.MainView


fun main() = singleWindowApplication(
    title = "Deuterium",
    state = WindowState(WindowPlacement.Maximized),
    icon = BitmapPainter(useResource("ic_launcher.png", ::loadImageBitmap)),
) {
    val mainModel = remember { MainModel() }
    menuBar(mainModel)
    MainView(mainModel)
}
