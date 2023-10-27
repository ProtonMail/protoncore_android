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

package me.proton.core.presentation.utils

import android.view.View
import io.mockk.mockk
import io.mockk.verify
import me.proton.core.presentation.ui.view.AdditionalOnFocusChangeListener
import me.proton.core.presentation.ui.view.MultipleFocusChangeListener
import org.junit.Test

class MultipleFocusChangeListenerTest {
    private val tested = MultipleFocusChangeListener()

    @Test
    fun `multiple focus listener register all invoked`() {
        val listener1 = mockk<View.OnFocusChangeListener>(relaxed = true)
        val listener2 = mockk<AdditionalOnFocusChangeListener>(relaxed = true)
        val view = mockk<View>()
        tested.addListener(listener1)
        tested.addListener(listener2)

        tested.onFocusChange(view, true)

        verify { listener1.onFocusChange(view, true) }
        verify { listener2.onFocusChange(view, true) }
    }

    @Test
    fun `multiple focus listener unregister`() {
        val listener1 = mockk<View.OnFocusChangeListener>(relaxed = true)
        val listener2 = mockk<AdditionalOnFocusChangeListener>(relaxed = true)
        val view = mockk<View>()
        tested.addListener(listener1)
        tested.addListener(listener2)

        tested.onFocusChange(view, true)

        tested.addListener(null)

        tested.onFocusChange(view, true)

        verify(exactly = 1) { listener1.onFocusChange(view, true) }
        verify(exactly = 2) { listener2.onFocusChange(view, true) }
    }

    @Test
    fun `multiple focus additional listener unregister`() {
        val listener1 = mockk<View.OnFocusChangeListener>(relaxed = true)
        val listener2 = mockk<AdditionalOnFocusChangeListener>(relaxed = true)
        val view = mockk<View>()
        tested.addListener(listener1)
        tested.addListener(listener2)

        tested.onFocusChange(view, true)

        tested.addListener(null)
        tested.removeAdditionalOnFocusChangeListener()

        tested.onFocusChange(view, true)

        verify(exactly = 1) { listener1.onFocusChange(view, true) }
        verify(exactly = 1) { listener2.onFocusChange(view, true) }
    }
}