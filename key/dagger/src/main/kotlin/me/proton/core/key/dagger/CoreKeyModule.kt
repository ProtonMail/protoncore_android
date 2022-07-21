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

package me.proton.core.key.dagger

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.key.data.db.KeySaltDatabase
import me.proton.core.key.data.db.PublicAddressDatabase
import me.proton.core.key.data.repository.KeySaltRepositoryImpl
import me.proton.core.key.data.repository.PrivateKeyRepositoryImpl
import me.proton.core.key.data.repository.PublicAddressRepositoryImpl
import me.proton.core.key.domain.repository.KeySaltRepository
import me.proton.core.key.domain.repository.PrivateKeyRepository
import me.proton.core.key.domain.repository.PublicAddressRepository
import me.proton.core.network.data.ApiProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public object CoreKeyModule {

    @Provides
    @Singleton
    public fun provideKeySaltRepository(
        db: KeySaltDatabase,
        provider: ApiProvider
    ): KeySaltRepository = KeySaltRepositoryImpl(db, provider)

    @Provides
    @Singleton
    public fun providePrivateKeyRepository(
        provider: ApiProvider
    ): PrivateKeyRepository = PrivateKeyRepositoryImpl(provider)

    @Provides
    @Singleton
    public fun providePublicAddressKeyRepository(
        db: PublicAddressDatabase,
        provider: ApiProvider
    ): PublicAddressRepository = PublicAddressRepositoryImpl(db, provider)

}
