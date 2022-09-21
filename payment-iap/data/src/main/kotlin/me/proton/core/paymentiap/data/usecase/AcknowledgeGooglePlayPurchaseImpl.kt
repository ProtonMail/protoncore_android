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

import me.proton.core.payment.domain.repository.GooglePurchaseRepository
import me.proton.core.payment.domain.usecase.AcknowledgeGooglePlayPurchase
import me.proton.core.paymentiap.domain.repository.GoogleBillingRepository
import javax.inject.Inject
import javax.inject.Provider

public class AcknowledgeGooglePlayPurchaseImpl @Inject constructor(
    private val googleBillingRepositoryProvider: Provider<GoogleBillingRepository>,
    private val googlePurchaseRepository: GooglePurchaseRepository
) : AcknowledgeGooglePlayPurchase {
    override suspend fun invoke(paymentToken: String) {
        val googlePurchaseToken = requireNotNull(googlePurchaseRepository.findGooglePurchaseToken(paymentToken)) {
            "Could not find corresponding Google purchase token."
        }

        // Create a new instance of `GoogleBillingRepository` and clean up after it:
        googleBillingRepositoryProvider.get().use {
            it.acknowledgePurchase(googlePurchaseToken)
        }

        googlePurchaseRepository.deleteByGooglePurchaseToken(googlePurchaseToken)
    }
}
