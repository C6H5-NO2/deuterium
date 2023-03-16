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

import androidx.compose.runtime.mutableStateListOf
import com.c6h5no2.deuterium.platform.JbFile
import com.c6h5no2.deuterium.util.SingleSelection

class Editors {
    private val selection = SingleSelection()

    var editors = mutableStateListOf<Editor>()
        private set

    val active: Editor? get() = selection.selected as Editor?

    fun open(file: JbFile) {
        val editor = Editor(file)
        editor.selection = selection
        editor.close = { close(editor) }
        // only one editor
        if (editors.isNotEmpty())
            editors.forEach { it.close?.invoke() }
        editors.add(editor)
        editor.activate()
        assert(editors.size == 1)
    }

    private fun close(editor: Editor) {
        // todo: close file
        val index = editors.indexOf(editor)
        editors.remove(editor)
        if (editor.isActive) {
            selection.selected = editors.getOrNull(index.coerceAtMost(editors.lastIndex))
        }
    }
}
