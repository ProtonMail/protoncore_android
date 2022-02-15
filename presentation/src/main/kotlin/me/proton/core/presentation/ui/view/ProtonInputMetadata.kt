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

package me.proton.core.presentation.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.TextView
import androidx.core.content.withStyledAttributes
import androidx.viewbinding.ViewBinding
import com.google.android.material.textfield.TextInputLayout
import me.proton.core.presentation.R
import me.proton.core.presentation.databinding.ProtonInputMetadataBinding

class ProtonInputMetadata : ProtonInput {

    private var inputMetadataBinding: ViewBinding? = null

    override val binding: ViewBinding
        get() {
            if (inputMetadataBinding == null) {
                inputMetadataBinding = ProtonInputMetadataBinding.inflate(LayoutInflater.from(context), this)
            }
            return inputMetadataBinding!!
        }

    override val input: ProtonCopyPasteEditText
        get() = (binding as ProtonInputMetadataBinding).input

    override val inputLayout: TextInputLayout
        get() = (binding as ProtonInputMetadataBinding).inputLayout

    override val label: TextView
        get() = (binding as ProtonInputMetadataBinding).label

    private var focusOn: Long = 0
    private var focusOff: Long = 0
    private var focused: Boolean = false

    private var metricsEnabled: Boolean = false
        set(value) {
            field = value
            if (value) enableMetrics()
        }

    var clicksCounter: Int = 0

    val copies: List<String>
        get() = input.copyList

    val pastes: List<String>
        get() = input.pasteList

    val keys: MutableList<Char> = mutableListOf()

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
        context.withStyledAttributes(attrs, R.styleable.ProtonInputMetadata) {
            metricsEnabled = getBoolean(R.styleable.ProtonInputMetadata_metricsEnabled, false)
        }

        input.setOnKeyListener { _, _, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                keys.add(event.unicodeChar.toChar())
            }
            false
        }
    }

    fun calculateFocus(focusLost: Boolean = true): Long {
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

    companion object {
        private const val focusCheckDelay = 500L
    }
}
