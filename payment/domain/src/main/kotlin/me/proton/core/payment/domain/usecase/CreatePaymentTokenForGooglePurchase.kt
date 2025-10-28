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
package me.proton.core.payment.domain.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.PaymentType.GoogleIAP
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.util.kotlin.annotation.ExcludeFromCoverage

/**
 * Creates a Proton Payment Token after a successful In App Purchase. Essentially, the resulting
 * Google Purchase Token will be exchanged in the process.
 */
public interface CreatePaymentTokenForGooglePurchase {

    /**
     * Directly calls to create a Proton Payment Token using the [GoogleIAP] request
     * body, coupling this token creation to a Google In App Purchase.
     *
     * @param googleProductId the singular Google id for the product being purchased.
     * @param purchase the complete [GooglePurchase] object.
     * @param userId the id of the user that this purchase is to be consumed for.
     *
     * @return the resulting Proton Payment Token.
     */
    public suspend operator fun invoke(
        googleProductId: ProductId,
        purchase: GooglePurchase,
        userId: UserId?
    ): Result

    @ExcludeFromCoverage
    public data class Result(val token: ProtonPaymentToken)
}