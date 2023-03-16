package com.c6h5no2.deuterium.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*


enum class EditorState {
    EMPTY, LOADED, MODIFIED
}


class MainModel {
    // val windowState = WindowState()


    var editorState by mutableStateOf(EditorState.EMPTY)
        private set

    suspend fun openFile() {
        // todo: check save state
        val path = "../kt-scripts/hello.kt"
        // todo: check path
        println("try open")
        // try {
        //
        // }
    }


    lateinit var getCurrentFile: (() -> File?)

    suspend fun runOnce() {
        // todo: check save state
        val file = getCurrentFile()
        if (file == null) {
            // todo: log gracefully
            return
        }
        // todo: expand panel
        println(file.absolutePath)
        val script = file.absolutePath
        // todo: set from config
        val workingDirectory = file.parentFile
        val kotlinc = "kotlinc"
        val paramsStr = ""
        val params = StringTokenizer(paramsStr).run {
            val tokens = mutableListOf<String>()
            while (hasMoreTokens())
                tokens.add(nextToken())
            tokens.toList().toTypedArray()
        }

        // val pb = ProcessBuilder("\"$kotlinc\"", "-version")
        val pb = ProcessBuilder("\"$kotlinc\"", "-script", "\"$script\"", "--", *params)
            .directory(workingDirectory)
            .redirectInput(ProcessBuilder.Redirect.PIPE)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)

        withContext(Dispatchers.IO) {
            val process = pb.start()
            process.outputStream.close()
            val outputReader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))

            val outputBuffer = CharArray(1024)
            val errorBuffer = CharArray(1024)

            while (true) {
                val numCharOutput = outputReader.read(outputBuffer, 0, outputBuffer.size)
                if (numCharOutput > 0) {
                    val text = String(outputBuffer, 0, numCharOutput)
                    print(text)
                }

                val numCharError = errorReader.read(errorBuffer, 0, errorBuffer.size)
                if (numCharError > 0) {
                    val text = String(errorBuffer, 0, numCharError)
                    print("[Err]$text[/Err]")
                }

                delay(100)
                if (!process.isAlive)
                    break
            }

            val exitCode = process.waitFor()

            println("Process finished with ${if (exitCode == 0) "" else "non-zero "}exit code $exitCode")
        }
    }
}
