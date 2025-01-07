package me.proton.core.compose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.LifecycleResumeEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Launch a coroutine [block] when the Lifecycle receives ON_RESUME and cancel it ON_PAUSE, or any new unique value of [key].
 */
@Composable
fun LaunchResumeEffect(
    key: Any?,
    block: suspend CoroutineScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    LifecycleResumeEffect(key) {
        val job = coroutineScope.launch(block = block)
        onPauseOrDispose { job.cancel() }
    }
}

/**
 * Launch a coroutine [block] when the Lifecycle receives ON_RESUME and cancel it ON_PAUSE.
 */
@Composable
fun LaunchResumeEffect(
    block: suspend CoroutineScope.() -> Unit
) = LaunchResumeEffect(Unit, block)
