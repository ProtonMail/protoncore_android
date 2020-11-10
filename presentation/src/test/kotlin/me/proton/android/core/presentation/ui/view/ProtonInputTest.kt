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

import android.os.Build
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import me.proton.android.core.presentation.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Custom input view tests.
 */
@Config(sdk = [Build.VERSION_CODES.M])
@RunWith(RobolectricTestRunner::class)
class ProtonInputTest {

    private lateinit var protonInput: ProtonInput
    private lateinit var activity: AppCompatActivity

    @Before
    fun beforeEveryTest() {
        val activityController = Robolectric.buildActivity(AppCompatActivity::class.java)
        activity = activityController.get()
        protonInput = LayoutInflater.from(activity).inflate(
            R.layout.proton_input, ProtonInput(activity, null)
        ) as ProtonInput

        val parent = FrameLayout(activity)
        parent.addView(protonInput)
        activity.windowManager.addView(parent, WindowManager.LayoutParams(500, 500))
    }

    @Test
    fun `view ordering is correct`() {
        val editText = protonInput.findViewById<TextInputEditText>(R.id.input)
        val labelView = protonInput.findViewById<TextView>(R.id.label)

        assertTrue(editText is EditText)
        assertTrue(labelView is TextView)
    }

    @Test
    fun `label initially is gone`() {
        val labelView = protonInput.findViewById<TextView>(R.id.label)

        assertEquals(labelView.visibility, View.GONE)
    }

    @Test
    fun `set label makes it visible`() {
        val labelView = protonInput.findViewById<TextView>(R.id.label)

        protonInput.labelText = "test label"
        assertEquals(View.VISIBLE, labelView.visibility)
        assertEquals("test label", labelView.text.toString())
    }

    @Test
    fun `set label NULL makes it gone and text empty`() {
        val labelView = protonInput.findViewById<TextView>(R.id.label)

        protonInput.labelText = null
        assertEquals(View.GONE, labelView.visibility)
        assertEquals("", labelView.text.toString())
    }

    @Test
    fun `set text works correctly`() {
        val editText = protonInput.findViewById<TextInputEditText>(R.id.input)

        protonInput.text = "test text"
        assertEquals(View.VISIBLE, editText.visibility)
        assertEquals("test text", editText.text.toString())
    }

    @Test
    fun `set text NULL does not make input gone and text is empty`() {
        val editText = protonInput.findViewById<TextInputEditText>(R.id.input)

        protonInput.text = null
        assertEquals(View.VISIBLE, editText.visibility)
        assertEquals("", editText.text.toString())
    }

    @Test
    fun `display error message works as expected`() {
        val inputLayout = protonInput.findViewById<TextInputLayout>(R.id.inputLayout)
        val labelView = protonInput.findViewById<TextView>(R.id.label)

        protonInput.labelText = "test label"
        protonInput.helpText = "test assistive"
        protonInput.setInputError()

        assertEquals(ContextCompat.getColor(protonInput.context, R.color.notification_error), labelView.currentTextColor)
        assertEquals(protonInput.helpText, inputLayout.error)
    }

    @Test
    fun `set input error custom message shown`() {
        val inputLayout = protonInput.findViewById<TextInputLayout>(R.id.inputLayout)
        val editText = protonInput.findViewById<TextInputEditText>(R.id.input)
        val labelView = protonInput.findViewById<TextView>(R.id.label)

        protonInput.labelText = "test label"
        protonInput.helpText = "test assistive"
        protonInput.text = "test text"
        protonInput.setInputError("test error message")

        assertEquals("test text", editText.text.toString())
        assertEquals("test label", labelView.text.toString())
        assertEquals("test error message", inputLayout.error)
    }

    @Test
    fun `remove input error text works correctly`() {
        val inputLayout = protonInput.findViewById<TextInputLayout>(R.id.inputLayout)
        val editText = protonInput.findViewById<TextInputEditText>(R.id.input)
        val labelView = protonInput.findViewById<TextView>(R.id.label)

        protonInput.labelText = "test label"
        protonInput.helpText = "test assistive"
        protonInput.text = "test text"
        protonInput.setInputError("test error message")

        protonInput.clearInputError()
        assertEquals("test text", editText.text.toString())
        assertEquals("test label", labelView.text.toString())
        assertEquals(null, inputLayout.error)
    }

    @Test
    fun `remove input error colors works correctly`() {
        val editText = protonInput.findViewById<TextInputEditText>(R.id.input)
        val labelView = protonInput.findViewById<TextView>(R.id.label)

        protonInput.labelText = "test label"
        protonInput.helpText = "test assistive"
        protonInput.text = "test text"

        val originalLabelColor = labelView.currentTextColor
        val originalInputEditTextBackground = editText.background

        protonInput.setInputError("test error message")
        protonInput.clearInputError()

        assertEquals(
            shadowOf(originalInputEditTextBackground).createdFromResId,
            shadowOf(editText.background).createdFromResId
        )
        assertEquals(originalLabelColor, labelView.currentTextColor)
    }

    @Test
    fun `setting inputType sets the correct type to the EditText`() {
        protonInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        assertTrue(protonInput.isPasswordInput)
    }
}
