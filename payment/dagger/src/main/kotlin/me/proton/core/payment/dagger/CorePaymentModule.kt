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

package me.proton.core.payment.dagger

import dagger.Binds
import dagger.BindsOptionalOf
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.payment.data.PaymentManagerImpl
import me.proton.core.payment.data.ProtonIAPBillingLibraryImpl
import me.proton.core.payment.data.repository.GooglePurchaseRepositoryImpl
import me.proton.core.payment.data.repository.PaymentsRepositoryImpl
import me.proton.core.payment.domain.PaymentManager
import me.proton.core.payment.domain.repository.GooglePurchaseRepository
import me.proton.core.payment.domain.repository.PaymentsRepository
import me.proton.core.payment.domain.usecase.AcknowledgeGooglePlayPurchase
import me.proton.core.payment.domain.usecase.ProtonIAPBillingLibrary
import me.proton.core.payment.presentation.ActivePaymentProvider
import me.proton.core.payment.presentation.ActivePaymentProviderImpl
import me.proton.core.payment.presentation.entity.SecureEndpoint
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
public interface CorePaymentModule {

    @Binds
    @Singleton
    public fun providePaymentManager(impl: PaymentManagerImpl): PaymentManager

    @Binds
    @Singleton
    public fun providePaymentsRepository(impl: PaymentsRepositoryImpl): PaymentsRepository

    @Binds
    @Singleton
    public fun provideGooglePlayBillingLibrary(impl: ProtonIAPBillingLibraryImpl): ProtonIAPBillingLibrary

    @Binds
    public fun provideActivePaymentProvider(impl: ActivePaymentProviderImpl): ActivePaymentProvider

    @Binds
    public fun bindGooglePurchaseRepository(impl: GooglePurchaseRepositoryImpl): GooglePurchaseRepository

    /** Optional binding, provided by payment-iap-dagger. */
    @BindsOptionalOf
    public fun optionalAcknowledgeGooglePlayPurchase(): AcknowledgeGooglePlayPurchase?

    public companion object {
        @Provides
        @Singleton
        public fun provideSecureEndpoint(): SecureEndpoint = SecureEndpoint("secure.protonmail.com")
    }
}
