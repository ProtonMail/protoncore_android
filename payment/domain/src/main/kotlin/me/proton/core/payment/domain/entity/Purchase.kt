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

    /**
     * An In App Purchase has been initiated and we are awaiting a result.
     */
    Pending,

    /**
     * The In App Purchase has been successful and we have a Google Purchase Token.
     */
    Purchased,

    /**
     * A Google Purchase Token has been exchanged for a Proton Payment Token.
     */
    Tokenized,

    /**
     * The Proton Payment Token is now approved, and chargeable.
     */
    Approved,

    /**
     * The purchase has been systemically attached, and a subscription created or updated.
     */
    Subscribed,

    /**
     * Google has been informed that the users' entitlements have been granted.
     */
    Acknowledged,

    /**
     * Any permanent non-recoverable error.
     */
    Failed,

    /**
     * Any cancellation of the purchase.
     */
    Cancelled,

    /**
     * Just before actual deletion.
     */
    Deleted
}
