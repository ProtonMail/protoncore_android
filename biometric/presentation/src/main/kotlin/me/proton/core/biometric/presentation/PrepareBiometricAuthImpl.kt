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

import androidx.activity.compose.LocalActivity
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.ERROR_CANCELED
import androidx.biometric.BiometricPrompt.ERROR_HW_NOT_PRESENT
import androidx.biometric.BiometricPrompt.ERROR_HW_UNAVAILABLE
import androidx.biometric.BiometricPrompt.ERROR_LOCKOUT
import androidx.biometric.BiometricPrompt.ERROR_LOCKOUT_PERMANENT
import androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON
import androidx.biometric.BiometricPrompt.ERROR_NO_BIOMETRICS
import androidx.biometric.BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt.ERROR_NO_SPACE
import androidx.biometric.BiometricPrompt.ERROR_TIMEOUT
import androidx.biometric.BiometricPrompt.ERROR_UNABLE_TO_PROCESS
import androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED
import androidx.biometric.BiometricPrompt.ERROR_VENDOR
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.fragment.app.FragmentActivity
import me.proton.core.biometric.domain.AuthenticatorsResolver
import me.proton.core.biometric.domain.BiometricAuthErrorCode
import me.proton.core.biometric.domain.BiometricAuthResult
import me.proton.core.biometric.domain.BiometricAuthResult.AuthError
import me.proton.core.biometric.domain.BiometricAuthResult.Success
import me.proton.core.biometric.domain.BiometricAuthLauncher
import me.proton.core.biometric.domain.BiometricAuthenticator
import me.proton.core.biometric.domain.PrepareBiometricAuth
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Inject

@Composable
public fun rememberBiometricLauncher(
    onResult: (BiometricAuthResult) -> Unit
): BiometricAuthLauncher {
    val activity = LocalActivity.current as? FragmentActivity?
    return remember {
        when {
            activity != null -> PrepareBiometricAuthImpl(activity).invoke(onResult)
            else -> NoOpBiometricAuthImpl().invoke(onResult)
        }
    }
}

public class PrepareBiometricAuthImpl @Inject constructor(
    private val activity: FragmentActivity
) : PrepareBiometricAuth {
    override fun invoke(
        onResult: (BiometricAuthResult) -> Unit
    ): BiometricAuthLauncher {
        val prompt = BiometricPrompt(activity, AuthCallback(onResult))
        return BiometricAuthLauncherImpl(prompt)
    }
}

private class AuthCallback(
    private val onResult: (BiometricAuthResult) -> Unit
) : BiometricPrompt.AuthenticationCallback() {
    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        onResult(AuthError(errorCode.toAuthErrorCode(), errString))
    }

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        onResult(Success)
    }
}

private class NoOpBiometricAuthImpl : PrepareBiometricAuth {
    override fun invoke(onResult: (BiometricAuthResult) -> Unit) = NoOpBiometricAuthLauncherImpl()
}

private class NoOpBiometricAuthLauncherImpl : BiometricAuthLauncher {
    override fun launch(
        title: CharSequence,
        subtitle: CharSequence?,
        cancelButton: CharSequence,
        confirmationRequired: Boolean,
        allowedAuthenticators: Set<BiometricAuthenticator>,
        authenticatorsResolver: AuthenticatorsResolver
    ) {
        CoreLogger.w("NoOpBiometricAuthLauncherImpl", "Could not launch biometrics: FragmentActivity not found.")
    }
}

@Suppress("CyclomaticComplexMethod")
private fun Int.toAuthErrorCode(): BiometricAuthErrorCode = when (this) {
    ERROR_HW_UNAVAILABLE -> BiometricAuthErrorCode.HwUnavailable
    ERROR_UNABLE_TO_PROCESS -> BiometricAuthErrorCode.UnableToProcess
    ERROR_TIMEOUT -> BiometricAuthErrorCode.Timeout
    ERROR_NO_SPACE -> BiometricAuthErrorCode.NoSpace
    ERROR_CANCELED -> BiometricAuthErrorCode.Canceled
    ERROR_LOCKOUT -> BiometricAuthErrorCode.Lockout
    ERROR_VENDOR -> BiometricAuthErrorCode.Vendor
    ERROR_LOCKOUT_PERMANENT -> BiometricAuthErrorCode.LockoutPermanent
    ERROR_USER_CANCELED -> BiometricAuthErrorCode.UserCanceled
    ERROR_NO_BIOMETRICS -> BiometricAuthErrorCode.NoBiometrics
    ERROR_HW_NOT_PRESENT -> BiometricAuthErrorCode.HwNotPresent
    ERROR_NEGATIVE_BUTTON -> BiometricAuthErrorCode.NegativeButton
    ERROR_NO_DEVICE_CREDENTIAL -> BiometricAuthErrorCode.NoDeviceCredential
    else -> BiometricAuthErrorCode.Unknown
}
