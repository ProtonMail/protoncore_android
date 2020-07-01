package ch.protonmail.libs.auth.model.request

import me.proton.android.core.data.api.Field.CLIENT_SECRET
import me.proton.android.core.data.api.Field.USERNAME
import ch.protonmail.libs.auth.ProtonAuthConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class LoginInfoBody(

    @SerialName(USERNAME)
    private val username: String,

    @SerialName(CLIENT_SECRET)
    private val clientSecret: String = ProtonAuthConfig.clientSecret
)
