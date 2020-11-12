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

import android.os.Build
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import me.proton.core.presentation.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Custom progress button tests.
 *
 * @author Dino Kadrikj.
 */
@Config(sdk = [Build.VERSION_CODES.M])
@RunWith(RobolectricTestRunner::class)
class ProtonProgressButtonAttributesTest {

    private lateinit var protonProgressButton: ProtonProgressButton
    private lateinit var activity: AppCompatActivity
    private lateinit var parent: FrameLayout

    @Before
    fun beforeEveryTest() {
        val activityController = Robolectric.buildActivity(AppCompatActivity::class.java)
        activity = activityController.get()
        parent = FrameLayout(activity)
        activity.windowManager.addView(parent, WindowManager.LayoutParams(500, 500))
    }

    @Test
    fun `test autoLoading attribute FALSE is correct`() {
        val attributes = Robolectric.buildAttributeSet()
        attributes.addAttribute(R.attr.autoLoading, "false")

        protonProgressButton = ProtonProgressButton(activity, attributes.build())

        parent.addView(protonProgressButton)

        val drawables = protonProgressButton.compoundDrawables
        drawables.forEach {
            assertNull(it)
        }
    }

    @Test
    fun `test autoLoading attribute TRUE is correct`() {
        val attributes = Robolectric.buildAttributeSet()
        attributes.addAttribute(R.attr.autoLoading, "true")

        protonProgressButton = ProtonProgressButton(activity, attributes.build())

        parent.addView(protonProgressButton)

        val drawables = protonProgressButton.compoundDrawables
        val loadingSpinnerDrawable = drawables[2]
        assertTrue(loadingSpinnerDrawable != null)
        assertTrue(loadingSpinnerDrawable.isVisible)
    }

    @Test
    fun `test initialState attribute idle is correct`() {
        val attributes = Robolectric.buildAttributeSet()
        attributes.addAttribute(R.attr.initialState, "idle")

        protonProgressButton = ProtonProgressButton(activity, attributes.build())

        parent.addView(protonProgressButton)

        assertFalse(protonProgressButton.isActivated)
        val drawables = protonProgressButton.compoundDrawables
        drawables.forEach {
            assertNull(it)
        }
    }

    @Test
    fun `test initialState attribute loading is correct`() {
        val attributes = Robolectric.buildAttributeSet()
        attributes.addAttribute(R.attr.initialState, "loading")

        protonProgressButton = ProtonProgressButton(activity, attributes.build())

        parent.addView(protonProgressButton)

        assertTrue(protonProgressButton.isActivated)
        val drawables = protonProgressButton.compoundDrawables
        val loadingSpinnerDrawable = drawables[2]
        assertTrue(loadingSpinnerDrawable != null)
        assertTrue(loadingSpinnerDrawable.isVisible)
    }
}
