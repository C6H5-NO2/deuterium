package org.jetbrains.codeviewer.ui.editor

import androidx.compose.runtime.mutableStateListOf
import org.jetbrains.codeviewer.platform.File
import org.jetbrains.codeviewer.util.SingleSelection

class Editors {
    private val selection = SingleSelection()

    var editors = mutableStateListOf<Editor>()
        private set

    val active: Editor? get() = selection.selected as Editor?

    fun open(file: File) {
        val editor = Editor(file)
        editor.selection = selection
        editor.close = {
            close(editor)
        }
        // only one editor
        if (editors.isNotEmpty()) {
            editors.forEach { e ->
                val close = e.close
                if (close != null)
                    close()
            }
        }
        editors.add(editor)
        editor.activate()
    }

    private fun close(editor: Editor) {
        val index = editors.indexOf(editor)
        editors.remove(editor)
        if (editor.isActive) {
            selection.selected = editors.getOrNull(index.coerceAtMost(editors.lastIndex))
        }
    }
}
