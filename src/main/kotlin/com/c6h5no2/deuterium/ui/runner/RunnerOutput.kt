package com.c6h5no2.deuterium.ui.runner

import java.util.*
import java.util.concurrent.locks.ReentrantLock


class RunnerOutputs {
    val size: Int get() = lines.size

    operator fun get(index: Int): String =
        if (index < lines.size)
            lines[index].segments.joinToString(separator = "") { it.str }
        else
            ""

    fun getTyped(index: Int): List<RunnerOutputSegment> =
        if (index < lines.size)
            Collections.unmodifiableList(lines[index].segments)
        else
            listOf()


    private val lock = ReentrantLock()
    private val lines = ArrayList<RunnerOutputLine>(16)

    fun appendSegment(seg: String, type: RunnerOutputType) {
        lock.lock()
        try {
            if (lines.size == 0)
                lines.add(RunnerOutputLine())
            seg.splitToSequence('\n').forEachIndexed { idx, it ->
                if (idx > 0)
                    lines.add(RunnerOutputLine())
                if (it.isNotEmpty())
                    lines.last().segments.add(RunnerOutputSegment(it.replace("\t", "    "), type))
            }
        } finally {
            lock.unlock()
        }
    }
}


data class RunnerOutputLine(val segments: ArrayList<RunnerOutputSegment> = ArrayList(2))

data class RunnerOutputSegment(val str: String, val type: RunnerOutputType)

enum class RunnerOutputType {
    OUTPUT_STREAM, ERROR_STREAM, PROCESS_INFO, PROCESS_FATAL
}
