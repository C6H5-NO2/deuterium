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

package com.c6h5no2.deuterium.ui.editor

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.c6h5no2.deuterium.platform.JbFile
import com.c6h5no2.deuterium.util.EmptyTextLines
import com.c6h5no2.deuterium.util.SingleSelection
import kotlinx.coroutines.CoroutineScope


class Editor(
    val file: JbFile,
    val lines: (backgroundScope: CoroutineScope) -> Lines,
) {
    var close: (() -> Unit)? = null
    lateinit var selection: SingleSelection

    val fileName: String
        get() = file.name

    val isActive: Boolean
        get() = selection.selected === this

    fun activate() {
        selection.selected = null
        require(selection.selected == null)
        selection.selected = this
    }

    class Line(val number: Int, val content: Content)

    interface Lines {
        val lineNumberDigitCount: Int get() = size.toString().length
        val size: Int
        operator fun get(index: Int): Line
    }

    class Content(val value: State<String>, val isCode: Boolean)
}

fun Editor(file: JbFile) = Editor(
    file = file
) { backgroundScope ->
    val textLines = try {
        file.readLines(backgroundScope)
    } catch (e: Throwable) {
        e.printStackTrace()
        EmptyTextLines
    }
    val isCode = file.name.endsWith(".kts", ignoreCase = true)

    fun content(index: Int): Editor.Content {
        val text = textLines.get(index)
        val state = mutableStateOf(text)
        return Editor.Content(state, isCode)
    }

    object : Editor.Lines {
        override val size get() = textLines.size

        override fun get(index: Int) = Editor.Line(
            number = index + 1,
            content = content(index)
        )
    }
}
