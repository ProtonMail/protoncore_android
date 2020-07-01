package ch.protonmail.libs.auth.api

import ch.protonmail.libs.auth.model.request.UpgradePasswordBody
import ch.protonmail.libs.auth.model.response.UserSettingsResponse

internal interface UserSettingsService {

    suspend fun fetchUserSettings(): UserSettingsResponse

    //suspend fun updateNotify(updateNotify: Boolean): ResponseBody

    //suspend fun updateNotificationEmail(srpSession: String, clientEpheremal: String, clientProof: String, twoFactorCode: String?, email: String): SrpResponseBody

    //suspend fun updateLoginPassword(passwordChangeBody: PasswordChange): SrpResponseBody

    suspend fun upgradeLoginPassword(upgradePasswordBody: UpgradePasswordBody)
}
