package ch.protonmail.libs.auth.model.response

import me.proton.android.core.data.api.Field.MODULUS
import me.proton.android.core.data.api.Field.SALT
import me.proton.android.core.data.api.Field.SERVER_EPHEMERAL
import me.proton.android.core.data.api.Field.SRP_SESSION
import me.proton.android.core.data.api.Field.TWO_FACTOR
import me.proton.android.core.data.api.Field.VERSION
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class LoginInfoResponse(

    @SerialName(MODULUS)
    val modulus: String,

    @SerialName(SERVER_EPHEMERAL)
    val serverEphemeral: String,

    @SerialName(VERSION)
    val authVersion: Int,

    @SerialName(SALT)
    val salt: String,

    @SerialName(SRP_SESSION)
    val srpSession: String,

    @SerialName(TWO_FACTOR)
    val twoFactor: Int
)
