/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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
package me.proton.android.core.presentation.ui.view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.ListAdapter
import androidx.core.content.withStyledAttributes
import me.proton.android.core.presentation.R
import me.proton.android.core.presentation.databinding.ProtonAutocompleteInputBinding
import me.proton.android.core.presentation.ui.isInputTypePassword
import me.proton.android.core.presentation.ui.setTextOrGoneIfNull

/**
 * Custom Proton AutoComplete Input (base on a [TextInputLayout] containing a [AutoCompleteTextView]).
 */
// See https://material.io/develop/android/components/text-fields ("Implementing an exposed dropdown menu").
open class ProtonAutoCompleteInput : LinearLayout {

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init(context, attrs)
    }

    private val binding = ProtonAutocompleteInputBinding.inflate(LayoutInflater.from(context), this)

    private fun init(context: Context, attrs: AttributeSet? = null) {
        orientation = VERTICAL

        context.withStyledAttributes(attrs, R.styleable.ProtonAutoCompleteInput) {
            text = getString(R.styleable.ProtonAutoCompleteInput_android_text) ?: ""
            labelText = getString(R.styleable.ProtonAutoCompleteInput_label)
            hintText = getString(R.styleable.ProtonAutoCompleteInput_android_hint) ?: ""
            helpText = getString(R.styleable.ProtonAutoCompleteInput_help)
            prefixText = getString(R.styleable.ProtonAutoCompleteInput_prefix)
            suffixText = getString(R.styleable.ProtonAutoCompleteInput_suffix)
            inputType = getInteger(R.styleable.ProtonAutoCompleteInput_android_inputType, InputType.TYPE_CLASS_TEXT)
            isEnabled = getBoolean(R.styleable.ProtonAutoCompleteInput_android_enabled, true)
        }
    }

    // region Public API
    /**
     * Input text property. It is nullable for more convenient use. Defaults to an empty string when {@code null}.
     */
    var text: CharSequence?
        get() = binding.input.text
        set(value) {
            binding.input.setText(value ?: "")
        }

    /**
     * Label (above the input view) property.
     */
    var labelText: CharSequence?
        get() = binding.label.text
        set(value) {
            binding.label.setTextOrGoneIfNull(value)
        }

    /**
     * The input [EditText] hint value
     */
    var hintText: CharSequence?
        get() = binding.input.hint
        set(value) {
            binding.input.hint = value
        }

    /**
     * Optional help (below the input view) property.
     */
    var helpText: CharSequence?
        get() = binding.inputLayout.helperText
        set(value) {
            binding.inputLayout.helperText = value
        }

    /**
     * Optional suffix text (inside the input view) property.
     */
    var suffixText: CharSequence?
        get() = binding.inputLayout.suffixText
        private set(value) {
            binding.inputLayout.suffixText = value
        }

    /**
     * Optional prefix text (inside the input view) property.
     */
    var prefixText: CharSequence?
        get() = binding.inputLayout.prefixText
        private set(value) {
            binding.inputLayout.prefixText = value
        }

    /**
     * The [InputType] property of the compound EditText of the view.
     */
    var inputType: Int
        get() = binding.input.inputType
        set(value) {
            binding.input.inputType = value
        }

    /**
     * Property indicating whether the view is password input view.
     */
    val isPasswordInput
        get() = inputType.isInputTypePassword()

    /**
     * Set the enabled state of this view.
     *
     * The interpretation of the enabled state varies by subclass.
     *
     * @param enabled True if this view is enabled, false otherwise.
     */
    override fun setEnabled(enabled: Boolean) {
        binding.inputLayout.isEnabled = enabled
        binding.input.isEnabled = enabled
        binding.label.isEnabled = enabled
    }

    /**
     * Returns the enabled status for this view.
     *
     * The interpretation of the enabled state varies by subclass.
     */
    override fun isEnabled(): Boolean = binding.inputLayout.isEnabled

    /**
     * Set the adapter for the underlining [AutoCompleteTextView].
     */
    fun <T> setAdapter(adapter: T) where T : ListAdapter, T : Filterable {
        binding.input.setAdapter(adapter)
    }

    override fun setOnClickListener(listener: OnClickListener) {
        binding.inputLayout.setOnClickListener(listener)
        binding.inputLayout.setEndIconOnClickListener(listener)
        binding.input.setOnClickListener(listener)
        binding.label.setOnClickListener(listener)
    }
    // endregion
}
