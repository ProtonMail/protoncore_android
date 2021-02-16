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
import me.proton.core.user.domain.entity.Domain
import me.proton.core.user.domain.repository.DomainRepository
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.user.domain.repository.UserSettingRepository
import javax.inject.Inject

/**
 * Availability of domains and username for the address creation purposes.
 */
class UsernameDomainAvailability @Inject constructor(
    private val userRepository: UserRepository,
    private val domainRepository: DomainRepository,
    private val userSettingRepository: UserSettingRepository
) {
    suspend fun getDomains(): List<Domain> = domainRepository.getAvailableDomains()

    suspend fun getUser(userId: UserId) = userRepository.getUser(userId)

    suspend fun isUsernameAvailable(userId: UserId, username: String): Boolean {
        check(username.isNotBlank()) { "Username must not be blank." }

        val user = userRepository.getUser(userId)
        if (user.name == username) return true

        return userSettingRepository.isUsernameAvailable(username)
    }
}
