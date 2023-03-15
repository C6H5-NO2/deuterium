package com.c6h5no2.deuterium

import androidx.compose.runtime.remember
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import com.c6h5no2.deuterium.model.MainModel
import com.c6h5no2.deuterium.view.mainView
import com.c6h5no2.deuterium.view.menuBar


fun main() = singleWindowApplication(
    title = "Deuterium",
    state = WindowState(WindowPlacement.Maximized)
) {
    val mainModel = remember { MainModel() }
    menuBar(mainModel)
    mainView()
}
