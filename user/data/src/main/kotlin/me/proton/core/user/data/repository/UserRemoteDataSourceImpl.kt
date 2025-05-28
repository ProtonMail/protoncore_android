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

package me.proton.core.user.data.repository

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.user.data.api.UserApi
import me.proton.core.user.data.extension.toUser
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.repository.UserLocalDataSource
import me.proton.core.user.domain.repository.UserRemoteDataSource
import javax.inject.Inject

class UserRemoteDataSourceImpl @Inject constructor(
    private val apiProvider: ApiProvider,
    private val userLocalDataSource: UserLocalDataSource
) : UserRemoteDataSource {
    override suspend fun fetch(userId: UserId): User {
        val credentialLessUser = userLocalDataSource.getCredentialLessUser(userId)
        return credentialLessUser ?: apiProvider.get<UserApi>(userId).invoke {
            getUsers().user.toUser()
        }.valueOrThrow
    }
}
