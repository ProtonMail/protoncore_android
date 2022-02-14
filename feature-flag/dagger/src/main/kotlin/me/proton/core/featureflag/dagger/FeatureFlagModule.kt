/*
 * Copyright (c) 2022 Proton Technologies AG
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

package me.proton.core.featureflag.dagger

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.featureflag.data.FeatureFlagManagerImpl
import me.proton.core.featureflag.data.db.FeatureFlagDatabase
import me.proton.core.featureflag.data.repository.FeatureFlagRepositoryImpl
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import me.proton.core.network.data.ApiProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object FeatureFlagModule {

    @Provides
    @Singleton
    internal fun provideFeatureFlagRepositoryImpl(
        db: FeatureFlagDatabase,
        provider: ApiProvider
    ): FeatureFlagRepository = FeatureFlagRepositoryImpl(db, provider)

    @Provides
    @Singleton
    internal fun provideFeatureFlagManagerImpl(
        repository: FeatureFlagRepository
    ): FeatureFlagManager = FeatureFlagManagerImpl(repository)
}
