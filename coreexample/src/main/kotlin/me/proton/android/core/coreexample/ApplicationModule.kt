/*
 * Copyright (c) 2020 Proton Technologies AG
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

package me.proton.android.core.coreexample

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import me.proton.android.core.coreexample.Constants.BASE_URL
import me.proton.android.core.coreexample.api.CoreExampleApi
import me.proton.android.core.coreexample.api.CoreExampleApiClient
import me.proton.android.core.coreexample.user.User
import me.proton.core.humanverification.data.repository.HumanVerificationLocalRepositoryImpl
import me.proton.core.humanverification.data.repository.HumanVerificationRemoteRepositoryImpl
import me.proton.core.humanverification.domain.CurrentUsername
import me.proton.core.humanverification.domain.repository.HumanVerificationLocalRepository
import me.proton.core.humanverification.domain.repository.HumanVerificationRemoteRepository
import me.proton.core.humanverification.presentation.HumanVerificationChannel
import me.proton.core.humanverification.presentation.entity.HumanVerificationResult
import me.proton.core.humanverification.presentation.utils.HumanVerificationBinder
import me.proton.core.network.data.di.ApiFactory
import me.proton.core.network.data.di.NetworkManager
import me.proton.core.network.data.di.NetworkPrefs
import me.proton.core.network.domain.ApiClient
import me.proton.core.network.domain.ApiManager
import me.proton.core.network.domain.NetworkManager
import me.proton.core.network.domain.NetworkPrefs
import javax.inject.Singleton

/**
 * Application module singleton for Hilt dependencies.
 * @author Dino Kadrikj.
 */
@Module
@InstallIn(ApplicationComponent::class)
object ApplicationModule {

    @CurrentUsername
    @Provides
    fun provideCurrentUsername() = "testcurrentusername"

    @Provides
    fun provideCurrentUser(): User =
        User("testSession", "testAccessToken", "testRefreshToken")

    @Provides
    @Singleton
    fun provideNetworkManager(@ApplicationContext context: Context): NetworkManager =
        NetworkManager(context)

    @Provides
    @Singleton
    fun provideNetworkPrefs(@ApplicationContext context: Context) = NetworkPrefs(context)

    @HumanVerificationChannel
    @Provides
    @Singleton
    fun humanVerificationChannelProvider(): Channel<HumanVerificationResult> = Channel()

    @Provides
    @Singleton
    fun provideHumanVerificationBinder(
        @ApplicationContext context: Context,
        @HumanVerificationChannel channel: Channel<HumanVerificationResult>,
        user: User
    ): HumanVerificationBinder = HumanVerificationBinder(context, channel, user)

    @Provides
    @Singleton
    fun provideApiClient(
        binder: HumanVerificationBinder
    ): ApiClient = CoreExampleApiClient(binder)

    @Provides
    @Singleton
    fun provideApiFactory(
        apiClient: ApiClient,
        networkManager: NetworkManager,
        networkPrefs: NetworkPrefs
    ): ApiFactory = ApiFactory(
        BASE_URL, apiClient, CoreExampleLogger(), networkManager, networkPrefs,
        CoroutineScope(Job() + Dispatchers.Default)
    )

    @Provides
    @CoreExampleApiManager
    fun provideApiManager(apiFactory: ApiFactory, user: User): ApiManager<CoreExampleApi> =
        apiFactory.ApiManager(user, CoreExampleApi::class)

    @Provides
    fun provideLocalRepository(@ApplicationContext context: Context): HumanVerificationLocalRepository =
        HumanVerificationLocalRepositoryImpl(context)

    @Provides
    fun provideRemoteRepository(
        @CurrentUsername currentUsername: String,
        @CoreExampleApiManager apiManager: ApiManager<CoreExampleApi>
    ): HumanVerificationRemoteRepository =
        HumanVerificationRemoteRepositoryImpl(apiManager, currentUsername)
}
