package ch.protonmail.libs.auth.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.android.core.data.api.Field.DOMAIN

@Serializable
internal data class AddressSetupBody(

    @SerialName(DOMAIN)
    private val domain: String
)
