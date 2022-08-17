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

package me.proton.core.humanverification.dagger

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.humanverification.data.HumanVerificationListenerImpl
import me.proton.core.humanverification.data.HumanVerificationManagerImpl
import me.proton.core.humanverification.data.HumanVerificationProviderImpl
import me.proton.core.humanverification.data.repository.HumanVerificationRepositoryImpl
import me.proton.core.humanverification.data.repository.UserVerificationRepositoryImpl
import me.proton.core.humanverification.data.utils.NetworkRequestOverriderImpl
import me.proton.core.humanverification.domain.HumanVerificationExternalInput
import me.proton.core.humanverification.domain.HumanVerificationExternalInputImpl
import me.proton.core.humanverification.domain.HumanVerificationManager
import me.proton.core.humanverification.domain.HumanVerificationWorkflowHandler
import me.proton.core.humanverification.domain.repository.HumanVerificationRepository
import me.proton.core.humanverification.domain.repository.UserVerificationRepository
import me.proton.core.humanverification.domain.utils.NetworkRequestOverrider
import me.proton.core.network.domain.humanverification.HumanVerificationListener
import me.proton.core.network.domain.humanverification.HumanVerificationProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public interface CoreHumanVerificationModule {

    @Binds
    @Singleton
    public fun provideHumanVerificationConfiguration(impl: HumanVerificationExternalInputImpl): HumanVerificationExternalInput

    @Binds
    public fun provideNetworkRequestOverrider(impl: NetworkRequestOverriderImpl): NetworkRequestOverrider

    @Binds
    @Singleton
    public fun provideHumanVerificationListener(impl: HumanVerificationListenerImpl): HumanVerificationListener

    @Binds
    @Singleton
    public fun provideHumanVerificationProvider(impl: HumanVerificationProviderImpl): HumanVerificationProvider

    @Binds
    @Singleton
    public fun provideHumanVerificationRepository(impl: HumanVerificationRepositoryImpl): HumanVerificationRepository

    @Binds
    @Singleton
    public fun provideUserVerificationRepository(impl: UserVerificationRepositoryImpl): UserVerificationRepository

    @Binds
    @Singleton
    public fun bindHumanVerificationManager(humanVerificationManagerImpl: HumanVerificationManagerImpl): HumanVerificationManager

    @Binds
    @Singleton
    public fun bindHumanVerificationWorkflowHandler(humanVerificationManagerImpl: HumanVerificationManagerImpl): HumanVerificationWorkflowHandler
}
