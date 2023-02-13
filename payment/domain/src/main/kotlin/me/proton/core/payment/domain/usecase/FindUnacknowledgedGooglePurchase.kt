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

package me.proton.core.payment.domain.usecase

import me.proton.core.observability.domain.metrics.ObservabilityData
import me.proton.core.payment.domain.entity.GooglePurchase

public interface FindUnacknowledgedGooglePurchase {
    /** Returns any unredeemed purchases.
     * The most recent purchases are at the beginning of the list.
     * May return an empty list if Billing service is not available (either temporarily or permanently).
     */
    public suspend operator fun invoke(
        querySubscriptionsMetricData: ((Result<List<GooglePurchase>>) -> ObservabilityData?)? = null
    ): List<GooglePurchase>

    /** Return the most recent purchase for the given [customerId]. */
    public suspend fun byCustomer(
        customerId: String,
        querySubscriptionsMetricData: ((Result<List<GooglePurchase>>) -> ObservabilityData?)? = null
    ): GooglePurchase?

    /** Return the most recent purchase for the given [productId]. */
    public suspend fun byProduct(
        productId: String,
        querySubscriptionsMetricData: ((Result<List<GooglePurchase>>) -> ObservabilityData?)? = null
    ): GooglePurchase?
}
