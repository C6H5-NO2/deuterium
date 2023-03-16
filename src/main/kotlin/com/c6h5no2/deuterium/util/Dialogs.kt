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

package com.c6h5no2.deuterium.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.window.AwtWindow
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.WindowScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.awt.FileDialog
import java.io.File
import java.nio.file.Path
import javax.swing.JOptionPane


@Composable
fun FrameWindowScope.FileDialog(
    title: String,
    isLoad: Boolean,
    onResult: (result: Path?) -> Unit
) = AwtWindow(
    create = {
        object : FileDialog(window, "Choose a file", if (isLoad) LOAD else SAVE) {
            override fun setVisible(value: Boolean) {
                super.setVisible(value)
                if (value) {
                    if (file != null) {
                        onResult(File(directory).resolve(file).toPath())
                    } else {
                        onResult(null)
                    }
                }
            }
        }.apply {
            this.title = title
            this.file = "*.kts"
        }
    },
    dispose = FileDialog::dispose
)

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun WindowScope.YesNoCancelDialog(
    title: String,
    message: String,
    onResult: (result: AlertDialogResult) -> Unit
) {
    DisposableEffect(Unit) {
        val job = GlobalScope.launch(Dispatchers.IO) {
            val resultInt = JOptionPane.showConfirmDialog(
                window, message, title, JOptionPane.YES_NO_CANCEL_OPTION
            )
            val result = when (resultInt) {
                JOptionPane.YES_OPTION -> AlertDialogResult.Yes
                JOptionPane.NO_OPTION -> AlertDialogResult.No
                else -> AlertDialogResult.Cancel
            }
            onResult(result)
        }

        onDispose {
            job.cancel()
        }
    }
}

enum class AlertDialogResult {
    Yes, No, Cancel
}
