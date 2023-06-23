/*
 * Copyright (c) 2023 Proton Technologies AG
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

package me.proton.core.notification.dagger

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.notification.data.local.NotificationLocalDataSourceImpl
import me.proton.core.notification.data.remote.NotificationRemoteDataSourceImpl
import me.proton.core.notification.data.repository.NotificationRepositoryImpl
import me.proton.core.notification.domain.repository.NotificationLocalDataSource
import me.proton.core.notification.domain.repository.NotificationRemoteDataSource
import me.proton.core.notification.domain.repository.NotificationRepository

@Module
@InstallIn(SingletonComponent::class)
public abstract class CoreNotificationModule {

    @Binds
    public abstract fun bindNotificationLocalDataSource(impl: NotificationLocalDataSourceImpl): NotificationLocalDataSource

    @Binds
    public abstract fun bindNotificationRemoteDataSource(impl: NotificationRemoteDataSourceImpl): NotificationRemoteDataSource

    @Binds
    public abstract fun provideNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository
}
