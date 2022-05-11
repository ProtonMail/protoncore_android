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

package me.proton.core.push.data.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.push.domain.entity.PushId
import me.proton.core.push.domain.remote.PushRemoteDataSource
import javax.inject.Inject
import me.proton.core.network.data.ApiProvider
import me.proton.core.push.data.remote.resource.toPush
import me.proton.core.push.domain.entity.Push

public class PushRemoteDataSourceImpl @Inject constructor(
    private val apiProvider: ApiProvider,
) : PushRemoteDataSource {

    override suspend fun getAllPushes(userId: UserId): List<Push> {
        return apiProvider.get<PushApi>(userId).invoke {
            getAllPushes().pushes.map { pushResource ->
                pushResource.toPush(userId)
            }
        }.valueOrThrow
    }

    override suspend fun deletePush(userId: UserId, pushId: PushId) {
        return apiProvider.get<PushApi>(userId).invoke {
            deletePush(pushId.id)
        }.valueOrThrow
    }
}
