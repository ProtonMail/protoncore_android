package me.proton.core.challenge.presentation.compose

import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onInterceptKeyBeforeSoftKeyboard
import androidx.compose.ui.input.pointer.motionEventSpy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import me.proton.core.challenge.domain.entity.ChallengeFrameDetails

@OptIn(ExperimentalComposeUiApi::class)
public fun Modifier.payload(
    flow: String,
    frame: String,
    onTextChanged: Flow<Pair<String, String>>,
    onTextCopied: Flow<String>,
    onFrameUpdated: (ChallengeFrameDetails) -> Unit
): Modifier = composed {
    var clickCount: Int by remember(flow, frame) { mutableIntStateOf(0) }
    var lastFocusTimeMillis: Long by remember(flow, frame) { mutableLongStateOf(0L) }
    val focusList: MutableList<Int> = remember(flow, frame) { mutableStateListOf() }
    val keyList: MutableList<String> = remember(flow, frame) { mutableStateListOf() }
    val copyList: MutableList<String> = remember(flow, frame) { mutableStateListOf() }
    val pasteList: MutableList<String> = remember(flow, frame) { mutableStateListOf() }

    fun updateFrame() = onFrameUpdated(
        ChallengeFrameDetails(
            flow = flow,
            challengeFrame = frame,
            focusTime = focusList,
            clicks = clickCount,
            copy = copyList,
            paste = pasteList,
            keys = keyList
        )
    )

    fun onFocusChanged(isFocused: Boolean) {
        if (isFocused) {
            lastFocusTimeMillis = System.currentTimeMillis()
        } else if (lastFocusTimeMillis != 0L) {
            val diff = (System.currentTimeMillis() - lastFocusTimeMillis) / 1000
            focusList.add(diff.toInt())
        }
    }

    fun onTextCopied(text: String) {
        keyList.add(Keys.COPY)
        copyList.add(text)
    }

    fun onTextChanged(previous: String, current: String) {
        val diffCount = current.count() - previous.count()
        when {
            diffCount == 0 -> Unit
            diffCount > 1 -> {
                val newText = current.takeLast(diffCount)
                keyList.add(Keys.PASTE)
                pasteList.add(newText)
            }

            diffCount < 0 -> {
                keyList.add(Keys.BACKSPACE)
            }
            // diffCount == 1
            else -> {
                keyList.add(current.takeLast(1))
            }
        }
    }

    fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val char = when (event.keyCode) {
                // KeyEvent.KEYCODE_DEL -> BACKSPACE -> handled onTextChanged.
                KeyEvent.KEYCODE_TAB -> Keys.TAB
                KeyEvent.KEYCODE_SHIFT_LEFT -> Keys.SHIFT
                KeyEvent.KEYCODE_SHIFT_RIGHT -> Keys.SHIFT
                KeyEvent.KEYCODE_CAPS_LOCK -> Keys.CAPS
                KeyEvent.KEYCODE_DPAD_LEFT -> Keys.ARROW_LEFT
                KeyEvent.KEYCODE_DPAD_RIGHT -> Keys.ARROW_RIGHT
                KeyEvent.KEYCODE_DPAD_UP -> Keys.ARROW_UP
                KeyEvent.KEYCODE_DPAD_DOWN -> Keys.ARROW_DOWN
                KeyEvent.KEYCODE_COPY -> Keys.COPY
                KeyEvent.KEYCODE_PASTE -> Keys.PASTE
                else -> null
            }
            if (char != null) {
                keyList.add(char)
            }
        }
        return false
    }

    fun onMotionEvent(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_UP) {
            clickCount += 1
        }
    }

    LaunchedEffect(flow, frame) {
        onTextChanged.collect { (previous, current) ->
            onTextChanged(previous = previous, current = current)
        }
    }

    LaunchedEffect(flow, frame) {
        onTextCopied.filter { it.isNotEmpty() }.collect { text ->
            onTextCopied(text)
        }
    }

    DisposableEffect(flow, frame) {
        updateFrame()
        onDispose { updateFrame() }
    }

    this
        .onFocusChanged { state -> onFocusChanged(state.isFocused) }
        .onInterceptKeyBeforeSoftKeyboard { event -> onKeyEvent(event.nativeKeyEvent) }
        .motionEventSpy { event -> onMotionEvent(event) }
}

internal object Keys {
    const val BACKSPACE = "Backspace"
    const val PASTE = "Paste"
    const val COPY = "Copy"
    const val TAB = "Tab"
    const val CAPS = "Caps"
    const val SHIFT = "Shift"
    const val ARROW_LEFT = "ArrowLeft"
    const val ARROW_RIGHT = "ArrowRight"
    const val ARROW_UP = "ArrowUp"
    const val ARROW_DOWN = "ArrowDOWN"
}
