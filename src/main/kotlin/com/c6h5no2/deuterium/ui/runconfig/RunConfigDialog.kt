package com.c6h5no2.deuterium.ui.runconfig

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog


@Composable
fun runConfigDialog(runConfig: RunConfig) {
    if (runConfig.showDialog)
        Dialog(
            onCloseRequest = { runConfig.showDialog = false },
            title = "Run Configuration"
        ) {
            Column(Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = runConfig.kotlincPath,
                    onValueChange = { runConfig.kotlincPath = it },
                    modifier = Modifier.padding(4.dp).fillMaxWidth(),
                    label = { Text("Kotlin Compiler Path") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = runConfig.cmdArgs,
                    onValueChange = { runConfig.cmdArgs = it },
                    modifier = Modifier.padding(4.dp).fillMaxWidth(),
                    label = { Text("Command Line Arguments") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = runConfig.nRuns.toString(),
                    onValueChange = { runConfig.nRuns = maxOf((it.toIntOrNull() ?: 1), 1) },
                    modifier = Modifier.padding(4.dp).fillMaxWidth(),
                    label = { Text("Number of Runs") },
                )
            }
        }
}
