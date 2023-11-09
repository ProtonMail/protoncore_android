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

package me.proton.core.humanverification.presentation.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import me.proton.core.humanverification.presentation.ui.hv3.HV3DialogFragment
import kotlin.test.Test
import kotlin.test.assertIs

class UiUtilsKtTest {
    @Test
    fun `show hv3 fragment`() {
        // GIVEN
        val transaction = mockk<FragmentTransaction>(relaxed = true)
        val fm = mockk<FragmentManager> {
            every { beginTransaction() } returns transaction
            every { findFragmentByTag(any()) } returns null
        }

        // WHEN
        fm.showHumanVerification(
            HumanVerificationVersion.HV3,
            clientId = "client-id",
            clientIdType = "session",
            verificationToken = "token",
            verificationMethods = listOf()
        )

        // THEN
        val fragmentSlot = slot<Fragment>()
        verify { transaction.add(capture(fragmentSlot), TAG_HUMAN_VERIFICATION_DIALOG) }
        assertIs<HV3DialogFragment>(fragmentSlot.captured)
    }

    @Test
    fun `fragment is already added`() {
        // GIVEN
        val fm = mockk<FragmentManager> {
            every { findFragmentByTag(any()) } returns mockk()
        }

        // WHEN
        fm.showHumanVerification(
            HumanVerificationVersion.HV3,
            clientId = "client-id",
            clientIdType = "session",
            verificationToken = "token",
            verificationMethods = listOf()
        )

        // THEN
        verify(exactly = 0) { fm.beginTransaction() }
    }
}
