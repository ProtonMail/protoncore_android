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

package me.proton.core.biometric.domain

public interface CheckBiometricAuthAvailability {
    public operator fun invoke(
        allowedAuthenticators: Set<BiometricAuthenticator> = BiometricAuthenticator.DEFAULT_ALLOWED_AUTHENTICATORS,
        authenticatorsResolver: AuthenticatorsResolver
    ): Result

    public sealed interface Result {
        public data object Success : Result
        public sealed interface Failure : Result {
            /** Unable to determine whether the user can authenticate.
             * May be returned on older OS versions.
             * You can try to call [PrepareBiometricAuth.invoke], but be prepared to handle any errors.
             */
            public data object Unknown : Failure

            /** The specified options are incompatible with the current Android version.*/
            public data object Unsupported : Failure

            /** Hardware is unavailable. Try again later. */
            public data object HardwareUnavailable : Failure

            /** No biometric or device credential is enrolled. */
            public data object NotEnrolled : Failure

            /** No suitable hardware (e. g. no biometric sensor or no keyguard). */
            public data object NoHardware : Failure

            /** A security vulnerability has been discovered with one or more hardware sensors.
             * The affected sensor(s) are unavailable until a security update has addressed the issue.
             */
            public data object SecurityUpdateRequired : Failure

            /** An unexpected error has occurred (undocumented error code). */
            public data class Unexpected(val value: Int) : Failure
        }

        public fun canAttemptBiometricAuth(): Boolean = this is Success || this is Failure.Unknown
    }
}
