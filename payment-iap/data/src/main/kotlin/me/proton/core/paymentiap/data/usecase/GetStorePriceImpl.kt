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

package me.proton.core.paymentiap.data.usecase

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.entity.ProductPrice
import me.proton.core.payment.domain.repository.GoogleBillingRepository
import me.proton.core.payment.domain.usecase.GetStorePrice
import me.proton.core.paymentiap.domain.entity.GoogleProductPrice
import me.proton.core.paymentiap.domain.entity.unwrap
import me.proton.core.paymentiap.domain.pricingPhases
import javax.inject.Inject
import javax.inject.Provider

public class GetStorePriceImpl @Inject constructor(
    private val billingRepositoryProvider: Provider<GoogleBillingRepository<Activity>>
) : GetStorePrice {
    override suspend fun invoke(planName: ProductId): ProductPrice? =
        billingRepositoryProvider.get().use { repository ->
            val details = repository.getProductsDetails(listOf(planName))?.firstOrNull()?.unwrap()
            if (details == null) {
                null
            } else {
                val phases = details.pricingPhases()
                val current = phases.getOrNull(0)
                val default = phases.getOrNull(1)?.takeIf {
                    it.priceAmountMicros != current?.priceAmountMicros &&
                            current?.recurrenceMode == ProductDetails.RecurrenceMode.FINITE_RECURRING &&
                            it.recurrenceMode == ProductDetails.RecurrenceMode.INFINITE_RECURRING
                }
                current?.let { price ->
                    GoogleProductPrice(
                        priceAmountMicros = price.priceAmountMicros,
                        currency = price.priceCurrencyCode,
                        formattedPriceAndCurrency = price.formattedPrice,
                        defaultPriceAmountMicros = default?.priceAmountMicros,
                    )
                }
            }
        }
}


