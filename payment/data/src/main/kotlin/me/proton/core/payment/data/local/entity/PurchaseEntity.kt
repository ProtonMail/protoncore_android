package me.proton.core.payment.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import me.proton.core.account.data.entity.SessionEntity
import me.proton.core.network.domain.session.SessionId
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.Purchase
import me.proton.core.payment.domain.entity.PurchaseState
import me.proton.core.payment.domain.usecase.PaymentProvider

@Entity(
    primaryKeys = ["planName"],
    indices = [
        Index("planName"),
        Index("sessionId"),
        Index("purchaseState"),
        Index("paymentProvider")
    ],
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
public data class PurchaseEntity(
    val sessionId: SessionId,
    val planName: String,
    val planCycle: Int,
    val purchaseState: PurchaseState,
    val purchaseFailure: String?,
    val paymentProvider: PaymentProvider,
    val paymentOrderId: String?,
    val paymentToken: String?,
    val paymentCurrency: Currency,
    val paymentAmount: Long
) {
    public fun toPurchase(): Purchase = Purchase(
        sessionId = sessionId,
        planName = planName,
        planCycle = planCycle,
        purchaseState = purchaseState,
        purchaseFailure = purchaseFailure,
        paymentProvider = paymentProvider,
        paymentOrderId = paymentOrderId,
        paymentToken = paymentToken?.let { ProtonPaymentToken(it) },
        paymentCurrency = paymentCurrency,
        paymentAmount = paymentAmount
    )
}

internal fun Purchase.toPurchaseEntity() = PurchaseEntity(
    sessionId = sessionId,
    planName = planName,
    planCycle = planCycle,
    purchaseState = purchaseState,
    purchaseFailure = purchaseFailure,
    paymentProvider = paymentProvider,
    paymentOrderId = paymentOrderId,
    paymentToken = paymentToken?.value,
    paymentCurrency = paymentCurrency,
    paymentAmount = paymentAmount
)
