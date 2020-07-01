package ch.protonmail.libs.auth.api

import ch.protonmail.libs.auth.model.request.KeysSetupBody
import ch.protonmail.libs.auth.model.response.UserInfo

internal interface KeySpec {

    // suspend fun getPublicKeys(email: String): PublicKeyResponse

    // suspend fun getPublicKeys(emails: Collection<String>): Map<String, PublicKeyResponse?>

    // suspend fun updatePrivateKeys(body: SinglePasswordChange): ResponseBody

    suspend fun setupKeys(keysSetupBody: KeysSetupBody): UserInfo
}
