/*
 * Copyright (c) 2024 Proton AG
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

package me.proton.core.userrecovery.dagger

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.userrecovery.data.IsDeviceRecoveryEnabledImpl
import me.proton.core.userrecovery.data.repository.DeviceRecoveryRepositoryImpl
import me.proton.core.userrecovery.data.worker.UserRecoveryWorkerManagerImpl
import me.proton.core.userrecovery.domain.IsDeviceRecoveryEnabled
import me.proton.core.userrecovery.domain.repository.DeviceRecoveryRepository
import me.proton.core.userrecovery.domain.worker.UserRecoveryWorkerManager

@Module
@InstallIn(SingletonComponent::class)
public interface CoreDeviceRecoveryFeaturesModule {
    @Binds
    public fun bindIsDeviceRecoveryEnabled(
        impl: IsDeviceRecoveryEnabledImpl
    ): IsDeviceRecoveryEnabled
}

@Module
@InstallIn(SingletonComponent::class)
public interface CoreDeviceRecoveryModule {
    @Binds
    public fun bindDeviceRecoveryRepository(impl: DeviceRecoveryRepositoryImpl): DeviceRecoveryRepository

    @Binds
    public fun bindUserRecoveryWorkerManager(impl: UserRecoveryWorkerManagerImpl): UserRecoveryWorkerManager
}
