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

package me.proton.core.userrecovery.data

import me.proton.core.domain.entity.SessionUserId
import me.proton.core.user.domain.extension.hasMigratedKey
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.userrecovery.domain.CanUserDeviceRecover
import javax.inject.Inject

class CanUserDeviceRecoverImpl @Inject constructor(
    private val userAddressRepository: UserAddressRepository
) : CanUserDeviceRecover {

    override suspend fun invoke(sessionUserId: SessionUserId): Boolean =
        userAddressRepository.getAddresses(sessionUserId).hasMigratedKey()
}

