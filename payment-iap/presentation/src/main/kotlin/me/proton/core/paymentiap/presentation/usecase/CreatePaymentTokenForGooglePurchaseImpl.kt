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

package me.proton.core.paymentiap.presentation.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.features.IsOmnichannelEnabled
import me.proton.core.payment.domain.usecase.CreateOmnichannelPaymentToken
import me.proton.core.payment.domain.usecase.CreatePaymentToken
import me.proton.core.plan.domain.usecase.CreatePaymentTokenForGooglePurchase
import javax.inject.Inject

public class CreatePaymentTokenForGooglePurchaseImpl @Inject constructor(
    private val isOmnichannelEnabled: IsOmnichannelEnabled,
    private val createOmnichannelPaymentToken: CreateOmnichannelPaymentToken,
    private val createPaymentToken: CreatePaymentToken
) : CreatePaymentTokenForGooglePurchase {

    override suspend fun invoke(
        googleProductId: ProductId,
        purchase: GooglePurchase,
        userId: UserId?
    ): CreatePaymentTokenForGooglePurchase.Result {
        require(purchase.productIds.contains(googleProductId))

        val tokenResult = if (isOmnichannelEnabled(userId)) {
            createOmnichannelPaymentToken(
                sessionUserId = userId,
                packageName = purchase.packageName,
                productId = googleProductId.id,
                orderId = requireNotNull(purchase.orderId),
                googlePurchaseToken = purchase.purchaseToken
            )
        } else {
            createPaymentToken(
                userId = userId,
                paymentType = PaymentType.GoogleIAP(
                    productId = googleProductId.id,
                    purchaseToken = purchase.purchaseToken,
                    orderId = requireNotNull(purchase.orderId),
                    packageName = purchase.packageName,
                    customerId = requireNotNull(purchase.customerId)
                )
            )
        }

        return CreatePaymentTokenForGooglePurchase.Result(token = tokenResult.token)
    }
}
