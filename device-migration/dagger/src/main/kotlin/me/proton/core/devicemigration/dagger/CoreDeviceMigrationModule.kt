/*
 * Copyright (c) 2025 Proton AG
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

package me.proton.core.devicemigration.dagger

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import me.proton.core.devicemigration.data.feature.IsEasyDeviceMigrationEnabledImpl
import me.proton.core.devicemigration.data.usecase.IsEasyDeviceMigrationAvailableImpl
import me.proton.core.devicemigration.domain.feature.IsEasyDeviceMigrationEnabled
import me.proton.core.devicemigration.domain.usecase.GenerateEdmCode
import me.proton.core.devicemigration.domain.usecase.IsEasyDeviceMigrationAvailable
import me.proton.core.devicemigration.domain.usecase.ObserveEdmCode

@Module
@InstallIn(SingletonComponent::class)
public interface CoreDeviceMigrationModule {
    @Binds
    public fun bindIsEasyDeviceMigrationAvailable(
        impl: IsEasyDeviceMigrationAvailableImpl
    ): IsEasyDeviceMigrationAvailable
}

@Module
@InstallIn(ViewModelComponent::class)
public class CoreDeviceMigrationViewModelModule {
    @Provides
    @ViewModelScoped
    public fun provideObserveEdmCode(generateEdmCode: GenerateEdmCode): ObserveEdmCode = ObserveEdmCode(generateEdmCode)
}

@Module
@InstallIn(SingletonComponent::class)
public interface CoreDeviceMigrationFeaturesModule {
    @Binds
    public fun bindIsEasyDeviceMigrationDisabled(impl: IsEasyDeviceMigrationEnabledImpl): IsEasyDeviceMigrationEnabled
}
