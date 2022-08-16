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

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.proton.core.util.kotlin.CoreLogger
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
public object PaymentIapModule {
    @Provides
    @Singleton
    public fun providePurchasesUpdatedListener(): PurchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult: BillingResult, purchaseList: List<Purchase>? ->
            CoreLogger.d("IAP", "$billingResult -> $purchaseList")
        }

    @Provides
    @Singleton
    public fun provideBillingClient(
        @ApplicationContext context: Context,
        purchasesUpdatedListener: PurchasesUpdatedListener,
    ): BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()
}
