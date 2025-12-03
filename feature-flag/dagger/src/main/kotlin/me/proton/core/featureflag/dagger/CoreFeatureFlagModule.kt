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

import android.content.Context
import android.os.SystemClock
import dagger.Binds
import dagger.BindsOptionalOf
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.proton.core.featureflag.data.FeatureFlagManagerImpl
import me.proton.core.featureflag.data.R
import me.proton.core.featureflag.data.listener.FeatureDisabledListenerImpl
import me.proton.core.featureflag.data.local.FeatureFlagLocalDataSourceImpl
import me.proton.core.featureflag.data.remote.FeatureFlagRemoteDataSourceImpl
import me.proton.core.featureflag.data.remote.worker.FeatureFlagWorkerManagerImpl
import me.proton.core.featureflag.data.repository.FeatureFlagRepositoryImpl
import me.proton.core.featureflag.domain.FeatureFlagManager
import me.proton.core.featureflag.domain.FeatureFlagWorkerManager
import me.proton.core.featureflag.domain.repository.FeatureFlagContextProvider
import me.proton.core.featureflag.domain.repository.FeatureFlagLocalDataSource
import me.proton.core.featureflag.domain.repository.FeatureFlagRemoteDataSource
import me.proton.core.featureflag.domain.repository.FeatureFlagRepository
import me.proton.core.network.domain.feature.FeatureDisabledListener
import me.proton.core.network.domain.session.SessionProvider
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Module
@InstallIn(SingletonComponent::class)
public class CoreFeatureFlagModule {

    @Provides
    @Singleton
    public fun provideFeatureDisabledListener(
        @ApplicationContext appContext: Context,
        featureFlagRepositoryProvider: Provider<FeatureFlagRepository>,
        sessionProvider: SessionProvider,
    ): FeatureDisabledListener = FeatureDisabledListenerImpl(
        featureFlagRepositoryProvider = featureFlagRepositoryProvider,
        sessionProvider = sessionProvider,
        minimumFetchInterval = appContext.resources.getInteger(
            R.integer.core_feature_feature_flag_on_feature_disabled_minimum_fetch_interval_seconds
        ).toDuration(DurationUnit.SECONDS),
        monoClock = { SystemClock.elapsedRealtime() }
    )
}

@Module
@InstallIn(SingletonComponent::class)
public interface CoreFeatureFlagBindsModule {

    @Binds
    @Singleton
    public fun bindFeatureFlagLocalDataSource(impl: FeatureFlagLocalDataSourceImpl): FeatureFlagLocalDataSource

    @Binds
    @Singleton
    public fun bindFeatureFlagRemoteDataSource(impl: FeatureFlagRemoteDataSourceImpl): FeatureFlagRemoteDataSource

    @Binds
    @Singleton
    public fun bindRepository(featureFlagRepositoryImpl: FeatureFlagRepositoryImpl): FeatureFlagRepository

    @Binds
    @Singleton
    public fun bindManager(featureFlagManagerImpl: FeatureFlagManagerImpl): FeatureFlagManager

    @Binds
    @Singleton
    public fun bindWorkerManager(featureFlagWorkerManagerImpl: FeatureFlagWorkerManagerImpl): FeatureFlagWorkerManager

    @BindsOptionalOf
    public fun optionalFeatureFlagContextProvider(): FeatureFlagContextProvider
}
