package com.c6h5no2.deuterium.ui.runconfig

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


class RunConfig {
    var showDialog by mutableStateOf(false)
    var kotlincPath by mutableStateOf("kotlinc")
    var cmdArgs by mutableStateOf(" ")
    var nRuns by mutableStateOf(1)
}
