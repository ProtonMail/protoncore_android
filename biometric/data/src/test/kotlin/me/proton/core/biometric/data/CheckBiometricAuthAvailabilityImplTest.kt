/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.biometric.data

import androidx.biometric.BiometricManager
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import me.proton.core.biometric.domain.BiometricAuthenticator.DeviceCredential
import me.proton.core.biometric.domain.BiometricAuthenticator.Strong
import me.proton.core.biometric.domain.CheckBiometricAuthAvailability.Result.Failure
import me.proton.core.biometric.domain.CheckBiometricAuthAvailability.Result.Success
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CheckBiometricAuthAvailabilityImplTest {
    @MockK
    private lateinit var biometricManager: BiometricManager

    @BeforeTest
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `device credential success`() {
        every { biometricManager.canAuthenticate(any()) } returns BiometricManager.BIOMETRIC_SUCCESS
        assertEquals(
            Success,
            makeTested()(setOf(DeviceCredential)) { it }
        )
    }

    @Test
    fun `device credential and strong biometric with resolver`() {
        every { biometricManager.canAuthenticate(any()) } returns BiometricManager.BIOMETRIC_SUCCESS
        assertEquals(
            Success,
            makeTested()(setOf(DeviceCredential, Strong)) { setOf(Strong) }
        )
    }

    @Test
    fun `device credential and strong biometric on unsecured device`() {
        every { biometricManager.canAuthenticate(any()) } returns BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
        assertEquals(
            Failure.NotEnrolled,
            makeTested()(setOf(DeviceCredential, Strong)) { it }
        )
    }

    private fun makeTested() = CheckBiometricAuthAvailabilityImpl(biometricManager)
}
