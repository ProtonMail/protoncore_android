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

package me.proton.core.eventmanager.dagger

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.eventmanager.data.EventManagerConfigProviderImpl
import me.proton.core.eventmanager.data.EventManagerFactory
import me.proton.core.eventmanager.data.EventManagerProviderImpl
import me.proton.core.eventmanager.data.IsCoreEventManagerEnabledImpl
import me.proton.core.eventmanager.data.repository.EventMetadataRepositoryImpl
import me.proton.core.eventmanager.data.work.EventWorkerManagerImpl
import me.proton.core.eventmanager.domain.EventListener
import me.proton.core.eventmanager.domain.EventManagerConfigProvider
import me.proton.core.eventmanager.domain.EventManagerProvider
import me.proton.core.eventmanager.domain.IsCoreEventManagerEnabled
import me.proton.core.eventmanager.domain.repository.EventMetadataRepository
import me.proton.core.eventmanager.domain.work.EventWorkerManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public interface CoreEventManagerModule {

    @Binds
    @Singleton
    public fun provideIsCoreEventManagerEnabled(impl: IsCoreEventManagerEnabledImpl): IsCoreEventManagerEnabled

    @Binds
    @Singleton
    public fun provideEventManagerConfigProvider(impl: EventManagerConfigProviderImpl): EventManagerConfigProvider

    @Binds
    @Singleton
    public fun provideEventMetadataRepository(impl: EventMetadataRepositoryImpl): EventMetadataRepository

    @Binds
    @Singleton
    public fun provideEventWorkManager(impl: EventWorkerManagerImpl): EventWorkerManager

    public companion object {
        @Provides
        @Singleton
        @JvmSuppressWildcards
        public fun provideEventManagerProvider(
            eventManagerFactory: EventManagerFactory,
            eventManagerConfigProvider: EventManagerConfigProvider,
            eventListeners: Set<EventListener<*, *>>
        ): EventManagerProvider =
            EventManagerProviderImpl(eventManagerFactory, eventManagerConfigProvider, eventListeners)
    }
}
