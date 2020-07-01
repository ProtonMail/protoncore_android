package ch.protonmail.libs.auth.model.response

import me.proton.android.core.data.api.Field.KEY_SALT
import me.proton.android.core.data.api.Field.PASSWORD_MODE
import me.proton.android.core.data.api.Field.PRIVATE_KEY
import me.proton.android.core.data.api.Field.REFRESH_TOKEN
import me.proton.android.core.data.api.Field.SERVER_PROOF
import me.proton.android.core.data.api.Field.UID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.android.core.data.api.Field.ACCESS_TOKEN

@Serializable
internal class LoginResponse(

    @SerialName(ACCESS_TOKEN)
    override val accessToken: String,

    @SerialName(UID)
    val uid: String,

    @SerialName(REFRESH_TOKEN)
    val refreshToken: String,

    @SerialName(PRIVATE_KEY)
    val privateKey: String,

    /** TODO: Can be null? */
    @SerialName(KEY_SALT)
    val keySalt: String?,

    @SerialName(PASSWORD_MODE)
    val passwordMode: PasswordMode,

    @SerialName(SERVER_PROOF)
    override val serverProof: String

) : HasAccessToken, HasServerProof {

    val isValid = accessToken != null && uid != null && refreshToken != null
}
