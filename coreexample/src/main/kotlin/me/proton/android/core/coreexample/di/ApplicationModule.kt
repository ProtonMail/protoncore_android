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

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.android.core.coreexample.api.CoreExampleRepository
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.auth.domain.ClientSecret
import me.proton.core.domain.ApiVersion
import me.proton.core.domain.ApiVersionName
import me.proton.core.domain.entity.Product
import me.proton.core.network.data.ApiProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    @Singleton
    fun provideProduct(): Product =
        Product.Mail

    @Provides
    @Singleton
    fun provideRequiredAccountType(): AccountType =
        AccountType.Internal

    @Provides
    @ClientSecret
    fun provideClientSecret(): String = ""

    @Provides
    @Singleton
    fun provideCoreExampleRepository(apiProvider: ApiProvider): CoreExampleRepository =
        CoreExampleRepository(apiProvider)

    @Provides
    @ApiVersion
    fun provideApiVersion(): ApiVersionName = ApiVersionName.V4
}
