package ch.protonmail.libs.auth.model.response

import me.proton.android.core.data.api.Field.ADDRESS
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AddressSetupResponse(

    @SerialName(ADDRESS)
    val address: Address
)
