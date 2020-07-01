package ch.protonmail.libs.auth.api

import me.proton.android.core.data.api.service.ProtonPublicServiceSpec
import ch.protonmail.libs.auth.model.response.LoginInfoResponse
import ch.protonmail.libs.auth.model.response.LoginResponse
import ch.protonmail.libs.auth.model.response.ModulusResponse

/**
 * Specifications for Auth API
 * Inherits the Core-Data (API) public service specs.
 *
 * @author Davide Farella
 */
internal interface AuthenticationSpec :
    ProtonPublicServiceSpec {

    suspend fun login(
        username: String,
        srpSession: String,
        clientEphemeral: ByteArray,
        clientProof: ByteArray,
        twoFactor: String?
    ): LoginResponse

    suspend fun loginInfo(username: String): LoginInfoResponse

    suspend fun loginInfoForAuthentication(username: String): LoginInfoResponse

    suspend fun randomModulus(): ModulusResponse
}
