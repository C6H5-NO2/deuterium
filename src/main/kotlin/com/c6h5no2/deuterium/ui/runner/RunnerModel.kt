package com.c6h5no2.deuterium.ui.runner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.concurrent.thread


private val logger = mu.KotlinLogging.logger {}

class RunnerModel {
    var expandPanel: (() -> Unit)? = null

    var isRunning by mutableStateOf(false)
        private set

    var filename by mutableStateOf(".kts")
        private set

    var runnerOutputs: RunnerOutputs = RunnerOutputs()
        private set

    // no need to lock, the value is irrelevant
    var updateFlip by mutableStateOf(false)
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

                thread {
                    readFromStream(process.inputStream, RunnerOutputType.OUTPUT_STREAM)
                }

                thread {
                    readFromStream(process.errorStream, RunnerOutputType.ERROR_STREAM)
                }

                while (process.isAlive) {
                    Thread.sleep(100)
                    updateFlip = !updateFlip
                }

                val exitCode = process.waitFor()
                val msg = "Process finished with ${if (exitCode == 0) "" else "non-zero "}exit code $exitCode"
                logger.info { msg }
                runnerOutputs.appendSegment("\n\n$msg\n\n", RunnerOutputType.PROCESS_INFO)
            } catch (e: IOException) {
                val msg = "Process failed with $e"
                logger.info { msg }
                runnerOutputs.appendSegment("\n\n$msg\n\n", RunnerOutputType.PROCESS_FATAL)
            } finally {
                isRunning = false
            }
        }
    }


    private fun readFromStream(istream: InputStream, type: RunnerOutputType) {
        val buffer = CharArray(1024)
        var bytesRead: Int
        while (true) {
            bytesRead = istream.reader().read(buffer)
            logger.info { "Read $bytesRead bytes with $updateFlip" }
            updateFlip = !updateFlip
            if (bytesRead == -1)
                break
            runnerOutputs.appendSegment(String(buffer, 0, bytesRead), type)
        }
    }
}
