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
package me.proton.core.presentation.ui.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcelable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.text.method.KeyListener
import android.util.AttributeSet
import android.util.SparseArray
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.parcelize.Parcelize
import me.proton.core.presentation.R
import me.proton.core.presentation.databinding.ProtonInputBinding
import me.proton.core.presentation.ui.isInputTypePassword
import me.proton.core.presentation.ui.setTextOrGoneIfNull
import me.proton.core.presentation.utils.clearTextAndOverwriteMemory
import me.proton.core.presentation.utils.restoreChildViewStates
import me.proton.core.presentation.utils.saveChildViewStates

/**
 * Custom Proton input (advanced complex [EditText]) view.
 *
 * Includes an [TextInputEditText], additionally it includes a Label above the input view and an optional help
 * text located below the input view.
 *
 * The help text can also act as a validation error message (through [setInputError]).
 *
 * ProtonInput supports displaying error according to the latest Proton Android design guidelines, so, the client
 * does not need to worry about.
 */
class ProtonInput : LinearLayout {

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

    private val binding = ProtonInputBinding.inflate(LayoutInflater.from(context), this)

    private var lastEditTimeMillis: Long? = null

    private fun init(context: Context, attrs: AttributeSet? = null) {
        orientation = VERTICAL

        context.withStyledAttributes(attrs, R.styleable.ProtonInput) {
            text = getString(R.styleable.ProtonInput_android_text)
            labelText = getString(R.styleable.ProtonInput_label)
            hintText = getString(R.styleable.ProtonInput_android_hint)
            helpText = getString(R.styleable.ProtonInput_help)
            prefixText = getString(R.styleable.ProtonInput_prefix)
            suffixText = getString(R.styleable.ProtonInput_suffix)
            inputType = getInteger(R.styleable.ProtonInput_android_inputType, InputType.TYPE_CLASS_TEXT)
            imeOptions = getInteger(R.styleable.ProtonInput_android_imeOptions, EditorInfo.IME_ACTION_UNSPECIFIED)
            minLines = getInteger(R.styleable.ProtonInput_android_minLines, 1)
            editTextAlignment = getInteger(R.styleable.ProtonInput_editTextAlignment, 0)
            editTextDirection = getInteger(R.styleable.ProtonInput_editTextDirection, 0)
            isEnabled = getBoolean(R.styleable.ProtonInput_android_enabled, true)
            endIconMode = EndIconMode.map[getInt(R.styleable.ProtonInput_actionMode, 0)] ?: EndIconMode.NONE
            getDrawable(R.styleable.ProtonInput_endIconDrawable)?.let {
                endIconDrawable = it
            }

            val digits = getString(R.styleable.ProtonInput_android_digits)
            if (digits != null) {
                keyListener = DigitsKeyListener.getInstance(digits.toString())
            }

            val maxLength = getInt(R.styleable.ProtonInput_android_maxLength, -1)
            if (maxLength >= 0) {
                filters += InputFilter.LengthFilter(maxLength)
            }
        }

        // Clear error on text changed.
        binding.input.addTextChangedListener { editable ->
            if (editable?.isNotEmpty() == true) clearInputError()
        }

        binding.input.addTextChangedListener {
            lastEditTimeMillis = System.currentTimeMillis()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        resetInputIfNeeded()
    }

    private fun resetInputIfNeeded() {
        val durationFromLastEdit = System.currentTimeMillis() - (lastEditTimeMillis ?: 0L)
        if (isInputTypeTextPassword() && durationFromLastEdit >= RESET_DURATION_MILLIS) {
            text = null
        }
    }

    private fun isInputTypeTextPassword() =
        inputType and InputType.TYPE_TEXT_VARIATION_PASSWORD == InputType.TYPE_TEXT_VARIATION_PASSWORD

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        dispatchThawSelfOnly(container)
    }

    override fun onSaveInstanceState(): Parcelable {
        return ProtonInputState(
            superSavedState = super.onSaveInstanceState(),
            lastEditTimeMillis = lastEditTimeMillis,
            childSavedState = saveChildViewStates()
        )
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val protonInputState = state as ProtonInputState
        restoreChildViewStates(protonInputState.childSavedState)
        lastEditTimeMillis = protonInputState.lastEditTimeMillis
        super.onRestoreInstanceState(protonInputState.superSavedState)
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
     * The input [EditText] hint value.
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
        set(value) {
            binding.inputLayout.suffixText = value
            binding.inputLayout.isExpandedHintEnabled = false
        }

    var isSuffixTextVisible: Boolean
        get() = binding.inputLayout.suffixTextView.isVisible
        set(value) {
            binding.inputLayout.suffixTextView.isVisible = value
        }

    /**
     * Optional prefix text (inside the input view) property.
     */
    var prefixText: CharSequence?
        get() = binding.inputLayout.prefixText
        set(value) {
            binding.inputLayout.prefixText = value
        }

    /**
     * The imeOptions property of the compound EditText of the view.
     */
    var imeOptions: Int
        get() = binding.input.imeOptions
        set(value) {
            binding.input.imeOptions = value
        }

    /**
     * The keyListener property of the compound EditText of the view.
     */
    var keyListener: KeyListener
        get() = binding.input.keyListener
        set(value) {
            binding.input.keyListener = value
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
     * The minLines property of the compound EditText of the view.
     */
    var minLines: Int
        get() = binding.input.minLines
        set(value) {
            binding.input.minLines = value
        }

    /**
     * The textDirection property of the compound EditText of the view.
     */
    var editTextDirection: Int
        get() = binding.input.textDirection
        set(value) {
            binding.input.textDirection = value
        }

    /**
     * The textAlignment property of the compound EditText of the view.
     */
    var editTextAlignment: Int
        get() = binding.input.textAlignment
        set(value) {
            binding.input.textAlignment = value
        }

    /**
     * The input filters of the compound EditText of the view.
     */
    var filters: Array<InputFilter>
        get() = binding.input.filters
        set(value) {
            binding.input.filters = value
        }

    /**
     * Property indicating whether the view is password input view.
     */
    val isPasswordInput
        get() = inputType.isInputTypePassword()

    /**
     * End icon drawable. It goes together with [EndIconMode.CUSTOM_ICON], otherwise it does not have effect.
     * Call #endIconMode with [EndIconMode.CUSTOM_ICON] before setting the end drawable.
     */
    var endIconDrawable: Drawable?
        get() = binding.inputLayout.endIconDrawable
        set(value) {
            binding.inputLayout.apply {
                setEndIconTintList(null)
                endIconDrawable = value
            }
        }

    var endIconMode: EndIconMode
        get() = EndIconMode.map[binding.inputLayout.endIconMode] ?: EndIconMode.NONE
        set(value) {
            setActionMode(value)
        }

    fun addTextChangedListener(watcher: TextWatcher) {
        binding.input.addTextChangedListener(watcher)
    }

    fun setOnEditorActionListener(listener: TextView.OnEditorActionListener?) {
        binding.input.setOnEditorActionListener(listener)
    }

    fun setOnActionListener(action: Int, block: () -> Unit) {
        imeOptions = action
        setOnEditorActionListener { v, actionId, event ->
            if (actionId == action || event.keyCode == KeyEvent.KEYCODE_ENTER) {
                block()
                true
            } else false
        }
    }

    fun setOnNextActionListener(block: () -> Unit) = setOnActionListener(EditorInfo.IME_ACTION_NEXT, block)

    fun setOnDoneActionListener(block: () -> Unit) = setOnActionListener(EditorInfo.IME_ACTION_DONE, block)

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
     * Clear input text and overwrite the input text memory.
     *
     * Use this to clear password fields.
     * The method relies on undocumented behavior and it's not guaranteed that no text copies are left.
     * Make sure to avoid making copies of the password when using it!
     */
    fun clearTextAndOverwriteMemory() {
        binding.input.clearTextAndOverwriteMemory()
    }

    /**
     * Set the error UI layout to the ProtonInput view.
     *
     * Not only the [EditText] but also if visible the label and the help text.
     *
     * @param error error message to show. If [String.isNullOrEmpty], helpText is taken instead.
     */
    fun setInputError(error: String? = null) {
        binding.inputLayout.error = error ?: helpText ?: " "
        binding.inputLayout.errorIconDrawable = null
        binding.label.setTextColor(ContextCompat.getColor(context, R.color.notification_error))
    }

    /**
     * Clear the error UI layout of the ProtonInput view.
     */
    fun clearInputError() {
        binding.inputLayout.error = null
        binding.label.setTextColor(ContextCompat.getColor(context, R.color.text_norm))
    }

    fun setOnFocusLostListener(listener: OnFocusChangeListener) {
        binding.input.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) listener.onFocusChange(v, hasFocus)
        }
    }

    /**
     * Set the action mode for the end icon.
     */
    private fun setActionMode(mode: EndIconMode) {
        // See ProtonInput attributes (attrs.xml).
        when (mode) {
            // None
            EndIconMode.NONE -> {
                binding.inputLayout.endIconMode = TextInputLayout.END_ICON_NONE
                binding.inputLayout.setEndIconActivated(false)
            }
            // Clear Text
            EndIconMode.CLEAR_TEXT -> {
                binding.inputLayout.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
                binding.inputLayout.setEndIconActivated(true)
            }
            // PasswordToggle
            EndIconMode.PASSWORD_TOGGLE -> {
                binding.inputLayout.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
                binding.inputLayout.setEndIconActivated(true)
            }
            // Custom Icon
            EndIconMode.CUSTOM_ICON -> {
                binding.inputLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
                binding.inputLayout.setEndIconActivated(true)
            }
        }
    }

    enum class EndIconMode(val action: Int) {
        NONE(0),
        CLEAR_TEXT(1),
        PASSWORD_TOGGLE(2),
        CUSTOM_ICON(3);

        companion object {
            val map = values().associateBy { it.action }
        }
    }
    // endregion

    @Parcelize
    data class ProtonInputState(
        val superSavedState: Parcelable?,
        val lastEditTimeMillis: Long?,
        val childSavedState: SparseArray<Parcelable>,
    ) : BaseSavedState(superSavedState), Parcelable

    companion object {
        private const val RESET_DURATION_MILLIS = 1000L * 60L * 3L // 3 minutes
    }
}
