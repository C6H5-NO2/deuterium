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

package com.c6h5no2.deuterium.platform

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.c6h5no2.deuterium.util.TextLines
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.FilenameFilter
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

fun java.io.File.toProjectFile(): JbFile = object : JbFile {
    override val jvmFile get() = this@toProjectFile

    override val name: String get() = this@toProjectFile.name

    override val isDirectory: Boolean get() = this@toProjectFile.isDirectory

    override val children: List<JbFile>
        get() = this@toProjectFile
            .listFiles(FilenameFilter { _, name -> !name.startsWith(".") })
            .orEmpty()
            .map { it.toProjectFile() }

    override val hasChildren: Boolean
        get() = isDirectory && (listFiles()?.size ?: 0) > 0


    override fun readLines(scope: CoroutineScope): TextLines {
        var byteBufferSize: Int
        val byteBuffer = RandomAccessFile(this@toProjectFile, "r").use { file ->
            byteBufferSize = file.length().toInt()
            val byteBuffer = ByteBuffer.allocate(byteBufferSize)
            // the script file is relatively small
            file.channel.read(byteBuffer)
            file.channel.close()
            file.close()
            return@use byteBuffer
        }

        val lineStartPositions = IntList()

        var size by mutableStateOf(0)

        val refreshJob = scope.launch {
            delay(100)
            size = lineStartPositions.size
            while (true) {
                delay(1000)
                size = lineStartPositions.size
            }
        }

        scope.launch(Dispatchers.IO) {
            readLinePositions(lineStartPositions, byteBuffer)
            refreshJob.cancel()
            size = lineStartPositions.size
        }

        return object : TextLines {
            override val size get() = size

            override fun get(index: Int): String {
                val startPosition = lineStartPositions[index]
                val length = if (index + 1 < size) lineStartPositions[index + 1] - startPosition else
                    byteBufferSize - startPosition
                // Only JDK since 13 has slice() method we need, so do ugly for now.
                byteBuffer.position(startPosition)
                val slice = byteBuffer.slice()
                slice.limit(length)
                return StandardCharsets.UTF_8.decode(slice).toString()
            }
        }
    }
}

private fun java.io.File.readLinePositions(
    starts: IntList,
    buffer: ByteBuffer
) {
    val length = length()
    require(length <= Int.MAX_VALUE) {
        "Files with size over ${Int.MAX_VALUE} aren't supported"
    }

    val averageLineLength = 200
    starts.clear(length().toInt() / averageLineLength)

    try {
        starts.add(0)
        var position = 0
        while (position < length) {
            if (buffer[position].toInt().toChar() == '\n')
                starts.add(position + 1)
            position++
        }
    } catch (e: IOException) {
        e.printStackTrace()
        starts.clear(1)
        starts.add(0)
    }

    starts.compact()
}

/**
 * Compact version of List<Int> (without unboxing Int and using IntArray under the hood)
 */
private class IntList(initialCapacity: Int = 16) {
    @Volatile
    private var array = IntArray(initialCapacity)

    @Volatile
    var size: Int = 0
        private set

    fun clear(capacity: Int) {
        array = IntArray(capacity)
        size = 0
    }

    fun add(value: Int) {
        if (size == array.size) {
            doubleCapacity()
        }
        array[size++] = value
    }

    operator fun get(index: Int) = array[index]

    private fun doubleCapacity() {
        val newArray = IntArray(array.size * 2 + 1)
        System.arraycopy(array, 0, newArray, 0, size)
        array = newArray
    }

    fun compact() {
        array = array.copyOfRange(0, size)
    }
}