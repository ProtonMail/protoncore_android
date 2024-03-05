/*
 * Copyright (c) 2024 Proton Technologies AG
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

import kotlinx.coroutines.flow.first
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.UserRepository
import javax.inject.Inject

class GetPrimaryUser @Inject constructor(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(): User? =
        accountRepository.getPrimaryUserId().first()?.let { userRepository.getUser(it) }
}
