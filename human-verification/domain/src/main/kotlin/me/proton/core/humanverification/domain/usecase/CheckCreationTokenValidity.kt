/*
 * Copyright (c) 2021 Proton Technologies AG
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

package me.proton.core.humanverification.domain.usecase

import me.proton.core.humanverification.domain.repository.UserVerificationRepository
import me.proton.core.user.domain.entity.CreateUserType
import javax.inject.Inject

class CheckCreationTokenValidity @Inject constructor(
    private val userVerificationRepository: UserVerificationRepository
) {
    suspend operator fun invoke(token: String, tokenType: String, type: CreateUserType) =
        userVerificationRepository.checkCreationTokenValidity(
            token = token,
            tokenType = tokenType,
            type = type.value
        )
}
