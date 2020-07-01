package ch.protonmail.libs.auth.api

import me.proton.android.core.data.api.entity.response.RefreshResponse
import ch.protonmail.libs.auth.model.response.LoginInfoResponse
import ch.protonmail.libs.auth.model.response.LoginResponse
import ch.protonmail.libs.auth.model.response.ModulusResponse
import me.proton.android.core.data.api.entity.request.RefreshBody

internal class AuthenticationApi : AuthenticationSpec {

    override suspend fun login(
        username: String,
        srpSession: String,
        clientEphemeral: ByteArray,
        clientProof: ByteArray,
        twoFactor: String?
    ): LoginResponse {
        TODO("not implemented")
    }

    override suspend fun loginInfo(username: String): LoginInfoResponse {
        TODO("not implemented")
    }

    override suspend fun loginInfoForAuthentication(username: String): LoginInfoResponse {
        TODO("not implemented")
    }

    override suspend fun randomModulus(): ModulusResponse {
        TODO("not implemented")
    }

    override suspend fun refreshSync(refreshBody: RefreshBody): RefreshResponse {
        TODO("Not yet implemented")
    }
}
