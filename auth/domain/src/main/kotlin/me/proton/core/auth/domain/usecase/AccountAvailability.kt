/*
 * Copyright (c) 2023 Proton AG
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
import me.proton.core.user.domain.entity.Domain
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.DomainRepository
import me.proton.core.user.domain.repository.UserRepository
import javax.inject.Inject

/**
 * Availability of accounts (username, internal username@domain, external email).
 */
class AccountAvailability @Inject constructor(
    private val userRepository: UserRepository,
    private val domainRepository: DomainRepository
) {

    suspend fun getDomains(
        userId: UserId?
    ): List<Domain> = domainRepository.getAvailableDomains(userId)

    suspend fun getUser(
        userId: UserId,
        refresh: Boolean
    ): User = userRepository.getUser(userId, refresh)

    suspend fun checkUsernameAuthenticated(
        userId: UserId,
        username: String,
    ) {
        checkUsername(userId, username)
    }

    suspend fun checkUsernameUnauthenticated(
        username: String,
    ) {
        checkUsername(null, username)
    }

    suspend fun checkExternalEmail(email: String) {
        check(email.isNotBlank()) { "Email must not be blank." }
        userRepository.checkExternalEmailAvailable(email)
    }

    private suspend fun checkUsername(
        userId: UserId?,
        username: String
    ) {
        check(username.isNotBlank()) { "Username must not be blank." }
        userRepository.checkUsernameAvailable(sessionUserId = userId, username = username)
    }
}
