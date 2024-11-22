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

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.GoogleBillingFlowParams
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.ProductId

/**
 * Prepares a Google In App Purchase.
 */
public interface PrepareGiapPurchase {
    /**
     * Handling results:
     * - [Result.ProductDetailsNotFound] - Whenever a product with
     *  the given [googleProductId] was not found on Google Play.
     *  Usually you should display error message.
     * - [Result.Unredeemed] - When an unredeemed purchase for product
     *  with the given [googleProductId] was detected.
     *  Usually, you should start the redeem process.
     * - [Result.Success] - When the purchase is possible.
     *  Usually, you should follow up with [LaunchGiapBillingFlow].
     */
    public suspend operator fun invoke(
        googleCustomerId: String,
        googleProductId: ProductId,
        userId: UserId?
    ): Result

    public sealed class Result {
        public object ProductDetailsNotFound : Result()
        public data class Unredeemed(public val googlePurchase: GooglePurchase) : Result()
        public data class Success(public val params: GoogleBillingFlowParams) : Result()
    }
}
