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

package com.c6h5no2.deuterium.ui.statusbar

import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import org.jetbrains.codeviewer.ui.common.Settings

private val MinFontSize = 6.sp
private val MaxFontSize = 40.sp

@Composable
fun StatusBar(settings: Settings) = Box(
    Modifier
        .height(32.dp)
        .fillMaxWidth()
        .padding(4.dp)
) {
    Row(Modifier.fillMaxHeight().align(Alignment.CenterEnd)) {
        Text(
            text = "Text size",
            modifier = Modifier.align(Alignment.CenterVertically),
            color = LocalContentColor.current.copy(alpha = 0.60f),
            fontSize = 12.sp
        )

        Spacer(Modifier.width(8.dp))

        CompositionLocalProvider(LocalDensity provides LocalDensity.current.scale(0.5f)) {
            Slider(
                (settings.fontSize - MinFontSize) / (MaxFontSize - MinFontSize),
                onValueChange = { settings.fontSize = lerp(MinFontSize, MaxFontSize, it) },
                modifier = Modifier.width(240.dp).align(Alignment.CenterVertically)
            )
        }
    }
}

private fun Density.scale(scale: Float) = Density(density * scale, fontScale * scale)
private operator fun TextUnit.minus(other: TextUnit) = (value - other.value).sp
private operator fun TextUnit.div(other: TextUnit) = value / other.value
