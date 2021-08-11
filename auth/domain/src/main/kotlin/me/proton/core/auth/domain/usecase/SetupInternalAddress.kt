/*
 * Copyright (c) 2020 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
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
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.entity.UserAddressKey
import me.proton.core.user.domain.entity.UserKey
import me.proton.core.user.domain.extension.firstInternalOrNull
import me.proton.core.user.domain.repository.DomainRepository
import me.proton.core.user.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Setup a new internal [UserAddress] and [UserAddressKey], using [User.name].
 *
 * Prerequisite: Primary [UserKey.privateKey] must be unlocked (`isLocked == false`).
 *
 * @see [UserManager.unlockWithPassword]
 */
class SetupInternalAddress @Inject constructor(
    private val userAddressManager: UserAddressManager,
    private val userRepository: UserRepository,
    private val domainRepository: DomainRepository
) {
    suspend operator fun invoke(
        userId: UserId,
        domain: String? = null
    ) {
        val user = userRepository.getUser(userId)
        val username = checkNotNull(user.name) { "Username is needed to setup new internal address." }

        val finalDomain = domain ?: domainRepository.getAvailableDomains().first()

        val address = userAddressManager.getAddresses(userId).firstInternalOrNull()
        if (address == null || address.keys.isEmpty()) {
            userAddressManager.setupInternalAddress(userId, username, finalDomain)
        }
    }
}
