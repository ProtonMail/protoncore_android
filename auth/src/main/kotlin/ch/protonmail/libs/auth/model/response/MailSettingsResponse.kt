package ch.protonmail.libs.auth.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.android.core.data.api.Field.MAIL_SETTINGS

@Serializable
internal data class MailSettingsResponse(

    @SerialName(MAIL_SETTINGS)
    val mailSettings: MailSettings
)
