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

import com.c6h5no2.deuterium.platform.JbFile
import com.c6h5no2.deuterium.util.SingleSelection
import kotlinx.coroutines.CoroutineScope


private val logger = mu.KotlinLogging.logger {}

class Editor(
    val file: JbFile
) {
    val loader: ((scope: CoroutineScope) -> Boolean)

    var lines: Lines? = null
        private set

    var onModified: (() -> Unit)? = null

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

    class Line(val number: Int, val content: Content? = null)

    interface Lines {
        val lineNumberDigitCount: Int get() = size.toString().length
        val size: Int
        // operator fun get(index: Int): Line
        val content: Content
    }

    interface Content {
        var text: String
        val isCode: Boolean
    }

    init {
        loader = impl@{ scope ->
            logger.info { "Read file ${file.jvmFile.absolutePath}" }
            val textLines = try {
                file.readLines(scope)
            } catch (e: Throwable) {
                logger.error { e.stackTraceToString() }
                return@impl false
            }
            this.lines = object : Editor.Lines {
                override val size get() = textLines.size
                override val content = object : Editor.Content {
                    override var text: String
                        get() = textLines.getAllText()
                        set(value) {
                            if (text == value)
                                return
                            textLines.setAllText(value)
                            onModified?.invoke()
                        }
                    override val isCode: Boolean = file.name.endsWith(".kts", ignoreCase = true)
                }
            }
            return@impl true
        }
    }
}
