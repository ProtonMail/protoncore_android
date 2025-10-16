package me.proton.core.payment.domain.usecase

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.GooglePurchaseToken
import me.proton.core.payment.domain.entity.PaymentTokenResult
import me.proton.core.payment.domain.repository.GooglePurchaseRepository
import me.proton.core.payment.domain.repository.PaymentsRepository
import javax.inject.Inject

public class CreateOmnichannelPaymentToken @Inject constructor(
    private val paymentsRepository: PaymentsRepository,
    private val googlePurchaseRepository: GooglePurchaseRepository
) {

    public suspend operator fun invoke(
        sessionUserId: UserId?,
        packageName: String,
        productId: String,
        orderId: String,
        googlePurchaseToken: GooglePurchaseToken
    ): PaymentTokenResult.CreatePaymentTokenResult {
        return paymentsRepository.createOmnichannelPaymentToken(
            sessionUserId = sessionUserId,
            packageName = packageName,
            productId = productId,
            orderId = orderId
        ).also { result ->
            googlePurchaseRepository.updateGooglePurchase(
                googlePurchaseToken = googlePurchaseToken,
                paymentToken = result.token
            )
        }
    }
}