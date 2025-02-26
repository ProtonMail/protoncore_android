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
import me.proton.core.biometric.domain.BiometricAuthenticator
import me.proton.core.biometric.domain.BiometricAuthenticator.DeviceCredential
import me.proton.core.biometric.domain.BiometricAuthenticator.Strong
import me.proton.core.biometric.domain.BiometricAuthenticator.Weak

public fun Set<BiometricAuthenticator>.toAndroidXAuthenticatorType(): Int = fold(0) { acc, authenticator ->
    acc or when (authenticator) {
        DeviceCredential -> BiometricManager.Authenticators.DEVICE_CREDENTIAL
        Strong -> BiometricManager.Authenticators.BIOMETRIC_STRONG
        Weak -> BiometricManager.Authenticators.BIOMETRIC_WEAK
    }
}
