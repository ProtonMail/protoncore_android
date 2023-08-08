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

package me.proton.core.util.android.sentry

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetInstallationIdTest {
    private lateinit var context: Context
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var tested: GetInstallationId

    @BeforeTest
    fun setUp() {
        editor = mockk(relaxed = true)
        sharedPreferences = mockk {
            every { getString(any(), any()) } returns null
            every { edit() } returns editor
        }
        context = mockk {
            every { getSharedPreferences(any(), any()) } returns sharedPreferences
        }
        tested = GetInstallationId(context)
    }

    @Test
    fun `generate id and store it`() {
        // WHEN
        tested("prefs_name", "installationId")

        // THEN
        val installationIdSlot = slot<String>()
        verify { editor.putString("installationId", capture(installationIdSlot)) }
        assertTrue(installationIdSlot.captured.isNotBlank())
    }

    @Test
    fun `return existing id`() {
        // GIVEN
        every { sharedPreferences.getString(any(), any()) } returns "id"

        // WHEN
        val installationId = tested("prefs_name", "installationId")

        // THEN
        assertEquals("id", installationId)
        verify(exactly = 0) { editor.putString(any(), any()) }
    }
}