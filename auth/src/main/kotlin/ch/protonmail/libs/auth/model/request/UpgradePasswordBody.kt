package ch.protonmail.libs.auth.model.request

import me.proton.android.core.data.api.Field.AUTH
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UpgradePasswordBody(

    @SerialName(AUTH)
    private val auth: Auth

) {

    constructor(verifier: PasswordVerifier) : this(
        Auth(
            verifier.authVersion,
            verifier.modulusId,
            verifier.salt,
            verifier.srpVerifier
        )
    )
}
