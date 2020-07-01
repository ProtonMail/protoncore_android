package ch.protonmail.libs.auth.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.android.core.data.api.Field.MODULUS_ID
import me.proton.android.core.data.api.Field.SALT
import me.proton.android.core.data.api.Field.VERIFIER
import me.proton.android.core.data.api.Field.VERSION

@Serializable
internal data class Auth(

    @SerialName(VERSION)
    private val version: Int,

    @SerialName(MODULUS_ID)
    private val modulusId: String,

    @SerialName(SALT)
    private val salt: String,

    @SerialName(VERIFIER)
    private val srpVerifier: String
)
