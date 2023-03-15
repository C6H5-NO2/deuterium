package com.c6h5no2.deuterium.ui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.awt.Cursor


internal class SplitterState {
    var isResizing by mutableStateOf(false)
    var isResizeEnabled by mutableStateOf(false)
}


@Composable
internal fun horizontalSplitter(
    splitterState: SplitterState,
    onResize: (delta: Dp) -> Unit
) = Box {
    val density = LocalDensity.current
    Box(Modifier.height(8.dp).fillMaxWidth().run {
        if (splitterState.isResizeEnabled) {
            this.draggable(
                state = rememberDraggableState { with(density) { onResize(it.toDp()) } },
                orientation = Orientation.Vertical,
                startDragImmediately = true,
                onDragStarted = { splitterState.isResizing = true },
                onDragStopped = { splitterState.isResizing = false }
            ).pointerHoverIcon(PointerIcon(Cursor(Cursor.N_RESIZE_CURSOR)))
        } else {
            this
        }
    })
    // Box(Modifier.height(1.dp).fillMaxWidth().background(AppTheme.colors.backgroundDark))
    Box(Modifier.height(1.dp).fillMaxWidth())
}
