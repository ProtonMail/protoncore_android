package me.proton.core.auth.data.entity.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Note: Token and Signature are not mandatory and will be left out from the initial release.
 *
 * @author Dino Kadrikj.
 */
@Serializable
data class AddressKeySetupRequest(
    @SerialName("AddressID")
    val addressId: String,
    @SerialName("PrivateKey")
    val privateKey: String,
    @SerialName("Primary")
    val primary: Int,
    @SerialName("Token")
    val token: String? = null,
    @SerialName("Signature")
    val signature: String? = null,
    @SerialName("SignedKeyList")
    val signedKeyList: SignedKeyList
)
