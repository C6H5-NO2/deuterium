package com.c6h5no2.deuterium.ui.runner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.c6h5no2.deuterium.ui.runconfig.RunConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.concurrent.thread
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


private val logger = mu.KotlinLogging.logger {}

class RunnerModel {
    var expandPanel: (() -> Unit)? = null

    val runConfig = RunConfig()

    var isRunning by mutableStateOf(false)
        private set

    var filename by mutableStateOf(".kts")
        private set

    var runnerOutputs = RunnerOutputs()
        private set

    var onErrorClick: ((row: Int, col: Int) -> Unit)? = null

    // no need to lock, the value is irrelevant
    var updateFlip by mutableStateOf(false)
        private set


    private fun prepareRun(file: File): ProcessBuilder {
        isRunning = true
        filename = file.name
        runnerOutputs = RunnerOutputs()
        expandPanel?.invoke()

        val script = file.absolutePath
        logger.info { "Run script $script" }

        val kotlinc = runConfig.kotlincPath.ifEmpty { "kotlinc" }
        val paramsStr = runConfig.cmdArgs
        val params = paramsStr.splitToSequence(' ', '\t', '\n', '\r', '\u000c').toList().toTypedArray()

        // val command = listOf("\"$kotlinc\"", "-version")
        val command = listOf("\"$kotlinc\"", "-script", "\"$script\"", "--", *params)

        val workingDirectory = file.parentFile

        return ProcessBuilder(command)
            .directory(workingDirectory)
            .redirectInput(ProcessBuilder.Redirect.PIPE)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
    }


    private suspend fun performRun(builder: ProcessBuilder) {
        logger.info { "Execute command ${builder.command().joinToString(separator = " ")}" }
        withContext(Dispatchers.IO) {
            try {
                val process = builder.start()
                process.outputStream.close()

                val ostream = thread {
                    readFromStream(process.inputStream, RunnerOutputType.OUTPUT_STREAM)
                }

                val estream = thread {
                    readFromStream(process.errorStream, RunnerOutputType.ERROR_STREAM)
                }

                while (process.isAlive) {
                    updateFlip = !updateFlip
                    Thread.sleep(1000)
                }

                val exitCode = process.waitFor()
                val msg = "Process finished with ${if (exitCode == 0) "" else "non-zero "}exit code $exitCode"
                logger.info { msg }
                ostream.join()
                estream.join()
                runnerOutputs.appendSegment("\n\n$msg\n\n", RunnerOutputType.PROCESS_INFO)
            } catch (e: IOException) {
                val msg = "Process failed with $e"
                logger.error { msg }
                runnerOutputs.appendSegment("\n\n$msg\n\n", RunnerOutputType.PROCESS_FATAL)
            } finally {
                updateFlip = !updateFlip
            }
        }
    }


    var progress = RunnerProgress(1)
        private set


    @OptIn(ExperimentalTime::class)
    suspend fun runScript(file: File) {
        if (isRunning)
            return

        check(runConfig.nRuns > 0)
        progress = RunnerProgress(runConfig.nRuns)

        val processBuilder = prepareRun(file)

        measureTime {
            performRun(processBuilder)
        }.also {
            progress.finishRun(1, it)
        }

        for (runIdx in 2..runConfig.nRuns) {
            measureTime {
                performRun(processBuilder)
            }.also {
                progress.finishRun(runIdx, it)
            }
        }

        isRunning = false
    }


    private fun readFromStream(istream: InputStream, type: RunnerOutputType) {
        val buffer = CharArray(1024)
        var charsRead: Int
        istream.reader(Charsets.UTF_8).use {
            while (true) {
                charsRead = it.read(buffer)
                logger.info { "Read $charsRead chars from stream to $type" }
                if (charsRead == -1)
                    break
                runnerOutputs.appendSegment(String(buffer, 0, charsRead), type)
                updateFlip = !updateFlip
            }
        }
    }
}
