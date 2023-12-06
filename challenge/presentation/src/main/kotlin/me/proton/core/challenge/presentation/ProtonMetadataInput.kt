/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.proton.core.challenge.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnKeyListener
import android.view.View.OnTouchListener
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import androidx.viewbinding.ViewBinding
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.challenge.domain.ChallengeManager
import me.proton.core.challenge.presentation.ProtonCopyPasteEditText.OnCopyPasteListener
import me.proton.core.challenge.presentation.databinding.ProtonMetadataInputBinding
import me.proton.core.challenge.presentation.databinding.ProtonMetadataInputBinding.inflate
import me.proton.core.presentation.ui.view.ProtonInput
import javax.inject.Inject

@AndroidEntryPoint
public class ProtonMetadataInput : ProtonInput, OnKeyListener, OnTouchListener,
    OnCopyPasteListener {

    @Inject
    public lateinit var challengeManager: ChallengeManager

    private var inputMetadataBinding: ViewBinding? = null

    override val binding: ViewBinding
        get() {
            if (inputMetadataBinding == null) {
                inputMetadataBinding = inflate(LayoutInflater.from(context), this)
            }
            return inputMetadataBinding!!
        }

    override val input: ProtonCopyPasteEditText
        get() = (binding as ProtonMetadataInputBinding).input

    override val inputLayout: TextInputLayout
        get() = (binding as ProtonMetadataInputBinding).inputLayout

    override val label: TextView
        get() = (binding as ProtonMetadataInputBinding).label

    private var lastFocusTimeMillis: Long = 0
    private var clickCount: Int = 0

    private val focusList = mutableListOf<Int>()
    private val keyList = mutableListOf<String>()
    private val copyList = mutableListOf<String>()
    private val pasteList = mutableListOf<String>()

    private lateinit var flow: String
    private lateinit var frame: String

    public constructor(
        context: Context,
    ) : super(context) {
        init(context)
    }

    public constructor(
        context: Context,
        attrs: AttributeSet,
    ) : super(context, attrs) {
        init(context, attrs)
    }

    public constructor(
        context: Context,
        attrs: AttributeSet,
        defStyle: Int,
    ) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init(context: Context, attrs: AttributeSet? = null) {
        context.withStyledAttributes(attrs, R.styleable.ChallengeInput) {
            flow = getString(R.styleable.ChallengeInput_challengeFlow) ?: ""
            frame = getString(R.styleable.ChallengeInput_challengeFrame) ?: ""
        }
        input.setOnTouchListener(this)
        input.setOnKeyListener(this)
        input.addTextChangedListener(ProtonMetadataInputWatcher(keyList, pasteList))
        input.setOnCopyPasteListener(this)
    }

    public suspend fun flush() {
        challengeManager.addOrUpdateFrameToFlow(
            flow = flow,
            challengeFrame = frame,
            focusTime = focusList.toList(), // Pass a copy so it doesn't change when focusList is being mutated.
            clicks = clickCount,
            copies = copyList.toList(),
            pastes = pasteList.toList(),
            keys = keyList.toList()
        )
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            clickCount++
        }
        return false
    }

    override fun onKey(view: View, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val char = when (keyCode) {
                // KeyEvent.KEYCODE_DEL -> BACKSPACE -> handled onTextChanged.
                KeyEvent.KEYCODE_TAB -> TAB
                KeyEvent.KEYCODE_SHIFT_LEFT -> SHIFT
                KeyEvent.KEYCODE_SHIFT_RIGHT -> SHIFT
                KeyEvent.KEYCODE_CAPS_LOCK -> CAPS
                KeyEvent.KEYCODE_DPAD_LEFT -> ARROW_LEFT
                KeyEvent.KEYCODE_DPAD_RIGHT -> ARROW_RIGHT
                KeyEvent.KEYCODE_DPAD_UP -> ARROW_UP
                KeyEvent.KEYCODE_DPAD_DOWN -> ARROW_DOWN
                KeyEvent.KEYCODE_COPY -> COPY
                KeyEvent.KEYCODE_PASTE -> PASTE
                else -> null
            }
            if (char != null) {
                keyList.add(char)
            }
        }
        return false
    }

    override fun onCopyText(text: String) {
        keyList.add(COPY)
        copyList.add(text)
    }

    override fun onPasteText(text: String) {
        // handled onTextChanged.
    }

    override fun onFocus(focused: Boolean) {
        lastFocusTimeMillis = when {
            focused -> System.currentTimeMillis()
            else -> 0L.also {
                focusList.add(((System.currentTimeMillis() - lastFocusTimeMillis) / 1000).toInt())
            }
        }
    }

    internal companion object {
        internal const val BACKSPACE = "Backspace"
        internal const val PASTE = "Paste"
        private const val COPY = "Copy"
        private const val TAB = "Tab"
        private const val CAPS = "Caps"
        private const val SHIFT = "Shift"
        private const val ARROW_LEFT = "ArrowLeft"
        private const val ARROW_RIGHT = "ArrowRight"
        private const val ARROW_UP = "ArrowUp"
        private const val ARROW_DOWN = "ArrowDOWN"
    }
}
