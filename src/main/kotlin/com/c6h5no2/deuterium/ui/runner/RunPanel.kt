package com.c6h5no2.deuterium.ui.runner

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.c6h5no2.deuterium.platform.VerticalScrollbar
import com.c6h5no2.deuterium.ui.common.AppTheme
import com.c6h5no2.deuterium.util.withoutWidthConstraints


private val logger = mu.KotlinLogging.logger {}

@Composable
fun runnerView(runner: RunnerModel) {
    Box {
        Column(Modifier.align(Alignment.Center)) {
            SelectionContainer {
                Box {
                    runPanelLines(runner)
                }
            }
        }
    }
}


@Composable
private fun runPanelLines(runner: RunnerModel) = with(LocalDensity.current) {
    Box(Modifier.fillMaxSize()) {
        if (runner.runnerOutputs.size != 0) {
            val scrollState = rememberLazyListState()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = scrollState
            ) {
                items(runner.runnerOutputs.size) { index ->
                    Box(Modifier.height(17.toDp() * 1.6f)) {
                        runPanelLine(
                            Modifier.align(Alignment.CenterStart),
                            runner.runnerOutputs.getTyped(index),
                            runner.filename
                        )
                    }
                }
            }

            VerticalScrollbar(
                Modifier.align(Alignment.CenterEnd),
                scrollState
            )
        }
    }
}


@Composable
private fun runPanelLine(modifier: Modifier, segs: List<RunnerOutputSeg>, filename: String) {
    Row(modifier) {
        val text = styledString(segs, filename)
        val textModifier = Modifier.withoutWidthConstraints().padding(start = 6.dp, end = 12.dp)
        // bug of Compose: ClickableText does not work inside a SelectionContainer
        // https://issuetracker.google.com/issues/184950231
        DisableSelection {
            ClickableText(
                text = text,
                style = TextStyle(fontSize = 17.sp),
                modifier = textModifier,
                softWrap = false
            ) { offset ->
                text.getStringAnnotations(tag = "this-kts", offset, offset).firstOrNull()?.let {
                    val rc = it.item.split(':')
                    assert(rc.size == 3)
                    val row = rc[1].toInt()
                    val col = rc[2].toInt()
                    logger.info { "Move cursor to r$row:c$col" }
                    // todo: jump cursor
                }
            }
        }
    }
}


private fun styledString(segs: List<RunnerOutputSeg>, filename: String) = buildAnnotatedString {
    withStyle(AppTheme.code.simple) {
        val regex = Regex("${Regex.escape(filename)}(?::(\\d+))?(?::(\\d+))?")
        segs.forEach {
            when (it.type) {
                RunnerOutputType.OUTPUT_STREAM -> {
                    append(it.str)
                }

                RunnerOutputType.ERROR_STREAM -> {
                    val bias = length
                    append(it.str)

                    for (match in regex.findAll(it.str)) {
                        val start = bias + match.range.first
                        val end = bias + match.range.last + 1
                        addStyle(SpanStyle(Color.Cyan, textDecoration = TextDecoration.Underline), start, end)
                        val row = match.groups[1]?.value?.toInt() ?: 1
                        val col = match.groups[2]?.value?.toInt() ?: 1
                        addStringAnnotation(tag = "this-kts", annotation = ":$row:$col", start, end)
                    }
                }

                RunnerOutputType.PROCESS_INFO -> {
                    pushStyle(SpanStyle(Color.Yellow))
                    append(it.str)
                    pop()
                }

                RunnerOutputType.PROCESS_FATAL -> {
                    pushStyle(SpanStyle(Color.Red))
                    append(it.str)
                    pop()
                }
            }
        }
    }
}
