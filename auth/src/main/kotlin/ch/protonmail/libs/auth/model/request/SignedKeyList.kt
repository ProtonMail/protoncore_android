package ch.protonmail.libs.auth.model.request

import me.proton.android.core.data.api.Field.DATA
import me.proton.android.core.data.api.Field.SIGNATURE
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SignedKeyList(

    @SerialName(DATA)
    private val data: String,

    @SerialName(SIGNATURE)
    private val signature: String
)
