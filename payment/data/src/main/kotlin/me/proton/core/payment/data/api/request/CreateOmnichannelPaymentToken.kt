package me.proton.core.payment.data.api.request

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body data model for requesting tokenization of a successful In App Purchase.
 *
 * @param amount the purchase amount.
 * @param currency the currency the purchase was made in.
 * @param payment necessary details needed to tokenize the purchase.
 *
 * Note: [amount] and [currency] are marked as deprecated. This is because they are not actually
 * used by the server. The backend aims to remove these currently required properties, and once
 * they do, they will similarly be removed from this model.
 */
@Serializable
internal data class CreateOmnichannelPaymentToken(
    @Deprecated("No longer used by the backend, but still necessary to transmit")
    @EncodeDefault
    @SerialName("Amount")
    val amount: Long = 0L,
    @Deprecated("No longer used by the backend, but still necessary to transmit")
    @EncodeDefault
    @SerialName("Currency")
    val currency: String = "CHF",
    @SerialName("Payment")
    val payment: OmnichannelPayment
)