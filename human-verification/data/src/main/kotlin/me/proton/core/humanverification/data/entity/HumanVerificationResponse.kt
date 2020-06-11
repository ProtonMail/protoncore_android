package me.proton.core.humanverification.data.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.humanverification.data.api.Fields.TOKEN
import me.proton.core.humanverification.data.api.Fields.VERIFY_METHODS

/**
 * Created by dinokadrikj on 6/12/20.
 * Response class for Human Verification serialization
 */
@Serializable data class HumanVerificationResponse(
    @SerialName(VERIFY_METHODS)
    val verifyMethods: List<String>, // Only provided if Direct = 1
    @SerialName(TOKEN)
    val token: String
)
