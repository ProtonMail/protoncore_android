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
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Parcelable
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.text.method.SingleLineTransformationMethod
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.ToggleButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import me.proton.android.core.presentation.R
import me.proton.android.core.presentation.databinding.ProtonInputBinding
import me.proton.android.core.presentation.ui.isInputTypePassword
import me.proton.android.core.presentation.ui.setTextOrGoneIfNull
import me.proton.android.core.presentation.utils.clearText
import me.proton.android.core.presentation.utils.onClick

/**
 * Custom Proton input (advanced complex [EditText]) view.
 * Includes an [EditText] as a default input view, additionally it includes a Label which is located above the
 * input view and an optional assistive text located below the input view.
 * The assistive text can act also as a validation error message if an error message is passed in [setInputError]
 * function.
 * ProtonInput supports displaying error according to the latest Proton Android design guidelines, so, the client
 * does not need to worry about.
 *
 * @author Dino Kadrikj.
 */
open class ProtonInput : ConstraintLayout {
    // region constructors using @JvmOverloads is not safe (because passing default values which currently the super
    // constructor is using can change in one of the future SDK lib releases, so we implement all constructors.
    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs, defStyleAttr)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init(attrs, defStyleAttr, defStyleRes)
    }

    // endregion
    // region views
    private lateinit var input: EditText
    private lateinit var label: TextView
    private lateinit var assistiveText: TextView
    private lateinit var inputActionButton: ToggleButton

    // endregion
    // region state
    private var labelColor: ColorStateList? = null
    private var assistiveTextColor: ColorStateList? = null
    private var assistiveOriginalValue: CharSequence? = null

    // endregion
    private fun init(attrs: AttributeSet?, defStyleAttr: Int = 0, defStyleRes: Int = 0) {
        // initialize the view binding
        val viewBinding = ProtonInputBinding.inflate(LayoutInflater.from(context), this)
        viewBinding.apply {
            this@ProtonInput.label = label
            this@ProtonInput.assistiveText = assistiveText
            this@ProtonInput.input = input
            this@ProtonInput.inputActionButton = inputActionButton
        }

        // read the attributes
        context.withStyledAttributes(attrs, R.styleable.ProtonInput, defStyleAttr, defStyleRes) {
            inputText = getString(R.styleable.ProtonInput_android_text) ?: ""
            inputHint = getString(R.styleable.ProtonInput_android_hint) ?: ""
            inputLabel = getString(R.styleable.ProtonInput_label)
            inputAssistiveText = getString(R.styleable.ProtonInput_assistiveText)
            inputType = getInteger(R.styleable.ProtonInput_android_inputType, InputType.TYPE_CLASS_TEXT)
            isEnabled = getBoolean(R.styleable.ProtonInput_android_enabled, true)
        }
        // bind the values to the UI
        input.apply {
            inputType = this@ProtonInput.inputType
            isEnabled = this@ProtonInput.isEnabled
            setText(inputText)
            hint = inputHint
            addTextChangedListener {
                if (it?.isNotEmpty() == true) {
                    // this should reset to the default focused! style
                    input.hideError()
                    inputActionButton.visibility = View.VISIBLE
                } else {
                    inputActionButton.visibility = View.GONE
                }
            }
        }
        label.isEnabled = isEnabled
        assistiveText.isEnabled = isEnabled
        inputActionButton.apply {
            if (isPasswordInput) {
                setButtonDrawable(R.drawable.selector_password_toggle)
            }
            onClick(::onToggleClicked)
        }
        assistiveTextColor = assistiveText.textColors
        labelColor = label.textColors
    }
    // region public API
    /**
     * The [InputType] property of the compound EditText of the view.
     */
    var inputType: Int
        get() = input.inputType
        set(value) {
            input.inputType = value
        }

    /**
     * Property indicating whether the view is password input view.
     */
    val isPasswordInput
        get() = inputType.isInputTypePassword()

    /**
     * Input text property. It is nullable for more convenient use. Defaults to an empty string when {@code null}.
     */
    var inputText: CharSequence?
        get() = input.text
        set(value) {
            input.setText(value ?: "")
        }

    /** Label (above the input view) property. */
    var inputLabel: CharSequence?
        get() = label.text
        set(value) {
            label.setTextOrGoneIfNull(value)
        }

    /** The input [EditText] hint value */
    var inputHint: CharSequence?
        get() = input.hint
        set(value) {
            input.hint = value
        }

    /** Optional assistive (below the input view) property. */
    var inputAssistiveText: CharSequence?
        get() = assistiveText.text
        set(value) {
            assistiveText.setTextOrGoneIfNull(value)
        }

    /**
     * Set the enabled state of this view. The interpretation of the enabled
     * state varies by subclass.
     *
     * @param enabled True if this view is enabled, false otherwise.
     */
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        input.isEnabled = enabled
        label.isEnabled = enabled
        assistiveText.isEnabled = enabled
    }

    /**
     * Sets the error UI layout to the ProtonInput view. Not only the [EditText] but also if visible the label
     * (above the input view) and the assistive text (below the input view).
     */
    fun setInputError(assistiveErrorMessage: String? = null) {
        input.showError()
        label.showError()
        assistiveOriginalValue = inputAssistiveText
        assistiveText.showError(assistiveErrorMessage)
    }

    /**
     * Removed the error UI layout of the ProtonInput view.
     */
    fun removeInputError() {
        input.hideError()
        label.hideError(color = labelColor)
        assistiveText.hideError(originalText = assistiveOriginalValue, color = assistiveTextColor)
    }

    // endregion
    private fun onToggleClicked() {
        if (isPasswordInput) {
            if (inputActionButton.isChecked) {
                input.transformationMethod = SingleLineTransformationMethod.getInstance()
            } else {
                input.transformationMethod = PasswordTransformationMethod.getInstance()
            }
            input.setSelection(input.text?.length ?: 0)
        } else {
            input.clearText()
        }
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable?>?) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable?>?) {
        dispatchThawSelfOnly(container)
    }

    override fun onSaveInstanceState(): Parcelable? {
        return Bundle().apply {
            putParcelable(STATE_KEY_SUPER, super.onSaveInstanceState())
            val childViewStates = SparseArray<Parcelable>()
            children.forEach { child -> child.saveHierarchyState(childViewStates) }
            putSparseParcelableArray(STATE_KEY_SPARSE_ARRAY, childViewStates)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var newState = state
        if (newState is Bundle) {
            val childrenState = newState.getSparseParcelableArray<Parcelable>(STATE_KEY_SPARSE_ARRAY)
            childrenState?.let { children.forEach { child -> child.restoreHierarchyState(it) } }
            newState = newState.getParcelable(STATE_KEY_SUPER)
        }
        super.onRestoreInstanceState(newState)
    }

    companion object {
        private const val STATE_KEY_SPARSE_ARRAY = "state.sparse-array"
        private const val STATE_KEY_SUPER = "state.super"
    }
}

/**
 * Displays the error state UI for [TextView]. If this is called on an [EditText] subclass then the background
 * resource is being set (red outline [R.drawable.default_edit_text_error]), otherwise the text color resource is set
 * to [R.color.signal_error].  It also accepts an error message String to be set.
 *
 * @param errorMessage the error message to be set as a [TextView.setText].
 * @return the original text color of the [TextView] if caller needs it to keep and revert it later.
 */
private fun TextView.showError(errorMessage: String? = null) {
    if (this is EditText) {
        background = context.getDrawable(R.drawable.default_edit_text_error)
    } else {
        text = errorMessage ?: text
        setTextColor(ContextCompat.getColor(context, R.color.signal_error))
    }
}

/**
 * Removes the error state UI for [TextView] and brings back the view to the original state.
 *
 * @param originalText the text that should be default for the view. In the error state some views (such as the
 * optional assistive text) could change the text to display the error details.
 * @param color the original [ColorStateList] for the [TextView.setTextColor]
 */
private fun TextView.hideError(originalText: CharSequence? = null, color: ColorStateList? = null) = apply {
    if (this is EditText) {
        background = context.getDrawable(R.drawable.selector_default_edit_text_background)
    } else {
        text = originalText ?: text
        setTextColor(color ?: ColorStateList.valueOf(ContextCompat.getColor(context, R.color.text_secondary)))
    }
}
