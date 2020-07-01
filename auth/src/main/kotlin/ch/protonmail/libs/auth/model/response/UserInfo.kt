package ch.protonmail.libs.auth.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.android.core.data.api.Field.USER

@Serializable
internal data class UserInfo(

    @SerialName(USER)
    val user: User
)
