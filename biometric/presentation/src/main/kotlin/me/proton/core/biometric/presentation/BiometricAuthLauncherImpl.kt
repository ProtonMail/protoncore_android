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

package me.proton.core.biometric.presentation

import androidx.biometric.BiometricPrompt
import me.proton.core.biometric.data.toAndroidXAuthenticatorType
import me.proton.core.biometric.domain.BiometricAuthenticator
import me.proton.core.biometric.domain.BiometricAuthenticator.DeviceCredential
import me.proton.core.biometric.domain.AuthenticatorsResolver
import me.proton.core.biometric.domain.BiometricAuthLauncher

internal class BiometricAuthLauncherImpl(
    private val prompt: BiometricPrompt
) : BiometricAuthLauncher {
    override fun launch(
        title: CharSequence,
        subtitle: CharSequence?,
        cancelButton: CharSequence,
        confirmationRequired: Boolean,
        allowedAuthenticators: Set<BiometricAuthenticator>,
        authenticatorsResolver: AuthenticatorsResolver,
    ) {
        val resolvedAuthenticators = authenticatorsResolver(allowedAuthenticators)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setConfirmationRequired(confirmationRequired)
            .apply {
                // Negative button is incompatible with DeviceCredential
                if (!resolvedAuthenticators.contains(DeviceCredential)) {
                    setNegativeButtonText(cancelButton)
                }
            }
            .setAllowedAuthenticators(resolvedAuthenticators.toAndroidXAuthenticatorType())
            .build()
        prompt.authenticate(promptInfo)
    }
}
