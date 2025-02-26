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

import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import me.proton.core.biometric.domain.BiometricAuthenticator.DeviceCredential
import me.proton.core.biometric.domain.BiometricAuthenticator.Strong
import me.proton.core.biometric.domain.BiometricAuthenticator.Weak
import kotlin.test.Test
import kotlin.test.assertEquals

class BiometricAuthenticatorExtKtTest {
    @Test
    fun `converting to AndroidX authenticator type`() {
        assertEquals(
            DEVICE_CREDENTIAL,
            setOf(DeviceCredential).toAndroidXAuthenticatorType()
        )
        assertEquals(
            BIOMETRIC_STRONG,
            setOf(Strong).toAndroidXAuthenticatorType()
        )
        assertEquals(
            BIOMETRIC_WEAK,
            setOf(Weak).toAndroidXAuthenticatorType()
        )
        assertEquals(
            DEVICE_CREDENTIAL or BIOMETRIC_STRONG,
            setOf(DeviceCredential, Strong).toAndroidXAuthenticatorType()
        )
        assertEquals(
            DEVICE_CREDENTIAL or BIOMETRIC_WEAK,
            setOf(DeviceCredential, Weak).toAndroidXAuthenticatorType()
        )
        assertEquals(
            BIOMETRIC_STRONG or BIOMETRIC_WEAK,
            setOf(Strong, Weak).toAndroidXAuthenticatorType()
        )
        assertEquals(
            DEVICE_CREDENTIAL or BIOMETRIC_STRONG or BIOMETRIC_WEAK,
            setOf(DeviceCredential, Strong, Weak).toAndroidXAuthenticatorType()
        )
    }
}
