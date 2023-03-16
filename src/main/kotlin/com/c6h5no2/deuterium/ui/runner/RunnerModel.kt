package com.c6h5no2.deuterium.ui.runner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader


private val logger = mu.KotlinLogging.logger {}

class RunnerModel {
    var expandPanel: (() -> Unit)? = null

    var isRunning by mutableStateOf(false)
        private set

    var filename by mutableStateOf(".kts")
        private set

    var runnerOutputs: RunnerOutputs = RunnerOutputs()
        private set

    suspend fun runOnce(file: File) {
        if (isRunning)
            return
        isRunning = true
        filename = file.name
        runnerOutputs = RunnerOutputs()
        expandPanel?.invoke()

        val script = file.absolutePath
        logger.info { "Run script $script" }
        // todo: set from config
        val workingDirectory = file.parentFile
        val kotlinc = "kotlinc"
        val paramsStr = ""
        val params = paramsStr.splitToSequence(' ', '\t', '\n', '\r', '\u000c').toList().toTypedArray()

        // val command = listOf("\"$kotlinc\"", "-version")
        val command = listOf("\"$kotlinc\"", "-script", "\"$script\"", "--", *params)
        logger.info { "Execute command ${command.joinToString(separator = " ")}" }
        val pb = ProcessBuilder(command)
            .directory(workingDirectory)
            .redirectInput(ProcessBuilder.Redirect.PIPE)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)

        withContext(Dispatchers.IO) {
            try {
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
                        runnerOutputs.appendSegment(text, RunnerOutputType.OUTPUT_STREAM)
                    }

                    val numCharError = errorReader.read(errorBuffer, 0, errorBuffer.size)
                    if (numCharError > 0) {
                        val text = String(errorBuffer, 0, numCharError)
                        runnerOutputs.appendSegment(text, RunnerOutputType.ERROR_STREAM)
                    }

                    delay(100)
                    if (!process.isAlive)
                        break
                }

                val exitCode = process.waitFor()
                val msg = "Process finished with ${if (exitCode == 0) "" else "non-zero "}exit code $exitCode"
                runnerOutputs.appendSegment("\n\n$msg\n\n", RunnerOutputType.PROCESS_INFO)
            } catch (e: IOException) {
                runnerOutputs.appendSegment("\n\nProcess failed with $e\n\n", RunnerOutputType.PROCESS_FATAL)
            } finally {
                isRunning = false
            }
        }
    }
}
