package me.proton.core.auth.data.entity.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.auth.data.entity.FullAddressKeyEntity

/**
 * @author Dino Kadrikj.
 */
@Serializable
data class AddressKeySetupResponse(
    @SerialName("Key")
    val key: FullAddressKeyEntity
)
