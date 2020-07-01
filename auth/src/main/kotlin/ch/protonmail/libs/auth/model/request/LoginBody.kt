package ch.protonmail.libs.auth.model.request

import me.proton.android.core.data.api.Field.CLIENT_EPHEMERAL
import me.proton.android.core.data.api.Field.CLIENT_PROOF
import me.proton.android.core.data.api.Field.CLIENT_SECRET
import me.proton.android.core.data.api.Field.SRP_SESSION
import me.proton.android.core.data.api.Field.TWO_FACTOR_CODE
import me.proton.android.core.data.api.Field.USERNAME
import ch.protonmail.libs.auth.ProtonAuthConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal class LoginBody(

    @SerialName(USERNAME)
    private val username: String,

    @SerialName(SRP_SESSION)
    override val srpSession: String,

    @SerialName(CLIENT_EPHEMERAL)
    override val clientEphemeral: String,

    @SerialName(CLIENT_PROOF)
    override val clientProof: String,

    @SerialName(TWO_FACTOR_CODE)
    override val twoFactorCode: String,

    @SerialName(CLIENT_SECRET)
    private val clientSecret: String = ProtonAuthConfig.clientSecret

) : SrpRequestBody
