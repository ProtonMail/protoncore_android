package me.proton.core.payment.data.api.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Part of the [CreateOmnichannelPaymentToken] request body model, contains specific details about
 * a payment that are needed to setup, or update a subscription in the backend.
 *
 * @param packageName the client package name from where the purchase originated.
 * @param productId the identifier of the product that was purchased.
 * @param orderId the unique identifier of the the purchase transaction.
 */
@Serializable
public data class OmnichannelPaymentDetails(
    @SerialName("PackageName")
    val packageName: String,
    @SerialName("ProductID")
    val productId: String,
    @SerialName("OrderID")
    val orderId: String
)