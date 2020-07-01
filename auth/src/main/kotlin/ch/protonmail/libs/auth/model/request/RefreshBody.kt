package ch.protonmail.libs.auth.model.request

import me.proton.android.core.data.api.Field.GRANT_TYPE
import me.proton.android.core.data.api.Field.REDIRECT_URI
import me.proton.android.core.data.api.Field.REFRESH_TOKEN
import me.proton.android.core.data.api.Field.RESPONSE_TYPE
import ch.protonmail.libs.core.HTTP_PROTONMAIL_CH
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RefreshBody(

    @SerialName(REFRESH_TOKEN)
    private val refreshToken: String,

    @SerialName(RESPONSE_TYPE)
    private val responseType: String = "token",

    @SerialName(GRANT_TYPE)
    private val grantType: String = "refresh_token",

    @SerialName(REDIRECT_URI)
    private val redirectURI: String = HTTP_PROTONMAIL_CH
)
