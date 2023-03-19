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

package com.c6h5no2.deuterium.platform

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.c6h5no2.deuterium.util.TextLines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FilenameFilter

fun java.io.File.toProjectFile(): JbFile = object : JbFile {
    override val jvmFile get() = this@toProjectFile

    override val name: String get() = this@toProjectFile.name

    override val isDirectory: Boolean get() = this@toProjectFile.isDirectory

    override val children: List<JbFile>
        get() = this@toProjectFile
            .listFiles(FilenameFilter { _, name -> !name.startsWith(".") })
            .orEmpty()
            .map { it.toProjectFile() }

    override val hasChildren: Boolean
        get() = isDirectory && (listFiles()?.size ?: 0) > 0


    override fun readLines(scope: CoroutineScope): TextLines {
        var text by mutableStateOf("")
        var lineCount by mutableStateOf(0)
        scope.launch(Dispatchers.IO) {
            val stream = this@toProjectFile.inputStream()
            // the script file is relatively small
            text = stream.bufferedReader().use { it.readText() }
            lineCount = 1 + text.count { it == '\n' }
        }
        return object : TextLines {
            override val size: Int
                get() {
                    if (lineCount == -1)
                        lineCount = 1 + text.count { it == '\n' }
                    return lineCount
                }

            override fun getAllText(): String = text

            override fun setAllText(value: String) {
                text = value
                lineCount = -1
            }
        }
    }
}
