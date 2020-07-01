package ch.protonmail.libs.auth.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.android.core.data.api.Field.ADDRESS_ID
import me.proton.android.core.data.api.Field.PRIVATE_KEY
import me.proton.android.core.data.api.Field.SIGNED_KEY_LIST

@Serializable
internal data class AddressKey(

    @SerialName(ADDRESS_ID)
    private val addressId: String,

    @SerialName(PRIVATE_KEY)
    private val privateKey: String,

    @SerialName(SIGNED_KEY_LIST)
    private val signedKeyList: SignedKeyList
)
