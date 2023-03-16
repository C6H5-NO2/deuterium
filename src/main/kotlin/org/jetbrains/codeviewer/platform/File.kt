package org.jetbrains.codeviewer.platform

import kotlinx.coroutines.CoroutineScope
import org.jetbrains.codeviewer.util.TextLines

val HomeFolder: File get() = java.io.File(System.getProperty("user.home")).toProjectFile()

interface File {
    val jvmFile: java.io.File

    val name: String
    val isDirectory: Boolean
    val children: List<File>
    val hasChildren: Boolean

    fun readLines(scope: CoroutineScope): TextLines
}
