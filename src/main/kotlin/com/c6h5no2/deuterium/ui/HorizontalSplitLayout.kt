package com.c6h5no2.deuterium.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp


@Composable
fun horizontalSplitLayout(
    modifier: Modifier,
    upperContent: @Composable () -> Unit,
    lowerContent: @Composable () -> Unit,
    lowerLayoutInit: LowerLayoutInit? = null
) {
    val lowerLayoutState = if (lowerLayoutInit == null) remember { LowerLayoutState() } else remember { LowerLayoutState(lowerLayoutInit) }
    Layout(
        content = {
            Box { upperContent() }
            lowerOverlay(Modifier.height(lowerLayoutState.height()).fillMaxWidth(), lowerLayoutState) { lowerContent() }
            horizontalSplitter(lowerLayoutState.splitterState, onResize = {
                val newHeight = lowerLayoutState.expandedHeight - it
                lowerLayoutState.expandedHeight = newHeight.coerceIn(lowerLayoutState.minExpandedHeight, lowerLayoutState.maxExpandedHeight)
            })
        },
        modifier = modifier,
        measurePolicy = { measurables, constraints ->
            require(measurables.size == 3)
            val heightLower = lowerLayoutState.height()
                .coerceAtLeast(if (lowerLayoutState.isExpanded) lowerLayoutState.minExpandedHeight else lowerLayoutState.collapsedHeight)
                .coerceAtMost(minOf(constraints.maxHeight.toDp(), lowerLayoutState.maxExpandedHeight))
                .roundToPx()
            val upper = measurables[0].measure(
                Constraints(
                    minWidth = constraints.maxWidth, maxWidth = constraints.maxWidth,
                    minHeight = constraints.maxHeight - heightLower, maxHeight = constraints.maxHeight - heightLower
                )
            )
            val lower = measurables[1].measure(
                Constraints(
                    minWidth = constraints.maxWidth, maxWidth = constraints.maxWidth,
                    minHeight = heightLower, maxHeight = heightLower
                )
            )
            val splitter = measurables[2].measure(constraints)
            layout(constraints.maxWidth, constraints.maxHeight) {
                upper.place(0, 0)
                lower.place(0, upper.height)
                splitter.place(0, upper.height)
            }
        }
    )
}


data class LowerLayoutInit(
    val isExpanded: Boolean,
    val expandedHeight: Dp,
    val minExpandedHeight: Dp,
    val maxExpandedHeight: Dp,
    val collapsedHeight: Dp
)


private class LowerLayoutState(
    val minExpandedHeight: Dp = 90.dp,
    val maxExpandedHeight: Dp = 800.dp,
    val collapsedHeight: Dp = 30.dp,
    val splitterState: SplitterState = SplitterState()
) {
    var isExpanded by mutableStateOf(false)
    var expandedHeight by mutableStateOf(300.dp)

    constructor(init: LowerLayoutInit) : this(init.minExpandedHeight, init.maxExpandedHeight, init.collapsedHeight) {
        isExpanded = init.isExpanded
        expandedHeight = init.expandedHeight
        splitterState.isResizeEnabled = isExpanded
    }

    fun height() = if (isExpanded) expandedHeight else collapsedHeight
}


@Composable
private fun lowerOverlay(
    modifier: Modifier,
    state: LowerLayoutState,
    content: @Composable () -> Unit
) {
    val alpha = if (state.isExpanded) 1f else 0f
    Box(modifier) {
        Box(Modifier.fillMaxSize().graphicsLayer(alpha = alpha)) { content() }
        Icon(
            imageVector = if (state.isExpanded) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
            contentDescription = if (state.isExpanded) "Collapse" else "Expand",
            modifier = Modifier.width(30.dp).padding(6.dp).align(Alignment.TopEnd).clickable {
                state.isExpanded = !state.isExpanded
                state.splitterState.isResizeEnabled = state.isExpanded
            }
        )
    }
}
