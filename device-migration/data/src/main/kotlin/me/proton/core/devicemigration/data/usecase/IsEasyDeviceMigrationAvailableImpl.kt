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

package me.proton.core.devicemigration.data.usecase

import me.proton.core.biometric.data.StrongAuthenticatorsResolver
import me.proton.core.biometric.domain.CheckBiometricAuthAvailability
import me.proton.core.devicemigration.domain.feature.IsEasyDeviceMigrationEnabled
import me.proton.core.devicemigration.domain.usecase.IsEasyDeviceMigrationAvailable
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.extension.hasKeys
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.usersettings.domain.usecase.IsUserSettingEnabled
import javax.inject.Inject

public class IsEasyDeviceMigrationAvailableImpl @Inject constructor(
    private val checkBiometricAuthAvailability: CheckBiometricAuthAvailability,
    private val isEasyDeviceMigrationEnabled: IsEasyDeviceMigrationEnabled,
    private val isUserSettingsEnabled: IsUserSettingEnabled,
    private val passphraseRepository: PassphraseRepository,
    private val product: Product,
    private val strongAuthenticatorsResolver: StrongAuthenticatorsResolver,
    private val userManager: UserManager
) : IsEasyDeviceMigrationAvailable {
    /**
     * @param userId If it's not null, the check is performed for the given user as an origin device.
     *  Otherwise if it's null, the check is performed as a target device (the one trying to log in).
     */
    override suspend operator fun invoke(userId: UserId?): Boolean =
        isEasyDeviceMigrationEnabled(userId) && when {
            userId != null -> isAllowedForUser(userId)
            else -> true
        }

    private suspend fun isAllowedForUser(userId: UserId): Boolean = when {
        userId.hasOptedOut() -> false
        !hasBiometrics() -> false
        // If there's no passphrase, we only allow for VPN users without keys
        // (the user may want to migrate from VPN to VPN, so the passphrase is not required then).
        !userId.hasPassphrase() -> product == Product.Vpn && !userId.hasKeys()
        else -> true
    }

    private fun hasBiometrics(): Boolean =
        checkBiometricAuthAvailability(authenticatorsResolver = strongAuthenticatorsResolver).canAttemptBiometricAuth()

    private suspend fun UserId.hasOptedOut(): Boolean = runCatching {
        isUserSettingsEnabled(this) { easyDeviceMigrationOptOut }
    }.getOrNull() ?: true

    private suspend fun UserId.hasPassphrase(): Boolean =
        passphraseRepository.getPassphrase(this) != null

    private suspend fun UserId.hasKeys(): Boolean =
        userManager.getUser(this).hasKeys()
}
