package com.c6h5no2.deuterium.ui.runner

import com.c6h5no2.deuterium.util.TextLines
import java.util.*


class RunnerOutputs : TextLines {
    override val size: Int get() = lines.size

    override operator fun get(index: Int): String =
        lines[index].segments.joinToString(separator = "") { it.str }

    fun getTyped(index: Int): List<RunnerOutputSeg> = Collections.unmodifiableList(lines[index].segments)


    private val lines = ArrayList<RunnerOutputLine>(16)

    fun appendSegment(seg: String, type: RunnerOutputType) {
        if (lines.size == 0)
            lines.add(RunnerOutputLine())
        seg.splitToSequence('\n').forEachIndexed { idx, it ->
            if (idx > 0)
                lines.add(RunnerOutputLine())
            if (it.isNotEmpty())
                lines.last().segments.add(RunnerOutputSeg(it, type))
        }
    }
}


data class RunnerOutputLine(val segments: ArrayList<RunnerOutputSeg> = ArrayList(2))


data class RunnerOutputSeg(val str: String, val type: RunnerOutputType)


enum class RunnerOutputType {
    OUTPUT_STREAM, ERROR_STREAM, PROCESS_INFO, PROCESS_FATAL
}
