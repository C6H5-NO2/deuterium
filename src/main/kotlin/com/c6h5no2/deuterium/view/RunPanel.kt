package com.c6h5no2.deuterium.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


@Composable
fun runPanel() {
    Box {
        Column(Modifier.align(Alignment.Center)) {
            Text("Run Panel")
            Text(
                "No output yet",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
