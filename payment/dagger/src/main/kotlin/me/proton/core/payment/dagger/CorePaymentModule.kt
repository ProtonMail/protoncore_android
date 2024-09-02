/*
 * Copyright (c) 2023 Proton AG
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

import android.app.Activity
import dagger.Binds
import dagger.BindsOptionalOf
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.proton.core.payment.data.IsPaymentsV5EnabledImpl
import me.proton.core.payment.data.PaymentManagerImpl
import me.proton.core.payment.data.ProtonIAPBillingLibraryImpl
import me.proton.core.payment.data.PurchaseManagerImpl
import me.proton.core.payment.data.repository.GooglePurchaseRepositoryImpl
import me.proton.core.payment.data.repository.PaymentsRepositoryImpl
import me.proton.core.payment.data.repository.PurchaseRepositoryImpl
import me.proton.core.payment.domain.IsPaymentsV5Enabled
import me.proton.core.payment.domain.PaymentManager
import me.proton.core.payment.domain.PurchaseManager
import me.proton.core.payment.domain.repository.GooglePurchaseRepository
import me.proton.core.payment.domain.repository.PaymentsRepository
import me.proton.core.payment.domain.repository.PurchaseRepository
import me.proton.core.payment.domain.usecase.AcknowledgeGooglePlayPurchase
import me.proton.core.payment.domain.usecase.ConvertToObservabilityGiapStatus
import me.proton.core.payment.domain.usecase.FindUnacknowledgedGooglePurchase
import me.proton.core.payment.domain.usecase.GetStorePrice
import me.proton.core.payment.domain.usecase.LaunchGiapBillingFlow
import me.proton.core.payment.domain.usecase.PrepareGiapPurchase
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
    public fun providePurchaseManager(impl: PurchaseManagerImpl): PurchaseManager

    @Binds
    @Singleton
    public fun providePaymentsRepository(impl: PaymentsRepositoryImpl): PaymentsRepository

    @Binds
    @Singleton
    public fun provideGooglePlayBillingLibrary(impl: ProtonIAPBillingLibraryImpl): ProtonIAPBillingLibrary

    @Binds
    @Singleton
    public fun provideActivePaymentProvider(impl: ActivePaymentProviderImpl): ActivePaymentProvider

    @Binds
    @Singleton
    public fun providePurchaseRepository(impl: PurchaseRepositoryImpl): PurchaseRepository

    @Binds
    @Singleton
    public fun bindGooglePurchaseRepository(impl: GooglePurchaseRepositoryImpl): GooglePurchaseRepository

    /** Optional binding, provided by payment-iap-dagger. */
    @BindsOptionalOf
    public fun optionalAcknowledgeGooglePlayPurchase(): AcknowledgeGooglePlayPurchase

    /** Optional binding, provided by payment-iap-dagger. */
    @BindsOptionalOf
    public fun optionalFindUnredeemedGooglePurchase(): FindUnacknowledgedGooglePurchase

    /** Optional binding, provided by payment-iap-dagger. */
    @BindsOptionalOf
    public fun optionalGetPlanAndCurrency(): GetStorePrice

    @BindsOptionalOf
    public fun optionalLaunchGiapBillingFlow(): LaunchGiapBillingFlow<Activity>

    @BindsOptionalOf
    public fun optionalPrepareGiapPurchase(): PrepareGiapPurchase

    @BindsOptionalOf
    public fun optionalConvertToObservabilityGiapStatus(): ConvertToObservabilityGiapStatus

    public companion object {
        @Provides
        @Singleton
        public fun provideSecureEndpoint(): SecureEndpoint = SecureEndpoint("secure.protonmail.com")
    }
}

@Module
@InstallIn(SingletonComponent::class)
public interface CorePaymentFeaturesModule {
    @Binds
    public fun bindIsPaymentsV5Enabled(impl: IsPaymentsV5EnabledImpl): IsPaymentsV5Enabled
}
