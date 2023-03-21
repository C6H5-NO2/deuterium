/*
   Copyright 2020-2021 JetBrains s.r.o. and and respective authors and developers.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.c6h5no2.deuterium.ui

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
import androidx.compose.material.Text
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
import com.c6h5no2.deuterium.ui.editor.EditorEmptyView
import com.c6h5no2.deuterium.ui.editor.EditorView
import com.c6h5no2.deuterium.ui.filetree.FileTreeView
import com.c6h5no2.deuterium.ui.filetree.FileTreeViewTabView
import com.c6h5no2.deuterium.ui.runner.RunnerModel
import com.c6h5no2.deuterium.ui.runner.runnerView
import com.c6h5no2.deuterium.util.SplitterState
import com.c6h5no2.deuterium.util.VerticalSplittable
import com.c6h5no2.deuterium.util.hsplit.LowerLayoutState
import com.c6h5no2.deuterium.util.hsplit.horizontalSplitLayout

@Composable
fun CodeViewerView(codeViewer: CodeViewer, runner: RunnerModel) {
    val panelState = remember { PanelState() }
    val lowerLayoutState = remember { LowerLayoutState() }
    runner.expandPanel = { lowerLayoutState.toggleExpand(true) }

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
                FileTreeView(codeViewer.fileTree)
            }
        }

        Box {
            if (codeViewer.editors.active != null) {
                horizontalSplitLayout(
                    lowerLayoutState,
                    Modifier.fillMaxSize(),
                    upperContent = {
                        Box {
                            Column(Modifier.align(Alignment.Center)) {
                                // EditorTabsView(model.editors)
                                EditorView(codeViewer.editors.active!!, codeViewer.settings)
                            }
                        }
                    },
                    lowerContent = {
                        Box {
                            Column(Modifier.align(Alignment.Center)) {
                                runnerView(runner, codeViewer.settings)
                                // StatusBar(codeViewer.settings)
                            }
                        }
                    },
                    lowerContentTitleBar = {
                        Text(
                            text =
                            "Output" +
                                if (runner.isRunning) {
                                    " > Running" +
                                        if (runner.runConfig.nRuns > 1) {
                                            " ${runner.progress}" +
                                                (if (runner.updateFlip) " " else "")  // force update ui
                                        } else
                                            ""
                                } else
                                    "",
                            modifier = Modifier
                                .padding(6.dp)
                                .align(Alignment.TopStart)
                                .clickable { lowerLayoutState.toggleExpand() }
                        )
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
    var isExpanded by mutableStateOf(false)
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
