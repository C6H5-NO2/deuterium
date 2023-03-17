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

package com.c6h5no2.deuterium.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.c6h5no2.deuterium.platform.VerticalScrollbar
import com.c6h5no2.deuterium.ui.common.AppTheme
import com.c6h5no2.deuterium.ui.common.Fonts
import com.c6h5no2.deuterium.ui.common.Settings
import com.c6h5no2.deuterium.util.loadableScoped
import kotlin.text.Regex.Companion.fromLiteral

@Composable
fun EditorView(model: Editor, settings: Settings) = key(model) {
    with(LocalDensity.current) {
        SelectionContainer {
            Surface(
                Modifier.fillMaxSize(),
                color = AppTheme.colors.backgroundDark,
            ) {
                val lines by loadableScoped(model.lines)

                if (lines != null) {
                    Box {
                        Lines(lines!!, model, settings)
                        // Box(
                        //     Modifier
                        //         .offset(x = settings.fontSize.toDp() * 0.5f * settings.maxLineSymbols)
                        //         .width(1.dp)
                        //         .fillMaxHeight()
                        //         .background(AppTheme.colors.backgroundLight)
                        // )
                    }
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(36.dp)
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun Lines(lines: Editor.Lines, model: Editor, settings: Settings) = with(LocalDensity.current) {
    val maxNum = remember(lines.lineNumberDigitCount) {
        (1..lines.lineNumberDigitCount).joinToString(separator = "") { "9" }
    }

    Box(Modifier.fillMaxSize()) {
        val scrollState = rememberLazyListState()
        val editorScrollState = rememberScrollState()
        LaunchedEffect(scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset) {
            val ele = scrollState.layoutInfo.visibleItemsInfo.getOrNull(0) ?: return@LaunchedEffect
            val compensation = 4
            val offset = ele.index * ele.size - ele.offset + compensation
            editorScrollState.scrollTo(offset)
        }

        Row(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                state = scrollState
            ) {
                items(lines.size) { index ->
                    Box(Modifier.height(settings.fontSize.toDp() * 1.6f)) {
                        Line(Modifier.align(Alignment.CenterStart), maxNum, lines[index], settings)
                    }
                }
            }

            codeContent(
                model,
                Modifier
                    .fillMaxSize()
                    .padding(start = 28.dp, end = 12.dp)
                    .horizontalScroll(rememberScrollState())
                    .verticalScroll(editorScrollState),
                settings
            )
        }

        VerticalScrollbar(
            Modifier.align(Alignment.CenterEnd),
            scrollState
        )
    }
}

// Поддержка русского языка
// دعم اللغة العربية
// 中文支持
@Composable
private fun Line(modifier: Modifier, maxNum: String, line: Editor.Line, settings: Settings) {
    Row(modifier = modifier) {
        DisableSelection {
            Box {
                LineNumber(maxNum, Modifier.alpha(0f), settings)
                LineNumber(line.number.toString(), Modifier.align(Alignment.CenterEnd), settings)
            }
        }
        // LineContent(
        //     line.content,
        //     modifier = Modifier
        //         .weight(1f)
        //         .withoutWidthConstraints()
        //         .padding(start = 28.dp, end = 12.dp),
        //     settings = settings
        // )
    }
}

@Composable
private fun LineNumber(number: String, modifier: Modifier, settings: Settings) = Text(
    text = number,
    fontSize = settings.fontSize,
    fontFamily = Fonts.jetbrainsMono(),
    color = LocalContentColor.current.copy(alpha = 0.30f),
    modifier = modifier.padding(start = 12.dp)
)

@Composable
private fun LineContent(content: Editor.Content, modifier: Modifier, settings: Settings) = Text(
    text = if (content.isCode) {
        codeString(content.value.value)
    } else {
        buildAnnotatedString {
            withStyle(AppTheme.code.simple) {
                append(content.value.value)
            }
        }
    },
    fontSize = settings.fontSize,
    fontFamily = Fonts.jetbrainsMono(),
    modifier = modifier,
    softWrap = false
)

@Composable
fun codeContent(model: Editor, modifier: Modifier, settings: Settings) {
    val value = remember { mutableStateOf((1..100).joinToString("") { "$it\n" }) }  // todo
    val colors = TextFieldDefaults.textFieldColors()
    BasicTextField(
        value = value.value,
        onValueChange = {
            // todo
            value.value = it
        },
        modifier = modifier.background(colors.backgroundColor(enabled = true).value),
        textStyle = TextStyle(
            color = colors.textColor(enabled = true).value,
            fontSize = settings.fontSize,
            fontFamily = Fonts.jetbrainsMono(),
            lineHeight = settings.fontSize * 1.6f
        ),
        cursorBrush = SolidColor(colors.cursorColor(isError = false).value)
    )
}

private fun codeString(str: String) = buildAnnotatedString {
    withStyle(AppTheme.code.simple) {
        val strFormatted = str.replace("\t", "    ")
        append(strFormatted)
        addStyle(AppTheme.code.punctuation, strFormatted, ":")
        addStyle(AppTheme.code.punctuation, strFormatted, "=")
        addStyle(AppTheme.code.punctuation, strFormatted, "\"")
        addStyle(AppTheme.code.punctuation, strFormatted, "[")
        addStyle(AppTheme.code.punctuation, strFormatted, "]")
        addStyle(AppTheme.code.punctuation, strFormatted, "{")
        addStyle(AppTheme.code.punctuation, strFormatted, "}")
        addStyle(AppTheme.code.punctuation, strFormatted, "(")
        addStyle(AppTheme.code.punctuation, strFormatted, ")")
        addStyle(AppTheme.code.punctuation, strFormatted, ",")
        addStyle(AppTheme.code.keyword, strFormatted, "fun ")
        addStyle(AppTheme.code.keyword, strFormatted, "val ")
        addStyle(AppTheme.code.keyword, strFormatted, "var ")
        addStyle(AppTheme.code.keyword, strFormatted, "private ")
        addStyle(AppTheme.code.keyword, strFormatted, "internal ")
        addStyle(AppTheme.code.keyword, strFormatted, "for ")
        addStyle(AppTheme.code.keyword, strFormatted, "expect ")
        addStyle(AppTheme.code.keyword, strFormatted, "actual ")
        addStyle(AppTheme.code.keyword, strFormatted, "import ")
        addStyle(AppTheme.code.keyword, strFormatted, "package ")
        addStyle(AppTheme.code.value, strFormatted, "true")
        addStyle(AppTheme.code.value, strFormatted, "false")
        addStyle(AppTheme.code.value, strFormatted, Regex("[0-9]*"))
        addStyle(AppTheme.code.annotation, strFormatted, Regex("^@[a-zA-Z_]*"))
        addStyle(AppTheme.code.comment, strFormatted, Regex("^\\s*//.*"))
    }
}

private fun AnnotatedString.Builder.addStyle(style: SpanStyle, text: String, regexp: String) {
    addStyle(style, text, fromLiteral(regexp))
}

private fun AnnotatedString.Builder.addStyle(style: SpanStyle, text: String, regexp: Regex) {
    for (result in regexp.findAll(text)) {
        addStyle(style, result.range.first, result.range.last + 1)
    }
}
