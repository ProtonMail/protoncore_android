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
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.user.domain.repository.UserSettingRepository
import javax.inject.Inject

/**
 * Setup the username, then available as [User.name].
 */
class SetupUsername @Inject constructor(
    private val userRepository: UserRepository,
    private val userSettingRepository: UserSettingRepository
) {
    suspend operator fun invoke(
        userId: UserId,
        username: String
    ) {
        val user = userRepository.getUser(userId)
        if (user.name == null) {
            userSettingRepository.setUsername(userId, username)
            val updatedUser = userRepository.getUser(userId, refresh = true)
            check(updatedUser.name == username) { "Username has not been correctly set remotely." }
        } else {
            check(user.name == username) { "Username already set, and cannot be changed." }
        }
    }
}
