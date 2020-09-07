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

import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import me.proton.android.core.presentation.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Custom input view tests.
 *
 * @author Dino Kadrikj.
 */

@RunWith(RobolectricTestRunner::class)
class ProtonInputTest {

    private lateinit var protonInput: ProtonInput
    private lateinit var activity: AppCompatActivity

    @Before
    fun beforeEveryTest() {
        val activityController = Robolectric.buildActivity(AppCompatActivity::class.java)
        activity = activityController.get()
        protonInput =
            LayoutInflater.from(activity)
                .inflate(R.layout.proton_input, ProtonInput(activity, null)) as ProtonInput

        val parent = FrameLayout(activity)
        parent.addView(protonInput)
        activity.windowManager.addView(parent, WindowManager.LayoutParams(500, 500))
    }

    @Test
    fun `view ordering is correct`() {
        val inputViewParent = protonInput.getChildAt(1)
        assertTrue(inputViewParent is FrameLayout)
        assertEquals(2, inputViewParent.childCount)
        val editText = inputViewParent.getChildAt(0)
        val toggleButton = inputViewParent.getChildAt(1)
        assertTrue(editText is EditText)
        assertTrue(toggleButton is ToggleButton)
    }

    @Test
    fun `label initially is gone`() {
        val labelView = protonInput.findViewById<TextView>(R.id.label)
        assertEquals(labelView.visibility, View.GONE)
    }

    @Test
    fun `set label makes it visible`() {
        val labelView = protonInput.findViewById<TextView>(R.id.label)
        protonInput.inputLabel = "test label"
        assertEquals(View.VISIBLE, labelView.visibility)
        assertEquals("test label", labelView.text.toString())
    }

    @Test
    fun `set label NULL makes it gone and text emmpty`() {
        val labelView = protonInput.findViewById<TextView>(R.id.label)
        protonInput.inputLabel = null
        assertEquals(View.GONE, labelView.visibility)
        assertEquals("", labelView.text.toString())
    }

    @Test
    fun `set assistvive text makes it visible`() {
        val assistiveText = protonInput.findViewById<TextView>(R.id.assistiveText)
        protonInput.inputAssistiveText = "test assistive"
        assertEquals(View.VISIBLE, assistiveText.visibility)
        assertEquals("test assistive", assistiveText.text.toString())
    }

    @Test
    fun `set assistvive text NULL makes it gone and text empty`() {
        val assistiveText = protonInput.findViewById<TextView>(R.id.assistiveText)
        protonInput.inputAssistiveText = null
        assertEquals(View.GONE, assistiveText.visibility)
        assertEquals("", assistiveText.text.toString())
    }

    @Test
    fun `set text works correctly`() {
        val inputViewParent = protonInput.getChildAt(1) as FrameLayout
        val editText = inputViewParent.getChildAt(0) as EditText
        protonInput.inputText = "test text"
        assertEquals(View.VISIBLE, editText.visibility)
        assertEquals("test text", editText.text.toString())
    }

    @Test
    fun `set text NULL does not make input gone and text is empty`() {
        val inputViewParent = protonInput.getChildAt(1) as FrameLayout
        val editText = inputViewParent.getChildAt(0) as EditText
        protonInput.inputText = null
        assertEquals(View.VISIBLE, editText.visibility)
        assertEquals("", editText.text.toString())
    }

    @Test
    fun `display error message works as expected`() {
        val inputViewParent = protonInput.getChildAt(1) as FrameLayout
        val editText = inputViewParent.getChildAt(0) as EditText
        val labelView = protonInput.findViewById<TextView>(R.id.label)
        val assistiveText = protonInput.findViewById<TextView>(R.id.assistiveText)

        protonInput.inputLabel = "test label"
        protonInput.inputAssistiveText = "test assistive"
        protonInput.setInputError()
        assertEquals(ContextCompat.getColor(protonInput.context, R.color.signal_error), labelView.currentTextColor)
        assertEquals(ContextCompat.getColor(protonInput.context, R.color.signal_error), assistiveText.currentTextColor)

        val expectedDrawable = editText.resources.getDrawable(R.drawable.default_edit_text_error, null)
        val drawable = editText.background
        assertEquals(shadowOf(expectedDrawable).createdFromResId, shadowOf(drawable).createdFromResId)
    }

    @Test
    fun `set input error custom message shown`() {
        val inputViewParent = protonInput.getChildAt(1) as FrameLayout
        val editText = inputViewParent.getChildAt(0) as EditText
        val labelView = protonInput.findViewById<TextView>(R.id.label)
        val assistiveText = protonInput.findViewById<TextView>(R.id.assistiveText)

        protonInput.inputLabel = "test label"
        protonInput.inputAssistiveText = "test assistive"
        protonInput.inputText = "test text"
        protonInput.setInputError("test error message")

        assertEquals("test text", editText.text.toString())
        assertEquals("test label", labelView.text.toString())
        assertEquals("test error message", assistiveText.text.toString())
    }


    @Test
    fun `remove input error text works correctly`() {
        val inputViewParent = protonInput.getChildAt(1) as FrameLayout
        val editText = inputViewParent.getChildAt(0) as EditText
        val labelView = protonInput.findViewById<TextView>(R.id.label)
        val assistiveText = protonInput.findViewById<TextView>(R.id.assistiveText)

        protonInput.inputLabel = "test label"
        protonInput.inputAssistiveText = "test assistive"
        protonInput.inputText = "test text"
        protonInput.setInputError("test error message")

        protonInput.removeInputError()
        assertEquals("test text", editText.text.toString())
        assertEquals("test label", labelView.text.toString())
        assertEquals("test assistive", assistiveText.text.toString())
    }

    @Test
    fun `remove input error colors works correctly`() {
        val inputViewParent = protonInput.getChildAt(1) as FrameLayout
        val editText = inputViewParent.getChildAt(0) as EditText
        val labelView = protonInput.findViewById<TextView>(R.id.label)
        val assistiveText = protonInput.findViewById<TextView>(R.id.assistiveText)

        protonInput.inputLabel = "test label"
        protonInput.inputAssistiveText = "test assistive"
        protonInput.inputText = "test text"

        val originalLabelColor = labelView.currentTextColor
        val originalAssistiveColor = assistiveText.currentTextColor
        val originalInputEditTextBackground = editText.background

        protonInput.setInputError("test error message")
        protonInput.removeInputError()

        assertEquals(
            shadowOf(originalInputEditTextBackground).createdFromResId,
            shadowOf(editText.background).createdFromResId
        )
        assertEquals(originalLabelColor, labelView.currentTextColor)
        assertEquals(originalAssistiveColor, assistiveText.currentTextColor)
    }

    @Test
    fun `setting inputType sets the correct type to the EditText`() {
        protonInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        assertTrue(protonInput.isPasswordInput)
    }
}
