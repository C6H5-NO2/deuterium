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

package com.c6h5no2.deuterium.ui.filetree

import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.c6h5no2.deuterium.platform.VerticalScrollbar
import com.c6h5no2.deuterium.util.withoutWidthConstraints

@Composable
fun FileTreeViewTabView() = Surface {
    Row(
        Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Explorer",
            color = LocalContentColor.current.copy(alpha = 0.60f),
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

@Composable
fun FileTreeView(model: FileTree) = Surface(
    modifier = Modifier.fillMaxSize()
) {
    with(LocalDensity.current) {
        Box {
            val scrollState = rememberLazyListState()

            LazyColumn(
                modifier = Modifier.fillMaxSize().withoutWidthConstraints(),
                state = scrollState
            ) {
                items(model.items.size) {
                    FileTreeItemView(14.sp, 14.sp.toDp() * 1.5f, model.items[it])
                }
            }

            VerticalScrollbar(
                Modifier.align(Alignment.CenterEnd),
                scrollState
            )
        }
    }
}

@Composable
private fun FileTreeItemView(fontSize: TextUnit, height: Dp, model: FileTree.Item) = Row(
    modifier = Modifier
        .wrapContentHeight()
        .clickable { model.open() }
        .padding(start = 24.dp * model.level)
        .height(height)
        .fillMaxWidth()
) {
    val interactionSource = remember { MutableInteractionSource() }
    val active by interactionSource.collectIsHoveredAsState()

    FileItemIcon(Modifier.align(Alignment.CenterVertically), model)
    Text(
        text = model.name,
        color = if (active) LocalContentColor.current.copy(alpha = 0.60f) else LocalContentColor.current,
        modifier = Modifier
            .align(Alignment.CenterVertically)
            .clipToBounds()
            .hoverable(interactionSource),
        softWrap = true,
        fontSize = fontSize,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1
    )
}

@Composable
private fun FileItemIcon(modifier: Modifier, model: FileTree.Item) = Box(modifier.size(24.dp).padding(4.dp)) {
    when (val type = model.type) {
        is FileTree.ItemType.Folder -> when {
            !type.canExpand -> Unit
            type.isExpanded -> Icon(
                Icons.Default.KeyboardArrowDown, contentDescription = null, tint = LocalContentColor.current
            )
            else -> Icon(
                Icons.Default.KeyboardArrowRight, contentDescription = null, tint = LocalContentColor.current
            )
        }
        is FileTree.ItemType.File -> when (type.ext) {
            "kt" -> Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF3E86A0))
            "kts" -> Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF3E86A0))
            else -> Icon(Icons.Default.TextSnippet, contentDescription = null, tint = Color(0xFF87939A))
        }
    }
}
