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

package me.proton.android.core.coreexample.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import me.proton.core.accountmanager.data.db.AccountManagerDatabase
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.data.repository.KeySaltRepositoryImpl
import me.proton.core.key.data.repository.PublicAddressKeyRepositoryImpl
import me.proton.core.key.domain.repository.KeySaltRepository
import me.proton.core.key.domain.repository.PublicAddressKeyRepository
import me.proton.core.network.data.ApiProvider
import me.proton.core.user.data.UserManagerImpl
import me.proton.core.user.data.repository.UserAddressRepositoryImpl
import me.proton.core.user.data.repository.UserRepositoryImpl
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.repository.PassphraseRepository
import me.proton.core.user.domain.repository.UserAddressRepository
import me.proton.core.user.domain.repository.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AccountManagerModule {

    @Provides
    @Singleton
    fun provideUserRepositoryImpl(
        db: AccountManagerDatabase,
        provider: ApiProvider
    ): UserRepositoryImpl = UserRepositoryImpl(db, provider)

    @Provides
    @Singleton
    fun provideUserAddressRepository(
        userRepository: UserRepository,
        passphraseRepository: PassphraseRepository,
        db: AccountManagerDatabase,
        provider: ApiProvider,
        cryptoContext: CryptoContext
    ): UserAddressRepository =
        UserAddressRepositoryImpl(userRepository, passphraseRepository, db, provider, cryptoContext)

    @Provides
    @Singleton
    fun provideKeySaltRepository(
        db: AccountManagerDatabase,
        provider: ApiProvider
    ): KeySaltRepository = KeySaltRepositoryImpl(db, provider)

    @Provides
    @Singleton
    fun providePublicAddressKeyRepository(
        db: AccountManagerDatabase,
        provider: ApiProvider
    ): PublicAddressKeyRepository = PublicAddressKeyRepositoryImpl(db, provider)

    @Provides
    @Singleton
    fun provideUserManager(
        userRepository: UserRepository,
        userAddressRepository: UserAddressRepository,
        passphraseRepository: PassphraseRepository,
        keySaltRepository: KeySaltRepository,
        cryptoContext: CryptoContext
    ): UserManager =
        UserManagerImpl(userRepository, userAddressRepository, passphraseRepository, keySaltRepository, cryptoContext)
}

@Module
@InstallIn(ApplicationComponent::class)
abstract class UserManagerBindsModule {

    @Binds
    abstract fun provideUserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository

    @Binds
    abstract fun providePassphraseRepository(userRepositoryImpl: UserRepositoryImpl): PassphraseRepository
}
