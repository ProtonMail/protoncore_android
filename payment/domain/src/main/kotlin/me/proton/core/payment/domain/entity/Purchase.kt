package me.proton.core.payment.domain.entity

import me.proton.core.network.domain.session.SessionId
import me.proton.core.payment.domain.usecase.PaymentProvider

public data class Purchase(
    val sessionId: SessionId,
    val planName: String,
    val planCycle: Int,
    val purchaseState: PurchaseState,
    val purchaseFailure: String?,
    val paymentProvider: PaymentProvider,
    val paymentOrderId: String?,
    val paymentToken: ProtonPaymentToken?,
    val paymentCurrency: Currency,
    val paymentAmount: Long
)

public enum class PurchaseState {
    /** Mandatory fields: planId, planName, planCycle, paymentProvider, paymentCurrency, paymentAmount. */
    Pending,

    /** At this time, we have a Proton Token. */
    Purchased,

    /** Proton is aware of the Purchase. */
    Subscribed,

    /** Provider is aware Proton is aware. */
    Acknowledged,

    /** Any permanent error, non-recoverable. */
    Failed,

    /** Any cancellation of the Purchase. */
    Cancelled,

    /** Just before actual deletion. */
    Deleted,
}
