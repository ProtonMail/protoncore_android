/*
 * Copyright (c) 2023 Proton AG
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

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class StringBoxTest {
    @Test
    fun plainString() {
        val context = mockk<Context> {
            every { resources } returns mockk()
        }
        assertEquals("test", StringBox("test").get(context))
    }

    @Test
    fun resourceString() {
        val resId = 1
        val context = mockk<Context> {
            every { resources } returns mockk {
                every { getString(resId) } returns "test"
            }
        }
        assertEquals("test", StringBox(resId).get(context))
    }
}
