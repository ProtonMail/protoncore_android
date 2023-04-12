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

package me.proton.core.user.dagger

import dagger.Binds
import dagger.BindsOptionalOf
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.user.data.UserAddressManagerImpl
import me.proton.core.user.data.UserManagerImpl
import me.proton.core.user.data.repository.DomainRepositoryImpl
import me.proton.core.user.data.repository.UserAddressRepositoryImpl
import me.proton.core.user.data.repository.UserRepositoryImpl
import me.proton.core.user.domain.UserAddressManager
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.repository.DomainRepository
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.user.domain.SignedKeyListChangeListener
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public interface CoreUserRepositoriesModule {
    @Binds
    @Singleton
    public fun provideDomainRepository(impl: DomainRepositoryImpl): DomainRepository

    @Binds
    @Singleton
    public fun provideUserAddressRepository(impl: UserAddressRepositoryImpl): UserAddressRepository

    @Binds
    @Singleton
    public fun provideUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    public fun providePassphraseRepository(impl: UserRepository): PassphraseRepository
}

@Module
@InstallIn(SingletonComponent::class)
public interface CoreUserManagersModule {

    @Binds
    @Singleton
    public fun provideUserManager(impl: UserManagerImpl): UserManager

    @Binds
    @Singleton
    public fun provideUserAddressManager(impl: UserAddressManagerImpl): UserAddressManager

    @BindsOptionalOf
    public fun optionalSignedKeyListChangeListener(): SignedKeyListChangeListener
}
