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

public interface PrepareBiometricAuth {
    /** Prepares the biometric authentication flow.
     * This method should be called each time the activity is (re-)created (in `onCreate`).
     * Note: if biometric auth is in progress, and activity is re-created,
     * you just need to call [invoke] again, without the need to [launch][BiometricAuthLauncher.launch],
     * and the callback will be delivered to [onResult].
     * @return A [BiometricAuthLauncher] instance that can be used to launch the biometric authentication flow.
     */
    public operator fun invoke(
        onResult: (BiometricAuthResult) -> Unit
    ): BiometricAuthLauncher
}
