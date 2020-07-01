package ch.protonmail.libs.auth.api

import ch.protonmail.libs.auth.model.request.PasswordVerifier
import ch.protonmail.libs.auth.model.response.KeySalts
import ch.protonmail.libs.auth.model.response.UserInfo
import okhttp3.ResponseBody

internal interface UserSpec {

    suspend fun fetchUserInfo() : UserInfo

    suspend fun fetchKeySalts() : KeySalts

    // suspend fun fetchHumanVerificationOptions() : HumanVerifyOptionsResponse

    // suspend fun postHumanVerification(body: PostHumanVerificationBody): ResponseBody?

    suspend fun createUser(username: String, password: PasswordVerifier, updateMe: Boolean, tokenType: String, token: String): UserInfo

    // suspend fun sendVerificationCode(verificationCodeBody: VerificationCodeBody): ResponseBody

    suspend fun isUsernameAvailable(username: String): ResponseBody

    // suspend fun fetchDirectEnabled(): DirectEnabledResponse
}
