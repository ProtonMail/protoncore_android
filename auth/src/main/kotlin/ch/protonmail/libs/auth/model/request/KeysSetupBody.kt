package ch.protonmail.libs.auth.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.android.core.data.api.Field.ADDRESS_KEY
import me.proton.android.core.data.api.Field.AUTH
import me.proton.android.core.data.api.Field.KEY_SALT
import me.proton.android.core.data.api.Field.PRIMARY_KEY

@Serializable
internal data class KeysSetupBody(

    @SerialName(PRIMARY_KEY)
    private val primaryKey: String,

    @SerialName(KEY_SALT)
    private val keySalt: String,

    @SerialName(ADDRESS_KEY)
    private val addressKeys: List<AddressKey>,

    @SerialName(AUTH)
    private val auth: Auth
) {

    constructor(
        primaryKey: String,
        keySalt: String,
        addressKeys: List<AddressKey>,
        verifier: PasswordVerifier
    ) : this(
        primaryKey =    primaryKey,
        keySalt =       keySalt,
        addressKeys =   addressKeys,
        authVersion =   verifier.authVersion,
        modulusId =     verifier.modulusId,
        salt =          verifier.salt,
        srpVerifier =   verifier.srpVerifier
    )

    constructor(
        primaryKey: String,
        keySalt: String,
        addressKeys: List<AddressKey>,
        authVersion: Int,
        modulusId: String,
        salt: String,
        srpVerifier: String
    ) : this(
        primaryKey =    primaryKey,
        keySalt =       keySalt,
        addressKeys =   addressKeys,
        auth = Auth(
            authVersion,
            modulusId,
            salt,
            srpVerifier
        )
    )
}
