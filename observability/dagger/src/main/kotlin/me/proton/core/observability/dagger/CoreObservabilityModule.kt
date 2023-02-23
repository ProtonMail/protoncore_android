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

package me.proton.core.observability.dagger

import android.os.SystemClock
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.observability.data.IsObservabilityEnabledImpl
import me.proton.core.observability.data.ObservabilityRepositoryImpl
import me.proton.core.observability.data.usecase.SendObservabilityEventsImpl
import me.proton.core.observability.data.worker.ObservabilityWorkerManagerImpl
import me.proton.core.observability.domain.ObservabilityRepository
import me.proton.core.observability.domain.ObservabilityTimeTracker
import me.proton.core.observability.domain.ObservabilityWorkerManager
import me.proton.core.observability.domain.usecase.IsObservabilityEnabled
import me.proton.core.observability.domain.usecase.SendObservabilityEvents
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public interface CoreObservabilityModule {
    @Binds
    public fun bindSendObservabilityEvents(impl: SendObservabilityEventsImpl): SendObservabilityEvents

    @Binds
    public fun bindIsObservabilityEnabled(impl: IsObservabilityEnabledImpl): IsObservabilityEnabled

    @Binds
    public fun bindObservabilityRepository(impl: ObservabilityRepositoryImpl): ObservabilityRepository

    @Binds
    public fun bindObservabilityWorkerManager(impl: ObservabilityWorkerManagerImpl): ObservabilityWorkerManager

    public companion object {
        @Provides
        @Singleton
        public fun provideObservabilityTimeTracker(): ObservabilityTimeTracker =
            ObservabilityTimeTracker(clockMillis = { SystemClock.elapsedRealtime() })
    }
}
