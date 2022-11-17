/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.auth.domain.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserAddressManager
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.UserKey
import me.proton.core.user.domain.entity.UserAddressKey
import me.proton.core.user.domain.extension.filterExternal
import me.proton.core.user.domain.extension.filterHasNoKeys
import javax.inject.Inject

/** Setup any missing [address keys][UserAddressKey] for external addresses.
 *
 * Prerequisite: Primary [UserKey.privateKey] must be unlocked (`isLocked == false`).
 *
 * @see [UserManager.unlockWithPassword]
 */
class SetupExternalAddressKeys @Inject constructor(
    private val userAddressManager: UserAddressManager
) {
    suspend operator fun invoke(userId: UserId) {
        userAddressManager.getAddresses(userId)
            .filterExternal()
            .filterHasNoKeys()
            .filter { it.enabled }
            .forEach { userAddress ->
                userAddressManager.createAddressKey(userId, userAddress.addressId, isPrimary = true)
            }
    }
}
