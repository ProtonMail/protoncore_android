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

package me.proton.core.payment.domain.usecase

import me.proton.core.payment.domain.entity.GoogleBillingFlowParams
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.ProductId

/**
 * Launches a Google In App Purchase dialog.
 */
public interface LaunchGiapBillingFlow<A : Any> {
    public suspend operator fun invoke(
        activity: A,
        googleProductId: ProductId,
        params: GoogleBillingFlowParams
    ): Result

    public sealed class Result {
        public sealed class Error : Result() {
            public object EmptyCustomerId : Error()
            public object PurchaseNotFound: Error()
        }

        public data class PurchaseSuccess(public val purchase: GooglePurchase) : Result()
    }
}
