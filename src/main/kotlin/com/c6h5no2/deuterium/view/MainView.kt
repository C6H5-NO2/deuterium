package com.c6h5no2.deuterium.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.c6h5no2.deuterium.ui.horizontalSplitLayout


@Composable
fun mainView() {
    MaterialTheme {
        horizontalSplitLayout(
            Modifier.fillMaxSize(),
            upperContent = {
                Box {
                    Column(Modifier.align(Alignment.Center)) {
                        Text("Editor Panel")
                        Text(
                            "Open a file first",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            },
            lowerContent = {
                Box {
                    Column(Modifier.align(Alignment.Center)) {
                        Text("Output Panel")
                        Text(
                            "Output goes here",
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            },
        )
    }
}
