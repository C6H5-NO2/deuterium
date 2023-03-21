package com.c6h5no2.deuterium.ui.runner

import java.time.LocalTime
import kotlin.time.Duration
import kotlin.time.toJavaDuration


class RunnerProgress(private val nRuns: Int) {
    private var lastPos: Int = 0

    private var estOneRun: Duration? = null

    private var estEnd: LocalTime? = null


    /** @param position Position of last run which starts from 1 */
    fun finishRun(position: Int, time: Duration) {
        lastPos = position
        estOneRun = if (estOneRun == null) time else (estOneRun!! / 2 + time / 2)
        estEnd = LocalTime.now() + (estOneRun!! * (nRuns - position)).toJavaDuration()
    }


    override fun toString(): String {
        val time =
            if (estEnd == null)
                "unknown"
            else {
                val eta = java.time.Duration.between(LocalTime.now(), estEnd)
                if (eta.isNegative)
                    "0s"
                else
                    "${eta.toHours()}h ${eta.toMinutesPart()}m ${eta.toSecondsPart()}s"
            }
        return "${lastPos + 1} of $nRuns runs | Time remaining $time"
    }
}
