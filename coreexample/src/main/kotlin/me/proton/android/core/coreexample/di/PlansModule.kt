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

package me.proton.android.core.coreexample.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.domain.ApiVersion
import me.proton.core.domain.ApiVersionName
import me.proton.core.network.data.ApiProvider
import me.proton.core.plan.data.repository.PlansRepositoryImpl
import me.proton.core.plan.domain.SupportedPaidPlans
import me.proton.core.plan.domain.repository.PlansRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlansModule {

    @Provides
    @SupportedPaidPlans
    fun provideClientSupportedPaidPlanIds(): List<String> =
        listOf("mail2021", "drive2021")

    @Provides
    @Singleton
    fun providePlansRepository(@ApiVersion apiVersionName: ApiVersionName, apiProvider: ApiProvider): PlansRepository =
        PlansRepositoryImpl(provider = apiProvider, apiVersion = apiVersionName)
}
