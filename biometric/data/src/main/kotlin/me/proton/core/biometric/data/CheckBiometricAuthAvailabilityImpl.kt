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

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE
import androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
import androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
import androidx.biometric.BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED
import androidx.biometric.BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED
import androidx.biometric.BiometricManager.BIOMETRIC_STATUS_UNKNOWN
import androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
import dagger.hilt.android.qualifiers.ApplicationContext
import me.proton.core.biometric.domain.AuthenticatorsResolver
import me.proton.core.biometric.domain.BiometricAuthenticator
import me.proton.core.biometric.domain.CheckBiometricAuthAvailability
import me.proton.core.biometric.domain.CheckBiometricAuthAvailability.Result
import me.proton.core.biometric.domain.CheckBiometricAuthAvailability.Result.Failure
import me.proton.core.biometric.domain.CheckBiometricAuthAvailability.Result.Success
import javax.inject.Inject

public class CheckBiometricAuthAvailabilityImpl internal constructor(
    private val biometricManager: BiometricManager
) : CheckBiometricAuthAvailability {
    @Inject
    public constructor(@ApplicationContext context: Context) : this(BiometricManager.from(context))

    override fun invoke(
        allowedAuthenticators: Set<BiometricAuthenticator>,
        authenticatorsResolver: AuthenticatorsResolver
    ): Result {
        val resolved = authenticatorsResolver(allowedAuthenticators)
        return canAuthenticate(resolved)
    }

    private fun canAuthenticate(allowed: Set<BiometricAuthenticator>): Result {
        return when (val code = biometricManager.canAuthenticate(allowed.toAndroidXAuthenticatorType())) {
            BIOMETRIC_SUCCESS -> Success
            BIOMETRIC_STATUS_UNKNOWN -> Failure.Unknown
            BIOMETRIC_ERROR_UNSUPPORTED -> Failure.Unsupported
            BIOMETRIC_ERROR_HW_UNAVAILABLE -> Failure.HardwareUnavailable
            BIOMETRIC_ERROR_NONE_ENROLLED -> Failure.NotEnrolled
            BIOMETRIC_ERROR_NO_HARDWARE -> Failure.NoHardware
            BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> Failure.SecurityUpdateRequired
            else -> Failure.Unexpected(code)
        }
    }
}
