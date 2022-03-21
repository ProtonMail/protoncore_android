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
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import androidx.viewbinding.ViewBinding
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import me.proton.core.challenge.domain.ChallengeManager
import me.proton.core.challenge.presentation.databinding.ProtonMetadataInputBinding
import me.proton.core.network.domain.client.ClientIdProvider
import me.proton.core.presentation.ui.view.ProtonInput
import javax.inject.Inject

@AndroidEntryPoint
class ProtonMetadataInput : ProtonInput {

    @Inject
    lateinit var challengeManager: ChallengeManager

    @Inject
    lateinit var clientIdProvider: ClientIdProvider

    private var inputMetadataBinding: ViewBinding? = null

    override val binding: ViewBinding
        get() {
            if (inputMetadataBinding == null) {
                inputMetadataBinding = ProtonMetadataInputBinding.inflate(LayoutInflater.from(context), this)
            }
            return inputMetadataBinding!!
        }

    override val input: ProtonCopyPasteEditText
        get() = (binding as ProtonMetadataInputBinding).input

    override val inputLayout: TextInputLayout
        get() = (binding as ProtonMetadataInputBinding).inputLayout

    override val label: TextView
        get() = (binding as ProtonMetadataInputBinding).label

    private var focusOn: Long = 0
    private var focusOff: Long = 0
    private var focused: Boolean = false

    private lateinit var flow: String

    private lateinit var frame: String

    private var clicksCounter: Int = 0

    private val copies: List<String>
        get() = input.copyList

    private val pastes: List<String>
        get() = input.pasteList

    private val keys: MutableList<Char> = mutableListOf()

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet? = null) {
        context.withStyledAttributes(attrs, R.styleable.ChallengeInput) {
            flow = getString(R.styleable.ChallengeInput_challengeFlow) ?: ""
            frame = getString(R.styleable.ChallengeInput_challengeFrame) ?: ""
        }

        enableMetrics()
        input.setOnKeyListener { _, _, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                keys.add(event.unicodeChar.toChar())
            }
            false
        }
    }

    private fun calculateFocus(focusLost: Boolean = true): Long {
        return when {
            focusLost && focusOn != 0L -> {
                focusOff = System.currentTimeMillis()
                val focusTime = focusOff - focusOn
                resetFocusValues()
                focusTime
            }
            focused -> System.currentTimeMillis() - focusOn
            else -> 0
        }
    }

    private fun resetFocusValues() {
        focusOn = 0
        focusOff = 0
        focused = input.hasFocus()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun enableMetrics() {
        input.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                clicksCounter++
                handler.postDelayed(
                    {
                        focused = input.hasFocus()
                        if (focused) {
                            focusOn = System.currentTimeMillis()
                        }
                    },
                    focusCheckDelay
                )
            }
            super.onTouchEvent(event)
        }
    }

    suspend fun flush() {
        challengeManager.addOrUpdateFrameToFlow(
            flow = flow,
            challenge = frame,
            focusTime = calculateFocus(),
            clicks = clicksCounter,
            copies = copies,
            pastes = pastes,
            keys = keys
        )
    }

    companion object {
        private const val focusCheckDelay = 500L
    }
}
