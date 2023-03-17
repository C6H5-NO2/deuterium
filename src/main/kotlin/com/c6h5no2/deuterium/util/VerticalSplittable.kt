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

package com.c6h5no2.deuterium.util

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.c6h5no2.deuterium.platform.cursorForHorizontalResize
import com.c6h5no2.deuterium.ui.common.AppTheme

@Composable
fun VerticalSplittable(
    modifier: Modifier,
    splitterState: SplitterState,
    onResize: (delta: Dp) -> Unit,
    children: @Composable () -> Unit
) = Layout({
    children()
    VerticalSplitter(splitterState, onResize)
}, modifier, measurePolicy = { measurables, constraints ->
    require(measurables.size == 3)

    val firstPlaceable = measurables[0].measure(constraints.copy(minWidth = 0))
    val secondWidth = constraints.maxWidth - firstPlaceable.width
    val secondPlaceable = measurables[1].measure(
        Constraints(
            minWidth = secondWidth,
            maxWidth = secondWidth,
            minHeight = constraints.maxHeight,
            maxHeight = constraints.maxHeight
        )
    )
    val splitterPlaceable = measurables[2].measure(constraints)
    layout(constraints.maxWidth, constraints.maxHeight) {
        firstPlaceable.place(0, 0)
        secondPlaceable.place(firstPlaceable.width, 0)
        splitterPlaceable.place(firstPlaceable.width, 0)
    }
})

class SplitterState {
    var isResizing by mutableStateOf(false)
    var isResizeEnabled by mutableStateOf(false)
}

@Composable
fun VerticalSplitter(
    splitterState: SplitterState,
    onResize: (delta: Dp) -> Unit,
    color: Color = AppTheme.colors.backgroundDark
) = Box {
    val density = LocalDensity.current
    Box(
        Modifier
            .width(8.dp)
            .fillMaxHeight()
            .run {
                if (splitterState.isResizeEnabled) {
                    this.draggable(
                        state = rememberDraggableState {
                            with(density) {
                                onResize(it.toDp())
                            }
                        },
                        orientation = Orientation.Horizontal,
                        startDragImmediately = true,
                        onDragStarted = { splitterState.isResizing = true },
                        onDragStopped = { splitterState.isResizing = false }
                    ).cursorForHorizontalResize()
                } else {
                    this
                }
            }
    )

    Box(
        Modifier
            .width(1.dp)
            .fillMaxHeight()
            .background(color)
    )
}
