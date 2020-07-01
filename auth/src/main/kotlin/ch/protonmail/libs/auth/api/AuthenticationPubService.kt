package ch.protonmail.libs.auth.api

import ch.protonmail.libs.auth.model.request.LoginBody
import ch.protonmail.libs.auth.model.request.LoginInfoBody
import ch.protonmail.libs.auth.model.response.LoginInfoResponse
import ch.protonmail.libs.auth.model.response.LoginResponse
import ch.protonmail.libs.auth.model.response.ModulusResponse
import me.proton.android.core.data.api.JSON_CONTENT_TYPE
import me.proton.android.core.data.api.PM_ACCEPT_HEADER_V1
import me.proton.android.core.data.api.service.ProtonPublicService
import retrofit2.http.*

/**
 * Service definition for Auth API.
 * Inherits from Core-Data service definition.
 *
 * @author Davide Farella
 */
internal interface AuthenticationPubService :
    ProtonPublicService {

    @POST("auth")
    @Headers(
        JSON_CONTENT_TYPE,
        PM_ACCEPT_HEADER_V1
    )
    suspend fun login(@Body loginBody: LoginBody): LoginResponse

    @GET("auth/modulus")
    @Headers(
        JSON_CONTENT_TYPE,
        PM_ACCEPT_HEADER_V1
    )
    suspend fun randomModulus(): ModulusResponse

    @POST("auth/info")
    @Headers(
        JSON_CONTENT_TYPE,
        PM_ACCEPT_HEADER_V1
    )
    suspend fun loginInfo(@Body infoBody: LoginInfoBody, @Tag authTag: AuthTag? = null): LoginInfoResponse
}
