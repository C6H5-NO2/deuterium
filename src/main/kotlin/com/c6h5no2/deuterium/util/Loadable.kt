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

import androidx.compose.runtime.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope

@Composable
fun <T : Any> loadable(load: () -> T): MutableState<T?> {
    return loadableScoped { load() }
}

private val loadingKey = Any()

@Composable
fun <T : Any> loadableScoped(load: CoroutineScope.() -> T): MutableState<T?> {
    val state: MutableState<T?> = remember { mutableStateOf(null) }
    LaunchedEffect(loadingKey) {
        try {
            state.value = load()
        } catch (e: CancellationException) {
            // ignore
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return state
}
