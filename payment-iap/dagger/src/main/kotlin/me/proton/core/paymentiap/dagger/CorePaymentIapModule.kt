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

package me.proton.core.paymentiap.dagger

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.payment.domain.usecase.AcknowledgeGooglePlayPurchase
import me.proton.core.payment.domain.usecase.FindUnacknowledgedGooglePurchase
import me.proton.core.payment.domain.usecase.GetStorePrice
import me.proton.core.paymentiap.data.usecase.AcknowledgeGooglePlayPurchaseImpl
import me.proton.core.paymentiap.data.BillingClientFactoryImpl
import me.proton.core.paymentiap.data.repository.GoogleBillingRepositoryImpl
import me.proton.core.paymentiap.data.usecase.FindUnacknowledgedGooglePurchaseImpl
import me.proton.core.paymentiap.data.usecase.GetStorePriceImpl
import me.proton.core.paymentiap.domain.BillingClientFactory
import me.proton.core.paymentiap.domain.repository.GoogleBillingRepository

@Module
@InstallIn(SingletonComponent::class)
public interface CorePaymentIapBillingModule {
    @Binds
    public fun bindBillingClientProvider(impl: BillingClientFactoryImpl): BillingClientFactory
}

@Module
@InstallIn(SingletonComponent::class)
public interface CorePaymentIapModule {
    @Binds
    public fun bindGoogleBillingRepository(impl: GoogleBillingRepositoryImpl): GoogleBillingRepository

    @Binds
    public fun bindAcknowledgeGooglePlayPurchase(impl: AcknowledgeGooglePlayPurchaseImpl): AcknowledgeGooglePlayPurchase

    @Binds
    public fun bindFindUnredeemedGooglePurchase(impl: FindUnacknowledgedGooglePurchaseImpl): FindUnacknowledgedGooglePurchase

    @Binds
    public fun bindGetGooglePlanPriceAndCurrency(impl: GetStorePriceImpl): GetStorePrice
}
