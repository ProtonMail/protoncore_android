package me.proton.core.payment.data.api.request

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Whilst it is entirely possible to have multiple types of payments, for now the only supported
 * type in Android is Google In App Purchase.
 */
private const val PAYMENT_TYPE_GOOGLE_IAP: String = "google-iap"

/**
 * Part of the [CreateOmnichannelPaymentToken] request body model, nothing more than a wrapper
 * around details of the payment, with a constant type value.
 *
 * @param type for now, always type of Google IAP.
 * @param details purchase details necessary for tokenization.
 */
@Serializable
public data class OmnichannelPayment(
    @EncodeDefault
    @SerialName("Type")
    val type: String = PAYMENT_TYPE_GOOGLE_IAP,
    @SerialName("Details")
    val details: OmnichannelPaymentDetails
)