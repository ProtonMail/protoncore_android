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

package me.proton.core.metrics.dagger

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.metrics.data.MetricsManagerImpl
import me.proton.core.metrics.data.repository.MetricsRepositoryImpl
import me.proton.core.metrics.domain.MetricsManager
import me.proton.core.metrics.domain.repository.MetricsRepository

@Module
@InstallIn(SingletonComponent::class)
internal abstract class MetricsModule {
    @Binds
    abstract fun provideMetricsRepository(impl: MetricsRepositoryImpl): MetricsRepository

    @Binds
    abstract fun provideMetricsManager(impl: MetricsManagerImpl): MetricsManager
}
