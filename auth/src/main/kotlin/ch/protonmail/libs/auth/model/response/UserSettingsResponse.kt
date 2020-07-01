package ch.protonmail.libs.auth.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.android.core.data.api.Field.USER_SETTINGS

@Serializable
internal data class UserSettingsResponse(

    @SerialName(USER_SETTINGS)
    val userSettings: UserSettings
)
