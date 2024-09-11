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

package me.proton.core.auth.data.repository

import me.proton.core.auth.data.api.AuthDeviceApi
import me.proton.core.auth.domain.entity.MemberDevice
import me.proton.core.auth.domain.repository.MemberDeviceRemoteDataSource
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.network.data.ApiProvider
import javax.inject.Inject

class MemberDeviceRemoteDataSourceImpl @Inject constructor(
    private val provider: ApiProvider
) : MemberDeviceRemoteDataSource {
    override suspend fun getPendingMemberDevices(
        sessionUserId: SessionUserId
    ): List<MemberDevice> = provider.get<AuthDeviceApi>(sessionUserId).invoke {
        getPendingMemberDevices().devices.map { it.toMemberDevice(sessionUserId) }
    }.valueOrThrow
}
