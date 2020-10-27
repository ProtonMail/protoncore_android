package me.proton.core.auth.data.entity.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.auth.data.entity.AddressEntity

/**
 * @author Dino Kadrikj.
 */
@Serializable
data class AddressSetupResponse(
    @SerialName("Address")
    val address: AddressEntity
)
