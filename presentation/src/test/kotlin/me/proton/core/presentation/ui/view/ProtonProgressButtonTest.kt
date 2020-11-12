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
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import me.proton.core.presentation.utils.onClick
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
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
class ProtonProgressButtonTest {

    private lateinit var protonProgressButton: ProtonProgressButton
    private lateinit var activity: AppCompatActivity

    @Before
    fun beforeEveryTest() {
        val activityController = Robolectric.buildActivity(AppCompatActivity::class.java)
        activity = activityController.get()
        protonProgressButton = ProtonProgressButton(activity)

        val parent = FrameLayout(activity)
        parent.addView(protonProgressButton)
        activity.windowManager.addView(parent, WindowManager.LayoutParams(500, 500))
    }

    @Test
    fun `test initial state is correct`() {
        val drawables = protonProgressButton.compoundDrawables
        drawables.forEach {
            assertNull(it)
        }
        assertEquals(View.VISIBLE, protonProgressButton.visibility)
    }

    @Test
    fun `test idle state visibility`() {
        protonProgressButton.setIdle()
        val drawables = protonProgressButton.compoundDrawables
        drawables.forEach {
            assertNull(it)
        }
    }

    @Test
    fun `test idle state activated`() {
        protonProgressButton.setIdle()
        assertFalse(protonProgressButton.isActivated)
    }

    @Test
    fun `test loading state visibility`() {
        protonProgressButton.setLoading()
        val drawables = protonProgressButton.compoundDrawables
        val loadingSpinnerDrawable = drawables[2]
        assertTrue(loadingSpinnerDrawable != null)
        assertTrue(loadingSpinnerDrawable.isVisible)
    }

    @Test
    fun `test loading state activated`() {
        protonProgressButton.setLoading()
        assertTrue(protonProgressButton.isActivated)
    }

    @Test
    fun `test auto loading works correctly`() {
        protonProgressButton.autoLoading = true
        protonProgressButton.onClick {
            // empty, for test purposes
        }
        protonProgressButton.callOnClick()
        val drawables = protonProgressButton.compoundDrawables
        val loadingSpinnerDrawable = drawables[2]
        assertTrue(loadingSpinnerDrawable != null)
    }
}
