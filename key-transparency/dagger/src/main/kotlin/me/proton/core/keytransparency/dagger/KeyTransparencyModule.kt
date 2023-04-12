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

package me.proton.core.keytransparency.dagger

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.keytransparency.data.repository.KeyTransparencyRepositoryImpl
import me.proton.core.keytransparency.data.usecase.GetHostTypeImpl
import me.proton.core.keytransparency.data.usecase.IsKeyTransparencyEnabledImpl
import me.proton.core.keytransparency.data.usecase.VerifyEpochGolangImpl
import me.proton.core.keytransparency.data.usecase.VerifyProofGolangImpl
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.keytransparency.domain.usecase.GetHostType
import me.proton.core.keytransparency.domain.usecase.IsKeyTransparencyEnabled
import me.proton.core.keytransparency.domain.usecase.VerifyEpoch
import me.proton.core.keytransparency.domain.usecase.VerifyProof

@Module
@InstallIn(SingletonComponent::class)
internal interface KeyTransparencyModule {
    @Binds
    fun provideKeyTransparencyRepository(impl: KeyTransparencyRepositoryImpl): KeyTransparencyRepository

    @Binds
    fun provideProofVerifier(impl: VerifyProofGolangImpl): VerifyProof

    @Binds
    fun provideEpochVerifier(impl: VerifyEpochGolangImpl): VerifyEpoch

    @Binds
    fun provideIsKeyTransparencyEnabled(impl: IsKeyTransparencyEnabledImpl): IsKeyTransparencyEnabled

    @Binds
    fun provideGetHostType(impl: GetHostTypeImpl): GetHostType
}
