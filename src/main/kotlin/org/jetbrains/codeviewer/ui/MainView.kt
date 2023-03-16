package org.jetbrains.codeviewer.ui

import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.c6h5no2.deuterium.model.MainModel
import org.jetbrains.codeviewer.platform.HomeFolder
import org.jetbrains.codeviewer.ui.common.AppTheme
import org.jetbrains.codeviewer.ui.common.Settings
import org.jetbrains.codeviewer.ui.editor.Editors
import org.jetbrains.codeviewer.ui.filetree.FileTree

@Composable
fun MainView(mainModel: MainModel) {
    val codeViewer = remember {
        val editors = Editors()

        CodeViewer(
            editors = editors,
            fileTree = FileTree(HomeFolder, editors),
            settings = Settings()
        )
    }

    mainModel.getCurrentFile = { codeViewer.editors.active?.file?.jvmFile }

    DisableSelection {
        MaterialTheme(
            colors = AppTheme.colors.material
        ) {
            Surface {
                CodeViewerView(codeViewer)
            }
        }
    }
}
