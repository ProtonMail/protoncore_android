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

package me.proton.core.auth.dagger

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.proton.core.auth.data.MissingScopeListenerImpl
import me.proton.core.auth.data.repository.AuthRepositoryImpl
import me.proton.core.auth.domain.repository.AuthRepository
import me.proton.core.domain.entity.Product
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.scopes.MissingScopeListener
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public interface CoreAuthModule {
    @Binds
    @Singleton
    public fun provideMissingScopeListener(impl: MissingScopeListenerImpl): MissingScopeListener

    public companion object {
        @Provides
        @Singleton
        public fun provideAuthRepository(
            apiProvider: ApiProvider,
            @ApplicationContext context: Context,
            product: Product
        ): AuthRepository = AuthRepositoryImpl(apiProvider, context, product)
    }
}
