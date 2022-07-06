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

package me.proton.core.accountmanager.dagger

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.account.domain.repository.AccountRepository
import me.proton.core.accountmanager.data.AccountManagerImpl
import me.proton.core.accountmanager.data.AccountMigratorImpl
import me.proton.core.accountmanager.data.AccountStateHandler
import me.proton.core.accountmanager.data.SessionListenerImpl
import me.proton.core.accountmanager.data.SessionManagerImpl
import me.proton.core.accountmanager.data.SessionProviderImpl
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.SessionManager
import me.proton.core.accountmanager.domain.migrator.AccountMigrator
import me.proton.core.auth.domain.AccountWorkflowHandler
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.entity.Product
import me.proton.core.network.domain.session.SessionListener
import me.proton.core.network.domain.session.SessionProvider
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.repository.UserRepository
import me.proton.core.util.kotlin.CoroutineScopeProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreAccountManagerModule {

    @Provides
    @Singleton
    fun provideAccountManagerImpl(
        product: Product,
        accountRepository: AccountRepository,
        authRepository: AuthRepository,
        userManager: UserManager
    ): AccountManagerImpl =
        AccountManagerImpl(product, accountRepository, authRepository, userManager)

    @Provides
    @Singleton
    fun provideAccountMigrator(
        accountManager: AccountManager,
        accountRepository: AccountRepository,
        userRepository: UserRepository
    ): AccountMigrator =
        AccountMigratorImpl(accountManager, accountRepository, userRepository)

    @Provides
    @Singleton
    @Suppress("LongParameterList")
    fun provideAccountStateHandler(
        scopeProvider: CoroutineScopeProvider,
        userManager: UserManager,
        accountManager: AccountManager,
        accountRepository: AccountRepository,
        accountMigrator: AccountMigrator,
        product: Product,
    ): AccountStateHandler =
        AccountStateHandler(scopeProvider, userManager, accountManager, accountRepository, accountMigrator, product)

    @Provides
    @Singleton
    fun provideSessionProvider(
        accountRepository: AccountRepository
    ): SessionProvider =
        SessionProviderImpl(accountRepository)

    @Provides
    @Singleton
    fun provideSessionListener(
        accountRepository: AccountRepository
    ): SessionListener =
        SessionListenerImpl(accountRepository)

    @Provides
    @Singleton
    fun provideSessionManager(
        sessionListener: SessionListener,
        sessionProvider: SessionProvider,
        authRepository: AuthRepository
    ): SessionManager =
        SessionManagerImpl(sessionProvider, sessionListener, authRepository)
}

@Module
@InstallIn(SingletonComponent::class)
interface AccountManagerBindModule {

    @Binds
    fun bindAccountManager(accountManagerImpl: AccountManagerImpl): AccountManager

    @Binds
    fun bindAccountWorkflowHandler(accountManagerImpl: AccountManagerImpl): AccountWorkflowHandler
}
