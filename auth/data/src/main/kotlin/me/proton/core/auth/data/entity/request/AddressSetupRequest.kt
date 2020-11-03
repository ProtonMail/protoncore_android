package me.proton.core.auth.data.entity.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author Dino Kadrikj.
 */
@Serializable
data class AddressSetupRequest(
    @SerialName("Domain")
    val domain: String,
    @SerialName("DisplayName")
    val displayName: String
)
