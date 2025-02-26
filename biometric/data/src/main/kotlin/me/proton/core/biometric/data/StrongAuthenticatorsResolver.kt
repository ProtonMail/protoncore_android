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

import android.os.Build
import me.proton.core.biometric.domain.BiometricAuthenticator
import me.proton.core.biometric.domain.BiometricAuthenticator.DeviceCredential
import me.proton.core.biometric.domain.BiometricAuthenticator.Strong
import me.proton.core.biometric.domain.AuthenticatorsResolver
import javax.inject.Inject

public class StrongAuthenticatorsResolver internal constructor(private val sdkInt: Int) : AuthenticatorsResolver {
    @Inject
    public constructor() : this(Build.VERSION.SDK_INT)

    @Suppress("MagicNumber")
    override fun invoke(allowed: Set<BiometricAuthenticator>): Set<BiometricAuthenticator> = allowed.let {
        // DEVICE_CREDENTIAL alone is unsupported prior to API 30
        if (sdkInt < 30 && it == setOf(DeviceCredential)) {
            setOf(DeviceCredential, Strong)
        } else it
    }.let {
        // DEVICE_CREDENTIAL | BIOMETRIC_STRONG is unsupported on API 28-29
        if (sdkInt in arrayOf(28, 29) && it == setOf(DeviceCredential, Strong)) {
            setOf(Strong)
        } else it
    }
}
