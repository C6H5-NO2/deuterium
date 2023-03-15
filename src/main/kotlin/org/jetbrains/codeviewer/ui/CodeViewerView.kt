package org.jetbrains.codeviewer.ui

import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.c6h5no2.deuterium.ui.horizontalSplitLayout
import com.c6h5no2.deuterium.view.runPanel
import org.jetbrains.codeviewer.ui.editor.EditorEmptyView
import org.jetbrains.codeviewer.ui.editor.EditorTabsView
import org.jetbrains.codeviewer.ui.editor.EditorView
import org.jetbrains.codeviewer.ui.filetree.FileTreeView
import org.jetbrains.codeviewer.ui.filetree.FileTreeViewTabView
import org.jetbrains.codeviewer.ui.statusbar.StatusBar
import org.jetbrains.codeviewer.util.SplitterState
import org.jetbrains.codeviewer.util.VerticalSplittable

@Composable
fun CodeViewerView(model: CodeViewer) {
    val panelState = remember { PanelState() }

    val animatedSize = if (panelState.splitter.isResizing) {
        panelState.actualSize()
    } else {
        animateDpAsState(
            panelState.actualSize(),
            SpringSpec(stiffness = StiffnessLow)
        ).value
    }

    VerticalSplittable(
        Modifier.fillMaxSize(),
        panelState.splitter,
        onResize = {
            val newSize = panelState.expandedSize + it
            panelState.expandedSize = newSize.coerceIn(panelState.expandedSizeMin, panelState.expandedSizeMax)
        }
    ) {
        ResizablePanel(Modifier.width(animatedSize).fillMaxHeight(), panelState) {
            Column {
                FileTreeViewTabView()
                FileTreeView(model.fileTree)
            }
        }

        Box {
            if (model.editors.active != null) {
                horizontalSplitLayout(
                    Modifier.fillMaxSize(),
                    upperContent = {
                        Box {
                            Column(Modifier.align(Alignment.Center)) {
                                // EditorTabsView(model.editors)
                                EditorView(model.editors.active!!, model.settings)
                            }
                        }
                    },
                    lowerContent = {
                        Box {
                            Column(Modifier.align(Alignment.Center)) {
                                runPanel()
                                StatusBar(model.settings)
                            }
                        }
                    }
                )
            } else {
                EditorEmptyView()
            }
        }
    }
}

private class PanelState {
    val collapsedSize = 24.dp
    var expandedSize by mutableStateOf(300.dp)
    val expandedSizeMin = 90.dp
    val expandedSizeMax = 700.dp
    var isExpanded by mutableStateOf(true)
    val splitter = SplitterState()

    fun actualSize() = if (isExpanded) expandedSize else collapsedSize
}

@Composable
private fun ResizablePanel(
    modifier: Modifier,
    state: PanelState,
    content: @Composable () -> Unit,
) {
    val alpha by animateFloatAsState(if (state.isExpanded) 1f else 0f, SpringSpec(stiffness = StiffnessLow))

    Box(modifier) {
        Box(Modifier.fillMaxSize().graphicsLayer(alpha = alpha)) {
            content()
        }

        Icon(
            if (state.isExpanded) Icons.Default.ArrowBack else Icons.Default.ArrowForward,
            contentDescription = if (state.isExpanded) "Collapse" else "Expand",
            tint = LocalContentColor.current,
            modifier = Modifier
                .padding(top = 4.dp)
                .width(24.dp)
                .clickable {
                    state.isExpanded = !state.isExpanded
                    state.splitter.isResizeEnabled = state.isExpanded
                }
                .padding(4.dp)
                .align(Alignment.TopEnd)
        )
    }
}
