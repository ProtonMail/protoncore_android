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

package me.proton.core.push.hilt

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.push.data.local.PushLocalDataSourceImpl
import me.proton.core.push.data.remote.PushRemoteDataSourceImpl
import me.proton.core.push.data.repository.PushRepositoryImpl
import me.proton.core.push.domain.local.PushLocalDataSource
import me.proton.core.push.domain.remote.PushRemoteDataSource
import me.proton.core.push.domain.repository.PushRepository

@Module
@InstallIn(SingletonComponent::class)
public interface PushModule {
    @Binds
    public fun bindPushRepository(impl: PushRepositoryImpl): PushRepository

    @Binds
    public fun bindPushRemoteDataSource(impl: PushRemoteDataSourceImpl): PushRemoteDataSource

    @Binds
    public fun bindPushLocalDataSource(impl: PushLocalDataSourceImpl): PushLocalDataSource
}
