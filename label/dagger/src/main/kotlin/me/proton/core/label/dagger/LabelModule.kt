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

package me.proton.core.label.dagger

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.label.data.local.LabelLocalDataSourceImpl
import me.proton.core.label.data.remote.LabelRemoteDataSourceImpl
import me.proton.core.label.data.repository.LabelRepositoryImpl
import me.proton.core.label.domain.repository.LabelLocalDataSource
import me.proton.core.label.domain.repository.LabelRemoteDataSource
import me.proton.core.label.domain.repository.LabelRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class LabelModule {

    @Binds
    abstract fun bindLabelLocalDataSource(impl: LabelLocalDataSourceImpl): LabelLocalDataSource

    @Binds
    abstract fun bindLabelRemoteDataSource(impl: LabelRemoteDataSourceImpl): LabelRemoteDataSource

    @Binds
    abstract fun provideLabelRepository(impl: LabelRepositoryImpl): LabelRepository
}
