package ch.protonmail.libs.auth.model.response

import me.proton.android.core.data.api.Field
import me.proton.android.core.data.api.Field.PRIMARY
import me.proton.android.core.data.api.Field.PRIVATE_KEY
import ch.protonmail.libs.core.utils.toBoolean
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Key(

    @SerialName(Field.ID)
    val id: String,

    @SerialName(PRIVATE_KEY)
    val privateKey: String,

    @SerialName(PRIMARY)
    private val _primary: Int
) {

    val primary = _primary.toBoolean()
}
