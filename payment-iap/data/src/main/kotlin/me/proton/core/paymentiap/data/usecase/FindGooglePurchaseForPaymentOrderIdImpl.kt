/*
 * Copyright (c) 2025 Proton AG
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
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.extension.findGooglePurchase
import me.proton.core.payment.domain.repository.GoogleBillingRepository
import me.proton.core.payment.domain.usecase.FindGooglePurchaseForPaymentOrderId
import javax.inject.Inject
import javax.inject.Provider

public class FindGooglePurchaseForPaymentOrderIdImpl @Inject constructor(
    private val googleBillingRepository: Provider<GoogleBillingRepository<Activity>>
) : FindGooglePurchaseForPaymentOrderId {

    override suspend fun invoke(orderId: String?): GooglePurchase? {
        return googleBillingRepository.get().use { repository ->
            repository.findGooglePurchase(orderId)
        }
    }
}