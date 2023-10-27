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
import android.view.View.OnClickListener
import io.mockk.mockk
import io.mockk.verify
import me.proton.core.presentation.ui.view.AdditionalOnClickListener
import me.proton.core.presentation.ui.view.MultipleClickListener
import org.junit.Test

class MultipleClickListenerTest {

    private val tested = MultipleClickListener()

    @Test
    fun `multiple click listener register all invoked`() {
        val listener1 = mockk<OnClickListener>(relaxed = true)
        val listener2 = mockk<AdditionalOnClickListener>(relaxed = true)
        val view = mockk<View>()
        tested.addListener(listener1)
        tested.addListener(listener2)

        tested.onClick(view)

        verify { listener1.onClick(view) }
        verify { listener2.onClick(view) }
    }

    @Test
    fun `multiple click listener unregister`() {
        val listener1 = mockk<OnClickListener>(relaxed = true)
        val listener2 = mockk<AdditionalOnClickListener>(relaxed = true)
        val view = mockk<View>()
        tested.addListener(listener1)
        tested.addListener(listener2)

        tested.onClick(view)

        tested.addListener(null)

        tested.onClick(view)

        verify(exactly = 1) { listener1.onClick(view) }
        verify(exactly = 2) { listener2.onClick(view) }
    }

    @Test
    fun `multiple click listener additional unregister`() {
        val listener1 = mockk<OnClickListener>(relaxed = true)
        val listener2 = mockk<AdditionalOnClickListener>(relaxed = true)
        val view = mockk<View>()
        tested.addListener(listener1)
        tested.addListener(listener2)

        tested.onClick(view)

        tested.addListener(null)
        tested.removeAdditionalOnClickListener()

        tested.onClick(view)

        verify(exactly = 1) { listener1.onClick(view) }
        verify(exactly = 1) { listener2.onClick(view) }
    }
}