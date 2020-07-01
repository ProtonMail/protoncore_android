package ch.protonmail.libs.auth.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.android.core.data.api.Field.KEYS

@Serializable
internal data class User(

    @SerialName(KEYS)
    val keys: List<Key>
)
