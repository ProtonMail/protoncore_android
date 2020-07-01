package ch.protonmail.libs.auth.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.android.core.data.api.Field.MODULUS
import me.proton.android.core.data.api.Field.MODULUS_ID

@Serializable
internal data class ModulusResponse(

    @SerialName(MODULUS_ID)
    val modulusId: String,

    @SerialName(MODULUS)
    val modulus: String
)
