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
import dagger.BindsOptionalOf
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.key.domain.repository.PublicAddressVerifier
import me.proton.core.keytransparency.data.KeyTransparencyEnabled
import me.proton.core.keytransparency.data.repository.KeyTransparencyRepositoryImpl
import me.proton.core.keytransparency.data.usecase.GetHostTypeImpl
import me.proton.core.keytransparency.data.usecase.IsKeyTransparencyEnabledImpl
import me.proton.core.keytransparency.data.usecase.VerifyEpochGolangImpl
import me.proton.core.keytransparency.data.usecase.VerifyProofGolangImpl
import me.proton.core.keytransparency.domain.PublicAddressVerifierImpl
import me.proton.core.keytransparency.domain.SignedKeyListChangeListenerImpl
import me.proton.core.keytransparency.domain.repository.KeyTransparencyRepository
import me.proton.core.keytransparency.domain.usecase.GetHostType
import me.proton.core.keytransparency.domain.usecase.IsKeyTransparencyEnabled
import me.proton.core.keytransparency.domain.usecase.VerifyEpoch
import me.proton.core.keytransparency.domain.usecase.VerifyProof
import me.proton.core.user.domain.SignedKeyListChangeListener

@Module
@InstallIn(SingletonComponent::class)
public interface KeyTransparencyModule {
    @Binds
    public fun provideKeyTransparencyRepository(impl: KeyTransparencyRepositoryImpl): KeyTransparencyRepository

    @Binds
    public fun provideProofVerifier(impl: VerifyProofGolangImpl): VerifyProof

    @Binds
    public fun provideEpochVerifier(impl: VerifyEpochGolangImpl): VerifyEpoch

    @Binds
    public fun provideIsKeyTransparencyEnabled(impl: IsKeyTransparencyEnabledImpl): IsKeyTransparencyEnabled

    @Binds
    public fun provideSignedKeyListChangeListener(impl: SignedKeyListChangeListenerImpl): SignedKeyListChangeListener

    @Binds
    public fun providePublicAddressVerifier(impl: PublicAddressVerifierImpl): PublicAddressVerifier

    @Binds
    public fun provideGetHostType(impl: GetHostTypeImpl): GetHostType

    @BindsOptionalOf
    @KeyTransparencyEnabled
    public fun optionalKeyTransparencyEnabled(): Boolean
}




