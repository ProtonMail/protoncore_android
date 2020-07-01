package ch.protonmail.libs.auth.model.response

import me.proton.android.core.data.api.Field.ID
import me.proton.android.core.data.api.Field.KEY_SALTS
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class KeySalts(

    @SerialName(KEY_SALTS)
    var keySalts: List<KeySalt>
)

@Serializable
internal data class KeySalt(

    @SerialName(ID)
    var id: String,

    @SerialName(KEY_SALTS)
    var keySalt: String
)
